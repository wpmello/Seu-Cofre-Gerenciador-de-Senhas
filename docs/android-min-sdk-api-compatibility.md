# Compatibilidade de APIs com minSdk 24

## Causa

O `lint` identificou chamadas a APIs disponíveis apenas a partir do Android API 26 em um app com `minSdk` 24:

- `java.util.Base64` no fluxo de criptografia e fingerprint de senhas;
- `java.time` na formatação de datas da tela de edição de senha.

Essas chamadas compilavam, mas poderiam quebrar em dispositivos Android 7.x quando executadas sem desugaring adequado para essas APIs.

## Correção

O fluxo de criptografia passou a usar `android.util.Base64` com `Base64.NO_WRAP`, preservando o formato sem quebras de linha usado anteriormente por `java.util.Base64`.

A formatação de data passou a usar `SimpleDateFormat` com `Date`, APIs compatíveis com `minSdk` 24. A instância é criada localmente no ponto de uso para evitar compartilhamento de estado mutável.

## Validação

Após o ajuste, `./gradlew lint` passou sem erros de `NewApi`.
