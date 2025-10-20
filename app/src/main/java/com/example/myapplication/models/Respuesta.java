package com.example.myapplication.models;

public class Respuesta {
    private String textoRespuesta;
    private int imagenRespuestaId;

    public Respuesta(String textoRespuesta) {
        this.textoRespuesta = textoRespuesta;
        this.imagenRespuestaId = 0;
    }

    public Respuesta(int imagenRespuestaId) {
        this.imagenRespuestaId = imagenRespuestaId;
        this.textoRespuesta = null;
    }

    public String getTextoRespuesta() { return textoRespuesta; }
    public int getImagenRespuestaId() { return imagenRespuestaId; }
    public boolean esDeImagen() { return imagenRespuestaId != 0; }
}
