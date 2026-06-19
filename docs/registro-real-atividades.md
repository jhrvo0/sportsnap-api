# Registro Real de Atividades e Análise de Progresso

O SportSnap conta agora com um módulo para **Registro Real de Atividades** que opera de forma paralela e independente da gamificação. A intenção é que atletas registrem treinos reais manuais, consultem seu histórico e visualizem análises e métricas reais de evolução física (especialmente para corrida, caminhada e bicicleta), sem dependência ou alteração direta do XP, ranking ou reveal da Carta.

---

## 1. Banco de Dados

A tabela `REGISTRO_ATIVIDADE` foi evoluída por meio de uma migração Flyway (`V2__add_registro_atividade_real_fields.sql`). As colunas adicionadas e modificadas foram:

- `ATLETA_ID` (INT, NOT NULL) — Identificador do atleta dono do registro.
- `CHECK_IN_ID` (INT, NULL) — Caso o registro tenha vindo de um check-in de spot, mantém-se a referência (opcional).
- `INTENSIDADE` (VARCHAR, NULL) — Intensidade do treino (opcional para treinos manuais).
- `XP_CALCULADO` (DOUBLE, NULL) — XP ganho pelo treino (relevante apenas para a gamificação, zero para manuais).
- `ESPORTE` (VARCHAR, NOT NULL) — Modalidade esportiva (ex: `CORRIDA`, `BICICLETA`, `CAMINHADA`).
- `DATA` (TIMESTAMP, NOT NULL) — Data e hora de ocorrência do treino.
- `ESFORCO_PERCEBIDO` (INT, NULL) — Nível de esforço percebido pelo atleta de 1 a 10.
- `OBSERVACOES` (VARCHAR, NULL) — Comentários gerais sobre o percurso/clima.
- `ORIGEM_REGISTRO` (VARCHAR, NOT NULL) — Indica a origem do registro: `MANUAL`, `CHECKIN` ou `IMPORTADO`.
- `CRIADO_EM` / `ATUALIZADO_EM` (TIMESTAMP) — Carimbos de criação e edição.

---

## 2. API REST (Porta 8083)

Os seguintes endpoints foram expostos no `AtividadeControlador` do módulo `sportsnap-session-service`:

- `POST /api/atividades` — Registra manualmente uma nova atividade.
- `GET /api/atividades?atletaId={id}&esporte={esporte}` — Lista as atividades de um atleta com filtros opcionais.
- `GET /api/atividades/{id}` — Detalha uma atividade pelo ID.
- `PUT /api/atividades/{id}` — Atualiza os dados de um registro (duração, distância, esforço, observações).
- `DELETE /api/atividades/{id}` — Remove permanentemente um treino.
- `GET /api/atividades/analise?atletaId={id}&esporte={esporte}&periodo={7d|30d|90d}` — Retorna os dados agregados para geração do dashboard de progresso físico (total de sessões, distância e tempo acumulados, ritmo médio geral, melhor ritmo, maior distância, frequência semanal e coordenadas para plotagem dos gráficos de progresso).

---

## 3. Interfaces Web (sportsnap-web)

Foram integradas duas novas telas na aplicação Next.js:

1. **Histórico de Treinos** (`/atividades`):
   - Formulário para registrar treinos manuais (com validações de campos negativos, zerados ou datas futuras).
   - Tabela responsiva de listagem que detalha ritmos médios (min/km), calorias estimadas e observações.
   - Opções para editar treinos legados/manuais e excluí-los da base de dados.
   - Filtros dinâmicos rápidos por tipo de esporte e intervalo de tempo.

2. **Evolução Real** (`/atividades/analise`):
   - Cards sumários contendo os recordes e marcas acumuladas.
   - Um banner de consistência mostrando a frequência semanal média de treinos.
   - Gráficos em linha baseados em SVGs premium para acompanhar a flutuação da distância percorrida e a evolução do ritmo médio (min/km).
   - Lista histórica detalhada de todos os treinos que compõem a amostragem da análise.

---

## 4. Testes BDD

Os cenários de teste automatizados em português estão definidos em:
- [h03-registro-real-atividade.feature](file:///C:/Users/joaoh/Downloads/sportsnap-api-main/sportsnap-api-main/sportsnap-session-service/src/test/resources/features/h03-registro-real-atividade.feature)

E codificados em:
- [RegistroRealAtividadeSteps.java](file:///C:/Users/joaoh/Downloads/sportsnap-api-main/sportsnap-api-main/sportsnap-session-service/src/test/java/com/sportsnap/session/bdd/steps/RegistroRealAtividadeSteps.java)

### Como executar os testes:
A partir do diretório raiz da API do projeto (`C:\Users\joaoh\Downloads\sportsnap-api-main\sportsnap-api-main`), execute:
```bash
.\mvnw -pl sportsnap-session-service test
```
