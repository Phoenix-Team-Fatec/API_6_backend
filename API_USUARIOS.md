# API de Usuários

## Visão Geral

A API de Usuários fornece endpoints RESTful para gerenciar usuários do sistema com suporte a papéis (ADMIN/USER) e autenticação por senha criptografada.

## Estrutura MVC

### Model (Entity)
- **Classe**: `Usuario` (em `domain/model/Usuario.java`)
- **Campos**:
  - `id`: ID único (MongoDB)
  - `nome`: Nome completo do usuário
  - `email`: Email único
  - `senha`: Senha criptografada com BCrypt
  - `papel`: ADMIN ou USER
  - `ativo`: Status do usuário
  - `criadoEm`: Data de criação
  - `atualizadoEm`: Data da última atualização
- **Collection**: `usuarios` (MongoDB)

### Repository
- **Interface**: `UsuarioRepository` (em `domain/repository/UsuarioRepository.java`)
- **Métodos**:
  - `findByEmail(String email)`: Buscar por email
  - `findByAtivo(Boolean ativo)`: Listar usuários por status
  - `findByPapel(String papel)`: Listar usuários por papel
  - `findByEmailAndAtivo(String email, Boolean ativo)`: Buscar usuário ativo por email

### Service
- **Interface**: `UsuarioService` (em `service/UsuarioService.java`)
- **Implementação**: `UsuarioServiceImpl` (em `service/UsuarioServiceImpl.java`)
- **Métodos**:
  - `criarUsuario(CriarUsuarioRequest request)`: Cria novo usuário
  - `buscarPorId(String id)`: Busca por ID
  - `buscarPorEmail(String email)`: Busca por email
  - `listarAtivos()`: Lista usuários ativos
  - `listarPorPapel(String papel)`: Lista por papel
  - `listarTodos()`: Lista todos os usuários
  - `atualizar(String id, CriarUsuarioRequest request)`: Atualiza usuário
  - `alterarStatus(String id, Boolean ativo)`: Ativa/desativa usuário
  - `deletar(String id)`: Soft delete (marca como inativo)

### Controller
- **Classe**: `UsuarioController` (em `controller/UsuarioController.java`)
- **Rota Base**: `/api/usuarios`

## Endpoints da API

### 1. Criar Usuário
**POST** `/api/usuarios`

**Requisição:**
```json
{
  "nome": "João Silva",
  "email": "joao@example.com",
  "senha": "senha123",
  "papel": "USER"
}
```

**Resposta (201 Created):**
```json
{
  "id": "64f9a1b2c3d4e5f6g7h8i9j0",
  "nome": "João Silva",
  "email": "joao@example.com",
  "papel": "USER",
  "ativo": true,
  "criadoEm": "2025-04-21T10:30:00",
  "atualizadoEm": null
}
```

### 2. Buscar Usuário por ID
**GET** `/api/usuarios/{id}`

**Resposta (200 OK):**
```json
{
  "id": "64f9a1b2c3d4e5f6g7h8i9j0",
  "nome": "João Silva",
  "email": "joao@example.com",
  "papel": "USER",
  "ativo": true,
  "criadoEm": "2025-04-21T10:30:00",
  "atualizadoEm": null
}
```

### 3. Buscar Usuário por Email
**GET** `/api/usuarios/email/{email}`

**Resposta (200 OK):**
```json
{
  "id": "64f9a1b2c3d4e5f6g7h8i9j0",
  "nome": "João Silva",
  "email": "joao@example.com",
  "papel": "USER",
  "ativo": true,
  "criadoEm": "2025-04-21T10:30:00",
  "atualizadoEm": null
}
```

### 4. Listar Todos os Usuários
**GET** `/api/usuarios`

**Resposta (200 OK):**
```json
[
  {
    "id": "64f9a1b2c3d4e5f6g7h8i9j0",
    "nome": "João Silva",
    "email": "joao@example.com",
    "papel": "USER",
    "ativo": true,
    "criadoEm": "2025-04-21T10:30:00",
    "atualizadoEm": null
  },
  {
    "id": "64f9a1b2c3d4e5f6g7h8i9j1",
    "nome": "Maria Santos",
    "email": "maria@example.com",
    "papel": "ADMIN",
    "ativo": true,
    "criadoEm": "2025-04-21T11:00:00",
    "atualizadoEm": null
  }
]
```

### 5. Listar Usuários Ativos
**GET** `/api/usuarios/ativos/lista`

**Resposta (200 OK):**
```json
[
  {
    "id": "64f9a1b2c3d4e5f6g7h8i9j0",
    "nome": "João Silva",
    "email": "joao@example.com",
    "papel": "USER",
    "ativo": true,
    "criadoEm": "2025-04-21T10:30:00",
    "atualizadoEm": null
  }
]
```

### 6. Listar Usuários por Papel
**GET** `/api/usuarios/papel/{papel}`

**Exemplo**: `GET /api/usuarios/papel/ADMIN`

**Resposta (200 OK):**
```json
[
  {
    "id": "64f9a1b2c3d4e5f6g7h8i9j1",
    "nome": "Maria Santos",
    "email": "maria@example.com",
    "papel": "ADMIN",
    "ativo": true,
    "criadoEm": "2025-04-21T11:00:00",
    "atualizadoEm": null
  }
]
```

### 7. Atualizar Usuário
**PUT** `/api/usuarios/{id}`

**Requisição:**
```json
{
  "nome": "João Silva Atualizado",
  "email": "joao.novo@example.com",
  "senha": "novaSenha123",
  "papel": "ADMIN"
}
```

**Resposta (200 OK):**
```json
{
  "id": "64f9a1b2c3d4e5f6g7h8i9j0",
  "nome": "João Silva Atualizado",
  "email": "joao.novo@example.com",
  "papel": "ADMIN",
  "ativo": true,
  "criadoEm": "2025-04-21T10:30:00",
  "atualizadoEm": "2025-04-21T12:00:00"
}
```

### 8. Alternar Status do Usuário
**PATCH** `/api/usuarios/{id}/status?ativo=false`

**Resposta (200 OK):**
```json
{
  "id": "64f9a1b2c3d4e5f6g7h8i9j0",
  "nome": "João Silva",
  "email": "joao@example.com",
  "papel": "USER",
  "ativo": false,
  "criadoEm": "2025-04-21T10:30:00",
  "atualizadoEm": "2025-04-21T12:30:00"
}
```

### 9. Deletar Usuário (Soft Delete)
**DELETE** `/api/usuarios/{id}`

**Resposta**: 204 No Content

---

## Validações

- **Email**: Deve ser único e válido
- **Senha**: Será criptografada com BCrypt
- **Papel**: Apenas "ADMIN" ou "USER" são aceitos
- **Nome**: Campo obrigatório

## Códigos de Erro

- **201 Created**: Usuário criado com sucesso
- **200 OK**: Requisição bem-sucedida
- **204 No Content**: Recurso deletado
- **400 Bad Request**: Validação falhou (email duplicado, papel inválido, etc.)
- **404 Not Found**: Usuário não encontrado

## Testes Unitários

Testes disponíveis em:
- `src/test/java/team/phoenix/backend/service/UsuarioServiceTest.java`
- `src/test/java/team/phoenix/backend/controller/UsuarioControllerTest.java`

Para executar:
```bash
mvn test -Dtest=UsuarioServiceTest
mvn test -Dtest=UsuarioControllerTest
```

## Segurança

- Senhas são criptografadas com **BCryptPasswordEncoder**
- DTOs de resposta não expõem a senha
