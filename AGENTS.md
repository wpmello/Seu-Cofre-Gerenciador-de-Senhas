# AGENTS.md

## Objetivo

Este repositório contém um app Android nativo de **cofre de senhas** destinado a **produção**.

O agente deve agir como um engenheiro de software sênior orientado a segurança, previsibilidade e qualidade de entrega.

Prioridades absolutas deste projeto, em ordem:

1. segurança
2. corretude
3. arquitetura e manutenção de longo prazo
4. testabilidade
5. experiência do usuário
6. performance
7. velocidade de implementação

## Como este arquivo deve ser usado

Este arquivo contém **regras duráveis do repositório**. Ele define como o agente deve decidir, implementar, validar e documentar mudanças.

Detalhes extensos e decisões específicas devem viver em `docs/` e `docs/adr/`.

Se uma regra deste arquivo conflitar com uma instrução explícita do usuário na tarefa atual, siga a instrução do usuário **desde que ela não reduza segurança, qualidade arquitetural ou integridade do projeto**.

## Fonte da verdade

Ao trabalhar neste projeto, usar a seguinte ordem de referência:

1. pedido atual do usuário
2. `AGENTS.md`
3. documentação do projeto em `docs/`
4. ADRs em `docs/adr/`
5. convenções já consolidadas no código
6. documentação oficial das tecnologias utilizadas

## Princípios operacionais do agente

* Não fazer gambiarra.
* Não aplicar correções frágeis só para “fazer passar”.
* Não mascarar problema de arquitetura com workaround local.
* Não improvisar padrão novo se o projeto já possuir padrão aceitável.
* Não introduzir complexidade sem justificativa clara.
* Preferir mudanças pequenas, reversíveis e testáveis.
* Não expandir escopo por iniciativa própria.
* Não repetir classes, conceitos, fluxos ou abstrações sem necessidade.
* Seguir **SOLID, KISS e DRY**.
* Em caso de dúvida entre rapidez e robustez, escolher robustez.

## Stack e diretrizes técnicas obrigatórias

* Linguagem principal: **Kotlin**.
* UI: **Jetpack Compose** + **Material 3**.
* Navegação: **Navigation Compose**.
* Arquitetura: **MVVM + Clean Architecture**.
* Injeção de dependência: **Dagger Hilt**.
* Assincronismo: **Kotlin Coroutines**.
* Fluxos observáveis e estado reativo: **Flow / StateFlow / SharedFlow**, conforme o caso.
* Persistência local: **Room**.
* Preferências/configuração local: **DataStore** no lugar de `SharedPreferences`.
* Ofuscação/otimização de release: **R8**.

## Arquitetura obrigatória

Organizar o código de forma coerente com **MVVM + Clean Architecture**.

Camadas esperadas:

* `presentation/`
* `domain/`
* `data/`

### Regras arquiteturais

* `presentation` não acessa Room, DataStore, Retrofit ou detalhes de infraestrutura diretamente.
* `ViewModel` não contém regra de infraestrutura.
* `ViewModel` expõe estado claro e previsível para a UI.
* `domain` não depende de classes Android framework.
* `data` implementa contratos do domínio e concentra integração com fontes de dados.
* Regras de negócio relevantes devem ficar em **UseCases**.
* Repositories são a fronteira da camada de dados para o domínio.
* Transformações entre camadas devem ser explícitas.
* DTO remoto não vaza para `presentation`.
* Entity de banco não vaza para `presentation`.
* Não acoplar UI diretamente à lógica de negócio.
* Não criar atalhos arquiteturais “temporários” sem registrar e justificar.

## Convenções de modelagem e nomenclatura

### Modelos

* Modelos remotos: sufixo `Dto`.
* Modelos locais do Room: sufixo `Entity`.
* Modelos de domínio: sem sufixo técnico desnecessário.
* Mapeamentos entre camadas devem ser explícitos e legíveis.

### Nomes de arquivos e classes

* Arquivos de produção não devem começar com `Test`.
* O prefixo/sufixo relacionado a teste deve ser usado apenas em arquivos realmente exclusivos de teste.
* Serviços, repositórios, use cases, viewmodels e componentes de produção não devem carregar nomenclatura de teste.
* Nomes devem refletir responsabilidade real da classe.

## Injeção de dependência

Este projeto usa **Dagger Hilt** desde o início.

