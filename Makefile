# ==== Config ====
.RECIPEPREFIX := >
SHELL := /bin/sh
COMPOSE ?= docker compose
SERVICE ?= backend
IMAGE_NAME ?= quadrago-backend

# Escolhe wrapper conforme SO
UNAME_S := $(shell uname -s)
ifeq ($(OS),Windows_NT)
MVNW := mvnw.cmd
else
MVNW := ./mvnw
endif

.DEFAULT_GOAL := help

.PHONY: help
help:
> echo "Targets:"
> echo "  test             - roda testes com Maven Wrapper (perfil test)"
> echo "  test-docker      - roda testes em container maven usando cache local (~/.m2)"
> echo "  build            - docker compose build"
> echo "  up               - docker compose up (interativo)"
> echo "  up-d             - docker compose up -d (detached)"
> echo "  down             - docker compose down"
> echo "  clean            - down + remove volumes + órfãos"
> echo "  rebuild          - clean + build --no-cache + up"
> echo "  logs             - tail dos logs"
> echo "  bash             - shell dentro do service $(SERVICE)"
> echo "  status           - docker compose ps"

.PHONY: test
test:
> $(MVNW) -Dspring.profiles.active=test test

.PHONY: test-docker
test-docker:
> docker run --rm \
>  -v "$(HOME)/.m2":/root/.m2 \
>  -v "$(PWD)":/app -w /app \
>  maven:3.9.6-eclipse-temurin-22-alpine \
>  mvn -Dspring.profiles.active=test test

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
