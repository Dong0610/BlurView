package com.dbv.blurview

import android.graphics.Bitmap
import java.lang.IllegalArgumentException
import androidx.core.graphics.createBitmap

private const val externalName = "RenderScript Toolkit"

object Toolkit {
    @JvmOverloads
    fun blend(
        mode: BlendingMode,
        sourceArray: ByteArray,
        destArray: ByteArray,
        sizeX: Int,
        sizeY: Int,
        restriction: Range2d? = null
    ) {
        require(sourceArray.size >= sizeX * sizeY * 4) {
            "$externalName blend. sourceArray is too small for the given dimensions. " +
                    "$sizeX*$sizeY*4 < ${sourceArray.size}."
        }
        require(destArray.size >= sizeX * sizeY * 4) {
            "$externalName blend. sourceArray is too small for the given dimensions. " +
                    "$sizeX*$sizeY*4 < ${sourceArray.size}."
        }
        validateRestriction("blend", sizeX, sizeY, restriction)

        nativeBlend(nativeHandle, mode.value, sourceArray, destArray, sizeX, sizeY, restriction)
    }

    @JvmOverloads
    fun blend(
        mode: BlendingMode,
        sourceBitmap: Bitmap,
        destBitmap: Bitmap,
        restriction: Range2d? = null
    ) {
        validateBitmap("blend", sourceBitmap)
        validateBitmap("blend", destBitmap)
        require(
            sourceBitmap.width == destBitmap.width &&
                    sourceBitmap.height == destBitmap.height
        ) {
            "$externalName blend. Source and destination bitmaps should be the same size. " +
                    "${sourceBitmap.width}x${sourceBitmap.height} and " +
                    "${destBitmap.width}x${destBitmap.height} provided."
        }
        require(sourceBitmap.config == destBitmap.config) {
            "RenderScript Toolkit blend. Source and destination bitmaps should have the same " +
                    "config. ${sourceBitmap.config} and ${destBitmap.config} provided."
        }
        validateRestriction("blend", sourceBitmap.width, sourceBitmap.height, restriction)

        nativeBlendBitmap(nativeHandle, mode.value, sourceBitmap, destBitmap, restriction)
    }

    @JvmOverloads
    fun blur(
        inputArray: ByteArray,
        vectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        radius: Int = 5,
        restriction: Range2d? = null
    ): ByteArray {
        require(vectorSize == 1 || vectorSize == 4) {
            "$externalName blur. The vectorSize should be 1 or 4. $vectorSize provided."
        }
        require(inputArray.size >= sizeX * sizeY * vectorSize) {
            "$externalName blur. inputArray is too small for the given dimensions. " +
                    "$sizeX*$sizeY*$vectorSize < ${inputArray.size}."
        }
        require(radius in 1..25) {
            "$externalName blur. The radius should be between 1 and 25. $radius provided."
        }
        validateRestriction("blur", sizeX, sizeY, restriction)

        val outputArray = ByteArray(inputArray.size)
        nativeBlur(
            nativeHandle, inputArray, vectorSize, sizeX, sizeY, radius, outputArray, restriction
        )
        return outputArray
    }

    @JvmOverloads
    fun blur(inputBitmap: Bitmap, radius: Int = 5, restriction: Range2d? = null): Bitmap {
        validateBitmap("blur", inputBitmap)
        require(radius in 1..25) {
            "$externalName blur. The radius should be between 1 and 25. $radius provided."
        }
        validateRestriction("blur", inputBitmap.width, inputBitmap.height, restriction)

        val outputBitmap = createCompatibleBitmap(inputBitmap)
        nativeBlurBitmap(nativeHandle, inputBitmap, outputBitmap, radius, restriction)
        return outputBitmap
    }

