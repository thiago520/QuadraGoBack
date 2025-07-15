# QuadraGo - Backend

Sistema de gerenciamento de quadras esportivas com Spring Boot, JWT, Spring Boot Admin, PostgreSQL e Docker.

---

## 🚀 Tecnologias

* **Java 22 + Spring Boot 3.2**
* **Spring Boot Admin (Dashboard)**
* **Spring Security com JWT**
* **PostgreSQL 15**
* **Adminer (Interface DB)**
* **Docker e Docker Compose**
* **Makefile (automatização de comandos)**

---

## 📆 Estrutura dos containers

| Serviço      | Porta Local | Descrição                                 |
| ------------ | ----------- | ----------------------------------------- |
| Backend API  | 8080        | API Java com Spring Boot                  |
| Admin Server | 8081        | Painel Spring Boot Admin                  |
| Adminer      | 8082        | Interface web para gerenciar o PostgreSQL |
| PostgreSQL   | 5432        | Banco de dados relacional                 |

---

## 🔧 Requisitos

* [Docker Desktop](https://www.docker.com/products/docker-desktop)
* [Make](https://gnuwin32.sourceforge.net/packages/make.htm) (já instalado no MSYS2, Git Bash ou WSL)

---

## ▶️ Comandos via Makefile

| Comando        | Ação                                                            |
| -------------- | --------------------------------------------------------------- |
| `make build`   | Constrói as imagens Docker do projeto                           |
| `make up`      | Sobe os containers em modo interativo                           |
| `make up-d`    | Sobe os containers em modo background (recomendado)             |
| `make down`    | Derruba os containers e redes do projeto                        |
| `make clean`   | Derruba tudo e remove volumes/dados                             |
| `make rebuild` | Força rebuild e sobe novamente tudo                             |
| `make logs`    | Exibe os logs do container backend                              |
| `make bash`    | Acessa o shell do container backend (sh)                        |
| `make test`    | Executa os testes Maven manualmente dentro do container backend |

---

## 🔐 Credenciais padrão

| Tipo              | Usuário  | Senha    |
| ----------------- | -------- | -------- |
| Spring Boot Admin | admin    | admin    |
| Spring Security   | admin    | admin    |
| PostgreSQL        | postgres | postgres |

---

## 📂 Endpoints úteis

| URL                              | Descrição                             |
| -------------------------------- | ------------------------------------- |
| `http://localhost:8080`          | Backend API                           |
| `http://localhost:8081`          | Painel do Spring Boot Admin           |
| `http://localhost:8080/actuator` | Endpoints do Actuator                 |
| `http://localhost:8082`          | Adminer (interface de banco de dados) |

---

## 🔪 Testes

Para executar os testes unitários:

```bash
make test
```

(Executado dentro do container, com `mvn test`)

---

## 📁 Estrutura de pastas

```
.
├── src/                    # Código-fonte da aplicação Spring Boot
├── Dockerfile              # Imagem com etapa de build + execução
├── docker-compose.yml      # Orquestra os serviços
├── Makefile                # Comandos automatizados
├── README.md               # Este arquivo
└── application.properties  # Configuração da aplicação
```

---

## ✅ Observações

* O backend registra automaticamente no Spring Boot Admin.
* O `JwtAuthenticationFilter` e o Basic Auth coexistem sem conflitos.
* O painel admin está isolado do backend para evitar conflitos de rota e autenticação.
* A imagem `caoxuyang/spring-boot-admin-server:k8s-api-server-1` foi usada com sucesso para o Spring Boot Admin Server.

---

## 💬 Suporte

Se surgir algum erro ou comportamento estranho, verifique:

* Logs com `make logs`
* URLs internas dos containers (`backend`, `admin-server`)
* Firewall/bloqueio local nas portas 8080, 8081, 8082

---

> Desenvolvido com ❤️ para facilitar a gestão de quadras esportivas.
