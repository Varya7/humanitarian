package com.example.hum1.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.classes.Application;
import com.example.hum1.R;

import java.util.List;

/**
 * Адаптер для отображения списка заявок в RecyclerView в активности MainActivity2.
 * Обеспечивает привязку данных о заявках к элементам списка и обработку кликов.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder>{

    /**
     * Интерфейс для обработки кликов по элементам списка заявок.
     */
    public interface OnAppClickListener{
        /**
         * Вызывается при клике на элемент списка.
         *
         * @param app Заявка, по которой был выполнен клик
         * @param position Позиция заявки в списке
         */
        void onAppClick(Application app, int position);
    }

    private final OnAppClickListener onClickListener;

    private final LayoutInflater inflater;
    List<Application> applications;

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст приложения
     * @param applications Список заявок для отображения
     * @param onClickListener Обработчик кликов по элементам списка
     */

    public AppAdapter(Context context, List<Application> applications, OnAppClickListener onClickListener) {
        this.applications = applications;
        this.onClickListener = onClickListener;
        this.inflater = LayoutInflater.from(context);
    }


    /**
     * Создает новый объект ViewHolder при необходимости.
     *
     * @param parent Родительская ViewGroup
     * @param viewType Тип View
     * @return Новый экземпляр ViewHolder
     */

    @Override
    public AppAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_items, parent, false);
        return new ViewHolder(view);
    }


    /**
     * Привязывает данные заявки к ViewHolder на указанной позиции.
     *
     * @param holder ViewHolder, к которому привязываются данные
     * @param position Позиция данных в списке
     */

    @Override
    public void onBindViewHolder(AppAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Application app = applications.get(position);
        holder.fio.setText(app.getFIO());
        holder.date.setText(app.getDate());
        holder.time.setText(app.getTime());


        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onAppClick(app, position);
            }
        });
    }


    /**
     * Возвращает общее количество элементов в списке.
     *
     * @return Количество заявок в списке
     */

    @Override
    public int getItemCount() {
        return applications.size();
    }


    /**
     * ViewHolder для кэширования View-компонентов элементов списка.
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView date, time, email, fio, phone_number, birth, list;;


        /**
         * Конструктор ViewHolder.
         *
         * @param view Корневое View элемента списка
         */

        ViewHolder(View view){
            super(view);
            date = view.findViewById(R.id.date);
            time = view.findViewById(R.id.time);
            email = view.findViewById(R.id.email);
            fio = view.findViewById(R.id.fio);
            phone_number = view.findViewById(R.id.phone_number);
            birth = view.findViewById(R.id.birth);
            //family_members = view.findViewById(R.id.family_members);
            list = view.findViewById(R.id.list);
        }
    }
}
