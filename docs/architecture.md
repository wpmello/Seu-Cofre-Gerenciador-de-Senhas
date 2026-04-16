# Arquitetura

## Objetivo
Este projeto adota **MVVM + Clean Architecture**, com **Jetpack Compose**, **Navigation Compose**, **Dagger Hilt**, **Room**, **DataStore**, **Coroutines** e **Flow/StateFlow**.

A arquitetura foi escolhida para atender um app de produção com foco em:
- segurança
- previsibilidade de comportamento
- testabilidade
- separação de responsabilidades
- evolução incremental sem perda de clareza
- baixo acoplamento entre UI, regra de negócio e infraestrutura

## Princípios arquiteturais
As decisões deste projeto devem respeitar estes princípios:

- **Separation of Concerns**: cada camada deve resolver um tipo específico de problema.
- **Single Source of Truth**: cada dado deve ter uma origem responsável clara.
- **Unidirectional Data Flow (UDF)**: estado desce para a UI; eventos sobem para o `ViewModel`.
- **Explícito em vez de implícito**: mapeamentos, contratos e dependências devem ser claros.
- **Segurança por desenho**: dados sensíveis não podem depender de conveniência arquitetural.
- **Evolução segura**: mudanças devem ser pequenas, testáveis e compatíveis com a arquitetura vigente.

## Visão arquitetural
A recomendação atual do Android é que todo app tenha ao menos **UI layer** e **data layer**, com **domain layer** como camada adicional opcional quando ela ajuda a simplificar e reutilizar regras de negócio. Neste projeto, a **domain layer será adotada explicitamente** porque o domínio do app envolve segurança, persistência local, criptografia, autenticação biométrica, regras de validação e fluxos sensíveis que não devem ficar espalhados entre UI e infraestrutura.

Camadas do projeto:
- `presentation/`
- `domain/`
- `data/`

Regra de dependência:
- `presentation` pode depender de `domain`
- `domain` pode depender apenas de contratos e modelos próprios
- `data` implementa contratos consumidos por `domain`
- infraestrutura nunca deve vazar para `presentation`

## Organização recomendada
A organização principal deve ser **por feature**, preservando as camadas dentro de cada feature. Isso tende a manter coesão alta e reduz impacto de mudanças em apps que crescem com o tempo.

Exemplo:

```text
feature/
  vault/
    presentation/
    domain/
    data/
  settings/
    presentation/
    domain/
    data/
core/
  ui/
  designsystem/
  navigation/
  database/
  datastore/
  crypto/
  testing/
```

Quando uma feature ainda for muito pequena, é aceitável começar com estrutura mais simples, mas sem quebrar a separação entre camadas.

## UI / Presentation Layer
A UI layer é responsável por exibir estado, receber interação do usuário e delegar ações. No Android moderno, a UI deve ser guiada por estado, e o `ViewModel` deve atuar como state holder para lógica de negócio relacionada à tela. O Android também recomenda UDF para a UI, especialmente em Compose, onde composables recebem estado e expõem eventos.

### Responsabilidades
- telas em Jetpack Compose
- componentes de UI
- `UiState`
- `ViewModel`
- navegação
- eventos de UI
- transformação de estado de domínio em estado renderizável

### Regras obrigatórias
- A UI **não acessa** Room, Retrofit, DataStore, Keystore ou qualquer detalhe de infraestrutura diretamente.
- Composables devem ser preferencialmente **stateless** sempre que possível.
- Estado de tela deve ser exposto pelo `ViewModel` como `StateFlow`.
- Eventos do usuário devem subir por callbacks e ser tratados pelo `ViewModel`.
- `ViewModel` não deve conhecer detalhes concretos de armazenamento ou rede.
- Lógica de negócio não deve morar em composable puro.
- Navegação deve ser feita com **Navigation Compose**.
- Quando o requisito disser “tela”, interpretar como **tela inteira** por padrão.
- `Dialog`, `bottom sheet` ou modal só devem ser usados quando o requisito pedir explicitamente ou quando o fluxo oficial da feature já os definir.

