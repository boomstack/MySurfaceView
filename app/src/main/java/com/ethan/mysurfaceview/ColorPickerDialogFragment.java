package com.ethan.mysurfaceview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;

/**
 * Created by wangkangfei on 16/3/14.
 */
public class ColorPickerDialogFragment extends DialogFragment {
    ColorPicker picker;//颜色
    int pickedColor;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.layout_fragment_colorpicker, null);
        picker = (ColorPicker) convertView.findViewById(R.id.picker);
        picker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                pickedColor = color;
            }
        });
        builder.setView(convertView).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OnColorChosenListener li = (OnColorChosenListener) getActivity();
                if (pickedColor != 0) {
                    li.onColorChosen(pickedColor);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogDismiss();
            }
        });
        return builder.create();
    }


    public void dialogDismiss() {
        this.dismiss();
    }

    public interface OnColorChosenListener {
        void onColorChosen(int color);
    }
}
