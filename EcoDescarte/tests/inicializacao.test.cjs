const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const filename = path.resolve('6s/mongodb/init.js');
const script = new vm.Script(fs.readFileSync(filename, 'utf8'), { filename });

// Dublê do shell para verificar idempotência e preservação de dados.
// A aceitação/rejeição pelo validador é verificada separadamente com MongoDB real.
function banco(iniciais = []) {
    const colecoes = new Map();
    const mensagens = [];
    const db = {
        getSiblingDB: () => db,
        getCollectionNames: () => [...colecoes.keys()],
        createCollection(nome, regras) {
            const documentos = [];
            const collection = {
                documentos, regras,
                updateOne(filtro, atualizacao, opcoes = {}) {
                    let documento = documentos.find(d => d._id === filtro._id &&
                        (!filtro.organizacaoId || !Object.hasOwn(d, 'organizacaoId')));
                    if (!documento && opcoes.upsert) {
                        documento = { _id: filtro._id, ...structuredClone(atualizacao.$setOnInsert) };
                        documentos.push(documento);
                    }
                    if (documento && atualizacao.$set) Object.assign(documento, atualizacao.$set);
                },
                createIndex: () => {},
                countDocuments: () => documentos.filter(d => !d.organizacaoId).length
            };
            db[nome] = collection;
            colecoes.set(nome, collection);
        },
        runCommand: () => ({ ok: 1 })
    };
    if (iniciais.length) {
        db.createCollection('pontos_coleta');
        db.pontos_coleta.documentos.push(...structuredClone(iniciais));
    }
    return { db, mensagens, executar: () => script.runInNewContext({ db,
        process: { env: { ECODESCARTE_DB: 'teste' } }, print: mensagem => mensagens.push(mensagem) }) };
}

test('inicialização repetida mantém duas coleções e vínculos sem duplicar dados', () => {
    const b = banco(); b.executar(); b.executar();
    assert.deepEqual(b.db.getCollectionNames().sort(), ['organizacoes', 'pontos_coleta']);
    assert.equal(b.db.organizacoes.documentos.length, 1);
    assert.equal(b.db.pontos_coleta.documentos.length, 3);
    for (const p of b.db.pontos_coleta.documentos) {
        assert.ok(b.db.organizacoes.documentos.some(o => o._id === p.organizacaoId));
        assert.ok(p.endereco.cidade);
    }
});

test('migração preserva alterações, vínculos escolhidos e cadastros alheios aos exemplos', () => {
    const b = banco([
        { _id: 'ponto-inicial-maringa-centro', nome: 'Nome alterado pela equipe' },
        { _id: 'ponto-inicial-maringa-zona-7', nome: 'Outro', organizacaoId: 'responsavel-real' },
        { _id: 'cadastro-usuario', nome: 'Meu cadastro', endereco: { cidade: 'Outra cidade' } }
    ]);
    b.executar(); b.executar();
    const docs = b.db.pontos_coleta.documentos;
    assert.equal(docs.length, 4);
    assert.equal(docs[0].nome, 'Nome alterado pela equipe');
    assert.equal(docs[0].organizacaoId, 'organizacao-demonstracao');
    assert.equal(docs[1].organizacaoId, 'responsavel-real');
    assert.equal(docs[2].organizacaoId, undefined);
    assert.equal(docs[2].endereco.cidade, 'Outra cidade');
    assert.ok(b.mensagens.some(m => m.includes('1 ponto(s) antigo(s)')));
});

test('falha ao aplicar validador interrompe a inicialização', () => {
    const b = banco(); b.executar();
    b.db.runCommand = () => ({ ok: 0 });
    assert.throws(b.executar, /validar organizações/);
    b.db.runCommand = ({ collMod }) => ({ ok: collMod === 'organizacoes' ? 1 : 0 });
    assert.throws(b.executar, /aplicar o validador/);
});
