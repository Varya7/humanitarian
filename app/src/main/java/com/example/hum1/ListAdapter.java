package com.example.hum1;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private ArrayList<Map<String, String>> items;
    private OnItemClickListener listener;
    public ListAdapter(ArrayList<Map<String, String>> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }
    public ListAdapter(ArrayList<Map<String, String>> items) {
        this.items = items;
        this.listener = null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> item = items.get(position);
        holder.nameText.setText(item.get("name"));
        holder.quantityText.setText(item.get("quantity"));


        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
        } else {
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView quantityText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tvName);
            quantityText = itemView.findViewById(R.id.tvQuantity);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}