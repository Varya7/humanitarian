package com.example.hum1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListU3Adapter extends RecyclerView.Adapter<ListU3Adapter.ViewHolder> {

    private List<ListU3> listItems;

    public ListU3Adapter(List<ListU3> listItems) {
        this.listItems = listItems;
    }

    @NonNull
    @Override
    public ListU3Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items7, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListU3Adapter.ViewHolder holder, int position) {
        ListU3 item = listItems.get(position);
        holder.labelTextView.setText(item.getLabel());
        holder.valueTextView.setText(item.getValue());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView labelTextView;
        TextView valueTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            labelTextView = itemView.findViewById(R.id.label);
            valueTextView = itemView.findViewById(R.id.value);
        }
    }
}
