package com.example.boltparty;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class BookedRecyclerAdapter extends RecyclerView.Adapter<BookedRecyclerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Booked> items;
    private ItemClicked listener;

    public BookedRecyclerAdapter(Context context, ArrayList<Booked> items, ItemClicked listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        view.setOnClickListener(v -> {
            listener.onItemClicked(items.get(holder.getAdapterPosition()));
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Booked booked = items.get(position);
        holder.txtCafeName.setText(booked.getCafeName());
        holder.txtAddress.setText(booked.getAddress());
        Glide.with(context).load(booked.getImageUrl()).into(holder.cafeImg);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        protected TextView txtCafeName, txtAddress;
        protected ShapeableImageView cafeImg;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCafeName = itemView.findViewById(R.id.txtCafeName);
            txtAddress = itemView.findViewById(R.id.txtCafeAddress);
            cafeImg = itemView.findViewById(R.id.cafe_img);
        }
    }
}
