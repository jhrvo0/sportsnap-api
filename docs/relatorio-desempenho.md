# Relatório de Desempenho — SportSnap

## Cenário Testado
Dashboard do fotógrafo calculando total de lotes, fotos, vendas, receita e saldo,
comparando execução sequencial com execução paralela.

## Metodologia
- **Sequencial:** 10 chamadas a `DashboardServico.consultarResumo()` uma após a outra
- **Paralelo:** 10 threads simultâneas chamando `consultarResumo()` ao mesmo tempo
- Endpoint de teste: `GET /api/benchmark/dashboard?fotografoId=1`
- Métrica: tempo total em milissegundos (ms)

## Como Executar
```bash
# Com o marketplace-service rodando na porta 8082:
curl http://localhost:8082/api/benchmark/dashboard?fotografoId=1
```

Resposta esperada:
```json
{
  "iteracoes": 10,
  "tempoSequencialMs": 480,
  "tempoParaleloMs": 130,
  "throughputSequencial": 20.8,
  "throughputParalelo": 76.9,
  "fatorGanho": 3.69
}
```

## Resultados Esperados

| Métrica | Sequencial | Paralelo | Ganho |
|---|---|---|---|
| Tempo total | ~480ms | ~130ms | ~3.7x |
| Throughput (req/s) | ~21 | ~77 | ~3.7x |

> Os valores reais variam conforme hardware e volume de dados. Execute o endpoint para obter
> os números do seu ambiente.

## Análise

O ganho de ~3-4x com paralelismo é esperado para operações **I/O-bound** (consultas JPA ao H2).
O pool de 4 threads do `dashboardExecutor` processa até 4 lotes em paralelo por requisição.

**Quando o paralelismo não compensa:**
- Fotógrafos com menos de 3 lotes: overhead de thread management supera o ganho
- Operações CPU-bound puro: ganho limitado ao número de núcleos disponíveis

**Regiões críticas identificadas:**
- `AtomicInteger` para `totalFotos` e `totalVendas` — evita race conditions em contagens
- `AtomicReference<Dinheiro>` com `accumulateAndGet` — garante atomicidade nos somatórios monetários
