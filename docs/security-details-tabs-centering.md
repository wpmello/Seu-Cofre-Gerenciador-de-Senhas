# Centralização visual das tabs na SecurityDetailsScreen

## Contexto

A `SecurityDetailsScreen` renderizava o grupo de tabs dentro de uma `Row` com `fillMaxWidth()` e `horizontalScroll()`.

Na prática, isso fazia o container visível ocupar toda a largura disponível enquanto o conteúdo das tabs era distribuído a partir de `start`. Em telas compactas, médias e largas, o conjunto podia parecer levemente ancorado à esquerda, mesmo quando havia espaço suficiente para um alinhamento visual mais equilibrado.

## Causa raiz

O problema original de desalinhamento vinha da mistura entre viewport e conteúdo:

- a mesma `Row` definia a área visível;
- a mesma `Row` também definia a posição inicial do grupo;
- com `fillMaxWidth()`, o grupo nascia da borda inicial da área útil da tela.

Na tentativa inicial de corrigir isso, um wrapper com `wrapContentWidth(unbounded = true)` foi colocado acima de `horizontalScroll()`. Isso resolveu visualmente o alinhamento, mas introduziu uma regressão: o container scrollável passou a receber `maxWidth` infinito durante a medição, o que faz o Compose lançar `IllegalStateException`.

## Correção

A solução final passou a usar `BoxWithConstraints` para manter a viewport sempre finita e expandir a linha das tabs apenas até o mínimo necessário:

- o container externo continua ocupando toda a largura útil;
- a linha scrollável recebe `widthIn(min = maxWidth)`;
- quando o conteúdo é menor que a viewport, a própria linha cresce até a largura disponível e centraliza os itens com `Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)`;
- quando o conteúdo é maior que a viewport, a linha cresce conforme o conteúdo e a rolagem horizontal continua funcionando sem constraints infinitas.

Com isso, o centro geométrico do grupo de tabs permanece alinhado ao centro da área útil da tela sem alterar seleção, clique, filtros, tipografia, cores, shape ou espaçamento, e sem reintroduzir crash de medição.

## Testes

Foram adicionados testes instrumentados para validar que:

- o centro do grupo de tabs coincide aproximadamente com o centro do container;
- a validação cobre largura compacta e larga;
- o clique em tab continua emitindo `OnTabSelected`.
