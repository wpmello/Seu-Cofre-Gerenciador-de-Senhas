# Branching And Commit Strategy

## Objetivo

Este documento define a regra operacional para criação de branches e commits neste projeto.

Ele existe para evitar:
- branches empilhadas por engano;
- PRs que carregam alterações de contexto errado;
- histórico difícil de revisar;
- retrabalho ao final da tarefa para reorganizar commits e branches.

## Regra principal

Salvo caso explicitamente justificado, **cada branch nova deve ser criada a partir da branch para a qual ela será mergeada**.

Neste projeto, isso significa normalmente:
- criar a branch a partir de `master`;
- atualizar a `master` local antes de iniciar uma nova branch;
- manter a tarefa inteira nessa branch;
- separar responsabilidades por commit.

## Regra obrigatória ao iniciar um novo desenvolvimento

Quando um novo desenvolvimento for iniciado e o repositório local ainda estiver parado em uma branch antiga, o agente **não deve continuar a partir dessa branch antiga por conveniência**.

O procedimento correto é:
- fazer `checkout` para `master`;
- atualizar a `master` local com o estado mais recente já integrado nela;
- só então criar a nova branch de trabalho a partir dessa `master` atualizada.

### Motivo

Essa regra existe para evitar o seguinte erro:
- uma branch antiga já foi mergeada por PR;
- a `master` local ficou desatualizada;
- um novo desenvolvimento foi iniciado sem voltar para a `master`;
- o novo trabalho acabou sendo commitado em uma branch de contexto anterior.

### Regra prática

Antes de começar qualquer tarefa nova, o agente deve verificar:
- se a branch atual é apenas resquício de uma entrega anterior;
- se a `master` local já contém as atualizações mais recentes da branch remota de integração;
- se a nova branch nascerá da `master` atualizada, e não da branch antiga ainda aberta no workspace.

### Proibição explícita

Não é permitido:
- iniciar uma nova entrega diretamente em uma branch antiga só porque ela ainda está checkoutada localmente;
- assumir que uma branch antiga continua sendo base válida depois que o trabalho anterior já foi mergeado;
- criar a nova branch a partir de uma `master` local desatualizada quando o objetivo é continuar do ponto mais recente já integrado ao projeto.

## Regra de modelagem de trabalho

### 1. Uma tarefa coesa = uma branch

Se o usuário pediu uma única entrega coesa, a branch deve representar essa entrega inteira.

Exemplos:
- nova tela;
- correção de bug;
- ajuste de build;
- documentação de uma decisão;
- refactor localizado.

### 2. Uma branch não precisa conter um único tipo de arquivo

Uma feature pode incluir, na mesma branch:
- código de UI;
- navegação;
- recursos;
- testes;
- ajustes de dependência;
- ajustes de tema;
- documentação específica necessária para a própria feature.

Isso **não** exige múltiplas branches se tudo fizer parte da mesma entrega.

### 3. A separação deve acontecer por commit

Quando houver mais de uma natureza de mudança dentro da mesma tarefa, separar por commits lógicos.

Exemplos de commits válidos dentro da mesma branch de feature:
- commit de dependência necessária;
- commit da implementação principal;
- commit de testes;
- commit de documentação específica da entrega.

## O que não fazer

### Não empilhar branches por padrão

Não criar:
- `docs/...` em cima de `feature/...`
- `chore/...` em cima de `feature/...`
- `fix/...` em cima de `docs/...`

quando a intenção for abrir PRs separados para a branch de integração.

Esse erro faz com que branches posteriores carreguem commits anteriores por acidente.

### Não separar dependência em branch própria sem necessidade

Se a dependência foi introduzida para viabilizar a tarefa atual, ela deve ficar na mesma branch da tarefa.

Exemplo:
- uma tela nova que exige `Navigation Compose` ou outra lib necessária à própria tela;
- isso deve permanecer na branch da feature;
- a separação correta é por commit, não por branch adicional.

