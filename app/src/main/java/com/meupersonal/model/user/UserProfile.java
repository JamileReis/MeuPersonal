package com.meupersonal.model.user;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "perfis_usuarios",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "usuario_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "usuario_id", unique = true)
)
public class UserProfile extends BaseEntity {

    @ColumnInfo(name = "usuario_id")
    private long usuarioId;

    @ColumnInfo(name = "idade")
    private Integer idade;

    @ColumnInfo(name = "peso")
    private Double peso;

    @ColumnInfo(name = "altura")
    private Double altura;

    @ColumnInfo(name = "genero")
    private Gender genero;

    @ColumnInfo(name = "nivel_atividade")
    private ActivityLevel nivelAtividade;

    @ColumnInfo(name = "nivel_experiencia")
    private ExperienceLevel nivelExperiencia;

    public UserProfile(long usuarioId, Integer idade, Double altura, Double peso, Gender genero, ActivityLevel nivelAtividade, ExperienceLevel nivelExperiencia) {
        this.usuarioId = usuarioId;
        this.idade = idade;
        this.altura = altura;
        this.peso = peso;
        this.genero = genero;
        this.nivelAtividade = nivelAtividade;
        this.nivelExperiencia = nivelExperiencia;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public Gender getGenero() {
        return genero;
    }

    public void setGenero(Gender genero) {
        this.genero = genero;
    }

    public ActivityLevel getNivelAtividade() {
        return nivelAtividade;
    }

    public void setNivelAtividade(ActivityLevel nivelAtividade) {
        this.nivelAtividade = nivelAtividade;
    }

    public ExperienceLevel getNivelExperiencia() {
        return nivelExperiencia;
    }

    public void setNivelExperiencia(ExperienceLevel nivelExperiencia) {
        this.nivelExperiencia = nivelExperiencia;
    }
}
