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
- Cadastrar organizações responsáveis e vincular cada ponto a uma organização;
- Consultar, atualizar e excluir organizações pela API/Swagger; a exclusão é recusada enquanto houver pontos vinculados.

O projeto também possui uma interface web responsiva para usar essas funcionalidades sem precisar montar as requisições manualmente.

Os três pontos iniciais são **fictícios**, destinados à demonstração acadêmica. A PoC não verifica estabelecimentos reais, não possui geolocalização e deve ser executada localmente: não há autenticação ou controle de acesso nesta etapa.

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
- Springdoc OpenAPI;
- HTML, CSS e JavaScript.
- Node.js 22.16 ou superior e jsdom para os testes JavaScript (não necessários para executar a aplicação).

## Organização

O backend está na pasta `6s` e utiliza uma arquitetura em camadas:

- `models`: documentos armazenados no MongoDB;
- `repositories`: consultas e persistência;
- `services`: casos de uso da aplicação;
- `controllers`: endpoints HTTP;
- `configuration`: configuração do OpenAPI e carga de dados de demonstração;
- `src/test`: testes automatizados.

A documentação dos diagramas está em [`docs/diagramas.md`](docs/diagramas.md).
O roteiro da apresentação e a conferência de entrega estão em [`docs/segunda-entrega.md`](docs/segunda-entrega.md).
O acompanhamento da conclusão da entrega está no [`quadro de tarefas`](docs/quadro-tarefas.md).

## Banco de dados

A PoC utiliza duas coleções relacionadas: `organizacoes` e `pontos_coleta`. Uma organização (`id`, `nome`) pode ser responsável por vários pontos. Cada ponto referencia a organização por `organizacaoId` e possui:

- `nome`;
- `telefone`;
- `organizacaoId`, identificador de uma organização cadastrada;
- `endereco`, composto por logradouro, número, bairro, CEP, cidade e UF;
- `residuos`, uma lista com os materiais aceitos.

O script [`6s/mongodb/init.js`](6s/mongodb/init.js) cria as duas coleções, configura a validação e o índice do vínculo e insere uma organização e três pontos fictícios sem duplicação. Com o MongoDB em execução, inicialize o banco na raiz do repositório:

```powershell
mongosh "mongodb://localhost:27017" --file .\6s\mongodb\init.js
```

A aplicação utiliza a conexão `mongodb://localhost:27017/ecodescarte`, configurada em `application.properties`.

O projeto usa Spring Boot 4, cuja propriedade de conexão é `spring.mongodb.uri`. Para outro servidor/banco, defina a variável `MONGODB_URI`. Se inicializar um banco com outro nome pelo script, defina também `ECODESCARTE_DB` no terminal do `mongosh`, com o mesmo nome da URI.

O endereço fica incorporado em cada ponto. O identificador `_id` é gerado nos cadastros e exposto como `id` na API. Os exemplos usam IDs fixos, sem duplicação quando o script ou a aplicação são reiniciados.

O relacionamento é uma referência manual: `pontos_coleta.organizacaoId` guarda a representação textual do ID da organização. A API verifica sua existência ao criar ou atualizar um ponto; excluir uma organização em uso retorna `409 Conflict`. MongoDB não impõe chave estrangeira entre coleções. As verificações são adequadas à demonstração local sequencial; gravações diretas no banco ou alterações simultâneas entre coleções exigem controles adicionais, como transações em replica set.

**Atualização de um banco da primeira versão:** execute o script antes de iniciar a aplicação. Ele completa o vínculo ausente dos três exemplos conhecidos, preservando os demais campos e vínculos já existentes. Cadastros próprios antigos não recebem um responsável inventado: o script informa quantos estão sem organização, e a equipe deve editá-los na interface e selecionar o responsável correto.

Na inicialização, a aplicação recria a organização de demonstração e qualquer um dos três pontos de exemplo que estiver ausente. Para manter excluído um exemplo após reiniciar, defina `ECODESCARTE_DADOS_INICIAIS=false`. Essa opção controla apenas a carga da aplicação; o script de inicialização sempre verifica os exemplos. Os validadores do banco são instalados pelo script, por isso ele deve ser executado antes da aplicação.

## Como executar

### Pré-requisitos

- Git;
- JDK 21 com `JAVA_HOME` e `Path` configurados;
- MongoDB e MongoDB Shell (`mongosh`).

Os comandos abaixo usam PowerShell. Para o script opcional de verificação da API, use PowerShell 7 (`pwsh`). Confirme que `$env:JAVA_HOME` aponta para o JDK 21; em máquinas com várias versões do Java, o Maven Wrapper utiliza esse caminho.

O Maven não precisa ser instalado separadamente, pois o projeto inclui o Maven Wrapper.

### Clonar e iniciar

```powershell
git clone https://github.com/PauloMalavasi/Aep-6-Semestre.git
cd .\Aep-6-Semestre\6s
mongosh "mongodb://localhost:27017" --file .\mongodb\init.js
.\mvnw.cmd spring-boot:run
```

A interface web ficará disponível em `http://localhost:8080` e a API em `http://localhost:8080/ponto_coleta`.

Swagger interativo: `http://localhost:8080/docs`. Especificação OpenAPI: `http://localhost:8080/v3/api-docs`.

