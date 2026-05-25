# Cursor em TextFields com texto longo

## Causa raiz

O helper compartilhado de `TextFieldValue` normalizava todo texto com `selection` no fim e repetia essa normalizacao em cada `onValueChange`.

Esse comportamento fazia campos com texto longo abrirem ja rolados para o final e tambem sobrescrevia selecoes feitas pelo usuario, impedindo mover o cursor para o meio do texto.

## Ajuste aplicado

O estado do campo passou a preservar `TextFieldValue` localmente e sincronizar apenas o texto aceito pelo estado externo da tela.

A selecao vai para o fim somente quando o campo ganha foco. Enquanto o campo permanece focado, alteracoes manuais de cursor e selecao feitas pelo usuario sao preservadas.

Quando o estado externo rejeita ou limita uma entrada, o texto exibido volta ao valor aceito e a selecao e limitada ao tamanho desse texto.

## Como evitar regressao

Nao recriar helpers que normalizem `TextFieldValue.selection` em todo `onValueChange`.

Para campos editaveis controlados por estado externo, preservar a selecao local e tratar mudancas de foco separadamente de mudancas de texto.

Os cenarios esperados estao cobertos em `EndCursorTextFieldValueTest`.
