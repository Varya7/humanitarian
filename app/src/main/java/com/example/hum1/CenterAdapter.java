package com.example.hum1;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Адаптер для отображения списка центров в RecyclerView.
 * Обеспечивает привязку данных о центрах к элементам списка и обработку кликов.
 */
public class CenterAdapter extends RecyclerView.Adapter<CenterAdapter.ViewHolder> {

    /**
     * Интерфейс для обработки кликов по элементам списка центров.
     */
    interface OnCenterClickListener {
        /**
         * Вызывается при клике на элемент списка центров.
         *
         * @param center объект центра, по которому кликнули
         * @param position позиция элемента в списке
         */
        void onCenterClick(Center center, int position);
    }

    private final OnCenterClickListener onClickListener;
    private final LayoutInflater inflater;
    List<Center> centers;

    /**
     * Конструктор адаптера.
     *
     * @param context контекст приложения
     * @param centers список центров для отображения
     * @param onClickListener обработчик кликов по элементам списка
     */
    CenterAdapter(Context context, List<Center> centers, OnCenterClickListener onClickListener) {
        this.centers = centers;
        this.onClickListener = onClickListener;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Создает новый объект ViewHolder при необходимости.
     *
     * @param parent родительская ViewGroup
     * @param viewType тип представления
     * @return новый экземпляр ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_items3, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Обновляет список центров в адаптере.
     *
     * @param newList новый список центров
     */
    public void updateList(ArrayList<Center> newList) {
        centers = newList;
        notifyDataSetChanged();
    }

    /**
     * Привязывает данные центра к ViewHolder на указанной позиции.
     *
     * @param holder ViewHolder для привязки данных
     * @param position позиция данных в списке
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Center center = centers.get(position);
        holder.center_name.setText(center.getCenter_name());
        holder.address.setText("Адрес: " + center.getAddress());

        holder.itemView.setOnClickListener(v -> onClickListener.onCenterClick(center, position));
    }

    /**
     * Возвращает общее количество элементов в списке.
     *
     * @return количество центров в адаптере
     */
    @Override
    public int getItemCount() {
        return centers.size();
    }

    /**
     * ViewHolder для кэширования View-компонентов элементов списка.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView center_name, address;

        /**
         * Конструктор ViewHolder.
         *
         * @param view корневое View элемента списка
         */
        ViewHolder(View view) {
            super(view);
            center_name = view.findViewById(R.id.center_name);
            address = view.findViewById(R.id.address);
        }
    }
}