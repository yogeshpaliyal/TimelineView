package com.yogeshpaliyal.timelineview;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

public class Thumb {
    private float value;
    private float position;
    private Drawable drawable;

    public Thumb(){
        value = 0;
        position = 0;
    }

    public Thumb(@Nullable Drawable drawable) {
        value = 0;
        position = 0;
        // Clone the drawable so we can set the states individually
        this.drawable = drawable;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
        // Update value based on new position
        this.value = position;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
        this.position = value;
    }

}