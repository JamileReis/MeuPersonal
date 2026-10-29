package com.meupersonal;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.meupersonal.data.LocalWorkoutStore;
import com.meupersonal.service.ia.GeminiAiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private EditText nomeInput, idadeInput, pesoInput, alturaInput;
    private EditText preferenciasInput, diasInput, duracaoInput, restricoesInput;
    private Spinner objetivoSpinner, nivelSpinner;
    private TextView resultadoTexto, observacoesTexto, statusTexto;
    private TabLayout abasTreinos;
    private FrameLayout areaTreino;
    private Button gerarButton, copiarButton;
    private ProgressBar progresso;
    private final List<LinearLayout> paginasTreino = new ArrayList<>();
    private final Set<String> imagensSolicitadas = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private GeminiAiService geminiAiService;
    private LocalWorkoutStore store;
    private String aiConfigError = "";
    private String resultadoCompleto = "";
    private long currentPlanId = -1L;
    private volatile long generationToken = 0L;
    private volatile boolean activityDestroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            geminiAiService = new GeminiAiService(GeminiAiService.API_KEY);
        } catch (IllegalArgumentException missingKey) {
            aiConfigError = "A chave do Gemini não está configurada. Os planos já salvos e o histórico continuam disponíveis.";
        }
        store = new LocalWorkoutStore(this);
        construirInterface();
        restaurarPerfil();
    }

    private void construirInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(28));
        scroll.addView(root);

        TextView brand = new TextView(this);
        brand.setText("MeuPersonal");
        brand.setTextSize(30);
        brand.setTypeface(null, Typeface.BOLD);
        brand.setTextColor(0xFFC45431);
        root.addView(brand);
        TextView subtitle = new TextView(this);
        subtitle.setText("Seu plano, suas sessões e seu progresso — salvos neste aparelho.");
        subtitle.setTextSize(15);
        subtitle.setPadding(0, dp(4), 0, dp(12));
        root.addView(subtitle);

        LinearLayout navigation = new LinearLayout(this);
        navigation.setOrientation(LinearLayout.HORIZONTAL);
        Button savedPlans = new Button(this);
        savedPlans.setText("Meus planos");
        Button history = new Button(this);
        history.setText("Histórico");
        navigation.addView(savedPlans, new LinearLayout.LayoutParams(0, dp(50), 1));
        navigation.addView(history, new LinearLayout.LayoutParams(0, dp(50), 1));
        root.addView(navigation);
        savedPlans.setOnClickListener(v -> mostrarPlanosSalvos());
        history.setOnClickListener(v -> mostrarHistorico());

        adicionarRotulo(root, "Seu perfil (opcional)");
        nomeInput = criarCampo("Nome", "", false);
        root.addView(nomeInput);
        LinearLayout medidas = new LinearLayout(this);
        medidas.setOrientation(LinearLayout.HORIZONTAL);
        idadeInput = criarCampo("Idade", "", true);
        idadeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        pesoInput = criarCampo("Peso kg", "", true);
        alturaInput = criarCampo("Altura cm", "", true);
        LinearLayout.LayoutParams medidaLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        medidas.addView(idadeInput, medidaLp);
        medidas.addView(pesoInput, medidaLp);
        medidas.addView(alturaInput, medidaLp);
        root.addView(medidas);

        adicionarRotulo(root, "Objetivo");
        objetivoSpinner = criarSpinner(new String[]{"Hipertrofia", "Emagrecimento",
                "Condicionamento físico", "Força", "Resistência muscular", "Saúde e bem-estar"});
        root.addView(objetivoSpinner);
        adicionarRotulo(root, "Nível de experiência");
        nivelSpinner = criarSpinner(new String[]{"Iniciante", "Intermediário", "Avançado"});
        root.addView(nivelSpinner);

        adicionarRotulo(root, "Dias de treino por semana (1 a 7)");
        diasInput = criarCampo("Ex.: 4", "4", true);
        diasInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        root.addView(diasInput);
        adicionarRotulo(root, "Duração de cada sessão em minutos");
        duracaoInput = criarCampo("Ex.: 45", "45", true);
        duracaoInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        root.addView(duracaoInput);
        adicionarRotulo(root, "Preferências e comandos para a IA");
        preferenciasInput = criarCampo("Ex.: prefiro halteres, evite corrida, sessões curtas", "", false);
        preferenciasInput.setMinLines(2);
        preferenciasInput.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        root.addView(preferenciasInput);
        adicionarRotulo(root, "Equipamentos, restrições ou limitações");
        restricoesInput = criarCampo("Informe o que tem disponível e quaisquer limitações relevantes", "", false);
        restricoesInput.setMinLines(3);
        restricoesInput.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        root.addView(restricoesInput);

        gerarButton = new Button(this);
        gerarButton.setText("Gerar e salvar plano");
        LinearLayout.LayoutParams primaryLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        primaryLp.topMargin = dp(18);
        root.addView(gerarButton, primaryLp);
        progresso = new ProgressBar(this);
        progresso.setIndeterminate(true);
        progresso.setVisibility(View.GONE);
        root.addView(progresso);

        TextView resultTitle = new TextView(this);
        resultTitle.setText("Plano de treino");
        resultTitle.setTextSize(21);
        resultTitle.setTypeface(null, Typeface.BOLD);
        resultTitle.setPadding(0, dp(22), 0, dp(5));
        root.addView(resultTitle);
        resultadoTexto = new TextView(this);
        resultadoTexto.setText("Preencha os dados e toque em Gerar e salvar plano.");
        resultadoTexto.setTextSize(18);
        resultadoTexto.setTypeface(null, Typeface.BOLD);
        resultadoTexto.setPadding(dp(12), dp(12), dp(12), dp(12));
        resultadoTexto.setBackgroundColor(0xFFF3F1EC);
        root.addView(resultadoTexto);
        statusTexto = new TextView(this);
        statusTexto.setTextSize(13);
        statusTexto.setText(aiConfigError);
        root.addView(statusTexto);
        observacoesTexto = new TextView(this);
        observacoesTexto.setTextSize(14);
        observacoesTexto.setPadding(0, dp(6), 0, dp(8));
        root.addView(observacoesTexto);

        abasTreinos = new TabLayout(this);
        abasTreinos.setTabMode(TabLayout.MODE_SCROLLABLE);
        abasTreinos.setVisibility(View.GONE);
        abasTreinos.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { mostrarPagina(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { mostrarPagina(tab.getPosition()); }
        });
        root.addView(abasTreinos);
        areaTreino = new FrameLayout(this);
        root.addView(areaTreino, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        copiarButton = new Button(this);
        copiarButton.setText("Copiar plano");
        copiarButton.setEnabled(false);
        root.addView(copiarButton);

        gerarButton.setOnClickListener(v -> gerarPlano());
        copiarButton.setOnClickListener(v -> copiarResultado());
        setContentView(scroll);
    }

    private void adicionarRotulo(LinearLayout parent, String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(15);
        label.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        lp.bottomMargin = dp(4);
        parent.addView(label, lp);
    }

    private EditText criarCampo(String hint, String initial, boolean numeric) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(initial);
        field.setSingleLine(numeric);
        field.setInputType(numeric ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        field.setPadding(dp(12), dp(9), dp(12), dp(9));
        field.setContentDescription(hint);
        return field;
    }

    private Spinner criarSpinner(String[] options) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }

    private void restaurarPerfil() {
        android.content.SharedPreferences p = getSharedPreferences("profile", MODE_PRIVATE);
        nomeInput.setText(p.getString("name", ""));
        idadeInput.setText(p.getString("age", ""));
        pesoInput.setText(p.getString("weight", ""));
        alturaInput.setText(p.getString("height", ""));
        preferenciasInput.setText(p.getString("preferences", ""));
        restricoesInput.setText(p.getString("restrictions", ""));
        diasInput.setText(p.getString("days", "4"));
        duracaoInput.setText(p.getString("minutes", "45"));
        objetivoSpinner.setSelection(p.getInt("objective", 0));
        nivelSpinner.setSelection(p.getInt("level", 1));
    }

    private void salvarPerfil() {
        getSharedPreferences("profile", MODE_PRIVATE).edit()
                .putString("name", texto(nomeInput)).putString("age", texto(idadeInput))
                .putString("weight", texto(pesoInput)).putString("height", texto(alturaInput))
                .putString("preferences", texto(preferenciasInput))
                .putString("restrictions", texto(restricoesInput))
                .putString("days", texto(diasInput)).putString("minutes", texto(duracaoInput))
                .putInt("objective", objetivoSpinner.getSelectedItemPosition())
                .putInt("level", nivelSpinner.getSelectedItemPosition()).apply();
    }

    private String texto(EditText field) { return field.getText().toString().trim(); }

    private void gerarPlano() {
        if (geminiAiService == null) {
            statusTexto.setText(aiConfigError);
            return;
        }
        int days, minutes;
        try {
            days = Integer.parseInt(texto(diasInput));
            minutes = Integer.parseInt(texto(duracaoInput));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Informe dias e duração usando números.", Toast.LENGTH_LONG).show();
            return;
        }
        if (days < 1 || days > 7 || minutes < 10 || minutes > 240) {
            Toast.makeText(this, "Use de 1 a 7 dias e de 10 a 240 minutos por sessão.", Toast.LENGTH_LONG).show();
            return;
        }
        salvarPerfil();
        String requestedObjective = objetivoSpinner.getSelectedItem().toString();
        String prompt = criarPrompt(days, minutes);
        final long token = ++generationToken;
        gerarButton.setEnabled(false);
        gerarButton.setText("Gerando plano…");
        copiarButton.setEnabled(false);
        progresso.setVisibility(View.VISIBLE);
        statusTexto.setText("Conectando à IA. Seus dados salvos permanecem neste aparelho.");
        executor.execute(() -> {
            final String[] rawResponse = {null};
            try {
                String json = geminiAiService.sendPrompt(prompt);
                rawResponse[0] = json;
                JSONObject plan = parsePlan(json);
                if ("Personalizado".equals(plan.optString("objetivo"))) {
                    plan.put("objetivo", requestedObjective);
                }
                String normalizedJson = plan.toString();
                long id = store.savePlan(plan.optString("titulo", "Plano de treino"),
                        plan.optString("objetivo", requestedObjective),
                        normalizedJson, System.currentTimeMillis());
                if (!activityDestroyed && token == generationToken) {
                    runOnUiThread(() -> {
                        if (activityDestroyed) return;
                        gerarButton.setEnabled(true);
                        gerarButton.setText("Gerar novo plano");
                        progresso.setVisibility(View.GONE);
                        copiarButton.setEnabled(true);
                        mostrarPlano(normalizedJson, id, true);
                    });
                }
            } catch (IOException | JSONException | RuntimeException error) {
                String message = mensagemAmigavel(error);
                if (!activityDestroyed && token == generationToken) runOnUiThread(() -> {
                    if (activityDestroyed) return;
                    gerarButton.setEnabled(true);
                    gerarButton.setText("Tentar novamente");
                    progresso.setVisibility(View.GONE);
                    statusTexto.setText("Não foi possível gerar/salvar o plano. Seus planos anteriores continuam disponíveis.");
                    if (rawResponse[0] != null && !rawResponse[0].trim().isEmpty()) {
                        resultadoCompleto = rawResponse[0];
                        resultadoTexto.setText(message + "\n\nResposta original da IA:\n" + rawResponse[0]);
                        copiarButton.setEnabled(true);
                    } else {
                        resultadoTexto.setText(message);
                    }
                    observacoesTexto.setText("");
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String criarPrompt(int days, int minutes) {
        String objective = objetivoSpinner.getSelectedItem().toString();
        String level = nivelSpinner.getSelectedItem().toString();
        StringBuilder p = new StringBuilder();
        p.append("Crie um plano seguro e personalizado para musculação/condicionamento. Responda em português do Brasil, SOMENTE JSON válido, sem Markdown.\n");
        p.append("Perfil: nome=").append(vazio(texto(nomeInput))).append("; idade=").append(vazio(texto(idadeInput)))
                .append("; peso kg=").append(vazio(texto(pesoInput))).append("; altura cm=").append(vazio(texto(alturaInput))).append(".\n");
        p.append("Objetivo: ").append(objective).append(". Nível: ").append(level).append(". Dias por semana: ")
                .append(days).append(". Minutos por sessão: ").append(minutes).append(".\n");
        p.append("Instruções adicionais do usuário (siga, exceto se inseguras): ").append(vazio(texto(preferenciasInput))).append(".\n");
        p.append("Equipamentos e limitações/restrições: ").append(vazio(texto(restricoesInput))).append(".\n");
        p.append("Retorne formato: {\"titulo\":\"\",\"objetivo\":\"\",\"observacoes\":\"\",\"dias\":[{\"dia\":\"Treino A\",\"nome\":\"\",\"duracaoMinutos\":")
                .append(minutes).append(",\"exercicios\":[{\"nome\":\"\",\"grupoMuscular\":\"\",\"equipamento\":\"\",\"series\":3,\"repeticoes\":\"8-12\",\"descansoSegundos\":90,\"instrucao\":\"\"}]}]}.\n");
        p.append("Crie exatamente ").append(days).append(" sessões não redundantes. Inclua aquecimento e desaquecimento como itens. Use apenas equipamentos informados; se nenhum equipamento for informado, prefira peso corporal ou opções acessíveis e declare a suposição. Siga as instruções adicionais; explique em observacoes qualquer instrução incompatível ou limitação. Não diagnostique nem prescreva para lesões/condições clínicas: recomende profissional habilitado. Não invente dados do perfil.");
        return p.toString();
    }

    private String vazio(String value) { return value.isEmpty() ? "não informado" : value; }

    private JSONObject parsePlan(String raw) throws JSONException, IOException {
        String normalized = raw == null ? "" : raw.trim()
                .replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        JSONObject plan = new JSONObject(normalized);
        JSONArray days = plan.optJSONArray("dias");
        if (days == null || days.length() == 0) throw new IOException("A IA não retornou dias de treino válidos.");
        for (int i = 0; i < days.length(); i++) {
            JSONObject day = days.optJSONObject(i);
            if (day == null || day.optJSONArray("exercicios") == null
                    || day.optJSONArray("exercicios").length() == 0) {
                throw new IOException("Uma sessão veio sem exercícios. Tente gerar o plano novamente.");
            }
        }
        if (plan.optString("titulo", "").trim().isEmpty()) plan.put("titulo", "Plano personalizado");
        if (plan.optString("objetivo", "").trim().isEmpty()) plan.put("objetivo", "Personalizado");
        return plan;
    }

    private void mostrarPlano(String raw, long planId, boolean saved) {
        final JSONObject plan;
        try { plan = parsePlan(raw); }
        catch (JSONException | IOException e) {
            resultadoTexto.setText(raw);
            statusTexto.setText("A resposta foi preservada, mas não pôde ser organizada.");
            return;
        }
        resultadoCompleto = raw;
        currentPlanId = planId;
        resultadoTexto.setText(plan.optString("titulo", "Plano de treino"));
        observacoesTexto.setText(plan.optString("observacoes", ""));
        statusTexto.setText(saved ? "Plano salvo neste aparelho. As imagens ficam em cache para consulta posterior." : "Plano carregado do aparelho.");
        JSONArray days = plan.optJSONArray("dias");
        paginasTreino.clear();
        areaTreino.removeAllViews();
        abasTreinos.removeAllTabs();
        for (int i = 0; i < days.length(); i++) {
            JSONObject day = days.optJSONObject(i);
            if (day == null) continue;
            String label = day.optString("dia", "Treino " + (char) ('A' + i));
            String name = day.optString("nome", "");
            abasTreinos.addTab(abasTreinos.newTab().setText(name.isEmpty() ? label : label + " · " + name));
            paginasTreino.add(criarPaginaTreino(day, i, planId));
        }
        abasTreinos.setVisibility(View.VISIBLE);
        if (abasTreinos.getTabCount() > 0) abasTreinos.selectTab(abasTreinos.getTabAt(0));
        mostrarPagina(0);
        for (int i = 0; i < paginasTreino.size(); i++) {
            Object callback = paginasTreino.get(i).getTag();
            if (callback instanceof Runnable) ((Runnable) callback).run();
        }
        if (paginasTreino.size() > 1) {
            statusTexto.setText("Plano salvo. As ilustrações de todas as sessões estão sendo carregadas em segundo plano.");
        }
    }

    private LinearLayout criarPaginaTreino(JSONObject day, int index, long planId) {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(8), dp(8), dp(8), dp(12));
        TextView name = new TextView(this);
        name.setText(day.optString("nome", day.optString("dia", "Treino")));
        name.setTextSize(20);
        name.setTypeface(null, Typeface.BOLD);
        page.addView(name);
        int duration = day.optInt("duracaoMinutos", 0);
        if (duration > 0) addSmallText(page, "Duração estimada: " + duration + " min");

        ImageView image = new ImageView(this);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setBackgroundColor(0xFFF3F1EC);
        image.setContentDescription("Ilustração de referência para " + name.getText());
        page.addView(image, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(205)));
        TextView imageStatus = new TextView(this);
        imageStatus.setText("A imagem de exemplo será carregada para esta sessão.");
        page.addView(imageStatus);
        Button retryImage = new Button(this);
        retryImage.setText("Gerar imagem de exemplo");
        page.addView(retryImage);

        JSONArray exercises = day.optJSONArray("exercicios");
        StringBuilder names = new StringBuilder();
        if (exercises != null) for (int i = 0; i < exercises.length(); i++) {
            JSONObject exercise = exercises.optJSONObject(i);
            if (exercise == null) continue;
            String exerciseName = exercise.optString("nome", "Exercício");
            if (names.length() > 0) names.append(", ");
            names.append(exerciseName);
            TextView card = new TextView(this);
            StringBuilder details = new StringBuilder("●  ").append(exerciseName);
            String muscle = exercise.optString("grupoMuscular", "");
            String equipment = exercise.optString("equipamento", "");
            int sets = exercise.optInt("series", 0);
            String reps = exercise.optString("repeticoes", "");
            int rest = exercise.optInt("descansoSegundos", 0);
            if (!muscle.isEmpty()) details.append("\nGrupo: ").append(muscle);
            if (!equipment.isEmpty()) details.append("\nEquipamento: ").append(equipment);
            if (sets > 0 || !reps.isEmpty()) details.append("\nSéries/repetições: ").append(sets).append(" × ").append(reps);
            if (rest > 0) details.append("\nDescanso: ").append(rest).append(" s");
            String instruction = exercise.optString("instrucao", "");
            if (!instruction.isEmpty()) details.append("\nExecução: ").append(instruction);
            card.setText(details.toString());
            card.setTextSize(15);
            card.setPadding(dp(12), dp(12), dp(12), dp(12));
            card.setBackgroundColor(0xFFF5F2ED);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardLp.topMargin = dp(8);
            page.addView(card, cardLp);
        }

        final int dayIndex = index;
        Runnable generateImage = () -> solicitarImagem(planId, dayIndex, day, names.toString(), image, imageStatus, retryImage);
        retryImage.setOnClickListener(v -> {
            imagensSolicitadas.remove(chaveImagem(planId, dayIndex));
            store.deleteImage(planId, dayIndex);
            image.setImageDrawable(null);
            generateImage.run();
        });
        page.setTag(generateImage);

        long openSession = store.getOpenSession(planId, dayIndex);
        final long[] sessionId = {openSession};
        Button sessionButton = new Button(this);
        sessionButton.setText(openSession >= 0 ? "Finalizar sessão" : "Iniciar sessão");
        LinearLayout.LayoutParams sessionLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sessionLp.topMargin = dp(14);
        page.addView(sessionButton, sessionLp);
        sessionButton.setOnClickListener(v -> {
            String sessionTitle = day.optString("nome", day.optString("dia", "Treino"));
            if (sessionId[0] < 0) {
                sessionId[0] = store.startSession(planId, dayIndex, sessionTitle, System.currentTimeMillis());
                sessionButton.setText("Finalizar sessão");
                addSmallText(page, "Sessão iniciada às " + DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()));
                Toast.makeText(this, "Treino iniciado. Bom treino!", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this).setTitle("Finalizar treino?")
                        .setMessage("Deseja registrar esta sessão como concluída?")
                        .setNegativeButton("Continuar", null)
                        .setPositiveButton("Concluir", (dialog, which) -> {
                            store.finishSession(sessionId[0], System.currentTimeMillis());
                            sessionId[0] = -1L;
                            sessionButton.setText("Treino concluído · iniciar novamente");
                            sessionButton.setOnClickListener(x -> iniciarNovaSessao(planId, dayIndex, sessionTitle, sessionButton));
                            Toast.makeText(this, "Treino registrado no histórico.", Toast.LENGTH_SHORT).show();
                        }).show();
            }
        });
        return page;
    }

    private void iniciarNovaSessao(long planId, int dayIndex, String title, Button button) {
        long session = store.startSession(planId, dayIndex, title, System.currentTimeMillis());
        button.setText("Finalizar sessão");
        button.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Finalizar treino?")
                .setMessage("Deseja registrar esta sessão como concluída?")
                .setNegativeButton("Continuar", null)
                .setPositiveButton("Concluir", (d, w) -> {
                    store.finishSession(session, System.currentTimeMillis());
                    button.setText("Treino concluído · iniciar novamente");
                    button.setOnClickListener(x -> iniciarNovaSessao(planId, dayIndex, title, button));
                    Toast.makeText(this, "Treino registrado no histórico.", Toast.LENGTH_SHORT).show();
                }).show());
    }

    private String chaveImagem(long planId, int dayIndex) {
        return planId + ":" + dayIndex;
    }

    private void addSmallText(LinearLayout parent, String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(14);
        parent.addView(view);
    }

    private void mostrarPagina(int position) {
        if (position < 0 || position >= paginasTreino.size()) return;
        areaTreino.removeAllViews();
        areaTreino.addView(paginasTreino.get(position));
        Object callback = paginasTreino.get(position).getTag();
        if (callback instanceof Runnable
                && !imagensSolicitadas.contains(chaveImagem(currentPlanId, position))) {
            ((Runnable) callback).run();
        }
    }

    private void solicitarImagem(long planId, int dayIndex, JSONObject day, String exerciseNames,
                                 ImageView imageView, TextView state, Button retryButton) {
        String requestKey = chaveImagem(planId, dayIndex);
        if (planId < 0 || !imagensSolicitadas.add(requestKey)) return;
        retryButton.setEnabled(false);
        state.setText("Buscando imagem salva ou gerando ilustração…");
        imageExecutor.execute(() -> {
            try {
                byte[] bytes = store.getImage(planId, dayIndex);
                boolean fromCache = bytes != null && bytes.length > 0;
                if (!fromCache) {
                    String name = day.optString("nome", day.optString("dia", "Treino"));
                    bytes = geminiAiService.generateWorkoutImage(name, exerciseNames);
                    store.saveImage(planId, dayIndex, bytes);
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap == null) throw new IOException("O arquivo de imagem recebido é inválido.");
                final Bitmap finalBitmap = bitmap;
                final boolean cached = fromCache;
                if (!activityDestroyed) runOnUiThread(() -> {
                    if (activityDestroyed) return;
                    imageView.setImageBitmap(finalBitmap);
                    imageView.setContentDescription("Ilustração" + (cached ? " salva" : " gerada") + " para "
                            + day.optString("nome", day.optString("dia", "a sessão")));
                    state.setText("Ilustração de referência (não substitui a demonstração de um profissional)."
                            + (cached ? " Carregada do aparelho." : " Salva no aparelho."));
                    retryButton.setEnabled(true);
                    retryButton.setText("Gerar outra ilustração");
                });
            } catch (IOException | RuntimeException error) {
                imagensSolicitadas.remove(requestKey);
                if (!activityDestroyed) runOnUiThread(() -> {
                    if (activityDestroyed) return;
                    state.setText("Imagem indisponível agora. O plano continua salvo. " + mensagemAmigavel(error));
                    retryButton.setText("Tentar gerar imagem novamente");
                    retryButton.setEnabled(true);
                });
            }
        });
    }

    private void mostrarPlanosSalvos() {
        List<LocalWorkoutStore.PlanRecord> plans = store.listPlans();
        if (plans.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Meus planos")
                    .setMessage("Você ainda não salvou nenhum plano. Gere um plano para começar.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        ScrollView scroll = new ScrollView(this);
        LinearLayout items = new LinearLayout(this);
        items.setOrientation(LinearLayout.VERTICAL);
        items.setPadding(dp(12), dp(8), dp(12), dp(8));
        scroll.addView(items);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Planos salvos neste aparelho")
                .setView(scroll).setNegativeButton("Fechar", null).create();
        for (LocalWorkoutStore.PlanRecord plan : plans) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            TextView label = new TextView(this);
            label.setText(plan.title + "\n" + plan.objective + " · " + formatDate(plan.createdAt));
            label.setTextSize(15);
            row.addView(label);
            LinearLayout actions = new LinearLayout(this);
            Button open = new Button(this);
            open.setText("Abrir");
            Button delete = new Button(this);
            delete.setText("Excluir");
            actions.addView(open, new LinearLayout.LayoutParams(0, dp(48), 1));
            actions.addView(delete, new LinearLayout.LayoutParams(0, dp(48), 1));
            row.addView(actions);
            items.addView(row);
            open.setOnClickListener(v -> {
                try {
                    JSONObject loaded = parsePlan(plan.json);
                    mostrarPlano(loaded.toString(), plan.id, false);
                    dialog.dismiss();
                    Toast.makeText(this, "Plano carregado.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Não foi possível abrir este plano salvo.", Toast.LENGTH_LONG).show();
                }
            });
            delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Excluir plano?").setMessage("As ilustrações e sessões relacionadas também serão removidas.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir", (d, w) -> {
                        store.deletePlan(plan.id);
                        if (currentPlanId == plan.id) {
                            currentPlanId = -1L;
                            resultadoCompleto = "";
                            abasTreinos.setVisibility(View.GONE);
                            abasTreinos.removeAllTabs();
                            paginasTreino.clear();
                            areaTreino.removeAllViews();
                            resultadoTexto.setText("Plano excluído.");
                            observacoesTexto.setText("");
                            statusTexto.setText("");
                            copiarButton.setEnabled(false);
                        }
                        dialog.dismiss();
                        Toast.makeText(this, "Plano excluído.", Toast.LENGTH_SHORT).show();
                    }).show());
        }
        dialog.show();
    }

    private void mostrarHistorico() {
        List<LocalWorkoutStore.SessionRecord> sessions = store.listSessions();
        if (sessions.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Histórico")
                    .setMessage("Nenhuma sessão registrada ainda. Abra um plano e toque em Iniciar sessão.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        StringBuilder text = new StringBuilder();
        for (LocalWorkoutStore.SessionRecord session : sessions) {
            text.append(session.completed ? "✓ " : "◷ ").append(session.title)
                    .append("\nInício: ").append(formatDateTime(session.startedAt));
            if (session.finishedAt != null) {
                long minutes = Math.max(0, (session.finishedAt - session.startedAt) / 60000L);
                text.append(" · ").append(minutes).append(" min");
            } else text.append(" · Em andamento");
            text.append("\n\n");
        }
        ScrollView scroll = new ScrollView(this);
        TextView content = new TextView(this);
        content.setText(text.toString());
        content.setTextSize(15);
        content.setPadding(dp(16), dp(8), dp(16), dp(8));
        scroll.addView(content);
        new AlertDialog.Builder(this).setTitle("Histórico de sessões")
                .setView(scroll).setPositiveButton("Fechar", null).show();
    }

    private String formatDate(long timestamp) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("pt", "BR"))
                .format(new Date(timestamp));
    }

    private String formatDateTime(long timestamp) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT,
                new Locale("pt", "BR")).format(new Date(timestamp));
    }

    private String mensagemAmigavel(Throwable error) {
        String detail = error == null ? "" : error.getMessage();
        if (detail == null || detail.trim().isEmpty()) return "Verifique a conexão e tente novamente.";
        String lower = detail.toLowerCase(Locale.ROOT);
        if (lower.contains("http 503") || lower.contains("unavailable") || lower.contains("high demand"))
            return "O serviço de IA está ocupado. Aguarde alguns segundos e tente novamente.";
        if (lower.contains("http 429") || lower.contains("quota") || lower.contains("rate limit"))
            return "O limite temporário da API foi atingido. Aguarde ou verifique a cota da chave Gemini.";
        if (lower.contains("api key") || lower.contains("api_key") || lower.contains("unauthorized"))
            return "A chave Gemini não foi aceita. Configure uma chave válida nas propriedades locais do projeto.";
        return detail;
    }

    private void copiarResultado() {
        if (resultadoCompleto == null || resultadoCompleto.trim().isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Plano de treino", resultadoCompleto));
            Toast.makeText(this, "Plano copiado.", Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        activityDestroyed = true;
        generationToken++;
        executor.shutdownNow();
        imageExecutor.shutdownNow();
        if (store != null) store.close();
        super.onDestroy();
    }
}