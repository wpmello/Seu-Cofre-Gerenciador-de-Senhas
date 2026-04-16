# Estratégia de Testes

## Objetivo
Este documento define a estratégia oficial de testes do projeto.

Como este é um app Android de cofre de senhas com foco em produção, testes não são complemento nem etapa opcional. Testes são parte do processo de engenharia, da segurança do produto e da definição de pronto.

O projeto adota **TDD + BDD como regra de trabalho**, com prioridade para testes pequenos, determinísticos, rápidos e estáveis.

## Fontes oficiais de referência
As decisões deste documento devem seguir prioritariamente estas documentações oficiais:

- Android Developers — Test apps on Android: https://developer.android.com/training/testing
- Android Developers — Fundamentals of testing Android apps: https://developer.android.com/training/testing/fundamentals
- Android Developers — Testing strategies: https://developer.android.com/training/testing/fundamentals/strategies
- Android Developers — What to test in Android: https://developer.android.com/training/testing/fundamentals/what-to-test
- Android Developers — Build local unit tests: https://developer.android.com/training/testing/local-tests
- Android Developers — Build instrumented tests: https://developer.android.com/training/testing/instrumented-tests
- Android Developers — Test your Compose layout: https://developer.android.com/develop/ui/compose/testing
- Android Developers — Compose testing APIs: https://developer.android.com/develop/ui/compose/testing/apis
- Android Developers — Compose semantics for testing: https://developer.android.com/develop/ui/compose/testing/semantics
- Android Developers — Testing Kotlin coroutines on Android: https://developer.android.com/kotlin/coroutines/test
- Android Developers — Testing Kotlin flows on Android: https://developer.android.com/kotlin/flow/test
- Android Developers — Use test doubles in Android: https://developer.android.com/training/testing/fundamentals/test-doubles
- Android Developers — Hilt testing guide: https://developer.android.com/training/dependency-injection/hilt-testing
- Android Developers — Test and debug your database (Room): https://developer.android.com/training/data-storage/room/testing-db
- Android Developers — Migrate your Room database: https://developer.android.com/training/data-storage/room/migrating-db-versions

## Princípios obrigatórios
- **Teste primeiro.** Implementação relevante não começa antes da definição do comportamento e do teste correspondente.
- **Comportamento antes de implementação.** O projeto deve validar o que o sistema faz, não apenas como ele foi escrito.
- **Preferir testes pequenos.** A base deve priorizar testes unitários e de integração pequena, porque são mais rápidos e mais confiáveis.
- **Cobrir risco, não só linhas.** O foco é reduzir risco técnico, risco de regressão e risco de segurança.
- **Determinismo.** Testes não devem depender de timing arbitrário, rede real, banco persistente real compartilhado, clock implícito ou estado residual entre execuções.
- **Sem gambiarra de teste.** Não mascarar problemas reais com sleeps arbitrários, asserts frágeis, relaxamento indevido de regras de segurança ou bypass silencioso de arquitetura.
- **Refatoração segura.** Mudanças internas são aceitáveis somente quando o comportamento continua validado pela suíte relevante.

## Relação entre BDD e TDD
Neste projeto, a ordem correta é:

1. definir o comportamento esperado
2. escrever cenários BDD
3. transformar os cenários em testes executáveis
4. confirmar que o teste falha pelo motivo correto
5. implementar o mínimo necessário
6. refatorar sem alterar comportamento
7. executar novamente a suíte relevante

### Regra prática
Se não existe cenário claro para o comportamento novo, a implementação ainda não deve começar.

## BDD obrigatório
Antes de qualquer implementação, o comportamento deve ser descrito com cenários orientados a comportamento.

Formato preferencial:
- **Given** contexto inicial
- **When** ação executada
- **Then** resultado esperado

Os cenários devem deixar explícitos:
- estado inicial
- ação do usuário ou do sistema
- resultado funcional esperado
- resultado de erro quando aplicável
- impacto em segurança quando houver dado sensível

### Exemplo
- Given que o usuário informou uma senha válida
- When ele salva a credencial
- Then o valor deve ser protegido antes de ser persistido

## Estratégia por tamanho e tipo de teste
Seguir a estratégia oficial do Android de priorizar testes pequenos, complementados por testes médios e grandes apenas onde eles agregam fidelidade relevante.

### 1. Testes unitários locais (`src/test/`)
Devem formar a maior parte da suíte.

Cobrir prioritariamente:
- casos de uso
- regras de negócio
- validadores
- mapeadores entre `Dto`, `Entity` e domínio
- ViewModels com transformação de estado
- componentes de segurança desacoplados do framework
- lógica de formatação e interpretação de estados
- adaptadores de contrato entre camadas

