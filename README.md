# QuadraGo - Backend

Backend do sistema **QuadraGo**, desenvolvido em **Spring Boot** com **PostgreSQL**, responsável pela gestão de alunos, professores, turmas, características e avaliações.

---

## 🚀 Tecnologias
- **Java 17**
- **Spring Boot 3**
- **Spring Security (JWT)**
- **Spring Data JPA**
- **PostgreSQL**
- **Spring Boot Admin**
- **Lombok**
- **Actuator**

---

## 📂 Estrutura de Pastas

src/main/java/com/quadrago/backend/

├── config/ # Configurações (Security, JWT, etc.)

├── controllers/ # REST Controllers

├── dtos/ # Data Transfer Objects

├── filters/ # Filtros (ex: JwtAuthenticationFilter)

├── models/ # Entidades JPA

├── repositories/ # Repositórios JPA

├── services/ # Serviços de negócio


---

## 🔑 Autenticação
- Autenticação baseada em **JWT**.
- Usuários podem ter papéis (`ADMIN`, `TEACHER`, `STUDENT`).
- Controle de acesso aplicado via **Spring Security**.

---

## 📌 Endpoints REST

### 🔹 Auth
| Método | Endpoint           | Descrição                          | Acesso |
|--------|-------------------|------------------------------------|--------|
| POST   | `/auth/login`      | Login e geração de JWT             | Público |
| POST   | `/auth/register`   | Registro de novo usuário           | Público |

---

### 🔹 Students (`Aluno`)
| Método | Endpoint               | Descrição                  | Acesso |
|--------|-----------------------|----------------------------|--------|
| GET    | `/students`            | Listar todos os alunos     | `ADMIN`, `TEACHER`, `STUDENT` |
| GET    | `/students/{id}`       | Buscar aluno por ID        | `ADMIN`, `TEACHER`, `STUDENT` |
| POST   | `/students`            | Criar aluno                | `ADMIN`, `TEACHER` |
| PUT    | `/students/{id}`       | Atualizar aluno            | `ADMIN`, `TEACHER` |
| DELETE | `/students/{id}`       | Deletar aluno              | `ADMIN` |

---

### 🔹 Teachers (`Professor`)
| Método | Endpoint               | Descrição                    | Acesso |
|--------|-----------------------|------------------------------|--------|
| GET    | `/teachers`            | Listar todos os professores | `ADMIN` |
| GET    | `/teachers/{id}`       | Buscar professor por ID      | `ADMIN` |
| POST   | `/teachers`            | Criar professor              | `ADMIN` |
| PUT    | `/teachers/{id}`       | Atualizar professor          | `ADMIN` |
| DELETE | `/teachers/{id}`       | Deletar professor            | `ADMIN` |

---

### 🔹 Class Groups (`Turma`)
| Método | Endpoint                    | Descrição               | Acesso |
|--------|----------------------------|-------------------------|--------|
| GET    | `/class-groups`             | Listar todas as turmas  | `ADMIN`, `TEACHER` |
| GET    | `/class-groups/{id}`        | Buscar turma por ID     | `ADMIN`, `TEACHER` |
| POST   | `/class-groups`             | Criar turma             | `ADMIN`, `TEACHER` |
| PUT    | `/class-groups/{id}`        | Atualizar turma         | `ADMIN`, `TEACHER` |
| DELETE | `/class-groups/{id}`        | Deletar turma           | `ADMIN`, `TEACHER` |

---

### 🔹 Traits (`Caracteristica`)
| Método | Endpoint                        | Descrição                               | Acesso |
|--------|--------------------------------|-----------------------------------------|--------|
| GET    | `/traits/teacher/{teacherId}`   | Listar características de um professor  | `TEACHER` |
| POST   | `/traits`                       | Criar característica                    | `TEACHER` |
| PUT    | `/traits/{id}`                  | Atualizar característica                | `TEACHER` |
| DELETE | `/traits/{id}`                  | Deletar característica                  | `TEACHER` |

---

### 🔹 Trait Evaluations (`AvaliacaoCaracteristica`)
| Método | Endpoint                                       | Descrição                           | Acesso |
|--------|-----------------------------------------------|-------------------------------------|--------|
| POST   | `/trait-evaluations`                          | Criar avaliação                     | `TEACHER`, `STUDENT` |
| GET    | `/trait-evaluations/student/{studentId}`      | Listar avaliações de um aluno       | `TEACHER`, `STUDENT` |
| PUT    | `/trait-evaluations/{studentId}/{traitId}`    | Atualizar avaliação                 | `TEACHER`, `STUDENT` |
| DELETE | `/trait-evaluations/{studentId}/{traitId}`    | Deletar avaliação                   | `TEACHER` |

---

### 🔹 Admin & Monitoramento
| Método | Endpoint                | Descrição                        | Acesso |
|--------|------------------------|----------------------------------|--------|
| GET    | `/actuator/**`          | Endpoints do Actuator            | Público |
| GET    | `/admin/**`             | Endpoints administrativos        | `ADMIN` |
| GET    | `/applications/**`      | Spring Boot Admin (monitoramento)| Autenticado |
| GET    | `/instances/**`         | Spring Boot Admin (instâncias)   | Autenticado |

---

## ✅ Testes
O projeto conta com **testes unitários e de integração** cobrindo:
- Services
- Controllers
- Regras de segurança
- Regras de negócio

Para rodar os testes:

```sh
make test


📌 Próximos Passos

Implementar relatórios de desempenho.

Criar notificações para alunos e professores.

Melhorar documentação com exemplos de payloads.