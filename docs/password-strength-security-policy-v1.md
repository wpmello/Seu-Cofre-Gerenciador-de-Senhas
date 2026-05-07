# Política de Força e Segurança de Senhas v1 (Fonte de Verdade)

> Status: **Rascunho adotado para o projeto**
>
> Objetivo: definir as **regras baseadas em pesquisa** que vão orientar a UI de segurança de senhas do app, o índice de força, as tags e a implementação futura.  
> Escopo: este documento se aplica às **credenciais armazenadas dentro do gerenciador de senhas**, e não à senha de login/autenticação do próprio app.

---

## 1. Resumo executivo

Este projeto tratará a segurança de senhas como uma **estimativa de risco**, e não como uma garantia absoluta.

**Não existe “senha 100% segura” em sentido absoluto**. Uma senha pode ser forte contra tentativas de adivinhação e ainda assim ser exposta por phishing, malware, comprometimento do dispositivo ou violação no serviço de terceiros. Para este app, **100% significa “nota máxima dentro da política atual do aplicativo”**, e não “impossível de comprometer”.

Portanto, o app deve avaliar a segurança da senha usando uma combinação de:

1. **Adivinhabilidade** (o quão fácil é adivinhar a senha com padrões comuns de ataque)
2. **Detecção de senha conhecida como ruim / vazada**
3. **Detecção de duplicidade/reutilização dentro do cofre do usuário**
4. **Penalidades de contexto** (a senha contém nome do serviço, email ou termos pessoais/específicos do app)
5. **Feedback ao usuário** (tags + mensagem explicativa + índice claro)

---

## 2. Conclusões da pesquisa

### 2.1 O que conta como senha forte

As diretrizes modernas não recomendam mais depender principalmente de regras antigas de “complexidade” como “deve conter maiúscula + número + símbolo”. O NIST diz que senhas devem permitir todos os caracteres ASCII imprimíveis, espaços e Unicode; não devem impor regras extras de composição; devem ser verificadas contra uma blocklist de senhas comumente usadas, esperadas ou comprometidas; e devem ser comparadas por inteiro, sem truncamento. Gerenciadores de senha e colagem/autopreenchimento devem ser permitidos, porque ajudam os usuários a escolher senhas melhores.[^nist63b]

A OWASP também recomenda permitir senhas longas (com pelo menos 64 caracteres como limite máximo), aceitar todos os caracteres, evitar trocas periódicas arbitrárias, adicionar um medidor de força e bloquear senhas comuns ou previamente vazadas.[^owasp-auth]

**Conclusão para este app:** uma senha “forte” é, principalmente:
- longa o suficiente,
- difícil de adivinhar,
- única,
- ausente de bases conhecidas de vazamentos,
- não obviamente relacionada ao serviço/app/dados do usuário,
- e não reutilizada em múltiplas credenciais armazenadas.

### 2.2 O que conta como senha fraca ou insegura

Uma senha deve ser tratada como fraca/insegura se qualquer uma das condições abaixo for verdadeira:

- ela é **curta**,
- ela é **comum**,
- ela aparece em uma **base de vazamentos**,
- ela segue **padrões óbvios** (sequências de teclado, datas, repetições, sequências simples),
- ela inclui **palavras de contexto** como nome do app/serviço, email, nome de usuário ou dados pessoais fáceis,
- ou ela é **reutilizada/duplicada** em múltiplos itens do cofre.

Isso está alinhado com a orientação de blocklist do NIST e com estimadores de força como o zxcvbn, que penaliza explicitamente senhas comuns, nomes, palavras de dicionário, datas, repetições, sequências, padrões de teclado e variantes em l33t.[^nist63b][^zxcvbn]

### 2.3 Como uma senha deve ser avaliada no app

A OWASP recomenda explicitamente o uso de um **medidor de força de senha** e cita zxcvbn/zxcvbn-ts como opções adequadas.[^owasp-auth]

O projeto oficial do zxcvbn estima o quão adivinhável é uma senha e retorna:
- um número estimado de tentativas,
- uma nota de força de **0 a 4**,
- feedback direcionado,
- e penalidades opcionais com base em **user_inputs** como nome, email ou palavras específicas do site.[^zxcvbn]

**Conclusão para este app:** a melhor abordagem v1 é:
- usar um **estimador de adivinhabilidade no estilo zxcvbn** como nota base,
- depois aplicar regras do projeto para:
  - senha vazada/comum,
  - senha duplicada dentro do cofre,
  - penalidades de contexto,
  - e o mapeamento para a porcentagem/índice mostrado na UI do app.

