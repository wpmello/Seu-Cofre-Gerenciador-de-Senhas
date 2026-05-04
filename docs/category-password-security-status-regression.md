# Status de Segurança na Lista de Senhas da Categoria

## Contexto

A `EditCategoryScreen` exibe as senhas associadas a uma categoria usando a lista reutilizável de senhas. O status visual de segurança desses itens deve refletir a mesma análise usada na listagem principal de senhas e na tela de edição de senha.

## Causa raiz

O fluxo da listagem principal usava `ObservePasswordsUseCase`, que combinava os resumos de senha com `PasswordSecuritySnapshot` para calcular `PasswordSecurityRiskLevel`.

O fluxo da listagem por categoria usava `ObservePasswordsByCategoryUseCase`, mas esse caso de uso apenas repassava `repository.observePasswordsByCategoryId(categoryId)`. Como `PasswordSummary.securityRiskLevel` possui fallback seguro para `PasswordSecurityRiskLevel.High`, os itens da categoria chegavam à `EditCategoryScreen` como alto risco quando não tinham o risco calculado no próprio resumo.

Na UI isso aparecia como status fraco, com bolinha vermelha e flag `FRACA`, mesmo quando a senha era avaliada como segura na `EditPasswordScreen`.

## Correção

`ObservePasswordsByCategoryUseCase` passou a combinar:

- os resumos filtrados por categoria;
- os snapshots globais de segurança do cofre.

O cálculo de risco foi centralizado em um mapeador compartilhado por `ObservePasswordsUseCase` e `ObservePasswordsByCategoryUseCase`, evitando divergência entre a listagem principal e a listagem por categoria.

Os snapshots continuam globais para preservar a regra de duplicidade no cofre inteiro, não apenas dentro da categoria aberta.

## Testes

Foram adicionados/ajustados testes para garantir que:

- senhas da categoria com snapshot de senha forte sejam expostas como baixo risco;
- senhas sem snapshot mantenham fallback de alto risco;
- a `EditCategoryViewModel` converta os riscos calculados para os estados visuais corretos da lista;
- um item seguro na `EditCategoryScreen` não renderize a flag visual de senha fraca.
