# EcoDescarte

## Sobre o projeto

O EcoDescarte é uma Prova de Conceito desenvolvida para a AEP do curso de Engenharia de Software.

A solução tem como objetivo auxiliar usuários a encontrar pontos adequados para o descarte de diferentes tipos de resíduos, contribuindo para o descarte correto e para a redução de impactos ambientais.

## Problema

Muitas pessoas não sabem onde descartar corretamente resíduos como eletrônicos, pilhas, baterias e outros materiais que não devem ser descartados junto ao lixo comum.

O descarte incorreto desses materiais pode causar impactos ambientais e dificultar o processo de reciclagem.

## ODS

O projeto está relacionado ao:

**ODS 12 – Consumo e Produção Responsáveis**

A proposta contribui principalmente para o incentivo ao descarte correto de resíduos e à gestão ambientalmente adequada de materiais.

## Funcionalidades da PoC

A primeira versão do EcoDescarte permitirá:

- Cadastrar pontos de coleta;
- Cadastrar tipos de resíduos;
- Consultar os pontos de coleta cadastrados;
- Consultar os resíduos aceitos pelos pontos de coleta;
- Armazenar os dados utilizando MongoDB.

## Tecnologias

- Java
- Spring Boot
- Spring Web
- Spring Data MongoDB
- MongoDB
- Maven
- JUnit
- Mockito
- JaCoCo
- Git e GitHub

## Estrutura do projeto

O projeto utiliza uma arquitetura organizada em camadas:

- `model`: representação dos objetos do sistema;
- `repository`: comunicação com o MongoDB;
- `service`: regras e operações da aplicação;
- `controller`: endpoints da API;
- `dto`: objetos utilizados para entrada e saída de dados;
- `test`: testes automatizados.

## Banco de dados

O EcoDescarte utiliza MongoDB como banco de dados NoSQL.

A aplicação utiliza múltiplas coleções e relacionamentos entre os documentos, atendendo aos requisitos do segundo semestre da AEP.

Principais coleções:

- Pontos de coleta;
- Resíduos.

Os pontos de coleta também possuem informações estruturadas, como endereço e lista de resíduos aceitos.

## Como executar

### Pré-requisitos

- Java 21 ou superior;
- Maven;
- MongoDB.

### Executando o projeto

Clone o repositório:

```bash
git clone https://github.com/PauloMalavasi/Aep-6-Semestre.git