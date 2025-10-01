package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast; // Para mensajes simples
import androidx.appcompat.app.AlertDialog; // Para diálogos más elaborados
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

// Asumiendo que tienes un layout activity_quiz.xml con:
// - TextView: textViewPregunta, textViewPuntuacion
// - RadioGroup: radioGroupOpciones con 4 RadioButtons (radioButtonOpcion1, etc.)
// - Button: buttonConfirmarRespuesta

public class QuizActivity extends AppCompatActivity {

    private QuizViewModel viewModel;
    private TextView textViewPregunta;
    private TextView textViewPuntuacion;
    private RadioGroup radioGroupOpciones;
    private RadioButton[] radioButtons; // Array para los RadioButtons
    private Button buttonConfirmarRespuesta;
    // Puedes añadir otros elementos para diferentes tipos de pregunta (ImageViews, Spinner)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz); // Asegúrate que este layout existe

        viewModel = new ViewModelProvider(this).get(QuizViewModel.class);

        textViewPregunta = findViewById(R.id.textViewPregunta);
        textViewPuntuacion = findViewById(R.id.textViewPuntuacion);
        radioGroupOpciones = findViewById(R.id.radioGroupOpciones);
        buttonConfirmarRespuesta = findViewById(R.id.buttonConfirmarRespuesta);

        // Inicializar RadioButtons (asumiendo que los IDs son secuenciales o los buscas)
        radioButtons = new RadioButton[4]; // Si tienes 4 opciones
        radioButtons[0] = findViewById(R.id.radioButtonOpcion1);
        radioButtons[1] = findViewById(R.id.radioButtonOpcion2);
        radioButtons[2] = findViewById(R.id.radioButtonOpcion3);
        radioButtons[3] = findViewById(R.id.radioButtonOpcion4);
        // ... Asegúrate de que estos IDs existen en tu XML

        setupObservers();
        setupListeners();

        // Si usas ViewBinding, el acceso a los views sería:
        // binding = ActivityQuizBinding.inflate(getLayoutInflater());
        // setContentView(binding.getRoot());
        // binding.textViewPregunta.setText(...)
    }

    private void setupObservers() {
        viewModel.getPreguntaActual().observe(this, pregunta -> {
            if (pregunta != null) {
                textViewPregunta.setText(pregunta.getTextoPregunta());
                // Lógica para mostrar las opciones según el tipo de pregunta
                // Por ahora, asumimos TEXTO_SELECCION con RadioButtons
                if (pregunta.getTipoPregunta() == TipoPregunta.TEXTO_SELECCION) {
                    radioGroupOpciones.setVisibility(View.VISIBLE); // Hacer visible el RadioGroup
                    // Asegúrate de que haya suficientes RadioButtons para las opciones
                    for (int i = 0; i < radioButtons.length; i++) {
                        if (i < pregunta.getOpciones().size()) {
                            radioButtons[i].setText(pregunta.getOpciones().get(i));
                            radioButtons[i].setVisibility(View.VISIBLE);
                        } else {
                            radioButtons[i].setVisibility(View.GONE); // Ocultar si no hay opción
                        }
                    }
                    radioGroupOpciones.clearCheck(); // Limpiar selección anterior
                    buttonConfirmarRespuesta.setEnabled(true);
                }
                // Aquí añadirías lógica para FOTOS_SELECCION y MENU_DESPLEGABLE
            }
            // No hacemos nada si pregunta es null aquí, eso se maneja en ultimoResultado
        });

        viewModel.getPuntuacion().observe(this, puntuacion -> {
            textViewPuntuacion.setText("Puntuación: " + puntuacion);
        });

        viewModel.getUltimoResultado().observe(this, resultado -> {
            if (resultado == null) return; // Evitar procesamiento si es un reset

            buttonConfirmarRespuesta.setEnabled(false); // Deshabilitar confirmación mientras se muestra feedback

            switch (resultado) {
                case CORRECTA:
                    Toast.makeText(this, "¡Correcto! +3 puntos", Toast.LENGTH_SHORT).show();
                    // Avanzar automáticamente a la siguiente pregunta después de un breve delay o acción del usuario
                    // Aquí llamamos directamente para simplificar:
                    viewModel.avanzarAlSiguienteOPausa();
                    break;
                case INCORRECTA:
                    mostrarDialogoError();
                    break;
                case JUEGO_TERMINADO:
                    mostrarDialogoFinJuego();
                    break;
            }
        });
    }

    private void setupListeners() {
        buttonConfirmarRespuesta.setOnClickListener(v -> {
            int selectedRadioButtonId = radioGroupOpciones.getCheckedRadioButtonId();
            if (selectedRadioButtonId != -1) {
                RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                int respuestaIndex = -1;
                for(int i=0; i < radioButtons.length; i++){
                    if(radioButtons[i].getId() == selectedRadioButtonId){
                        respuestaIndex = i;
                        break;
                    }
                }
                if(respuestaIndex != -1){
                    viewModel.respuestaSeleccionada(respuestaIndex);
                } else {
                    Toast.makeText(this, "Error al obtener la opción seleccionada.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Por favor, selecciona una opción.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoError() {
        new AlertDialog.Builder(this)
                .setTitle("¡Incorrecto!")
                .setMessage("Has fallado. Se restan 2 puntos.")
                .setPositiveButton("Continuar", (dialog, which) -> {
                    viewModel.avanzarAlSiguienteOPausa(); // El ViewModel decidirá si hay más preguntas o termina
                })
                .setNegativeButton("Empezar de Nuevo", (dialog, which) -> {
                    viewModel.reiniciarJuego();
                })
                .setCancelable(false) // Evitar que se cierre al tocar fuera
                .show();
    }

    private void mostrarDialogoFinJuego() {
        new AlertDialog.Builder(this)
                .setTitle("¡Juego Terminado!")
                .setMessage("Tu puntuación final es: " + (viewModel.getPuntuacion().getValue() != null ? viewModel.getPuntuacion().getValue() : 0))
                .setPositiveButton("Jugar de Nuevo", (dialog, which) -> {
                    viewModel.reiniciarJuego();
                })
                .setNegativeButton("Salir", (dialog, which) -> {
                    finish(); // Cierra la actividad del quiz
                })
                .setCancelable(false)
                .show();
    }

    // No olvides definir tus clases Pregunta y TipoPregunta como se mostró antes
    // enum TipoPregunta { TEXTO_SELECCION, FOTOS_SELECCION, MENU_DESPLEGABLE }
    // class Pregunta { ... }
}

