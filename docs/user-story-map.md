# Mapa de Historias do Usuario — SportSnap

## Personas

| Persona | Descricao |
|---|---|
| **Atleta** | Praticante de esporte amador que quer acompanhar sua evolucao e ter fotos profissionais |
| **Fotografo** | Profissional que captura imagens em eventos esportivos e quer monetizar seu trabalho |

---

## User Story Map

### Backbone (Atividades Principais)

```
ATLETA:     Treinar          Comprar Foto       Evoluir Carta       Competir
FOTOGRAFO:  Fotografar       Gerenciar Lote     Receber Pagamento
```

---

## Historias do Usuario — Detalhamento

### Atividade: Treinar

**US01 — Realizar Check-in em Sessao [NT]**
> Como Atleta, quero realizar check-in em uma sessao esportiva ativa para registrar minha presenca e validar minhas atividades.

Criterios de aceitacao:
- CheckIn so e aceito se a Session estiver ativa (horario atual entre inicio e fim)
- CheckIn registra horario, latitude e longitude do Atleta
- CheckIn e vinculado a uma Session existente
- Atleta recebe confirmacao do check-in

**US02 — Registrar Atividade Esportiva [NT]**
> Como Atleta, quero registrar meus dados de performance (distancia, duracao, intensidade) apos um treino para acumular XP nos meus StatusPotencial.

Criterios de aceitacao:
- RegistroDeAtividade e vinculado a um CheckIn existente
- XP e calculado com base na intensidade e duracao
- StatusPotencial (ShadowStats) do Atleta e atualizado
- StreakDeConsistencia e incrementado se treino for consecutivo (dentro de 48h)

---

### Atividade: Fotografar

**US03 — Upload de Fotos em Lote [NT]**
> Como Fotografo, quero fazer upload de multiplas fotos de uma sessao esportiva para disponibiliza-las aos atletas.

Criterios de aceitacao:
- Lote e vinculado a uma Session e um Spot
- Metadados EXIF (timestamp, localizacao) sao extraidos de cada foto
- Preview com marca d'agua e gerado automaticamente
- Upload processa multiplas fotos em paralelo (concorrencia)

**US04 — Gerenciar Lotes e Fotos**
> Como Fotografo, quero visualizar e gerenciar meus lotes de fotos para acompanhar quais foram vendidas.

Criterios de aceitacao:
- Fotografo visualiza lista de seus Lotes
- Cada Lote mostra quantidade de fotos e vendas
- Fotografo pode ver detalhes de cada foto (preview, status de licenca)

---

### Atividade: Comprar Foto

**US05 — Adquirir Licenca de Imagem [NT]**
> Como Atleta, quero comprar a licenca de uma foto onde apareco para destravar a sincronizacao da minha carta.

Criterios de aceitacao:
- Foto so e sugerida se timestamp EXIF estiver dentro do intervalo do CheckIn (RN02)
- LicencaDeImagem e criada com preco e vinculada a Foto e ao Atleta
- SplitFinanceiro e gerado atomicamente: 70% Fotografo, 30% plataforma (RN03)
- Em caso de compra concorrente, JPA Lock garante consistencia

**US06 — Visualizar Fotos Sugeridas**
> Como Atleta, quero ver as fotos que correspondem as minhas sessoes para decidir quais comprar.

Criterios de aceitacao:
- Motor de Match cruza CheckIns com timestamps EXIF
- Apenas fotos com match temporal sao exibidas
- Fotos sao exibidas com marca d'agua (preview)

---

### Atividade: Evoluir Carta

**US07 — Sincronizar Carta do Atleta (Reveal) [NT]**
> Como Atleta, quero sincronizar minha CartaOficial para revelar publicamente meu progresso acumulado.

Criterios de aceitacao:
- Sincronizacao so e permitida com LicencaDeImagem posterior a ultima sincronizacao (RN01)
- StatusPotencial (XP) e transferido para os AtributoEsportivo da CartaOficial
- Overall e recalculado como media ponderada dos atributos
- Data da ultima sincronizacao e atualizada
- Sincronizacao sem licenca valida e rejeitada

**US08 — Calcular Overall e Atualizar Ranking [NT]**
> Como Atleta, quero que meu Overall seja recalculado apos sincronizacao para refletir minha posicao real no ranking.

Criterios de aceitacao:
- Overall = media ponderada de todos os AtributoEsportivo
- Ranking e ordenado por Overall decrescente
- Apenas cartas sincronizadas participam do ranking
- Calculo pode agregar atributos de multiplos esportes concorrentemente

---

## Matriz de Responsabilidade

| Integrante | Funcionalidades |
|---|---|
| **Antonio** | US01 (Check-in) + US02 (Registro Atividade) |
| **Galileu** | US03 (Upload Lote) + US04 (Gerenciar Lotes) |
| **Marco** | US05 (Comprar Licenca) + US06 (Fotos Sugeridas) |
| **jhrvo0** | US07 (Sincronizar Carta) + US08 (Calcular Overall) |

---

## Prioridade de Implementacao

```
Release 1 (1a Entrega — BDD + Dominio):
  US01, US02, US05, US07 — funcionalidades core com cenarios BDD

Release 2 (2a Entrega — Completa):
  US03, US04, US06, US08 — funcionalidades complementares
  + Padroes de design
  + Concorrencia explicita
  + Camada web
```
