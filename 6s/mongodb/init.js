db = db.getSiblingDB("ecodescarte");

const validator = {
    $jsonSchema: {
        bsonType: "object",
        required: ["nome", "telefone", "endereco", "residuos"],
        properties: {
            nome: {
                bsonType: "string"
            },
            telefone: {
                bsonType: "string"
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
                        bsonType: "string"
                    },
                    numero: {
                        bsonType: "string"
                    },
                    bairro: {
                        bsonType: "string"
                    },
                    cep: {
                        bsonType: "string"
                    },
                    cidade: {
                        bsonType: "string"
                    },
                    uf: {
                        bsonType: "string"
                    }
                }
            },
            residuos: {
                bsonType: "array",
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

    db.runCommand({
        collMod: "pontos_coleta",
        validator: validator,
        validationLevel: "strict",
        validationAction: "error"
    });

    print("Colecao pontos_coleta atualizada.");

} else {

    db.createCollection("pontos_coleta", {
        validator: validator,
        validationLevel: "strict",
        validationAction: "error"
    });

    print("Colecao pontos_coleta criada.");
}

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
    db.pontos_coleta.updateOne(
        { _id: ponto._id },
        { $setOnInsert: ponto },
        { upsert: true }
    );
});

print("Pontos iniciais verificados: 2 em Maringa e 1 em Jandaia do Sul.");