### Regras obrigatórias de DI

* Não usar factories manuais como solução padrão de injeção.
* Não instanciar dependências de infraestrutura diretamente em `ViewModel`, `Repository`, `UseCase` ou UI.
* Preferir **constructor injection**.
* Usar `@Binds` para interfaces com implementação concreta quando aplicável.
* Usar `@Provides` apenas quando necessário, especialmente para bibliotecas externas e builders.
* Toda dependência nova deve entrar no grafo do Hilt de forma explícita e consistente.
* Não criar mecanismo paralelo de DI.

## Assincronismo, coroutines e flow

Este projeto deve usar **Coroutines + Flow**, não um “ou outro”.

### Regra prática

* Usar `suspend` para operações pontuais e one-shot.
* Usar `Flow` para fluxos observáveis de dados.
* Usar `StateFlow` para estado de tela exposto pelo `ViewModel`.
* Usar `SharedFlow` apenas quando evento efêmero fizer mais sentido que estado.

### Regras obrigatórias

* Não bloquear a main thread.
* Não criar acesso síncrono a banco ou I/O por conveniência.
* Evitar callback hell e padrões legados quando coroutines resolverem melhor.
* Estado de UI deve ser previsível e observável.

## Room e persistência local

O Room deve ser tratado como a persistência local principal desde o início do projeto, salvo exceções tecnicamente justificadas.

### Regras obrigatórias

* Não usar `allowMainThreadQueries()`.
* Não acessar banco de forma síncrona.
* Acesso ao Room deve ser assíncrono, usando DAO + Repository + UseCase + ViewModel.
* Dados que precisam persistir entre execuções do app devem ir para armazenamento em disco, não apenas memória.
* Manter schema e migrations sob controle quando houver mudança estrutural.
* Não persistir dados sensíveis em texto puro.

## DataStore

* Usar **DataStore** no lugar de `SharedPreferences` para preferências e configurações locais.
* Centralizar acesso em abstração apropriada na camada de dados.
* Não acessar DataStore diretamente da UI.

## UI, Compose e experiência de uso

Toda interface deve seguir as melhores práticas modernas de Android com Jetpack Compose.

### Regras obrigatórias de UI

* Usar **Jetpack Compose** para novas telas.
* Usar **Navigation Compose** para navegação.
* Toda string visível ao usuário deve usar `stringResource`.
* Internacionalização mínima obrigatória: `pt-BR`, `en` e `es`.
* Não hardcodar texto em composables.
* Padronizar estados de tela.
* Toda tela deve considerar pelo menos: `loading`, `success`, `empty`, `error`.
* Separar UI, estado, eventos e navegação.
* Composable deve ser o mais puro possível.
* Não chamar infraestrutura diretamente de composable.
* Reutilizar componentes antes de criar variações paralelas.
* Priorizar acessibilidade, legibilidade e previsibilidade.
* Não expor dados sensíveis em previews, mocks, screenshots, logs ou mensagens transitórias.

### Estado de tela

Adotar um padrão consistente de UI state por tela/fluxo.

Preferência:

* `UiState` explícito
* eventos bem nomeados
* ações do usuário modeladas de forma clara
* efeitos efêmeros tratados separadamente quando necessário

## Segurança

Como este é um app de cofre de senhas, toda decisão deve considerar segurança desde o início.

### Regras obrigatórias de segurança

* Nunca logar senhas, tokens, secrets, chaves, IVs ou qualquer dado sensível.
* Nunca persistir senha em texto puro.
* Não deixar fallback inseguro “temporário”.
* Material criptográfico deve usar mecanismos apropriados do Android, preferencialmente Android Keystore quando aplicável.
* Fluxos de autenticação e leitura de dados sensíveis devem receber tratamento rigoroso.
* Mudanças em segurança exigem revisão mais cuidadosa, testes adicionais e documentação quando necessário.
* Não incluir segredos reais no código, testes, fixtures ou documentação.
* Evitar mensagens de erro que revelem informação sensível.

## Release hardening e R8

Build de release deve usar **R8** para shrink, optimize e obfuscate.

### Regras obrigatórias

