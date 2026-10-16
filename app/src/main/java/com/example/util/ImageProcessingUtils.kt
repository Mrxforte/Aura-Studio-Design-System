package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import com.example.data.model.ImageAdjustments
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object ImageProcessingUtils {

    /**
     * Applies non-destructive adjustments (brightness, contrast, saturation, temperature,
     * tint, vignette, filter preset, rotation, flip) to a Bitmap.
     */
    fun applyAdjustments(
        source: Bitmap,
        adjustments: ImageAdjustments
    ): Bitmap {
        var base = source

        // Handle Rotation & Flip
        if (adjustments.rotation != 0 || adjustments.flipH || adjustments.flipV) {
            val matrix = Matrix()
            if (adjustments.rotation != 0) {
                matrix.postRotate(adjustments.rotation.toFloat())
            }
            val scaleX = if (adjustments.flipH) -1f else 1f
            val scaleY = if (adjustments.flipV) -1f else 1f
            if (scaleX != 1f || scaleY != 1f) {
                matrix.postScale(scaleX, scaleY)
            }
            base = Bitmap.createBitmap(base, 0, 0, base.width, base.height, matrix, true)
        }

        val result = Bitmap.createBitmap(base.width, base.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Combined ColorMatrix for Brightness, Contrast, Saturation, Temperature, Tint, and Filters
        val cm = ColorMatrix()

        // 1. Saturation
        val satFactor = (adjustments.saturation + 100f) / 100f // 0 to 2
        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(max(0f, satFactor))
        cm.postConcat(satMatrix)

        // 2. Brightness & Exposure
        val brightnessOffset = (adjustments.brightness + adjustments.exposure) * 1.5f
        val brightMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightnessOffset,
                0f, 1f, 0f, 0f, brightnessOffset,
                0f, 0f, 1f, 0f, brightnessOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(brightMatrix)

        // 3. Contrast
        val contrastFactor = (adjustments.contrast + 100f) / 100f // 0 to 2
        val t = (1.0f - contrastFactor) / 2.0f * 255.0f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrastFactor, 0f, 0f, 0f, t,
                0f, contrastFactor, 0f, 0f, t,
                0f, 0f, contrastFactor, 0f, t,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMatrix)

        // 4. Temperature (Warmth) & Tint
        val tempR = adjustments.temperature * 0.8f
        val tempB = -adjustments.temperature * 0.8f
        val tintG = adjustments.tint * 0.6f
        val tempMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, tempR,
                0f, 1f, 0f, 0f, tintG,
                0f, 0f, 1f, 0f, tempB,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(tempMatrix)

        // 5. Preset Filters
        val filterMatrix = getFilterMatrix(adjustments.filterPreset, adjustments.filterIntensity)
        if (filterMatrix != null) {
            cm.postConcat(filterMatrix)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }

        canvas.drawBitmap(base, 0f, 0f, paint)

        // 6. Vignette overlay if enabled
        if (adjustments.vignette > 0) {
            val vPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val radius = max(result.width, result.height) * 0.7f
            val alpha = ((adjustments.vignette / 100f) * 220).toInt()
            val shader = RadialGradient(
                result.width / 2f,
                result.height / 2f,
                radius,
                intArrayOf(Color.TRANSPARENT, Color.argb(alpha / 3, 0, 0, 0), Color.argb(alpha, 0, 0, 0)),
                floatArrayOf(0.4f, 0.75f, 1.0f),
                Shader.TileMode.CLAMP
            )
            vPaint.shader = shader
            canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), vPaint)
        }

        return result
    }

    private fun getFilterMatrix(preset: String, intensity: Float): ColorMatrix? {
        val clampedIntensity = intensity.coerceIn(0f, 1f)
        if (clampedIntensity <= 0.05f || preset == "Normal") return null

        val target = when (preset) {
            "Cinematic" -> ColorMatrix(
                floatArrayOf(
                    1.1f, 0f, 0f, 0f, 15f,
                    0f, 0.95f, 0f, 0f, 0f,
                    0f, 0f, 1.25f, 0f, 25f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "Cyberpunk" -> ColorMatrix(
                floatArrayOf(
                    1.3f, 0f, 0.2f, 0f, 20f,
                    0f, 0.8f, 0.2f, 0f, -10f,
                    0.2f, 0.2f, 1.5f, 0f, 35f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "Film Noir" -> {
                val m = ColorMatrix()
                m.setSaturation(0f)
                val c = ColorMatrix(
                    floatArrayOf(
                        1.4f, 0f, 0f, 0f, -30f,
                        0f, 1.4f, 0f, 0f, -30f,
                        0f, 0f, 1.4f, 0f, -30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                m.postConcat(c)
                m
            }
            "Vintage 70s" -> ColorMatrix(
                floatArrayOf(
                    1.2f, 0.1f, 0f, 0f, 25f,
                    0.1f, 1.0f, 0f, 0f, 10f,
                    0f, 0.1f, 0.75f, 0f, -15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "Golden Hour" -> ColorMatrix(
                floatArrayOf(
                    1.3f, 0.1f, 0f, 0f, 30f,
                    0.1f, 1.1f, 0f, 0f, 15f,
                    0f, 0f, 0.8f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "Pastel" -> ColorMatrix(
                floatArrayOf(
                    0.9f, 0.1f, 0.1f, 0f, 30f,
                    0.1f, 0.9f, 0.1f, 0f, 30f,
                    0.1f, 0.1f, 0.9f, 0f, 35f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            "Emerald" -> ColorMatrix(
                floatArrayOf(
                    0.8f, 0.1f, 0f, 0f, -10f,
                    0.1f, 1.3f, 0.1f, 0f, 25f,
                    0f, 0.1f, 1.0f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            else -> return null
        }

        // Interpolate between Identity and Target matrix according to intensity
        val identity = ColorMatrix()
        val idArr = identity.array
        val targetArr = target.array
        val blended = FloatArray(20)
        for (i in 0 until 20) {
            blended[i] = idArr[i] + (targetArr[i] - idArr[i]) * clampedIntensity
        }
        return ColorMatrix(blended)
    }

    /**
     * Performs foreground extraction by sampling edge background colors and computing
     * color distance/luminance with edge feathering. Produces true 32-bit transparent PNG.
     */
    fun removeBackground(
        source: Bitmap,
        tolerance: Float = 0.25f,
        refineEdges: Boolean = true
    ): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Sample corner/edge pixels to find the predominant background color
        val corners = listOf(
            pixels[0],
            pixels[width - 1],
            pixels[(height - 1) * width],
            pixels[width * height - 1],
            pixels[width / 2],
            pixels[(height - 1) * width + width / 2]
        )
        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        for (c in corners) {
            totalR += Color.red(c)
            totalG += Color.green(c)
            totalB += Color.blue(c)
        }
        val bgR = (totalR / corners.size).toInt()
        val bgG = (totalG / corners.size).toInt()
        val bgB = (totalB / corners.size).toInt()

        val maxDist = sqrt(255.0 * 255.0 * 3.0) * tolerance

        val outputPixels = IntArray(width * height)
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)

            val dr = (r - bgR).toDouble()
            val dg = (g - bgG).toDouble()
            val db = (b - bgB).toDouble()
            val dist = sqrt(dr * dr + dg * dg + db * db)

            if (dist < maxDist) {
                // Background pixel: make transparent with subtle edge feather
                val alpha = if (refineEdges && dist > maxDist * 0.7) {
                    val ratio = (dist - maxDist * 0.7) / (maxDist * 0.3)
                    (ratio * 255).toInt().coerceIn(0, 255)
                } else {
                    0
                }
                outputPixels[i] = Color.argb(alpha, r, g, b)
            } else {
                outputPixels[i] = color
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Erases masked region using neighborhood content-aware synthesis.
     */
    fun inpaintMask(
        source: Bitmap,
        mask: Bitmap
    ): Bitmap {
        val width = source.width
        val height = source.height
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val srcPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)
        mask.getPixels(maskPixels, 0, width, 0, 0, width, height)

        val outPixels = srcPixels.clone()

        // Multi-pass diffusion/synthesis from nearest non-masked boundary
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                // If masked (mask pixel alpha or brightness > threshold)
                if (Color.alpha(maskPixels[idx]) > 30) {
                    var rSum = 0
                    var gSum = 0
                    var bSum = 0
                    var count = 0
                    val radius = 12

                    for (dy in -radius..radius step 2) {
                        for (dx in -radius..radius step 2) {
                            val nx = (x + dx).coerceIn(0, width - 1)
                            val ny = (y + dy).coerceIn(0, height - 1)
                            val nIdx = ny * width + nx
                            if (Color.alpha(maskPixels[nIdx]) <= 30) {
                                val neighborColor = srcPixels[nIdx]
                                rSum += Color.red(neighborColor)
                                gSum += Color.green(neighborColor)
                                bSum += Color.blue(neighborColor)
                                count++
                            }
                        }
                    }

                    if (count > 0) {
                        outPixels[idx] = Color.argb(255, rSum / count, gSum / count, bSum / count)
                    }
                }
            }
        }

        result.setPixels(outPixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Saves a bitmap to local app storage and returns its absolute path.
     */
    fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        filename: String = "aura_${System.currentTimeMillis()}.png",
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): String {
        val dir = File(context.filesDir, "studio_exports").apply { mkdirs() }
        val file = File(dir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
        return file.absolutePath
    }

    /**
     * Generates a stunning procedural starter photo for testing/demoing photo editing,
     * background removal, object erasing, and templates.
     */
    fun createSampleCreativeBitmap(type: String = "portrait"): Bitmap {
        val width = 800
        val height = 800
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (type) {
            "portrait" -> {
                // Background dark studio gradient
                val bgShader = RadialGradient(
                    width * 0.5f, height * 0.35f, width * 0.8f,
                    intArrayOf(Color.rgb(45, 30, 75), Color.rgb(20, 15, 35), Color.rgb(16, 17, 20)),
                    floatArrayOf(0f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = bgShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                // Ambient Neon light bars
                paint.color = Color.argb(80, 103, 216, 240)
                canvas.drawCircle(width * 0.25f, height * 0.4f, 120f, paint)

                paint.color = Color.argb(80, 192, 132, 252)
                canvas.drawCircle(width * 0.75f, height * 0.5f, 160f, paint)

                // Silhouette Subject Body & Head
                val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(220, 210, 240)
                }
                // Torso
                canvas.drawRoundRect(
                    RectF(width * 0.28f, height * 0.55f, width * 0.72f, height.toFloat()),
                    60f, 60f, bodyPaint
                )
                // Head
                canvas.drawOval(
                    RectF(width * 0.36f, height * 0.25f, width * 0.64f, height * 0.58f),
                    bodyPaint
                )

                // High-fashion sunglasses / eyewear
                val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        width * 0.38f, height * 0.38f, width * 0.62f, height * 0.42f,
                        Color.rgb(103, 216, 240), Color.rgb(192, 132, 252),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(
                    RectF(width * 0.38f, height * 0.36f, width * 0.62f, height * 0.42f),
                    12f, 12f, glassPaint
                )

                // Neon Aura highlight ring
                val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                    color = Color.rgb(139, 92, 246)
                }
                canvas.drawCircle(width * 0.5f, height * 0.42f, 210f, ringPaint)
            }
            "landscape" -> {
                // Sunset mountain scene with neon cyberpunk sun
                val skyShader = LinearGradient(
                    0f, 0f, 0f, height * 0.65f,
                    Color.rgb(25, 15, 45), Color.rgb(139, 92, 246),
                    Shader.TileMode.CLAMP
                )
                paint.shader = skyShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                // Radiant Sun
                paint.color = Color.rgb(245, 193, 108)
                canvas.drawCircle(width * 0.5f, height * 0.45f, 120f, paint)

                // Cyber grid floor
                paint.color = Color.rgb(16, 17, 20)
                canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), paint)

                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(120, 103, 216, 240)
                    strokeWidth = 2.5f
                }
                for (y in 0..7) {
                    val lineY = height * 0.65f + y * 35f
                    canvas.drawLine(0f, lineY, width.toFloat(), lineY, linePaint)
                }
                for (x in -5..15) {
                    val x1 = width * 0.5f + x * 20f
                    val x2 = width * 0.5f + x * 100f
                    canvas.drawLine(x1, height * 0.65f, x2, height.toFloat(), linePaint)
                }
            }
            else -> {
                // Minimalist Product Stage
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(23, 25, 30)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                // Spotlight
                val spot = RadialGradient(
                    width * 0.5f, height * 0.5f, width * 0.4f,
                    Color.argb(120, 139, 92, 246), Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                paint.shader = spot
                canvas.drawCircle(width * 0.5f, height * 0.5f, width * 0.4f, paint)
                paint.shader = null

                // Pedestal
                paint.color = Color.rgb(35, 38, 45)
                canvas.drawOval(
                    RectF(width * 0.3f, height * 0.6f, width * 0.7f, height * 0.75f),
                    paint
                )

                // Glowing geometric object on pedestal
                val objPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        width * 0.4f, height * 0.35f, width * 0.6f, height * 0.6f,
                        Color.rgb(103, 216, 240), Color.rgb(192, 132, 252),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(
                    RectF(width * 0.4f, height * 0.35f, width * 0.6f, height * 0.6f),
                    24f, 24f, objPaint
                )
            }
        }

        return bmp
    }
}
