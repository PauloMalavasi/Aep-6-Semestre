# EcoDescarte

## Sobre o projeto

O EcoDescarte é uma Prova de Conceito desenvolvida para a AEP do 6º semestre de Engenharia de Software. A aplicação ajuda usuários a localizar pontos que recebem resíduos como eletrônicos, pilhas, baterias, papel, plástico, vidro e metal.

O projeto está relacionado principalmente aos seguintes Objetivos de Desenvolvimento Sustentável da ONU:

- **ODS 11 — Cidades e Comunidades Sustentáveis**;
- **ODS 12 — Consumo e Produção Responsáveis**.

## Funcionalidades

- Cadastrar um ponto de coleta;
- Listar os pontos cadastrados;
- Buscar um ponto pelo identificador;
- Buscar pontos pelo nome;
- Buscar pontos pelo resíduo aceito;
- Atualizar um ponto de coleta;
- Excluir um ponto de coleta.

O projeto também possui uma interface web responsiva para usar essas funcionalidades sem precisar montar as requisições manualmente.

## Tecnologias

- Java 21;
- Spring Boot 4.1.1;
- Spring Web MVC;
- Spring Data MongoDB;
- MongoDB;
- Maven Wrapper;
- JUnit 5;
- Mockito e MockMvc;
- JaCoCo;
- Springdoc OpenAPI.
- HTML, CSS e JavaScript.

## Organização

O backend está na pasta `6s` e utiliza uma arquitetura em camadas:

- `models`: documentos armazenados no MongoDB;
- `repositories`: consultas e persistência;
- `services`: casos de uso da aplicação;
- `controllers`: endpoints HTTP;
- `configuration`: configuração do OpenAPI;
- `src/test`: testes automatizados.

A documentação dos diagramas está em [`docs/diagramas.md`](docs/diagramas.md).

## Banco de dados

A PoC utiliza somente a coleção homogênea `pontos_coleta`. Cada documento possui:

- `nome`;
- `telefone`;
- `endereco`, composto por logradouro, número, bairro, CEP, cidade e UF;
- `residuos`, uma lista com os materiais aceitos.

O script [`6s/mongodb/init.js`](6s/mongodb/init.js) cria a coleção e configura sua validação. Com o MongoDB em execução, inicialize o banco na raiz do repositório:

```powershell
mongosh "mongodb://localhost:27017" --file .\6s\mongodb\init.js
```

A aplicação utiliza a conexão `mongodb://localhost:27017/ecodescarte`, configurada em `application.properties`.

## Como executar

### Pré-requisitos

- Git;
- JDK 21 com `JAVA_HOME` e `Path` configurados;
- MongoDB e MongoDB Shell (`mongosh`).

O Maven não precisa ser instalado separadamente, pois o projeto inclui o Maven Wrapper.

### Clonar e iniciar

```powershell
git clone https://github.com/PauloMalavasi/Aep-6-Semestre.git
cd .\Aep-6-Semestre\6s
.\mvnw.cmd spring-boot:run
```

A interface web ficará disponível em `http://localhost:8080` e a API em `http://localhost:8080/ponto_coleta`.

### Como usar a interface

1. Inicie o MongoDB local.
2. Execute a aplicação com o comando mostrado acima.
3. Abra `http://localhost:8080` no navegador.
4. Use **Novo ponto** para cadastrar um local de coleta.
5. Pesquise pelo nome do ponto ou pelo resíduo aceito.
6. Use os botões dos cartões para editar ou excluir um cadastro.

A interface se adapta a computadores, tablets e celulares. Caso o MongoDB não esteja em execução, a página ainda abre, mas exibirá um aviso ao tentar carregar ou alterar os pontos.

## Endpoints

| Método | Caminho | Operação |
| --- | --- | --- |
| `POST` | `/ponto_coleta` | Cadastrar ponto |
| `GET` | `/ponto_coleta` | Listar pontos |
| `GET` | `/ponto_coleta/{id}` | Buscar por ID |
| `GET` | `/ponto_coleta/nome-ponto?nome={nome}` | Buscar por nome |
| `GET` | `/ponto_coleta/nome-residuo?nome={residuo}` | Buscar por resíduo |
| `PUT` | `/ponto_coleta/{id}` | Atualizar ponto |
| `DELETE` | `/ponto_coleta/{id}` | Excluir ponto |

Exemplo de corpo para cadastro ou atualização:

```json
{
  "nome": "Eco Ponto Centro",
  "telefone": "(44) 99999-9999",
  "endereco": {
    "logradouro": "Avenida Brasil",
    "numero": "100",
    "bairro": "Centro",
    "cep": "87000-000",
    "cidade": "Maringá",
    "uf": "PR"
  },
  "residuos": ["ELETRONICO", "PILHA", "BATERIA"]
}
```

## Testes e cobertura

Os testes de Service e Controller usam Mockito e MockMvc. Eles não precisam de uma instalação local do MongoDB.

Para executar os testes:

```powershell
cd .\6s
.\mvnw.cmd test
```

Para executar a verificação completa, gerar o relatório e validar o mínimo de 70%:

```powershell
.\mvnw.cmd clean verify
```

O relatório HTML será gerado em:

```text
6s/target/site/jacoco/index.html
```

Na validação mais recente foram executados 19 testes, sem falhas, mantendo a cobertura mínima configurada no JaCoCo.
