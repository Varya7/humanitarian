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
            centerView = view.findViewById(R.id.center);
            statusView = view.findViewById(R.id.status);
        }
    }

}