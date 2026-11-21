package io.legado.app.ui.widget.image

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import io.legado.app.R
import kotlin.math.max

class FilletImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    internal var width: Float = 0.toFloat()
    internal var height: Float = 0.toFloat()
    private var leftTopRadius: Int = 0
    private var rightTopRadius: Int = 0
    private var rightBottomRadius: Int = 0
    private var leftBottomRadius: Int = 0

    init {
        // 读取配置
        val array = context.obtainStyledAttributes(attrs, R.styleable.FilletImageView)
        val defaultRadius = resources.getDimensionPixelSize(R.dimen.corner_radius_media)
        val radius =
            array.getDimensionPixelOffset(R.styleable.FilletImageView_radius, defaultRadius)
        leftTopRadius = array.getDimensionPixelOffset(
            R.styleable.FilletImageView_left_top_radius,
            radius
        )
        rightTopRadius = array.getDimensionPixelOffset(
            R.styleable.FilletImageView_right_top_radius,
            radius
        )
        rightBottomRadius =
            array.getDimensionPixelOffset(
                R.styleable.FilletImageView_right_bottom_radius,
                radius
            )
        leftBottomRadius = array.getDimensionPixelOffset(
            R.styleable.FilletImageView_left_bottom_radius,
            radius
        )

        array.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        width = getWidth().toFloat()
        height = getHeight().toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        //这里做下判断，只有图片的宽高大于设置的圆角距离的时候才进行裁剪
        val maxLeft = max(leftTopRadius, leftBottomRadius)
        val maxRight = max(rightTopRadius, rightBottomRadius)
        val minWidth = maxLeft + maxRight
        val maxTop = max(leftTopRadius, rightTopRadius)
        val maxBottom = max(leftBottomRadius, rightBottomRadius)
        val minHeight = maxTop + maxBottom
        if (width >= minWidth && height > minHeight) {
            @SuppressLint("DrawAllocation") val path = Path()
            //四个角：右上，右下，左下，左上
            path.moveTo(leftTopRadius.toFloat(), 0f)
            path.lineTo(width - rightTopRadius, 0f)
            path.quadTo(width, 0f, width, rightTopRadius.toFloat())

            path.lineTo(width, height - rightBottomRadius)
            path.quadTo(width, height, width - rightBottomRadius, height)

            path.lineTo(leftBottomRadius.toFloat(), height)
            path.quadTo(0f, height, 0f, height - leftBottomRadius)

            path.lineTo(0f, leftTopRadius.toFloat())
            path.quadTo(0f, 0f, leftTopRadius.toFloat(), 0f)

            canvas.clipPath(path)
        }
        super.onDraw(canvas)
    }

}
