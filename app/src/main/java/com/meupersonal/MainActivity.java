package com.meupersonal;

import static com.meupersonal.service.ia.GeminiAiService.API_KEY;

import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meupersonal.service.ia.GeminiAiService;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText preferenciasInput;
    private EditText diasInput;
    private EditText duracaoInput;
    private EditText restricoesInput;
    private Spinner objetivoSpinner;
    private Spinner nivelSpinner;
    private TextView resultadoTexto;
    private Button gerarButton;
    private Button copiarButton;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private GeminiAiService geminiAiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        geminiAiService = new GeminiAiService(API_KEY);

        construirInterface();
    }

    private void construirInterface() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(20), dp(24), dp(20), dp(24));

        scrollView.addView(conteudo);

        TextView titulo = new TextView(this);
        titulo.setText("MeuPersonal");
        titulo.setTextSize(28);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setTextColor(0xFFC45431);
        conteudo.addView(titulo);

        TextView subtitulo = new TextView(this);
        subtitulo.setText("Gere seu plano de treino personalizado");
        subtitulo.setTextSize(16);
        subtitulo.setPadding(0, dp(8), 0, dp(24));
        conteudo.addView(subtitulo);

        adicionarRotulo(conteudo, "Objetivo");

        objetivoSpinner = criarSpinner(new String[]{
                "Hipertrofia",
                "Emagrecimento",
                "Condicionamento físico",
                "Força",
                "Resistência muscular",
                "Saúde e bem-estar"
        });
        conteudo.addView(objetivoSpinner);

        adicionarRotulo(conteudo, "Nível de experiência");

        nivelSpinner = criarSpinner(new String[]{
                "Iniciante",
                "Intermediário",
                "Avançado"
        });
        nivelSpinner.setSelection(1);
        conteudo.addView(nivelSpinner);

        adicionarRotulo(conteudo, "Dias de treino por semana");

        diasInput = criarCampo("Ex.: 4", "4");
        diasInput.setInputType(2);
        conteudo.addView(diasInput);

        adicionarRotulo(conteudo, "Duração de cada sessão (minutos)");

        duracaoInput = criarCampo("Ex.: 45", "45");
        duracaoInput.setInputType(2);
        conteudo.addView(duracaoInput);

        adicionarRotulo(conteudo, "Preferências de treino");

        preferenciasInput = criarCampo(
                "Ex.: musculação, exercícios livres, cardio",
                ""
        );
        conteudo.addView(preferenciasInput);

        adicionarRotulo(conteudo, "Equipamentos, restrições ou limitações");

        restricoesInput = criarCampo(
                "Informe os equipamentos disponíveis e as limitações relevantes",
                ""
        );
        restricoesInput.setMinLines(3);
        restricoesInput.setGravity(
                android.view.Gravity.TOP | android.view.Gravity.START
        );
        conteudo.addView(restricoesInput);

        gerarButton = new Button(this);
        gerarButton.setText("Gerar plano com IA");
        LinearLayout.LayoutParams parametrosBotao =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        parametrosBotao.topMargin = dp(20);
        conteudo.addView(gerarButton, parametrosBotao);

        TextView tituloResultado = new TextView(this);
        tituloResultado.setText("Plano gerado");
        tituloResultado.setTextSize(20);
        tituloResultado.setTypeface(null, Typeface.BOLD);
        tituloResultado.setPadding(0, dp(24), 0, dp(8));
        conteudo.addView(tituloResultado);

        resultadoTexto = new TextView(this);
        resultadoTexto.setText(
                "Preencha os dados acima e toque em Gerar plano com IA."
        );
        resultadoTexto.setTextSize(15);
        resultadoTexto.setTextIsSelectable(true);
        resultadoTexto.setPadding(dp(12), dp(12), dp(12), dp(12));
        resultadoTexto.setBackgroundColor(0xFFF3F1EC);
        conteudo.addView(resultadoTexto);

        copiarButton = new Button(this);
        copiarButton.setText("Copiar resultado");
        copiarButton.setEnabled(false);
        conteudo.addView(copiarButton);

        gerarButton.setOnClickListener(v -> gerarPlano());
        copiarButton.setOnClickListener(v -> copiarResultado());

        setContentView(scrollView);
    }

    private void adicionarRotulo(
            LinearLayout conteudo,
            String texto
    ) {
        TextView rotulo = new TextView(this);
        rotulo.setText(texto);
        rotulo.setTextSize(15);
        rotulo.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        parametros.topMargin = dp(16);
        parametros.bottomMargin = dp(6);

        conteudo.addView(rotulo, parametros);
    }

    private Spinner criarSpinner(String[] opcoes) {
        Spinner spinner = new Spinner(this);

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcoes
        );

        adaptador.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adaptador);
        return spinner;
    }

    private EditText criarCampo(
            String dica,
            String valorInicial
    ) {
        EditText campo = new EditText(this);
        campo.setHint(dica);
        campo.setText(valorInicial);
        campo.setSingleLine(false);
        campo.setPadding(dp(12), dp(10), dp(12), dp(10));
        return campo;
    }

    private void gerarPlano() {
        String dias = diasInput.getText().toString().trim();
        String duracao = duracaoInput.getText().toString().trim();

        if (dias.isEmpty() || duracao.isEmpty()) {
            Toast.makeText(
                    this,
                    "Informe os dias e a duração do treino.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int quantidadeDias;
        int minutos;

        try {
            quantidadeDias = Integer.parseInt(dias);
            minutos = Integer.parseInt(duracao);
        } catch (NumberFormatException erro) {
            Toast.makeText(
                    this,
                    "Informe números válidos.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (quantidadeDias < 1 || quantidadeDias > 7
                || minutos < 10 || minutos > 240) {
            Toast.makeText(
                    this,
                    "Use de 1 a 7 dias e de 10 a 240 minutos.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String objetivo = objetivoSpinner.getSelectedItem().toString();
        String nivel = nivelSpinner.getSelectedItem().toString();
        String preferencias = preferenciasInput.getText().toString().trim();
        String restricoes = restricoesInput.getText().toString().trim();

        String prompt = criarPrompt(
                objetivo,
                nivel,
                quantidadeDias,
                minutos,
                preferencias,
                restricoes
        );

        gerarButton.setEnabled(false);
        gerarButton.setText("Gerando...");
        copiarButton.setEnabled(false);
        resultadoTexto.setText(
                "A IA está preparando seu plano. Aguarde..."
        );

        executor.execute(() -> {
            try {
                String resultado = geminiAiService.sendPrompt(prompt);

                runOnUiThread(() -> {
                    resultadoTexto.setText(resultado);
                    gerarButton.setEnabled(true);
                    gerarButton.setText("Gerar plano com IA");
                    copiarButton.setEnabled(true);
                });

            } catch (IOException | RuntimeException erro) {
                runOnUiThread(() -> {
                    resultadoTexto.setText(
                            "Não foi possível gerar o plano.\n\n"
                                    + erro.getMessage()
                    );
                    gerarButton.setEnabled(true);
                    gerarButton.setText("Tentar novamente");
                });
            }
        });
    }

    private String criarPrompt(
            String objetivo,
            String nivel,
            int dias,
            int minutos,
            String preferencias,
            String restricoes
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "Você é o assistente de treinamento do MeuPersonal. "
                        + "Crie uma proposta de treino personalizada em português do Brasil.\n\n"
        );

        prompt.append("Objetivo: ").append(objetivo).append('\n');
        prompt.append("Experiência: ").append(nivel).append('\n');
        prompt.append("Frequência semanal: ").append(dias).append(" dias\n");
        prompt.append("Duração por sessão: ").append(minutos).append(" minutos\n");
        prompt.append("Preferências: ")
                .append(preferencias.isEmpty()
                        ? "Não informadas"
                        : preferencias)
                .append('\n');

        prompt.append("Equipamentos e restrições: ")
                .append(restricoes.isEmpty()
                        ? "Não informados"
                        : restricoes)
                .append("\n\n");

        prompt.append(
                "Retorne somente um JSON válido, sem Markdown, com esta estrutura: "
                        + "{\"titulo\":\"\",\"objetivo\":\"\",\"observacoes\":\"\","
                        + "\"dias\":[{\"dia\":\"\",\"nome\":\"\",\"duracaoMinutos\":0,"
                        + "\"exercicios\":[{\"nome\":\"\",\"grupoMuscular\":\"\","
                        + "\"series\":0,\"repeticoes\":\"\",\"descansoSegundos\":0,"
                        + "\"instrucao\":\"\"}]}]}\n"
        );

        prompt.append(
                "Gere exatamente a quantidade de sessões solicitada. "
                        + "Inclua aquecimento e orientações de execução. "
                        + "Não invente informações de saúde. "
                        + "Se forem relatadas dores, lesões ou condições clínicas, "
                        + "oriente a avaliação de profissional habilitado e não prescreva "
                        + "exercícios potencialmente inadequados."
        );

        return prompt.toString();
    }

    private void copiarResultado() {
        String resultado = resultadoTexto.getText().toString();

        if (resultado.trim().isEmpty()) {
            return;
        }

        ClipboardManager areaTransferencia =
                (ClipboardManager) getSystemService(
                        Context.CLIPBOARD_SERVICE
                );

        if (areaTransferencia != null) {
            areaTransferencia.setPrimaryClip(
                    ClipData.newPlainText(
                            "Plano de treino",
                            resultado
                    )
            );

            Toast.makeText(
                    this,
                    "Resultado copiado.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private int dp(int valor) {
        return (int) (
                valor * getResources()
                        .getDisplayMetrics().density
        );
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}