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

public class FavoriteRecyclerAdapter extends RecyclerView.Adapter<FavoriteRecyclerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Favorite> items;

    public FavoriteRecyclerAdapter(Context context, ArrayList<Favorite> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Favorite fav = items.get(position);
        holder.txtCafeName.setText(fav.getCafeName());
        holder.txtAddress.setText(fav.getAddress());
        Glide.with(context).load(fav.getImageUrl()).into(holder.cafeImg);
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
