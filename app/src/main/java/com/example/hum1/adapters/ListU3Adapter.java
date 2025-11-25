package com.example.hum1.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.classes.ListU3;
import com.example.hum1.R;

import java.util.List;

/**
 * Адаптер {@code ListU3Adapter} для отображения списка объектов {@link ListU3}
 * в компоненте {@link RecyclerView}. Каждый элемент содержит название вещи (label) и количество (value),
 * которые отображаются в виде двух текстовых полей. Используется в ViewApplic, ViewApplicC, ViewAppComplete.
 */
public class ListU3Adapter extends RecyclerView.Adapter<ListU3Adapter.ViewHolder> {

    /** Список элементов, содержащих пары "вещь — количество". */
    private List<ListU3> listItems;

    /**
     * Конструктор адаптера.
     *
     * @param listItems Список объектов {@link ListU3} для отображения.
     */
    public ListU3Adapter(List<ListU3> listItems) {
        this.listItems = listItems;
    }

    /**
     * Создает новый {@link ViewHolder}, заполняя его макетом элемента списка.
     *
     * @param parent   Родительский ViewGroup.
     * @param viewType Тип представления (не используется).
     * @return Новый экземпляр {@link ViewHolder}.
     */
    @NonNull
    @Override
    public ListU3Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items7, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Привязывает данные из {@link ListU3} к представлению элемента списка.
     *
     * @param holder   ViewHolder, в который будут записаны данные.
     * @param position Позиция элемента в списке.
     */
    @Override
    public void onBindViewHolder(@NonNull ListU3Adapter.ViewHolder holder, int position) {
        ListU3 item = listItems.get(position);
        holder.labelTextView.setText(item.getLabel());
        holder.valueTextView.setText(item.getValue());
    }

    /**
     * Возвращает общее количество элементов в списке.
     *
     * @return Размер списка {@code listItems}.
     */
    @Override
    public int getItemCount() {
        return listItems.size();
    }

    /**
     * ViewHolder — внутренний класс, представляющий элемент списка.
     * Содержит два текстовых поля: метку и значение.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** Поле для отображения названия вещи. */
        TextView labelTextView;

        /** Поле для отображения количества вещей. */
        TextView valueTextView;

        /**
         * Конструктор ViewHolder.
         *
         * @param itemView Представление одного элемента списка.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            labelTextView = itemView.findViewById(R.id.label);
            valueTextView = itemView.findViewById(R.id.value);
        }
    }
}
