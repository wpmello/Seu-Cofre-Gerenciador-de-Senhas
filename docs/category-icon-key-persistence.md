# Persistência de Ícones de Categoria por `iconKey`

## O que foi decidido

As categorias persistem apenas uma chave estável de ícone no banco, em `iconKey`, em vez de salvar:

- inteiro de `R.drawable`
- `Drawable`
- `Painter`
- `Bitmap`
- qualquer referência Android framework na camada de domínio

O catálogo de ícones disponíveis é fixo, controlado pelo app e exposto por um mapeamento explícito:

- `iconKey`: identidade estável persistida
- `drawableResId`: referência visual usada apenas na UI/presentation

## Por que foi decidido

Salvar `R.drawable` como inteiro no banco é frágil porque:

- ids de recurso não são contrato estável de persistência
- o domínio passaria a depender de detalhe Android
- renomeações, merges de recursos ou reorganização de drawables podem quebrar dados persistidos
- fica mais difícil tratar legado e fallback seguro

Persistir `iconKey` resolve isso porque:

- mantém o domínio independente do framework
- torna o banco mais estável diante de mudanças visuais
- permite fallback explícito quando um ícone antigo deixa de existir
- facilita testes de mapeamento e migração

## Modelagem aplicada

### Banco / data

`CategoryEntity` passou a persistir:

- `id`
- `name`
- `iconKey`
- `itemCount`

Foi criada a migration `1 -> 2` adicionando `icon_key` com valor padrão seguro:

- `ic_directory`

### Domínio

`Category` passou a carregar:

- `id`
- `name`
- `iconKey`
- `itemCount`

### Presentation

A UI resolve o `iconKey` para o drawable real via catálogo fixo:

- `CategoryIconCatalog`
- `DefaultCategoryIconCatalog`

Esse catálogo é a fonte de verdade dos ícones permitidos para:

- seleção na `NewCategoryScreen`
- renderização na `CategoriesScreen`

## Regra operacional

Para futuras telas e fluxos de categoria:

- nunca persistir ids de drawable
- nunca persistir objetos gráficos
- sempre persistir apenas `iconKey`
- sempre resolver `iconKey` para drawable por mapeamento explícito e controlado
- manter fallback seguro para chaves desconhecidas ou legadas

## Fallback seguro

Quando um `iconKey` não for encontrado no catálogo atual, a UI deve usar o ícone padrão:

- `ic_directory`

Esse fallback deve evitar crash e preservar a renderização da lista.

## Alternativas consideradas

### Salvar `R.drawable` como inteiro

Rejeitada porque cria acoplamento com recurso Android e não oferece estabilidade de persistência.

### Resolver drawable por nome com API genérica

Rejeitada como abordagem principal porque aumenta fragilidade, reduz previsibilidade e dificulta refactor seguro.

### Salvar URL, path ou payload visual

Rejeitada porque a lista de ícones é fixa e controlada pelo app; isso introduziria complexidade sem benefício real.

## Impactos

- a criação de categoria continua desacoplada do framework Android na domain layer
- a `CategoriesScreen` reflete automaticamente o ícone correto ao observar a lista real do banco
- migrations precisam preservar `iconKey`
- testes precisam cobrir:
  - persistência correta de `iconKey`
  - fallback de chave desconhecida
  - renderização coerente na presentation
