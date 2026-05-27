# Flash de conteúdo padrão no loading da tela de categorias

## Contexto

A tela de categorias exibia um `CircularProgressIndicator` enquanto `CategoriesUiState.categoriesState` estava em `Loading`, mas mantinha a composição do conteúdo principal dentro da `LazyVerticalGrid`.

Como `SecuritySummaryCard`, `CategoriesSectionHeader` e os demais blocos dependentes de dados eram renderizados antes do branch de loading, a tela podia abrir mostrando valores default por um instante. Na prática, isso aparecia como um card de resumo de segurança verde com textos padrão antes do carregamento real terminar.

## Causa raiz

O estado `Loading` era tratado como apenas mais um item da grade. Isso fazia com que o spinner coexistisse com componentes que dependem de dados já mapeados no `UiState`, mesmo quando esses dados ainda estavam apenas nos valores default do estado inicial.

O problema não estava no `CategoriesViewModel` nem no mapeamento dos dados, e sim no ponto em que a composição decidia o que deveria ou não ser renderizado.

## Correção

O branch de `Loading` foi promovido para o nível do conteúdo principal da `CategoriesScreen`.

Com isso:

- durante `Loading`, apenas um container centralizado com `CircularProgressIndicator` é renderizado;
- `VaultFloatingTopBar` e `CategoryCreateFab` permanecem visíveis;
- `SecuritySummaryCard`, `CategoriesSectionHeader`, `HighlightedCategoryCard` e a grade de categorias só são compostos depois que `categoriesState` deixa de ser `Loading`.

## Testes

Foram adicionados e ajustados testes para garantir que:

- a tela em `Loading` exiba `categories_loading`;
- `categories_top_bar` e `categories_create_fab` continuem visíveis durante o loading;
- `categories_security_summary_card` não seja renderizado enquanto o conteúdo principal ainda estiver carregando;
- o estado inicial do `CategoriesViewModel` continue nascendo em `Loading`.
