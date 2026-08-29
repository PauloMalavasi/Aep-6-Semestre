# Diagramas do EcoDescarte

Este documento registra a arquitetura atual da Prova de Conceito. Os diagramas acompanham o código existente e devem ser atualizados quando a estrutura do backend mudar.

## Arquitetura em camadas

```mermaid
flowchart LR
    Cliente[Cliente HTTP / Postman] --> Controller[ControllerPontoColeta]
    Controller --> Service[ServicePontoColeta]
    Service --> Repository[RepositoryPontoColeta]
    Repository --> MongoDB[(MongoDB)]
    MongoDB --> Collection[(pontos_coleta)]
```

O Controller recebe as requisições HTTP. O Service organiza os casos de uso. O Repository executa as operações na coleção `pontos_coleta` do MongoDB.

## Diagrama de classes

```mermaid
classDiagram
    class PontoColeta {
        -String id
        -String nome
        -String telefone
        -Endereco endereco
        -List~String~ residuos
    }

    class Endereco {
        -String logradouro
        -String numero
        -String bairro
        -String cep
        -String cidade
        -String uf
    }

    class RepositoryPontoColeta {
        <<interface>>
        +findByResiduos(String residuos) List~PontoColeta~
        +buscarPorNome(String nome) List~PontoColeta~
    }

    class ServicePontoColeta {
        +cadastrar(PontoColeta ponto) PontoColeta
        +listarPontos() List~PontoColeta~
        +buscarPorNome(String nome) List~PontoColeta~
        +buscarPorResiduo(String residuo) List~PontoColeta~
        +buscaPontoId(String id) PontoColeta
        +deletar(String id) void
        +atualizar(String id, PontoColeta dados) PontoColeta
    }

    class ControllerPontoColeta {
        +createPontoColeta(PontoColeta ponto) PontoColeta
        +listarPontos() List~PontoColeta~
        +buscarPorNome(String nome) List~PontoColeta~
        +buscarPorResiduo(String nome) List~PontoColeta~
        +buscaPontoId(String id) PontoColeta
        +deletar(String id) void
        +atualizar(String id, PontoColeta dados) PontoColeta
    }

    PontoColeta *-- Endereco
    RepositoryPontoColeta ..> PontoColeta
    ServicePontoColeta --> RepositoryPontoColeta
    ControllerPontoColeta --> ServicePontoColeta
```

## Modelo do documento MongoDB

```mermaid
flowchart TB
    Documento[pontos_coleta]
    Documento --> Id[id: ObjectId]
    Documento --> Nome[nome: string]
    Documento --> Telefone[telefone: string]
    Documento --> EnderecoDoc[endereco: documento incorporado]
    Documento --> Residuos[residuos: lista de strings]
    EnderecoDoc --> Logradouro[logradouro]
    EnderecoDoc --> Numero[numero]
    EnderecoDoc --> Bairro[bairro]
    EnderecoDoc --> Cep[cep]
    EnderecoDoc --> Cidade[cidade]
    EnderecoDoc --> Uf[uf]
```

O endereço é incorporado ao documento do ponto de coleta. Portanto, não existe uma coleção separada para endereços ou resíduos nesta etapa da PoC.