### Contrato recomendado por tela
Cada tela deve, sempre que fizer sentido, declarar explicitamente:
- `UiState`
- `UiEvent` ou ações recebidas da UI
- `UiEffect` apenas se houver efeito transitório inevitável, como snackbar, navegação disparada por evento ou ação one-shot

Exemplo conceitual:

```text
PasswordDetailUiState
PasswordDetailAction
PasswordDetailViewModel
```

### Estados de tela
Cada tela deve modelar estados previsíveis. No mínimo, avaliar:
- `loading`
- `content`
- `empty`
- `error`
- estados específicos de segurança, como `locked` e `unlocked`, quando aplicável

### Strings e internacionalização
A UI deve usar `stringResource(...)` e ser preparada para internacionalização desde o início, com suporte mínimo planejado para:
- `pt-BR`
- `en`
- `es`

## Domain Layer
Embora a camada de domínio seja opcional no guia geral do Android, neste projeto ela é mandatória. Ela existe para concentrar regras de negócio e impedir que decisões importantes fiquem espalhadas entre `ViewModel`, banco e serviços de infraestrutura.

### Responsabilidades
- modelos de domínio
- casos de uso
- contratos de repositório
- regras de negócio
- políticas de validação
- orquestração de fluxos de negócio

### Regras obrigatórias
- A camada de domínio não deve depender de classes Android.
- Modelos de domínio não usam sufixos técnicos como `Dto` ou `Entity`.
- Casos de uso devem existir quando houver regra de negócio, orquestração ou reutilização relevante.
- O domínio deve falar em termos do negócio, não em termos da tecnologia.
- O domínio deve ser a camada mais estável do projeto.

### Quando criar um UseCase
Criar `UseCase` quando houver pelo menos um destes sinais:
- validação de regra de negócio
- orquestração entre múltiplos repositórios ou serviços
- transformação relevante antes da UI
- regra de segurança
- necessidade de reuso entre telas

Evitar criar `UseCase` artificial quando ele apenas repassa uma chamada sem agregar clareza.

## Data Layer
Segundo a documentação atual do Android, a data layer contém os dados da aplicação e a business logic ligada à criação, armazenamento e alteração desses dados. Neste projeto, essa camada é responsável por integrar persistência local, preferências, criptografia, autenticação de leitura quando aplicável e qualquer fonte remota futura.

### Responsabilidades
- implementações concretas de `Repository`
- `data sources` locais e remotos
- entidades Room
- DTOs
- mapeadores
- gateways de criptografia
- acesso a DataStore
- integração com serviços externos

### Regras obrigatórias
- Repositórios são a fronteira entre domínio e fontes de dados.
- A origem de cada dado deve ser explícita.
- `Repository` não deve vazar DTOs ou Entities para camadas superiores.
- Conversões entre camadas devem ser explícitas.
- Room deve ser acessado de forma assíncrona; `allowMainThreadQueries()` é proibido.
- Dados persistentes do produto não devem ficar apenas em memória sem motivo técnico real.
- DataStore é o mecanismo padrão para preferências e configurações; não usar `SharedPreferences` em código novo.
- Dados sensíveis não podem ser persistidos em texto puro.

### Persistência local
Neste app, a persistência local não é detalhe secundário; ela faz parte da arquitetura central. Room deve ser introduzido desde o início das features que lidam com dados persistentes, para evitar acoplamento posterior a armazenamento em memória e reduzir retrabalho.

## Assincronismo e fluxos de dados
No Android moderno, coroutines e flows são usados em conjunto: `suspend` para operações one-shot e `Flow` para fluxos observáveis ao longo do tempo. A UI deve coletar estado de forma lifecycle-aware, e a arquitetura oficial recomenda comunicação entre camadas com coroutines e flows.

### Regras do projeto
- Usar **Kotlin Coroutines** como base de concorrência.
- Usar `suspend` para operações pontuais.
- Usar `Flow` para streams de dados observáveis.
- Usar `StateFlow` para estado de tela exposto pelo `ViewModel`.
- O projeto deve evitar APIs síncronas para leitura/escrita persistente.
- Fluxos devem ser testáveis com fakes e assertiva de emissões.