    val identityMatrix: FloatArray
        get() = floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )

    val greyScaleColorMatrix: FloatArray
        get() = floatArrayOf(
            0.299f, 0.299f, 0.299f, 0f,
            0.587f, 0.587f, 0.587f, 0f,
            0.114f, 0.114f, 0.114f, 0f,
            0f, 0f, 0f, 1f
        )

    val rgbToYuvMatrix: FloatArray
        get() = floatArrayOf(
            0.299f, -0.14713f, 0.615f, 0f,
            0.587f, -0.28886f, -0.51499f, 0f,
            0.114f, 0.436f, -0.10001f, 0f,
            0f, 0f, 0f, 1f
        )

    val yuvToRgbMatrix: FloatArray
        get() = floatArrayOf(
            1f, 1f, 1f, 0f,
            0f, -0.39465f, 2.03211f, 0f,
            1.13983f, -0.5806f, 0f, 0f,
            0f, 0f, 0f, 1f
        )

    @JvmOverloads
    fun colorMatrix(
        inputArray: ByteArray,
        inputVectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        outputVectorSize: Int,
        matrix: FloatArray,
        addVector: FloatArray = floatArrayOf(0f, 0f, 0f, 0f),
        restriction: Range2d? = null
    ): ByteArray {
        require(inputVectorSize in 1..4) {
            "$externalName colorMatrix. The inputVectorSize should be between 1 and 4. " +
                    "$inputVectorSize provided."
        }
        require(outputVectorSize in 1..4) {
            "$externalName colorMatrix. The outputVectorSize should be between 1 and 4. " +
                    "$outputVectorSize provided."
        }
        require(inputArray.size >= sizeX * sizeY * inputVectorSize) {
            "$externalName colorMatrix. inputArray is too small for the given dimensions. " +
                    "$sizeX*$sizeY*$inputVectorSize < ${inputArray.size}."
        }
        require(matrix.size == 16) {
            "$externalName colorMatrix. matrix should have 16 entries. ${matrix.size} provided."
        }
        require(addVector.size == 4) {
            "$externalName colorMatrix. addVector should have 4 entries. " +
                    "${addVector.size} provided."
        }
        validateRestriction("colorMatrix", sizeX, sizeY, restriction)

        val outputArray = ByteArray(sizeX * sizeY * paddedSize(outputVectorSize))
        nativeColorMatrix(
            nativeHandle, inputArray, inputVectorSize, sizeX, sizeY, outputArray, outputVectorSize,
            matrix, addVector, restriction
        )
        return outputArray
    }

    @JvmOverloads
    fun colorMatrix(
        inputBitmap: Bitmap,
        matrix: FloatArray,
        addVector: FloatArray = floatArrayOf(0f, 0f, 0f, 0f),
        restriction: Range2d? = null
    ): Bitmap {
        validateBitmap("colorMatrix", inputBitmap)
        require(matrix.size == 16) {
            "$externalName colorMatrix. matrix should have 16 entries. ${matrix.size} provided."
        }
        require(addVector.size == 4) {
            "$externalName colorMatrix. addVector should have 4 entries."
        }
        validateRestriction("colorMatrix", inputBitmap.width, inputBitmap.height, restriction)

        val outputBitmap = createCompatibleBitmap(inputBitmap)
        nativeColorMatrixBitmap(
            nativeHandle,
            inputBitmap,
            outputBitmap,
            matrix,
            addVector,
            restriction
        )
        return outputBitmap
    }

    @JvmOverloads
    fun convolve(
        inputArray: ByteArray,
        vectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        coefficients: FloatArray,
        restriction: Range2d? = null
    ): ByteArray {
        require(vectorSize in 1..4) {
            "$externalName convolve. The vectorSize should be between 1 and 4. " +
                    "$vectorSize provided."
        }
        require(inputArray.size >= sizeX * sizeY * vectorSize) {
            "$externalName convolve. inputArray is too small for the given dimensions. " +
                    "$sizeX*$sizeY*$vectorSize < ${inputArray.size}."
        }
        require(coefficients.size == 9 || coefficients.size == 25) {
            "$externalName convolve. Only 3x3 or 5x5 convolutions are supported. " +
                    "${coefficients.size} coefficients provided."
        }
        validateRestriction("convolve", sizeX, sizeY, restriction)

        val outputArray = ByteArray(inputArray.size)
        nativeConvolve(
            nativeHandle,
            inputArray,
            vectorSize,
            sizeX,
            sizeY,
            outputArray,
            coefficients,
            restriction
        )
        return outputArray
    }

    @JvmOverloads
    fun convolve(
        inputBitmap: Bitmap,
        coefficients: FloatArray,
        restriction: Range2d? = null
    ): Bitmap {
        validateBitmap("convolve", inputBitmap)
        require(coefficients.size == 9 || coefficients.size == 25) {
            "$externalName convolve. Only 3x3 or 5x5 convolutions are supported. " +
                    "${coefficients.size} coefficients provided."
        }
        validateRestriction("convolve", inputBitmap, restriction)

        val outputBitmap = createCompatibleBitmap(inputBitmap)
        nativeConvolveBitmap(nativeHandle, inputBitmap, outputBitmap, coefficients, restriction)
        return outputBitmap
    }

    @JvmOverloads
    fun histogram(
        inputArray: ByteArray,
        vectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        restriction: Range2d? = null
    ): IntArray {
        require(vectorSize in 1..4) {
            "$externalName histogram. The vectorSize should be between 1 and 4. " +
                    "$vectorSize provided."
        }
        require(inputArray.size >= sizeX * sizeY * vectorSize) {
            "$externalName histogram. inputArray is too small for the given dimensions. " +
                    "$sizeX*$sizeY*$vectorSize < ${inputArray.size}."
        }
        validateRestriction("histogram", sizeX, sizeY, restriction)

        val outputArray = IntArray(256 * paddedSize(vectorSize))
        nativeHistogram(
            nativeHandle,
            inputArray,
            vectorSize,
            sizeX,
            sizeY,
            outputArray,
            restriction
        )
        return outputArray
    }

    @JvmOverloads
    fun histogram(
        inputBitmap: Bitmap,
        restriction: Range2d? = null
    ): IntArray {
        validateBitmap("histogram", inputBitmap)
        validateRestriction("histogram", inputBitmap, restriction)

        val outputArray = IntArray(256 * vectorSize(inputBitmap))
        nativeHistogramBitmap(nativeHandle, inputBitmap, outputArray, restriction)
        return outputArray
    }

    @JvmOverloads
    fun histogramDot(
        inputArray: ByteArray,
        vectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        coefficients: FloatArray? = null,
        restriction: Range2d? = null
    ): IntArray {
        require(vectorSize in 1..4) {
            "$externalName histogramDot. The vectorSize should be between 1 and 4. " +
                    "$vectorSize provided."
        }
        require(inputArray.size >= sizeX * sizeY * vectorSize) {
            "$externalName histogramDot. inputArray is too small for the given dimensions. " +
                    "$sizeX*$sizeY*$vectorSize < ${inputArray.size}."
        }
        validateHistogramDotCoefficients(coefficients, vectorSize)
        validateRestriction("histogramDot", sizeX, sizeY, restriction)

        val outputArray = IntArray(256)
        val actualCoefficients = coefficients ?: floatArrayOf(0.299f, 0.587f, 0.114f, 0f)
        nativeHistogramDot(
            nativeHandle,
            inputArray,
            vectorSize,
            sizeX,
            sizeY,
            outputArray,
            actualCoefficients,
            restriction
        )
        return outputArray
    }

    @JvmOverloads
    fun histogramDot(
        inputBitmap: Bitmap,
        coefficients: FloatArray? = null,
        restriction: Range2d? = null
    ): IntArray {
        validateBitmap("histogramDot", inputBitmap)
        validateHistogramDotCoefficients(coefficients, vectorSize(inputBitmap))
        validateRestriction("histogramDot", inputBitmap, restriction)

        val outputArray = IntArray(256)
        val actualCoefficients = coefficients ?: floatArrayOf(0.299f, 0.587f, 0.114f, 0f)
        nativeHistogramDotBitmap(
            nativeHandle, inputBitmap, outputArray, actualCoefficients, restriction
        )
        return outputArray
    }

    @JvmOverloads
    fun lut(
        inputArray: ByteArray,
        sizeX: Int,
        sizeY: Int,
        table: LookupTable,
        restriction: Range2d? = null
    ): ByteArray {
        require(inputArray.size >= sizeX * sizeY * 4) {
            "$externalName lut. inputArray is too small for the given dimensions. " +
                    "$sizeX*$sizeY*4 < ${inputArray.size}."
        }
        validateRestriction("lut", sizeX, sizeY, restriction)

        val outputArray = ByteArray(inputArray.size)
        nativeLut(
            nativeHandle,
            inputArray,
            outputArray,
            sizeX,
            sizeY,
            table.red,
            table.green,
            table.blue,
            table.alpha,
            restriction
        )
        return outputArray
    }

    @JvmOverloads
    fun lut(
        inputBitmap: Bitmap,
        table: LookupTable,
        restriction: Range2d? = null
    ): Bitmap {
        validateBitmap("lut", inputBitmap)
        validateRestriction("lut", inputBitmap, restriction)

        val outputBitmap = createCompatibleBitmap(inputBitmap)
        nativeLutBitmap(
            nativeHandle,
            inputBitmap,
            outputBitmap,
            table.red,
            table.green,
            table.blue,
            table.alpha,
            restriction
        )
        return outputBitmap
    }

    @JvmOverloads
    fun lut3d(
        inputArray: ByteArray,
        sizeX: Int,
        sizeY: Int,
        cube: Rgba3dArray,
        restriction: Range2d? = null
    ): ByteArray {
        require(inputArray.size >= sizeX * sizeY * 4) {
            "$externalName lut3d. inputArray is too small for the given dimensions. " +
                    "$sizeX*$sizeY*4 < ${inputArray.size}."
        }
        require(
            cube.sizeX >= 2 && cube.sizeY >= 2 && cube.sizeZ >= 2 &&
                    cube.sizeX <= 256 && cube.sizeY <= 256 && cube.sizeZ <= 256
        ) {
            "$externalName lut3d. The dimensions of the cube should be between 2 and 256. " +
                    "(${cube.sizeX}, ${cube.sizeY}, ${cube.sizeZ}) provided."
        }
        validateRestriction("lut3d", sizeX, sizeY, restriction)

        val outputArray = ByteArray(inputArray.size)
        nativeLut3d(
            nativeHandle, inputArray, outputArray, sizeX, sizeY, cube.values, cube.sizeX,
            cube.sizeY, cube.sizeZ, restriction
        )
        return outputArray
    }

    @JvmOverloads
    fun lut3d(
        inputBitmap: Bitmap,
        cube: Rgba3dArray,
        restriction: Range2d? = null
    ): Bitmap {
        validateBitmap("lut3d", inputBitmap)
        validateRestriction("lut3d", inputBitmap, restriction)

        val outputBitmap = createCompatibleBitmap(inputBitmap)
        nativeLut3dBitmap(
            nativeHandle, inputBitmap, outputBitmap, cube.values, cube.sizeX,
            cube.sizeY, cube.sizeZ, restriction
        )
        return outputBitmap
    }

    @JvmOverloads
    fun resize(
        inputArray: ByteArray,
        vectorSize: Int,
        inputSizeX: Int,
        inputSizeY: Int,
        outputSizeX: Int,
        outputSizeY: Int,
        restriction: Range2d? = null
    ): ByteArray {
        require(vectorSize in 1..4) {
            "$externalName resize. The vectorSize should be between 1 and 4. $vectorSize provided."
        }
        require(inputArray.size >= inputSizeX * inputSizeY * vectorSize) {
            "$externalName resize. inputArray is too small for the given dimensions. " +
                    "$inputSizeX*$inputSizeY*$vectorSize < ${inputArray.size}."
        }
        validateRestriction("resize", outputSizeX, outputSizeY, restriction)

        val outputArray = ByteArray(outputSizeX * outputSizeY * paddedSize(vectorSize))
        nativeResize(
            nativeHandle,
            inputArray,
            vectorSize,
            inputSizeX,
            inputSizeY,
            outputArray,
            outputSizeX,
            outputSizeY,
            restriction
        )
        return outputArray
    }

    @JvmOverloads
    fun resize(
        inputBitmap: Bitmap,
        outputSizeX: Int,
        outputSizeY: Int,
        restriction: Range2d? = null
    ): Bitmap {
        validateBitmap("resize", inputBitmap)
        validateRestriction("resize", outputSizeX, outputSizeY, restriction)

        val outputBitmap = Bitmap.createBitmap(outputSizeX, outputSizeY, Bitmap.Config.ARGB_8888)
        nativeResizeBitmap(nativeHandle, inputBitmap, outputBitmap, restriction)
        return outputBitmap
    }

    fun yuvToRgb(inputArray: ByteArray, sizeX: Int, sizeY: Int, format: YuvFormat): ByteArray {
        require(sizeX % 2 == 0 && sizeY % 2 == 0) {
            "$externalName yuvToRgb. Non-even dimensions are not supported. " +
                    "$sizeX and $sizeY were provided."
        }

        val outputArray = ByteArray(sizeX * sizeY * 4)
        nativeYuvToRgb(nativeHandle, inputArray, outputArray, sizeX, sizeY, format.value)
        return outputArray
    }

    fun yuvToRgbBitmap(inputArray: ByteArray, sizeX: Int, sizeY: Int, format: YuvFormat): Bitmap {
        require(sizeX % 2 == 0 && sizeY % 2 == 0) {
            "$externalName yuvToRgbBitmap. Non-even dimensions are not supported. " +
                    "$sizeX and $sizeY were provided."
        }

        val outputBitmap = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_8888)
        nativeYuvToRgbBitmap(nativeHandle, inputArray, sizeX, sizeY, outputBitmap, format.value)
        return outputBitmap
    }

    private var nativeHandle: Long = 0

    init {
        System.loadLibrary("renderscript-toolkit")
        nativeHandle = createNative()
    }

    fun shutdown() {
        destroyNative(nativeHandle)
        nativeHandle = 0
    }

    private external fun
            createNative(): Long

    private external fun destroyNative(nativeHandle: Long)

    private external fun nativeBlend(
        nativeHandle: Long,
        mode: Int,
        sourceArray: ByteArray,
        destArray: ByteArray,
        sizeX: Int,
        sizeY: Int,
        restriction: Range2d?
    )

    private external fun nativeBlendBitmap(
        nativeHandle: Long,
        mode: Int,
        sourceBitmap: Bitmap,
        destBitmap: Bitmap,
        restriction: Range2d?
    )

    private external fun nativeBlur(
        nativeHandle: Long,
        inputArray: ByteArray,
        vectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        radius: Int,
        outputArray: ByteArray,
        restriction: Range2d?
    )

    private external fun nativeBlurBitmap(
        nativeHandle: Long,
        inputBitmap: Bitmap,
        outputBitmap: Bitmap,
        radius: Int,
        restriction: Range2d?
    )

    private external fun nativeColorMatrix(
        nativeHandle: Long,
        inputArray: ByteArray,
        inputVectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        outputArray: ByteArray,
        outputVectorSize: Int,
        matrix: FloatArray,
        addVector: FloatArray,
        restriction: Range2d?
    )

    private external fun nativeColorMatrixBitmap(
        nativeHandle: Long,
        inputBitmap: Bitmap,
        outputBitmap: Bitmap,
        matrix: FloatArray,
        addVector: FloatArray,
        restriction: Range2d?
    )

    private external fun nativeConvolve(
        nativeHandle: Long,
        inputArray: ByteArray,
        vectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        outputArray: ByteArray,
        coefficients: FloatArray,
        restriction: Range2d?
    )

    private external fun nativeConvolveBitmap(
        nativeHandle: Long,
        inputBitmap: Bitmap,
        outputBitmap: Bitmap,
        coefficients: FloatArray,
        restriction: Range2d?
    )

    private external fun nativeHistogram(
        nativeHandle: Long,
        inputArray: ByteArray,
        vectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        outputArray: IntArray,
        restriction: Range2d?
    )

    private external fun nativeHistogramBitmap(
        nativeHandle: Long,
        inputBitmap: Bitmap,
        outputArray: IntArray,
        restriction: Range2d?
    )

    private external fun nativeHistogramDot(
        nativeHandle: Long,
        inputArray: ByteArray,
        vectorSize: Int,
        sizeX: Int,
        sizeY: Int,
        outputArray: IntArray,
        coefficients: FloatArray,
        restriction: Range2d?
    )

    private external fun nativeHistogramDotBitmap(
        nativeHandle: Long,
        inputBitmap: Bitmap,
        outputArray: IntArray,
        coefficients: FloatArray,
        restriction: Range2d?
    )

    private external fun nativeLut(
        nativeHandle: Long,
        inputArray: ByteArray,
        outputArray: ByteArray,
        sizeX: Int,
        sizeY: Int,
        red: ByteArray,
        green: ByteArray,
        blue: ByteArray,
        alpha: ByteArray,
        restriction: Range2d?
    )

    private external fun nativeLutBitmap(
        nativeHandle: Long,
        inputBitmap: Bitmap,
        outputBitmap: Bitmap,
        red: ByteArray,
        green: ByteArray,
        blue: ByteArray,
        alpha: ByteArray,
        restriction: Range2d?
    )

    private external fun nativeLut3d(
        nativeHandle: Long,
        inputArray: ByteArray,
        outputArray: ByteArray,
        sizeX: Int,
        sizeY: Int,
        cube: ByteArray,
        cubeSizeX: Int,
        cubeSizeY: Int,
        cubeSizeZ: Int,
        restriction: Range2d?
    )

    private external fun nativeLut3dBitmap(
        nativeHandle: Long,
        inputBitmap: Bitmap,
        outputBitmap: Bitmap,
        cube: ByteArray,
        cubeSizeX: Int,
        cubeSizeY: Int,
        cubeSizeZ: Int,
        restriction: Range2d?
    )

    private external fun nativeResize(
        nativeHandle: Long,
        inputArray: ByteArray,
        vectorSize: Int,
        inputSizeX: Int,
        inputSizeY: Int,
        outputArray: ByteArray,
        outputSizeX: Int,
        outputSizeY: Int,
        restriction: Range2d?
    )

    private external fun nativeResizeBitmap(
        nativeHandle: Long,
        inputBitmap: Bitmap,
        outputBitmap: Bitmap,
        restriction: Range2d?
    )

    private external fun nativeYuvToRgb(
        nativeHandle: Long,
        inputArray: ByteArray,
        outputArray: ByteArray,
        sizeX: Int,
        sizeY: Int,
        format: Int
    )

    private external fun nativeYuvToRgbBitmap(
        nativeHandle: Long,
        inputArray: ByteArray,
        sizeX: Int,
        sizeY: Int,
        outputBitmap: Bitmap,
        value: Int
    )
}

