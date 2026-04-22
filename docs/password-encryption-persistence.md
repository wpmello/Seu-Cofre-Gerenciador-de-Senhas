# Persistência criptografada de senhas

## Objetivo

Registrar a estratégia mínima adotada para salvar novas credenciais na feature **Nova Senha** sem persistir segredo em texto puro.

## O que foi decidido

- a senha digitada pelo usuário é criptografada **antes** de qualquer persistência no Room;
- a criptografia usa **AES/GCM/NoPadding** com chave gerenciada pelo **Android Keystore**;
- o banco armazena somente:
  - `encrypted_password`
  - `password_iv`
  - `password_cipher_version`
- o valor em texto claro não é salvo no banco;
- o banco do cofre foi explicitamente excluído do backup automático e da transferência entre dispositivos.

## Por que foi decidido

- o projeto exige proteção em repouso para dados altamente sensíveis;
- a responsabilidade criptográfica precisava ficar fora da UI e antes da fronteira do Room;
- o Android Keystore oferece o mecanismo mais adequado e alinhado às diretrizes de segurança já documentadas no projeto;
- armazenar IV e versão da cifra prepara a base para futuras leituras/descriptografias e possíveis evoluções de formato.

## Alternativas consideradas

### Persistir a senha em texto puro e proteger só por ofuscação

Rejeitada porque viola diretamente `AGENTS.md` e `docs/security.md`.

### Guardar o segredo em DataStore

Rejeitada porque o dado é estruturado, faz parte do cofre e precisa ficar na fonte de verdade relacional da feature.

### Introduzir uma biblioteca externa de criptografia específica para esta entrega

Rejeitada porque a plataforma já cobre o caso com APIs oficiais, e a feature precisava da fundação mínima correta sem ampliar dependências.

## Impactos

- Room recebeu migration `3 -> 4` para os campos criptografados;
- `PasswordRepository` passou a concentrar a orquestração de criptografia + persistência;
- a tela `Nova Senha` salva credenciais reais e a `PasswordsScreen` reflete o novo item via `Flow`.
