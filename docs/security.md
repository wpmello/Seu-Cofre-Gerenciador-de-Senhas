# Segurança

## Objetivo
Definir as regras de segurança obrigatórias do projeto para desenvolvimento, testes, release e manutenção de um app Android nativo de **cofre de senhas** destinado a **produção**.

Neste projeto, segurança não é uma camada opcional nem uma etapa final. Ela deve orientar modelagem, persistência, UX, integração com o Android framework e critérios de aceite.

## Escopo e premissas
Este documento complementa `AGENTS.md`, `docs/architecture.md` e `docs/testing.md`.

O projeto deve ser construído assumindo que:
- o dispositivo pode ser perdido, roubado ou temporariamente acessado por terceiros;
- o armazenamento local pode ser inspecionado;
- logs podem vazar informações sensíveis;
- backups podem expor dados se não houver política explícita;
- telas, clipboard, compartilhamento e arquivos exportados podem se tornar vetores de exposição;
- código ofuscado ajuda na resiliência, mas **não substitui** criptografia correta, controle de acesso, boa arquitetura e testes.

## Princípios obrigatórios
- Nunca armazenar segredos em texto puro.
- Nunca logar senhas, tokens, chaves, IVs, conteúdo descriptografado ou qualquer dado sensível.
- Nunca hardcodar segredos no código, em testes, fixtures, exemplos, documentação ou configuração versionada.
- Nunca confiar em obscuridade como mecanismo principal de proteção.
- Não criar criptografia “caseira”.
- Preferir mecanismos oficiais da plataforma e bibliotecas oficiais/maduras quando realmente necessárias.
- Tratar falhas de segurança com comportamento seguro por padrão.
- Sempre minimizar coleta, retenção, exposição e tempo de vida de dados sensíveis em memória e armazenamento.

## Classificação de dados do projeto
### 1. Dados altamente sensíveis
Exemplos:
- senha principal do cofre
- credenciais salvas pelo usuário
- segredo em texto claro
- material derivado de descriptografia
- tokens de autenticação sensíveis
- chaves criptográficas e IVs quando aplicável

Regras:
- nunca persistir em texto puro;
- nunca enviar a logs;
- nunca expor em previews, screenshots automáticas, mensagens de erro ou analytics;
- armazenar apenas o mínimo necessário e pelo menor tempo possível.

### 2. Dados sensíveis operacionais
Exemplos:
- login/username
- URLs privadas
- notas seguras
- metadados que revelem comportamento do usuário
- timestamps como `createdAt` e `updatedAt` quando associados a credenciais

Regras:
- tratar como privados por padrão;
- evitar exposição desnecessária fora da UI esperada;
- revisar necessidade de backup, compartilhamento e exportação.

### 3. Dados não sensíveis
Exemplos:
- preferência de tema
- idioma
- configurações locais sem impacto de segurança

Regras:
- podem usar DataStore quando apropriado;
- ainda assim devem respeitar privacidade, integridade e consistência.

## Armazenamento local e persistência
### Regras gerais
- Persistência estruturada deve usar **Room**.
- Preferências e configurações não sensíveis devem usar **DataStore**, não `SharedPreferences` novos.
- Dados sensíveis devem permanecer em armazenamento **interno do app**; não usar armazenamento externo para segredos.
- Não usar armazenamento compartilhado, SD card, exportação solta de arquivos ou caminhos acessíveis por outros apps para dados sensíveis.
- Não usar `allowMainThreadQueries()`.
- Toda gravação e leitura deve ser assíncrona, coerente com `Coroutines + Flow`.

### Política para Room
- O banco local deve armazenar apenas o que for necessário para o funcionamento do produto.
- Campos sensíveis persistidos devem estar protegidos antes de chegar ao banco.
- Entidades e migrações devem ser revisadas com foco em minimização de dados.
- Schema exportado para testes e versionamento **não deve** conter segredos reais.

### Política para DataStore
- Usar DataStore apenas para preferências/configurações que não exijam proteção equivalente à do cofre.
- Não usar DataStore como atalho para guardar segredos.
- Separar claramente preferências de UX de dados do cofre.

## Criptografia e gerenciamento de chaves
### Regras obrigatórias
- O projeto deve usar as APIs oficiais de criptografia do Android.
- Chaves criptográficas devem ser gerenciadas com **Android Keystore** quando aplicável.
- O material de chave deve ser tratado como **não exportável** sempre que o caso permitir.
- Restrições de uso de chave devem ser configuradas de forma deliberada.
- Algoritmos, modos e parâmetros devem seguir as capacidades e recomendações oficiais da plataforma.
- Não duplicar ou reinventar a responsabilidade do Keystore em código próprio.