enum class BlendingMode(val value: Int) {
    CLEAR(0),
    SRC(1),
    DST(2),
    SRC_OVER(3),
    DST_OVER(4),
    SRC_IN(5),
    DST_IN(6),
    SRC_OUT(7),
    DST_OUT(8),
    SRC_ATOP(9),
    DST_ATOP(10),
    XOR(11),
    MULTIPLY(12),
    ADD(13),
    SUBTRACT(14)
}

class LookupTable {
    var red = ByteArray(256) { it.toByte() }
    var green = ByteArray(256) { it.toByte() }
    var blue = ByteArray(256) { it.toByte() }
    var alpha = ByteArray(256) { it.toByte() }
}

enum class YuvFormat(val value: Int) {
    NV21(0x11),
    YV12(0x32315659),
}

data class Range2d(
    val startX: Int,
    val endX: Int,
    val startY: Int,
    val endY: Int
) {
    constructor() : this(0, 0, 0, 0)
}

class Rgba3dArray(val values: ByteArray, val sizeX: Int, val sizeY: Int, val sizeZ: Int) {
    init {
        require(values.size >= sizeX * sizeY * sizeZ * 4)
    }

    operator fun get(x: Int, y: Int, z: Int): ByteArray {
        val index = indexOfVector(x, y, z)
        return ByteArray(4) { values[index + it] }
    }

