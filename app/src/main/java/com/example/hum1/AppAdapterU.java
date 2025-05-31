package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Адаптер для отображения списка заявок пользователей в RecyclerView в MyApplications.
 * Обеспечивает привязку данных о заявках к элементам интерфейса и обработку пользовательских кликов.
 */
public class AppAdapterU extends RecyclerView.Adapter<AppAdapterU.ViewHolder> {

    /**
     * Интерфейс обработчика кликов по элементам списка заявок.
     */

    interface OnAppClickListener{

        /**
         * Вызывается при клике на элемент списка заявок.
         *
         * @param app объект заявки, по которой кликнули
         * @param position позиция элемента в списке
         */
        void onAppClick(ApplicationU app, int position);
    }


    private final OnAppClickListener onClickListener;
    private final LayoutInflater inflater;
    final List<ApplicationU> applicationsU;


    /**
     * Конструктор адаптера.
     *
     * @param context контекст приложения
     * @param applicationsU список заявок пользователей для отображения
     * @param onClickListener обработчик кликов по элементам списка
     */

    AppAdapterU(Context context, List<ApplicationU> applicationsU, OnAppClickListener onClickListener) {
        this.applicationsU = applicationsU;
        this.onClickListener = (OnAppClickListener) onClickListener;
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
    public AppAdapterU.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_items2, parent, false);
        return new ViewHolder(view);
    }


    /**
     * Привязывает данные заявки к ViewHolder на указанной позиции.
     *
     * @param holder ViewHolder для привязки данных
     * @param position позиция данных в списке
     */
    @Override
    public void onBindViewHolder(AppAdapterU.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ApplicationU app = applicationsU.get(position);
        ApplicationU applicationU = applicationsU.get(position);
        holder.dateView.setText(applicationU.getDate());
        holder.timeView.setText(applicationU.getTime());
        holder.centerView.setText(applicationU.getCenter());
        holder.statusView.setText(applicationU.getStatus());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onAppClick(app, position);
            }
        });
    }


    /**
     * Возвращает общее количество элементов в списке.
     */
    @Override
    public int getItemCount() {
        return applicationsU.size();
    }

    /**
     * Класс ViewHolder для кэширования View-компонентов элементов списка.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView dateView, timeView, statusView, centerView;

        /**
         * Конструктор ViewHolder.
         *
         * @param view корневое View элемента списка
         */
        ViewHolder(View view){
            super(view);
            dateView = view.findViewById(R.id.date);
            timeView = view.findViewById(R.id.time);
            centerView = view.findViewById(R.id.center);
            statusView = view.findViewById(R.id.status);
        }
    }

}