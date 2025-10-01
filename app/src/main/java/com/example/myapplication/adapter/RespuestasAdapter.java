package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import com.example.myapplication.R;
import com.example.myapplication.models.Respuesta;

import java.util.List;

public class RespuestasAdapter extends RecyclerView.Adapter<RespuestasAdapter.RespuestaViewHolder> {

    private final List<Respuesta> opciones;
    private final LayoutInflater inflater;
    private int selectedPosition = RecyclerView.NO_POSITION; // -1, no hay nada seleccionado al inicio

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    private final OnItemClickListener listener;

    public RespuestasAdapter(Context context, List<Respuesta> opciones, OnItemClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.opciones = opciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RespuestaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_respuesta_imagen, parent, false);
        return new RespuestaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RespuestaViewHolder holder, int position) {
        Respuesta respuesta = opciones.get(position);

        holder.imageView.setImageResource(respuesta.getImagenRespuestaId());


        if (position == selectedPosition) {

            holder.cardView.setStrokeColor(Color.parseColor("#FFC107")); // Dorado
            holder.cardView.setStrokeWidth(8); // Un borde más notable
        } else {

            holder.cardView.setStrokeColor(Color.TRANSPARENT);
            holder.cardView.setStrokeWidth(2); // Devolvemos al grosor por defecto
        }


        holder.itemView.setOnClickListener(v -> {

            int previousSelectedPosition = selectedPosition;

            selectedPosition = holder.getAdapterPosition();

            if (previousSelectedPosition != -1) {
                notifyItemChanged(previousSelectedPosition); // Deseleccionar el antiguo
            }
            notifyItemChanged(selectedPosition); // Seleccionar el nuevo

            listener.onItemClick(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return opciones.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    static class RespuestaViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imageView;

        RespuestaViewHolder(View itemView) {
            super(itemView);


            cardView = itemView.findViewById(R.id.card_view_imagen);
            imageView = itemView.findViewById(R.id.imageViewRespuesta);

        }
    }
}
