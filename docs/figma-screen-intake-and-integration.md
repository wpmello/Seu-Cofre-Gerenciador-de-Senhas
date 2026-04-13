# Figma Screen Intake and Integration

## Objetivo

Este documento define o fluxo obrigatório para transformar **um link de tela/frame do Figma** em trabalho de desenvolvimento real no projeto.

Ele existe para evitar que uma tela nova seja tratada como um pedido isolado de UI. Sempre que uma nova tela for enviada, o agente deve:

- extrair requisitos visuais e funcionais da tela
- identificar relação com telas, fluxos e entidades já existentes
- integrar a nova tela à arquitetura atual
- atualizar estado, navegação, domínio, persistência e testes quando necessário
- documentar mudanças relevantes

O objetivo é que o desenvolvimento a partir do Figma seja **incremental, integrado, testável e consistente**.

---

## Quando este fluxo deve ser usado

Este fluxo deve ser seguido quando o usuário:

- enviar um link de frame/tela do Figma
- pedir para implementar uma nova tela com base em design existente
- pedir para continuar um fluxo já iniciado a partir de outra tela do Figma
- pedir para integrar uma nova tela a telas já criadas
- pedir para atualizar uma tela já implementada com nova versão do design

---

## Regra principal

**Toda nova tela recebida do Figma deve ser tratada como parte de um sistema já existente, nunca como uma ilha.**

Ao receber uma nova tela, o agente deve avaliar não apenas:

- como construir a UI

mas também:

- onde essa tela entra no fluxo do app
- com quais telas ela se relaciona
- quais dados ela exige
- quais estados ela introduz
- quais ações ela aciona
- quais regras de negócio ela sugere
- quais testes precisam nascer ou ser ajustados

---

## Fontes de verdade a consultar

Ao receber uma tela do Figma, o agente deve consultar, nesta ordem:

1. frame/seleção exata enviada pelo usuário
2. componentes, variants, variables e tokens do arquivo Figma
3. `docs/ui-source-of-truth-figma-compose.md`
4. `AGENTS.md`
5. `docs/architecture.md`
6. `docs/testing.md`
7. `docs/security.md`
8. código já existente no projeto
9. documentação oficial Android relevante para Compose, Navigation, recursos, acessibilidade e testes

---

## Resultado esperado do intake

Depois de analisar uma tela, o agente deve ser capaz de responder internamente a estas perguntas:

- Qual é o objetivo da tela?
- Esta tela é nova ou evolução de uma já existente?
- Em qual fluxo do app ela entra?
- De onde o usuário chega nela?
- Para onde o usuário pode sair dela?
- Que dados precisam existir para a tela funcionar?
- Esses dados já existem no domínio?
- Esses dados já existem na persistência?
- Esta tela cria, lê, atualiza ou remove dados?
- Há requisitos implícitos de segurança?
- Há novos estados de UI?
- Há novos eventos ou efeitos?
- Há impacto em navegação?
- Há impacto em testes?
- Há impacto em documentação?

---

## Workflow obrigatório ao receber um link de tela do Figma

### Etapa 1 — Identificar a tela

Extrair do Figma, sempre que possível:

- nome da tela/frame
- objetivo aparente
- contexto dentro do fluxo
- componentes reutilizados
- variantes visuais
- estado principal mostrado
- estados alternativos visíveis
- ações aparentes do usuário
- textos exibidos
- campos e atributos mostrados
- elementos sensíveis

### Etapa 2 — Classificar a natureza da tela

Classificar a tela em uma ou mais categorias:

- entrada do app
- autenticação
- listagem
- detalhe
- criação
- edição
- confirmação
- exclusão
- configuração
- onboarding
- modal/dialog/sheet
- estado vazio
- erro
- loading
- sucesso

### Etapa 3 — Relacionar com o que já existe

O agente deve verificar:

- se já existe tela equivalente
- se já existe fluxo relacionado
- se já existe componente reutilizável
- se já existe entidade de domínio compatível
- se já existe rota de navegação relacionada
- se já existe caso de uso aproveitável
- se já existem testes que devem ser estendidos

### Etapa 4 — Extrair requisitos visíveis

A partir da UI, listar:

- dados exibidos
- ações do usuário
- estados da tela
- validações aparentes
- feedbacks visuais
- requisitos de navegação
- requisitos de acessibilidade
- requisitos de segurança visual

### Etapa 5 — Inferir impacto técnico

O agente deve determinar se a tela exige:

