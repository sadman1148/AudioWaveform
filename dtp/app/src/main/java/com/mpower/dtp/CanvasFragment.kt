package com.mpower.dtp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mpower.dtp.databinding.FragmentCanvasBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class CanvasFragment : Fragment() {

    private lateinit var binding: FragmentCanvasBinding
    private lateinit var webSocket: WebSocket
    private lateinit var audioProcessor: AudioProcessor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCanvasBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectToWebSocket()
        audioProcessor = AudioProcessor(binding.audioWaveform)
    }

    private fun connectToWebSocket() {
        val client = getUnsafeOkHttpClient()
        val request = Request.Builder().url("wss://192.168.23.68:3001").build()
        val listener = AudioWebSocketListener()
        webSocket = client.newWebSocket(request, listener)
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(

                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {

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
//            Log.d("socket", "data: $bytes")
            audioProcessor.processAudioData(bytes)
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
}