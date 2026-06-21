# Autenticação local nas telas de edição

## Decisão

As rotas de edição de senha e categoria exigem autenticação local antes de carregar qualquer dado persistido.

O desbloqueio usa o AndroidX Biometric com `BIOMETRIC_STRONG | DEVICE_CREDENTIAL`. Biometria forte e credencial segura do dispositivo são aceitas como fatores equivalentes para esta liberação local.

## Comportamento esperado

Ao entrar em `EditPasswordScreen` ou `EditCategoryScreen`, o `ViewModel` inicia em estado bloqueado e solicita autenticação local por efeito efêmero da rota. Enquanto a autenticação não retorna sucesso:

* nenhum use case de busca da senha ou categoria é executado;
* observers de categorias e senhas associadas não são iniciados;
* ações de edição, cópia, salvamento, exclusão e transferência são ignoradas;
* a UI mostra somente o estado protegido, com voltar e retry explícito.

Se o usuário cancelar, a autenticação falhar ou o dispositivo não possuir bloqueio seguro disponível, os dados permanecem bloqueados.

## Alternativas consideradas

Carregar dados antes do prompt e apenas ocultar a UI foi rejeitado porque manteria dados sensíveis em estado renderizável antes da autenticação.

Manter desbloqueio por sessão foi rejeitado nesta entrega para reduzir escopo e garantir que cada nova entrada em tela de edição exija confirmação local.

## Impactos

`MainActivity` herda de `FragmentActivity` para que o `BiometricPrompt` seja gerenciado com lifecycle adequado. A política não substitui criptografia em repouso nem validações de domínio; ela apenas impede carregamento e exibição das telas de edição sem autenticação local.
