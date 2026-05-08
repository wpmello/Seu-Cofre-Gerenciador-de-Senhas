# Regressão: status de segurança incorreto nas senhas recentes da Home

## Contexto

A lista de senhas recentes da `HomeScreen` exibiu indicador vermelho e flag de senha fraca para itens que não eram fracos segundo a política vigente de força de senha.

## Causa raiz

A Home consumia `PasswordRepository.observeRecentPasswords(...)` diretamente. Essa consulta retorna `PasswordSummary` sem executar a análise de segurança, então o campo `securityRiskLevel` permanecia com o fallback seguro por padrão do modelo: `PasswordSecurityRiskLevel.High`.

Como o `VaultHomeViewModel` mapeia `High` para `VaultPasswordListSecurityLevel.Weak`, qualquer senha recente sem risco calculado chegava à UI como fraca, mesmo quando o snapshot real da credencial produziria `Low` ou `Medium`.

## Correção

Foi criado `ObserveRecentPasswordsUseCase` para combinar:

- a lista real de senhas recentes do repositório;
- os snapshots reais de segurança das credenciais;
- a mesma política de avaliação usada pelas telas de senhas e segurança.

A `ObserveVaultHomeUseCase` passou a consumir esse caso de uso em vez de usar a lista recente crua do repositório.

## Prevenção

Foram adicionadas regressões cobrindo:

- senha recente forte exibida com risco `Low`, não `High`;
- transformação do estado da Home para `VaultPasswordListSecurityLevel.Safe`;
- UI da Home sem flag `FRACA` quando o item recente é seguro.

Ao adicionar novos pontos de exibição de status de senha, a regra é não usar diretamente o fallback de `PasswordSummary.securityRiskLevel`; o status deve vir da política de segurança por meio dos use cases do domínio.
