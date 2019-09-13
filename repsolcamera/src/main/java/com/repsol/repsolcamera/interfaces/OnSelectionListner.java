package com.repsol.repsolcamera.interfaces;

import android.view.View;

import com.repsol.repsolcamera.modals.Img;


public interface OnSelectionListner {
    void OnClick(Img Img, View view, int position);

    void OnLongClick(Img img, View view, int position);
}