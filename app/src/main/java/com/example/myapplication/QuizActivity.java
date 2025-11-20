package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.RespuestasAdapter;
import com.example.myapplication.database.QuizDbHelper;
import com.example.myapplication.models.Pregunta;
import com.example.myapplication.models.Respuesta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuizActivity extends AppCompatActivity {

    // --- CORRECCIÓN FINAL: Un MediaPlayer para cada tipo de sonido concurrente ---
    private MediaPlayer responseSoundPlayer; // Para acierto (sound1) y fallo (sound2)
    private MediaPlayer timerWarningPlayer; // Exclusivamente para la advertencia (sound3)
    private long tiempoInicioQuiz;


    // Vistas y variables del Quiz (sin cambios)
    private TextView textViewPuntuacion, textViewPregunta, textViewTemporizador;
    private Button buttonConfirmarRespuesta;
    private RadioGroup radioGroupOpciones;
    private RecyclerView recyclerViewOpcionesImagen;
    private ListView listViewOpciones;
    private Spinner spinnerOpciones;
    private ImageView imageViewPregunta;
    private List<RadioButton> radioButtons;
    private List<Pregunta> listaDePreguntas;
    private CountDownTimer countDownTimer;
    private static final long TIEMPO_POR_PREGUNTA_MS = 15000;
    private int preguntaActualIndex = 0;
    private int puntuacion = 0;
    private boolean juegoTerminado = false;
    private boolean tiempoAgotado = false;
    private RespuestasAdapter respuestasAdapter;
    private ArrayAdapter<String> listViewAdapter;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tiempoInicioQuiz = System.currentTimeMillis();

        inicializarVistas();
        cargarPreguntas();

        if (listaDePreguntas.isEmpty()) {
            Toast.makeText(this, "No se pudieron cargar preguntas.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mostrarPregunta();

        buttonConfirmarRespuesta.setOnClickListener(v -> {
            if (juegoTerminado) {
                devolverPuntuacionYFinalizar(puntuacion);
            } else {
                comprobarRespuesta();
            }
        });
    }

    private void iniciarTemporizador() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        textViewTemporizador.setText(String.valueOf(TIEMPO_POR_PREGUNTA_MS / 1000));
        textViewTemporizador.setTextColor(Color.WHITE);

        countDownTimer = new CountDownTimer(TIEMPO_POR_PREGUNTA_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTemporizador.setText(String.valueOf(millisUntilFinished / 1000));
                // Lógica de advertencia a los 5 segundos
                if (millisUntilFinished / 1000 == 5) {
                    textViewTemporizador.setTextColor(Color.RED);
                    playTimerWarningSound(); // Llama al método de sonido exclusivo
                }
            }

            @Override
            public void onFinish() {
                textViewTemporizador.setText("0");
                tiempoAgotado = true;
                comprobarRespuesta();
            }
        }.start();
    }

    private void comprobarRespuesta() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        buttonConfirmarRespuesta.setEnabled(false);

        Pregunta preguntaActual = listaDePreguntas.get(preguntaActualIndex);
        int respuestaSeleccionadaIndex = -1;

        if (!tiempoAgotado) {
            switch (preguntaActual.getTipo()) {
                case TEXTO_RADIOBUTTON:
                    int selectedId = radioGroupOpciones.getCheckedRadioButtonId();
                    if (selectedId != -1) respuestaSeleccionadaIndex = radioButtons.indexOf(findViewById(selectedId));
                    break;
                case IMAGEN_GRID:
                    if (respuestasAdapter != null) respuestaSeleccionadaIndex = respuestasAdapter.getSelectedPosition();
                    break;
                case TEXTO_LISTVIEW:
                    respuestaSeleccionadaIndex = listViewOpciones.getCheckedItemPosition();
                    break;
                case TEXTO_SPINNER:
                    respuestaSeleccionadaIndex = spinnerOpciones.getSelectedItemPosition();
                    break;
            }
        }

        if (respuestaSeleccionadaIndex == -1) {
            if (tiempoAgotado) {
                puntuacion -= 2;
                playResponseSound(R.raw.sound2);
                textViewPuntuacion.setText("Puntuación: " + puntuacion);
                mostrarDialogoDeFallo("¡Tiempo agotado!");
            } else {
                mostrarDialogoSinRespuesta();
                buttonConfirmarRespuesta.setEnabled(true);
            }
            return;
        }

        if (respuestaSeleccionadaIndex == preguntaActual.getRespuestaCorrectaIndex()) {
            puntuacion+=3;
            playResponseSound(R.raw.sound1);
            textViewPuntuacion.setText("Puntuación: " + puntuacion);
            mostrarDialogoDeAcierto();
        } else {
            puntuacion -= 2;
            playResponseSound(R.raw.sound2);
            textViewPuntuacion.setText("Puntuación: " + puntuacion);
            mostrarDialogoDeFallo("¡Respuesta Incorrecta!");
        }
    }

    // --- MÉTODOS DE SONIDO REESTRUCTURADOS ---

    private void playResponseSound(int soundId) {
        // Este método gestiona sound1 y sound2
        if (responseSoundPlayer != null) {
            responseSoundPlayer.release();
        }
        responseSoundPlayer = MediaPlayer.create(this, soundId);
        if (responseSoundPlayer != null) {
            responseSoundPlayer.setOnCompletionListener(mp -> mp.release());
            responseSoundPlayer.start();
        }
    }

    private void playTimerWarningSound() {
        if (timerWarningPlayer != null) {
            timerWarningPlayer.release();
            timerWarningPlayer = null; // Opcional pero buena práctica.
        }

        // 2. Crear una instancia completamente nueva.
        timerWarningPlayer = MediaPlayer.create(this, R.raw.sound3);

        // 3. Si se creó correctamente, configurarla y reproducirla.
        if (timerWarningPlayer != null) {
            // Establecer el listener para que se libere a sí mismo AL TERMINAR.
            timerWarningPlayer.setOnCompletionListener(mp -> {
                mp.release();
                timerWarningPlayer = null; // Para asegurar que la próxima vez se cree de nuevo.
            });
            timerWarningPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar todos los recursos de sonido
        if (responseSoundPlayer != null) {
            responseSoundPlayer.release();
            responseSoundPlayer = null;
        }
        if (timerWarningPlayer != null) {
            timerWarningPlayer.release();
            timerWarningPlayer = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void inicializarVistas() {
        textViewPuntuacion = findViewById(R.id.textViewPuntuacion);
        textViewPregunta = findViewById(R.id.textViewPregunta);
        textViewTemporizador = findViewById(R.id.textViewTemporizador);
        buttonConfirmarRespuesta = findViewById(R.id.buttonConfirmarRespuesta);
        radioGroupOpciones = findViewById(R.id.radioGroupOpciones);
        recyclerViewOpcionesImagen = findViewById(R.id.recyclerViewOpcionesImagen);
        listViewOpciones = findViewById(R.id.listViewOpciones);
        spinnerOpciones = findViewById(R.id.spinnerOpciones);
        imageViewPregunta = findViewById(R.id.imageViewPregunta);

        radioButtons = new ArrayList<>();
        radioButtons.add(findViewById(R.id.radioButtonOpcion1));
        radioButtons.add(findViewById(R.id.radioButtonOpcion2));
        radioButtons.add(findViewById(R.id.radioButtonOpcion3));
        radioButtons.add(findViewById(R.id.radioButtonOpcion4));
    }

    private void cargarPreguntas() {
        List<Pregunta> todasLasPreguntas = new ArrayList<>();
        crearPreguntasLocales(todasLasPreguntas);
        QuizDbHelper dbHelper = new QuizDbHelper(this);
        List<Pregunta> preguntasDeLaBD = dbHelper.getAllQuestions();

        if (preguntasDeLaBD != null && !preguntasDeLaBD.isEmpty()) {
            todasLasPreguntas.addAll(preguntasDeLaBD);
        }

        Collections.shuffle(todasLasPreguntas);

        if (todasLasPreguntas.size() > 5) {
            listaDePreguntas = new ArrayList<>(todasLasPreguntas.subList(0, 5));
        } else {
            listaDePreguntas = todasLasPreguntas;
        }
    }

    private void mostrarPregunta() {
        if (preguntaActualIndex < listaDePreguntas.size()) {
            tiempoAgotado = false; // Reiniciar flag
            Pregunta preguntaActual = listaDePreguntas.get(preguntaActualIndex);
            textViewPregunta.setText(preguntaActual.getTextoPregunta());
            ocultarTodosLosControles();
            configurarControlesParaPregunta(preguntaActual);
            buttonConfirmarRespuesta.setEnabled(true);
            iniciarTemporizador();
        } else {
            finDelQuiz();
        }
    }

    private void avanzarPregunta() {
        preguntaActualIndex++;
        mostrarPregunta();
    }

    private void reiniciarQuiz() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    // ... (Y aquí el resto de métodos: diálogos, crearPreguntas, etc.)
    private void ocultarTodosLosControles() {
        radioGroupOpciones.setVisibility(View.GONE);
        recyclerViewOpcionesImagen.setVisibility(View.GONE);
        listViewOpciones.setVisibility(View.GONE);
        spinnerOpciones.setVisibility(View.GONE);
        imageViewPregunta.setVisibility(View.GONE);
    }
    private void configurarControlesParaPregunta(Pregunta pregunta) {
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
    private void crearPreguntasLocales(List<Pregunta> lista) {
        lista.add(new Pregunta("¿Qué ejercicio se enfoca principalmente en los pectorales?", Arrays.asList(new Respuesta("Sentadilla"), new Respuesta("Press de banca"), new Respuesta("Peso muerto"), new Respuesta("Curl de bíceps")), 1, Pregunta.TipoPregunta.TEXTO_RADIOBUTTON));
        lista.add(new Pregunta("¿Cuál de estas imágenes muestra una 'dominada' (pull-up)?", Arrays.asList(new Respuesta(R.drawable.sentadilla_img), new Respuesta(R.drawable.press_banca_img), new Respuesta(R.drawable.dominada_img), new Respuesta(R.drawable.curl_biceps_img)), 2, Pregunta.TipoPregunta.IMAGEN_GRID));
        lista.add(new Pregunta("¿Cuál de los siguientes es un macronutriente?", Arrays.asList(new Respuesta("Vitamina C"), new Respuesta("Calcio"), new Respuesta("Proteína"), new Respuesta("Hierro"), new Respuesta("Magnesio")), 2, Pregunta.TipoPregunta.TEXTO_LISTVIEW));
        lista.add(new Pregunta("Para hipertrofia, el rango de repeticiones más común es...", Arrays.asList(new Respuesta("1-5"), new Respuesta("6-12"), new Respuesta("15-20"), new Respuesta("Más de 25")), 1, Pregunta.TipoPregunta.TEXTO_SPINNER));
        lista.add(new Pregunta("¿Qué ejercicio se muestra en la imagen?", Arrays.asList(new Respuesta("Press de banca"), new Respuesta("Peso muerto"), new Respuesta("Sentadilla"), new Respuesta("Remo con barra")), 2, Pregunta.TipoPregunta.TEXTO_RADIOBUTTON, R.drawable.sentadilla_img));
    }
    private void finDelQuiz() {
        juegoTerminado = true;
        ocultarTodosLosControles();
        long tiempoFinQuiz = System.currentTimeMillis();
        long tiempoTotalMs = tiempoFinQuiz - tiempoInicioQuiz;
        long segundosTotales = tiempoTotalMs / 1000;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button buttonPositive = dialogView.findViewById(R.id.dialog_button_positive);
        Button buttonNegative = dialogView.findViewById(R.id.dialog_button_negative);
        dialogTitle.setText("¡Quiz Finalizado!");
        String mensajeFinal = "Puntuación final: " + puntuacion + "\nTiempo total: " + segundosTotales + " segundos";
        dialogMessage.setText(mensajeFinal);
        dialogMessage.setText("Tu puntuación final es: " + puntuacion+"\nTiempo total: " + segundosTotales + " segundos ");
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
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
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
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
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
        dialogTitle.setTextColor(Color.parseColor("#FFC107"));
        dialogMessage.setText("¡Sigue así!");
        buttonPositive.setVisibility(View.GONE);
        buttonNegative.setVisibility(View.GONE);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            avanzarPregunta();
        }, 1500);
    }
    private void mostrarDialogoDeFallo(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button buttonPositive = dialogView.findViewById(R.id.dialog_button_positive);
        Button buttonNegative = dialogView.findViewById(R.id.dialog_button_negative);
        dialogTitle.setText(title);
        dialogMessage.setText("Has perdido 2 puntos. ¿Continuar?");
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
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        MusicManager.getInstance().startMusic();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (!isChangingConfigurations()) {
            MusicManager.getInstance().pauseMusic();
        }
    }
}
