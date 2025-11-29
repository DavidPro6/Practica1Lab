package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements BlankFragment.OnStartQuizListener {

    private MainViewModel mainViewModel;

    private final ActivityResultLauncher<Intent> quizLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int score = result.getData().getIntExtra("final_score", 0);
                    mainViewModel.setFinalScore(score);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        MusicManager.getInstance().initialize(this);
        MusicManager.getInstance().startMusic();

        FloatingActionButton fabHelp = findViewById(R.id.fabHelp);
        fabHelp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        });
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

    @Override
    public void onStartQuiz() {
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        quizLauncher.launch(intent);
    }
}
