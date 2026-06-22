# DataStore Preferences Test File Rename Failure

## Causa

`DataStoreAppPreferencesRepositoryTest` validava três chamadas sequenciais de `DataStore.edit` no mesmo arquivo dentro de um teste JVM.

No Windows, a versão usada do DataStore falhou ao renomear `settings.preferences_pb.tmp` para `settings.preferences_pb` após o arquivo de destino já existir. A mensagem sugere múltiplas instâncias de DataStore para o mesmo arquivo, mas o teste criava um único DataStore; o problema estava no padrão do teste JVM exigir múltiplos overwrites do mesmo arquivo temporário.

## Ajuste

O teste passou a criar um `TestScope` dedicado para cada instância de DataStore usando `UnconfinedTestDispatcher`.

Esses escopos são registrados e cancelados no `tearDown`, garantindo que:

- cada teste tenha um DataStore isolado;
- cada DataStore use arquivo temporário próprio;
- o actor interno do DataStore seja encerrado ao final do teste;
- writes sequenciais não dependam do `backgroundScope` do `runTest`.

O cenário que fazia três writes no mesmo arquivo foi dividido em cenários independentes:

- atualização de nome;
- atualização de idioma;
- atualização de tema.

Isso preserva a cobertura do contrato público do repositório sem depender de múltiplos overwrites do mesmo arquivo temporário no JVM Windows.

## Como investigar se voltar a ocorrer

Verificar se algum teste:

- cria mais de uma instância de DataStore apontando para o mesmo arquivo;
- reutiliza o mesmo arquivo temporário entre métodos;
- deixa escopos de DataStore ativos após o fim do teste;
- executa writes concorrentes sem escopo controlado.

Rodar primeiro:

```powershell
.\gradlew.bat testDebugUnitTest --tests *DataStoreAppPreferencesRepositoryTest
```

Depois validar a suíte:

```powershell
.\gradlew.bat testDebugUnitTest
```
