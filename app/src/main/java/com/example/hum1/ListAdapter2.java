package com.example.hum1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAdapter2 extends RecyclerView.Adapter<ListAdapter2.ViewHolder> {
    private List<Map<String, String>> items;
    private Map<String, Integer> selectedQuantities = new HashMap<>();

    public ListAdapter2(List<Map<String, String>> items) {
        this.items = items;
        for (Map<String, String> item : items) {
            selectedQuantities.put(item.get("name"), 0);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items4, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> item = items.get(position);
        String itemName = item.get("name");
        String maxQuantity = item.get("quantity");
        if (!selectedQuantities.containsKey(itemName)) {
            selectedQuantities.put(itemName, 0);
        }
        holder.nameText.setText(itemName);
        holder.quantityText.setText(String.valueOf(selectedQuantities.get(itemName)));
        holder.btnIncrease.setOnClickListener(v -> {
            int current = selectedQuantities.getOrDefault(itemName, 0);
            if (current < Integer.parseInt(maxQuantity)) {
                selectedQuantities.put(itemName, current + 1);
                holder.quantityText.setText(String.valueOf(current + 1));
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int current = selectedQuantities.getOrDefault(itemName, 0);
            if (current > 0) {
                selectedQuantities.put(itemName, current - 1);
                holder.quantityText.setText(String.valueOf(current - 1));
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
    public Map<String, Integer> getSelectedQuantities() {
        return selectedQuantities;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText;
        Button btnIncrease, btnDecrease;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.item_name);
            quantityText = view.findViewById(R.id.item_quantity);
            btnIncrease = view.findViewById(R.id.btn_increase);
            btnDecrease = view.findViewById(R.id.btn_decrease);
        }
    }
}