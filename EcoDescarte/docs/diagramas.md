# Diagramas do EcoDescarte

Este documento registra a arquitetura atual da Prova de Conceito. Os diagramas acompanham o código existente e devem ser atualizados quando a estrutura do backend mudar.

## Arquitetura em camadas

```mermaid
flowchart LR
    Cliente[Interface HTML CSS JavaScript / Swagger] --> Controller[ControllerPontoColeta]
    Controller --> Validacao[Bean Validation e ApiExceptionHandler]
    Controller --> Service[ServicePontoColeta]
    Service --> Repository[RepositoryPontoColeta]
    Repository --> MongoDB[(MongoDB)]
    MongoDB --> Collection[(pontos_coleta)]
    Cliente --> ControllerOrg[ControllerOrganizacao]
    ControllerOrg --> ServiceOrg[ServiceOrganizacao]
    ServiceOrg --> RepositoryOrg[RepositoryOrganizacao]
    RepositoryOrg --> MongoDB
    MongoDB --> Organizacoes[(organizacoes)]
    Service --> RepositoryOrg
    ServiceOrg --> Repository
```

Os Controllers recebem HTTP e validam os dados. Os Services organizam os casos de uso e verificam os vínculos; os Repositories persistem nas coleções `pontos_coleta` e `organizacoes` do MongoDB.

O frontend é servido pelo próprio Spring Boot. O Controller valida os dados de entrada, e `ApiExceptionHandler` devolve erros em JSON com status 400, 404 ou 503. `DadosIniciaisConfiguration` insere três registros fictícios ausentes na inicialização; essa carga pode ser desativada por configuração.

## Diagrama de classes

```mermaid
classDiagram
    class PontoColeta {
        -String id
        -String nome
        -String telefone
        -String organizacaoId
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
        +createPontoColeta(PontoColeta ponto) ResponseEntity~PontoColeta~
        +listarPontos() List~PontoColeta~
        +buscarPorNome(String nome) List~PontoColeta~
        +buscarPorResiduo(String nome) List~PontoColeta~
        +buscaPontoId(String id) PontoColeta
        +deletar(String id) void
        +atualizar(String id, PontoColeta dados) PontoColeta
    }

    PontoColeta *-- Endereco
    class Organizacao {
        -String id
        -String nome
    }
    Organizacao "1" <-- "0..*" PontoColeta : organizacaoId
    RepositoryPontoColeta ..> PontoColeta
    ServicePontoColeta --> RepositoryPontoColeta
    ControllerPontoColeta --> ServicePontoColeta
```

## Modelo do documento MongoDB

```mermaid
flowchart TB
    Documento[pontos_coleta]
    Documento --> Id[_id: ObjectId ou string]
    Documento --> Nome[nome: string]
    Documento --> Telefone[telefone: string]
    Documento --> Vinculo[organizacaoId: string]
    Vinculo --> OrganizacaoDoc[organizacoes: _id e nome]
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

A organização é armazenada separadamente porque pode responder por vários pontos. O vínculo é obrigatório nos novos cadastros e atualizações, verificado pela API e indexado em `pontos_coleta.organizacaoId`. O endereço incorporado é o objeto complexo exigido pelo enunciado. A API impede excluir organizações com pontos vinculados (409); não há exclusão em cascata. As verificações entre coleções não são transacionais: a PoC demonstra uso local sequencial, sem garantir integridade sob gravações diretas ou concorrentes no banco.

Na API, o identificador é exposto como `id` (string). Cadastros novos recebem um ObjectId gerado pelo MongoDB; os três exemplos iniciais usam strings fixas para permitir a carga repetida sem duplicação. Todos os documentos mantêm a mesma estrutura de negócio.
