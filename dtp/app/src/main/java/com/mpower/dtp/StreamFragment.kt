package com.mpower.dtp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mpower.dtp.databinding.FragmentStreamBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.security.cert.X509Certificate
import javax.net.ssl.*
import kotlin.math.abs

class StreamFragment : Fragment() {

    private lateinit var binding: FragmentStreamBinding
    private lateinit var webSocket: WebSocket

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStreamBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectToWebSocket()
    }

    private fun connectToWebSocket() {
        val client = getUnsafeOkHttpClient()
        val request = Request.Builder().url("wss://192.168.23.68:3001").build()
        val listener = AudioWebSocketListener()
        webSocket = client.newWebSocket(request, listener)
    }

    private inner class AudioWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Log.d("socket", "connection established: $response")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // Handle incoming audio data in text form
            // Parse and update your graph here
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            // Handle incoming raw audio data (if applicable)
            // Parse and update your graph here
            Log.d("socket", "data: $bytes")
            drawWaveformFromByteString(bytes)
//            updateFFTChart(processBinaryData(bytes))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            Log.d("socket", "failed to connect: $response")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d("socket", "closed socket because: $reason")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webSocket.close(1000, "Fragment destroyed")
    }

    fun drawWaveformFromByteString(byteString: ByteString) {
        // Convert ByteString to ByteArray
        val byteArray = byteString.toByteArray()

        // Convert ByteArray to PCM ShortArray
        val shortArray = byteArrayToShortArray(byteArray)

        // Process the waveform data
        val processedArray = processWaveform(shortArray)

        // Convert ShortArray to IntArray and feed it to the waveform view
        binding.waveformSeekBar.setSampleFrom(processedArray.map { it.toInt() }.toIntArray())
    }

    private fun byteArrayToShortArray(byteArray: ByteArray): ShortArray {
        val shortArray = ShortArray(byteArray.size / 2)
        for (i in shortArray.indices) {
            shortArray[i] = ((byteArray[2 * i + 1].toInt() shl 8) or (byteArray[2 * i].toInt() and 0xFF)).toShort()
        }
        return shortArray
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) { }
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) { }
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            // Create an sslSocketFactory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true } // Disable hostname verification
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun normalize(shortArray: ShortArray): IntArray {
        // Find the max absolute value in the short array
        val maxAmplitude = shortArray.maxOfOrNull { abs(it.toInt()) } ?: 1

        // Normalize the data by scaling down based on the max amplitude
        return shortArray.map { (it.toFloat() / maxAmplitude * 32767).toInt() }.toIntArray()
    }

    private fun smooth(intArray: IntArray, windowSize: Int = 5): IntArray {
        val smoothedArray = mutableListOf<Int>()
        for (i in intArray.indices) {
            val start = maxOf(0, i - windowSize / 2)
            val end = minOf(intArray.size - 1, i + windowSize / 2)
            val avg = intArray.slice(start..end).average().toInt()
            smoothedArray.add(avg)
        }
        return smoothedArray.toIntArray()
    }

    private fun clampValues(intArray: IntArray, min: Int, max: Int): IntArray {
        return intArray.map { it.coerceIn(min, max) }.toIntArray()
    }

    private fun processWaveform(shortArray: ShortArray): IntArray {
        // Step 1: Normalize the data
        val normalizedArray = normalize(shortArray)

        // Step 2: Apply smoothing
        val smoothedArray = smooth(normalizedArray, windowSize = 5)

        // Step 3: Clamp values to a reasonable range
        return clampValues(smoothedArray, -20000, 20000) // No conversion to ShortArray needed
    }
}