## Quando criar múltiplas branches para o mesmo contexto

Só faz sentido quando as entregas são realmente independentes e mergeáveis separadamente.

Critérios:
- cada branch pode ser revisada e mergeada sem depender da outra;
- cada branch tem valor próprio;
- cada branch não carrega commits estranhos ao seu objetivo;
- a base de merge está clara.

Exemplos válidos:
- uma branch só de documentação que pode ser mergeada sem depender da feature;
- uma branch só de tooling/infra que tem valor próprio e não altera a regra funcional da tarefa principal;
- uma branch de segurança independente de uma feature visual.

Mesmo nesses casos, a base continua sendo a branch de integração alvo, salvo instrução explícita em contrário.

## Workflow obrigatório antes de criar commits finais

### Regra de encerramento explícito

Existe uma diferença obrigatória entre:

- pedido para apenas criar commits
- pedido para criar commits **e encerrar o desenvolvimento**

Quando o usuário pedir algo equivalente a:

- `faça os commits e feche o desenvolvimento`
- `faça os commits, é o final da feature`
- `faça os commits e finalize`

o agente deve tratar isso como **encerramento explícito da entrega**.

Quando o usuário pedir apenas algo equivalente a:

- `faça os commits`

o agente **não** deve assumir encerramento da entrega e **não** deve gerar automaticamente texto de descrição de pull request.

### Etapa 1 — Entender o contexto da tarefa

O agente deve identificar:
- qual é a entrega principal;
- se existe apenas uma tarefa coesa ou mais de uma entrega independente;
- qual é a branch de integração alvo;
- se o histórico pretendido será por uma branch ou por várias.

### Etapa 2 — Definir a base correta

Por padrão:
- usar `master` como base.

Exceção:
- quando o usuário disser explicitamente que o merge será para outra branch.

### Etapa 2.1 — Atualizar a branch de integração local antes de iniciar nova tarefa

Se a tarefa é um **novo desenvolvimento** e o agente estiver parado em uma branch anterior:
- sair da branch antiga;
- voltar para a branch de integração alvo, normalmente `master`;
- atualizar essa branch local com o estado mais recente já integrado no remoto;
- criar a nova branch somente depois dessa atualização.

Essa etapa é obrigatória mesmo quando o desenvolvimento anterior já tiver sido mergeado, porque o merge no remoto não atualiza sozinho a `master` local.

### Etapa 3 — Agrupar mudanças por objetivo

Antes de commitar, o agente deve classificar as mudanças em grupos lógicos.

Exemplos:
- implementação principal;
- testes;
- documentação;
- tooling.

### Etapa 4 — Decidir branch única ou múltiplas branches

Pergunta obrigatória:

As mudanças fazem parte de uma única entrega ou de entregas independentes?

Se for uma única entrega:
- uma única branch;
- múltiplos commits lógicos.

Se forem entregas independentes:
- múltiplas branches;
- cada uma criada a partir da branch de integração alvo.

### Etapa 5 — Validar a árvore de branches antes de abrir PR

O agente deve verificar:
- se a branch atual parte da base correta;
- se ela não carrega commits de outra branch por acidente;
- se o PR resultante terá somente os arquivos esperados.

### Etapa 6 — Gerar texto para descrição do PR apenas no encerramento explícito

Somente quando o usuário tiver indicado que é o encerramento da entrega, o agente deve:

1. analisar o que realmente entrou na branch e nos commits finais;
2. resumir o objetivo da entrega;
3. listar mudanças principais em linguagem de revisão;
4. destacar testes/validações executados;
5. registrar riscos, limitações ou TODOs relevantes, se existirem;
6. entregar um texto pronto para o usuário colar na descrição do pull request.

Esse texto deve ser baseado no que realmente foi implementado, não em intenção genérica.

### Template obrigatório da descrição de PR

