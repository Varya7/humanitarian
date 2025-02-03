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

public class AppAdapterU extends RecyclerView.Adapter<AppAdapterU.ViewHolder> {
    interface OnAppClickListener{
        void onAppClick(ApplicationU app, int position);
    }


    private final OnAppClickListener onClickListener;
    private final LayoutInflater inflater;
    private final List<ApplicationU> applicationsU;



    AppAdapterU(Context context, List<ApplicationU> applicationsU, OnAppClickListener onClickListener) {
        this.applicationsU = applicationsU;
        this.onClickListener = (OnAppClickListener) onClickListener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public AppAdapterU.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_items2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AppAdapterU.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ApplicationU app = applicationsU.get(position);
        ApplicationU applicationU = applicationsU.get(position);
        holder.dateView.setText("Дата: " + applicationU.getDate());
        holder.timeView.setText("Время: " +applicationU.getTime());
        //holder.nameView.setText(applicationU.getName());
        //holder.surnameView.setText(applicationU.getSurname());
        //holder.emailView.setText(applicationU.getEmail());
        //holder.phone_numberView.setText(applicationU.getPhone_number());
        //holder.birthView.setText(applicationU.getBirth());
        //holder.family_membersView.setText(applicationU.getFamily_members());
        //holder.listView.setText(applicationU.getFamily_members());
        holder.centerView.setText(applicationU.getCenter());
        holder.statusView.setText(applicationU.getStatus());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onAppClick(app, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applicationsU.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView dateView, timeView, statusView, centerView;
        ViewHolder(View view){
            super(view);
            dateView = view.findViewById(R.id.date);
            timeView = view.findViewById(R.id.time);
            //nameView = view.findViewById(R.id.name);
            //surnameView = view.findViewById(R.id.surname);
            //emailView = view.findViewById(R.id.email);
            //phone_numberView = view.findViewById(R.id.phone_number);
            //birthView = view.findViewById(R.id.birth);
            //family_membersView = view.findViewById(R.id.family_members);
            //listView = view.findViewById(R.id.list);
            centerView = view.findViewById(R.id.center);
            statusView = view.findViewById(R.id.status);
        }
    }

}