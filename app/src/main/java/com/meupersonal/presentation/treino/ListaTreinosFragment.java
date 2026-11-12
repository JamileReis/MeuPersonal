package com.meupersonal.presentation.treino;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.meupersonal.R;
import com.meupersonal.data.LocalWorkoutStore;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public final class ListaTreinosFragment extends Fragment {
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
            card.setClickable(true);
            card.setFocusable(true);

            card.addView(createText(plan.title, 17, 0xFFF4F5F2, true));
            card.addView(createText(plan.objective, 14, 0xFFB7F36B, false));
            card.addView(createText(
                    "Criado em " + dateFormat.format(new Date(plan.createdAt)),
                    12,
                    0xFFA8B0A8,
                    false));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dp(10);
            card.setLayoutParams(params);
            card.setOnClickListener(v -> openPlan(plan.id));
            list.addView(card);
        }
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
}