- nova rota de navegação
- novo `UiState`
- novos `UiEvents` ou `UiActions`
- novo `UiEffect` quando necessário
- novo `ViewModel`
- novo `UseCase`
- novo contrato de repositório
- novo campo em modelo de domínio
- novo campo em `Entity`
- novo campo em `Dto`
- nova migration do Room
- novos testes unitários
- novos testes de UI
- atualização de documentação

### Etapa 6 — Integrar ao fluxo existente

Se a tela se relaciona com outras já existentes, o agente deve integrar automaticamente:

- navegação de entrada e saída
- callbacks entre telas e fluxos
- passagem de argumentos
- atualização do estado anterior quando necessário
- refresh de listas após criação/edição/exclusão
- sincronização entre detalhe, edição e listagem
- mensagens de sucesso/erro coerentes com o fluxo já implementado

### Etapa 7 — Atualizar documentação quando necessário

O agente deve atualizar documentação se a tela introduzir:

- novo requisito funcional estável
- novo dado de domínio
- novo comportamento importante
- nova dependência estrutural
- novo fluxo relevante
- nova decisão arquitetural
- nova regra de segurança

---

## Como interpretar a UI como requisito de produto

### Regra geral

Quando a UI mostra um dado de forma estável e com significado funcional, o agente deve tratá-lo como **candidato a requisito formal do projeto**.

Exemplos:

- `createdAt`
- `updatedAt`
- categoria da credencial
- favorito
- nível de força da senha
- histórico de alteração
- indicador de comprometimento

Esses itens não devem ficar apenas “na tela”. Eles devem ser avaliados como possíveis partes de:

- modelo de domínio
- persistência local
- contrato de exibição
- testes

### O que não deve virar domínio automaticamente

Nem tudo que aparece na UI vira dado persistido.

Exemplos que geralmente **não** são domínio:

- cor do componente
- ícone decorativo
- estado local de expansão visual
- senha mascarada/desmascarada apenas como estado local de UI
- animação de entrada

### Regra prática

Promover para domínio/persistência apenas o que tiver:

- significado funcional
- impacto em comportamento
- necessidade de reaproveitamento entre telas
- necessidade de persistência
- relevância para teste ou regra de negócio

---

## Relação entre telas

Ao receber uma nova tela, o agente deve tentar encaixá-la em um destes padrões:

### Fluxo de listagem → detalhe
Exemplo:
- lista de senhas
- detalhe da senha

### Fluxo de detalhe → edição
Exemplo:
- detalhe da senha
- editar senha

### Fluxo de criação → confirmação → retorno
Exemplo:
- nova senha
- confirmação de criação
- retorno para lista/detalhe

### Fluxo de autenticação → desbloqueio de conteúdo sensível
Exemplo:
- biometria
- tela desbloqueada

### Fluxo de configuração → alteração persistida
Exemplo:
- settings
- idioma/tema/preferências

Se a nova tela pertencer a um fluxo já existente, o agente deve:

- reutilizar a arquitetura do fluxo
- evitar duplicar lógica
- manter consistência visual e comportamental
- garantir que os testes do fluxo sejam atualizados

---

## Contrato mínimo que o agente deve produzir por tela

Cada tela deve ser convertida mentalmente ou documentalmente em um contrato com estes itens:

### 1. Objetivo da tela
Exemplo:
- permitir que o usuário visualize os detalhes de uma credencial salva

### 2. Dados exibidos
Exemplo:
- título
- usuário/login
- senha mascarada
- website
- notas
- `createdAt`
- `updatedAt`

### 3. Ações disponíveis
Exemplo:
- voltar
- revelar senha
- copiar senha
- editar
- excluir

### 4. Estados possíveis
Exemplo:
- loading
- content
- error
- locked
- unlocked

### 5. Dependências de domínio
Exemplo:
- `PasswordEntry`
- `GetPasswordDetailsUseCase`
- `DeletePasswordUseCase`

### 6. Impacto em persistência
Exemplo:
- `PasswordEntryEntity` precisa conter `createdAt` e `updatedAt`

### 7. Impacto em testes
Exemplo:
- validar exibição de datas
- validar bloqueio de segredo por padrão
- validar autenticação antes de revelar senha

---

## Regras de integração automática

Quando novas telas forem enviadas, o agente deve integrar automaticamente o que for necessário, sem depender de um prompt detalhando cada relação óbvia.

### Deve integrar automaticamente

- rotas entre telas relacionadas
- `UiState` complementar
- ações/callbacks coerentes
- atualização de lista após CRUD
- reuso de componentes compartilhados
- consistência de copy e recursos de string
- internacionalização via `stringResource(...)`
- estados de loading, error, empty e success quando aplicável
- testes impactados pelo novo fluxo
- documentação impactada

