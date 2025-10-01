package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.models.Pregunta;
import com.example.myapplication.models.Respuesta; // <<< Asegúrate de importar Respuesta

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuizViewModel extends ViewModel {

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


        java.util.function.Function<List<String>, List<Respuesta>> convertirOpciones =
                opcionesStr -> opcionesStr.stream().map(Respuesta::new).collect(Collectors.toList());

        List<String> opcionesStr1 = Arrays.asList("Press de banca", "Sentadilla", "Peso muerto", "Curl de bíceps");
        todasLasPreguntas.add(new Pregunta(
                "¿Cuál de estos ejercicios trabaja principalmente los cuádriceps?",
                convertirOpciones.apply(opcionesStr1),
                1, // Índice de la respuesta correcta ("Sentadilla")
                Pregunta.TipoPregunta.TEXTO_RADIOBUTTON));

        List<String> opcionesStr2 = Arrays.asList("Glúteos", "Hombros", "Tríceps", "Abdominales");
        todasLasPreguntas.add(new Pregunta(
                "¿Qué músculo principal trabaja el 'Hip Thrust'?",
                convertirOpciones.apply(opcionesStr2),
                0, // Índice de la respuesta correcta ("Glúteos")
                Pregunta.TipoPregunta.TEXTO_RADIOBUTTON));



        /*
        List<Respuesta> opcionesImg3 = Arrays.asList(
                new Respuesta(R.drawable.flexion_diamante), // Correcta
                new Respuesta(R.drawable.flexion_normal),
                new Respuesta(R.drawable.press_banca),
                new Respuesta(R.drawable.sentadilla)
        );
        todasLasPreguntas.add(new Pregunta(
                "¿Cuál de estas imágenes muestra una 'flexión diamante'?",
                opcionesImg3,
                0,
                Pregunta.TipoPregunta.IMAGEN_GRID));
        */

        List<String> opcionesStr4 = Arrays.asList("Proteína", "Creatina", "BCAAs", "Pre-entreno");
        todasLasPreguntas.add(new Pregunta(
                "¿Qué suplemento es conocido por ayudar a la recuperación muscular y el crecimiento?",
                convertirOpciones.apply(opcionesStr4),
                0, // Índice de la respuesta correcta ("Proteína")
                Pregunta.TipoPregunta.TEXTO_SPINNER));

        List<String> opcionesStr5 = Arrays.asList("1-2 veces", "3-5 veces", "6-7 veces", "Solo fines de semana");
        todasLasPreguntas.add(new Pregunta(
                "¿Con qué frecuencia semanal se recomienda entrenar para hipertrofia (principiantes-intermedios)?",
                convertirOpciones.apply(opcionesStr5),
                1, // Índice de la respuesta correcta ("3-5 veces")
                Pregunta.TipoPregunta.TEXTO_LISTVIEW));
    }



    public void respuestaSeleccionada(int indiceRespuesta) {
        Pregunta currentPreg = preguntaActual.getValue();
        if (currentPreg == null) return;

        int puntosActuales = puntuacion.getValue() != null ? puntuacion.getValue() : 0;

        if (currentPreg.getRespuestaCorrectaIndex() == indiceRespuesta) {
            puntuacion.setValue(puntosActuales + PUNTOS_POR_ACIERTO);
            ultimoResultado.setValue(ResultadoRespuesta.CORRECTA);
        } else {
            puntuacion.setValue(puntosActuales + PUNTOS_POR_FALLO);
            ultimoResultado.setValue(ResultadoRespuesta.INCORRECTA);
        }
    }

    public void avanzarAlSiguienteOPausa() {
        if (indicePreguntaActual < todasLasPreguntas.size()) {
            preguntaActual.setValue(todasLasPreguntas.get(indicePreguntaActual));
            indicePreguntaActual++;
            ultimoResultado.setValue(null);
        } else {
            preguntaActual.setValue(null);
            ultimoResultado.setValue(ResultadoRespuesta.JUEGO_TERMINADO);
        }
    }

    public void reiniciarJuego() {
        puntuacion.setValue(0);
        indicePreguntaActual = 0;
        ultimoResultado.setValue(null);

        siguientePregunta();
    }

    private void siguientePregunta() {

        if (!todasLasPreguntas.isEmpty()) {
            indicePreguntaActual = 0;
            preguntaActual.setValue(todasLasPreguntas.get(indicePreguntaActual));
            indicePreguntaActual++; // Prepara el índice para la siguiente llamada
        } else {
            preguntaActual.setValue(null);
            ultimoResultado.setValue(ResultadoRespuesta.JUEGO_TERMINADO);
        }
    }
}
