// ECODESCARTE_DB permite testar em um banco separado; o padrão é ecodescarte.
db = db.getSiblingDB(process.env.ECODESCARTE_DB || "ecodescarte");

const organizacaoValidator = { $jsonSchema: {
    bsonType: "object", required: ["nome"],
    properties: { nome: { bsonType: "string", minLength: 1, maxLength: 100, pattern: "\\S" } }
} };
if (db.getCollectionNames().includes("organizacoes")) {
    const resultado = db.runCommand({ collMod: "organizacoes", validator: organizacaoValidator,
        validationLevel: "strict", validationAction: "error" });
    if (resultado.ok !== 1) throw new Error("Não foi possível validar organizações.");
} else {
    db.createCollection("organizacoes", { validator: organizacaoValidator,
        validationLevel: "strict", validationAction: "error" });
}
db.organizacoes.updateOne({ _id: "organizacao-demonstracao" },
    { $setOnInsert: { nome: "Rede EcoDescarte (fictícia)" } }, { upsert: true });

const validator = {
    $jsonSchema: {
        bsonType: "object",
        required: ["nome", "telefone", "endereco", "residuos", "organizacaoId"],
        properties: {
            organizacaoId: { bsonType: "string", minLength: 1, maxLength: 100, pattern: "\\S" },
            nome: {
                bsonType: "string", minLength: 1, maxLength: 100, pattern: "\\S"
            },
            telefone: {
                bsonType: "string", pattern: "^(?:\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}|\\d{10,11})$"
            },
            endereco: {
                bsonType: "object",
                required: [
                    "logradouro",
                    "numero",
                    "bairro",
                    "cep",
                    "cidade",
                    "uf"
                ],
                properties: {
                    logradouro: {
                        bsonType: "string", minLength: 1, maxLength: 150, pattern: "\\S"
                    },
                    numero: {
                        bsonType: "string", minLength: 1, maxLength: 10, pattern: "\\S"
                    },
                    bairro: {
                        bsonType: "string", minLength: 1, maxLength: 100, pattern: "\\S"
                    },
                    cep: {
                        bsonType: "string", pattern: "^\\d{5}-?\\d{3}$"
                    },
                    cidade: {
                        bsonType: "string", minLength: 1, maxLength: 100, pattern: "\\S"
                    },
                    uf: {
                        enum: ["AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO"]
                    }
                }
            },
            residuos: {
                bsonType: "array",
                minItems: 1,
                maxItems: 8,
                items: {
                    enum: [
                        "ELETRONICO",
                        "PILHA",
                        "BATERIA",
                        "PAPEL",
                        "PLASTICO",
                        "VIDRO",
                        "METAL",
                        "ORGANICO"
                    ]
                }
            }
        }
    }
};

if (db.getCollectionNames().includes("pontos_coleta")) {

    const resultado = db.runCommand({
        collMod: "pontos_coleta",
        validator: validator,
        validationLevel: "strict",
        validationAction: "error"
    });
    if (resultado.ok !== 1) throw new Error("Não foi possível aplicar o validador: " + JSON.stringify(resultado));

    print("Colecao pontos_coleta atualizada.");

} else {

    db.createCollection("pontos_coleta", {
        validator: validator,
        validationLevel: "strict",
        validationAction: "error"
    });

    print("Colecao pontos_coleta criada.");
}

// Dados fictícios para demonstração acadêmica; não são locais verificados.
const pontosIniciais = [
    {
        _id: "ponto-inicial-maringa-centro",
        nome: "Eco Ponto Centro",
        telefone: "(44) 3221-1234",
        endereco: {
            logradouro: "Avenida Brasil",
            numero: "1000",
            bairro: "Zona 1",
            cep: "87013-000",
            cidade: "Maringá",
            uf: "PR"
        },
        residuos: ["ELETRONICO", "PILHA", "BATERIA"]
    },
    {
        _id: "ponto-inicial-maringa-zona-7",
        nome: "Eco Ponto Zona 7",
        telefone: "(44) 3221-5678",
        endereco: {
            logradouro: "Avenida Mandacaru",
            numero: "2500",
            bairro: "Zona 7",
            cep: "87080-000",
            cidade: "Maringá",
            uf: "PR"
        },
        residuos: ["PAPEL", "PLASTICO", "VIDRO", "METAL"]
    },
    {
        _id: "ponto-inicial-jandaia-centro",
        nome: "Eco Ponto Jandaia do Sul",
        telefone: "(43) 3432-1234",
        endereco: {
            logradouro: "Avenida Getúlio Vargas",
            numero: "500",
            bairro: "Centro",
            cep: "86900-000",
            cidade: "Jandaia do Sul",
            uf: "PR"
        },
        residuos: ["ELETRONICO", "PILHA", "PAPEL", "PLASTICO"]
    }
];

pontosIniciais.forEach(ponto => {
    ponto.organizacaoId = "organizacao-demonstracao";
    // Migra apenas o vínculo ausente dos exemplos conhecidos; preserva os demais dados.
    db.pontos_coleta.updateOne({ _id: ponto._id, organizacaoId: { $exists: false } },
        { $set: { organizacaoId: ponto.organizacaoId } });
    db.pontos_coleta.updateOne(
        { _id: ponto._id },
        { $setOnInsert: ponto },
        { upsert: true }
    );
});

db.pontos_coleta.createIndex({ organizacaoId: 1 });
const semOrganizacao = db.pontos_coleta.countDocuments({ $or: [
    { organizacaoId: { $exists: false } }, { organizacaoId: null }, { organizacaoId: "" }
] });
if (semOrganizacao) print("ATENÇÃO: " + semOrganizacao + " ponto(s) antigo(s) precisam de organização. Edite-os na interface; nenhum responsável foi atribuído automaticamente.");

print("Pontos iniciais verificados: 2 em Maringa e 1 em Jandaia do Sul.");
