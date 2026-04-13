# Design System Operational Source of Truth

## Objetivo

Este documento transforma o design system conceitual do Stitch em um **guia operacional de implementação para Android nativo com Jetpack Compose**.

Ele existe para orientar o agente quando:
- não houver tela pronta no Figma;
- o Figma estiver incompleto ou ambíguo;
- uma tela nova precisar ser criada mantendo coerência com a identidade visual do app;
- for necessário derivar componentes, tokens, estados visuais e padrões de interação sem inventar uma linguagem visual paralela.

Este documento **não substitui** o Figma quando uma tela aprovada existir. Ele atua como:
- fallback visual;
- complemento de tokens e princípios;
- referência para criação de novas telas sem frame aprovado;
- ponte entre design conceitual e implementação Android.

---

## Fontes de verdade e ordem de prioridade

Quando houver conflito, seguir esta ordem:

1. frame/seleção exata do Figma usado na tarefa;
2. componentes, variants, variables e tokens do mesmo arquivo Figma;
3. `docs/ui-source-of-truth-figma-compose.md`;
4. este documento (`design-system-operational-source-of-truth.md`);
5. `AGENTS.md`, `docs/architecture.md`, `docs/security.md` e `docs/testing.md`;
6. padrões já existentes no código do projeto;
7. boas práticas idiomáticas oficiais do Android/Jetpack Compose.

### Regra prática
- **Se existe Figma aprovado, o Figma manda.**
- **Se não existe Figma aprovado, este documento vira a principal referência visual e composicional.**
- **Se existe Figma, mas faltam detalhes de estado, tokens ou componentes, usar este documento para preencher lacunas sem quebrar a direção visual.**

---

## Relação com o `design-system-stitch-origin.md`

O arquivo `design-system-stitch-origin.md` é a **fonte bruta de origem** do design system criado no Stitch. Ele contém a direção criativa, linguagem visual e princípios conceituais, como:
- Creative North Star “The Obsidian Vault”;
- regra de “no-line”;
- glassmorphism controlado;
- tipografia editorial;
- tonal layering;
- componentes-base e seus estilos.

Este documento é a **versão operacional derivada** dessa fonte.

### Decisão de uso
- **Manter os dois arquivos no repositório faz mais sentido.**
- `design-system-stitch-origin.md` deve ser preservado como fonte original do Stitch e referência de intenção visual. O Stitch define uma linguagem premium, com assimetria intencional, camadas tonais profundas e glassmorphism luminoso.
- `design-system-operational-source-of-truth.md` deve ser o arquivo que o Codex consulta no dia a dia para implementar UI, porque ele traduz essa intenção em regras de Android + Compose.

### Regra de consulta para o agente
- consultar primeiro o arquivo operacional;
- recorrer ao `design-system-stitch-origin.md` apenas quando precisar validar a intenção criativa original ou destrinchar uma regra de estilo ainda não operacionalizada.

---

## Identidade visual oficial do app

A identidade visual oficial deste projeto deve seguir estes princípios, derivados do Stitch:

### 1. North Star visual
A linguagem do app deve passar a sensação de:
- cofre premium;
- ambiente arquitetônico e editorial;
- interface escura, sofisticada e precisa;
- segurança percebida como refinamento, não como frieza utilitária.

### 2. Regra de composição
O sistema rejeita aparência “template Material puro” e deve privilegiar:
- assimetria intencional;
- profundidade por camadas tonais;
- vidro luminoso controlado;
- contraste forte de tipografia;
- respiro grande entre seções;
- hierarquia construída com massa visual, e não com linhas divisórias.

### 3. Regra de sobriedade
Mesmo sendo sofisticada, a interface não deve parecer experimental a ponto de comprometer:
- legibilidade;
- previsibilidade;
- acessibilidade;
- segurança visual;
- manutenção em Compose.

---

## Stack e implementação obrigatórias

Toda implementação guiada por este documento deve assumir:
- Kotlin;
- Android nativo;
- Jetpack Compose;
- Material 3 como base técnica;
- MVVM + Clean Architecture;
- Hilt;
- Navigation Compose quando houver fluxo entre destinos;
- strings via recursos Android e `stringResource(...)`;
- estado explícito e previsível via `UiState` e fluxo unidirecional.

