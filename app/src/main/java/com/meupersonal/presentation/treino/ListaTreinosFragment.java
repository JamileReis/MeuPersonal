package com.meupersonal.presentation.treino;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.meupersonal.R;
import com.meupersonal.data.LocalWorkoutStore;
import com.meupersonal.data.auth.SecureSessionStore;
import com.meupersonal.model.auth.AuthSession;
import com.meupersonal.service.SupabaseDataService;
import com.meupersonal.service.treino.StarterWorkoutSeeder;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ListaTreinosFragment extends Fragment {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private LocalWorkoutStore store;
    private TextView description;
    private TextView empty;
    private LinearLayout list;

    public ListaTreinosFragment() {
        super(R.layout.fragment_lista_treinos);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);

        store = new LocalWorkoutStore(requireContext());
        StarterWorkoutSeeder.ensureAvailable(requireContext(), store);
        TextView title = view.findViewById(R.id.textTituloPlano);
        description = view.findViewById(R.id.textObservacoes);
        empty = view.findViewById(R.id.textVazio);
        list = view.findViewById(R.id.containerListaPlanos);
        Button create = view.findViewById(R.id.btnNovoTreino);

        title.setText("Seus planos de treino");
        create.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_lista_to_prompt));

        loadPlans();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (store != null && list != null) {
            loadPlans();
        }
    }

    private void loadPlans() {
        List<LocalWorkoutStore.PlanRecord> plans = store.listPlans();
        list.removeAllViews();

        description.setText(plans.size() + " plano(s) salvo(s)");
        boolean hasPlans = !plans.isEmpty();
        empty.setVisibility(hasPlans ? View.GONE : View.VISIBLE);
        list.setVisibility(hasPlans ? View.VISIBLE : View.GONE);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        for (LocalWorkoutStore.PlanRecord plan : plans) {
            if (plan == null) continue;

            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(18), dp(16), dp(18), dp(16));
            card.setBackgroundColor(0xFF202720);

            card.addView(createText(plan.title, 17, 0xFFF4F5F2, true));
            card.addView(createText(plan.objective, 14, 0xFFB7F36B, false));
            card.addView(createText(
                    "Criado em " + dateFormat.format(new Date(plan.createdAt)),
                    12,
                    0xFFA8B0A8,
                    false));

            LinearLayout actions = new LinearLayout(requireContext());
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setPadding(0, dp(12), 0, 0);

            Button open = new Button(requireContext());
            open.setText("Abrir treino");
            open.setAllCaps(false);
            open.setTextColor(0xFFB7F36B);
            open.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF303A30));
            open.setOnClickListener(v -> openPlan(plan.id));

            Button delete = new Button(requireContext());
            delete.setText("Excluir");
            delete.setAllCaps(false);
            delete.setTextColor(0xFFFFB4AB);
            delete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF3B2927));
            delete.setOnClickListener(v -> confirmDelete(plan));

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f);
            buttonParams.setMarginEnd(dp(8));
            actions.addView(open, buttonParams);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f);
            actions.addView(delete, deleteParams);
            card.addView(actions);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dp(10);
            card.setLayoutParams(params);
            list.addView(card);
        }
    }

    private void confirmDelete(LocalWorkoutStore.PlanRecord plan) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir plano de treino?")
                .setMessage("\"" + plan.title + "\" será removido junto com as imagens e sessões salvas neste aparelho. Essa ação não pode ser desfeita.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> deletePlan(plan))
                .show();
    }

    private void deletePlan(LocalWorkoutStore.PlanRecord plan) {
        if (!plan.remoteSynced || plan.remoteClientId == null
                || plan.remoteClientId.trim().isEmpty()) {
            store.deletePlan(plan.id);
            loadPlans();
            Toast.makeText(requireContext(), "Plano excluído.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthSession session = new SecureSessionStore(
                requireContext().getApplicationContext()).read();
        if (session == null) {
            Toast.makeText(requireContext(),
                    "Entre na sua conta para excluir também o plano sincronizado.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        executor.execute(() -> {
            try {
                new SupabaseDataService().deletePlan(session, plan.remoteClientId);
                store.deletePlan(plan.id);
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    loadPlans();
                    Toast.makeText(requireContext(), "Plano excluído.", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException | RuntimeException error) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            "Não foi possível excluir o plano sincronizado. Verifique a conexão e tente novamente.",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void openPlan(long planId) {
        Bundle args = new Bundle();
        args.putLong("plan_id", planId);

        NavController controller = NavHostFragment.findNavController(this);
        controller.navigate(R.id.action_lista_to_treino_dia, args);
    }

    private TextView createText(String value, float size, int color, boolean bold) {
        TextView text = new TextView(requireContext());
        text.setText(value == null || value.trim().isEmpty()
                ? "Não informado" : value);
        text.setTextSize(size);
        text.setTextColor(color);
        if (bold) text.setTypeface(null, android.graphics.Typeface.BOLD);
        return text;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
