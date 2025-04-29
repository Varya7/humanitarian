package com.example.hum1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListU2Adapter extends RecyclerView.Adapter<ListU2Adapter.ViewHolder>{
    private List<ListU2> items;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public EditText editText;

        public ViewHolder(View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.margin);
        }
    }
    public ListU2Adapter(List<ListU2> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ListU2Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items6, parent, false);
        return new ListU2Adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListU2Adapter.ViewHolder holder, int position) {
        ListU2 item = items.get(position);
        holder.editText.setHint(item.getMargin());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<ListU2> newList) {
        items = newList;
        notifyDataSetChanged();
    }
}