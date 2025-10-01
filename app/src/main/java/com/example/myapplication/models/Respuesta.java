package com.example.myapplication.models;

public class Respuesta {
    private String textoRespuesta;
    private int imagenRespuestaId; // ID de un recurso drawable (ej: R.drawable.mi_imagen)

    public Respuesta(String textoRespuesta) {
        this.textoRespuesta = textoRespuesta;
        this.imagenRespuestaId = 0; // 0 indica que no hay imagen
    }

    public Respuesta(int imagenRespuestaId) {
        this.imagenRespuestaId = imagenRespuestaId;
        this.textoRespuesta = null; // null indica que no hay texto
    }

    public String getTextoRespuesta() { return textoRespuesta; }
    public int getImagenRespuestaId() { return imagenRespuestaId; }
    public boolean esDeImagen() { return imagenRespuestaId != 0; }
}