### 2.4 Verificação de senha vazada

O NIST exige verificar senhas candidatas contra uma blocklist de senhas conhecidamente comuns, esperadas ou comprometidas.[^nist63b]

A OWASP aponta o **Pwned Passwords** como referência para verificar senhas contra bases de senhas previamente vazadas.[^owasp-auth] O Have I Been Pwned informa que, se uma senha apareceu em vazamentos de dados, ela **nunca deve ser usada**; também alerta que “não encontrada” **não** significa automaticamente que a senha é boa.[^hibp-passwords]

**Conclusão para este app:**
- a v1 deve suportar o conceito de **detecção de senha conhecida como ruim**.
- “Não vazada” **não** deve significar automaticamente “forte”.
- Um acerto em vazamento/blocklist deve impor uma penalidade forte no índice de segurança.

### 2.5 Senhas duplicadas

Senhas duplicadas/reutilizadas não são aceitáveis em uma experiência de gerenciador de senhas, porque uma violação em um serviço pode se transformar em tomada de conta em outros serviços. O Have I Been Pwned alerta explicitamente que a reutilização é comum e aumenta o risco por credential stuffing.[^hibp-passwords]

**Conclusão para este app:** a detecção de duplicidade é uma regra do produto e deve ser exposta claramente por meio de tag(s), mesmo quando a senha for longa ou parecer complexa.

### 2.6 Modelo de armazenamento para este projeto

Para sistemas de autenticação, a OWASP diz que senhas normalmente devem ser armazenadas com hash, não com criptografia reversível. No entanto, a própria OWASP observa que criptografia é a escolha correta no caso especial em que o sistema precisa recuperar a senha em texto puro para usá-la com outro sistema.[^owasp-storage]

**Conclusão para este app:** como este projeto é um **cofre de senhas** e o app precisa ser capaz de revelar/copiar a credencial original de volta para o usuário, o cofre deve usar o modelo de **criptografia reversível em repouso** definido pelo projeto, e não hash irreversível.  
Isso **não** muda a lógica de força; muda apenas a forma de armazenamento do segredo.

---

## 3. Definições que servem como fonte de verdade

### 3.1 Definições

- **Senha forte**
  Uma senha suficientemente difícil de adivinhar segundo as regras atuais do app, que não está duplicada no cofre e não é conhecida como comprometida nem obviamente baseada em contexto.

- **Senha fraca**  
  Uma senha curta, comum, previsível, bloqueada por blocklist, vazada ou fácil demais de adivinhar segundo o estimador e a política do projeto.

- **Senha duplicada**  
  O mesmo segredo em texto claro normalizado, reutilizado por pelo menos duas credenciais armazenadas no cofre do usuário.

- **Índice de segurança**  
  Uma **porcentagem específica do projeto** derivada do estimador + penalidades da política.  
  Não é uma verdade universal e **não** é garantia absoluta. É uma pontuação normalizada usada pela UI deste app.

---

## 4. Decisões de política para este app (v1)

Estas são as **decisões do projeto** que o Codex deve seguir, a menos que este documento seja revisado de propósito.

### 4.1 Modelo de força

Usar um **modelo em duas etapas**:

#### Etapa A - Estimador base
Usar localmente um estimador no estilo zxcvbn para analisar a senha em texto puro e produzir uma nota base.

A entrada do estimador deve incluir strings de contexto quando disponíveis, por exemplo:
- nome do app/serviço,
- email,
- nome de usuário/parte local do email,
- nome da categoria (se relevante),
- strings óbvias específicas do aplicativo.

Isso segue o comportamento documentado de `user_inputs` do zxcvbn.[^zxcvbn]

#### Etapa B - Modificadores de política
Depois que o estimador retornar uma nota base, aplicar os modificadores do projeto.

---

## 5. Política do app v1 - regras de pontuação

### 5.1 Mapeamento da nota base

Mapear a nota base do estimador (0-4) para uma porcentagem inicial:

- nota **0** -> **15%**
- nota **1** -> **35%**
- nota **2** -> **60%**
- nota **3** -> **80%**
- nota **4** -> **95%**

Justificativa:
- preserva o significado do estimador, de “muito fraca” a “muito forte”,
- mantém as faixas da UI do app simples,
- reserva 91-100 para senhas realmente fortes dentro da política do projeto.

