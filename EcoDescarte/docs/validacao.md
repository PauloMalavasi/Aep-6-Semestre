# Registro de validação — 31/10/2026

Preparação baseada na `master` de commit `7510648`, na branch `codex/preparacao-segunda-entrega`. Este registro documenta verificações técnicas executadas; não substitui a rubrica da disciplina.

## Compilação e testes Java

Comando executado em `6s`: `./mvnw.cmd clean verify`.

- Resultado: **BUILD SUCCESS**.
- 65 testes executados, 0 falhas, 0 erros e 0 ignorados.
- Cobertura de instruções Java: **99,23%** (644 cobertas de 649).
- Cobertura de linhas Java: **98,83%** (169 cobertas de 171).
- Cobertura de ramificações Java: **100%** (18 de 18).
- Mínimo configurado: 70% de instruções, verificado pelo JaCoCo.
- Relatórios reproduzíveis: `6s/target/surefire-reports/` e `6s/target/site/jacoco/index.html`.

A suíte inclui validações de campos obrigatórios, telefone, endereço, resíduos, JSON inválido, IDs inexistentes, indisponibilidade do banco, proteção contra sobrescrita por POST e tratamento literal de buscas. Também inicia o contexto Spring e verifica a página inicial e o Swagger via HTTP. A suíte foi executada sem MongoDB na porta padrão 27017.

No ambiente de validação, a primeira execução do teste com servidor HTTP foi bloqueada pelas restrições de abertura de portas locais. A execução com essa permissão passou. Isso não é uma falha funcional do projeto.

A verificação completa foi repetida após implementar as duas coleções relacionadas; o pacote foi gerado pelo mesmo `clean verify` aprovado.

## Testes JavaScript

Com Node.js 22.16.0, na raiz: `npm ci --ignore-scripts` e `npm run test:report`.

- **13 testes aprovados**, sem falhas ou ignorados.
- `app.js`: 100% de linhas, 96,24% de ramificações e 97,62% de funções.
- `mongodb/init.js`: 100% de linhas, 92,86% de ramificações e 100% de funções.
- Conjunto JavaScript: 100% de linhas, 95,92% de ramificações e 97,67% de funções.
- Relatório: `coverage/javascript.lcov`; limite mínimo automatizado de 70% nas três métricas.

Os testes executam o código real da interface com HTML real e DOM simulado (jsdom), incluindo cadastro de organização, seleção do vínculo, CRUD de pontos, máscaras, erros, escape de HTML e respostas de busca fora de ordem. A inicialização é executada com um dublê de MongoDB para conferir repetição e preservação de cadastros anteriores. O validador do banco foi conferido também com MongoDB real, conforme abaixo. Java e JavaScript são medidos separadamente; não se somam percentuais de métricas diferentes.

## API com MongoDB real

Ambiente: Windows 11, JDK 21.0.8, MongoDB Community 8.0.32 e mongosh 2.10.0. Foram usados um banco isolado `ecodescarte_entrega_validacao` em `127.0.0.1:27018` e a aplicação em `127.0.0.1:8081`.

O script `mongodb/init.js` foi executado duas vezes sobre o banco de validação da versão anterior: preservou os três pontos, completou seus vínculos e criou uma organização de demonstração, sem duplicação. Os validadores das duas coleções rejeitaram documentos inválidos com código 121. Os vínculos e os endereços incorporados foram conferidos diretamente no banco.

O comando abaixo aprovou **35 verificações**:

```powershell
pwsh -File .\scripts\verificar-api.ps1 -BaseUrl http://127.0.0.1:8081
```

Foram verificados a interface, Swagger/OpenAPI, conexão com o banco, cadastro 201 e Location, persistência do endereço, busca por nome, busca com acento, caracteres de expressão regular tratados como texto, entrada inválida 400, atualização 200, preservação do ID, POST sem sobrescrita, exclusão 204 e ausência 404. Os registros gerados pelo script foram removidos ao final.

As verificações também cobrem CRUD de organizações, persistência do vínculo, rejeição de organização inexistente no POST e PUT do ponto, bloqueio da exclusão de organização em uso (409) e exclusão após remover os pontos vinculados.

## Verificação no navegador

O fluxo de cadastrar, editar, pesquisar e excluir um registro fictício foi realizado na interface. A listagem voltou aos três exemplos após a exclusão. As máscaras de telefone, CEP e UF foram exercitadas. O layout foi inspecionado em tela estreita e as mensagens de sucesso foram exibidas.

O botão Limpar, antes visível sem busca ativa por causa do CSS, recebeu uma correção para respeitar o atributo `hidden`.

Após ampliar o banco, foram cadastrados no navegador uma organização fictícia e um ponto associado. A seleção automática da organização, o cadastro do ponto e a busca exibindo seu responsável foram conferidos. Os cadastros desse teste foram removidos do banco isolado ao terminar.

## Limites da evidência

- As coberturas Java e JavaScript não garantem ausência de defeitos. HTML/CSS declarativos e os próprios testes não entram nos relatórios.
- Os testes não representam carga, segurança em produção ou estudo com usuários.
- A configuração de GitHub Actions foi preparada; a execução no GitHub depende de publicar a branch e não foi usada como evidência deste resultado local.
- O enunciado `AEP_ESoft_6S.pdf` foi conferido e a equipe confirmou o requisito de múltiplas coleções, implementado nesta revisão. Publicação no GitHub, vídeo e submissão acadêmica são etapas separadas; a [conferência da segunda entrega](segunda-entrega.md) registra esses itens. O preenchimento de dados pessoais fica a cargo da equipe.

## Referências de configuração

- [Spring Boot 4 — MongoDB](https://docs.spring.io/spring-boot/4.0/reference/data/nosql.html): propriedade `spring.mongodb.uri`.
- [Springdoc — propriedades da interface](https://springdoc.org/ui-properties.html): `springdoc.swagger-ui.path` e `operationsSorter`.
- [Mockito — configuração do agente](https://github.com/mockito/mockito/blob/main/mockito-core/src/main/java/org/mockito/Mockito.java): carregamento explícito do agente nos testes com Java 21.
