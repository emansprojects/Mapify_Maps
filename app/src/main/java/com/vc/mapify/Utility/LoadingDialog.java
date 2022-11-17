package com.vc.mapify.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.vc.mapify.R;
import com.vc.mapify.databinding.DialogLayoutBinding;

//CLASS TO DISPLAY THE LOADING DIALOG
public class LoadingDialog {

    //VARIABLES DECLARED
    private Activity activity;
    private AlertDialog alertDialog;

    //CONSTRUCTOR TO SET THE DIALOG
    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    //THE DIALOG INDICATES LOADING
    public void startLoading() {
        //SETS THE STYLE OF THE DIALOG
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.loadingDialogStyle);

        //BINDS THE DIALOG TO THE RELATED ACTIVITY
        DialogLayoutBinding binding = DialogLayoutBinding.inflate(LayoutInflater.from(activity), null, false);
        builder.setView(binding.getRoot());
        builder.setCancelable(false);
        alertDialog = builder.create();
        binding.rotateLoading.start();
        //SHOWS THE DIALOG
        alertDialog.show();
    }

    //DISMISSES THE LOADING DIALOG
    public void stopLoading() {
        alertDialog.dismiss();
    }
}
