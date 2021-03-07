package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var downloadProgress = 0f

    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
    }

    init {
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private val paintButtonBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    private val paintCircleBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val paintProgressCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }

    private var buttonText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textSize = 48f
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //default background
        canvas?.apply {
            drawPaint(paintButtonBackground)
        }
        // progress background
        downloadProgress = 75f
        val progressWidth = widthSize * (downloadProgress / 100)
        canvas?.apply {
            drawRect(0f, 0f, progressWidth, heightSize.toFloat(), paintProgress)
        }
        // default circle background
        val padding = heightSize.toFloat() * 0.1f
        val radius = (heightSize.toFloat() / 2.0f) - padding
        canvas?.apply {
            drawCircle(widthSize.toFloat() - (radius + padding), heightSize.toFloat() / 2.0f, radius, paintCircleBackground)
        }

        // progress circle
        val widthOffset = widthSize.toFloat() - 2 * radius - 2 * padding
        val circle = RectF().apply {
            top = padding
            left = padding + widthOffset
            bottom = 2.0f * radius + padding
            right = 2.0f * radius + padding + widthOffset
        }
        canvas?.apply {
            drawArc(circle, 0f, (downloadProgress / 100) * 360f, true, paintProgressCircle)
        }
        // paint text
        var textBox = Rect()
        val text = "Download"
        buttonText.getTextBounds(text, 0, text.length, textBox)
        canvas?.apply {
            drawText(text, widthSize.toFloat() / 2.0f - textBox.centerX(), heightSize.toFloat() / 2.0f - textBox.centerY(), buttonText)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
                MeasureSpec.getSize(w),
                heightMeasureSpec,
                0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}