### 5.2 Penalidades obrigatórias

Aplicar as seguintes penalidades/tetos após o mapeamento base.

#### A. Senha vazia ou em branco
- nota final = **0**
- status = **risco alto**
- tags = `Senha fraca`

#### B. Senha conhecida como ruim / vazada / bloqueada por blocklist
Se a senha for encontrada em lista de senhas vazadas ou na blocklist do projeto:
- nota final = **min(atual, 25)**
- tags incluem `Senha fraca`
- o status não pode ser verde

#### C. Senha muito curta
- comprimento < 8 -> nota final = **min(atual, 20)**
- comprimento 8-11 -> nota final = **min(atual, 45)**

#### D. Senha sensível a contexto
Se a senha incluir contexto óbvio como:
- nome do app/serviço,
- parte local do email,
- nome de usuário,
- nome da categoria,
- token pessoal comum já disponível no formulário,
então:
- nota final = **min(atual, 45)**

#### E. Senha duplicada dentro do cofre
Se a mesma senha já estiver sendo usada por outra credencial:
- nota final = **min(atual, 80)**
- tags incluem `Senha duplicada`

> Observação: duplicidade sozinha não precisa obrigatoriamente deixar o status vermelho, mas impede que a senha seja tratada como “limpa/ideal”.

### 5.3 Bônus positivo opcional

Se todas as condições abaixo forem verdadeiras:
- nota do estimador é 4,
- comprimento da senha >= 16,
- não está duplicada,
- não está vazada/bloqueada,
- não recebeu penalidade de contexto,
então:
- a nota final pode ser elevada para **100**

**Importante:** neste projeto, **100 significa “melhor nota dentro da política do app”**, e não “segurança absoluta”.

---

## 6. Faixas de UI e comportamento

### 6.1 Faixas do índice

O app deve usar estas faixas de UI:

- **0% a 49%** -> **vermelho** -> **risco alto**
- **50% a 90%** -> **amarelo/âmbar** -> **risco médio / moderada**
- **91% a 100%** -> **verde** -> **excelente / senha forte**

### 6.2 Área de cabeçalho/status

#### Risco alto (0-49)
- cor: **vermelha**
- ícone: **atenção/alerta**
- título: **Risco de Segurança**

#### Risco médio / moderada (50-90)
- cor: **amarela/âmbar**
- ícone: **atenção/alerta**
- título: **Risco Moderado**

#### Excelente / senha forte (91-100)
- cor: **verde**
- ícone: **bolinha verde**
- título: **Senha Forte**

### 6.3 Área de tags

As tags são derivadas dos resultados da política.

#### Mostrar `Senha fraca`
Mostrar esta tag se qualquer uma das condições abaixo for verdadeira:
- nota final <= 49
- acerto em blocklist/vazamento
- nota do estimador <= 1
- comprimento < 8

#### Mostrar `Senha duplicada`
Mostrar esta tag se a senha estiver reutilizada por outro item do cofre.

#### Mostrar `Senha forte`
Mostrar esta tag **somente se**:
- `Senha fraca` estiver ausente
- `Senha duplicada` estiver ausente
- nota final >= 91

#### Combinações de tags
Combinações permitidas:
- `Senha fraca`
- `Senha duplicada`
- `Senha fraca` + `Senha duplicada`

Não permitido:
- `Senha forte` junto com qualquer tag de alerta

### 6.4 Área de texto informativo

#### Risco alto
Manter o texto atual em estilo de alerta, na cor vermelha.

Direção recomendada de conteúdo:
- senhas curtas / comuns / reutilizadas são arriscadas,
- atacantes frequentemente exploram senhas reutilizadas ou fáceis de adivinhar,
- recomendar a criação de uma senha mais longa e única.

#### Risco médio
Manter essencialmente a mesma orientação, mas com estilo amarelo/âmbar.

Direção recomendada de conteúdo:
- a senha é melhor, mas ainda pode ser aprimorada,
- recomendar aumento de comprimento e unicidade,
- recomendar evitar padrões reutilizados ou específicos do serviço.

#### Segura
Exibir orientação positiva em verde.

Direção recomendada de conteúdo:
- a senha é forte segundo as regras atuais do app,
- ela aparenta ser longa/única o suficiente e não mostra os alertas atuais,
- ainda assim, lembrar o usuário de que nenhuma senha é isenta de risco e de que a unicidade continua sendo importante.

---

