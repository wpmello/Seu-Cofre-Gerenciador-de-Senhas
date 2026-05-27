# Security Details Screen

## O que foi decidido

A tela `SecurityDetailsScreen` apresenta uma visão agregada da saúde das senhas do cofre a partir da mesma análise individual usada pelo app. Ela não cria uma regra nova de scoring.

A tela consome um estado preparado na camada de apresentação com:

- índice médio do cofre;
- status geral derivado do índice médio;
- total de senhas analisadas;
- critérios/faixas exibidos ao usuário;
- contadores por bucket;
- lista de senhas por bucket.

## Regra de segurança usada

A fonte de verdade para análise individual continua sendo `EvaluatePasswordSecurityUseCase`, coerente com `password-strength-security-policy-v1`.

A agregação da tela usa os scores individuais já calculados para:

- calcular a média arredondada do cofre;
- derivar o status geral do cofre;
- agrupar cada senha em `Weak`, `Moderate` ou `Safe`.

Os buckets da tela seguem os mesmos limiares operacionais usados pelo resumo do cofre:

- `0 - 49`: fraca / ruim;
- `50 - 90`: moderada;
- `91 - 100`: forte / excelente.

Esses buckets são usados para organização da tela de detalhes. A análise individual de risco da política de segurança continua centralizada no domínio e não deve ser reimplementada em Compose.

## Por que foi decidido assim

O `SecuritySummaryCard` já mostrava uma avaliação agregada do cofre. A nova tela detalha essa avaliação sem duplicar lógica de negócio e sem expor senhas em claro para a UI.

Centralizar a avaliação em use cases de domínio mantém:

- consistência entre resumo, detalhe e edição de senha;
- testabilidade da agregação sem dependência de Android;
- menor risco de divergência futura;
- separação entre cálculo de segurança e renderização.

## Alternativas consideradas

### Calcular os buckets diretamente no composable

Foi rejeitado porque colocaria regra de negócio na UI e dificultaria testes determinísticos.

### Criar um scoring específico para a tela

Foi rejeitado porque criaria uma política paralela à `password-strength-security-policy-v1` e poderia gerar divergência entre telas.

### Manter uma quarta aba intermediária

Foi rejeitado porque a tela deve expor somente os três status de segurança do app: `Fracas`, `Moderadas` e `Fortes`. Scores entre 50 e 90 entram no grupo moderado.

## Custos, riscos e impactos

- A tela precisa descriptografar senhas na camada de dados para reaproveitar a análise individual, como o resumo já fazia.
- A UI nunca recebe a senha em claro; recebe apenas metadados, score, tags e status.
- `PasswordRepositoryImpl.observePasswordSecuritySnapshots()` ainda descriptografa todas as senhas a cada emissão, mas o trabalho criptográfico e de fingerprint roda em dispatcher explícito fora da Main Thread. Um ciclo futuro pode avaliar um desenho de snapshot que reduza retenção temporária de senha em claro.
- Alterações futuras nos limiares de agregação devem ser feitas no domínio, não na UI.
- A tela é uma rota interna e não deve renderizar BottomBar.

## Testes esperados

Esta tela deve manter testes para:

- cálculo do índice médio;
- status geral;
- bucketização;
- contadores por bucket;
- seleção de abas;
- estado vazio;
- falha de carregamento;
- integração com a análise individual existente.
