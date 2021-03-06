package com.jether.nrmtuwaaye;

import android.widget.Filter;

import com.jether.nrmtuwaaye.Adapters.AdapterSourceList;
import com.jether.nrmtuwaaye.Models.ModelSourceList;

import java.util.ArrayList;

public class FilterSourceList extends Filter {

    private AdapterSourceList adapter;
    private ArrayList<ModelSourceList> filterList;

    public FilterSourceList(AdapterSourceList adapter, ArrayList<ModelSourceList> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results= new FilterResults();

        //check contsraint validity
        if(constraint != null && constraint.length() >0){

            //change to uppercase
            constraint = constraint.toString().toUpperCase();

            //store our filtered records
            ArrayList<ModelSourceList> filteredModels= new ArrayList<>();
            for(int i=0;i<filterList.size();i++){

                if(filterList.get(i).getName().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }

            }

            results.count=filteredModels.size();
            results.values=filteredModels;
        }else {
            results.count=filterList.size();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {
adapter.sourceLists=(ArrayList<ModelSourceList>) results.values;

//refresh list
        adapter.notifyDataSetChanged();
    }
}
