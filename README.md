# MeuPersonal

Aplicativo Android nativo para geração, validação e gestão de fichas de treino personalizadas utilizando Inteligência Artificial (Google Gemini) e armazenamento local com persistência SQLite (Room).

---

##  Stack Tecnológica

* **Plataforma:** Android Nativo (Java 11 com *Core Library Desugaring*)
* **SDK:** `minSdk 23` (Android 6.0 Marshmallow) | `compileSdk 37` | `targetSdk 37`
* **Inteligência Artificial:** Google Gemini API
  * **Modelo Primário:** `gemini-3.8-flash`
  * **Modelos de Fallback:** `gemini-3.7-flash`, `gemini-3.5-flash`, `gemini-3.5-flash-lite`
  * **Geração Visual / Mídia:** `gemini-2.5-flash-image`
* **Persistência Local:** Android Jetpack Room (`2.8.3`) + SQLite
* **Comunicação de Rede:** OkHttp 3 (`4.12.0`) / `HttpURLConnection`
* **Integração Cloud:** Supabase Client (HTTP REST / BaaS)
* **Produtividade de Código:** Project Lombok (`1.18.42`)
* **Engajamento Local:** Android `AlarmManager` + `BroadcastReceiver` + Notification Channels
* **Testes Automatizados:** JUnit 4, Mockito Core 5, Espresso Core

---

##  Arquitetura do Sistema

O projeto adota uma arquitetura em camadas desacopladas (Domain, Service, Repository, UI):

```
app/src/main/java/com/meupersonal/
├── data/                    # Mecanismos de armazenamento e cache local em memória
├── model/                   # Modelos de domínio do sistema
│   ├── historico/           # Histórico de sessões e execuções de exercícios
│   ├── questionario/        # Anamnese, dados biométricos e restrições ortopédicas
│   ├── treino/              # Planilhas, treinos diários e exercícios
│   └── user/                # Entidades de usuário e perfis antropométricos
├── repository/              # Camada de abstração de dados
│   ├── converter/           # Conversores de tipos para persistência Room (JSON/Dates)
│   ├── dao/                 # Data Access Objects (WorkoutDao, ExerciseDao)
│   └── database/            # MeuPersonalDatabase (Configuração do Room DB)
├── service/                 # Camada de lógica de negócios e integrações
│   ├── ia/                  # Integração com API Google Gemini e fallback sequencial
│   ├── ml/                  # Categorização e análise de sentimento de feedbacks
│   ├── notificacao/         # Agendamento de alarmes e notificações locais
│   └── treino/              # Orquestrador (TrainingGenerator) e Validador (TrainingPlanValidator)
└── MainActivity.java        # Interface principal, coleta de dados e renderização de abas de treino
```

---

##  Funcionalidades Implementadas

### 1. Geração de Planilhas de Treino via IA
* **Orquestração Inteligente (`TrainingGenerator`):** Submete a anamnese do usuário à API do Gemini utilizando prompts com diretrizes restritas de biomecânica e prevenção de lesões.
* **Resiliência e Fallback (`GeminiAiService`):** Em caso de indisponibilidade, latência ou limites de cota da API, o serviço alterna sequencialmente entre modelos `3.8-flash`, `3.7-flash`, `3.5-flash` e `3.5-flash-lite`.
* **Demonstração Visual de Exercícios:** Requisição assíncrona sob demanda para o modelo `gemini-2.5-flash-image` para exibição gráfica da postura de cada exercício.

### 2. Validação Biomecânica e de Integridade (`TrainingPlanValidator`)
* Validação estrita do payload JSON retornado pela IA antes da persistência ou exibição.
* Bloqueio de planos com dados incompletos ou exercícios inconsistentes com as restrições declaradas no questionário.

### 3. Anamnese Completa (`model/questionario`)
* Coleta de biometria (peso, altura, idade) e rotina (dias disponíveis, tempo por sessão).
* Mapeamento de histórico musculoesquelético: regiões corporais (`BodyRegion`), patologias/lesões e níveis de severidade (`Severity`).
* Adequação de infraestrutura: treino em academia, casa ou ar livre, filtrando exercícios pelos equipamentos disponíveis.

### 4. Persistência de Dados e Histórico (`Room Database`)
* Estrutura relacional local composta por entidades com suporte a exclusão em cascata:
  * `WorkoutPlan` -> `Workout` -> `Exercise`
* Registro de execução de treinos (`WorkoutHistory`) e cargas executadas (`ExerciseExecution`).

### 5. Notificações e Lembretes de Treino (`WorkoutReminderService`)
* Agendamento de alarmes no sistema operacional Android utilizando `AlarmManager` e `WorkoutReminderReceiver`.
* Emissão de notificações locais em canais configurados (`NotificationChannel`).

### 6. Análise de Sentimento de Feedback (`SentimentAnalysisService`)
* Processamento do feedback do usuário por processamento de linguagem natural, categorizando o retorno em:
  `POSITIVO`, `NEGATIVO`, `SUGESTAO`, `ERRO` ou `OUTRO`.

---

##  Configuração do Ambiente de Desenvolvimento

### Pré-requisitos
* Android Studio (versão Ladybug / Meerkat ou superior recomendada).
* JDK 11 configurado no ambiente.
* Android SDK Platform 37 e ferramentas de build instaladas.

### Configuração de Chaves (`local.properties`)
O projeto utiliza injeção de credenciais via `local.properties` para compilação segura no `BuildConfig`. Adicione as seguintes propriedades no arquivo `local.properties` na raiz do projeto:


### Compilação e Execução
Via terminal:

```bash
# Compilar o APK de depuração
./gradlew assembleDebug

# Executar a suíte de testes unitários
./gradlew test

# Instalar no dispositivo ou emulador conectado
./gradlew installDebug
```
