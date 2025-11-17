package com.example.myapplication.models;

import android.provider.BaseColumns;

public final class QuizBD {

    private QuizBD() {}

    // Tabla de Preguntas (table_name=preguntas)
    public static class PreguntasTable implements BaseColumns {
        public static final String TABLE_NAME = "pregunta";
        public static final String COLUMN_ENUNCIADO = "enunciado"; // enunciado(text, not null)
    }

    // Tabla de Respuestas (table_name=respuestas)
    public static class RespuestasTable implements BaseColumns {
        public static final String TABLE_NAME = "respuesta";
        public static final String COLUMN_RESPUESTA = "respuesta";     // respuesta(text, not null)
        public static final String COLUMN_CORRECTA = "correcta";       // correcta(bool, not null) -> se almacenará como INTEGER 0 o 1
        public static final String COLUMN_PREGUNTA_ID = "pregunta_id"; // pregunta_id(int) -> Foreign Key
    }
}