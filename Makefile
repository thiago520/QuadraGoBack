# Nome da imagem para facilitar reutilização
IMAGE_NAME=quadrago-backend

# Executa os testes usando Maven localmente
test:
	mvnw.cmd test

# Executa os testes dentro do container de build (sem subir app)
test-docker:
	docker run --rm -v $(PWD):/app -w /app maven:3.9.6-eclipse-temurin-22-alpine mvn test

# Constrói a imagem com docker compose
build:
	docker compose build

# Sobe os containers em modo interativo
up:
	docker compose up

# Sobe os containers em background (detached)
up-d:
	docker compose up -d

# Derruba todos os containers e redes do projeto
down:
	docker compose down

# Derruba, remove volumes e limpa tudo
clean:
	docker compose down -v --remove-orphans

# Rebuild forçado + up
rebuild:
	docker compose down -v --remove-orphans
	docker compose build --no-cache
	docker compose up

# Mostra os logs (como tail -f)
logs:
	docker compose logs -f

# Executa um shell no container backend (usa sh por padrão)
bash:
	docker exec -it quadrago-back sh

# Verifica o status dos containers
status:
	docker compose ps
