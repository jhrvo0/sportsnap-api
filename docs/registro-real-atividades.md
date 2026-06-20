# Registro Real de Atividades e Analise de Progresso

O SportSnap conta com um modulo para **Registro Real de Atividades** que opera de forma paralela e independente da gamificacao. A intencao e que atletas registrem treinos reais manuais, consultem seu historico e visualizem analises e metricas reais de evolucao fisica, sem dependencia direta do XP, ranking ou reveal da carta.

---

## 1. Banco de Dados

A tabela `REGISTRO_ATIVIDADE` foi evoluida por meio de uma migracao Flyway (`V2__add_registro_atividade_real_fields.sql`). As colunas adicionadas e modificadas foram:

- `ATLETA_ID` (INT, NOT NULL) - Identificador do atleta dono do registro.
- `CHECK_IN_ID` (INT, NULL) - Caso o registro venha de um check-in de spot, mantem a referencia opcional.
- `INTENSIDADE` (VARCHAR, NULL) - Intensidade do treino.
- `XP_CALCULADO` (DOUBLE, NULL) - XP ganho pelo treino.
- `ESPORTE` (VARCHAR, NOT NULL) - Modalidade esportiva.
- `DATA` (TIMESTAMP, NOT NULL) - Data e hora de ocorrencia do treino.
- `ESFORCO_PERCEBIDO` (INT, NULL) - Nivel de esforco percebido pelo atleta de 1 a 10.
- `OBSERVACOES` (VARCHAR, NULL) - Comentarios gerais sobre o percurso ou clima.
- `ORIGEM_REGISTRO` (VARCHAR, NOT NULL) - Indica a origem do registro: `MANUAL`, `CHECKIN` ou `IMPORTADO`.
- `CRIADO_EM` / `ATUALIZADO_EM` (TIMESTAMP) - Carimbos de criacao e edicao.

---

## 2. API REST (Porta 8083)

Os seguintes endpoints foram expostos no `AtividadeControlador` do modulo `sportsnap-session-service`:

- `POST /api/atividades` - Registra manualmente uma nova atividade.
- `GET /api/atividades?atletaId={id}&esporte={esporte}` - Lista as atividades de um atleta com filtros opcionais.
- `GET /api/atividades/{id}` - Detalha uma atividade pelo ID.
- `PUT /api/atividades/{id}` - Atualiza os dados de um registro.
- `DELETE /api/atividades/{id}` - Remove permanentemente um treino.
- `GET /api/atividades/analise?atletaId={id}&esporte={esporte}&periodo={7d|30d|90d}` - Retorna os dados agregados para o dashboard de progresso fisico.

---

## 3. Interfaces Web (sportsnap-web)

Foram integradas duas novas telas na aplicacao Next.js:

1. **Historico de Treinos** (`/atividades`):
   - Formulario para registrar treinos manuais com validacoes de campos negativos, zerados ou datas futuras.
   - Tabela responsiva de listagem que detalha ritmos medios, calorias estimadas e observacoes.
   - Opcoes para editar treinos legados ou manuais e exclui-los da base de dados.
   - Filtros dinamicos por tipo de esporte e intervalo de tempo.

2. **Evolucao Real** (`/atividades/analise`):
   - Cards sumarios contendo os recordes e marcas acumuladas.
   - Banner de consistencia mostrando a frequencia semanal media de treinos.
   - Graficos em linha para acompanhar a distancia percorrida e a evolucao do ritmo medio.
   - Lista historica detalhada de todos os treinos que compoem a amostragem da analise.

---

## 4. Testes BDD

Os cenarios de teste automatizados em portugues estao definidos em:
- [h03-registro-real-atividade.feature](../sportsnap-session-service/src/test/resources/features/h03-registro-real-atividade.feature)

E codificados em:
- [RegistroRealAtividadeSteps.java](../sportsnap-session-service/src/test/java/com/sportsnap/session/bdd/steps/RegistroRealAtividadeSteps.java)

### Como executar os testes:
A partir do diretorio raiz da API do projeto, execute:
```bash
.\mvnw -pl sportsnap-session-service test
```
