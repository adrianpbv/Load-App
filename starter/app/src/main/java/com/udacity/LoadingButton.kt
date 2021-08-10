package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var buttonText = ""
    private var progress = 0f

    private var rectangle = Rect() // Rectangle to draw the button view
    private var circleRect = RectF() // Rectangle with four points to draw the circle
    private val handle = Handler(Looper.getMainLooper()) // A handler to delay the animation


    // declare variables to cache the color values
    private var buttonNormalColor = 0
    private var buttonDownloadColor = 0
    private var circleColor = 0

    // ValueAnimator to perform the animations on the view
    private lateinit var valueAnimator: ValueAnimator

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed)
    { p, old, new ->
        when (new) {
            ButtonState.Loading -> {
                animateViews()
            }
            ButtonState.Completed -> {
                endAnimations()
            }
        }
    }

    // Paint object to make the style things on the view
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 60.0f
        typeface = Typeface.create("", Typeface.NORMAL)
    }

    init {
        isClickable = true
        buttonText = context.getString(R.string.button_name)

        // Get the view's colors
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonNormalColor = getColor(R.styleable.LoadingButton_colorOne, 0)
            buttonDownloadColor = getColor(R.styleable.LoadingButton_colorTwo, 0)
            circleColor = getColor(R.styleable.LoadingButton_colorThree, 0)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Set the background color for the normal state
        paint.color = buttonNormalColor

        // Draw the main button
        canvas.drawRect(rectangle, paint)

        // Set the color for the animation
        paint.color = buttonDownloadColor

        // Draw the custom button for the animation
        canvas.drawRect(0f, 0f, progress * widthSize.toFloat(), heightSize.toFloat(), paint)

        // Change the color to write the text of the button
        paint.color = Color.WHITE
        canvas.drawText(buttonText, widthSize.toFloat() / 2, heightSize.toFloat() / 1.7f, paint)

        // Change the color to draw the circle
        paint.color = circleColor

        // Draw the animated circle
        canvas.drawArc(circleRect, 0f, progress * 360f, true, paint)
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

        // Set up the button with the dimensions already calculated
        rectangle.set(0,0, widthSize, heightSize)

        // Set up the circle, with the center at widthSize*0.75 (x axis) & heightSize*0.5 (y axis)
        val circleRadius = 30f
        val xLeft = widthSize*0.75f - circleRadius
        val xRight = widthSize*0.75f + circleRadius
        val yTop = heightSize*0.5f - circleRadius
        val yBottom = heightSize*0.5f + circleRadius
        circleRect.set(xLeft, yTop, xRight,yBottom)

        setMeasuredDimension(w, h)
    }

    /**
     * Start the animation on the [LoadingButton]
     * Set the text and color while the animation is running
     * Call this function when the button state [ButtonState] is Loading
     */
    fun animateViews(){
        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3000L
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateInterpolator(1f)
            addUpdateListener {
                progress = animatedValue as Float
                invalidate()
            }
        }
        // change the button text before starting the animation and calling invalidate()
        buttonText = context.getText(R.string.button_loading).toString()
        valueAnimator.disableViewDuringAnimation(this)
        valueAnimator.start()
    }

    /**
     * End the animations and set up to the normal state
     * Call this function when the button state [ButtonState] has been Completed
     */
    private fun endAnimations() {
        // run the animations until it has completed
        if (valueAnimator.isRunning){
            handle.postDelayed({
                valueAnimator.end()

                // Reset the custom variables that animates the view
                buttonText = context.getString(R.string.button_name)
                progress = 0f

                // Redraw the view to the normal state
                invalidate()
            },3000 - valueAnimator.currentPlayTime)
        }
    }

    private fun ValueAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.isEnabled = true
            }
        })
    }


}