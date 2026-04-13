# UI Source of Truth — Figma → Jetpack Compose (Cofre de Senhas)

## Objetivo

Este documento define como qualquer agente, colaborador ou ferramenta deve implementar UI do app **Cofre de Senhas** a partir do **Figma**, usando **Android nativo com Jetpack Compose**.

Este documento existe para garantir:
- fidelidade ao design aprovado
- implementação idiomática em Compose
- separação correta entre UI, estado, navegação e regra de negócio
- reaproveitamento consistente de tokens e componentes
- segurança visual compatível com o domínio sensível do app
- previsibilidade para testes, manutenção e evolução

Este arquivo é uma **fonte de verdade operacional para UI**. Ele não substitui `AGENTS.md`, `docs/architecture.md`, `docs/security.md` e `docs/testing.md`; ele complementa esses arquivos do ponto de vista de interface.

---

## Escopo

Este documento se aplica a:
- telas novas
- refatorações visuais
- componentes reutilizáveis
- estados de tela
- tokens visuais
- navegação visual
- microinterações e animações leves
- interpretação do Figma em Compose
- extração de requisitos de UI para domínio, persistência e testes

Este documento **não autoriza** o agente a inventar:
- regra de negócio
- criptografia
- biometria
- contratos de repositório
- fluxos de persistência
- navegação fora do que estiver definido no projeto

Quando a UI sugerir um requisito funcional que ainda não esteja documentado, o agente deve tratar isso como **pista de requisito** e atualizar a documentação apropriada antes de implementar comportamento estrutural.

---

## Ordem de prioridade das fontes de verdade

Quando houver conflito, seguir esta ordem:

1. solicitação atual do usuário
2. frame, fluxo ou seleção exata do Figma usada na tarefa
3. componentes, variants, variables, tokens e styles do mesmo arquivo Figma
4. este documento
5. `AGENTS.md`
6. `docs/architecture.md`
7. `docs/security.md`
8. `docs/testing.md`
9. padrões existentes no código, desde que não conflitem com os itens acima
10. boas práticas oficiais do Android/Compose

### Regra principal
O agente **não deve inventar design** quando o Figma já define a solução.

Se algo estiver ambíguo no Figma, seguir esta ordem:
1. verificar variants, componentes, tokens, estilos e telas vizinhas
2. verificar se já existe padrão equivalente no projeto
3. escolher a solução mais conservadora, segura e consistente com Compose
4. documentar a suposição se ela impactar comportamento, domínio ou persistência

---

## Stack alvo obrigatória

Toda implementação visual deve assumir:
- Kotlin
- Android nativo
- Jetpack Compose
- Material 3 como base técnica de componentes e tema
- MVVM + Clean Architecture
- Hilt para DI
- Navigation Compose para navegação
- Coroutines + Flow/StateFlow para estado observável
- strings via recursos Android, com internacionalização desde o início

### Não permitido
- gerar código em React
- gerar HTML/CSS/Tailwind
- gerar telas novas em XML, salvo solicitação explícita do usuário
- copiar estrutura mental de UI web para Android
- introduzir biblioteca visual nova sem justificativa técnica e documental
- acoplar UI diretamente a Room, Retrofit, DataStore, Keystore, biometria ou criptografia

---

## Princípios mandatórios de implementação

### 1. Fidelidade ao Figma
A UI deve refletir o Figma com o máximo de precisão viável em Android nativo.

Preservar, quando definidos no design:
- hierarquia visual
- composição geral da tela
- espaçamentos e ritmo visual
- tipografia
- cores
- shapes
- alinhamentos
- proporções
- estados visuais
- ordem dos elementos
- intenção da interação

### 2. Android idiomático
Alta fidelidade visual não autoriza soluções ruins. A implementação precisa continuar idiomática para Compose.

A tela deve:
- usar composição clara
- respeitar estado e recomposição
- evitar hacks e workarounds frágeis
- ser legível
- ser fácil de testar
- se encaixar na arquitetura do projeto

### 3. Reuso antes de criação
Antes de criar qualquer componente novo:
1. verificar se já existe componente equivalente no projeto
2. verificar se a variação cabe no componente existente
3. criar novo componente apenas se o reuso degradar clareza, flexibilidade ou fidelidade

### 4. Segurança visual por padrão
Como este é um app de cofre de senhas, a UI deve ser conservadora quanto à exposição de dados sensíveis.

### 5. Estado previsível
Toda tela relevante deve ter contrato explícito de estado, eventos e callbacks.

### 6. Internacionalização desde o começo
Textos de tela devem usar recursos Android e `stringResource(...)`. O projeto deve nascer preparado para, no mínimo:
- `pt-BR`
- `en`
- `es`

