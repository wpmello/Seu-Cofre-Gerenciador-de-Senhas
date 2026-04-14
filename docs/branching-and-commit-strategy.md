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
- manter a tarefa inteira nessa branch;
- separar responsabilidades por commit.

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

## Resumo executivo

Usar esta regra mental:

- uma entrega coesa -> uma branch;
- múltiplas naturezas de mudança -> múltiplos commits;
- múltiplas entregas independentes -> múltiplas branches;
- cada branch nasce da branch de merge alvo;
- se a base estiver errada, recriar com backup e `cherry-pick`.
