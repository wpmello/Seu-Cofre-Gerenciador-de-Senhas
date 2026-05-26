# Regressão na exclusão individual de senha

## Causa

A tela de edição de senha manteve o botão de exclusão, mas o `EditPasswordViewModel` tratava `EditPasswordAction.OnDeleteClick` como `Unit`. O `git blame` aponta essa linha para `c528486 feat: add edit password screen flow`.

O histórico também contém um `refs/stash` de 2026-05-07 chamado `deleting passwords and categories with associated passwords logic`, onde havia uma implementação parcial da exclusão individual (`deletePasswordById` no DAO/repositório e `DeletePasswordByIdUseCase`). Essa alteração não foi integrada ao `master`. Depois, `826ca3c feat: support deleting categories with associated passwords` reintegrou apenas a exclusão de senhas por categoria, deixando a exclusão individual desconectada.

## Ajuste

A exclusão individual foi reintegrada no fluxo arquitetural completo:

- `PasswordDao.deletePasswordById` remove a linha do Room e retorna a quantidade de linhas afetadas.
- `PasswordsLocalDataSource` e `PasswordRepositoryImpl` expõem a operação sem descriptografar a senha.
- `DeletePasswordByIdUseCase` mapeia sucesso, senha inexistente e falha.
- `EditPasswordViewModel` segue o padrão de exclusão do projeto: o clique inicial abre uma confirmação e a remoção só ocorre após confirmação explícita.
- `EditPasswordUiState` passou a expor `deleteFlowState` e `isDeleting`, e a UI renderiza o diálogo de confirmação antes de desabilitar ações durante a operação.

Como a remoção ocorre diretamente na tabela `passwords`, os `Flow`s já existentes para lista, recentes, busca e resumos passam a refletir a exclusão automaticamente.

## Prevenção

Foram adicionados testes cobrindo:

- uso do `DeletePasswordByIdUseCase`;
- delegação do repositório sem descriptografar dados sensíveis;
- delegação do data source ao DAO;
- comportamento do `EditPasswordViewModel` em sucesso, clique duplicado, falha e senha inexistente;
- confirmação obrigatória antes de executar a exclusão;
- remoção real no DAO instrumentado.
