package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BlankFragment extends Fragment {

    public interface OnStartQuizListener {
        void onStartQuiz();
    }

    private OnStartQuizListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnStartQuizListener) {
            listener = (OnStartQuizListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStartQuizListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonStartQuiz = view.findViewById(R.id.buttonStartQuiz);
        buttonStartQuiz.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStartQuiz();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
