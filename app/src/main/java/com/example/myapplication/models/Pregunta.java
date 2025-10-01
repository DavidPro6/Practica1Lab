package com.example.myapplication.models;

import java.util.List;

public class Pregunta {
    private String textoPregunta;
    private List<Respuesta> opciones;
    private int respuestaCorrectaIndex;
    private TipoPregunta tipo;

    public enum TipoPregunta {
        TEXTO_RADIOBUTTON, // 4 opciones de texto
        IMAGEN_GRID,      // 4 opciones de imagen
        TEXTO_LISTVIEW,   // Múltiples opciones de texto en una lista
        TEXTO_SPINNER     // Múltiples opciones de texto en un desplegable
    }

    public Pregunta(String textoPregunta, List<Respuesta> opciones, int respuestaCorrectaIndex, TipoPregunta tipo) {
        this.textoPregunta = textoPregunta;
        this.opciones = opciones;
        this.respuestaCorrectaIndex = respuestaCorrectaIndex;
        this.tipo = tipo;
    }

    public String getTextoPregunta() { return textoPregunta; }
    public List<Respuesta> getOpciones() { return opciones; }
    public int getRespuestaCorrectaIndex() { return respuestaCorrectaIndex; }
    public TipoPregunta getTipo() { return tipo; }
}
