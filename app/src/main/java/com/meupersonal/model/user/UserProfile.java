package com.meupersonal.model.user;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
}
