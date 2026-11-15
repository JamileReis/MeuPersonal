package com.meupersonal.presentation.historicoFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.meupersonal.R;
import com.meupersonal.data.LocalWorkoutStore;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
public class HistoricoFragment extends Fragment {

    private LocalWorkoutStore store;
    private LinearLayout container;
    private TextView empty;
    private TextView summary;

    public HistoricoFragment() {
        super(R.layout.fragment_historico);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        store = new LocalWorkoutStore(requireContext());
        container = view.findViewById(R.id.containerHistorico);
        empty = view.findViewById(R.id.textHistoricoVazio);
        summary = view.findViewById(R.id.textResumoHistorico);

        loadHistory();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (store != null) {
            loadHistory();
        }
    }

    private void loadHistory() {
        container.removeAllViews();

        List<LocalWorkoutStore.SessionRecord> sessions =
                store.listFinishedSessions();

        int total = sessions == null ? 0 : sessions.size();

        summary.setText(total + (total == 1
                ? " treino concluído"
                : " treinos concluídos"));

        empty.setVisibility(total == 0 ? View.VISIBLE : View.GONE);

        if (sessions == null) {
            return;
        }

        DateFormat format = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT
        );

        for (LocalWorkoutStore.SessionRecord session : sessions) {
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(18), dp(16), dp(18), dp(16));
            card.setBackgroundColor(0xFF202720);

            TextView title = createText(
                    session.title,
                    17,
                    0xFFF4F5F2,
                    true
            );

            TextView date = createText(
                    format.format(new Date(session.finishedAt)),
                    13,
                    0xFFA8B0A8,
                    false
            );

            card.addView(title);
            card.addView(date);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.bottomMargin = dp(12);
            card.setLayoutParams(params);

            container.addView(card);
        }
    }

    private TextView createText(
            String value,
            float size,
            int color,
            boolean bold
    ) {
        TextView text = new TextView(requireContext());
        text.setText(value == null ? "" : value);
        text.setTextSize(size);
        text.setTextColor(color);

        if (bold) {
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        return text;
    }

    private int dp(int value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }
}