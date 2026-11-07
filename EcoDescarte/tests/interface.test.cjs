const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { JSDOM } = require('jsdom');

const scriptPath = path.resolve('6s/src/main/resources/static/js/app.js');
const html = fs.readFileSync('6s/src/main/resources/static/index.html', 'utf8');
const script = new vm.Script(fs.readFileSync(scriptPath, 'utf8'), { filename: scriptPath });
const exemplo = () => ({ id: 'p1', nome: 'Centro', telefone: '(44) 99999-9999', organizacaoId: 'o1',
    endereco: { logradouro: 'Rua A', numero: '100', bairro: 'Centro', cidade: 'Maringá', uf: 'PR', cep: '87000-000' },
    residuos: ['PILHA', 'PLASTICO'] });
const resposta = (body, status = 200) => ({ ok: status < 400, status,
    json: async () => body, text: async () => body == null ? '' : JSON.stringify(body) });
const tick = () => new Promise(resolve => setImmediate(resolve));

async function montar(t, pontos = [exemplo()]) {
    const dom = new JSDOM(html, { url: 'http://localhost', runScripts: 'outside-only' });
    t.after(() => dom.window.close());
    const w = dom.window;
    w.HTMLDialogElement.prototype.showModal = function () { this.open = true; };
    w.HTMLDialogElement.prototype.close = function () { this.open = false; };
    const chamadas = [];
    let handler = (url) => resposta(url === '/organizacoes' ? [{ id: 'o1', nome: 'Associação Centro' }] : pontos);
    w.fetch = async (url, options = {}) => { chamadas.push({ url, ...options }); return handler(url, options); };
    script.runInContext(dom.getInternalVMContext());
    await tick();
    return { w, chamadas, $: id => w.document.querySelector(id), responder: fn => { handler = fn; } };
}

test('lista pontos e responsáveis, conta resíduos e escapa dados externos', async t => {
    const p = exemplo(); p.nome = '<img src=x onerror=alert(1)>'; p.residuos.push('DESCONHECIDO');
    const { $, w } = await montar(t, [p]);
    assert.equal($('#total-points').textContent, '1');
    assert.equal($('#total-residues').textContent, '3');
    assert.match($('#points-grid').textContent, /Associação Centro/);
    assert.equal($('#points-grid img'), null);
    assert.match($('#points-grid').textContent, /DESCONHECIDO/);
    assert.equal($('#points-grid').getAttribute('aria-busy'), 'false');
    assert.match(w.criarCard({}), /Endereço não informado/);
    assert.match(w.criarCard({}), /Sem resíduos informados/);
    assert.equal(w.escapeHtml('&<>"\''), '&amp;&lt;&gt;&quot;&#039;');
    assert.equal(w.escapeHtml(null), '');
});

test('buscas por nome e resíduo, estados vazios e limpeza', async t => {
    const { $, w, responder, chamadas } = await montar(t, []);
    assert.match($('#empty-title').textContent, /Nenhum ponto cadastrado/);
    await w.buscarPontos({ preventDefault() {} });
    assert.equal(w.document.activeElement, $('#search-input'));
    assert.match($('#toast').textContent, /Digite/);
    $('#search-input').value = ' vidro ';
    $('#search-type').value = 'residuo';
    await w.buscarPontos({ preventDefault() {} });
    assert.ok(chamadas.some(c => c.url === '/ponto_coleta/nome-residuo?nome=vidro'));
    assert.equal($('#clear-search').hidden, false);
    assert.match($('#empty-title').textContent, /Resíduo não disponível/);
    $('#search-type').value = 'nome';
    $('#search-input').value = 'nome & acento';
    await w.buscarPontos({ preventDefault() {} });
    assert.ok(chamadas.some(c => c.url.endsWith('nome=nome%20%26%20acento')));
    assert.match($('#empty-title').textContent, /Nenhum ponto encontrado/);
    responder(url => resposta(url === '/organizacoes' ? [] : {}));
    await w.limparBusca();
    assert.equal($('#clear-search').hidden, true);
    assert.equal($('#search-input').value, '');
    assert.equal($('#total-points').textContent, '0');
});

