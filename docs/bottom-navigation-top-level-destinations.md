# Bottom Navigation Apenas Em Destinos Principais

## Causa

A bottom navigation era resolvida por prefixo de rota em
`appBottomDestinationForRoute`. Como a função usava `substringBefore("/")`,
rotas internas como `passwords/new`, `passwords/{id}/edit` e
`categories/all` eram classificadas como tabs principais.

Isso mantinha a bottom bar visível em telas secundárias e podia marcar
indevidamente `Passwords` ou `Categories` como destino selecionado.

## Correção

`appBottomDestinationForRoute` agora considera apenas correspondência exata
com as rotas de `AppBottomDestination`: `vault`, `passwords`, `categories` e
`settings`.

Telas de criação, edição, detalhes, lista completa e busca global não resolvem
para destino de bottom navigation.

## Fluxo Após Salvar

Edições e criações iniciadas pela Home carregam a origem por argumento de rota.
Ao salvar:

- senha aberta/criada pela Home navega para `Passwords`;
- categoria aberta pela Home navega para `Categories`;
- fluxos internos como edição a partir de categoria, detalhes de segurança e
  busca global preservam o retorno específico já existente.

O botão voltar de criação/edição de senha continua usando o fluxo de retorno
normal da pilha, para que voltar sem salvar retorne à tela anterior. A edição
de senha diferencia o efeito de voltar do efeito de salvar para permitir que
apenas o sucesso iniciado pela Home navegue para `Passwords`.

Quando um fluxo interno termina navegando para uma tab principal, essa
navegação não usa `saveState` nem `restoreState`. Isso impede que telas internas
removidas da pilha sejam restauradas indevidamente ao clicar depois em Home,
Senhas ou Categorias.

## Prevenção

Os testes de `AppBottomDestinationTest` cobrem rotas top-level exatas e rotas
internas conhecidas. Os testes de criação/edição de senha cobrem a origem
propagada pelo `SavedStateHandle`.
