# Inferencia de login pela busca da tela de senhas

## Contexto

A `PasswordsScreen` exibe o identificador de login com mascara para reduzir exposicao de e-mails, usernames e outros identificadores sensiveis na lista do cofre.

Mesmo com a mascara visual aplicada, a busca da tela ainda precisa respeitar o mesmo limite de exposicao: consultas digitadas pelo usuario nao devem permitir confirmar se um login bruto existe no cofre quando esse dado nao esta visivel na lista.

## Causa raiz

O `PasswordsViewModel` montava os itens da lista com `supportingText` mascarado, mas filtrava os dados antes do mapeamento visual usando o modelo de dominio cru.

O filtro aceitava correspondencia por:

- `password.title`
- `password.login`

Com isso, uma busca por partes do login bruto podia retornar a senha correta mesmo que a UI exibisse apenas o identificador mascarado. Esse comportamento permitia inferencia de e-mails ou usernames reais pela barra de pesquisa.

## Correcao

O filtro da `PasswordsScreen` passou a considerar somente `PasswordSummary.title`.

O mapeamento visual continua usando:

```kotlin
supportingText = login.maskCredentialIdentifierForDisplay()
```

Nenhum fluxo de persistencia, DAO, repositorio, criacao, edicao ou mascara de identificador foi alterado por esta correcao.

## Prevencao

Foram adicionadas regressoes em `PasswordsViewModelTest` garantindo que:

- busca por username bruto nao retorna a senha;
- busca por e-mail bruto nao retorna a senha;
- busca por titulo continua retornando a senha;
- resultado vazio continua emitindo `PasswordsContentState.EmptySearchResult`.

Ao adicionar novos campos pesquisaveis em telas que exibem dados sensiveis mascarados, a regra e avaliar se o campo pode confirmar informacao que nao esta visivel ao usuario. Campos mascarados nao devem ser pesquisaveis pelo valor bruto sem uma decisao explicita de seguranca e produto.
