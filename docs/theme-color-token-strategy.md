# Theme Color Token Strategy

## Contexto

O app nasceu com identidade visual dark-first, mas várias telas consumiam diretamente cores como `MidnightBlue`, `DeepNavy`, `SlateBlue`, `SoftWhite` e `MistText`. Isso preservava o Figma em tema escuro, mas fazia o tema claro reaproveitar fundos e textos escuros fora de contexto.

## Decisão

A camada de UI passa a consumir tokens semânticos por meio de `MaterialTheme.vaultColors`.

Os tokens originais continuam existindo como base da paleta escura e como referência de marca, mas não devem ser usados diretamente em telas ou componentes. O uso direto deve ficar restrito aos arquivos de tema, onde as paletas escura e clara são definidas.

## Regra Operacional

Novas telas e componentes devem usar:

- `MaterialTheme.vaultColors.background` para fundo de tela.
- `MaterialTheme.vaultColors.surface`, `surfaceHigh`, `surfaceHighest` e `surfaceBright` para cards, campos e containers.
- `MaterialTheme.vaultColors.textPrimary` e `textSecondary` para textos.
- `MaterialTheme.vaultColors.primary`, `primaryDim` e `secondary` para acentos de marca.
- `MaterialTheme.vaultColors.success`, `warning` e `danger` para estados semânticos.
- Tokens específicos `security*Gradient*` para gradientes de resumo de segurança.

`MaterialTheme.colorScheme` continua válido para componentes Material e estados padrão, mas telas do cofre devem preferir `vaultColors` quando a cor representa linguagem visual própria do app.

## Alternativas Consideradas

Manter apenas `MaterialTheme.colorScheme` foi descartado porque o app possui uma identidade visual própria extraída do Figma, com acentos e superfícies mais específicos do que os papéis padrão do Material 3.

Manter hardcodes locais foi descartado porque reproduz o problema original: o tema claro não consegue adaptar fundos, textos e containers de forma previsível.

## Impactos

O tema escuro preserva a identidade original. O tema claro usa uma variação clara dos mesmos papéis visuais, mantendo os acentos do app sem forçar fundos escuros em todas as telas.

Ao revisar PRs de UI, procurar usos novos de `Color(0x...)`, `MidnightBlue`, `DeepNavy`, `SlateBlue`, `SurfaceBright`, `SoftWhite`, `MistText`, `GhostOutline`, `ElectricBlue`, `NeonPink`, `VaultAmber` ou `VaultGreen` fora do pacote `ui/theme`.
