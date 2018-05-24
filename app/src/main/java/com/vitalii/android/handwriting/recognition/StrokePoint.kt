package com.vitalii.android.handwriting.recognition

data class StrokePoint(var x: Float, var y: Float, var strokeId: Int) {
    constructor() : this(0f, 0f, 0)
}

