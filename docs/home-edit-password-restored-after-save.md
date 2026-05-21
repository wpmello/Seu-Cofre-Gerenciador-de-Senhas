# Home Restaurando Edicao De Senha Apos Save

## Causa

O fluxo Home -> editar senha -> salvar navegava corretamente para `Passwords`,
mas usava a mesma configuracao de navegacao das tabs principais:
`popUpTo(startDestination) { saveState = true }` com `restoreState = true`.

Nesse contexto, a tela interna `EditPasswordScreen` era removida da pilha e
salva como estado restauravel associado a `Vault`. Ao clicar depois na tab
Home, o Navigation Compose restaurava essa subpilha e abria novamente a tela de
edicao.

O botao Voltar fisico parecia corrigir o problema porque apenas desempilhava a
rota atual e revelava a Home que continuava abaixo na pilha. O botao Home da
bottom navigation seguia outro caminho: ele navegava para o start destination
com restauracao de estado habilitada, entao reabria a subpilha interna salva.

## Correcao

A navegacao top-level foi separada por intencao e destino:

- selecao manual de bottom navigation para tabs que nao sao o start destination
  preserva estado de tab;
- selecao manual de `Vault`, a Home e start destination atual, nao salva nem
  restaura estado;
- conclusao de fluxo interno nao salva nem restaura subpilhas internas.

Com isso, salvar senha criada/editada pela Home continua indo para `Passwords`,
mas nao deixa `EditPasswordScreen` restauravel quando o usuario volta para
Home. O mesmo vale para categorias editadas a partir da Home: salvar continua
indo para `Categories`, e clicar Home abre `VaultHomeRoute` diretamente.

## Casos Relacionados

O mesmo padrao podia acontecer em conclusoes internas que caiam em uma tab por
fallback:

- criar senha pela Home e salvar para `Passwords`;
- editar senha pela Home e salvar para `Passwords`;
- editar categoria vinda da Home, lista completa ou busca global e salvar para
  `Categories`.

Esses fluxos agora usam a politica de conclusao interna, sem `saveState` e sem
`restoreState`.

## Prevencao

`TopLevelNavigationPolicyTest` fixa a diferenca entre navegacao por tab nao
inicial, navegacao manual para `Vault` e conclusao de fluxo interno. Os testes
de ViewModel continuam garantindo que os efeitos de voltar e salvar carregam a
origem correta.