Regras:
- rodar na JVM local por padrão
- não depender de framework Android quando isso puder ser evitado
- usar doubles de teste quando necessário para isolar o sujeito sob teste
- falhas devem apontar claramente qual regra de negócio foi quebrada
- para todo contrato novo relevante em `data`, `domain` ou `presentation`, cobrir pelo menos um caminho de sucesso e um caminho de falha quando o comportamento puder falhar
- quando um componente não tiver caminho triste significativo por ser mapeamento puro, estrutura passiva ou contrato sem ramificação, isso deve ser uma decisão consciente e explícita, não omissão acidental

### 2. Testes de integração pequenos ou médios
Usar quando for necessário validar integração real entre duas ou mais partes importantes.

Exemplos prioritários:
- Repository + DAO
- Repository + mapper + source local
- ViewModel + UseCase + fake repository
- criptografia + persistência local controlada
- DataStore e leitura de preferências críticas

Regra:
- só subir o nível do teste quando o risco não puder ser coberto adequadamente por teste unitário.

### 3. Testes instrumentados (`src/androidTest/`)
Devem ser usados quando o valor do teste depende do runtime Android real, framework, Compose UI real, Room em ambiente Android ou integração com Hilt no device/emulador.

Cobrir principalmente:
- fluxos reais de UI em Compose
- integração com framework Android
- comportamento de navegação crítica
- comportamento de autenticação biométrica quando desacoplamento não for suficiente para cobrir o risco
- testes de Room onde a fidelidade ao ambiente Android importa
- migrações de banco

## O que testar por camada

### Presentation
Cobrir:
- transformação de eventos em intenção de ação
- produção de `UiState`
- estados `loading`, `success`, `empty` e `error` quando aplicável
- efeitos transitórios relevantes, como mensagens, navegação e pedidos de autenticação
- comportamento visual crítico em Compose
- proteção visual de dados sensíveis

### Domain
Cobrir com prioridade máxima:
- casos de uso
- regras de negócio
- validações
- contratos de comportamento
- tratamento de erro de domínio
- garantias de segurança que não dependem da UI
- tanto o resultado esperado quanto a propagação ou tradução de falhas quando o contrato puder quebrar

### Data
Cobrir:
- mapeadores
- contratos de repositório
- integração com Room
- leitura/escrita em DataStore
- persistência segura de dados
- tratamento de erro de fontes locais e remotas
- migrações de banco quando houver mudança de schema
- propagação, tradução ou contenção de falhas das fontes quando esse comportamento fizer parte do contrato do componente

## Prioridades de teste deste projeto

### Prioridade máxima
- criptografia e descriptografia
- proteção contra persistência insegura
- nunca armazenar senha em texto puro
- autenticação biométrica quando houver lógica desacoplável
- mapeamento entre `Dto`, `Entity` e modelo de domínio
- contratos de repositório
- casos de uso centrais
- persistência local via Room
- migrações de banco
- leitura/escrita de configurações críticas em DataStore

### Prioridade alta
- ViewModels com transformação de estado
- fluxos de erro
- regras de edição, exclusão e atualização
- regras derivadas do Figma que impactam domínio e persistência, como `createdAt` e `updatedAt`
- navegação crítica
- UI sensível em Compose

### Prioridade média
- componentes puros de apoio
- formatações simples
- elementos visuais sem regra relevante de negócio

## Coroutines e Flow
Como o projeto usa **Kotlin Coroutines + Flow/StateFlow**, a suíte deve seguir as práticas oficiais para testes assíncronos.

Regras obrigatórias:
- usar `runTest` para testes de coroutines
- controlar dispatchers explicitamente em testes
- evitar `Thread.sleep()` e temporizações arbitrárias
- testar `Flow` conforme o papel do fluxo no sujeito sob teste: entrada, saída ou estado
- estados expostos por ViewModel devem ser validados de forma determinística

Cobrir especialmente:
- emissões esperadas de `Flow`
- estados iniciais e subsequentes de `StateFlow`
- cancelamento e conclusão quando relevantes
- tratamento de exceções assíncronas

## Compose UI testing
As telas em Jetpack Compose devem usar as APIs oficiais de teste do Compose.

Regras obrigatórias:
- testar comportamento observável da UI, não detalhes irrelevantes de implementação
- usar semantics para localizar e validar nós quando necessário
- garantir que elementos críticos tenham identificadores semânticos adequados quando isso melhorar robustez do teste
- testar estados de tela, ações do usuário e resultado visível
- não usar screenshot testing como substituto dos testes de comportamento