Salvo instrução explícita do usuário em contrário, a descrição de PR gerada no encerramento da entrega deve usar este formato em Markdown:

```md
# Objetivo

# O que mudou

# Decisões arquiteturais

# Arquivos principais

# Testes adicionados/ajustados

# Validação executada

# Observações
```

### Regras para esse template

- usar exatamente esses títulos como base;
- preencher apenas com informação derivada do trabalho realmente entregue;
- preferir bullets curtos e verificáveis;
- listar validações realmente executadas, não validações presumidas;
- mencionar arquivos principais por área de mudança, sem transformar a descrição em changelog exaustivo;
- usar `# Observações` apenas quando houver contexto funcional relevante sobre o escopo entregue, como ações não implementadas, não alteradas, propositalmente fora do escopo ou limitações perceptíveis para produto/revisão;
- preferir observações concretas sobre a funcionalidade desenvolvida, por exemplo: `A ação de excluir senha não foi implementada nem alterada.`;
- manter riscos técnicos, TODOs, dependências externas ou contexto local fora de `# Observações`; quando forem relevantes para a revisão, usar uma seção específica adicional.

### Seções adicionais permitidas

O agente pode incluir seções extras apenas quando agregarem valor concreto à revisão, por exemplo:

- `# Riscos`
- `# TODOs`
- `# Impactos`
- `# Migração`

Essas seções adicionais não substituem o template base; elas apenas complementam quando necessário.

### Regra de não ativação

Se o usuário não disser explicitamente que está fechando o desenvolvimento:

- não gerar automaticamente texto de PR;
- não inferir que o momento de abrir PR chegou;
- limitar a resposta ao trabalho de branches e commits pedido.

## Procedimento de correção quando a base estiver errada

Se uma branch foi criada sobre base errada, corrigir sem perder conteúdo:

1. criar branch de backup do estado atual;
2. identificar o commit ou conjunto mínimo de commits que pertencem à branch;
3. recriar a branch a partir da base correta;
4. reaplicar apenas os commits relevantes com `cherry-pick`;
5. validar o grafo final;
6. só então atualizar o remoto.

### Regra de segurança

Nunca reescrever sem antes preservar:
- backup branch; ou
- stash; ou
- ambos, quando a árvore local estiver suja.

## Caso real já observado neste projeto

Houve um caso em que:
- `feature` foi criada primeiro;
- `docs` foi criada em cima da `feature`;
- `chore` foi criada em cima de `docs`.

Resultado:
- os PRs de `docs` e `chore` não podiam ser mergeados de forma limpa no destino esperado;
- foi necessário recriar branches e reaplicar commits.

### Lição consolidada

Esse tipo de empilhamento só deve acontecer quando a estratégia de stack estiver explícita e aceita.  
Fora disso, cada branch deve nascer da branch de integração alvo.

Houve também um caso em que:
- uma branch antiga continuou ativa localmente após o merge da entrega anterior;
- a `master` local não foi atualizada;
- um novo desenvolvimento precisou ser commitado na branch antiga porque a base correta não tinha sido preparada antes.

### Lição adicional consolidada

Antes de qualquer novo desenvolvimento:
- voltar para `master`;
- atualizar a `master` local;
- criar a nova branch a partir dessa base atualizada.

Se isso não for feito, o histórico pode misturar entregas independentes e forçar commits em branch errada mesmo quando o PR anterior já tiver sido mergeado.

## Resumo executivo

Usar esta regra mental:

- uma entrega coesa -> uma branch;
- múltiplas naturezas de mudança -> múltiplos commits;
- múltiplas entregas independentes -> múltiplas branches;
- cada branch nasce da branch de merge alvo;
- se a base estiver errada, recriar com backup e `cherry-pick`;
- texto de descrição de PR só é gerado quando o usuário explicitar o encerramento da entrega;
- quando gerado, o texto deve seguir o template padrão do projeto.
