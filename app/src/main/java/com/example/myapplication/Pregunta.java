package com.example.myapplication;

import java.util.List;

public class Pregunta {
    private String textoPregunta;
    private List<String> opciones;
    private List<String> rutaImagenOpciones; // O podrías usar List<Integer> para IDs de drawable
    private int respuestaCorrectaIndex;
    private TipoPregunta tipoPregunta;

    public Pregunta(String textoPregunta, List<String> opciones, List<String> rutaImagenOpciones, int respuestaCorrectaIndex, TipoPregunta tipoPregunta) {
        this.textoPregunta = textoPregunta;
        this.opciones = opciones;
        this.rutaImagenOpciones = rutaImagenOpciones;
        this.respuestaCorrectaIndex = respuestaCorrectaIndex;
        this.tipoPregunta = tipoPregunta;
    }
    // Getters
    public String getTextoPregunta() {
        return textoPregunta;
    }
    public List<String> getOpciones() {
        return opciones;
    }
    public int getRespuestaCorrectaIndex() {
        return respuestaCorrectaIndex;
    }
    public List<String> getRutaImagenOpciones() {
        return rutaImagenOpciones;
    }
    public TipoPregunta getTipoPregunta() {
        return tipoPregunta;
    }
}
