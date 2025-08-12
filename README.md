# 🏗️ Construction Finance

**API REST + Aplicação Web** para **controle financeiro de obra residencial**.  
O objetivo é registrar e acompanhar todos os gastos da construção, de forma simples e organizada, permitindo visualizar despesas por **categoria**, **fornecedor** e **quem pagou**.

---

## 📌 Funcionalidades

- **Autenticação JWT** (usuário e senha)
- **Cadastro de categorias** (materiais, mão de obra, equipamentos, taxas, etc.)
- **Cadastro de fornecedores**
- **Cadastro de pagadores** (ex.: Gabriel, Esposa)
- **Lançamento de despesas** com:
  - Data
  - Descrição
  - Categoria
  - Fornecedor
  - Pagador
  - Forma de pagamento
  - Valor
  - Anexo (nota fiscal / recibo)
- **Filtros de pesquisa** por data, categoria, fornecedor e pagador
- **Relatórios básicos**:
  - Total por categoria
  - Total por mês
  - Total por pagador
- **Documentação automática da API com Swagger**

---

## 🛠️ Tecnologias

### Backend
- Java 21
- Spring Boot
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Validation
- PostgreSQL
- JWT (Java JWT / Auth0)
- Lombok
- Flyway (versionamento do banco)
- **Springdoc OpenAPI** (Swagger UI)

### Frontend
- (A definir: React, Angular ou Vue)
- Framework de UI (Material UI / Bootstrap / PrimeVue)

---

## 📂 Estrutura do Repositório

```bash
construction-finance/
│
├── backend/        # API REST em Spring Boot
│   ├── pom.xml
│   └── src/...
│
├── frontend/       # Aplicação web
│   ├── package.json
│   └── src/...
│
├── docs/           # Documentação e diagramas
│   ├── modelo_bd.png
│   └── ...
│
├── .gitignore
└── README.md
```

---

## 🚀 Como Rodar o Projeto

### 1️⃣ Clonar o repositório
```bash
git clone https://github.com/seu-usuario/construction-finance.git
cd construction-finance
```

### 2️⃣ Rodar o Backend
```bash
cd backend
# Configurar application.properties com as credenciais do PostgreSQL
mvn spring-boot:run
```
Backend disponível em: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 3️⃣ Rodar o Frontend
```bash
cd frontend
npm install
npm start
```
Frontend disponível em: `http://localhost:3000`

---

## 📊 Modelo de Dados

![Modelo de Dados](docs/modelo_bd.png)

---

## 📜 Licença
Este projeto está sob a licença [MIT](LICENSE).

---

## 💡 Observações
Este projeto é voltado para uso pessoal, com foco exclusivo no controle de gastos da construção de uma casa.  
Não possui automações, notificações ou multiusuário avançado — apenas registro e consulta de despesas.
