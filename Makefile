# ==== Config ====
.RECIPEPREFIX := >
SHELL := /bin/sh

# Docker Compose (raiz do repo)
COMPOSE ?= docker compose -f docker-compose.yml
SERVICE ?= backend

# Caminhos host (podem ser sobrescritos: make test BACKEND_DIR=/c/projeto/backend)
HOST_M2     ?= $(HOME)/.m2
BACKEND_DIR ?= $(CURDIR)/backend

# Evita conversão de paths no Git Bash (Windows)
export MSYS_NO_PATHCONV=1

# Maven em container
DOCKER_MVN := docker run --rm \
	-v "$(HOST_M2):/root/.m2" \
	-v "$(BACKEND_DIR):/app" \
	-w /app \
	maven:3.9.9-eclipse-temurin-22-alpine mvn

.DEFAULT_GOAL := help

.PHONY: help
help:
> echo "Targets:"
> echo "  test             - mvn test (perfil test) via Docker"
> echo "  test-docker      - alias de test"
> echo "  build            - docker compose build"
> echo "  up               - docker compose up (interativo)"
> echo "  up-d             - docker compose up -d"
> echo "  down             - docker compose down"
> echo "  clean            - down + volumes + órfãos"
> echo "  rebuild          - clean + build --no-cache + up"
> echo "  logs             - tail dos logs"
> echo "  bash             - shell dentro do service $(SERVICE)"
> echo "  status           - docker compose ps"
> echo ""
> echo "Vars: BACKEND_DIR=$(BACKEND_DIR)  HOST_M2=$(HOST_M2)"

.PHONY: test
test:
> $(DOCKER_MVN) -Dspring.profiles.active=test test

.PHONY: test-docker
test-docker: test

.PHONY: build
build:
> $(COMPOSE) build

.PHONY: up
up:
> $(COMPOSE) up

.PHONY: up-d
up-d:
> $(COMPOSE) up -d

.PHONY: down
down:
> $(COMPOSE) down

.PHONY: clean
clean:
> $(COMPOSE) down -v --remove-orphans

.PHONY: rebuild
rebuild:
> $(COMPOSE) down -v --remove-orphans
> $(COMPOSE) build --no-cache
> $(COMPOSE) up

.PHONY: logs
logs:
> $(COMPOSE) logs -f

.PHONY: bash
bash:
> $(COMPOSE) exec $(SERVICE) sh

.PHONY: status
status:
> $(COMPOSE) ps