## 7. O que este app deve considerar “seguro o suficiente” na v1

Uma senha deve cair na faixa verde, em geral, apenas quando for:

- não vazia,
- não bloqueada / não vazada,
- não duplicada dentro do cofre,
- não obviamente baseada em termos do app/serviço/email/usuário,
- longa o suficiente (preferencialmente **15+ caracteres** para senhas escolhidas pelo usuário),
- e forte o suficiente segundo o estimador.

Isso segue a direção geral das recomendações de NIST/OWASP, ainda que usando uma porcentagem específica de produto para a UI.[^nist63b][^owasp-auth]

---

## 8. O que este app deve considerar “inseguro” na v1

Uma senha deve ser tratada como insegura se ela for qualquer uma das seguintes:

- encontrada em bases de vazamentos,
- extremamente curta,
- comum ou baseada em dicionário,
- padrão óbvio (`123456`, `qwerty`, `abcd`, datas, repetições),
- baseada em contexto do app/serviço/email/usuário,
- ou reutilizada em outro lugar do cofre.

---

## 9. Observações de produto para implementação

Estas são restrições de implementação do projeto derivadas da pesquisa e da arquitetura atual do app.

### 9.1 A nota de força é uma estimativa, não uma promessa
A UI não deve comunicar que “100% seguro” significa “impossível de quebrar”.  
A nota é uma **pontuação de política**, não uma garantia.

### 9.2 A detecção de duplicidade é local
A detecção de duplicidade é uma **regra local do cofre**.  
Ela deve comparar as credenciais armazenadas dentro do cofre do usuário e marcar reutilização de acordo com isso.

### 9.3 Verificação de vazamentos
O produto poderá futuramente suportar:
- uma blocklist offline/local, ou
- uma verificação externa de vazamentos com preservação de privacidade.

Se um serviço externo vier a ser adicionado no futuro, isso deve ser feito de forma deliberada e documentada.  
Por enquanto, a fonte de verdade é apenas que uma **senha conhecida como ruim deve receber penalidade forte**.

### 9.4 Não regredir a segurança de armazenamento
Como este app é um cofre, as credenciais armazenadas devem permanecer sob o desenho de criptografia em repouso definido no projeto.  
Não substituir o armazenamento das credenciais do cofre por hash irreversível, porque o app precisa revelar/copiar a senha de volta ao usuário.[^owasp-storage]

---

## 10. Não objetivos da v1

Os itens abaixo **não** fazem parte da implementação atual da política de força:

- timeline de histórico da senha,
- UX de geração automática de senha,
- integração em tempo real com API de vazamentos,
- fluxos de edição sensíveis a categoria,
- pontuação de MFA/passkey,
- pontuação de resistência a phishing,
- pontuação de risco baseada em notas,
- motores corporativos de política.

---

## 11. Regras finais de fonte de verdade para o Codex

Ao implementar a lógica de força de senha deste projeto, o Codex deve seguir estas regras:

1. Tratar a nota como uma **estimativa de adivinhabilidade/risco**, e não como verdade absoluta.
2. Usar um **estimador no estilo zxcvbn** como base.
3. Aplicar penalidades do projeto para:
   - senha vazada/comum,
   - senha duplicada,
   - comprimento curto,
   - senha específica de contexto.
4. Mapear o resultado final nas três faixas de UI do app:
   - 0-49 vermelho,
   - 50-90 amarelo,
   - 91-100 verde.
5. As tags devem funcionar exatamente assim:
   - mostrar `Senha fraca` quando a senha for fraca,
   - mostrar `Senha duplicada` quando a senha estiver reutilizada,
   - mostrar `Senha forte` somente quando não houver nenhuma tag de alerta.
6. “100%” significa **melhor nota dentro da política atual do app**, e não “absolutamente segura”.
7. Manter o armazenamento do cofre alinhado ao modelo de criptografia do projeto.
8. Não inventar regras extras, a menos que este arquivo seja atualizado.

---

## 12. Referências

[^nist63b]: NIST SP 800-63B / 63-4 password guidance: https://pages.nist.gov/800-63-4/sp800-63b.html
[^owasp-auth]: OWASP Authentication Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
[^owasp-storage]: OWASP Password Storage Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
[^hibp-passwords]: Have I Been Pwned — Pwned Passwords: https://haveibeenpwned.com/Passwords
[^zxcvbn]: Dropbox zxcvbn: https://github.com/dropbox/zxcvbn
