package com.jether.nrmtuwaaye.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jether.nrmtuwaaye.FilterSourceList;
import com.jether.nrmtuwaaye.Models.ModelSourceList;
import com.jether.nrmtuwaaye.R;

import java.util.ArrayList;

public class AdapterSourceList extends RecyclerView.Adapter<AdapterSourceList.HolderSourceList> implements Filterable {

    private Context context;
    public ArrayList<ModelSourceList> sourceLists,filterList;
    private FilterSourceList filter;

    public AdapterSourceList(Context context, ArrayList<ModelSourceList> sourceLists) {
        this.context = context;
        this.sourceLists = sourceLists;
        this.filterList = sourceLists;
    }

    @NonNull
    @Override
    public HolderSourceList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view=LayoutInflater.from(context).inflate(R.layout.row_source,parent,false);
        return new HolderSourceList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderSourceList holder, int position) {
        //get data
        ModelSourceList model=sourceLists.get(position);
        String id=model.getId();
        String name=model.getName();
        String description=model.getDescription();
        String country=model.getCountry();
        String category=model.getCategory();
        String language=model.getLanguage();

        //set ui views
        holder.nameTv.setText(name);
        holder.descriptionTv.setText(description);
        holder.countryTv.setText("Country:"+country);
        holder.categoryTv.setText("Category:"+category);
        holder.languageTv.setText("Language:"+language);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return sourceLists.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter= new FilterSourceList(this,filterList);
        }
        return filter;
    }


    class HolderSourceList extends RecyclerView.ViewHolder{

        TextView nameTv,descriptionTv,countryTv,categoryTv,languageTv;
        public HolderSourceList(@NonNull View itemView) {
            super(itemView);

            nameTv=itemView.findViewById(R.id.nameTv);
            descriptionTv=itemView.findViewById(R.id.descriptionTv);
            countryTv=itemView.findViewById(R.id.countryTv);
           categoryTv =itemView.findViewById(R.id.categoryTv);
            languageTv=itemView.findViewById(R.id.languageTv);
        }
    }
}
