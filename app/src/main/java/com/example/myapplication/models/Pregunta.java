package com.example.myapplication.models;

import java.util.List;

public class Pregunta {
    private String textoPregunta;
    private List<Respuesta> opciones;
    private int respuestaCorrectaIndex;
    private TipoPregunta tipo;
    private int imagenPreguntaId;

    public enum TipoPregunta {
        TEXTO_RADIOBUTTON,
        IMAGEN_GRID,
        TEXTO_LISTVIEW,
        TEXTO_SPINNER
        // Ya no necesitamos IMAGEN_TEXTO_LIBRE
    }


    public Pregunta(String textoPregunta, List<Respuesta> opciones, int respuestaCorrectaIndex, TipoPregunta tipo) {
        this(textoPregunta, opciones, respuestaCorrectaIndex, tipo, 0);
    }

    public Pregunta(String textoPregunta, List<Respuesta> opciones, int respuestaCorrectaIndex, TipoPregunta tipo, int imagenPreguntaId) {
        this.textoPregunta = textoPregunta;
        this.opciones = opciones;
        this.respuestaCorrectaIndex = respuestaCorrectaIndex;
        this.tipo = tipo;
        this.imagenPreguntaId = imagenPreguntaId;
    }
    public String getTextoPregunta() { return textoPregunta; }
    public List<Respuesta> getOpciones() { return opciones; }
    public int getRespuestaCorrectaIndex() { return respuestaCorrectaIndex; }
    public TipoPregunta getTipo() { return tipo; }
    public int getImagenPreguntaId() { return imagenPreguntaId; }
}