Isso é coerente com a documentação oficial do Android sobre Compose, recursos, arquitetura de UI, Navigation Compose e acessibilidade.

Referências oficiais:
- https://developer.android.com/develop/ui/compose/documentation
- https://developer.android.com/develop/ui/compose/resources
- https://developer.android.com/develop/ui/compose/architecture
- https://developer.android.com/develop/ui/compose/navigation
- https://developer.android.com/develop/ui/compose/accessibility
- https://developer.android.com/topic/architecture

### Não permitido
- criar tela web, HTML, Tailwind ou React;
- gerar XML para telas novas sem instrução explícita;
- introduzir biblioteca visual nova sem justificativa arquitetural/documentada;
- inventar design fora deste documento quando não houver Figma;
- contrariar o `ui-source-of-truth-figma-compose` existente, que já exige Compose idiomático, reuso, segurança visual e separação entre UI, estado e navegação.

---

## Princípios operacionais de design

## 1. No-Line Rule
É proibido depender de divisórias de 1px para organizar seções, cards ou listas.

Separação visual deve acontecer por:
- mudança de superfície;
- gradiente luminoso em áreas de destaque;
- espaçamento generoso;
- agrupamento por massa visual;
- recuo visual e hierarquia tipográfica.

### Implementação prática em Compose
Preferir:
- `surface` como fundo-base da tela;
- `surfaceContainerLow` para agrupamentos secundários;
- `surfaceContainerHighest` para elementos flutuantes;
- `Spacer`, `Arrangement.spacedBy(...)` e paddings consistentes em vez de linhas divisórias.

### Exceção
Se acessibilidade ou clareza exigir reforço de borda, usar apenas “ghost border” sutil e sem aparência de divider tradicional.

---

## 2. Tonal Layering Above Shadows
Profundidade deve vir principalmente de **camadas tonais**, não de sombras pesadas.

### Regra
O agente deve preferir:
- contraste entre superfícies;
- níveis visuais por elevação tonal;
- sobreposição controlada;
- brilho ambiente leve apenas em estados flutuantes.

### Não fazer
- usar sombras padrão pesadas do Android como linguagem principal;
- criar visual “muddy” escuro demais;
- depender de shadow para separar todos os blocos.

---

## 3. Glassmorphism Controlado
Glassmorphism é permitido apenas onde ele reforça a sensação premium sem prejudicar leitura.

### Locais preferenciais
- FAB;
- navegação top-level;
- busca flutuante;
- elementos destacados que pairam sobre um hero card;
- estados flutuantes específicos.

### Regra
Usar glass como **acentuação**, não como textura aplicada em tudo.

### Não fazer
- transformar todas as superfícies em vidro;
- reduzir contraste do texto;
- usar blur exagerado que atrapalhe performance ou legibilidade.

---

## 4. Editorial Typography
A tipografia deve parecer editorial e premium, sem sacrificar legibilidade técnica.

### Direção derivada do Stitch
- títulos e destaques devem ter peso visual forte;
- dados e conteúdo real devem priorizar leitura clara;
- contraste de escala entre headline e label pode orientar navegação;
- evitar blocos homogêneos de texto com peso visual excessivamente uniforme.

### Regra operacional
Mesmo que a família tipográfica final mude na implementação Android, o comportamento deve ser preservado:
- títulos com presença;
- corpo legível;
- labels discretas;
- hierarquia forte entre dado principal e metadado;
- nada de puro branco para texto, priorizando tons do tema escuro.

---

## Paleta e tokens base

## Base escura obrigatória
O app deve ser predominantemente dark-first.

### Base visual derivada do Stitch
- `surface`: `#070e1b`
- `surface-container-low`: `#0c1322`
- `surface-container-highest`: `#1c2639`
- `surface-bright`: `#222c41`
- `primary`: `#89acff`
- `secondary`: `#ea73fb`
- `on_surface`: `#e2e8fb`
- `outline-variant`: `#414857`
- `error`: `#ff716c`
- `tertiary_fixed`: `#3fff8b`
- `primary_dim`: `#0f6df3`

### Regra
Esses valores devem ser tratados como **ponto de partida do tema do app**, não como hardcodes espalhados.

