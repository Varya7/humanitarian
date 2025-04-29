package com.example.hum1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ListUAdapter extends RecyclerView.Adapter<ListUAdapter.ViewHolder> {
    private List<ListU> items;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.margin);
        }
    }

    public ListUAdapter(List<ListU> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_items5, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListU item = items.get(position);
        holder.textView.setText(item.getMargin());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    public void updateList(List<ListU> newList) {
        items = newList;
        notifyDataSetChanged();
    }
}