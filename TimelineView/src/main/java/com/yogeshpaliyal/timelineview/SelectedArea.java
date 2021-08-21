package com.yogeshpaliyal.timelineview;

import android.graphics.drawable.Drawable;

class SelectedArea {
    private float startStep, endStep;
    private Drawable drawable;

    public SelectedArea(Drawable drawable, float startStep, float endStep) {
        this.startStep = startStep;
        this.endStep = endStep;
        this.drawable = drawable;
    }


    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public float getStartStep() {
        return startStep;
    }

    public void setStartStep(float startStep) {
        this.startStep = startStep;
    }

    public float getEndStep() {
        return endStep;
    }

    public void setEndStep(float endStep) {
        this.endStep = endStep;
    }


}
