# Restauração do onboarding com gate de primeiro uso

## Causa

O app permaneceu com strings e intenção visual de onboarding, mas sem um fluxo funcional de primeiro uso na navegação raiz. Na prática, a splash sempre encaminhava o usuário diretamente para `app`, sem consultar um estado persistido que distinguisse primeira execução de relançamentos posteriores.

## Correção

Foi introduzido um gate de launch baseado em `DataStore`, com `hasCompletedOnboarding` armazenado em `AppPreferences`.

O fluxo passou a funcionar assim:

- `splash` inicia o app e mantém a animação mínima;
- `SplashViewModel` observa `AppPreferences`;
- se `hasCompletedOnboarding == false`, a navegação segue para `onboarding`;
- se `hasCompletedOnboarding == true`, a navegação segue para `app`;
- ao concluir o último passo do onboarding, `CompleteOnboardingUseCase` persiste a flag e a navegação limpa o back stack da raiz.

## Prevenção

Para evitar regressão do mesmo problema:

- o contrato persistido de preferências agora inclui explicitamente o estado de onboarding;
- o gate de launch foi centralizado em um `ViewModel` testável, em vez de uma decisão hardcoded no composable da splash;
- foram adicionados testes unitários para persistência, caso de uso, gate da splash e `OnboardingViewModel`;
- foram adicionados testes instrumentados cobrindo o fluxo visual do onboarding e a limpeza do back stack ao concluir.
