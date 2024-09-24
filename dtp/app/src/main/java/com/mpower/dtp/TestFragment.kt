package com.mpower.dtp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.mpower.dtp.databinding.FragmentTestBinding
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import kotlin.math.log10

class TestFragment : Fragment() {

    private lateinit var binding: FragmentTestBinding
    private lateinit var mSocket: Socket

    override fun onDestroy() {
        super.onDestroy()
        mSocket.disconnect()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTestBinding.inflate(inflater)
        return binding.root
    }

    private fun initFFTChart() {
        binding.fftSpectrumChart.description.isEnabled = false
        binding.fftSpectrumChart.setDrawGridBackground(false)
        binding.fftSpectrumChart.data = LineData()
    }

    private fun updateFFTChart(fftData: Array<Double>) {
        val entries = fftData.mapIndexed { index, value ->
            Entry(index.toFloat(), log10(value).toFloat())
        }

        val dataSet = LineDataSet(entries, "FFT Spectrum")
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 2f

        val lineData = LineData(dataSet)
        binding.fftSpectrumChart.data = lineData
        binding.fftSpectrumChart.invalidate() // Refresh the chart
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the chart with empty data
        initFFTChart()

        // Initialize Socket.IO connection
        try {
            mSocket = IO.socket("http://192.168.23.158:3000/streamer.html")
        } catch (e: Exception) {
            Log.e("socket", "error: ${e.message}")
        }

        // Connect to the socket
        mSocket.connect()

        // Listen for data from the server
        mSocket.on(Socket.EVENT_CONNECT) {
            Log.d("socket", "connection established")
        }

        mSocket.on("audioData") { args ->
            if (args[0] != null) {
                val data = args[0] as JSONObject
                // Parse the data and update your graph
                val fftData = parseFFTDataFromSocket(data)
                updateFFTChart(fftData)
            }
        }

        mSocket.on(Socket.EVENT_DISCONNECT) {
            Log.d("socket", "socket disconnected")
        }

        mSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("socket", "connection error: ${args[0]}")
        }
    }

    private fun parseFFTDataFromSocket(data: JSONObject): Array<Double> {
        // Parse the incoming data as needed
        // Assume the data is in JSON format and represents FFT data
        // Convert it to an array of doubles
        return arrayOf() // Replace with actual parsing logic
    }

}