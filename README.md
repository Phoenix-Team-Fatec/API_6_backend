# API_6_backend

0) .env

Crie um .env com a variável MONGODB_URI

1) Docker

Ctrl + Shift + P

```
Dev Containers: Rebuild and Reopen in Container
```

2) Importar dados

É necessário ter a pasta docs na raiz do projeto

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=import
```

2) Executar testes

```bash
mvn test
```

3) Executar o Backend

```bash
mvn spring-boot:run
```
