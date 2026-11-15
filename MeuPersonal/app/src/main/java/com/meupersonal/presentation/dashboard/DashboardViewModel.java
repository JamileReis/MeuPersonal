package com.meupersonal.presentation.dashboard;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.meupersonal.data.LocalWorkoutStore;

public final class DashboardViewModel extends AndroidViewModel {

    private final MutableLiveData<DashboardState> state = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        carregarDashboard();
    }

    public LiveData<DashboardState> getState() {
        return state;
    }

    public void carregarDashboard() {
        Application application = getApplication();

        SharedPreferences preferences = application.getSharedPreferences(
                "meu_personal_profile",
                Application.MODE_PRIVATE
        );

        String nome = preferences.getString("nome", "");

        String objetivo = preferences.getString("objetivo", "");

        LocalWorkoutStore store = new LocalWorkoutStore(application);

        try {
            LocalWorkoutStore.PlanRecord plan = store.getLatestPlan();

            int treinosConcluidos = store.countCompletedSessionsLastDays(7);

            state.setValue(new DashboardState(
                    nome,
                    objetivo,
                    plan != null ? plan.title : "Nenhum treino disponível",
                    plan != null ? plan.objective : "Crie seu primeiro plano de treino",
                    treinosConcluidos,
                    plan != null
            ));
        } finally {
            store.close();
        }
    }

    public static final class DashboardState {
        public final String nome;
        public final String objetivo;
        public final String tituloPlano;
        public final String descricaoPlano;
        public final int treinosConcluidos;
        public final boolean possuiPlano;

        public DashboardState(
                String nome,
                String objetivo,
                String tituloPlano,
                String descricaoPlano,
                int treinosConcluidos,
                boolean possuiPlano
        ) {
            this.nome = nome;
            this.objetivo = objetivo;
            this.tituloPlano = tituloPlano;
            this.descricaoPlano = descricaoPlano;
            this.treinosConcluidos = treinosConcluidos;
            this.possuiPlano = possuiPlano;
        }
    }
}