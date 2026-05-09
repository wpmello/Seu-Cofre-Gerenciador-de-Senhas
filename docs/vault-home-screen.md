# Tela Home do Cofre

## Objetivo

A tela Home da aba `Cofre` é o dashboard principal após a splash. Ela consolida uma visão resumida do cofre sem expor senhas em texto claro:

- total real de senhas;
- totais de senhas fracas, moderadas e fortes calculados pela política vigente de segurança;
- até 3 categorias reais recentes;
- card fixo `Outros` quando houver pelo menos 3 categorias reais;
- até 4 senhas recentes;
- ação principal para criar uma nova senha.

## Decisões

### Fonte de dados

A tela usa `ObserveVaultHomeUseCase` como ponto único de agregação de domínio.

O card superior usa:

- `PasswordRepository.observePasswordCount()` para o total real de senhas;
- `ObserveVaultSecurityDetailsUseCase` para contar senhas nos buckets `Weak`, `Moderate` e `Safe`, reaproveitando a mesma política usada pela tela de detalhes de segurança.

Assim, a Home não cria regra paralela para classificar força de senha.

### Tags de segurança do resumo

O `VaultHomeSummaryCard` exibe as contagens dos três grupos oficiais do app:

- fracas: bucket `Weak`, com cor de erro;
- moderadas: bucket `Moderate`, com cor âmbar;
- fortes: bucket `Safe`, com cor verde.

Essas tags são apresentadas em um componente horizontalmente scrollável. Por enquanto elas são informativas e não disparam ação de clique.

### Categorias resumidas

As categorias da Home são ordenadas por:

1. `lastModifiedAt` decrescente;
2. `id` decrescente como desempate determinístico.

A tela exibe no máximo 3 categorias reais. O card `Outros` aparece apenas quando existem pelo menos 3 categorias reais; com menos de 3, a tela mostra somente as disponíveis.

### Senhas recentes

Recentes são definidas por:

```text
max(createdAt, updatedAt) desc
id desc
```

A camada de dados expõe query específica via `observeRecentPasswords(limit)`, e o domínio reaplica a ordenação/limite como proteção determinística para testes e fakes.

### Navegação

`AppBottomDestination.Vault` passa a ser o start destination interno do app após a splash.

A Home navega para:

- categoria real: `EditCategoryRoute` com origem `vault`;
- `Outros`: `AllCategoriesScreen`;
- `Ver todos`: aba `Passwords`;
- senha recente: `EditPasswordDestination` com origem `vault`;
- FAB: `NewPasswordScreen`.

As origens `vault` foram adicionadas aos fluxos de edição de categoria e senha para retorno correto à aba `Cofre`.

## Segurança

A Home não acessa Room, DataStore, Keystore ou criptografia diretamente. A UI recebe apenas estado renderizável e não exibe senhas descriptografadas.

O cálculo das contagens de segurança usa a fonte de verdade existente, derivada de `docs/password-strength-security-policy-v1.md`.

## Testes

Foram adicionados testes para:

- agregação de total e contagens de senhas fracas, moderadas e fortes;
- seleção determinística de categorias;
- ordenação e limite de senhas recentes;
- estado e efeitos do `VaultHomeViewModel`;
- callbacks visíveis da `VaultHomeScreen`;
- queries/delegação de contagem e recentes em DAO/data source/repository;
- rotas com origem `vault`.
