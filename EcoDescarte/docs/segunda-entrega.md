# Preparação da segunda entrega — EcoDescarte

Este guia foi conferido com o enunciado `AEP_ESoft_6S.pdf`, AEP 2026.2, especialmente as seções 6 a 11. A preparação técnica foi validada localmente, mas a submissão ainda depende dos itens pendentes abaixo. O PDF não informa o prazo de envio; confira-o no ambiente da disciplina.

## Conferência da rubrica da segunda entrega

| Critério | Peso | Evidência disponível e pendência |
| --- | --- | --- |
| PoC funcional e evolução | 0,1 | CRUD validado localmente. A equipe indicou a versão do repositório fornecido como primeira entrega; a base desta preparação é `7510648`. |
| Banco NoSQL | 0,1 | Requisito de múltiplas coleções confirmado pela equipe: implementadas `organizacoes` e `pontos_coleta`, ligadas por `organizacaoId`, com endereço aninhado. |
| Orientação a objetos e qualidade | 0,1 | Controller, Service, Repository e modelos com responsabilidades documentadas; validações e erros tratados. |
| Metodologia de trabalho | 0,1 | [Quadro de tarefas](quadro-tarefas.md) preparado. A equipe deve mantê-lo atualizado com responsáveis reais e evidências; ele não reconstrói a metodologia usada anteriormente. |
| Testes automatizados | 0,1 | 65 testes Java, 13 testes JavaScript e 35 verificações HTTP aprovados; estas últimas com MongoDB real. |
| Cobertura mínima de 70% | 0,1 | Java: 99,23% de instruções pelo JaCoCo. JavaScript: 100% das linhas, 95,92% das ramificações e 97,67% das funções pelo Node; relatórios e comandos reproduzíveis no README. |
| Documentação técnica | 0,1 | README, diagramas, instruções de execução e testes, registro de validação e este guia. Revisar caso a modelagem mude. |
| GitHub e versionamento | 0,1 | Alterações preparadas em branch local. Publicar a versão revisada e identificar as duas entregas por tag, release ou commit, conforme seção 9. |
| Vídeo de demonstração | 0,2 | Roteiro abaixo. Gravar vídeo de 3 a 5 minutos e disponibilizar o link do YouTube. |

**Evolução técnica desde a base `7510648`:** duas coleções relacionadas, cadastro de organizações na interface, validação do vínculo, proteção contra exclusão de organização em uso, respostas de erro consistentes, configuração MongoDB corrigida, testes ampliados e cobertura JavaScript. O endereço continua incorporado ao ponto de coleta. A referência da segunda entrega será o commit final publicado pela equipe.

## Problema e proposta

Pessoas que precisam descartar resíduos podem ter dificuldade para encontrar locais que aceitem cada material. O EcoDescarte organiza um cadastro de pontos de coleta com nome, contato, endereço e resíduos aceitos. O usuário consulta os pontos pelo nome ou pelo tipo de resíduo.

A proposta se relaciona ao ODS 11, pela organização de informações úteis à cidade, e ao ODS 12, pelo apoio ao descarte responsável. O projeto demonstra um fluxo de informação; não mediu redução de resíduos ou impacto ambiental real.

## Escopo implementado

| Capacidade | Implementação | Como demonstrar |
| --- | --- | --- |
| Cadastrar ponto | POST com validação e ID gerado | Formulário Novo ponto |
| Consultar pontos | Listagem, ID, nome e resíduo | Lista, busca e Swagger |
| Atualizar ponto | PUT preserva o ID | Botão Editar |
| Excluir ponto | DELETE com confirmação na interface | Botão Excluir |
| Persistir dados | MongoDB, pontos_coleta e organizacoes, endereço incorporado | Consulta no mongosh e recarga da página |
| Vincular responsável | Cadastro de organização e referência obrigatória no ponto | Criar organização, selecioná-la e mostrar seu nome no cartão |
| Tratar entradas inválidas | Validação na API e na coleção | POST vazio retorna 400 |
| Tratar ausência | ID inexistente retorna 404 | Consulta após excluir |
| Documentar API | OpenAPI e Swagger em /docs | Abrir endpoints e schemas |
| Testar | JUnit, Mockito, MockMvc, contexto Spring e roteiro HTTP | Resultado Maven, JaCoCo e script da API |

## Roteiro do vídeo (meta de 4 minutos e 30 segundos)

O tempo obrigatório é de **3 a 5 minutos**. Deixe a aplicação, os diagramas e os relatórios abertos antes de gravar. Execute os testes previamente e mostre o comando e o resultado real, sem consumir o vídeo esperando a compilação.

