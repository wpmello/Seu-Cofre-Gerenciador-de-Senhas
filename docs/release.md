# Release

## Objetivo
Este documento atua como referência mínima para decisões e verificações de release do projeto.

## Direção atual
As regras principais de release continuam definidas em:
- `AGENTS.md`
- `docs/security.md`
- `docs/testing.md`

## Pontos obrigatórios
Em alterações que afetem build, empacotamento, shrink/obfuscation, persistência, navegação, serialização, DI ou segurança, validar quando aplicável:
- build debug
- build release
- testes relevantes
- regras de keep e comportamento de R8

## Evolução
Quando a estratégia de release ficar mais detalhada, este arquivo deve concentrar o procedimento operacional consolidado.
