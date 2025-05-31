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

/**
 * Адаптер для отображения списка дооступных вещей в RecyclerView в ViewCenter и SettingC.
 * Обеспечивает привязку данных о заявках к элементам интерфейса и обработку пользовательских кликов.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    ArrayList<Map<String, String>> items;
    private OnItemClickListener listener;

    /**
     * Конструктор адаптера с обработчиком нажатия.
     *
     * @param items    Список элементов, где каждый элемент — это Map с данными.
     * @param listener Обработчик нажатий на элемент.
     */
    public ListAdapter(ArrayList<Map<String, String>> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    /**
     * Конструктор адаптера без обработчика нажатия.
     *
     * @param items Список элементов, где каждый элемент — это Map с данными.
     */
    public ListAdapter(ArrayList<Map<String, String>> items) {
        this.items = items;
        this.listener = null;
    }

    /**
     * Создает новый объект ViewHolder при необходимости.
     *
     * @param parent   Родительский ViewGroup.
     * @param viewType Тип представления (не используется).
     * @return Новый ViewHolder.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Привязывает данные к ViewHolder.
     *
     * @param holder   Объект ViewHolder.
     * @param position Позиция элемента в списке.
     */
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
     * Класс-контейнер для элементов списка.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView quantityText;

        /**
         * Конструктор ViewHolder.
         *
         * @param itemView Представление одного элемента списка.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tvName);
            quantityText = itemView.findViewById(R.id.tvQuantity);
        }
    }

    /**
     * Интерфейс для обработки нажатий на элементы списка.
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}