# Line Endings And Worktree Hygiene

## Objetivo

Evitar diffs mecânicos causados apenas por normalização de fim de linha, sem mudança funcional real.

## Problema observado

Este projeto chegou a ter arquivos versionados com três estados diferentes:
- `LF`
- `CRLF`
- `mixed`

Sem uma política versionada no próprio repositório, o fim de linha ficou dependente de:
- configuração local do Git;
- sistema operacional da máquina;
- editor/IDE usado pelo desenvolvedor;
- comportamento de criação de arquivos novos.

O efeito prático foi:
- arquivos aparecerem como modificados sem mudança semântica;
- commits de escopo funcional ficarem contaminados por diffs mecânicos;
- risco de uma entrega pequena gerar um commit grande e difícil de revisar.

## Decisão adotada

O repositório passa a adotar:
- `.gitattributes` como fonte de verdade para normalização;
- `.editorconfig` para orientar os editores;
- `LF` como padrão para arquivos texto do projeto;
- `CRLF` somente para scripts Windows quando necessário.

## Regra operacional

Ao encontrar diffs grandes que desaparecem com `git diff --ignore-cr-at-eol`, tratar isso primeiro como possível problema de line ending, não como mudança funcional.

Checklist:
- verificar se o diff some com `--ignore-cr-at-eol`;
- separar a normalização mecânica de qualquer mudança funcional;
- não misturar renormalização ampla com feature, fix ou refactor;
- usar branch/commit próprios quando a renormalização for necessária.

## Quando renormalizar é permitido

Renormalização ampla é aceitável quando:
- o repositório acabou de receber regras novas de `.gitattributes`/`.editorconfig`;
- há necessidade real de alinhar arquivos legados à política atual;
- a renormalização for isolada em commit próprio;
- o objetivo for reduzir ruído futuro e não mascarar mudança funcional.

## Quando não fazer

Não renormalizar em massa:
- no meio de uma feature funcional sem necessidade;
- misturando com correções de comportamento;
- só porque o editor local regravou arquivos tocados incidentalmente.

## Comando útil de diagnóstico

Para verificar se um diff é apenas de fim de linha:

```bash
git diff --ignore-cr-at-eol
```

Se o diff desaparecer, a alteração é mecânica e deve ser tratada separadamente.
