package com.vitalii.android.handwriting

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.vitalii.android.handwriting.recognition.StrokePoint
import kotlin.collections.ArrayList
import kotlin.math.abs

class PaintView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    val BRUSH_SIZE = 20f
    val BRUSH_COLOR = Color.BLUE

    private val TOUCH_TOLERANCE = 3f
    private val TAG = this::class.java.simpleName

    private var mX = 0f
    private var mY = 0f
    private var mStrokeId = 0
    private var mDrawablePath = ArrayList<Path>()
    private var mStrokes = ArrayList<StrokePoint>()
    private val mPaint = Paint()
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)


    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas

    init {
        mPaint.apply {
            isAntiAlias = true
            isDither = true
            color = BRUSH_COLOR
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            xfermode = null
            strokeWidth = BRUSH_SIZE
        }
    }

    fun getStrokes(): ArrayList<StrokePoint> = mStrokes

    fun init(metrics: DisplayMetrics) {
        val height = metrics.heightPixels
        val width = metrics.widthPixels

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
    }

    fun cancel() {
        Log.d(TAG, "cancel: undo last stroke $mStrokeId")

        if (mStrokeId <= 0) return
        --mStrokeId
        mStrokes.removeAll { point -> point.strokeId > mStrokeId }
        mDrawablePath.remove(mDrawablePath.last())

        Log.d(TAG, "cancel: mStrokeId=$mStrokeId, mStrokes=${mStrokes.size}, mDrawablePath=${mDrawablePath.size}")
        invalidate()
    }

    fun clear() {
        Log.d(TAG, "clear: delete input")

        mStrokeId = 0
        mDrawablePath.clear()
        mStrokes.clear()

        Log.d(TAG, "clear: mStrokes: ${mStrokes.size}")
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.save()
        mCanvas.drawColor(Color.WHITE)
        mDrawablePath.forEach { path -> mCanvas.drawPath(path, mPaint) }
        canvas?.drawBitmap(mBitmap, 0f, 0f, mBitmapPaint)
        canvas?.restore()
    }

    private fun touchStart(x: Float, y: Float) {
        Log.d(TAG, "touchStart: touch started")
        Log.v(TAG, "x:$x y:$y")

        mStrokes.add(StrokePoint(x, y, ++mStrokeId))
        Log.d(TAG, mStrokes.last().toString())
        mDrawablePath.add(Path())
        mDrawablePath.last().apply {
            reset()
            moveTo(x, y)
        }
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        Log.v(TAG, "x:$x y:$y")

        val dx = abs(x - mX)
        val dy = abs(y - mY)

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mDrawablePath.last().quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mStrokes.add(StrokePoint(x, y, mStrokeId))
            mX = x
            mY = y
        }
    }

    private fun touchUp(x: Float, y: Float) {
        Log.d(TAG, "touchUp: touch is up")
        Log.v(TAG, "x:$x y:$y")

        mStrokes.add(StrokePoint(x, y, mStrokeId))
        Log.d(TAG, mStrokes.last().toString())
        mDrawablePath.last().lineTo(x, y)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.apply {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStart(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    touchMove(x, y)
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    touchUp(x, y)
                    invalidate()
                }
            }
        }
        return true
    }
}