### Diretrizes práticas
- Separar a lógica de criptografia em componentes próprios, testáveis e isolados da UI.
- A criptografia deve acontecer **antes** da persistência do dado sensível.
- A descriptografia deve acontecer apenas no ponto de uso e pelo menor tempo possível.
- Conteúdo descriptografado não deve permanecer mais tempo que o necessário em memória, estado de UI ou caches.
- Erros de criptografia e descriptografia devem ser tratados sem revelar detalhes sensíveis ao usuário.

## Autenticação local e biometria
### Regras obrigatórias
- Leitura e revelação de dados altamente sensíveis devem considerar proteção adicional com autenticação local quando o fluxo exigir isso.
- Usar a integração oficial de biometria do Android.
- O app não deve confundir autenticação do usuário com criptografia em si; são responsabilidades relacionadas, mas distintas.
- Falhas, cancelamentos e indisponibilidade de biometria devem ter tratamento seguro e previsível.

### Diretriz do projeto
- Ações como **revelar senha**, **copiar senha** ou **abrir detalhe sensível desbloqueado** devem ser tratadas como operações de alto risco.
- O projeto deve definir explicitamente quais ações exigem autenticação local e quais só exigem proteção criptográfica em repouso.

## UI, privacidade visual e interação com segredos
### Regras obrigatórias
- Dados sensíveis não devem aparecer por padrão em texto claro sem necessidade funcional.
- A UI deve privilegiar estado mascarado para segredos até que a ação apropriada aconteça.
- Previews, mocks, screenshots de documentação, seeds e dados de teste não devem conter credenciais reais.
- O projeto deve avaliar e, para telas sensíveis, preferir proteção contra captura usando `FLAG_SECURE` ou mecanismo equivalente apropriado, documentando a decisão e seus trade-offs.
- Não mostrar erros técnicos crus ou detalhes de infraestrutura ao usuário final.

### Clipboard
- Copiar segredo para a área de transferência deve ser tratado como operação sensível.
- Quando o app copiar conteúdo sensível, marcar o clip como sensível usando a flag oficial apropriada.
- O fluxo deve considerar redução do risco de exposição acidental e comunicar a ação de forma clara ao usuário.

## Compartilhamento, exportação e arquivos
### Regras obrigatórias
- Não compartilhar segredos com outros apps por padrão.
- Exportação/importação de dados do cofre só pode existir com desenho explícito, justificativa forte, proteção adequada e documentação correspondente.
- Quando for necessário compartilhar arquivos, usar **FileProvider** e `content://`, nunca `file://` exposto diretamente.
- Não gravar arquivos sensíveis em armazenamento externo para conveniência.

### Política conservadora do projeto
- Enquanto não existir especificação formal de exportação/importação segura, considerar esse fluxo **proibido**.
- Se o fluxo existir no futuro, ele deve ganhar ADR, cobertura de testes e revisão de segurança dedicadas.

## Backup e restore
### Regras obrigatórias
- O projeto deve ter política **explícita** para backup; não depender de default sem revisão.
- Dados do cofre, bancos, arquivos exportados e outros artefatos sensíveis devem ser avaliados explicitamente quanto a inclusão ou exclusão de backup.
- O objetivo deve ser evitar vazamento acidental de segredos por mecanismos de backup/restauração.

### Política inicial recomendada
- Adotar postura conservadora: excluir do backup automático tudo que contenha ou possa reconstruir segredos, salvo se existir desenho formal de backup seguro aprovado para o produto.
- Toda mudança nessa política deve ser documentada com justificativa técnica e teste correspondente.

## Rede e comunicação externa
Se o projeto usar backend, sync, analytics, crash reporting ou qualquer integração remota:
- usar HTTPS/TLS corretamente;
- configurar **Network Security Configuration** quando necessário;
- não transmitir dado sensível sem necessidade clara;
- preferir processamento local quando isso reduzir exposição de dados sensíveis;
- não enviar segredos em texto puro por parâmetros, logs de rede ou mensagens de erro.

## Logs, debug e observabilidade
### Regras obrigatórias
- Logs de produção não devem conter dados sensíveis.
- Logs de debug também não devem conter segredos; mascaramento é o mínimo, não a primeira escolha.
- Não deixar `println`, `Log.d`, dumps temporários ou interceptadores verbosos expondo payloads sensíveis.
- Ferramentas de observabilidade, crash e analytics devem ser configuradas com revisão explícita de privacidade.
- Stack traces e mensagens devem ser suficientes para diagnóstico sem expor conteúdo sensível.

