package com.yogeshpaliyal.timelineview

import android.graphics.drawable.Drawable

class Thumb {
    private var value: Float
    private var position: Float
    var drawable: Drawable? = null

    constructor() {
        value = 0f
        position = 0f
    }

    constructor(drawable: Drawable?) {
        value = 0f
        position = 0f
        // Clone the drawable so we can set the states individually
        this.drawable = drawable
    }

    fun getPosition(): Float {
        return position
    }

    fun setPosition(position: Float) {
        this.position = position
        // Update value based on new position
        value = position
    }

    fun getValue(): Float {
        return value
    }

    fun setValue(value: Float) {
        this.value = value
        position = value
    }
}