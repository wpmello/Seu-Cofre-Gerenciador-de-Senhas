# Crash na abertura da tela de categorias por carregamento incorreto de drawable

## Problema
Ao sair do onboarding e abrir a tela de categorias, o app encerrava com o seguinte erro:

```text
Caused by: java.lang.NullPointerException: null cannot be cast to non-null type android.graphics.drawable.BitmapDrawable
at androidx.compose.ui.res.ImageResources_androidKt.imageResource(ImageResources.android.kt:39)
```

O crash acontecia no carregamento de ícones da tela de categorias.

## Causa raiz
Foram identificadas duas causas raiz do mesmo tipo de falha:

### 1. Colisão de nome de recurso
O projeto tinha dois recursos com o mesmo nome lógico `ic_search`:

- `app/src/main/res/drawable/ic_search.xml`
- `app/src/main/res/drawable-nodpi/ic_search.png`

Essa colisão deixava a resolução do drawable ambígua em runtime. Na abertura da tela, o Compose acabava entrando no caminho de carregamento incompatível com o recurso efetivamente resolvido, resultando no cast inválido para `BitmapDrawable`.

### 2. Asset SVG salvo com extensão `.png`
O recurso `app/src/main/res/drawable-nodpi/ic_current_category.png` não era um PNG real. O arquivo começava com `<svg ...>`, ou seja, era um SVG textual salvo com extensão errada.

Nesse cenário, o Compose tentava tratar o arquivo como bitmap por causa da extensão e falhava em runtime no mesmo caminho de `BitmapDrawable`.

## Correção aplicada
- Removido o asset duplicado `drawable-nodpi/ic_search.png`
- Mantido apenas o vetor `drawable/ic_search.xml` como fonte de verdade do ícone
- Alterado o carregamento na UI para `ImageVector.vectorResource(R.drawable.ic_search)`, deixando explícito que o recurso esperado é vetorial
- Removido o falso PNG `drawable-nodpi/ic_current_category.png`
- Criado `drawable/ic_current_category.xml` como vetor Android válido equivalente ao asset usado no card azul
- Alterado o carregamento desse ícone para `ImageVector.vectorResource(R.drawable.ic_current_category)`

## Regra para evitar recorrência
- Não manter bitmap e vetor com o mesmo nome lógico em `res/drawable*`
- Para ícones vetoriais usados em Compose, preferir `ImageVector.vectorResource(...)` quando o recurso for explicitamente um vetor
- Não salvar SVG exportado do Figma com extensão `.png`; validar a assinatura do arquivo antes de integrar assets rasterizados
- Ao importar assets do Figma, verificar colisões de nome entre `drawable/`, `drawable-nodpi/`, `mipmap/` e demais variantes antes de integrar a tela

## Validação esperada
- A tela de categorias deve renderizar sem crash ao sair do onboarding
- A suíte deve manter pelo menos um teste de renderização da tela para detectar falhas semelhantes de resource loading