### Implementação
Traduzir esses valores para:
- `ColorScheme` do Material 3;
- tokens de tema do projeto;
- wrappers/aliases reutilizáveis quando necessário.

### Não permitido
- repetir hex em vários arquivos de UI;
- criar subpaletas paralelas sem documentação;
- misturar paletas conflitantes com a identidade do app.

---

## Gradiente assinatura
O gradiente `primary -> secondary` em 135° deve ser tratado como elemento de assinatura visual.

### Usar para
- CTA primário;
- hero cards;
- áreas de destaque do dashboard;
- pontos de entrada com alto peso visual.

### Não usar para
- tudo indiscriminadamente;
- planos de fundo inteiros da aplicação;
- áreas com muito texto pequeno e denso.

### Regra de contraste
Texto e ícones em cima de gradiente devem manter contraste suficiente e passar validação visual.

---

## Espaçamento, forma e composição

## Espaçamento
O sistema deve respirar.

### Regra
Usar espaços generosos entre seções principais. O Stitch destaca `spacing-12`, `spacing-16` e maiores como parte da linguagem visual.

### Aplicação prática
- seções principais com respiro grande;
- itens internos com espaçamentos menores e consistentes;
- evitar telas densas “apertadas”;
- usar o espaço vazio como elemento de hierarquia.

## Cantos
Nada de cantos agressivamente retos.

### Regra
Usar apenas escala de roundedness média a alta (`md` a `xl`). O Stitch proíbe cantos cortantes na linguagem do sistema.

### Aplicação prática
- botões principais com roundedness alta;
- cards com roundedness `xl` quando forem elementos de destaque;
- inputs com roundedness média;
- modais e folhas seguindo o mesmo idioma.

---

## Regras operacionais por componente

## Botões
### Primário
- fundo em gradiente assinatura;
- sem borda;
- alta roundedness;
- usado para a ação principal da tela.

### Secundário
- aparência glass/tonal;
- superfície translúcida controlada;
- ghost border opcional e sutil;
- usado para ação relevante secundária.

### Terciário
- texto puro;
- sem peso visual maior que a ação principal;
- cor derivada do primário/dim quando fizer sentido.

## Cards e vault items
- sem divider entre itens;
- agrupamento por card/superfície;
- cards com `surfaceContainerLow` ou similar;
- destaque assimétrico permitido em hero cards;
- dados principais devem dominar metadados.

## Inputs
- fundo tonal, não branco;
- sem borda por padrão;
- borda/foco apenas no estado focado;
- foco com ghost border e leve glow quando apropriado;
- labels claras e erros próximos ao campo.

## Indicadores de segurança
- segurança fraca: linguagem do `error` com aura leve;
- segurança forte: linguagem luminosa verde/tertiary;
- indicadores devem parecer discretos, táteis e confiáveis, nunca infantis ou gritantes.

## FAB e navegação top-level
- podem usar glassmorphism controlado;
- não devem parecer soltos do restante da tela;
- precisam manter contraste, alvo de toque e previsibilidade.

---

## Como usar este documento quando não houver Figma

Ao receber um pedido de tela sem frame do Figma, o agente deve:

1. identificar o objetivo funcional da tela;
2. verificar se existe tela semelhante já implementada;
3. verificar se existe fluxo próximo em documentação ou no código;
4. usar este design system como referência visual oficial;
5. compor a tela em Compose respeitando:
- tonal layering;
- no-line rule;
- dark-first;
- gradiente assinatura;
- tipografia editorial;
- acessibilidade e performance;
6. derivar estados da tela (`loading`, `empty`, `content`, `error`, estados sensíveis);
7. organizar a implementação em `Route`, `Screen`, `UiState`, `UiEvent`/`UiAction`, `ViewModel` e componentes;
8. atualizar testes e docs quando a tela introduzir contrato novo.

### Regra crítica
Quando não houver Figma, o agente **não pode inventar uma linguagem visual aleatória**. Ele deve usar este documento como design baseline e o `ui-source-of-truth-figma-compose` como guia estrutural de Compose. O arquivo de UI já determina Compose idiomático, reuso, segurança visual, organização por tela e respeito à arquitetura.

---

## Como usar este documento quando houver Figma parcial ou ambíguo

