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

public class CenterAdapter extends RecyclerView.Adapter<CenterAdapter.ViewHolder>{

    interface OnCenterClickListener{
        void onCenterClick(Center center, int position);
    }
    private final CenterAdapter.OnCenterClickListener onClickListener;

    private final LayoutInflater inflater;
    private List<com.example.hum1.Center> centers;

    CenterAdapter(Context context, List<Center> centers, CenterAdapter.OnCenterClickListener onClickListener) {
        this.centers = centers;
        this.onClickListener = onClickListener;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public CenterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_items3, parent, false);
        return new CenterAdapter.ViewHolder(view);
    }

    public void updateList(ArrayList<Center> newList) {
        centers = newList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(CenterAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Center center = centers.get(position);
        holder.center_name.setText(center.getCenter_name());
        holder.address.setText("Адрес: " + center.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onCenterClick(center, position);
            }
        });
    }
    @Override
    public int getItemCount() {
        return centers.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView center_name, address;//, email, fio, phone_number, list;;


        ViewHolder(View view){
            super(view);
            center_name = view.findViewById(R.id.center_name);
            address = view.findViewById(R.id.address);
        }
    }
}
