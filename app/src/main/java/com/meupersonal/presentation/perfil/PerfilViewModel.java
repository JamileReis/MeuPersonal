package com.meupersonal.presentation.perfil;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
public final class PerfilViewModel extends AndroidViewModel {

    private static final String PREFS = "meu_personal_profile";

    private final SharedPreferences preferences;
    private final MutableLiveData<Perfil> perfil = new MutableLiveData<>();

    public PerfilViewModel(@NonNull Application application) {
        super(application);
        preferences = application.getSharedPreferences(PREFS, Application.MODE_PRIVATE);
        carregarPerfil();
    }

    public LiveData<Perfil> getPerfil() {
        return perfil;
    }

    public void carregarPerfil() {
        perfil.setValue(new Perfil(
                preferences.getString("nome", ""),
                preferences.getInt("idade", 0),
                preferences.getString("peso", ""),
                preferences.getString("altura", ""),
                preferences.getString("objetivo", ""),
                preferences.getString("limitacoes", ""),
                preferences.getString("preferencias", "")
        ));
    }

    public boolean salvarPerfil(Perfil dados) {
        if (dados == null || dados.nome.trim().isEmpty()) {
            return false;
        }

        boolean saved = preferences.edit()
                .putString("nome", dados.nome.trim())
                .putInt("idade", dados.idade)
                .putString("peso", dados.peso)
                .putString("altura", dados.altura)
                .putString("objetivo", dados.objetivo)
                .putString("limitacoes", dados.limitacoes)
                .putString("preferencias", dados.preferencias)
                .commit();

        if (saved) {
            carregarPerfil();
        }

        return saved;
    }

    public static final class Perfil {
        public final String nome;
        public final int idade;
        public final String peso;
        public final String altura;
        public final String objetivo;
        public final String limitacoes;
        public final String preferencias;

        public Perfil(
                String nome,
                int idade,
                String peso,
                String altura,
                String objetivo,
                String limitacoes,
                String preferencias
        ) {
            this.nome = nome;
            this.idade = idade;
            this.peso = peso;
            this.altura = altura;
            this.objetivo = objetivo;
            this.limitacoes = limitacoes;
            this.preferencias = preferencias;
        }
    }
}