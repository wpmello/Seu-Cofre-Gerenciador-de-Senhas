# Exclusão de categoria com senhas associadas

## Decisão

O fluxo de edição de categoria diferencia três operações:

- exclusão simples da categoria, usada somente quando não há senhas associadas;
- exclusão da categoria com senhas associadas, executada em transação local;
- transferência em lote das senhas para outra categoria antes da decisão de excluir a categoria original.

## Motivo

Categoria e senhas têm impacto direto na integridade do cofre. A exclusão em cascata feita no `ViewModel` por listas em memória permitiria estados inconsistentes, duplicação de operação por recomposição/clique duplo e falhas parciais.

Por isso, a confirmação destrutiva chama use cases explícitos, e a camada de dados executa as operações críticas com `SeuCofreDatabase.withTransaction` por meio de `DatabaseTransactionRunner`.

## Contratos

- `DeleteCategoryUseCase` exclui apenas a categoria.
- `DeleteCategoryWithAssociatedPasswordsUseCase` valida a categoria e delega a exclusão transacional de senhas e categoria.
- `TransferPasswordsToCategoryUseCase` valida origem e destino, impede origem igual ao destino e delega a atualização em lote.
- `PasswordDao.updatePasswordsCategory` altera somente `category_id`; dados criptografados, IV e fingerprint não são modificados.

## Riscos tratados

- Falha na exclusão total faz rollback da operação transacional.
- Falha na transferência mantém a categoria original e não navega.
- Categoria atual não é oferecida como destino de transferência.
- Operações críticas são representadas como estado único de tela para evitar confirmações duplicadas.