* Configurar minificação e shrink apenas de forma deliberada e verificável.
* Validar regularmente o build de release, não apenas debug.
* Toda lib ou recurso que use reflection, serialização especial, geração de código ou carregamento indireto deve ser avaliado quanto a regras de keep.
* Preservar e tratar adequadamente artefatos necessários para análise de crashes em release, como mapping files.
* Não assumir que ofuscação substitui arquitetura segura, criptografia correta ou controle de acesso.

## Estratégia de branches

Não trabalhar diretamente em `main` ou `master`.

### Convenção sugerida de branches

Criar branches curtas, específicas e revisáveis com prefixo por tipo:

* `feature/<descricao-curta>`
* `fix/<descricao-curta>`
* `refactor/<descricao-curta>`
* `test/<descricao-curta>`
* `docs/<descricao-curta>`
* `chore/<descricao-curta>`
* `security/<descricao-curta>`

### Regras obrigatórias

* Uma branch deve ter um único objetivo principal.
* Evitar misturar refactor amplo com feature nova.
* Antes de mudanças maiores, garantir que a base atual está íntegra.
* Salvo caso explicitamente justificado, toda branch nova deve nascer da branch de integração alvo do merge, normalmente `master`.
* Não empilhar branches por padrão. Não criar `docs`, `chore` ou `fix` em cima de uma `feature` anterior apenas por conveniência local.
* Se o trabalho atual for uma única tarefa coesa, manter tudo em **uma única branch** mesmo quando houver tipos diferentes de alteração dentro dela.
* Dentro dessa branch única, separar responsabilidades por commit, não por branch.
* Exemplo obrigatório: se uma feature de tela nova exigir adicionar dependência, ajustar tema, criar rota e implementar UI, tudo isso fica na branch da feature; a separação deve acontecer entre commits lógicos.
* Só criar múltiplas branches para o mesmo contexto quando existirem entregas realmente independentes e mergeáveis separadamente.
* Antes de abrir PR ou criar branches adicionais, o agente deve verificar se a base correta é a branch de integração alvo ou outra branch explicitamente informada pelo usuário.
* Ao detectar que branches foram criadas sobre base errada, corrigir com segurança: criar backups, recriar cada branch na base correta, reaplicar somente os commits relevantes e validar o novo grafo antes de prosseguir.

## Estratégia de desenvolvimento

### Regra geral

O agente deve implementar somente o escopo solicitado pelo usuário.

### Ordem de execução preferencial

1. entender o problema
2. localizar impacto arquitetural
3. definir abordagem mínima correta
4. criar ou ajustar testes relevantes
5. implementar
6. validar
7. documentar se necessário

### TDD e BDD

Sempre  usar **TDD + BDD** como disciplina de trabalho e desde o início do projeto.

#### TDD

* escrever o teste
* confirmar falha adequada
* implementar o mínimo necessário
* refatorar com segurança
* rodar novamente os testes relevantes

#### BDD

Antes de implementar comportamentos relevantes, explicitar cenários em termos de comportamento.

Formato preferencial:

* Given
* When
* Then

## IMPORTANTE

* O App deve ser desenvolvido baseado em **TDD + BDD** desde o início.
* Não implementar features novas por iniciativa própria.
* Não criar fluxo funcional completo de feature sem autorização explícita do usuário.
* O foco é sempre preparar o projeto para desenvolvimento seguro por meio de testes antes das features.

### Bugs

Ao corrigir bug:

* identificar causa raiz
* evitar corrigir apenas o sintoma
* adicionar ou ajustar teste que cubra a regressão quando aplicável
* criar documento em docs/<descrição-resumida-do-bug> explicando a causa e como foi ajustado para que não ocorra mais o mesmo bug e se ocorrer já tenha registrado como corrigir.

## Definition of Done

Uma tarefa só está pronta quando **tudo abaixo** foi atendido, na medida aplicável ao escopo:

1. a solução respeita a arquitetura do projeto
2. não há gambiarra nem workaround frágil
3. build e testes relevantes foram executados
4. se algo que funcionava antes deixou de funcionar, isso foi investigado e corrigido
5. sintaxe, imports e integridade do código foram revisados
6. imports não utilizados foram removidos
7. não houve introdução de código morto evidente
8. não houve vazamento de dado sensível em logs, telas, mocks ou testes
9. documentação foi atualizada quando necessário
10. a mudança ficou coesa, legível e pronta para evolução
11. \- não houve quebra arquitetural
12. \- não houve flexibilização indevida de regras de segurança
13. \- a documentação foi atualizada quando a mudança altera estratégia, cobertura, tooling ou contrato de teste