### Não deve assumir automaticamente

- regra de negócio não sugerida pela UI nem pelo contexto do projeto
- comportamento de backend não definido
- criptografia específica sem documentação/arquitetura correspondente
- decisão de produto controversa não evidenciada na tela

---

## Regras específicas para o projeto Cofre de Senhas

### Segurança visual
Ao receber tela nova, o agente deve sempre avaliar:

- existe dado sensível visível?
- o dado está mascarado por padrão quando necessário?
- há ação explícita para revelar/copiar?
- a tela pede confirmação para ação destrutiva?
- a tela sugere necessidade de biometria?
- previews e dados fake estão seguros?

### Persistência
Se a tela sugere dado que precisa existir ao longo do tempo, o agente deve considerar impacto em:

- modelo de domínio
- `Entity`
- DAO
- repositório
- caso de uso
- migration

### Testes
Toda nova tela deve ser analisada para derivar:

- cenários BDD
- testes unitários do comportamento relacionado
- testes de UI Compose quando relevantes
- testes de integração do fluxo

---

## Atualizações obrigatórias por tipo de tela

### Tela nova de listagem
Verificar:
- rota
- estado de loading/empty/error/content
- item de lista reutilizável
- comportamento de refresh
- navegação para detalhe
- testes de listagem e item

### Tela nova de detalhe
Verificar:
- origem do item
- carregamento por id/argumento
- proteção de dados sensíveis
- ações disponíveis
- relação com edição e exclusão
- testes de detalhe

### Tela nova de criação/edição
Verificar:
- campos obrigatórios
- validações
- estado de submissão
- persistência
- atualização de `createdAt`/`updatedAt` quando aplicável
- impacto em lista e detalhe
- testes de formulário e persistência

### Tela nova de configurações
Verificar:
- persistência em DataStore quando apropriado
- reflexo imediato no app quando aplicável
- recursos localizados
- testes de preferência e estado

### Tela nova de segurança/autenticação
Verificar:
- biometria
- estados bloqueado/autenticando/autenticado/falha
- mensagens seguras
- ausência de exposição indevida de segredo
- testes de sucesso/falha

---

## Quando atualizar documentação

O agente deve atualizar documentação quando a nova tela revelar ou introduzir:

- nova entidade de domínio
- novo campo persistido
- novo fluxo de navegação
- novo tipo de estado de tela recorrente
- nova regra de segurança
- novo padrão de componente reutilizável importante
- novo requisito funcional estável

### Documentos que podem precisar de atualização

- `AGENTS.md`
- `docs/architecture.md`
- `docs/testing.md`
- `docs/security.md`
- `docs/ui-source-of-truth-figma-compose.md`
- `docs/adr/`

---

## O que o agente deve evitar

- tratar a tela como pedido puramente visual
- implementar UI sem integrar com o fluxo existente
- ignorar impacto em domínio e persistência
- criar componente duplicado quando já existe equivalente
- ignorar estado de erro/loading por não estar desenhado explicitamente
- assumir que toda informação visual é dado persistido
- deixar testes para depois
- esquecer atualização de documentação quando o contrato do produto mudou

---

## Prompt implícito que deve ser seguido ao receber nova tela

Ao receber um link de tela do Figma, o agente deve operar como se o pedido fosse:

> Analise esta tela do Figma como parte do app já existente. Extraia requisitos visuais e funcionais, identifique relação com telas e fluxos já implementados, atualize a arquitetura necessária, integre navegação, estado, domínio, persistência e testes, e se necessário documente mudanças relevantes sem inventar regras além do que a tela e o contexto do projeto suportam.

---

## Definition of Done para intake + integração de tela

Uma tela recebida do Figma só pode ser considerada corretamente incorporada quando:

- foi analisada como parte de um fluxo e não como elemento isolado
- seus dados visíveis foram avaliados como requisitos potenciais
- sua relação com telas existentes foi identificada
- navegação e callbacks necessários foram integrados
- impactos em domínio, persistência e testes foram tratados
- estados relevantes da tela foram contemplados
- a UI foi implementada com fidelidade ao Figma
- a segurança visual foi preservada
- a documentação relevante foi atualizada quando necessário

---

## Regra final

**Receber uma nova tela do Figma não significa apenas “criar mais uma screen”.**

Significa:

- analisar contrato visual
- extrair requisito funcional
- identificar impacto técnico
- integrar ao app existente
- proteger consistência arquitetural
- atualizar testes
- evoluir a documentação do projeto

