# Splash Screen UI Retrospective

## Objetivo

Este documento registra as decisões corretas e incorretas observadas na implementação da splash screen do app.

Ele existe para servir como referência rápida quando telas futuras reutilizarem:
- o motivo visual de escudo;
- o container de marca;
- o halo central;
- a composição de loading seguro.

## Contexto

Frame de referência no Figma:
- arquivo `Seu Cofre`
- node `1:3`

Implementação de referência no código:
- [SplashScreen.kt](/workspace/app/src/main/java/com/inovalou/seucofregerenciadordesenhas/feature/splash/presentation/SplashScreen.kt)

## O que foi feito de errado

### 1. O escudo inicial não refletia bem o frame
A primeira solução usou um desenho vetorial manual que, embora funcional, não tinha leitura suficientemente próxima do escudo aprovado no Figma.

### 2. O fundo do logo ficou pesado demais
O bloco do logo foi tratado inicialmente como um card muito denso. O frame aprovado pedia uma combinação mais leve entre superfície tonal e glow difuso.

### 3. Houve uso incorreto de APIs composable em `drawBehind`
`MaterialTheme.colorScheme.outlineVariant` foi acessado dentro de `drawBehind`, o que quebrou a compilação.

### 4. A reconsulta ao frame demorou mais do que deveria
Quando o símbolo central ainda não coincidia bem com a intenção visual, o passo correto era reabrir o frame antes de continuar refinando localmente.

## O que foi feito de certo

### 1. O frame foi revalidado antes da iteração final
Isso evitou que a tela continuasse evoluindo com base em memória ou interpretação frouxa do design.

### 2. O padrão visual foi simplificado e aproximado do Figma
O resultado final ficou mais fiel por seguir a estrutura central do frame:
- halo circular suave;
- card com `40.dp` de raio;
- símbolo de escudo central;
- barra de progresso fina com gradiente;
- hint secundário discreto.

### 3. O símbolo foi tratado como motivo reutilizável
O escudo deixou de ser um detalhe isolado da splash e passou a ser entendido como parte da identidade visual do app.

### 4. O erro de Compose foi corrigido da forma certa
A correção correta foi resolver tokens do tema antes das draw lambdas, e não contornar a falha com hardcodes espalhados.

## Regras para futuras telas

### Repetir
- usar a splash como referência quando houver escudo central com leitura de segurança;
- manter halo suave e profundidade tonal em vez de sombra pesada;
- preservar proporção, radius e hierarquia visual do bloco do logo;
- resolver cores do tema fora de `drawBehind`, `drawWithCache` e `Canvas`.

### Evitar
- reinterpretar demais o escudo quando o Figma já o define suficientemente;
- transformar o bloco central em card genérico de componente;
- introduzir variações visuais sem necessidade em telas que deveriam reforçar a mesma identidade de marca.

## Decisão operacional

Para futuras telas que reutilizarem este motivo visual:
- consultar primeiro o frame exato do Figma, se existir;
- usar `docs/ui-source-of-truth-figma-compose.md` e `docs/design-system-operational-source-of-truth.md` como regra operacional;
- usar este documento como memória do caso real já resolvido no projeto.