| Tempo | Conteúdo |
| --- | --- |
| 0:00–0:25 | Apresentar EcoDescarte, o problema de encontrar pontos de coleta e a relação com ODS 11 e 12. Informar que os dados de demonstração são fictícios. |
| 0:25–0:55 | Mostrar a base `7510648` indicada pela equipe e o commit final publicado; explicar a evolução de uma para duas coleções relacionadas e as novas validações e testes. |
| 0:55–1:25 | Mostrar o diagrama final: interface HTML/CSS/JavaScript, API Java/Spring e MongoDB. Explicar Controller, Service, Repository e a modelagem final do banco. |
| 1:25–3:10 | Buscar `Centro` por nome e `plástico` por resíduo; cadastrar um ponto fictício com endereço e resíduos; recarregar para mostrar persistência; editar e excluir apenas esse cadastro. Deixar os dados de demonstração preparados para preencher sem demora. |
| 3:10–4:00 | Mostrar `clean verify`, o resultado dos testes, o relatório JaCoCo e o comando de reprodução. Informar o escopo da cobertura exibida e mostrar o resultado da verificação HTTP com banco real. Usar os números da versão final, após eventuais mudanças. |
| 4:00–4:30 | Mostrar o quadro de tarefas e a documentação; resumir os fluxos entregues e os limites da PoC. Conferir que a versão exibida é a mesma indicada para avaliação. |

Ensaiar com cronômetro. Evitar afirmar impacto ambiental medido ou funcionalidades não implementadas. O roteiro não substitui o vídeo gravado e publicado.

## Evidências que a equipe pode apresentar

- Interface com a listagem e os três exemplos.
- Cadastro/edição funcionando e busca por resíduo.
- Swagger com as operações documentadas.
- MongoDB mostrando um documento com endereço incorporado e lista de resíduos.
- Resultado `BUILD SUCCESS` da execução `clean verify`.
- Relatório HTML do JaCoCo e resultado do script `scripts/verificar-api.ps1`.

Os resultados efetivamente obtidos nesta preparação estão em [validacao.md](validacao.md). Prints não substituem os arquivos-fonte e os comandos de reprodução.

## Explicações técnicas para a banca

- **Por que MongoDB?** Um ponto é um documento com endereço incorporado e lista de resíduos. A organização fica em outra coleção porque pode responder por vários pontos; `organizacaoId` representa esse relacionamento.
- **Como o código está organizado?** O Controller recebe HTTP e valida a entrada; o Service aplica as regras; o Repository acessa MongoDB. A interface usa a mesma API pelo navegador.
- **Como evitar dados inválidos?** Bean Validation verifica a API; o script de inicialização aplica regras à coleção. Erros retornam status HTTP e mensagens legíveis.
- **O que a cobertura prova?** JaCoCo mede o Java; Node mede o JavaScript da interface e da inicialização do banco. Os percentuais são apresentados separadamente por métrica. Não garantem ausência de bugs; há também verificação com MongoDB real e navegador.
- **Por que os exemplos voltam após reiniciar?** A carga inicial recria os três IDs ausentes. `ECODESCARTE_DADOS_INICIAIS=false` desativa esse comportamento na aplicação.

## Limitações declaradas

A PoC é destinada à demonstração local e não possui autenticação, permissões por usuário, geolocalização, cálculo de distância, paginação ou verificação de locais reais. A busca por nome preserva acentos; a busca por resíduo os normaliza. Melhorias futuras incluem validar estabelecimentos, controlar quem altera os dados e testar a experiência com usuários.

## Conferência antes de submeter

- [x] Confirmar o requisito de múltiplas coleções e implementar vínculo e objetos complexos.
- [x] Medir a cobertura Java e JavaScript com comandos reproduzíveis.
- [x] Registrar a base da primeira entrega indicada pela equipe (`7510648`) e as mudanças técnicas.
- [ ] Atualizar o quadro com responsáveis reais, evidências e estado das tarefas.
- [ ] Reproduzir o README na máquina da apresentação e atualizar os resultados se houver mudanças no código.
- [ ] Publicar o código revisado no GitHub, identificar a versão final e conferir acesso pelo link informado.
- [ ] Gravar e disponibilizar no YouTube o vídeo obrigatório de 3 a 5 minutos.
- [ ] Preencher o formulário oficial da seção 11 com os dados reais e links acessíveis.
- [ ] Conferir prazo e canal de envio no ambiente da disciplina e as regras sobre declaração de ferramentas utilizadas.

O preenchimento do formulário da seção 11 e os dados pessoais dos integrantes ficam a cargo da equipe, conforme solicitado. Registrar junto ao link do repositório os commits, tags ou releases correspondentes às duas entregas.
