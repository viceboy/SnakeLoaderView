package com.viceboy.reptileloader

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

interface Drawer {
    fun onDraw(canvas: Canvas, paintSimple: Paint, paintBlur: Paint)
    fun onStart()
    fun onEnd(canvas: Canvas, paintSimple: Paint, paintBlur: Paint, snakeLoaderView: SnakeLoaderView, onEndAnimation: () -> Unit)
    fun initStartAndEndCoord(contentRect: Rect)
}