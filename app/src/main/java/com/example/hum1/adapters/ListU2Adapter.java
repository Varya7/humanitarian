package com.example.hum1.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.classes.ListU2;
import com.example.hum1.R;

import java.util.List;

/**
 * Адаптер {@code ListU2Adapter} для отображения списка {@link ListU2} в {@link RecyclerView}.
 * Каждый элемент содержит поле ввода (EditText) с подсказкой, основанной на значении поля {@code margin},
 * представляющем данные для подачи заявки в MainActivity и MainActivity3.
 */
public class ListU2Adapter extends RecyclerView.Adapter<ListU2Adapter.ViewHolder> {

    /** Список элементов модели {@link ListU2}. */
    private List<ListU2> items;

    /**
     * ViewHolder — внутренний класс, представляющий один элемент списка.
     * Содержит поле {@link EditText} для ввода значений.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** Поле ввода, отображающее margin в виде подсказки. */
        public EditText editText;

        /**
         * Конструктор ViewHolder.
         *
         * @param itemView Представление (View) одного элемента списка.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.margin);
        }
    }

    /**
     * Конструктор адаптера.
     *
     * @param items Список объектов {@link ListU2}, содержащих данные для отображения.
     */
    public ListU2Adapter(List<ListU2> items) {
        this.items = items;
    }

    /**
     * Создает новый {@link ViewHolder} при необходимости.
     *
     * @param parent   Родительский ViewGroup, в который будет добавлено новое представление.
     * @param viewType Тип представления (не используется).
     * @return Новый объект {@link ViewHolder}.
     */
    @NonNull
    @Override
    public ListU2Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items6, parent, false);
        return new ListU2Adapter.ViewHolder(view);
    }

    /**
     * Привязывает данные из {@link ListU2} к {@link ViewHolder}.
     *
     * @param holder   Объект {@link ViewHolder}.
     * @param position Позиция элемента в списке.
     */
    @Override
    public void onBindViewHolder(@NonNull ListU2Adapter.ViewHolder holder, int position) {
        ListU2 item = items.get(position);
        holder.editText.setHint(item.getMargin());
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
     * Обновляет список данных и перерисовывает {@link RecyclerView}.
     *
     * @param newList Новый список объектов {@link ListU2}.
     */
    public void updateList(List<ListU2> newList) {
        items = newList;
        notifyDataSetChanged();
    }
}
