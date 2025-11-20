package com.example.myapplication;


import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Button buttonBack = findViewById(R.id.buttonBack);
        // Al pulsar el botón "Volver", se cierra esta actividad y se regresa a la anterior.
        buttonBack.setOnClickListener(v -> finish());
    }

    // --- Controlamos la música también en esta actividad ---
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