## Dependências, permissões e configuração do app
### Dependências
- Preferir solução nativa/oficial quando a plataforma já cobre o problema adequadamente.
- Não adicionar biblioteca “de segurança” sem justificar por que o Android framework/Jetpack não resolve o caso.
- Toda nova dependência com impacto em segurança deve ser registrada na documentação quando a decisão for relevante.

### Permissões
- Solicitar apenas permissões estritamente necessárias.
- Justificar qualquer permissão potencialmente sensível.
- Não pedir acesso amplo a arquivos/dispositivo sem necessidade real de produto.

### Manifest e configuração
- Revisar componentes exportados, receivers, providers e atividades sensíveis.
- Aplicar configuração de backup e rede de forma explícita.
- Configurar release com **R8** e manter regras de keep sob controle quando houver reflection/serialização/geração de código.

## Testes e validação obrigatórios
Toda mudança em segurança deve ter validação proporcional ao risco.

### Cobertura mínima esperada
- criptografia com sucesso;
- falha de criptografia;
- descriptografia com sucesso;
- falha de descriptografia;
- recusa/cancelamento de autenticação;
- persistência segura;
- ausência de persistência em texto puro;
- tratamento seguro de erro;
- comportamento seguro em UI para dados sensíveis;
- regressão para bugs de segurança corrigidos.

### Revisão obrigatória para mudanças nestas áreas
- criptografia
- biometria/autenticação local
- Room e schema do cofre
- backup/restore
- exportação/importação
- clipboard
- compartilhamento de arquivos
- logs/analytics/crash reporting
- rede e sync
- configuração de manifest e componentes exportados

## Quando documentar
Criar ou atualizar documentação em `docs/` ou `docs/adr/` quando houver:
- mudança de estratégia criptográfica;
- nova política de autenticação local;
- introdução de exportação/importação/backup de dados sensíveis;
- adição ou remoção de biblioteca com impacto em segurança;
- mudança relevante em política de logs, analytics, crash ou rede;
- decisão arquitetural de segurança que afete várias features.

Abrir ADR quando a decisão:
- tiver trade-offs relevantes;
- for difícil de reverter;
- afetar arquitetura, operação ou conformidade do app;
- criar custo futuro de manutenção ou migração.

## Definition of Done para mudanças sensíveis
Uma alteração com impacto em segurança só pode ser considerada pronta quando:
- a arquitetura continua íntegra;
- não há regressão funcional ou de segurança conhecida;
- testes relevantes foram criados/atualizados e estão verdes;
- build apropriado continua funcionando;
- logs, mensagens, previews e dados de teste não expõem segredo;
- política de backup/exportação/compartilhamento continua coerente;
- documentação foi atualizada quando necessário;
- não houve introdução de gambiarra ou fallback inseguro.

## Referências oficiais
As páginas abaixo são as fontes primárias recomendadas para este projeto. Manter links úteis no documento é intencional, para facilitar consulta do agente e revisão humana.

- Android Security Best Practices: https://developer.android.com/privacy-and-security/security-best-practices
- Android Security Checklist / Security Tips: https://developer.android.com/privacy-and-security/security-tips
- Mitigate Security Risks in Your App: https://developer.android.com/privacy-and-security/risks
- Android Keystore System: https://developer.android.com/privacy-and-security/keystore
- Cryptography on Android: https://developer.android.com/privacy-and-security/cryptography
- Biometric Authentication: https://developer.android.com/identity/sign-in/biometric-auth
- Network Security Configuration: https://developer.android.com/privacy-and-security/security-config
- Secure Network Protocols: https://developer.android.com/privacy-and-security/security-ssl
- Backup Best Practices: https://developer.android.com/privacy-and-security/risks/backup-best-practices
- Auto Backup for Apps: https://developer.android.com/identity/data/autobackup
- Data Backup Overview: https://developer.android.com/identity/data/backup
- Sensitive Data Stored in External Storage: https://developer.android.com/privacy-and-security/risks/sensitive-data-external-storage
- Secure Clipboard Handling: https://developer.android.com/privacy-and-security/risks/secure-clipboard-handling
- Secure File Sharing / FileProvider: https://developer.android.com/training/secure-file-sharing
- FileProvider reference: https://developer.android.com/reference/androidx/core/content/FileProvider
- Detect screenshots / `FLAG_SECURE`: https://developer.android.com/about/versions/14/features/screenshot-detection
- Data storage overview: https://developer.android.com/training/data-storage
- App-specific storage: https://developer.android.com/training/data-storage/app-specific
- DataStore overview: https://developer.android.com/topic/libraries/architecture/datastore
