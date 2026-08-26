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