# Padroes de Design — SportSnap

## Padroes Implementados (6 total — 4 obrigatorios + 2 extras)

### 1. Strategy (Antonio)
**Localizacao:** `sportsnap-gamification-service`

**Problema:** Diferentes esportes devem calcular XP de formas diferentes. Corrida prioriza distancia, musculacao prioriza intensidade.

**Solucao:** Interface `EstrategiaCalculoXp` com implementacoes concretas por esporte.

**Arquivos:**
- `domain/usecases/EstrategiaCalculoXp.java` — interface Strategy
- `application/EstrategiaCalculoXpCorrida.java` — estrategia para corrida
- `application/EstrategiaCalculoXpMusculacao.java` — estrategia para musculacao

```java
// Pattern: Strategy
public interface EstrategiaCalculoXp {
    Double calcularXp(Double distancia, Long duracaoSegundos, Double intensidade);
}
```

---

### 2. Observer (Galileu)
**Localizacao:** `sportsnap-marketplace-service`

**Problema:** Quando um evento acontece no marketplace (foto sugerida, licenca adquirida), multiplos componentes precisam ser notificados sem acoplamento direto.

**Solucao:** Interface `MarketplaceObserver` com publisher que mantem lista de observers.

**Arquivos:**
- `domain/usecases/MarketplaceObserver.java` — interface Observer
- `application/LogNotificacaoObserver.java` — observer concreto (logs)
- `application/MarketplaceEventPublisher.java` — publisher com lista de observers

```java
// Pattern: Observer
public interface MarketplaceObserver {
    void onFotoSugerida(Long atletaId, Long fotoId, String mensagem);
    void onLicencaAdquirida(Long atletaId, Long fotoId, String mensagem);
}
```

---

### 3. Template Method (Marco)
**Localizacao:** `sportsnap-gamification-service`

**Problema:** O processo de sincronizacao da carta tem etapas fixas (validar, transferir XP, recalcular, finalizar) mas a implementacao de cada etapa pode variar.

**Solucao:** Classe abstrata `TemplateSincronizacao` define o esqueleto; `SincronizacaoPadrao` implementa os passos.

**Arquivos:**
- `application/TemplateSincronizacao.java` — classe abstrata com template method
- `application/SincronizacaoPadrao.java` — implementacao concreta

```java
// Pattern: Template Method
public abstract class TemplateSincronizacao {
    public final void sincronizar(Atleta atleta, CartaOficial carta, StatusPotencial status) {
        validar(atleta, status);        // passo 1
        transferirXp(carta, status);    // passo 2
        recalcularOverall(carta);       // passo 3
        finalizarSincronizacao(carta, status); // passo 4
    }
}
```

---

### 4. Decorator (jhrvo0)
**Localizacao:** `sportsnap-marketplace-service`

**Problema:** Fotos precisam passar por processamentos variados (marca d'agua, resize) que podem ser combinados de formas diferentes.

**Solucao:** Interface `ProcessadorFoto` com decoradores que envolvem o processador base.

**Arquivos:**
- `domain/usecases/ProcessadorFoto.java` — interface Component
- `application/ProcessadorFotoBase.java` — componente concreto
- `application/MarcaDaguaDecorator.java` — decorador de marca d'agua
- `application/ResizeDecorator.java` — decorador de resize

```java
// Pattern: Decorator — composicao de processamentos
ProcessadorFoto processador = new ResizeDecorator(
    new MarcaDaguaDecorator(
        new ProcessadorFotoBase()
    )
);
String resultado = processador.processar("foto.jpg");
// resultado: "thumb_watermark_foto.jpg"
```

---

### 5. Iterator (Extra)
**Localizacao:** `sportsnap-gamification-service`

**Problema:** Percorrer o ranking de atletas filtrando apenas cartas sincronizadas, sem expor a estrutura interna da colecao.

**Solucao:** `RankingIterator` implementa `Iterator<CartaOficial>` com logica de negocio (filtra cartas nao reveladas).

**Arquivos:**
- `application/RankingIterator.java` — iterador customizado

```java
// Pattern: Iterator
RankingIterator it = new RankingIterator(cartasOrdenadas);
while (it.hasNext()) {
    CartaOficial carta = it.next();
    System.out.println("#" + it.getPosicaoAtual() + " — Overall: " + carta.getOverall());
}
```

---

### 6. Proxy (Extra)
**Localizacao:** `sportsnap-gamification-service`

**Problema:** Consultas frequentes ao ranking geram carga desnecessaria no banco quando os dados nao mudaram.

**Solucao:** `RankingProxy` intercepta chamadas ao repositorio e retorna dados do cache (TTL 30s).

**Arquivos:**
- `application/RankingProxy.java` — proxy com cache e ConcurrentHashMap

```java
// Pattern: Proxy — cache transparente
List<CartaOficial> ranking = rankingProxy.getRanking(); // cache hit ou miss
rankingProxy.invalidarCache(); // invalida apos sincronizacao
```