Texto hardcoded em composables só é aceitável em preview local temporário e deve ser removido antes da conclusão da tarefa.

---

## Como usar o Figma como insumo de projeto

O Figma não é apenas referência visual. Ele também pode revelar:
- entidades de domínio
- atributos persistidos
- estados de tela
- ações do usuário
- requisitos de navegação
- mensagens e feedbacks
- regras de segurança visível
- cenários de teste

### Regra de interpretação
Quando um dado aparece de forma estável na UI e possui significado funcional para o produto, ele deve ser tratado como **candidato a requisito formal do projeto**.

Exemplos:
- se a tela mostra `Criado em` e `Atualizado em`, esses campos são fortes candidatos a existir em domínio, persistência e testes
- se a tela mostra status de item, esse status provavelmente pertence ao contrato de tela e talvez ao domínio
- se a UI exige revelar senha mediante ação explícita, isso deve refletir estados, eventos, fluxo de segurança e testes

### O que **não** vira modelo automaticamente
Nem tudo que aparece na UI vira atributo persistido.

Normalmente **não** viram domínio/persistência por padrão:
- cor de destaque
- ícone decorativo
- expansão local de card
- seleção temporária
- foco momentâneo
- snackbar momentâneo
- senha visível/oculta apenas como estado local de UI, quando isso não fizer parte do estado de negócio

---

## Protocolo obrigatório ao analisar uma tela do Figma

Antes de implementar uma tela, o agente deve extrair pelo menos:

### 1. Identidade da tela
- nome da tela
- objetivo da tela
- entrada e saída do fluxo
- relação com telas anteriores e posteriores

### 2. Estrutura visual
- top app bar, bottom bar, FAB, sheets, dialogs e conteúdo principal
- regiões scrolláveis
- listas, grids, formulários e seções
- componentes reutilizáveis

### 3. Tokens e design system
- cores
- tipografia
- spacing
- radius
- elevação
- ícones
- estados visuais dos componentes

### 4. Dados exibidos
- quais campos aparecem
- quais parecem vir de domínio
- quais parecem derivados ou formatados
- quais parecem dados sensíveis

### 5. Ações do usuário
- cliques principais
- ações perigosas
- ações secundárias
- comportamento esperado após a ação

### 6. Estados da tela
- loading
- empty
- content
- error
- disabled
- success
- estados sensíveis, quando aplicável

### 7. Impactos fora da UI
- novos campos de domínio sugeridos pelo design
- novos campos persistidos sugeridos pelo design
- novas regras de navegação
- novos cenários de teste
- necessidade de atualizar docs ou ADR

---

## Artefatos que devem nascer de uma análise de tela

Quando o Figma revelar comportamento ou estrutura relevantes, o agente deve atualizar, quando aplicável:
- `docs/testing.md` ou especificação local da feature, com cenários novos
- `docs/architecture.md`, se houver impacto arquitetural
- `docs/security.md`, se houver impacto em exposição de segredo, biometria, clipboard, exportação ou confirmação de ações perigosas
- documentação de feature/tela, quando existir
- ADR em `docs/adr/`, se a decisão for estrutural, custosa ou controversa

### Regra prática
Se uma decisão afeta apenas composição visual, não precisa virar ADR.
Se uma decisão afeta estrutura, fluxo de dados, domínio, persistência, navegação, dependências ou segurança, ela deve ser documentada.

---

## Workflow obrigatório para implementar cada tela

### Etapa 1 — Ler o design
Identificar no Figma:
- objetivo
- estrutura
- componentes
- variants
- estados
- tokens
- interações aparentes
- possíveis dados sensíveis

### Etapa 2 — Traduzir para contrato técnico
Definir:
- `Route`
- `Screen`
- `UiState`
- `UiAction` ou `UiEvent`
- `UiEffect` quando existir efeito transitório real
- callbacks públicos
- estados obrigatórios da tela

### Etapa 3 — Verificar impacto em domínio/persistência/testes
Se a tela revelar requisitos novos, atualizar primeiro a documentação adequada.

### Etapa 4 — Implementar a estrutura visual
Começar por:
- layout
- hierarquia
- componentes
- tokens
- previews
- estados principais

### Etapa 5 — Integrar estado e interação
Depois do layout:
- conectar `ViewModel`
- conectar callbacks
- conectar navegação na camada Route
- integrar eventos e side effects de forma controlada

### Etapa 6 — Validar qualidade
Antes de concluir:
- revisar fidelidade ao Figma
- revisar acessibilidade
- revisar performance
- revisar segurança visual
- revisar internacionalização
- revisar previews
- revisar testabilidade

