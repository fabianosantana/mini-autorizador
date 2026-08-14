# Mini Autorizador

Sistema autorizador de transações de cartões de benefícios desenvolvido em **Java 21** e **Spring Boot**. O sistema oferece uma interface totalmente REST para criação de cartões, consulta de saldo e autorização de transações financeiras em tempo real com controle rigoroso de concorrência e validação de regras de negócio.

---

## 🛠️ Tecnologias e Stack Utilizada

- **Linguagem:** Java 21 (Records, Pattern Matching, Sealed Types)
- **Framework Principal:** Spring Boot 3.3.2
  - Spring Web (REST Controllers & Exception Handler)
  - Spring Data JPA (Persistência e Locking Pessimista)
  - Bean Validation (`jakarta.validation`)
- **Banco de Dados:**
  - **MySQL 5.7** (Ambiente de Produção / Docker)
  - **H2 Database** (Perfil de Testes e Execução Local Rápida)
- **Documentação de API:** SpringDoc OpenAPI 2.6.0 (**Swagger UI**)
- **Containerização:** Docker & Docker Compose
- **Testes Automatizados:** JUnit 5, MockMvc (Testes Unitários, de Integração e de Concorrência Multithread)
- **Build Tool:** Apache Maven 3.9+

---

## 🚀 Funcionalidades Principais

- **Criação de Cartões:** Permite cadastrar novos cartões. Todo cartão é criado automaticamente com um saldo inicial pré-configurado de **R$ 500,00**.
- **Obtenção de Saldo:** Consulta o saldo disponível de um cartão cadastrado pelo seu número.
- **Autorização de Transações:** Processa tentativas de pagamento utilizando o cartão como meio de pagamento.

### 📋 Regras de Autorização de Transação

Uma transação é aprovada e debitada do saldo do cartão se e somente se todas as regras a seguir forem satisfeitas:
1. **O cartão deve existir** no sistema (caso contrário, falha com `CARTAO_INEXISTENTE`).
2. **A senha informada deve ser a correta** (caso contrário, falha com `SENHA_INVALIDA`).
3. **O cartão deve possuir saldo suficiente** para o valor da transação (caso contrário, falha com `SALDO_INSUFICIENTE`).

---

## 💡 Destaques Arquiteturais

- **Rich Domain Model & Zero `if` Statements:** Toda a lógica de negócio foi projetada em modelo rico (`Cartao.debitar()`, `Cartao.possuiSaldoPara()`, `Cartao.senhaConfere()`) e encadeamento funcional utilizando `java.util.Optional` e *Pattern Matching* com *Guard Clauses* (`when`) do Java 21, sem utilizar nenhuma instrução `if`, `switch`, `break` ou `continue`.
- **Controle de Concorrência e Alta Disponibilidade:**
  - **Débitos Concorrentes:** Utilização de bloqueio pessimista (`SELECT ... FOR UPDATE` via `@Lock(LockModeType.PESSIMISTIC_WRITE)`) garantindo atomicidade absoluta mesmo com múltiplas instâncias da aplicação processando transações no mesmo cartão ao mesmo tempo.
  - **Criação Concorrente de Cartões:** Proteção em duas camadas usando `existsByNumeroCartao` e delegação da constraint única para a chave primária/índice do banco de dados tratada via `GlobalExceptionHandler`.

---

## 📌 Contratos dos Serviços REST

### 1. Criar novo cartão
```http
POST /cartoes HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
    "numeroCartao": "6549873025634501",
    "senha": "1234"
}
```

#### Respostas:
- **Sucesso (`201 Created`):**
  ```json
  {
      "numeroCartao": "6549873025634501",
      "senha": "1234"
  }
  ```
- **Cartão já existente (`422 Unprocessable Entity`):**
  ```json
  {
      "numeroCartao": "6549873025634501",
      "senha": "1234"
  }
  ```

---

### 2. Obter saldo do cartão
```http
GET /cartoes/{numeroCartao} HTTP/1.1
Host: localhost:8080
```

#### Respostas:
- **Sucesso (`200 OK`):**
  ```text
  495.15
  ```
- **Cartão inexistente (`404 Not Found`):**
  *(Sem corpo de resposta)*

---

### 3. Realizar uma transação
```http
POST /transacoes HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
    "numeroCartao": "6549873025634501",
    "senhaCartao": "1234",
    "valor": 10.00
}
```

#### Respostas:
- **Transação Aprovada (`201 Created`):**
  ```text
  OK
  ```
- **Regra de Autorização Recusada (`422 Unprocessable Entity`):**
  - Corpo da resposta: `SALDO_INSUFICIENTE` ou `SENHA_INVALIDA` ou `CARTAO_INEXISTENTE`

---

## 🔧 Como Executar o Sistema

### Pré-requisitos
- **Java 21** instalado
- **Maven 3.9+** instalado
- **Docker** e **Docker Compose** instalados (opcional para execução com MySQL)

---

### 1. Inicializar o Banco de Dados via Docker Compose
Navegue até a pasta do projeto e inicie o container do banco de dados:

```bash
docker compose -f docker/docker-compose.yml up -d
```

---

### 2. Executar a Aplicação Spring Boot
Para rodar a aplicação localmente na porta `8080`:

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

---

### 3. Acessar a Documentação Interativa (Swagger UI)
Com a aplicação em execução, acesse o Swagger no navegador para visualizar e testar todos os endpoints:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

---

### 4. Executar os Testes Automatizados
Para rodar toda a suíte de testes unitários, de integração e de concorrência multithread:

```bash
mvn clean test
```