package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.models.Pregunta;
import com.example.myapplication.models.QuizBD.*;
import com.example.myapplication.models.Respuesta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "QuizApp.db";
    private static final int DATABASE_VERSION = 1;

    public QuizDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de preguntas
        final String SQL_CREATE_PREGUNTAS_TABLE = "CREATE TABLE " +
                PreguntasTable.TABLE_NAME + " ( " +
                PreguntasTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PreguntasTable.COLUMN_ENUNCIADO + " TEXT NOT NULL" +
                ")";

        // Crear tabla de respuestas
        final String SQL_CREATE_RESPUESTAS_TABLE = "CREATE TABLE " +
                RespuestasTable.TABLE_NAME + " ( " +
                RespuestasTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RespuestasTable.COLUMN_RESPUESTA + " TEXT NOT NULL, " +
                RespuestasTable.COLUMN_CORRECTA + " INTEGER NOT NULL, " +
                RespuestasTable.COLUMN_PREGUNTA_ID + " INTEGER, " +
                "FOREIGN KEY(" + RespuestasTable.COLUMN_PREGUNTA_ID + ") REFERENCES " +
                PreguntasTable.TABLE_NAME + "(" + PreguntasTable._ID + ") ON DELETE CASCADE" +
                ")";

        db.execSQL(SQL_CREATE_PREGUNTAS_TABLE);
        db.execSQL(SQL_CREATE_RESPUESTAS_TABLE);

        fillDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RespuestasTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PreguntasTable.TABLE_NAME);
        onCreate(db);
    }

    private void fillDatabase(SQLiteDatabase db) {
        addPreguntaConRespuestas(db,
                "¿Qué significa el acrónimo 'RM' en el contexto del levantamiento de pesas?",
                "Repetición Máxima", // Respuesta correcta
                "Ritmo Muscular", "Resistencia Mínima", "Rango de Movimiento");

        addPreguntaConRespuestas(db,
                "¿Cuál de estos ejercicios se enfoca principalmente en los hombros?",
                "El press militar", // Respuesta correcta
                "El peso muerto", "Las zancadas (Lunges)", "El remo con barra");

        addPreguntaConRespuestas(db,
                "Un entrenamiento de tipo 'HIIT' se caracteriza por ser...",
                "Intervalos de alta intensidad", // Respuesta correcta
                "De larga duración y baja intensidad", "Exclusivamente con peso corporal", "Enfocado en la flexibilidad");

        addPreguntaConRespuestas(db,
                "En una rutina 'Push/Pull/Legs' (PPL), ¿qué ejercicio correspondería al día de 'Pull' (Tirón)?",
                "Dominadas (Pull-ups)", // Respuesta correcta
                "Press de banca (Bench Press)", "Sentadillas (Squats)", "Fondos en paralelas (Dips)");

        addPreguntaConRespuestas(db,
                "¿Qué tipo de agarre implica tener las palmas de las manos mirando hacia ti?",
                "Supino", // Respuesta correcta
                "Prono", "Neutro", "Mixto");
    }

    private void addPreguntaConRespuestas(SQLiteDatabase db, String enunciado, String respuestaCorrecta, String... respuestasIncorrectas) {
        ContentValues preguntaCV = new ContentValues();
        preguntaCV.put(PreguntasTable.COLUMN_ENUNCIADO, enunciado);
        long preguntaId = db.insert(PreguntasTable.TABLE_NAME, null, preguntaCV);

        ContentValues respCorrectaCV = new ContentValues();
        respCorrectaCV.put(RespuestasTable.COLUMN_RESPUESTA, respuestaCorrecta);
        respCorrectaCV.put(RespuestasTable.COLUMN_CORRECTA, 1); // 1 para true
        respCorrectaCV.put(RespuestasTable.COLUMN_PREGUNTA_ID, preguntaId);
        db.insert(RespuestasTable.TABLE_NAME, null, respCorrectaCV);

        for (String respIncorrecta : respuestasIncorrectas) {
            ContentValues respIncorrectaCV = new ContentValues();
            respIncorrectaCV.put(RespuestasTable.COLUMN_RESPUESTA, respIncorrecta);
            respIncorrectaCV.put(RespuestasTable.COLUMN_CORRECTA, 0); // 0 para false
            respIncorrectaCV.put(RespuestasTable.COLUMN_PREGUNTA_ID, preguntaId);
            db.insert(RespuestasTable.TABLE_NAME, null, respIncorrectaCV);
        }
    }

    public List<Pregunta> getAllQuestions() {
        List<Pregunta> listaDePreguntas = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursorPreguntas = db.rawQuery("SELECT * FROM " + PreguntasTable.TABLE_NAME, null);

        if (cursorPreguntas.moveToFirst()) {
            do {
                int preguntaId = cursorPreguntas.getInt(cursorPreguntas.getColumnIndexOrThrow(PreguntasTable._ID));
                String enunciado = cursorPreguntas.getString(cursorPreguntas.getColumnIndexOrThrow(PreguntasTable.COLUMN_ENUNCIADO));

                List<Respuesta> opciones = new ArrayList<>();
                int indiceCorrecto = -1;

                Cursor cursorRespuestas = db.query(
                        RespuestasTable.TABLE_NAME,
                        null,
                        RespuestasTable.COLUMN_PREGUNTA_ID + " = ?",
                        new String[]{String.valueOf(preguntaId)},
                        null, null, "RANDOM()"
                );

                if (cursorRespuestas.moveToFirst()) {
                    int indiceActual = 0;
                    do {
                        String textoRespuesta = cursorRespuestas.getString(cursorRespuestas.getColumnIndexOrThrow(RespuestasTable.COLUMN_RESPUESTA));
                        int esCorrecta = cursorRespuestas.getInt(cursorRespuestas.getColumnIndexOrThrow(RespuestasTable.COLUMN_CORRECTA));

                        opciones.add(new Respuesta(textoRespuesta));
                        if (esCorrecta == 1) {
                            indiceCorrecto = indiceActual;
                        }
                        indiceActual++;
                    } while (cursorRespuestas.moveToNext());
                }
                cursorRespuestas.close();

                if (!opciones.isEmpty()) {
                    Pregunta pregunta = new Pregunta(enunciado, opciones, indiceCorrecto, Pregunta.TipoPregunta.TEXTO_RADIOBUTTON);
                    listaDePreguntas.add(pregunta);
                }

            } while (cursorPreguntas.moveToNext());
        }
        cursorPreguntas.close();

        Collections.shuffle(listaDePreguntas);
        return listaDePreguntas;
    }
}
