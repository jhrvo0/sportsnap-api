# Concorrencia — SportSnap

## 1. Funcionalidades com Concorrencia Explicita

### 1.1 Upload e Indexacao em Lote (Marketplace)
**Classe:** `UploadIndexacaoEmLoteImpl.java`
**Mecanismo:** `ExecutorService` com pool de 4 threads

**Descricao:** Quando um fotografo faz upload de multiplas fotos, a extracao de metadados EXIF de cada foto e feita em paralelo. Cada thread processa uma foto independentemente.

**Regiao Critica:** A lista `fotosProcessadas` e compartilhada entre as threads. Protegida com `Collections.synchronizedList()`.

**Mecanismo de protecao:** Lista sincronizada + `Future.get()` para aguardar conclusao.

```java
List<Foto> fotosProcessadas = Collections.synchronizedList(new ArrayList<>());
ExecutorService executor = Executors.newFixedThreadPool(4);
// Cada thread processa uma foto e adiciona a lista sincronizada
```

---

### 1.2 Calculo de Overall (Gamification)
**Classe:** `CalcularOverallImpl.java`
**Mecanismo:** `ExecutorService` com pool dinamico

**Descricao:** O calculo do Overall de um atleta agrega atributos de multiplos esportes concorrentemente. Cada thread calcula o valor ponderado de um atributo.

**Regiao Critica:** Os acumuladores `somaValoresPonderados` e `somaPesos` sao compartilhados. Protegidos com `AtomicReference<Double>` e operacoes CAS.

**Mecanismo de protecao:** `AtomicReference.updateAndGet()` com Compare-And-Swap.

```java
AtomicReference<Double> somaValoresPonderados = new AtomicReference<>(0.0);
// Cada thread acumula com CAS atomico
somaValoresPonderados.updateAndGet(atual -> atual + valorPonderado);
```

---

### 1.3 Motor de Match Automatico (Session)
**Classe:** `MotorDeMatchAutomaticoImpl.java`
**Mecanismo:** `ExecutorService` com pool de 4 threads

**Descricao:** Cruza check-ins de multiplos atletas com o intervalo da sessao em paralelo. Os check-ins sao divididos em particoes e cada thread valida uma particao.

**Regiao Critica:** O conjunto `atletasComMatch` e compartilhado entre as threads. Protegido com `Collections.synchronizedSet()`.

**Mecanismo de protecao:** Set sincronizado + particionamento de dados.

```java
Set<Long> atletasComMatch = Collections.synchronizedSet(new HashSet<>());
// Check-ins divididos em particoes para threads diferentes
```

---

## 2. Controle de Concorrencia na Persistencia

### Cenario: Compra concorrente de licenca de foto
**Classe:** `ProcessarVendaFotoImpl.java` + `JpaFotoRepository.java`

**Problema:** Dois atletas tentam comprar a licenca da mesma foto ao mesmo tempo. Sem controle, ambos poderiam ler a foto, verificar disponibilidade e criar licencas duplicadas.

**Solucao — Duas camadas de protecao:**

**Camada 1: Pessimistic Write Lock (JPA)**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT f FROM Foto f WHERE f.id = :id")
Optional<Foto> findByIdComLock(Long id);
```
O `PESSIMISTIC_WRITE` gera um `SELECT ... FOR UPDATE` no SQL. A primeira transacao bloqueia a linha no banco. A segunda transacao espera ate a primeira concluir.

**Camada 2: Optimistic Lock (@Version)**
```java
@Version
private Long version;
```
Se por algum motivo ambas as transacoes conseguirem ler a foto, o `@Version` garante que a segunda falha com `OptimisticLockException` ao tentar salvar.

**Fluxo do cenario:**
```
Atleta A: findByIdComLock(1) → bloqueia linha → cria licenca → commit → libera lock
Atleta B: findByIdComLock(1) → ESPERA → le apos commit de A → cria licenca → commit
```

---

## 3. Medicao de Desempenho

### Cenario: Extracao EXIF de 50 fotos
**Classe:** `BenchmarkUpload.java`

**Como executar:**
```bash
cd sportsnap-marketplace-service
mvn compile -o
java -cp target/classes com.sportsnap.marketplace.application.BenchmarkUpload
```

**Resultados esperados:**

| Metrica | Sequencial | Concorrente (4 threads) |
|---|---|---|
| Tempo de execucao | ~2500 ms | ~650 ms |
| Throughput | ~20 fotos/s | ~77 fotos/s |
| Speedup | 1.0x | ~3.8x |

**Analise:** O ganho e significativo porque a extracao EXIF e uma operacao I/O-bound (leitura de arquivo). Multiplas threads permitem que enquanto uma espera I/O, outra processa. O speedup proximo a 4x corresponde ao numero de threads no pool, indicando boa utilizacao dos recursos.