test('erro de rede mostra recuperação; resposta antiga não substitui busca recente', async t => {
    const { $, w, responder } = await montar(t);
    responder(() => { throw new Error('offline'); });
    await w.carregarPontos();
    assert.equal($('#empty-state').hidden, false);
    assert.match($('#empty-title').textContent, /Não foi possível/);
    let finalizar;
    responder(url => url === '/lento' ? new Promise(resolve => { finalizar = resolve; }) : resposta([]));
    const lento = w.carregarPontos('/lento');
    await w.carregarPontos('/rapido', 'Mais recente');
    finalizar(resposta([exemplo()]));
    await lento;
    assert.equal($('#result-label').textContent, 'Mais recente');
    let rejeitar;
    responder(url => url === '/erro-lento' ? new Promise((_, reject) => { rejeitar = reject; }) : resposta([]));
    const erroLento = w.carregarPontos('/erro-lento');
    await w.carregarPontos('/rapido', 'Preservado');
    rejeitar(new Error('antigo'));
    await erroLento;
    assert.equal($('#result-label').textContent, 'Preservado');
});

test('cadastro exige resíduo e envia dados normalizados com organização', async t => {
    const { $, w, responder, chamadas } = await montar(t);
    $('#open-create').click();
    assert.equal($('#point-dialog').open, true);
    await w.salvarPonto({ preventDefault() {} });
    assert.equal($('#residue-error').hidden, false);
    const dados = { nome: ' Novo ponto ', telefone: '(44) 99999-9999', organizacaoId: 'o1',
        logradouro: ' Rua B ', numero: '2', bairro: 'Centro', cep: '87000-000', cidade: 'Maringá', uf: 'pr' };
    for (const [id, valor] of Object.entries(dados)) $('#' + id).value = valor;
    $('input[value="PAPEL"]').checked = true;
    responder((url, options) => resposta(options.method === 'POST' ? { id: 'novo' } : []));
    await w.salvarPonto({ preventDefault() {} });
    const payload = JSON.parse(chamadas.find(c => c.method === 'POST').body);
    assert.equal(payload.nome, 'Novo ponto');
    assert.equal(payload.organizacaoId, 'o1');
    assert.equal(payload.endereco.uf, 'PR');
    assert.deepEqual(payload.residuos, ['PAPEL']);
    assert.equal($('#point-dialog').open, false);
    assert.equal($('#submit-form').disabled, false);
    assert.match($('#toast').textContent, /cadastrado com sucesso/);
});

test('edição preserva ID, preenche vínculo e impede fechar durante gravação', async t => {
    const { $, w, responder, chamadas } = await montar(t);
    $('.edit').click();
    assert.equal($('#nome').value, 'Centro');
    assert.equal($('#organizacaoId').value, 'o1');
    assert.equal($('input[value="PILHA"]').checked, true);
    let finalizar;
    responder((url, options) => options.method === 'PUT' ? new Promise(resolve => { finalizar = resolve; }) : resposta([]));
    const salvar = w.salvarPonto({ preventDefault() {} });
    $('#close-dialog').click();
    assert.equal($('#point-dialog').open, true);
    const cancelar = new w.Event('cancel', { cancelable: true });
    $('#point-dialog').dispatchEvent(cancelar);
    assert.equal(cancelar.defaultPrevented, true);
    finalizar(resposta(exemplo()));
    await salvar;
    assert.equal(chamadas.find(c => c.method === 'PUT').url, '/ponto_coleta/p1');
    assert.match($('#toast').textContent, /atualizado com sucesso/);
    w.abrirEdicao({ id: 'legado' });
    assert.equal($('#organizacaoId').value, '');
    $('#point-dialog').click();
    assert.equal($('#point-dialog').open, false);
});

test('falha ao salvar mantém formulário e apresenta campos inválidos', async t => {
    const { $, w, responder } = await montar(t);
    w.abrirEdicao(exemplo());
    responder(() => resposta({ campos: { organizacaoId: 'Selecione uma organização cadastrada.' } }, 400));
    await w.salvarPonto({ preventDefault() {} });
    assert.equal($('#point-dialog').open, true);
    assert.equal($('#submit-form').disabled, false);
    assert.equal($('#submit-form').textContent, 'Salvar alterações');
    assert.match($('#toast').textContent, /organização cadastrada/);
    $('#cancel-form').click();
    assert.equal($('#point-dialog').open, false);
});

