package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView; // Importante añadir ImageView
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.RespuestasAdapter;
import com.example.myapplication.models.Pregunta;
import com.example.myapplication.models.Respuesta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuizActivity extends AppCompatActivity {

    private TextView textViewPuntuacion, textViewPregunta;
    private Button buttonConfirmarRespuesta;

    // Vistas para los tipos de respuesta
    private RadioGroup radioGroupOpciones;
    private RecyclerView recyclerViewOpcionesImagen;
    private ListView listViewOpciones;
    private Spinner spinnerOpciones;

    // Vista para la imagen de la pregunta
    private ImageView imageViewPregunta;

    private List<RadioButton> radioButtons;

    private List<Pregunta> listaDePreguntas;
    private int preguntaActualIndex = 0;
    private int puntuacion = 0;
    private boolean juegoTerminado = false;

    // Adapters
    private RespuestasAdapter respuestasAdapter;
    private ArrayAdapter<String> listViewAdapter;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        inicializarVistas();
        crearPreguntas();
        mostrarPregunta();

        buttonConfirmarRespuesta.setOnClickListener(v -> {
            if (juegoTerminado) {
                devolverPuntuacionYFinalizar(puntuacion);
            } else {
                comprobarRespuesta();
            }
        });
    }

    private void inicializarVistas() {
        textViewPuntuacion = findViewById(R.id.textViewPuntuacion);
        textViewPregunta = findViewById(R.id.textViewPregunta);
        buttonConfirmarRespuesta = findViewById(R.id.buttonConfirmarRespuesta);

        // Vistas de respuesta
        radioGroupOpciones = findViewById(R.id.radioGroupOpciones);
        recyclerViewOpcionesImagen = findViewById(R.id.recyclerViewOpcionesImagen);
        listViewOpciones = findViewById(R.id.listViewOpciones);
        spinnerOpciones = findViewById(R.id.spinnerOpciones);

        // Vista de imagen de pregunta
        imageViewPregunta = findViewById(R.id.imageViewPregunta);

        radioButtons = new ArrayList<>();
        radioButtons.add(findViewById(R.id.radioButtonOpcion1));
        radioButtons.add(findViewById(R.id.radioButtonOpcion2));
        radioButtons.add(findViewById(R.id.radioButtonOpcion3));
        radioButtons.add(findViewById(R.id.radioButtonOpcion4));
    }

    private void mostrarPregunta() {
        if (preguntaActualIndex < listaDePreguntas.size()) {
            Pregunta preguntaActual = listaDePreguntas.get(preguntaActualIndex);
            textViewPregunta.setText(preguntaActual.getTextoPregunta());

            ocultarTodosLosControles();
            configurarControlesParaPregunta(preguntaActual);

            buttonConfirmarRespuesta.setEnabled(true);
        } else {
            finDelQuiz();
        }
    }

    private void configurarControlesParaPregunta(Pregunta pregunta) {
        // Mostrar la imagen de la pregunta si existe
        if (pregunta.getImagenPreguntaId() != 0) {
            imageViewPregunta.setImageResource(pregunta.getImagenPreguntaId());
            imageViewPregunta.setVisibility(View.VISIBLE);
        } else {
            imageViewPregunta.setVisibility(View.GONE);
        }

        try {
            switch (pregunta.getTipo()) {
                case TEXTO_RADIOBUTTON:
                    radioGroupOpciones.setVisibility(View.VISIBLE);
                    radioGroupOpciones.clearCheck();
                    for (int i = 0; i < radioButtons.size(); i++) {
                        if (i < pregunta.getOpciones().size()) {
                            radioButtons.get(i).setText(pregunta.getOpciones().get(i).getTextoRespuesta());
                            radioButtons.get(i).setVisibility(View.VISIBLE);
                        } else {
                            radioButtons.get(i).setVisibility(View.GONE);
                        }
                    }
                    break;

                case IMAGEN_GRID:
                    recyclerViewOpcionesImagen.setVisibility(View.VISIBLE);
                    respuestasAdapter = new RespuestasAdapter(this, pregunta.getOpciones(), position -> {});
                    recyclerViewOpcionesImagen.setLayoutManager(new GridLayoutManager(this, 2));
                    recyclerViewOpcionesImagen.setAdapter(respuestasAdapter);
                    break;

                case TEXTO_LISTVIEW:
                    listViewOpciones.setVisibility(View.VISIBLE);
                    List<String> opcionesListView = pregunta.getOpciones().stream()
                            .map(Respuesta::getTextoRespuesta)
                            .collect(Collectors.toList());
                    listViewAdapter = new ArrayAdapter<>(this, R.layout.list_item_layout, opcionesListView);
                    listViewOpciones.setAdapter(listViewAdapter);
                    listViewOpciones.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    listViewOpciones.clearChoices();
                    break;

                case TEXTO_SPINNER:
                    spinnerOpciones.setVisibility(View.VISIBLE);
                    List<String> opcionesSpinner = pregunta.getOpciones().stream()
                            .map(Respuesta::getTextoRespuesta)
                            .collect(Collectors.toList());
                    spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_layout, opcionesSpinner);
                    spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerOpciones.setAdapter(spinnerAdapter);
                    break;
            }
        } catch (Exception e) {
            mostrarDialogoDeError("Error al Cargar Pregunta", e.getMessage());
        }
    }

    private void comprobarRespuesta() {
        buttonConfirmarRespuesta.setEnabled(false);
        Pregunta preguntaActual = listaDePreguntas.get(preguntaActualIndex);
        int respuestaSeleccionadaIndex = -1;

        switch (preguntaActual.getTipo()) {
            case TEXTO_RADIOBUTTON:
                int selectedRadioButtonId = radioGroupOpciones.getCheckedRadioButtonId();
                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                    respuestaSeleccionadaIndex = radioButtons.indexOf(selectedRadioButton);
                }
                break;
            case IMAGEN_GRID:
                if (respuestasAdapter != null) {
                    respuestaSeleccionadaIndex = respuestasAdapter.getSelectedPosition();
                }
                break;
            case TEXTO_LISTVIEW:
                respuestaSeleccionadaIndex = listViewOpciones.getCheckedItemPosition();
                break;
            case TEXTO_SPINNER:
                respuestaSeleccionadaIndex = spinnerOpciones.getSelectedItemPosition();
                break;
        }

        if (respuestaSeleccionadaIndex == -1) {
            mostrarDialogoSinRespuesta();
            buttonConfirmarRespuesta.setEnabled(true);
            return;
        }

        // --- ¡AQUÍ ESTÁ LA NUEVA LÓGICA DE PUNTUACIÓN! ---
        if (respuestaSeleccionadaIndex == preguntaActual.getRespuestaCorrectaIndex()) {
            // Si la respuesta es correcta, suma 1 punto.
            puntuacion++; // Cambiado de puntuacion += 10 a puntuacion++
            mostrarDialogoDeAcierto();
        } else {
            // Si la respuesta es incorrecta, resta 2 puntos.
            puntuacion -= 2;
            mostrarDialogoDeFallo();
        }

        // Actualiza el texto de la puntuación en la pantalla.
        textViewPuntuacion.setText("Puntuación: " + puntuacion);
        // --- FIN DE LA LÓGICA DE PUNTUACIÓN ---
    }


    private void avanzarPregunta() {
        preguntaActualIndex++;
        if (preguntaActualIndex < listaDePreguntas.size()) {
            mostrarPregunta();
        } else {
            finDelQuiz();
        }
    }

    private void ocultarTodosLosControles() {
        // Ocultar todas las vistas de respuesta
        radioGroupOpciones.setVisibility(View.GONE);
        recyclerViewOpcionesImagen.setVisibility(View.GONE);
        listViewOpciones.setVisibility(View.GONE);
        spinnerOpciones.setVisibility(View.GONE);
        // Y también ocultar la imagen de la pregunta por defecto
        imageViewPregunta.setVisibility(View.GONE);
    }

    private void crearPreguntas() {
        listaDePreguntas = new ArrayList<>();

        // Pregunta 1: Texto con RadioButtons
        listaDePreguntas.add(new Pregunta(
                "¿Qué ejercicio se enfoca principalmente en los pectorales?",
                Arrays.asList(
                        new Respuesta("Sentadilla"),
                        new Respuesta("Press de banca"), // Correcta
                        new Respuesta("Peso muerto"),
                        new Respuesta("Curl de bíceps")
                ),
                1,
                Pregunta.TipoPregunta.TEXTO_RADIOBUTTON
        ));

        // Pregunta 2: Texto con Opciones de Imagen
        listaDePreguntas.add(new Pregunta(
                "¿Cuál de estas imágenes muestra una 'dominada' (pull-up)?",
                Arrays.asList(
                        new Respuesta(R.drawable.sentadilla_img),
                        new Respuesta(R.drawable.press_banca_img),
                        new Respuesta(R.drawable.dominada_img), // Correcta
                        new Respuesta(R.drawable.curl_biceps_img)
                ),
                2,
                Pregunta.TipoPregunta.IMAGEN_GRID
        ));

        // Pregunta 3: Texto con ListView
        listaDePreguntas.add(new Pregunta(
                "¿Cuál de los siguientes es un macronutriente?",
                Arrays.asList(
                        new Respuesta("Vitamina C"),
                        new Respuesta("Calcio"),
                        new Respuesta("Proteína"), // Correcta
                        new Respuesta("Hierro"),
                        new Respuesta("Magnesio")
                ),
                2,
                Pregunta.TipoPregunta.TEXTO_LISTVIEW
        ));

        // Pregunta 4: Texto con Spinner
        listaDePreguntas.add(new Pregunta(
                "Para hipertrofia, el rango de repeticiones más común es...",
                Arrays.asList(
                        new Respuesta("1-5"),
                        new Respuesta("6-12"), // Correcta
                        new Respuesta("15-20"),
                        new Respuesta("Más de 25")
                ),
                1,
                Pregunta.TipoPregunta.TEXTO_SPINNER
        ));

        // Pregunta 5: Imagen con Opciones de Texto (RadioButtons)
        listaDePreguntas.add(new Pregunta(
                "¿Qué ejercicio se muestra en la imagen?", // Enunciado
                Arrays.asList(
                        new Respuesta("Press de banca"),
                        new Respuesta("Peso muerto"),
                        new Respuesta("Sentadilla"), // Respuesta Correcta
                        new Respuesta("Remo con barra")
                ),
                2, // Índice de la respuesta correcta ("Sentadilla")
                Pregunta.TipoPregunta.TEXTO_RADIOBUTTON,
                R.drawable.sentadilla_img // ID de la imagen a mostrar
        ));
    }

    // --- EL RESTO DE MÉTODOS (DIÁLOGOS, REINICIO, ETC.) NO NECESITAN CAMBIOS ---

    private void finDelQuiz() {
        juegoTerminado = true;
        ocultarTodosLosControles();

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button buttonPositive = dialogView.findViewById(R.id.dialog_button_positive);
        Button buttonNegative = dialogView.findViewById(R.id.dialog_button_negative);

        dialogTitle.setText("¡Quiz Finalizado!");
        dialogMessage.setText("Tu puntuación final es: " + puntuacion);

        buttonPositive.setText("Volver a Jugar");
        buttonNegative.setText("Salir");

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        buttonPositive.setOnClickListener(v -> {
            dialog.dismiss();
            devolverPuntuacionYFinalizar(puntuacion);
        });

        buttonNegative.setOnClickListener(v -> {
            dialog.dismiss();
            devolverPuntuacionYFinalizar(puntuacion);
        });

        dialog.setCancelable(false);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void reiniciarQuiz() {
        juegoTerminado = false;
        preguntaActualIndex = 0;
        puntuacion = 0;
        textViewPuntuacion.setText("Puntuación: 0");
        mostrarPregunta();
    }

    private void devolverPuntuacionYFinalizar(int score) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("final_score", score);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void mostrarDialogoDeError(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button buttonPositive = dialogView.findViewById(R.id.dialog_button_positive);
        Button buttonNegative = dialogView.findViewById(R.id.dialog_button_negative);

        dialogTitle.setText(title);
        dialogMessage.setText("Ha ocurrido un problema:\n" + message);
        buttonPositive.setText("Saltar Pregunta");
        buttonNegative.setText("Reiniciar Quiz");

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        buttonPositive.setOnClickListener(v -> {
            dialog.dismiss();
            avanzarPregunta();
        });

        buttonNegative.setOnClickListener(v -> {
            dialog.dismiss();
            reiniciarQuiz();
        });

        dialog.setCancelable(false);
        dialog.show();


        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void mostrarDialogoSinRespuesta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button buttonPositive = dialogView.findViewById(R.id.dialog_button_positive);
        Button buttonNegative = dialogView.findViewById(R.id.dialog_button_negative);

        dialogTitle.setText("¡Atención!");
        dialogMessage.setText("Por favor, selecciona una respuesta para continuar.");
        buttonPositive.setText("Entendido");
        buttonNegative.setVisibility(View.GONE);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        buttonPositive.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void mostrarDialogoDeAcierto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button buttonPositive = dialogView.findViewById(R.id.dialog_button_positive);
        Button buttonNegative = dialogView.findViewById(R.id.dialog_button_negative);

        dialogTitle.setText("¡Correcto!");
        dialogTitle.setTextColor(android.graphics.Color.parseColor("#FFC107"));
        dialogMessage.setText("¡Sigue así!");
        buttonPositive.setVisibility(View.GONE);
        buttonNegative.setVisibility(View.GONE);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            avanzarPregunta();
        }, 1500);
    }

    private void mostrarDialogoDeFallo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button buttonPositive = dialogView.findViewById(R.id.dialog_button_positive);
        Button buttonNegative = dialogView.findViewById(R.id.dialog_button_negative);

        dialogTitle.setText("¡Respuesta Incorrecta!");
        dialogMessage.setText("Has fallado. ¿Qué te gustaría hacer?");
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        buttonPositive.setText("Continuar");
        buttonPositive.setOnClickListener(v -> {
            dialog.dismiss();
            avanzarPregunta();
        });

        buttonNegative.setText("Reiniciar Test");
        buttonNegative.setOnClickListener(v -> {
            dialog.dismiss();
            reiniciarQuiz();
        });

        dialog.setCancelable(false);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