Cenários prioritários para telas:
- carregamento
- conteúdo com sucesso
- erro
- vazio
- ação principal
- navegação crítica
- proteção de dados sensíveis
- acessibilidade mínima quando relevante ao comportamento testado

### Criar testes só para telas apenas quando for muito necessário

## Hilt e doubles de teste
O projeto usa **Dagger Hilt** como padrão de injeção; os testes devem seguir a abordagem oficial de Hilt para substituição de dependências em cenários instrumentados e integração.

Regras:
- usar doubles de teste para isolar comportamento
- preferir fakes simples e explícitos quando suficientes
- usar Hilt testing quando o teste depender do grafo real de injeção
- não criar mecanismos paralelos de injeção apenas para o teste se Hilt já cobre o caso

## Room e persistência local
Regras obrigatórias:
- testar DAOs, queries relevantes e constraints importantes
- testar migrations sempre que houver alteração de schema
- exportar schema do banco para suportar testes de migração
- não usar `allowMainThreadQueries()` como atalho de produção; em testes, qualquer uso precisa ser justificado e restrito ao contexto do próprio teste
- validar que dados sensíveis continuam protegidos quando salvos e recuperados

## DataStore
Como o projeto usa DataStore no lugar de `SharedPreferences`, testes relevantes devem cobrir:
- leitura e escrita assíncronas
- valores padrão
- atualização transacional de preferências
- recuperação após reinicialização controlada quando aplicável
- erro de leitura ou corrupção quando isso fizer parte do comportamento tratado

## Segurança
Como este é um app sensível, toda mudança envolvendo segurança exige testes de sucesso e de falha.

Cobrir, sempre que aplicável:
- criptografia bem-sucedida
- falha de criptografia
- descriptografia bem-sucedida
- falha de descriptografia
- recusa de autenticação
- acesso não autorizado a dado sensível
- persistência segura
- não exposição de segredos em logs, previews, doubles ou mensagens de erro
- comportamento seguro diante de dependência inconsistente

## Regressão
Todo bug relevante corrigido deve, quando viável, ganhar teste de regressão.

A correção só é considerada encerrada quando:
- o defeito é reproduzido por teste ou por cenário objetivo
- a causa foi corrigida sem violar a arquitetura
- a suíte relevante continua verde

## Fase inicial do projeto
Enquanto o usuário não autorizar explicitamente a implementação da primeira feature funcional completa, o foco deve permanecer em:
- cenários
- contratos
- testes
- testabilidade
- segurança
- infraestrutura mínima necessária para testar

Nessa fase, o agente pode criar:
- interfaces
- contratos
- modelos mínimos
- doubles de teste
- scaffolding técnico necessário para suportar a suíte

Nessa fase, o agente não deve:
- avançar para implementação funcional completa por iniciativa própria
- pular testes em nome de velocidade
- inflar a base com infraestrutura sem necessidade clara de teste

## Definition of Done para mudanças relevantes
Uma mudança só pode ser considerada pronta quando:
- os cenários BDD relevantes foram definidos
- os testes correspondentes foram escritos antes da implementação relevante
- os testes falharam pelo motivo correto antes da implementação
- o mínimo necessário foi implementado
- a suíte relevante foi executada novamente e está verde
- não houve quebra arquitetural
- não houve regressão conhecida
- não houve flexibilização indevida de regras de segurança
- a documentação foi atualizada quando a mudança altera estratégia, cobertura, tooling ou contrato de teste

## Quando atualizar este arquivo
Atualizar `docs/testing.md` quando houver, por exemplo:
- mudança relevante na estratégia de testes
- adoção ou remoção de ferramenta de teste
- mudança na forma oficial de testar coroutines, Flow, Hilt, Room ou DataStore no projeto
- novo tipo de teste obrigatório para a definição de pronto
- nova regra de cobertura para segurança, persistência ou arquitetura

## Checklist operacional para o agente
Antes de concluir uma tarefa relevante:
- confirmar quais cenários precisam existir
- identificar o menor nível de teste que cobre o risco
- escrever os testes primeiro
- validar falha inicial
- implementar o mínimo necessário
- rerodar a suíte afetada
- remover código morto, doubles inúteis e imports não usados
- confirmar que não foi criada dependência indevida de infraestrutura nos testes pequenos

## Diretriz final
Neste projeto, a ordem correta não é “implementar e depois testar”.

A ordem correta é:
- entender o comportamento
- definir o contrato
- escrever o teste
- ver a falha certa
- implementar o mínimo necessário
- refatorar com segurança
- validar novamente
