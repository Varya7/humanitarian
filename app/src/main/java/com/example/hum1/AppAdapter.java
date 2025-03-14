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

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder>{

    interface OnAppClickListener{
        void onAppClick(Application app, int position);
    }

    private final OnAppClickListener onClickListener;

    private final LayoutInflater inflater;
    private List<com.example.hum1.Application> applications;// = Collections.emptyList();

    AppAdapter(Context context, List<Application> applications, OnAppClickListener onClickListener) {
        this.applications = applications;
        this.onClickListener = onClickListener;
        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public AppAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_items, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(AppAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Application app = applications.get(position);
        holder.date.setText("Дата: " + app.getDate());
        holder.time.setText("Время: " + app.getTime());

        //holder.email.setText(app.getEmail());
        //holder.name.setText(app.getName());
        //holder.surname.setText(app.getSurname());
        //holder.phone_number.setText(app.getPhone_number());
        //holder.birth.setText(app.getBirth());
        //holder.family_members.setText(app.getFamily_members());
        //holder.list.setText(app.getList());
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onAppClick(app, position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return applications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView date, time, email, fio, phone_number, birth, family_members, list;;


        ViewHolder(View view){
            super(view);
            date = view.findViewById(R.id.date);
            time = view.findViewById(R.id.time);
            email = view.findViewById(R.id.email);
            fio = view.findViewById(R.id.fio);
            phone_number = view.findViewById(R.id.phone_number);
            birth = view.findViewById(R.id.birth);
            family_members = view.findViewById(R.id.family_members);
            list = view.findViewById(R.id.list);
        }
    }
}
