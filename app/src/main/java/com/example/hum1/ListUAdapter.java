package com.example.hum1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Адаптер {@code ListUAdapter} предназначен для отображения списка объектов {@link ListU}
 * в компоненте {@link RecyclerView}. Каждый элемент списка содержит текстовое поле с margin —
 * значением, необходимое для подачи заявки.
 */
public class ListUAdapter extends RecyclerView.Adapter<ListUAdapter.ViewHolder> {

    /** Список элементов данных {@link ListU}. */
    private List<ListU> items;

    /**
     * ViewHolder — внутренний класс, представляющий отдельный элемент списка.
     * Содержит один {@link TextView}, отображающий поле.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** Текстовое поле для отображения поля. */
        public TextView textView;

        /**
         * Конструктор ViewHolder.
         *
         * @param itemView Представление одного элемента списка.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.margin);
        }
    }

    /**
     * Конструктор адаптера.
     *
     * @param items Список объектов {@link ListU}, которые нужно отобразить.
     */
    public ListUAdapter(List<ListU> items) {
        this.items = items;
    }

    /**
     * Создает новый {@link ViewHolder} и заполняет его макетом list_items5.
     *
     * @param parent   Родительский ViewGroup.
     * @param viewType Тип элемента (не используется).
     * @return Новый {@link ViewHolder}.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items5, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Привязывает данные из модели {@link ListU} к представлению {@link ViewHolder}.
     *
     * @param holder   ViewHolder, в который будут загружены данные.
     * @param position Позиция элемента в списке.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListU item = items.get(position);
        holder.textView.setText(item.getMargin());
    }

    /**
     * Возвращает количество элементов в списке.
     *
     * @return Размер списка {@code items}.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Обновляет список данных и уведомляет адаптер об изменениях.
     *
     * @param newList Новый список объектов {@link ListU}.
     */
    public void updateList(List<ListU> newList) {
        items = newList;
        notifyDataSetChanged();
    }
}