    operator fun set(x: Int, y: Int, z: Int, value: ByteArray) {
        require(value.size == 4)
        val index = indexOfVector(x, y, z)
        for (i in 0..3) {
            values[index + i] = value[i]
        }
    }

    private fun indexOfVector(x: Int, y: Int, z: Int): Int {
        require(x in 0 until sizeX)
        require(y in 0 until sizeY)
        require(z in 0 until sizeZ)
        return ((z * sizeY + y) * sizeX + x) * 4
    }
}

internal fun validateBitmap(
    function: String,
    inputBitmap: Bitmap,
    alphaAllowed: Boolean = true
) {
    if (alphaAllowed) {
        require(
            inputBitmap.config == Bitmap.Config.ARGB_8888 ||
                    inputBitmap.config == Bitmap.Config.ALPHA_8
        ) {
            "$externalName. $function supports only ARGB_8888 and ALPHA_8 bitmaps. " +
                    "${inputBitmap.config} provided."
        }
    } else {
        require(inputBitmap.config == Bitmap.Config.ARGB_8888) {
            "$externalName. $function supports only ARGB_8888. " +
                    "${inputBitmap.config} provided."
        }
    }
    require(inputBitmap.width * vectorSize(inputBitmap) == inputBitmap.rowBytes) {
        "$externalName $function. Only bitmaps with rowSize equal to the width * vectorSize are " +
                "currently supported. Provided were rowBytes=${inputBitmap.rowBytes}, " +
                "width={${inputBitmap.width}, and vectorSize=${vectorSize(inputBitmap)}."
    }
}

