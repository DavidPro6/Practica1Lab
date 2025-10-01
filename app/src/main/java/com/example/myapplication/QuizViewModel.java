package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuizViewModel extends ViewModel {

    // Para comunicar el resultado de la respuesta a la UI
    public enum ResultadoRespuesta {
        CORRECTA,
        INCORRECTA,
        JUEGO_TERMINADO // Para cuando no hay más preguntas
    }

    private MutableLiveData<Pregunta> preguntaActual = new MutableLiveData<>();
    private MutableLiveData<Integer> puntuacion = new MutableLiveData<>(0);
    private MutableLiveData<ResultadoRespuesta> ultimoResultado = new MutableLiveData<>();

    private List<Pregunta> todasLasPreguntas;
    private int indicePreguntaActual = 0;
    private final int PUNTOS_POR_ACIERTO = 3;
    private final int PUNTOS_POR_FALLO = -2; // Restar 2 puntos

    public QuizViewModel() {
        cargarPreguntas();
        siguientePregunta();
    }

    public LiveData<Pregunta> getPreguntaActual() {
        return preguntaActual;
    }

    public LiveData<Integer> getPuntuacion() {
        return puntuacion;
    }

    public LiveData<ResultadoRespuesta> getUltimoResultado() {
        return ultimoResultado;
    }

    private void cargarPreguntas() {
        todasLasPreguntas = new ArrayList<>();
        List<String> opciones1 = Arrays.asList("Press de banca", "Sentadilla", "Peso muerto", "Curl de bíceps");
        todasLasPreguntas.add(new Pregunta("¿Cuál de estos ejercicios trabaja principalmente los cuádriceps?", opciones1, null, 1, TipoPregunta.TEXTO_SELECCION));

        List<String> opciones2 = Arrays.asList("Glúteos", "Hombros", "Tríceps", "Abdominales");
        todasLasPreguntas.add(new Pregunta("¿Qué músculo principal trabaja el 'Hip Thrust'?", opciones2, null, 0, TipoPregunta.TEXTO_SELECCION));
        // Pregunta 3: FOTOS_SELECCION (necesitarás imágenes en drawable y la lógica en QuizActivity)
        List<String> imagenesP3 = Arrays.asList("nombre_img1", "nombre_img2", "nombre_img3", "nombre_img4"); // Nombres de tus drawables (sin extensión)
        todasLasPreguntas.add(new Pregunta("¿Cuál de estas imágenes muestra una 'flexión diamante'?", null, imagenesP3, 0, TipoPregunta.FOTOS_SELECCION));

        // Pregunta 4: MENU_DESPLEGABLE (necesitarás la lógica en QuizActivity para el Spinner)
        List<String> opciones4 = Arrays.asList("Proteína", "Creatina", "BCAAs", "Pre-entreno");
        todasLasPreguntas.add(new Pregunta("¿Qué suplemento es conocido por ayudar a la recuperación muscular y el crecimiento?", opciones4, null, 0, TipoPregunta.MENU_DESPLEGABLE));

        // Pregunta 5: Texto Selección (ejemplo)
        List<String> opciones5 = Arrays.asList("1-2 veces", "3-5 veces", "6-7 veces", "Solo fines de semana");
        todasLasPreguntas.add(new Pregunta("¿Con qué frecuencia semanal se recomienda entrenar para hipertrofia (principiantes-intermedios)?", opciones5, null, 1, TipoPregunta.TEXTO_SELECCION));
    }

    public void respuestaSeleccionada(int indiceRespuesta) {
        Pregunta currentPreg = preguntaActual.getValue();
        if (currentPreg == null) return; // No debería pasar si el juego no ha terminado

        int puntosActuales = puntuacion.getValue() != null ? puntuacion.getValue() : 0;

        if (currentPreg.getRespuestaCorrectaIndex() == indiceRespuesta) {
            puntuacion.setValue(puntosActuales + PUNTOS_POR_ACIERTO);
            ultimoResultado.setValue(ResultadoRespuesta.CORRECTA);
        } else {
            puntuacion.setValue(puntosActuales + PUNTOS_POR_FALLO);
            ultimoResultado.setValue(ResultadoRespuesta.INCORRECTA);
        }
        // La decisión de "continuar" o "empezar de nuevo" tras un error
        // se manejará en la UI, pero el ViewModel avanzará o terminará.
    }

    public void avanzarAlSiguienteOPausa() {
        // Este método se llamará desde la UI después de mostrar el feedback de la respuesta
        if (indicePreguntaActual < todasLasPreguntas.size()) {
            preguntaActual.setValue(todasLasPreguntas.get(indicePreguntaActual));
            indicePreguntaActual++;
            // Reseteamos ultimoResultado para que no se dispare de nuevo al observar
            // si el usuario rota la pantalla o algo similar antes de la siguiente acción.
            // Opcionalmente, puedes tener un evento de una sola vez (SingleLiveEvent).
            ultimoResultado.setValue(null);
        } else {
            // Fin del juego
            preguntaActual.setValue(null); // Indicar que no hay más preguntas
            ultimoResultado.setValue(ResultadoRespuesta.JUEGO_TERMINADO);
        }
    }

    public void reiniciarJuego() {
        puntuacion.setValue(0);
        indicePreguntaActual = 0;
        ultimoResultado.setValue(null);
        cargarPreguntas(); // Si las preguntas pudieran cambiar o necesitas resetearlas
        siguientePregunta(); // Carga la primera pregunta
    }

    // Método para cargar la primera pregunta, llamado también en reiniciarJuego
    private void siguientePregunta() {
        if (!todasLasPreguntas.isEmpty()) {
            indicePreguntaActual = 0; // Siempre empezamos desde la primera pregunta
            preguntaActual.setValue(todasLasPreguntas.get(indicePreguntaActual));
            indicePreguntaActual++; // Preparamos el índice para la *siguiente* llamada a avanzarAlSiguienteOPausa
        } else {
            preguntaActual.setValue(null);
            ultimoResultado.setValue(ResultadoRespuesta.JUEGO_TERMINADO); // No hay preguntas
        }
    }
}