### Fluxo arquitetural padrão
```text
Compose UI -> ViewModel -> UseCase -> Repository -> Data Source -> Mapper -> Domain -> ViewModel -> UiState -> Compose UI
```

Esse fluxo pode variar em casos simples, mas a direção das dependências não deve ser invertida.

## Convenções de nomenclatura

### Modelos remotos
Classes de transporte remoto usam o sufixo `Dto`.

Exemplos:
- `PasswordDto`
- `LoginResponseDto`
- `VaultItemDto`

### Modelos locais
Classes persistidas com Room usam o sufixo `Entity`.

Exemplos:
- `PasswordEntity`
- `VaultEntryEntity`
- `UserPreferencesEntity` apenas se realmente for persistido em banco relacional

### Modelos de domínio
Modelos de domínio usam nomes simples, sem sufixos técnicos.

Exemplos:
- `PasswordEntry`
- `User`
- `VaultItem`

### Classes de UI
Seguir preferencialmente estes nomes:
- `<Feature>Screen`
- `<Feature>ViewModel`
- `<Feature>UiState`
- `<Feature>Action` ou `<Feature>Event`
- `<Verb><Entity>UseCase`

### Regra de nomenclatura importante
Não usar prefixo `Test` em classes de produção. Esse prefixo deve ficar restrito a classes de teste ou contextos em que o nome faça sentido real dentro do escopo de testes.

## Mapeamento entre modelos
Conversão explícita entre `Dto`, `Entity` e domínio. Mapeamento não é detalhe de estilo, é regra de proteção de fronteira entre camadas

### Regras
- Cada fronteira entre camadas deve ser atravessada por mapeamento explícito.
- Não expor DTO na UI.
- Não expor Entity na UI.
- Não misturar serialização, persistência e domínio em uma mesma classe.
- Mapeadores devem ficar próximos da camada que conhece a origem/destino.

Exemplos conceituais:
- `PasswordDto -> PasswordEntry`
- `PasswordEntity -> PasswordEntry`
- `PasswordEntry -> PasswordEntity`

## Repositórios
A documentação atual do Android reforça a existência de uma data layer clara, e as recomendações de arquitetura tratam repositórios como uma peça central dessa fronteira. Neste projeto, repositório não é simples proxy; ele é a API interna da camada de dados para o domínio.

### Regras
- Um `Repository` deve expor operações orientadas ao caso de uso, não ao mecanismo de armazenamento.
- A implementação concreta decide entre fonte local, remota ou combinação de fontes.
- Estratégias de cache, fallback e sincronização pertencem ao `Repository` e à data layer.
- O domínio deve depender da interface, não da implementação.

## Injeção de dependência
O projeto usa **Dagger Hilt** como solução padrão de DI. A documentação oficial do Android recomenda Hilt para reduzir boilerplate de injeção manual, e a documentação do Dagger para Android continua recomendando `@Inject` quando possível, `@Binds` para conectar interface a implementação e `@Provides` para classes que o projeto não controla.

### Regras obrigatórias
- Não fazer injeção manual por padrão.
- Classes próprias do projeto devem preferir `constructor injection`.
- Interfaces com implementação concreta devem usar `@Binds` quando possível.
- Builders e objetos externos devem usar `@Provides`.
- `ViewModel` deve usar a integração oficial do Hilt.
- Não usar padrão `Factory` próprio para resolver o que Hilt já resolve no projeto.
- O grafo de dependências deve respeitar as fronteiras entre camadas.

## Navegação
A navegação oficial do projeto deve usar **Navigation Compose**.

### Regras
- Rotas devem ser tipadas ou centralizadas de forma previsível.
- Argumentos de navegação devem ser mínimos.
- Não trafegar dados sensíveis em texto claro na rota.
- Sempre que possível, passar identificadores estáveis e buscar o dado pela camada apropriada.
- Navegação não deve carregar regra de negócio.

## Figma como insumo arquitetural

O Figma não deve ser tratado apenas como referência visual.  
Quando uma tela aprovada no Figma introduzir ou confirmar dados, estados, ações ou relações de navegação, isso deve ser avaliado como possível requisito formal do produto e da arquitetura.

