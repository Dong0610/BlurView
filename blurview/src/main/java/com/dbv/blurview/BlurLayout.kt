package com.dbv.blurview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.Px
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.withTranslation
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap

class BlurLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    fun interface CornerRadiusProvider {
        fun provideCornerRadius(): Float
    }

    fun interface BlurRadiusProvider {
        fun provideBlurRadius(): Int
    }

    companion object {
        private lateinit var onApplyBlur: (bitmap: Bitmap, blurRadius: Int) -> Bitmap

        fun init(
            onApplyBlur: (bitmap: Bitmap, blurRadius: Int) -> Bitmap,
        ) {
            this.onApplyBlur = onApplyBlur
        }
    }

    private var targetChildId: Int? = null
    private lateinit var targetChild: View

    private var blurRadius: Int? = null
        get() = (targetChild as? BlurRadiusProvider)?.provideBlurRadius() ?: field

    @Px
    private var targetChildBackgroundCornerRadius: Float = 0f
        get() = (targetChild as? CornerRadiusProvider)?.provideCornerRadius() ?: field

    init {
        context.withStyledAttributes(attrs, R.styleable.BlurLayout) {
            targetChildId = getResourceId(
                R.styleable.BlurLayout_targetChildId,
                0
            ).takeIf { it != 0 }
            blurRadius = getInt(
                R.styleable.BlurLayout_blurRadius,
                -1
            ).takeIf { it != -1 }
            targetChildBackgroundCornerRadius = getDimension(
                R.styleable.BlurLayout_childBackgroundCornerRadius,
                0f
            )
        }

        applyBlurIfSingleChild()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        init(
            onApplyBlur = { bitmap, blurRadius ->
                Toolkit.blur(inputBitmap = bitmap, radius = blurRadius)
            },
        )
    }

    fun setBlurredBackgroundForTargetChild(
        targetChild: View? = null,
        blurRadius: Int? = null,
        @Px targetChildBackgroundCornerRadius: Float = 0f,
    ) {
        this.targetChild = targetChild ?: children.singleOrNull()
                ?: error("targetChild can be omitted only when there's a single child in BlurLayout")
        this.blurRadius = blurRadius
        this.targetChildBackgroundCornerRadius = targetChildBackgroundCornerRadius

        applyBlur()
    }

    fun setBlurredBackgroundForTargetChild(
        @IdRes targetChildId: Int,
        blurRadius: Int? = null,
        @Px targetChildBackgroundCornerRadius: Float = 0f,
    ) {
        setBlurredBackgroundForTargetChild(
            targetChild = findViewById(targetChildId),
            blurRadius = blurRadius,
            targetChildBackgroundCornerRadius = targetChildBackgroundCornerRadius
        )
    }


    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        if (child.id == targetChildId) {
            targetChild = child
            applyBlur()
        }
    }

    private fun applyBlurIfSingleChild() {
        doOnLayout {
            val singleChild = children.singleOrNull()
            if (singleChild != null && !this::targetChild.isInitialized) {
                targetChild = singleChild
                applyBlur()
            }
        }
    }

    private fun applyBlur() = targetChild.doOnLayout { targetChild ->
        val blurRadius = blurRadius
            ?: error("Blur radius must be specified explicitly or should be provided by child implementing BlurRadiusProvider")

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        background?.apply {
            setBounds(0, 0, width, height)
        }?.draw(canvas)

        for (child in children) {
            val isTargetChild = child == targetChild
            canvas.withTranslation(child.left.toFloat(), child.top.toFloat()) {
                if (!isTargetChild) {
                    child.draw(canvas)
                } else {
                    targetChild.background?.apply {
                        setBounds(0, 0, targetChild.width, targetChild.height)
                    }?.draw(canvas)
                }
            }
            if (isTargetChild) break
        }

        val croppedBitmap =
            Bitmap.createBitmap(
                bitmap,
                targetChild.left,
                targetChild.top,
                targetChild.width,
                targetChild.height
            )
        bitmap.recycle()
        val blurredBackgroundBitmap = onApplyBlur(croppedBitmap, blurRadius)
        croppedBitmap.recycle()

        targetChild.background = if (targetChildBackgroundCornerRadius > 0) {
            RoundedBitmapDrawableFactory.create(context.resources, blurredBackgroundBitmap).apply {
                cornerRadius = targetChildBackgroundCornerRadius
            }
        } else {
            blurredBackgroundBitmap.toDrawable(context.resources)
        }
    }
}