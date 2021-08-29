package com.yogeshpaliyal.timelineview;

import android.graphics.drawable.Drawable;

class SelectedArea {
    private int startStep, endStep;
    private Drawable drawable;

    public SelectedArea(Drawable drawable, int startStep, int endStep) {
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

    public int getStartStep() {
        return startStep;
    }

    public void setStartStep(int startStep) {
        this.startStep = startStep;
    }

    public int getEndStep() {
        return endStep;
    }

    public void setEndStep(int endStep) {
        this.endStep = endStep;
    }


}