### Regra
Ao receber uma nova tela, frame ou fluxo do Figma, o agente deve analisar se ele implica impacto em:

- modelo de domínio
- persistência local
- contratos de repositório
- casos de uso
- `UiState`
- navegação
- testes
- documentação

### Dados visíveis na UI
Quando um dado aparece de forma estável e funcional na interface, ele deve ser avaliado para existir também nas camadas corretas.

Exemplos:
- `createdAt`
- `updatedAt`
- categoria
- favorito
- indicador de força
- status de sincronização
- origem da credencial

### Regra de decisão
- se o dado é apenas visual ou efêmero, ele pode existir somente como estado de UI
- se o dado representa comportamento, auditoria, persistência, regra de negócio ou contrato do produto, ele deve ser refletido nas camadas apropriadas
- a presença do dado no Figma não autoriza modelagem automática sem análise arquitetural, mas obriga a avaliação explícita desse impacto

### Estados e fluxos
Quando o Figma indicar estados ou transições, o agente deve mapear isso para a estrutura arquitetural correta.

Exemplos:
- loading
- empty
- error
- locked
- unlocked
- biometric required
- success
- disabled

Esses estados devem ser refletidos em `UiState` e nos fluxos adequados do `ViewModel`, sem misturar regra de negócio dentro do composable puro.

### Navegação
Se uma nova tela do Figma se conectar a telas já existentes, o agente deve avaliar:

- origem e destino da navegação
- parâmetros necessários
- impacto em rotas existentes
- efeitos colaterais após ações como salvar, editar, excluir ou autenticar

### Testes e documentação
Sempre que o Figma introduzir ou alterar contrato funcional, o agente deve:

- revisar impacto em testes
- atualizar cenários relevantes em `docs/testing.md`
- atualizar `docs/ui-source-of-truth-figma-compose.md` quando necessário
- registrar decisão em `docs/adr/` quando houver mudança arquitetural relevante

### Exemplo prático
Se a tela de detalhe da senha mostrar `Criado em` e `Atualizado em`, o agente deve avaliar explicitamente:
- se esses campos pertencem ao modelo de domínio
- se devem existir em `Entity`
- se exigem mapeamento dedicado
- se devem aparecer em casos de uso, testes e contratos da UI

### Regra do projeto
Requisitos inferidos do Figma devem ser promovidos para documentação técnica quando impactarem:
- modelos de domínio
- entidades persistidas
- navegação
- segurança
- testes
- contratos entre camadas

## Segurança aplicada à arquitetura
Como o app é sensível, a arquitetura deve impedir atalhos inseguros.

### Regras obrigatórias
- senhas, chaves, IVs, tokens e segredos não devem aparecer em logs
- material criptográfico deve usar Android Keystore quando aplicável
- leitura de dados altamente sensíveis pode exigir autenticação biométrica conforme a política de segurança da feature
- arquitetura não deve introduzir persistência em texto puro por conveniência
- debug helpers, previews e mocks não devem expor dados reais

## O que não fazer
- Não acessar banco, DataStore ou API diretamente da UI.
- Não colocar regra de negócio dentro de composable puro.
- Não deixar `ViewModel` crescer como “classe Deus”.
- Não criar padrão paralelo se o projeto já tiver uma solução definida.
- Não misturar responsabilidades de `Dto`, `Entity` e modelo de domínio.
- Não introduzir bibliotecas de infraestrutura sem necessidade e sem registro de decisão quando relevante.
- Não quebrar a direção das dependências entre camadas.
- Não fazer gambiarra para encaixar feature fora da arquitetura.

## Quando atualizar este documento
Atualizar `docs/architecture.md` quando houver, por exemplo:
- alteração estrutural de camadas
- mudança de organização por feature ou módulo
- adoção/remoção de lib arquitetural relevante
- alteração no padrão de estado, navegação, persistência ou DI
- decisão que afete como requisitos do produto atravessam as camadas

Quando a mudança exigir registro mais histórico e comparativo, criar ou atualizar um documento em `docs/adr/`.
