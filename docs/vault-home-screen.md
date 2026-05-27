# Tela Home do Cofre

## Objetivo

A tela Home da aba `Cofre` é o dashboard principal após a splash. Ela consolida uma visão resumida do cofre sem expor senhas em texto claro:

- total real de senhas;
- totais de senhas fracas, moderadas e fortes calculados pela política vigente de segurança;
- até 3 categorias reais recentes;
- card fixo `Outros` quando houver pelo menos 3 categorias reais;
- até 4 senhas recentes;
- ação principal para abrir as opções de criar uma nova senha ou uma nova categoria.

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

Essas tags são apresentadas em um componente horizontalmente scrollável e são clicáveis. Ao selecionar uma tag, o próprio `VaultHomeSummaryCard` deixa o modo de resumo e passa a exibir a lista de senhas daquele bucket, sem abrir uma nova tela.

No modo lista, o card:

- oculta o total, o ícone de escudo e as tags;
- mostra um botão de voltar interno e o título da classificação selecionada;
- exibe senhas sem revelar o valor da senha;
- navega para `EditPasswordDestination` com origem `vault` ao tocar em um item;
- mantém a classificação aberta ao retornar da edição porque o estado fica no `VaultHomeViewModel`;
- possui estados explícitos de loading, vazio e erro para a lista selecionada.

A altura da lista é limitada para evitar avançar sobre o topo dos controles de criação quando a posição do card e desses controles já foi medida pela composição. Quando essa medição ainda não está disponível, o fallback é a altura de até 5 itens. Listas com menos itens usam altura natural e não reservam espaço vazio para cinco linhas.

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
- FAB: abre localmente as opções `nova senha` e `nova categoria`;
- `nova senha`: `NewPasswordScreen` com origem `vault`, salvando e retornando para a aba `Passwords`;
- `nova categoria`: `NewCategoryScreen` com origem `vault`, salvando e retornando para a aba `Categories`.

As origens `vault` são usadas para fluxos iniciados pela Home, mantendo o voltar natural para `Cofre` e o retorno pós-salvamento para as abas de destino definidas pelo fluxo.

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
- seleção das tags de segurança e transformação do summary card em lista;
- estado vazio e erro da lista por classificação;
- navegação de item da lista de classificação para detalhes de senha;
- abertura e fechamento das opções de criação pelo FAB da Home;
- callbacks de criação de senha e categoria pelas opções do FAB;
- queries/delegação de contagem e recentes em DAO/data source/repository;
- rotas com origem `vault`.
