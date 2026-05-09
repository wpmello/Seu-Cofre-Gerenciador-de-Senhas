# Preferências de Ajustes

## Objetivo

Registrar a estratégia inicial para preferências do app expostas pela tela de Ajustes.

## Decisão

As preferências não sensíveis de Ajustes são persistidas em Preferences DataStore:

- nome de exibição do usuário;
- idioma do app;
- preferência de tema.

Esses dados ficam em `core/preferences`, porque são preferências globais do app e não pertencem somente à composição visual da tela de Ajustes.

## Por que DataStore

DataStore é o mecanismo padrão definido no projeto para preferências/configurações locais. Ele evita novo uso de `SharedPreferences`, expõe leitura assíncrona via `Flow` e encaixa melhor no fluxo `Repository -> UseCase -> ViewModel -> UiState`.

## Regras de comportamento

- O nome exibido no card usa fallback visual quando não há nome salvo.
- Alterar o texto no bottom sheet não muda o card até o usuário confirmar em `Salvar`.
- Idioma e tema são escolhidos em dialogs com seleção explícita e só são persistidos ao salvar.
- O idioma escolhido é aplicado no nível raiz de Compose para que `stringResource` use os recursos localizados.
- A preferência de tema é aplicada no `MaterialTheme`; telas antigas ainda podem usar tokens escuros diretamente por serem parte do design dark-first já existente.

## Segurança

Essas preferências não armazenam senhas, tokens, chaves ou conteúdo sensível do cofre. Mesmo assim, não devem ser logadas de forma desnecessária.
