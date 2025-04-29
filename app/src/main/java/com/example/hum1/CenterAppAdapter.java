package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CenterAppAdapter extends RecyclerView.Adapter<CenterAppAdapter.ViewHolder>{
    interface OnCenterAppClickListener{

        void onCenterAppClick(CenterApp app, int position);

    }
    private final CenterAppAdapter.OnCenterAppClickListener onClickListener;

    private final LayoutInflater inflater;
    private List<CenterApp> centers;


    public CenterAppAdapter(Context context, List<CenterApp> centers, CenterAppAdapter.OnCenterAppClickListener onClickListener){
        this.centers = centers;
        this.onClickListener = onClickListener;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public CenterAppAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_items_mod, parent, false);
        return new CenterAppAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CenterAppAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CenterApp centerApp = centers.get(position);
        holder.center.setText(centerApp.getCenter());

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onCenterAppClick(centerApp, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return centers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView center;
        ViewHolder(View view){
            super(view);
            center = view.findViewById(R.id.center);
        }
    }
}
