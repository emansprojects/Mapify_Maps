package com.vc.mapify.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.vc.mapify.GooglePlaceModel;
import com.vc.mapify.NearLocationInterface;
import com.vc.mapify.R;
import com.vc.mapify.databinding.PlaceItemLayoutBinding;

import java.util.List;

//CLASS TO SHOW PLACES THAT ARE CLOSE TO THE USER'S LOCATION
public class GooglePlaceAdapter extends RecyclerView.Adapter<GooglePlaceAdapter.ViewHolder> {

    //LIST AND INTERFACE DECLARED
    private List<GooglePlaceModel> googlePlaceModels;
    private NearLocationInterface nearLocationInterface;

    //CONSTRUCTOR SET TO GET THE LOCATION OF THE USER
    public GooglePlaceAdapter(NearLocationInterface nearLocationInterface) {
        this.nearLocationInterface = nearLocationInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //GETS THE LAYOUT OF THE PLACE AND SETS THE CONTENT TO MATCH THE LAYOUT
        PlaceItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.place_item_layout, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //GETS THE PLACES ON THE MAP
        if (googlePlaceModels != null) {
            GooglePlaceModel placeModel = googlePlaceModels.get(position);
            holder.binding.setGooglePlaceModel(placeModel);
            holder.binding.setListener(nearLocationInterface);
        }

    }

    @Override
    public int getItemCount() {
        if (googlePlaceModels != null)
            //RETURNS THE LIST OF PLACES AVAILABLE
            return googlePlaceModels.size();
        else
            return 0;
    }

    //GETS AND SETS THE PLACES AVAILABLE
    public void setGooglePlaceModels(List<GooglePlaceModel> googlePlaceModels) {
        this.googlePlaceModels = googlePlaceModels;
        notifyDataSetChanged();
    }

    //BINDS THE PLACES AND DATA AVAILABLE ON THE MAP TO THE LAYOUT
    public class ViewHolder extends RecyclerView.ViewHolder {
        private PlaceItemLayoutBinding binding;

        public ViewHolder(@NonNull PlaceItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