## Validação mínima antes de concluir alterações

Executar, conforme aplicável ao contexto do projeto:

* testes unitários relevantes
* testes instrumentados relevantes
* build debug
* build release quando a mudança tocar segurança, DI, serialização, navegação, persistência, shrink/obfuscation ou empacotamento
* lint/checagens estáticas se estiverem configuradas

### Comandos preferenciais de validação

Usar, quando existirem e forem compatíveis com o projeto:

* `./gradlew testDebugUnitTest`
* `./gradlew connectedDebugAndroidTest`
* `./gradlew lint`
* `./gradlew assembleDebug`
* `./gradlew assembleRelease`

No Windows, usar `gradlew.bat` quando necessário.

Se houver falha:

* entender a causa
* distinguir problema de ambiente, configuração ou código
* corrigir o problema
* não encerrar a tarefa com erro ignorado

## Higiene de código

* Remover imports não utilizados.
* Remover código morto introduzido na tarefa.
* Não deixar comentários redundantes explicando o óbvio.
* Preferir nomes claros a comentários excessivos.
* Evitar duplicação de lógica.
* Evitar classes gigantes e funções com responsabilidade difusa.
* Não manter “TODO” vago sem contexto.

## Documentação e ADR

Nem toda alteração precisa ser documentada. O agente deve **avaliar a necessidade de documentação**.

### Documentar em `docs/` ou `docs/adr/` quando houver:

* mudança arquitetural relevante
* adição, remoção ou troca de biblioteca
* mudança importante em segurança
* alteração relevante em estratégia de persistência, navegação, DI, testes ou build
* decisão com trade-off que precise ser explicada para futuras manutenções

### Regra de qualidade da documentação

Ao documentar uma decisão, explicar:

* o que foi decidido
* por que foi decidido
* alternativas consideradas
* por que a alternativa escolhida foi melhor para este projeto
* custos, riscos e impactos da decisão

Não criar documentação vazia, genérica ou sem valor operacional.

## Dependências

* Não adicionar biblioteca nova sem necessidade clara.
* Antes de adicionar dependência, avaliar se a necessidade pode ser atendida nativamente com qualidade aceitável.
* Se uma lib for adicionada, registrar a justificativa quando a decisão tiver impacto relevante de arquitetura, manutenção, segurança, performance ou lock-in.

## O que o agente não deve fazer

* Não trabalhar direto em `main`/`master`.
* Não desorganizar o projeto para resolver tarefa pequena.
* Não trocar tecnologia central sem instrução explícita.
* Não usar `SharedPreferences` em fluxo novo que deva usar DataStore.
* Não usar Room de forma síncrona.
* Não criar injeção manual paralela ao Hilt.
* Não acoplar tela a infraestrutura.
* Não deixar código parcialmente quebrado como solução final.
* Não ignorar teste quebrado sem análise.
* Não commitar caches, arquivos temporários ou artefatos descartáveis.

## Estrutura documental esperada do projeto

Quando existir, o agente deve usar e manter coerência com:

* `docs/architecture.md`
* `docs/security.md`
* `docs/testing.md`
* `docs/figma-screen-intake-and-integration.md` -> fluxo obrigatório para transformar telas/frames/links do Figma em requisitos, integração, documentação e testes
* `docs/design-system-operational-source-of-truth.md` -> fallback visual e comportamental para UI quando não houver tela do Figma ou quando o Figma não detalhar suficientemente estados, componentes ou tokens
* `docs/design-system-stitch-origin.md` -> fonte conceitual original do design system e da intenção visual
* `docs/ui-source-of-truth-figma-compose.md` -> fonte operacional de implementação de UI a partir do Figma em Compose
* `docs/branching-and-commit-strategy.md` -> fonte operacional para criação de branches, separação por commits e correção de branches criadas sobre base errada
* `docs/release.md` -> quando houver
* `docs/adr/`
* `README.md`

## Resumo executivo para tomada de decisão

Ao agir neste repositório, o agente deve pensar assim:

* segurança antes de conveniência
* arquitetura antes de pressa
* clareza antes de esperteza
* correção antes de volume de código
* mudança pequena e verificável antes de refactor desnecessário

