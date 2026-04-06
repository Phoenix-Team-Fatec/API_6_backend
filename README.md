# API_6 Backend

Backend da aplicação de regras de negócio, desenvolvido com Java 17 + Spring Boot 4 + MongoDB.

## Pré-requisitos

- Java 17+
- Maven 3.9+ ou uso do Maven Wrapper (`./mvnw` ou `mvnw.cmd`)
- MongoDB acessível pela aplicação

Para conferir versões instaladas:

```bash
java -version
mvn -v
```

## 1. Clonar e entrar na pasta

```bash
git clone <url-do-repositorio>
cd API_6_backend
```

## 2. Configurar variáveis de ambiente

Crie o arquivo `.env` com base no `.env.example`.

Windows (PowerShell):

```powershell
Copy-Item .env.example .env
```

Linux/macOS:

```bash
cp .env.example .env
```

Depois, edite o `.env` conforme seu cenário.

Exemplo:

```env
MONGODB_URI=mongodb://phoenix:phoenix@localhost:27018/api6?authSource=admin
```

### O que a variável faz

- `MONGODB_URI`: string de conexão usada pelo Spring Boot em `spring.mongodb.uri`.

### Referências rápidas de uso

- Dentro do devcontainer: `mongodb://phoenix:phoenix@mongo:27017/api6?authSource=admin`
- Na máquina host, com Docker expondo a porta: `mongodb://phoenix:phoenix@localhost:27018/api6?authSource=admin`
- Em produção/staging: use a URI completa do MongoDB/Atlas

## 3. Preparar os dados de importação

Para usar o perfil de importação, a pasta `docs/` precisa existir na raiz do projeto com as planilhas esperadas pelo serviço:

- `docs/dom-rock/BASE_COMMISS_FINAL.xlsx`
- `docs/dom-rock/BASE RH/*.xlsx`
- `docs/dom-rock/BASE_VENDAS/*.xlsx`

Esse fluxo apaga os dados atuais das coleções e recarrega os registros a partir dos arquivos Excel.

## 4. Rodar a importação inicial

Para popular o MongoDB com os dados das planilhas:

Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=import"
```

Linux/macOS:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=import
```

Se preferir usar Maven instalado globalmente:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=import
```

## 5. Rodar a aplicação em desenvolvimento

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Por padrão, a API sobe em `http://localhost:8080`.

## 6. Executar testes

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

Ou com Maven global:

```bash
mvn test
```

## Endpoints principais

- `GET /health`: endpoint simples de healthcheck.
- `GET /api/commission/simulate?matricula=<valor>&month=yyyy-MM`: simula o cálculo de comissão para um colaborador em um mês.
- `GET /api/rules/commission-rates`: lista regras de comissão com filtros opcionais.
- `GET /api/rules/commission-rates/trash`: lista regras removidas logicamente.
- `GET /api/rules/{id}`: busca uma regra por id.
- `POST /api/rules`: cria uma nova regra.
- `PUT /api/rules/{id}`: atualiza uma regra.
- `DELETE /api/rules/{id}`: faz exclusão lógica de uma regra.
- `POST /api/rules/{id}/activate`: ativa uma regra.
- `POST /api/rules/{id}/deactivate`: desativa uma regra.
- `POST /api/rules/{id}/restore`: restaura uma regra removida.
- `POST /api/rules/{id}/rollback`: reverte uma regra para versão anterior.
- `GET /api/rules/exceptions?month=yyyy-MM`: lista exceções mensais, com filtros opcionais por tipo e matrícula.

## Exemplos rápidos

Healthcheck:

```bash
curl http://localhost:8080/health
```

Simulação de comissão:

```bash
curl "http://localhost:8080/api/commission/simulate?matricula=12345&month=2025-08"
```

## Comandos disponíveis

- `./mvnw spring-boot:run`: inicia a API em desenvolvimento.
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=import`: importa dados das planilhas para o MongoDB.
- `./mvnw test`: executa a suíte de testes.
- `./mvnw package`: gera o artefato da aplicação.

No Windows, substitua `./mvnw` por `.\mvnw.cmd`.

## Troubleshooting rápido

- Erro de conexão com MongoDB:
  verifique se o valor de `MONGODB_URI` está correto e se a instância está acessível.
- Erro ao importar dados:
  confirme se a pasta `docs/` existe e se os arquivos Excel estão nos caminhos esperados.
- Aplicação sobe, mas endpoints retornam falha:
  confira se o banco foi populado com o perfil `import`.
- Porta `8080` em uso:
  encerre o processo que está usando a porta ou configure outra porta no Spring Boot.

## Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data MongoDB
- MongoDB
- Apache POI
- JUnit / Spring Boot Test
