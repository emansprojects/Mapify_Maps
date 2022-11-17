package com.vc.mapify.Adapter;

import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.vc.mapify.Global;
import com.vc.mapify.Model.DirectionPlaceModel.DirectionStepModel;
import com.vc.mapify.databinding.StepItemLayoutBinding;

import java.text.DecimalFormat;
import java.util.List;

//GETS THE DATA FOR THE STEPS TO THE PLACE
public class DirectionStepAdapter extends RecyclerView.Adapter<DirectionStepAdapter.ViewHolder> {

    //VARIABLES DECLARED
    private List<DirectionStepModel> directionStepModels;
    private FirebaseAuth firebaseAuth;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //BINDS THE DATA WITH THE LAYOUT
        StepItemLayoutBinding binding = StepItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //GET CURRENT USER
        firebaseAuth = FirebaseAuth.getInstance();
        if (directionStepModels != null) {

            //GET POSITION OF THE USER
            DirectionStepModel stepModel = directionStepModels.get(position);

            //SETS THE STEPS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.binding.txtStepHtml.setText(Html.fromHtml(stepModel.getHtmlInstructions(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.binding.txtStepHtml.setText(Html.fromHtml(stepModel.getHtmlInstructions()));
            }


            //SETS THE TIME TO REACH
            holder.binding.txtStepTime.setText(stepModel.getDuration().getText());


            //IF THE USER PREFERS THE DISTANCE TYPE - KILOMETRES, DISPLAY STEPS IN KILOMETRES
            if(Global.distanceVal.matches("kilometres")){
                holder.binding.txtStepDistance.setText(stepModel.getDistance().getText());

                //IF THE USER PREFERS THE DISTANCE TYPE - MILES, DISPLAY STEPS IN MILES
            }else if(Global.distanceVal.matches("miles")){

                //GET THE DISTANCE FOR THE STEP TO A STRING
                String text = stepModel.getDistance().getText();

                //REMOVES THE 'KM' FROM THE END OF THE DISTANCE
                String text2 = text.substring(0,text.length() - 2);

                //CONVERTS THE DISTANCE TO A DOUBLE
                double num1 = Double.parseDouble(text2);
                //CONVERTS KM TO MILES
                double miles = num1 * 0.621371;

                //GET THE DISTANCE TO BE DECIMAL FORMAT - ROUND OF TO 2 DIGITS
                DecimalFormat df = new DecimalFormat("####0.00");

                //DISPLAY FORMATTED DISPLAY IN MILES
                holder.binding.txtStepDistance.setText(df.format(miles) + " miles");
            }

        }

    }

    //GET THE LIST OF STEPS TO A PLACE
    @Override
    public int getItemCount() {

        if (directionStepModels != null)
            return directionStepModels.size();
        else
            return 0;
    }

    //SETS THE STEPS TO A PLACE
    public void setDirectionStepModels(List<DirectionStepModel> directionStepModels) {
        this.directionStepModels = directionStepModels;
        notifyDataSetChanged();
    }

    //BINDS THE DATA WITH THE LAYOUT
    public class ViewHolder extends RecyclerView.ViewHolder {
        private StepItemLayoutBinding binding;

        public ViewHolder(@NonNull StepItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

