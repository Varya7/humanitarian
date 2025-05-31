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

/**
 * Адаптер для RecyclerView, позволяющий пользователю увеличивать и уменьшать
 * количество каждого элемента в пределах заданного максимума для отправления заявки в MainActivity.
 * Каждый элемент списка содержит название и количество.
 */
public class ListAdapter2 extends RecyclerView.Adapter<ListAdapter2.ViewHolder> {

    /** Список элементов, где каждый элемент — это Map с ключами "name" и "quantity". */
    private List<Map<String, String>> items;

    /** Хранит выбранные пользователем количества для каждого элемента по имени. */
    private Map<String, Integer> selectedQuantities = new HashMap<>();

    /**
     * Конструктор адаптера.
     *
     * @param items Список элементов, где каждый элемент содержит имя и максимальное количество.
     */
    public ListAdapter2(List<Map<String, String>> items) {
        this.items = items;
        for (Map<String, String> item : items) {
            selectedQuantities.put(item.get("name"), 0);
        }
    }

    /**
     * Создает и возвращает ViewHolder для отображения элемента.
     *
     * @param parent   Родительский ViewGroup.
     * @param viewType Тип представления (не используется).
     * @return ViewHolder, содержащий ссылки на виджеты элемента.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items4, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Привязывает данные к элементу списка.
     *
     * @param holder   ViewHolder элемента.
     * @param position Позиция элемента в списке.
     */
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

        // Обработчик кнопки увеличения количества
        holder.btnIncrease.setOnClickListener(v -> {
            int current = selectedQuantities.getOrDefault(itemName, 0);
            if (current < Integer.parseInt(maxQuantity)) {
                selectedQuantities.put(itemName, current + 1);
                holder.quantityText.setText(String.valueOf(current + 1));
            }
        });

        // Обработчик кнопки уменьшения количества
        holder.btnDecrease.setOnClickListener(v -> {
            int current = selectedQuantities.getOrDefault(itemName, 0);
            if (current > 0) {
                selectedQuantities.put(itemName, current - 1);
                holder.quantityText.setText(String.valueOf(current - 1));
            }
        });
    }

    /**
     * Возвращает общее количество элементов в списке.
     *
     * @return Количество элементов.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Возвращает Map выбранных пользователем количеств по именам элементов.
     *
     * @return Map с выбранными количествами.
     */
    public Map<String, Integer> getSelectedQuantities() {
        return selectedQuantities;
    }

    /**
     * Внутренний класс, представляющий один элемент списка.
     * Содержит ссылки на текстовые поля и кнопки.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText;
        Button btnIncrease, btnDecrease;

        /**
         * Конструктор ViewHolder, инициализирующий компоненты интерфейса.
         *
         * @param view View элемента.
         */
        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.item_name);
            quantityText = view.findViewById(R.id.item_quantity);
            btnIncrease = view.findViewById(R.id.btn_increase);
            btnDecrease = view.findViewById(R.id.btn_decrease);
        }
    }
}
