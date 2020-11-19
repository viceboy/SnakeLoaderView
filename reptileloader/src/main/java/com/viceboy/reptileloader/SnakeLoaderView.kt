package com.viceboy.reptileloader

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.BlurMaskFilter.Blur
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.annotation.Dimension
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat

// TODO: 19-11-2020 SETUP PAINT COLOR, WIDTH, LOADER PADDING (I.E Inset) LOADER IMAGE, LOADER SPEED LIMIT AND LOADER LENGTH LIMIT
class SnakeLoaderView : RelativeLayout {

    // Required Canvas for Drawer
    private lateinit var canvas: Canvas
    private lateinit var drawer: Drawer
    private lateinit var scaleType: ScaleType

    //Tracking Value Animator
    private var valueAnimator: ValueAnimator? = null

    // Required Attributes for View
    @Dimension(unit = Dimension.PX)
    private var loaderLength = 0
    @Dimension(unit = Dimension.PX)
    private var loaderPadding = 0f
    private var loaderSpeed = 0

    private val _paintSimple = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val _paintBlur = Paint().apply {
        set(_paintSimple)
    }

    // FIXME: 19-11-2020 Rounded Rect not working
    // Store the corner radius
    private var gd = GradientDrawable()

    @Dimension(unit = Dimension.PX)
    private var cornerRadius: Float = -1f
        set(value) {
            if (field != value) {
                gd.cornerRadius = value
                field = value
            }
        }

    // Store the background color
    private var bgColor = 0
        set(value) {
            if (field != value) {
                //gd.setColor(value)
                setBackgroundColor(value)
                field = value
            }
        }

    // Setting up loader color
    private var loaderColor = 0
        set(value) {
            if (field != value) {
                _paintSimple.color = value
                _paintBlur.color = value
                _paintBlur.maskFilter = BlurMaskFilter(15f, Blur.NORMAL)
                field = value
            }
        }

    // Setting up background image
    private var loaderImage = 0
        set(value) {
            if (field != value) {
                field = value
            }
        }

    // Setting up paint stroke width
    @Dimension(unit = Dimension.PX)
    private var loaderWidth = 0f
        set(value) {
            _paintSimple.strokeWidth = value
            _paintBlur.strokeWidth = value + 10f
            field = value
        }


    //Tracking current rect dimension
    private val contentRect = Rect()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    ) {
        val view = View.inflate(context, R.layout.layout_loading, null)
        addView(view)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SnakeLoaderView)
        try {
            retrieveAttributes(typedArray)
        } finally {
            typedArray.recycle()
        }
    }


    private lateinit var bitmap: Bitmap
    private var left = 0f
    private var top = 0f
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val width = if (scaleType == ScaleType.CENTER) width / 2 else width
        val height = if (scaleType == ScaleType.CENTER) height / 4 else height

        getGlobalVisibleRect(contentRect)
        drawer.initStartAndEndCoord(contentRect)
        bitmap = resources.createScaledBitmap(loaderImage, width, height)

        this.left = if (scaleType == ScaleType.CENTER) (getWidth() - bitmap.width) / 2f else 0f
        this.top = if (scaleType == ScaleType.CENTER) (getHeight() - bitmap.height) / 2f else 0f
    }


    override fun onDraw(canvas: Canvas) {
        if (layerType != View.LAYER_TYPE_SOFTWARE) setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        canvas.drawBitmap(bitmap, this.left, this.top, null)
        this.canvas = canvas
        drawer.onDraw(canvas, _paintSimple, _paintBlur)
        super.onDraw(canvas)
    }

    /**
     * Retrieve all the attributes required by view
     */
    private fun retrieveAttributes(typedArray: TypedArray) {
        loaderLength = typedArray.getDimension(R.styleable.SnakeLoaderView_loaderLength, 10f).toInt()
        loaderWidth = typedArray.getDimension(R.styleable.SnakeLoaderView_loaderWidth, 10f)
        loaderSpeed = typedArray.getInt(R.styleable.SnakeLoaderView_loaderSpeed, 10)
        loaderPadding = typedArray.getDimension(R.styleable.SnakeLoaderView_loaderPadding,0f)
        loaderColor = typedArray.getColor(R.styleable.SnakeLoaderView_loaderColor, ContextCompat.getColor(context, android.R.color.white))
        loaderImage = typedArray.getResourceId(R.styleable.SnakeLoaderView_loaderImage, R.drawable.loading)

        scaleType = typedArray.getEnum(R.styleable.SnakeLoaderView_imageScaleType, ScaleType.CENTER)

        bgColor = typedArray.getColor(R.styleable.SnakeLoaderView_bgColor, ContextCompat.getColor(context, android.R.color.background_dark))
        //background = gd

        preConditionAttributeCheck()

        //Init Drawer
        drawer = RectDrawer(loaderLength, cornerRadius.toInt(),loaderPadding.toInt(), loaderSpeed)
    }

    /**
     * Check required attributes with invalid value
     */
    private fun preConditionAttributeCheck() {
        if (loaderLength <= 0 || loaderWidth <= 0f || loaderSpeed <= 0)
            throw IllegalArgumentException("Loader length, loader width and loader speed cannot be zero or less then zero")
    }

    /**
     * Set the paint alpha to complete visibility
     */
    private fun restorePaintAlpha() {
        _paintSimple.alpha = 255
        _paintBlur.alpha = 255
    }

    /**
     * Start the animation
     */
    fun startAnimation(onStartAnimation: () -> Unit) {
        // Restore Paint Alpha
        restorePaintAlpha()
        // Create value animator and start the animation
        valueAnimator = ValueAnimator.ofInt(0, 2 * (contentRect.height() + contentRect.height())).apply {
            duration = Long.MAX_VALUE
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART

            addUpdateListener {
                drawer.onStart()
                postInvalidate()
            }

            doOnStart {
                onStartAnimation()
            }
        }
        // Start the animation
        valueAnimator?.start()
    }

    /**
     * Stop the animation
     */
    fun stopAnimation(onEndAnimation : ()->Unit) {
        valueAnimator?.cancel()
        valueAnimator = null
        drawer.onEnd(canvas, _paintSimple, _paintBlur, this,onEndAnimation)
    }

    private enum class ScaleType {
        CENTER, FIT_XY
    }
}

/*** Extension methods ***/

/**
 * Using extension method [getEnum] from https://www.thetopsites.net/article/54341705.shtml
 */
inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
        getInt(index, -1).let {
            if (it >= 0) enumValues<T>()[it] else default
        }

/**
 * Scaling bitmap with input width and height
 */
fun Resources.createScaledBitmap(drawable: Int, width: Int, height: Int): Bitmap {
    val bitmap = BitmapFactory.decodeResource(this, drawable)
    return Bitmap.createScaledBitmap(bitmap, width, height, false)
}