No Linux/macOS, use `bash ./mvnw spring-boot:run` dentro de `6s`. Após gerar o pacote, também é possível executar `java -jar target/6s-0.0.1-SNAPSHOT.jar` com Java 21.

### Como usar a interface

1. Inicie o MongoDB local.
2. Execute a aplicação com o comando mostrado acima.
3. Abra `http://localhost:8080` no navegador.
4. Use **Novo ponto** para cadastrar um local de coleta.
5. Selecione uma organização responsável no formulário. Se necessário, use **Cadastrar organização**; o novo cadastro será selecionado automaticamente. Pesquise pontos por parte do nome (por exemplo, `Centro`) ou pelo resíduo aceito (por exemplo, `pilha` ou `plástico`). A busca por resíduo aceita acentos; a busca por nome ignora maiúsculas/minúsculas, mas preserva acentos.
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
| `GET` | `/organizacoes` | Listar organizações |
| `GET` | `/organizacoes/{id}` | Consultar organização |
| `POST` | `/organizacoes` | Cadastrar organização |
| `PUT` | `/organizacoes/{id}` | Atualizar organização |
| `DELETE` | `/organizacoes/{id}` | Excluir organização sem pontos vinculados |

Respostas: `201 Created` e cabeçalho `Location` no cadastro; `200 OK` nas consultas/atualizações; `204 No Content` na exclusão; `400 Bad Request` para dados ou buscas inválidas; `404 Not Found` para ID inexistente; `503 Service Unavailable` quando o banco está indisponível. Buscas válidas sem resultados retornam `200` com `[]`.

POST sempre cria um documento novo; um ID fornecido pelo cliente é ignorado. PUT substitui os dados de um ponto existente e mantém seu ID. Os campos são obrigatórios, telefone deve incluir DDD, CEP deve ter oito dígitos e UF deve ser válida. É necessário informar entre um e oito resíduos com os códigos permitidos. Resíduos válidos: `ELETRONICO`, `PILHA`, `BATERIA`, `PAPEL`, `PLASTICO`, `VIDRO`, `METAL` e `ORGANICO`.

Exemplo de corpo para cadastro ou atualização:

```json
{
  "nome": "Eco Ponto Centro",
  "telefone": "(44) 99999-9999",
  "organizacaoId": "organizacao-demonstracao",
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

Os testes de Service e Controller usam Mockito e MockMvc. Os testes de aplicação iniciam um servidor HTTP em uma porta livre e verificam a interface, o Swagger e a configuração do banco. A carga de exemplos é desativada nesses testes: a suíte Maven não exige MongoDB em execução e não grava dados. Em ambientes restritos, é necessário permitir a abertura dessa porta local temporária.

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

O JaCoCo verifica um mínimo de 70% de cobertura de instruções Java. Essa métrica não mede o JavaScript nem substitui o teste com o banco real. A configuração do Mockito já inclui o agente necessário ao Java 21. Os resultados da preparação da entrega estão em [`docs/validacao.md`](docs/validacao.md).

Na raiz do repositório, com Node.js 22.16 ou superior, execute também:

```powershell
npm ci --ignore-scripts
npm run test:report
```

Os testes usam o HTML e o JavaScript reais com DOM simulado por jsdom, cobrindo cadastro, edição, exclusão, organizações, buscas, respostas fora de ordem e erros. Também verificam a idempotência e a preservação de dados do script `mongodb/init.js` com um dublê do shell. A validação real do MongoDB é conferida separadamente. As requisições HTTP são simuladas nesses testes JavaScript.

O Node mede linhas, funções e ramificações dos dois arquivos JavaScript de produção, exigindo pelo menos 70% em cada métrica do conjunto. A saída exibe a cobertura por arquivo; o relatório LCOV fica em `coverage/javascript.lcov`. Java e JavaScript têm relatórios separados, sem misturar percentuais de métricas diferentes. HTML/CSS declarativos e os próprios testes não integram a medição. Execute ambos os comandos de verificação para avaliar o código da PoC.

Com a aplicação e o MongoDB em execução, rode na raiz (PowerShell 7):

```powershell
pwsh -File .\scripts\verificar-api.ps1
```

Esse script exercita o CRUD HTTP, buscas, validações e respostas de erro. Cria registros com identificador de teste e tenta removê-los ao finalizar, preservando os demais dados. Para outra porta, passe `-BaseUrl http://localhost:8081`.

A configuração em `.github/workflows/ci.yml` executa as suítes Java e JavaScript em pushes e pull requests e publica os relatórios como artefatos da execução.

## Problemas de execução

- **Java incompatível:** confirme o JDK 21 em `JAVA_HOME` e execute `.\mvnw.cmd -version`.
- **Banco indisponível:** inicie o MongoDB, confira `MONGODB_URI` e execute o script de inicialização no mesmo banco.
- **Dados antigos no banco `test`:** versões anteriores usavam uma propriedade de conexão incorreta. A aplicação agora usa `ecodescarte`; os dados de `test` não são apagados nem migrados automaticamente.
- **Porta 8080 ocupada:** inicie com `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"` e use essa porta no navegador.
- **Primeira compilação:** requer acesso à internet para baixar Maven e dependências; nas próximas execuções, eles ficam em cache.