---

## Contrato arquitetural de tela

Sempre que compatível com o projeto, organizar cada tela próximo de:

```text
presentation/
  feature_name/
    FeatureRoute.kt
    FeatureScreen.kt
    FeatureUiState.kt
    FeatureUiAction.kt
    FeatureViewModel.kt
    components/
      FeatureSection.kt
      FeatureCard.kt
```

### Responsabilidades
- **Route**: coleta estado do ViewModel, injeta navegação, dispara efeitos e conecta callbacks
- **Screen**: composable puro de tela; recebe estado e callbacks e renderiza UI
- **UiState**: estado imutável da tela
- **UiAction / UiEvent**: ações vindas da UI
- **UiEffect**: eventos transitórios quando realmente necessários, como navegação, snackbar ou abrir diálogo controlado por evento
- **ViewModel**: coordena estado, casos de uso e mapeamento para contrato de tela
- **components/**: peças reutilizáveis da própria feature

### Regras
- composables reutilizáveis devem ser preferencialmente stateless
- regra de negócio não deve ficar espalhada no corpo do composable
- decisões de rota não devem ficar no composable puro
- a UI não deve conhecer `Dto`, `Entity`, DAO, datasource, Keystore ou implementação concreta de repository

---

## Regras oficiais de Compose aplicadas ao projeto

### Estado
- usar estado imutável para `UiState`
- evitar múltiplas fontes de verdade
- hoistar estado para o menor ancestral comum apropriado
- manter estado local no Compose apenas quando ele for estritamente visual ou efêmero
- usar `remember` e `rememberSaveable` com critério
- side effects devem usar APIs apropriadas, como `LaunchedEffect`, `DisposableEffect` e correlatas

### Estado que pode ficar no Compose
Exemplos usuais:
- expansão/colapso local
- foco visual
- sheet aberta/fechada
- animação local
- seleção temporária
- senha visível/oculta **somente** quando isso for estado estritamente local de apresentação

### Estado que não deve ficar no composable puro
- carregamento de dados
- persistência
- autenticação
- biometria
- criptografia
- decisões de negócio
- integração com repositório
- validações centrais da feature
- estado compartilhado entre múltiplas partes da tela quando já caracterizado como screen state

### UDF
A UI deve seguir fluxo unidirecional:
- estado desce
- eventos sobem
- efeitos transitórios são controlados fora do composable puro

---

## Strings, recursos e internacionalização

### Regras obrigatórias
- usar recursos Android para strings visíveis ao usuário
- acessar strings em Compose por meio de `stringResource(...)`
- usar recursos também para `contentDescription` relevantes
- evitar concatenar texto manualmente quando houver formatação localizada apropriada
- preparar textos para `pt-BR`, `en` e `es`

### Não permitido
- deixar copy final hardcoded em `Text(...)`
- hardcode de `contentDescription` traduzível
- usar texto de preview como texto final da tela

### Formatação
Sempre que houver datas, números, pluralização ou mensagens parametrizadas, usar a abordagem apropriada de recursos/localização, e não concatenação improvisada.

---

## Design system, tema e tokens

Toda tela deve traduzir o Figma para tokens consistentes do app.

Mapear, quando fizer sentido:
- `ColorScheme`
- tipografia
- spacing
- shapes
- elevação
- tamanhos de ícone
- tamanhos de componentes
- estados `focused`, `pressed`, `disabled`, `error`, `selected`

### Regras
- se um token já existir no app, reutilizar
- se não existir, criar com nome semântico e potencial de reuso
- valores mágicos repetidos devem ser extraídos
- não criar um design system paralelo desconectado do tema do app

### Material 3
Material 3 é a base técnica padrão do projeto, mas não substitui o Figma como definição visual. Ele deve ser customizado quando necessário para refletir o design aprovado.

---

## Formulários

Telas de formulário devem seguir:
- labels claras
- placeholders como apoio, não como substituto de label
- mensagens de erro perto do campo
- foco previsível
- teclado apropriado
- IME actions coerentes
- feedback imediato sem agressividade visual
- botões habilitados/desabilitados de forma compreensível

### Campos sensíveis
No contexto de senha:
- mascarar valor por padrão quando aplicável
- exigir ação explícita para revelar
- não logar o conteúdo digitado
- não usar preview com segredo real
- não expor segredo por padrão em screenshots, listas ou estados intermediários

---

## Listas, grids e itens clicáveis

### Regras
- usar `LazyColumn`, `LazyRow` ou grids adequados quando houver coleção potencialmente grande
- fornecer `key` estável sempre que possível
- separar item em componente próprio quando houver complexidade ou reuso
- tratar loading, empty, content e error quando aplicável
- manter feedback visual de toque
- garantir acessibilidade do item e de ações internas

### Para lista de senhas
- nunca exibir senha descriptografada por padrão
- priorizar título e metadados seguros
- qualquer indicador de força, categoria ou status só deve aparecer se fizer parte do escopo visual e funcional definido

---

## Navegação

A UI não deve navegar sozinha a partir do composable puro.

### Regra
A tela deve expor callbacks, por exemplo:
- `onBackClick`
- `onAddClick`
- `onItemClick`
- `onEditClick`
- `onDeleteClick`
- `onConfirmClick`
- `onDismissClick`

A decisão de rota e a integração com `NavController` devem ficar na camada `Route` ou equivalente.

### Splash e loading screens
Quando splash/loading for etapa visual independente, implementar como destino próprio de navegação. Não renderizar splash dentro do scaffold da home se isso causar vazamento de chrome visual da próxima tela.

---

## Estados obrigatórios de tela

Toda tela relevante deve estar preparada, quando aplicável, para lidar com:
- `loading`
- `empty`
- `content`
- `error`
- `disabled`
- `success`
- `locked`
- `revealed`
- `biometricRequired`
- `biometricFailed`

### Regra
Mesmo quando o Figma mostrar apenas o estado principal, a estrutura do código deve permitir evolução segura para os estados complementares necessários ao produto.

---

## Domínio sensível: regras específicas do Cofre de Senhas

### Exposição de segredos
- nunca mostrar senha aberta por padrão sem necessidade explícita
- sempre exigir ação intencional para revelar ou copiar conteúdo sensível
- minimizar o tempo e a área de exposição do segredo na tela
- evitar que dados sensíveis apareçam em previews, mocks, screenshots de documentação ou logs

### Ações perigosas
Para ações como:
- excluir senha
- sobrescrever senha
- revelar senha
- copiar senha
- exportar dado sensível

A UI deve deixar claro:
- o que vai acontecer
- se a ação é reversível ou não
- quando é necessária confirmação explícita

### Biometria
- biometria é fluxo de aplicação, não responsabilidade do composable puro
- a UI deve apenas refletir estado e callbacks
- o composable não deve conter implementação acoplada de criptografia
- a tela deve prever estados como bloqueado, autenticando, autenticado e falha, quando aplicável

---

## Acessibilidade e semântica

Toda tela deve nascer acessível.

### Regras mínimas
- touch targets adequados
- contraste suficiente
- `contentDescription` para ícones relevantes
- elementos decorativos com `contentDescription = null` quando apropriado
- semântica coerente em botões, campos, listas e grupos
- ordem de leitura coerente
- não depender apenas de cor para comunicar estado
- suporte razoável a tamanhos de fonte maiores

### Regras de semântica
- usar semântica adicional apenas quando ela agrega clareza real
- não poluir a árvore semântica sem necessidade
- usar semântica para acessibilidade e testes quando isso ajudar de fato

---

## Performance

A implementação deve evitar custo desnecessário em recomposição, layout e desenho.

### Regras
- não criar objetos pesados em toda recomposição
- evitar cálculos repetidos no corpo do composable
- extrair estado derivado quando fizer sentido
- usar `remember` quando houver ganho real e justificável
- evitar composables gigantescos
- manter parâmetros estáveis quando viável
- usar listas lazy para coleções maiores
- não acoplar repositório ou operação pesada à UI

### Não fazer
- lógica de negócio no corpo da UI
- formatação pesada recalculada sem necessidade
- side effects improvisados fora das APIs apropriadas
- árvores profundas e opacas só para reproduzir visual que pode ser implementado de forma mais clara

---

## Previews

Telas e componentes relevantes devem ter previews quando viável.

### Previews recomendados
- estado principal
- loading
- empty
- error
- modo claro
- modo escuro, se o app suportar
- fonte aumentada quando útil para validar acessibilidade

### Regras
- usar dados fake seguros
- nunca usar segredos reais
- previews não devem depender de backend, Room, Keystore, NavController real ou ViewModel real
- o composable de preview deve preferir `Screen` e componentes puros, não `Route`

---

## Testabilidade de UI

Mesmo quando a tarefa principal for visual, a implementação deve nascer fácil de testar.

### Deve facilitar
- testes de `UiState`
- testes de `ViewModel`
- testes de Compose UI
- validação de callbacks
- validação de estados visuais importantes
- validação de semântica relevante

### Regras
- composables puros recebem dados e callbacks
- `testTag` só deve ser usado quando realmente ajudar em testes importantes
- não poluir o código de produção só para satisfazer teste frágil

---

## Regras de código

### Nomeação
- nomes explícitos
- evitar abreviações obscuras
- composables com nome que indique papel visual real
- estados com nomes sem ambiguidade

### Organização
- imports limpos
- funções privadas quando apropriado
- helpers específicos perto da tela
- componentes genéricos em local reutilizável
- remover imports e parâmetros não usados

### Legibilidade
- preferir clareza a esperteza
- evitar comentários óbvios
- comentar apenas quando a decisão não for evidente

---

## O que o agente deve fazer ao receber um frame do Figma

1. identificar a tela, o fluxo e o objetivo
2. identificar componentes, variants e tokens
3. levantar dados exibidos, ações e estados
4. identificar se o design revela requisitos de domínio, persistência ou segurança
5. verificar se já existe componente equivalente no projeto
6. definir contrato técnico da tela
7. atualizar documentação se a tela introduzir requisito novo
8. implementar UI em Compose com fidelidade máxima e estrutura limpa
9. criar previews seguros
10. revisar acessibilidade, performance, segurança visual e testabilidade

---

## O que o agente não deve fazer

- não gerar versão web do layout
- não improvisar arquitetura paralela
- não criar design fora do Figma
- não usar XML para tela nova sem pedido explícito
- não acoplar criptografia, biometria ou datasource ao composable puro
- não misturar navegação com layout sem necessidade
- não expor dados sensíveis em previews, logs ou mocks de produção
- não ignorar estados de erro e loading quando a tela exigir
- não duplicar componente existente sem necessidade real
- não hardcodar strings finais de UI
- não alterar o design “porque parece melhor” sem respaldo no Figma ou no usuário

---

## Definition of Done para uma tela

Uma tela só pode ser considerada pronta quando:
- está fiel ao Figma
- usa Compose de forma idiomática
- respeita `AGENTS.md`, `docs/architecture.md`, `docs/security.md` e `docs/testing.md`
- separa UI, estado, navegação e negócio corretamente
- usa recursos Android para textos finais da UI
- tem estados previsíveis
- tem acessibilidade mínima adequada
- tem segurança visual compatível com o domínio
- tem performance razoável para o escopo
- tem previews úteis e seguros quando viável
- está pronta para testes relevantes
- atualizou documentação quando o design introduziu requisito estrutural novo

---

## Referências oficiais úteis

As referências abaixo devem ser consultadas quando a tarefa tocar nesses temas:

- Compose overview: https://developer.android.com/develop/ui/compose/documentation
- Compose UI architecture / UDF: https://developer.android.com/develop/ui/compose/architecture
- State in Compose: https://developer.android.com/develop/ui/compose/state
- State hoisting: https://developer.android.com/develop/ui/compose/state-hoisting
- Resources in Compose (`stringResource`, etc.): https://developer.android.com/develop/ui/compose/resources
- Display text and string resources: https://developer.android.com/develop/ui/compose/text/display-text
- Material 3 in Compose: https://developer.android.com/develop/ui/compose/designsystems/material3
- Material components in Compose: https://developer.android.com/develop/ui/compose/components
- Navigation with Compose: https://developer.android.com/develop/ui/compose/navigation
- Lists and grids in Compose: https://developer.android.com/develop/ui/compose/lists
- Accessibility in Compose: https://developer.android.com/develop/ui/compose/accessibility
- Accessibility API defaults: https://developer.android.com/develop/ui/compose/accessibility/api-defaults
- Semantics in Compose: https://developer.android.com/develop/ui/compose/accessibility/semantics
- Merging and clearing semantics: https://developer.android.com/develop/ui/compose/accessibility/merging-clearing
- Understand gestures in Compose: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures
- Compose performance overview: https://developer.android.com/develop/ui/compose/performance
- Compose performance best practices: https://developer.android.com/develop/ui/compose/performance/bestpractices
- Compose stability: https://developer.android.com/develop/ui/compose/performance/stability
- Preview tooling: https://developer.android.com/develop/ui/compose/tooling/previews
- Compose testing: https://developer.android.com/develop/ui/compose/testing
- Compose testing APIs: https://developer.android.com/develop/ui/compose/testing/apis

---

## Regra final

**O Figma define a aparência e revela parte dos requisitos.**  
**A arquitetura do projeto define a estrutura.**  
**A segurança do domínio define os limites.**  
**As práticas oficiais de Compose definem a forma correta de implementar.**

Quando houver dúvida, o agente deve escolher a solução que seja, ao mesmo tempo:
- mais fiel ao design
- mais segura
- mais idiomática para Android
- mais simples de manter
- mais consistente com o projeto existente
