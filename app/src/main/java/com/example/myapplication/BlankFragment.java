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

    // 1. Definir la interfaz
    public interface OnStartQuizListener {
        void onStartQuiz();
    }

    private OnStartQuizListener listener;

    // 2. onAttach: El fragmento se "conecta" a la Activity.
    // Aquí nos aseguramos de que la Activity implemente nuestra interfaz.
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    // 3. onViewCreated: Este método se llama DESPUÉS de que la vista del fragmento ha sido creada.
    // Es el lugar seguro para buscar vistas y configurar listeners.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonStartQuiz = view.findViewById(R.id.buttonStartQuiz);
        buttonStartQuiz.setOnClickListener(v -> {
            // Cuando se hace clic, llamamos al método de la interfaz.
            // La Activity se encargará del resto.
            if (listener != null) {
                listener.onStartQuiz();
            }
        });
    }

    // 4. onDetach: Limpiamos la referencia al listener para evitar memory leaks.
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
