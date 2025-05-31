package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Адаптер для отображения списка заявок на регистрацию центров {@link CenterApp} в виде элементов RecyclerView.
 */
public class CenterAppAdapter extends RecyclerView.Adapter<CenterAppAdapter.ViewHolder> {

    /**
     * Интерфейс для обработки нажатий на элементы списка.
     */
    interface OnCenterAppClickListener {
        /**
         * Метод вызывается при нажатии на элемент списка.
         *
         * @param app      объект {@link CenterApp}, связанный с нажатым элементом
         * @param position позиция элемента в списке
         */
        void onCenterAppClick(CenterApp app, int position);
    }

    private final OnCenterAppClickListener onClickListener;
    private final LayoutInflater inflater;
    private List<CenterApp> centers;

    /**
     * Конструктор адаптера.
     *
     * @param context         контекст, необходимый для создания LayoutInflater
     * @param centers         список объектов {@link CenterApp}, отображаемых в списке
     * @param onClickListener слушатель кликов по элементам списка
     */
    public CenterAppAdapter(Context context, List<CenterApp> centers, OnCenterAppClickListener onClickListener) {
        this.centers = centers;
        this.onClickListener = onClickListener;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Создаёт новый {@link ViewHolder} при необходимости.
     *
     * @param parent   родительский ViewGroup
     * @param viewType тип представления (не используется)
     * @return новый ViewHolder
     */
    @Override
    public CenterAppAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_items_mod, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Привязывает данные к ViewHolder.
     *
     * @param holder   объект {@link ViewHolder}, в который будут переданы данные
     * @param position позиция элемента в списке
     */
    @Override
    public void onBindViewHolder(CenterAppAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CenterApp centerApp = centers.get(position);
        holder.center.setText(centerApp.getCenter());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onCenterAppClick(centerApp, position);
            }
        });
    }

    /**
     * Возвращает общее количество элементов в списке.
     *
     * @return количество элементов
     */
    @Override
    public int getItemCount() {
        return centers.size();
    }

    /**
     * Класс ViewHolder, содержащий представление одного элемента списка.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * TextView, отображающий название центра.
         */
        final TextView center;

        /**
         * Конструктор ViewHolder.
         *
         * @param view представление элемента списка
         */
        ViewHolder(View view) {
            super(view);
            center = view.findViewById(R.id.center);
        }
    }
}