internal fun createCompatibleBitmap(inputBitmap: Bitmap) = createBitmap(inputBitmap.width, inputBitmap.height, inputBitmap.config!!)

internal fun validateHistogramDotCoefficients(
    coefficients: FloatArray?,
    vectorSize: Int
) {
    require(coefficients == null || coefficients.size == vectorSize) {
        "$externalName histogramDot. The coefficients should be null or have $vectorSize values."
    }
    if (coefficients !== null) {
        var sum = 0f
        for (i in 0 until vectorSize) {
            require(coefficients[i] >= 0.0f) {
                "$externalName histogramDot. Coefficients should not be negative. " +
                        "Coefficient $i was ${coefficients[i]}."
            }
            sum += coefficients[i]
        }
        require(sum <= 1.0f) {
            "$externalName histogramDot. Coefficients should add to 1 or less. Their sum is $sum."
        }
    }
}

internal fun validateRestriction(tag: String, bitmap: Bitmap, restriction: Range2d? = null) {
    validateRestriction(tag, bitmap.width, bitmap.height, restriction)
}

internal fun validateRestriction(
    tag: String,
    sizeX: Int,
    sizeY: Int,
    restriction: Range2d? = null
) {
    if (restriction == null) return
    require(restriction.startX < sizeX && restriction.endX <= sizeX) {
        "$externalName $tag. sizeX should be greater than restriction.startX and greater " +
                "or equal to restriction.endX. $sizeX, ${restriction.startX}, " +
                "and ${restriction.endX} were provided respectively."
    }
    require(restriction.startY < sizeY && restriction.endY <= sizeY) {
        "$externalName $tag. sizeY should be greater than restriction.startY and greater " +
                "or equal to restriction.endY. $sizeY, ${restriction.startY}, " +
                "and ${restriction.endY} were provided respectively."
    }
    require(restriction.startX < restriction.endX) {
        "$externalName $tag. Restriction startX should be less than endX. " +
                "${restriction.startX} and ${restriction.endX} were provided respectively."
    }
    require(restriction.startY < restriction.endY) {
        "$externalName $tag. Restriction startY should be less than endY. " +
                "${restriction.startY} and ${restriction.endY} were provided respectively."
    }
}

internal fun vectorSize(bitmap: Bitmap): Int {
    return when (bitmap.config) {
        Bitmap.Config.ARGB_8888 -> 4
        Bitmap.Config.ALPHA_8 -> 1
        else -> throw IllegalArgumentException(
            "$externalName. Only ARGB_8888 and ALPHA_8 Bitmap are supported."
        )
    }
}

internal fun paddedSize(vectorSize: Int) = if (vectorSize == 3) 4 else vectorSize