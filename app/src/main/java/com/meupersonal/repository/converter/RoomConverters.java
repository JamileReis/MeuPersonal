package com.meupersonal.repository.converter;

import android.os.Build;

import androidx.room.TypeConverter;

import com.meupersonal.model.questionario.GoalType;
import com.meupersonal.model.treino.MuscleGroup;
import com.meupersonal.model.treino.PlanStatus;

import java.time.DayOfWeek;
import java.util.Date;
public interface RoomConverters {
    @TypeConverter
    public static String diaDaSemanaParaTexto(DayOfWeek dia) {
        return dia == null ? null : dia.name();
    }

    @TypeConverter
    public static DayOfWeek textoParaDiaDaSemana(String dia) {
        if (dia == null) {
            return null;
        }

        for (DayOfWeek diaSemana : DayOfWeek.values()) {
            if (diaSemana.name().equals(dia)) {
                return diaSemana;
            }
        }

        return null;
    }

    @TypeConverter
    public static String grupoMuscularParaTexto(MuscleGroup grupo) {
        return grupo == null ? null : grupo.name();
    }

    @TypeConverter
    public static MuscleGroup textoParaGrupoMuscular(String grupo) {
        return grupo == null ? null : MuscleGroup.valueOf(grupo);
    }

    @TypeConverter
    public static String objetivoParaTexto(GoalType objetivo) {
        return objetivo == null ? null : objetivo.name();
    }

    @TypeConverter
    public static GoalType textoParaObjetivo(String objetivo) {
        return objetivo == null ? null : GoalType.valueOf(objetivo);
    }

    @TypeConverter
    public static String statusParaTexto(PlanStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static PlanStatus textoParaStatus(String status) {
        return status == null ? null : PlanStatus.valueOf(status);
    }

    @TypeConverter
    public static Long dataParaTimestamp(Date data) {
        return data == null ? null : data.getTime();
    }

    @TypeConverter
    public static Date timestampParaData(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}
