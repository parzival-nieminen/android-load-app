package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
    private var valueAnimator = ValueAnimator()

    private var colorBar = 0
    private var colorCircle = 0
    private var progress = 0f

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0).apply {
            colorBar = getColor(R.styleable.LoadingButton_colorBar, 0)
            colorCircle = getColor(R.styleable.LoadingButton_colorCircle, 0)
        }
    }

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> {
                if (valueAnimator.isRunning) {
                    valueAnimator.cancel()
                }
                valueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
                    setAnimatorListeners()
                    repeatCount = 1
                    repeatMode = ValueAnimator.REVERSE
                    duration = 700
                    start()
                }
                invalidate()
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
                progress = 0f
                invalidate()
            }
            ButtonState.Loading -> {
                if (valueAnimator.isRunning) {
                    valueAnimator.cancel()
                }
                valueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
                    setAnimatorListeners()
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.REVERSE
                    duration = 700
                    start()
                }
                invalidate()
            }
        }
    }

    private fun ValueAnimator.setAnimatorListeners() {
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                isEnabled = true
                progress = 0f
            }
        })
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
        color = colorBar
    }

    private val paintCircleBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val paintProgressCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorCircle
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
        val progressWidth = widthSize * (progress / 100)
        canvas?.apply {
            drawRect(0f, 0f, progressWidth, heightSize.toFloat(), paintProgress)
        }
        // default circle background
        val centerY = heightSize.toFloat() / 2.0f
        val centerX = widthSize.toFloat() / 2.0f
        val padding = heightSize.toFloat() * 0.1f
        val radius = centerY - padding

        // progress circle
        val widthOffset = widthSize.toFloat() - 2 * radius - 2 * padding
        val circle = RectF().apply {
            top = padding
            left = padding + widthOffset
            bottom = 2.0f * radius + padding
            right = 2.0f * radius + padding + widthOffset
        }
        canvas?.apply {
            drawArc(circle, 0f, (progress / 100) * 360f, true, paintProgressCircle)
        }
        // paint text
        var textBox = Rect()
        val text = "Download"
        buttonText.getTextBounds(text, 0, text.length, textBox)
        canvas?.apply {
            drawText(
                text,
                centerX - textBox.centerX(),
                centerY - textBox.centerY(),
                buttonText
            )
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

    fun setState(newState: ButtonState) {
        buttonState = newState
    }

}