package com.mpower.dtp

import okio.ByteString
import org.jtransforms.fft.FloatFFT_1D
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

class AudioProcessor(private val waveformView: AudioWaveformView) {
    private val fftSize = 1024
    private val fft = FloatFFT_1D(fftSize.toLong())

    fun processAudioData(byteString: ByteString) {
        val floatArray = byteStringToFloatArray(byteString)
        val paddedArray = padArray(floatArray, fftSize)

        fft.realForward(paddedArray)

        val magnitudes = FloatArray(fftSize / 2)
        for (i in 0 until fftSize / 2) {
            val real = paddedArray[2 * i]
            val imag = paddedArray[2 * i + 1]
            magnitudes[i] = sqrt(real * real + imag * imag)
        }

        // Convert to dB scale
        for (i in magnitudes.indices) {
            magnitudes[i] = 20 * log10(max(magnitudes[i], 1e-6f))
        }

        // Normalize and prepare symmetrical data
        val maxMagnitude = magnitudes.maxOrNull() ?: 1f
        val minMagnitude = magnitudes.minOrNull() ?: 0f
        val range = maxMagnitude - minMagnitude
        val symmetricalData = FloatArray(magnitudes.size * 2)
        for (i in magnitudes.indices) {
            val normalizedValue = (magnitudes[i] - minMagnitude) / range
            val smoothedValue = if (i > 0) {
                (symmetricalData[symmetricalData.size / 2 + i - 1] * 0.7f + normalizedValue * 0.3f)
            } else {
                normalizedValue
            }
            symmetricalData[symmetricalData.size / 2 + i] = smoothedValue
            symmetricalData[symmetricalData.size / 2 - 1 - i] = smoothedValue
        }

        // Apply tapering to the edges
        applyEdgeTapering(symmetricalData)

        waveformView.updateWaveform(symmetricalData)
    }

    private fun applyEdgeTapering(data: FloatArray) {
        val taperLength = data.size / 8  // Taper the first and last 1/8 of the data
        for (i in 0 until taperLength) {
            val factor = i.toFloat() / taperLength
            data[i] *= factor
            data[data.size - 1 - i] *= factor
        }
    }

    private fun byteStringToFloatArray(byteString: ByteString): FloatArray {
        val buffer = ByteBuffer.wrap(byteString.toByteArray())
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val floatArray = FloatArray(byteString.size / 4)
        for (i in floatArray.indices) {
            floatArray[i] = buffer.float
        }
        return floatArray
    }

    private fun padArray(array: FloatArray, targetSize: Int): FloatArray {
        return if (array.size >= targetSize) {
            array.copyOf(targetSize)
        } else {
            FloatArray(targetSize).also {
                System.arraycopy(array, 0, it, 0, array.size)
            }
        }
    }
}