test('exclusão confirma, trata falha e remove o ponto selecionado', async t => {
    const { $, w, responder, chamadas } = await montar(t);
    await w.confirmarExclusao();
    $('#points-grid').click();
    w.tratarAcaoDoCard({ target: { closest: () => ({ dataset: { id: 'ausente' } }) } });
    $('.delete').click();
    assert.equal($('#delete-name').textContent, 'Centro');
    assert.equal($('#delete-dialog').open, true);
    responder(() => resposta({ detail: 'Banco indisponível' }, 503));
    await w.confirmarExclusao();
    assert.match($('#toast').textContent, /Banco indisponível/);
    assert.equal($('#confirm-delete').disabled, false);
    responder((url, options) => resposta(options.method === 'DELETE' ? null : [], options.method === 'DELETE' ? 204 : 200));
    await w.confirmarExclusao();
    assert.ok(chamadas.some(c => c.url === '/ponto_coleta/p1' && c.method === 'DELETE'));
    assert.equal($('#delete-dialog').open, false);
    assert.match($('#toast').textContent, /excluído/);
    $('#delete-dialog').showModal(); $('#delete-dialog').click();
    assert.equal($('#delete-dialog').open, false);
    $('#delete-dialog').showModal(); $('#cancel-delete').click();
    assert.equal($('#delete-dialog').open, false);
});

test('cadastro de organização seleciona o novo vínculo e informa erros no diálogo', async t => {
    const { $, w, responder, chamadas } = await montar(t);
    $('#open-create').click(); $('#open-organization').click();
    assert.equal($('#organization-dialog').open, true);
    $('#organization-name').value = ' Associação nova ';
    responder(() => resposta({ detail: 'Nome inválido' }, 400));
    await w.salvarOrganizacao({ preventDefault() {} });
    assert.equal($('#organization-error').hidden, false);
    assert.equal($('#organization-error').textContent, 'Nome inválido');
    assert.equal($('#organization-dialog').open, true);
    let finalizar;
    responder(() => new Promise(resolve => { finalizar = resolve; }));
    const salvar = w.salvarOrganizacao({ preventDefault() {} });
    $('#cancel-organization').click();
    const evento = new w.Event('cancel', { cancelable: true });
    $('#organization-dialog').dispatchEvent(evento);
    assert.equal(evento.defaultPrevented, true);
    assert.equal($('#organization-dialog').open, true);
    finalizar(resposta({ id: 'nova', nome: 'Associação nova' }, 201));
    await salvar;
    assert.equal($('#organizacaoId').value, 'nova');
    assert.equal($('#organization-dialog').open, false);
    assert.equal($('#organization-error').hidden, true);
    assert.equal(JSON.parse(chamadas.at(-1).body).nome, 'Associação nova');
    $('#open-organization').click(); $('#cancel-organization').click();
    assert.equal($('#organization-dialog').open, false);
});

test('máscaras de telefone, CEP e UF funcionam pelos eventos de entrada', async t => {
    const { $, w } = await montar(t);
    for (const [id, entrada, esperado] of [
        ['cep', '870001231234', '87000-123'], ['telefone', '4432221234', '(44) 3222-1234'],
        ['telefone', '44999991234', '(44) 99999-1234'], ['uf', 'p1rx', 'PR']]) {
        $('#' + id).value = entrada;
        $('#' + id).dispatchEvent(new w.Event('input'));
        assert.equal($('#' + id).value, esperado);
    }
});

test('requisição interpreta respostas vazias, JSON e diferentes formatos de erro', async t => {
    const { w, responder } = await montar(t);
    responder(() => resposta(null));
    assert.equal(await w.requisicao('/vazio'), null);
    for (const [corpo, mensagem] of [[{ message: 'mensagem' }, 'mensagem'], [{ error: 'erro' }, 'erro'], [{}, 'A API retornou um erro.']]) {
        responder(() => resposta(corpo, 400));
        await assert.rejects(w.requisicao('/erro'), { message: mensagem });
    }
    responder(() => ({ ok: false, json: async () => { throw new Error('não JSON'); } }));
    await assert.rejects(w.requisicao('/erro'), /A API retornou um erro/);
});