Se a tela do Figma:
- não mostrar todos os estados;
- não definir componente derivado;
- não detalhar o comportamento visual de erro/loading;
- não mostrar tokens suficientes;
- não tiver tela irmã para comparação;

então o agente deve completar os vazios usando este documento.

### Regra
Completar sem “melhorar o design”, apenas preservando a linguagem oficial.

---

## Relação com domínio, navegação e testes

Este documento é visual, mas ele afeta decisões de implementação.

### Domínio
Quando uma tela sem Figma for criada com base neste documento, o agente deve identificar:
- quais dados são estruturais à experiência;
- quais campos precisam existir em domínio e persistência;
- quais estados são de UI e quais pertencem ao fluxo do app.

### Navegação
A UI deve continuar expondo callbacks e estados, sem colocar decisão de rota no composable puro, em linha com o `ui-source-of-truth-figma-compose`.

### Testes
Toda tela derivada deste documento deve gerar cenários de teste coerentes com:
- comportamento observável da UI;
- estados relevantes;
- segurança visual;
- não regressão.

O projeto já determina TDD + BDD e cenários de segurança com sucesso e falha.

---

## Internacionalização e texto

Mesmo quando a tela for criada sem Figma, o agente deve:
- usar `stringResource(...)`;
- evitar texto hardcoded em composables;
- preparar recursos para `pt-BR`, `en` e `es`;
- preservar consistência textual entre telas.

Referência oficial:
- https://developer.android.com/develop/ui/compose/resources
- https://developer.android.com/guide/topics/resources/string-resource

---

## Acessibilidade obrigatória

A identidade premium nunca deve reduzir acessibilidade.

### Regras mínimas
- manter contraste adequado em dark theme;
- garantir tamanho de toque apropriado;
- usar `contentDescription` apenas quando fizer sentido;
- não depender só de cor para indicar risco, força ou erro;
- manter hierarquia semântica coerente;
- não sacrificar legibilidade em nome do efeito glass.

Referências oficiais:
- https://developer.android.com/develop/ui/compose/accessibility
- https://developer.android.com/develop/ui/compose/accessibility/semantics

---

## Performance obrigatória

O design system não autoriza UI cara ou difícil de manter.

### Regras
- evitar composição excessivamente profunda sem necessidade;
- evitar blur/glow em excesso;
- preferir soluções estáveis e reutilizáveis;
- usar `LazyColumn`/listas lazy para coleções;
- evitar recalcular gradientes, estilos ou objetos pesados em recomposição.

Referências oficiais:
- https://developer.android.com/develop/ui/compose/performance
- https://developer.android.com/develop/ui/compose/lists

---

## Previews e segurança visual

Previews devem refletir a linguagem do design system, mas com dados fake seguros.

### Regras
- nunca usar senhas reais;
- nunca usar segredos verdadeiros;
- mascarar conteúdo sensível quando aplicável;
- criar preview do estado principal e pelo menos um estado alternativo relevante;
- não depender de backend ou ViewModel real.

---

## Quando documentar

Atualizar este documento quando houver:
- mudança real na linguagem visual oficial;
- novo padrão visual repetível sem Figma;
- decisão de componente base que passa a ser regra do projeto;
- alteração de tokens, gradiente assinatura, linguagem de cards, inputs ou navegação top-level.

Se a mudança for arquitetural ou de longo prazo, registrar também ADR.

---

## Definition of Done para UI sem Figma

Uma tela criada sem frame aprovado só pode ser considerada pronta quando:
- respeita este documento e o `ui-source-of-truth-figma-compose`;
- parece parte natural do mesmo produto;
- segue Compose idiomático;
- respeita arquitetura, navegação e separação de estado;
- usa recursos Android para texto;
- mantém acessibilidade mínima;
- mantém segurança visual do domínio;
- tem previews úteis e seguras;
- possui cenários de teste compatíveis com o comportamento;
- não introduz design paralelo ou inconsistente.

---

## Regra final

Quando não houver Figma, este documento define **como o produto deve parecer**.

Quando houver Figma, este documento define **como preservar coerência fora do que o Figma explicitou**.

O objetivo não é dar liberdade criativa ao agente. O objetivo é impedir lacunas visuais, manter consistência de produto e permitir evolução segura da UI em um app premium e sensível.
