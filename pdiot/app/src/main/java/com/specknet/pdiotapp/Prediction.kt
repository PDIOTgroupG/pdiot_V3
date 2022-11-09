package com.specknet.pdiotapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.specknet.pdiotapp.ml.Model
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import kotlinx.android.synthetic.main.activity_prediction.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class Prediction : AppCompatActivity() {
    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet

    var time = 0f
    lateinit var allRespeckData: LineData
    lateinit var respeckChart: LineChart

    // global broadcast receiver so we can unregister it
    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper
    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)

    lateinit var output1:TextView
    lateinit var output2:TextView
    lateinit var probability1:TextView
    lateinit var probability2:TextView
    lateinit var finalactivity:TextView


    // todo 检查一下label是不是对的，如果label和模型输出的label对不上，会导致准确率高，但是输出不对
    val INDEX_TO_NAME_MAPPING = mapOf(
        0 to "Sitting",
        1 to "Walking at normal speed",
        2 to "Lying down on back",
        3 to "Desk work",
        4 to "Sitting bent forward",
        5 to "Sitting bent backward",
        6 to "Lying down right",
        7 to "Lying down left",
        8 to "Lying down on stomach",
        9 to "Movement",
        10 to "Standing",
        11 to "Running",
        12 to "Climbing stairs",
        13 to "Descending stairs"
    )

    var tfinput = FloatArray(50 * 6) { 0.toFloat() }
    var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)
        setupCharts()


        // set up the broadcast receiver
        respeckLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_RESPECK_LIVE_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.RESPECK_LIVE_DATA) as RESpeckLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ
                    val gx = liveData.gyro.x
                    val gy = liveData.gyro.y
                    val gz = liveData.gyro.z
                    modelinput(x, y, z, gx, gy, gz)
                    time += 1
                    updateGraph("respeck", x, y, z)

                }
            }
        }
        // register receiver on another thread
        val handlerThreadRespeck = HandlerThread("bgThreadRespeckLive")
        handlerThreadRespeck.start()
        looperRespeck = handlerThreadRespeck.looper
        val handlerRespeck = Handler(looperRespeck)
        this.registerReceiver(respeckLiveUpdateReceiver, filterTestRespeck, null, handlerRespeck)


    }
    fun setupCharts() {
        output1 = findViewById(R.id.output1)
        output2 = findViewById(R.id.output2)
        probability1 = findViewById(R.id.prob1)
        probability2 = findViewById(R.id.prob2)

        respeckChart = findViewById(R.id.respeck_chart2)

        // Respeck

        time = 0f
        val entries_res_accel_x = ArrayList<Entry>()
        val entries_res_accel_y = ArrayList<Entry>()
        val entries_res_accel_z = ArrayList<Entry>()

        dataSet_res_accel_x = LineDataSet(entries_res_accel_x, "Accel X")
        dataSet_res_accel_y = LineDataSet(entries_res_accel_y, "Accel Y")
        dataSet_res_accel_z = LineDataSet(entries_res_accel_z, "Accel Z")

        dataSet_res_accel_x.setDrawCircles(false)
        dataSet_res_accel_y.setDrawCircles(false)
        dataSet_res_accel_z.setDrawCircles(false)

        dataSet_res_accel_x.setColor(
            ContextCompat.getColor(
                this,
                R.color.red
            )
        )
        dataSet_res_accel_y.setColor(
            ContextCompat.getColor(
                this,
                R.color.green
            )
        )
        dataSet_res_accel_z.setColor(
            ContextCompat.getColor(
                this,
                R.color.blue
            )
        )

        val dataSetsRes = ArrayList<ILineDataSet>()
        dataSetsRes.add(dataSet_res_accel_x)
        dataSetsRes.add(dataSet_res_accel_y)
        dataSetsRes.add(dataSet_res_accel_z)

        allRespeckData = LineData(dataSetsRes)
        respeckChart.data = allRespeckData
        respeckChart.invalidate()

    }

    fun updateGraph(graph: String, x: Float, y: Float, z: Float) {
        // take the first element from the queue
        // and update the graph with it
        if (graph == "respeck") {
            dataSet_res_accel_x.addEntry(Entry(time, x))
            dataSet_res_accel_y.addEntry(Entry(time, y))
            dataSet_res_accel_z.addEntry(Entry(time, z))

            runOnUiThread {
                allRespeckData.notifyDataChanged()
                respeckChart.notifyDataSetChanged()
                respeckChart.invalidate()
                respeckChart.setVisibleXRangeMaximum(150f)
                respeckChart.moveViewToX(respeckChart.lowestVisibleX + 40)
            }
        }
    }
    // 模型输入
    fun modelinput(x: Float, y: Float, z: Float, x1: Float, y1: Float, z1: Float) {
        if (counter < 300) {
            this.tfinput.set(counter, x)
            this.tfinput.set(counter + 1, y)
            this.tfinput.set(counter + 2, z)
            this.tfinput.set(counter + 3, x1)
            this.tfinput.set(counter + 4, y1)
            this.tfinput.set(counter + 5, z1)
            counter += 6
        }

        else if (counter >= 300) {
            // tocheck the concise model is open or not. If open,select the 4 activity model.
            // otherwise using the original model.


            val pdiotModel = Model.newInstance(this)
            val inputFeatures = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
            inputFeatures.loadArray(tfinput)

            val outputs = pdiotModel.process(inputFeatures)
            val outputFeatures = outputs.outputFeature0AsTensorBuffer

            val indexToProbability = hashMapOf<Int, Float>()

            for (i in 0..13){
                indexToProbability[i] = outputFeatures.floatArray[i]
            }

            // sort the map in descending 递减
            val sortedMap = indexToProbability.toSortedMap(compareByDescending { it })


//            var floatarray = FloatArray(14)
//            var index = 0
//            var maxValue :Float
//            maxValue = 0F
//            for (i in 0..13) {
//                floatarray[i] = outputFeatures.floatArray[i]
//                if(floatarray[i]>maxValue){
//                    maxValue = floatarray[i]
//                    index = i
//                }
//            }


            output1 = findViewById(R.id.output1)
            probability1 = findViewById(R.id.prob1)
            finalactivity = findViewById(R.id.finalActivity)

            val stringPrediction1 = INDEX_TO_NAME_MAPPING[index]
//            Log.d("###########",""+stringPrediction1)

            if (stringPrediction1 != null) {
                setText(output1,stringPrediction1)
                setText(finalactivity,stringPrediction1)
            }
            setTextInt(probability1,maxValue)

            pdiotModel.close()
            counter = 150
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(respeckLiveUpdateReceiver)
        looperRespeck.quit()
    }
    private fun setText(text: TextView, value: String) {
        runOnUiThread {
            text.text = value
        }
    }
    private fun setTextInt(text: TextView, value: Float) {
        runOnUiThread {
            text.text = String.format("%.1f", (value * 100)) + '%'
        }
    }
}