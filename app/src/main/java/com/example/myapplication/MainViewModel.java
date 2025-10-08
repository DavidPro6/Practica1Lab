package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Integer> finalScore = new MutableLiveData<>();

    public void setFinalScore(int score) {
        finalScore.setValue(score);
    }

    public LiveData<Integer> getFinalScore() {
        return finalScore;
    }
}
