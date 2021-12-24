package com.jether.nrmtuwaaye.Adapters;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jether.nrmtuwaaye.Models.plateModel;
import com.jether.nrmtuwaaye.R;

import java.util.List;

public class PlateAdapter extends RecyclerView.Adapter<PlateAdapter.PlateViewHolder> {

    private List<plateModel> plateModelList;
    private Context context;

    public PlateAdapter(List<plateModel> plateModelList, Context context) {
        this.plateModelList = plateModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_plates,parent,false);
        return new  PlateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlateViewHolder holder, int position) {

        plateModel plateModelz=plateModelList.get(position);
        Glide.with(context).load(plateModelz.getPlate_img()).into(holder.imageView5);

    }

    @Override
    public int getItemCount() {
        return plateModelList.size();
    }

    public class PlateViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView5;

        public PlateViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView5=(ImageView) itemView.findViewById(R.id.imageView5);
        }
    }
}
