# EcoDescarte

Projeto acadêmico da AEP do 6º semestre de Engenharia de Software, voltado ao cadastro e à consulta de pontos de coleta de resíduos. Relaciona-se aos **ODS 11 e 12**.

## Funcionalidades

- Cadastrar, listar, buscar, editar e excluir pontos de coleta.
- Pesquisar por nome e por tipo de resíduo.
- Cadastrar e gerenciar organizações responsáveis pelos pontos.
- Utilizar as funcionalidades por uma interface web responsiva ou pela API REST.

## Tecnologias

Java 21, Spring Boot, MongoDB, Maven, HTML, CSS e JavaScript. Testes com JUnit, Mockito, MockMvc, JaCoCo e Node.js.

## Banco de dados e evolução

Na primeira entrega, a aplicação utilizava a coleção `pontos_coleta`. Na segunda, passou a utilizar duas coleções relacionadas:

- `organizacoes`: dados das organizações responsáveis.
- `pontos_coleta`: nome, telefone, endereço, resíduos aceitos e `organizacaoId`.

A segunda entrega também acrescentou validações, tratamento de erros e mais testes. O sistema possui três pontos fictícios para demonstração, inseridos automaticamente na inicialização quando o MongoDB está disponível. O script abaixo prepara as coleções e suas regras de validação.

## Como executar no Windows

**Pré-requisitos:** JDK 21 (`JAVA_HOME` configurado), MongoDB Server em execução, MongoDB Shell (`mongosh`) e Git.

No PowerShell:

```powershell
git clone https://github.com/PauloMalavasi/Aep-6-Semestre.git
cd .\Aep-6-Semestre
mongosh "mongodb://localhost:27017" --file .\6s\mongodb\init.js
cd .\6s
.\mvnw.cmd spring-boot:run
```

Acesse:

- **Aplicação:** http://localhost:8080
- **API de pontos:** http://localhost:8080/ponto_coleta
- **Documentação da API (Swagger):** http://localhost:8080/docs

## Testes e cobertura

Na pasta `6s`, execute:

```powershell
.\mvnw.cmd clean verify
```

O relatório de cobertura fica em `6s/target/site/jacoco/index.html` (caminho a partir da raiz do repositório).

Na validação documentada em **31/10/2026**, foram registrados **65 testes Java aprovados** e **99,23% de cobertura de instruções Java**, acima do mínimo de 70% exigido na AEP. Para os testes JavaScript, na raiz do repositório, com Node.js instalado:

```powershell
npm ci --ignore-scripts
npm run test:report
```

## Documentação

- [Diagramas](docs/diagramas.md)
- [Validação e resultados dos testes](docs/validacao.md)
- [Quadro de tarefas](docs/quadro-tarefas.md)
- [Segunda entrega](docs/segunda-entrega.md)
