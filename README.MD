# 📦 Guia de Testes - Mercado Livre BDNR (Spring Boot + Mongo + Redis)

Este documento descreve o fluxo passo a passo para testar as funcionalidades da aplicação, incluindo a persistência no MongoDB, o cache Redis-First e o sistema de autenticação/permissões (Atividades EX2 e EX3).

## ⚙️ 1. Configuração Prévia

Antes de iniciar, certifique-se de que o arquivo `src/main/resources/application.properties` está configurado corretamente com:
1.  **MongoDB Atlas:** URI de conexão válida e `spring.data.mongodb.database=BDNR`.
2.  **Redis Cloud:** Host, Porta e Senha configurados.

---

## 🧪 2. Fluxo de Execução

### Passo 1: Primeira Execução e "Setup" (Popular Banco)
Como o banco de dados pode estar vazio e o sistema exige login, criamos um comando de inicialização.

1.  Inicie a aplicação (`MercadolivreApplication`).
2.  No prompt de login, utilize o comando secreto de setup:
    * **E-mail:** `setup`
    * **Enter**
3.  **Resultado Esperado:** O console mostrará logs do `DatabaseSeeder` limpando o banco e criando:
    * 1 Administrador
    * 2 Vendedores (com senhas criptografadas)
    * 2 Clientes (com senhas criptografadas)
    * Produtos iniciais e Compras.

---

### Passo 2: Teste de Administrador & Sincronização (EX3)
O Administrador é o único perfil autorizado a mover dados entre Redis e Mongo.

1.  Faça login como Admin:
    * **E-mail:** `admin@fatec.com`
    * **Senha:** `123456`
2.  No Menu Principal, observe que o "Tipo" é **Administrador**.
3.  Acesse a opção **`6. ⚡ EX3: GERENCIAR SINCRONIZAÇÃO/MIGRAÇÃO`**.
4.  Teste a **Opção 1 (Migrar Mongo -> Redis)**:
    * Isso carregará produtos do banco de dados para o cache Redis para serem manipulados.
5.  Teste a **Opção 2 (Sincronizar Redis -> Mongo)**:
    * Isso pegará tudo que está no Redis, salvará definitivamente no MongoDB (Commit) e limpará o cache.

---

### Passo 3: Teste de Vendedor (Redis-First & Permissões)
O Vendedor manipula produtos, mas as alterações ocorrem primeiro no Redis.

1.  Faça Logout (Opção 7) e logue como Vendedor:
    * **E-mail:** `vendedor@teste.com`
    * **Senha:** `123456`
2.  No Menu Principal, vá para **`3. Gerenciar Produtos`**.
3.  **Crie um Produto (Opção 1)**:
    * Preencha os dados.
    * **Observe:** O sistema **não** pede o ID do Vendedor. Ele identifica automaticamente que é o "João da Silva" criando.
    * **Resultado:** "Produto criado com sucesso! (Salvo no Redis)".
4.  **Tente Sincronizar (Teste de Segurança)**:
    * Volte ao Menu Principal.
    * Tente acessar a opção **`6. EX3`**.
    * **Resultado Esperado:** Mensagem de `⚠️ ACESSO NEGADO`. Vendedores não podem comitar dados no banco principal.

---

### Passo 4: Teste de Cliente (Bloqueios)
O Cliente consome dados, mas não pode alterá-los.

1.  Faça Logout e logue como Cliente:
    * **E-mail:** `vinicius@gmail.com`
    * **Senha:** `123456`
2.  Vá para **`3. Gerenciar Produtos`**.
3.  Tente **Criar um Produto (Opção 1)** ou **Deletar (Opção 4)**.
    * **Resultado Esperado:** `⛔ ACESSO NEGADO`.
4.  Vá para **`4. Gerenciar Compras`**.
5.  Faça uma compra normalmente (isso gera um registro direto no MongoDB).

---

### Passo 5: Validando a Persistência (A Prova Real)

Para provar que o sistema funciona conforme o exercício pede (Redis-First):

1.  Logue como **Vendedor**.
2.  Crie um produto chamado "Teste Redis".
3.  Vá no seu **MongoDB Atlas (Web)** e verifique a coleção `produtos`.
    * **O produto "Teste Redis" NÃO deve estar lá ainda.** (Ele está apenas no Redis Cloud).
4.  Faça Logout e logue como **Administrador**.
5.  Vá na opção **6** e escolha **`2. Sincronizar REDIS -> MONGODB`**.
6.  Atualize o **MongoDB Atlas**.
    * **Agora o produto "Teste Redis" deve aparecer.**

---

## 📋 Resumo de Logins

| Perfil | E-mail | Senha | Permissões Principais |
| :--- | :--- | :--- | :--- |
| **Setup** | `setup` | *(n/a)* | Popula o Banco de Dados |
| **Admin** | `admin@fatec.com` | `123456` | Sync Mongo/Redis, CRUD Total |
| **Vendedor** | `vendedor@teste.com` | `123456` | CRUD Produtos Próprios (Redis) |
| **Cliente** | `vinicius@gmail.com` | `123456` | Comprar, Favoritar, Ler Dados |