# Crash ao abrir rotas com Hilt após troca de idioma em Ajustes

## Problema

Ao iniciar o app depois da implementação das preferências de Ajustes, o app encerrava na composição das rotas que usam `hiltViewModel()` com o erro:

```text
java.lang.IllegalStateException: Expected an activity context for creating a HiltViewModelFactory but instead found: android.app.ContextImpl
```

O crash foi observado na abertura da tela inicial, mas o problema podia afetar qualquer rota criada dentro do provider global de idioma.

## Causa raiz

O provider global de idioma substituía `LocalContext` por um contexto criado com `Context.createConfigurationContext(...)`.

Esse contexto aplica corretamente os recursos localizados, mas retorna um `ContextImpl` que não mantém uma cadeia de `ContextWrapper` até a `Activity`. O `hiltViewModel()` usado pelo Navigation Compose depende de conseguir obter uma `Activity` a partir do `LocalContext` para criar o `HiltViewModelFactory`. Como a `Activity` deixava de estar acessível, a composição da rota falhava em runtime.

## Correção aplicada

O contexto localizado passou a ser criado com `ContextThemeWrapper`, usando o contexto atual como base e aplicando a configuração de idioma por `applyOverrideConfiguration(...)`.

Com isso:

- `LocalContext` continua fornecendo recursos localizados para `stringResource`;
- a cadeia de contexto preserva a `Activity` para integrações como Hilt Navigation Compose;
- a troca de idioma permanece centralizada no provider raiz de Compose.

## Regra para evitar recorrência

- Providers globais de Compose não devem substituir `LocalContext` por um contexto que perca a cadeia até a `Activity`.
- Quando for necessário aplicar configuração localizada no contexto, usar um wrapper que mantenha o contexto base rastreável.
- Mudanças em providers raiz devem ser validadas contra telas que usam `hiltViewModel()` dentro de `NavHost`.

## Validação esperada

- O app deve abrir a tela inicial sem crash.
- Rotas que usam `hiltViewModel()` devem continuar funcionando dentro do provider de idioma.
- Os recursos localizados devem continuar sendo resolvidos conforme o idioma escolhido.
