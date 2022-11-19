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
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.getbase.floatingactionbutton.FloatingActionButton
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.specknet.pdiotapp.bean.HistoryData
import com.specknet.pdiotapp.ml.Cnn14
import com.specknet.pdiotapp.ml.Cnn4
import com.specknet.pdiotapp.ml.Thingy
import com.specknet.pdiotapp.utils.Constants
import com.specknet.pdiotapp.utils.RESpeckLiveData
import com.specknet.pdiotapp.utils.ThingyLiveData
import org.apache.commons.lang3.ObjectUtils.Null
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
    lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
    lateinit var modelLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper
    lateinit var looperThingy: Looper
    lateinit var looperModel: Looper

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    lateinit var image1:ImageView
    lateinit var image2:ImageView
    lateinit var image3:ImageView
    lateinit var image4:ImageView

    lateinit var output1:TextView
    lateinit var output2:TextView
    lateinit var output3:TextView
    lateinit var output4:TextView
    lateinit var probability1:TextView
    lateinit var probability2:TextView
    lateinit var probability3:TextView
    lateinit var probability4:TextView
    lateinit var finalactivity:TextView

    lateinit var conciseMode: Switch

    lateinit var user_name:String
    lateinit var date:String
    lateinit var fbtHistory:FloatingActionButton

    private lateinit var mySQLite:MySQLite




//    val INDEX_TO_NAME_MAPPING = mapOf(
//        0 to "Sitting",
//        1 to "Walking at normal speed",
//        2 to "Lying down on back",
//        3 to "Desk work",
//        4 to "Sitting bent forward",
//        5 to "Sitting bent backward",
//        6 to "Lying down right",
//        7 to "Lying down left",
//        8 to "Lying down on stomach",
//        9 to "Movement",
//        10 to "Standing",
//        11 to "Running",
//        12 to "Climbing stairs",
//        13 to "Descending stairs"
//    )

//    val four_activity_map = mapOf(
//        0 to "Sitting/Standing",
//        1 to "Walking",
//        2 to "Running",
//        3 to "Lying Down"
//    )
    val fourteenActivity = listOf("Sitting","Walking at normal speed","Lying down on back","Desk work","Sitting bent forward",
                                  "Sitting bent backward","Lying down right","Lying down left","Lying down on stomach","Movement",
                                  "Standing","Running","Climbing stairs","Descending stairs")

    val fourActivity = listOf("Sitting/Standing","Walking","Running","Lying Down")
    val thingyActivity = listOf("Sitting","Standing")



    var respeckTfInput = FloatArray(50 * 6) { 0.toFloat() }
    var thingyIfInput = FloatArray(50 * 6) { 0.toFloat() }
    var respeck_Input = FloatArray(1*6){0.toFloat()}
    var thingy_Input = FloatArray(1*6){0.toFloat()}
    var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)
        setupCharts()
        setUpButton()
        setupClickListeners()

        val hd:HistoryData = HistoryData()
        date = hd.getDateTime()


        mySQLite = MySQLite(this)

        val intent_from_main:Intent = getIntent()
        user_name= intent_from_main.getStringExtra("account_name").toString()

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
                    set_respeck_input(x, y, z, gx, gy, gz)
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

        // set up the broadcast receiver
        thingyLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)

                val action = intent.action

                if (action == Constants.ACTION_THINGY_BROADCAST) {

                    val liveData =
                        intent.getSerializableExtra(Constants.THINGY_LIVE_DATA) as ThingyLiveData
                    Log.d("Live", "onReceive: liveData = " + liveData)

                    // get all relevant intent contents
                    val x = liveData.accelX
                    val y = liveData.accelY
                    val z = liveData.accelZ
                    val gx = liveData.gyro.x
                    val gy = liveData.gyro.y
                    val gz = liveData.gyro.z
                    set_thingy_input(x, y, z, gx, gy, gz)
                    time += 1
                }
            }
        }

        // register receiver on another thread
        val handlerThreadThingy = HandlerThread("bgThreadThingyLive")
        handlerThreadThingy.start()
        looperThingy = handlerThreadThingy.looper
        val handlerThingy = Handler(looperThingy)
        this.registerReceiver(thingyLiveUpdateReceiver, filterTestThingy, null, handlerThingy)

        // set up the broadcast receiver
        modelLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)
                val action = intent.action
                ModelInput(respeck_Input,thingy_Input)
                time += 1
            }
        }
        val handlerThreadModel = HandlerThread("bgModelThingyLive")
        handlerThreadModel.start()
        looperModel = handlerThreadThingy.looper
        val handlerModel = Handler(looperModel)
        this.registerReceiver(modelLiveUpdateReceiver, filterTestRespeck, null, handlerModel)

    }

    private fun setUpButton(){
        fbtHistory = findViewById(R.id.history_check)
    }


    private fun setupClickListeners() {
        fbtHistory.setOnClickListener {
            val intent = Intent(this,ViewHistoryActivity::class.java)
            intent.putExtra("name",user_name)
            intent.putExtra("date",date)
            startActivity(intent)
        }
    }



    fun ModelInput(respeck_Input:FloatArray,thingy_Input:FloatArray){
        if (counter <= 294) {
            this.respeckTfInput.set(counter, respeck_Input[0])
            this.respeckTfInput.set(counter + 1, respeck_Input[1])
            this.respeckTfInput.set(counter + 2, respeck_Input[2])
            this.respeckTfInput.set(counter + 3, respeck_Input[3])
            this.respeckTfInput.set(counter + 4, respeck_Input[4])
            this.respeckTfInput.set(counter + 5, respeck_Input[5])

            Log.d("###########","Collected the respeck data")
            this.thingyIfInput.set(counter, thingy_Input[0])
            this.thingyIfInput.set(counter + 1, thingy_Input[1])
            this.thingyIfInput.set(counter + 2, thingy_Input[2])
            this.thingyIfInput.set(counter + 3, thingy_Input[3])
            this.thingyIfInput.set(counter + 4, thingy_Input[4])
            this.thingyIfInput.set(counter + 5, thingy_Input[5])
            Log.d("###########","Collected the thingy data")
            counter += 6
        }
        else if(counter>294){
            Log.d("###########","Make Prediction")
            conciseMode = findViewById(R.id.switch_concisemodel)
            var activityList = listOf<String>()
            val probabilityList = get_respeck_model_outputs(conciseMode.isChecked,respeckTfInput)
            val thingyProbability = get_thingy_model_outputs(conciseMode.isChecked,thingyIfInput)
            val activityToProbability = hashMapOf<String, Float>()
            if (conciseMode.isChecked){
                // activityList = fourActivity
                activityList = fourteenActivity
            }
            else{
                // activityList = fourteenActivity
                activityList = fourActivity
            }

            for (i in activityList.indices){
                val activity = activityList[i]
                if(activity == "Sitting"){
                    activityToProbability[activity] = thingyProbability[0]
                }
                else if (activity == "Standing"){
                    activityToProbability[activity] = thingyProbability[1]
                }
                else{
                    activityToProbability[activity] = probabilityList[i]
                }
            }

            val sortedMap = activityToProbability.toList().sortedByDescending { (_, value) -> value }.toMap()

            image1 = findViewById(R.id.imageView)
            image2 = findViewById(R.id.imageView2)
            image3 = findViewById(R.id.imageView3)
            image4 = findViewById(R.id.imageView4)

            output1 = findViewById(R.id.output1)
            output2 = findViewById(R.id.output2)
            output3 = findViewById(R.id.output3)
            output4 = findViewById(R.id.output4)



            probability1 = findViewById(R.id.prob1)
            probability2 = findViewById(R.id.prob2)
            probability3 = findViewById(R.id.prob3)
            probability4 = findViewById(R.id.prob4)

            finalactivity = findViewById(R.id.finalActivity)

            var count = 1
            for (entry in sortedMap.entries.iterator()) {
                if (count == 5) {
                    break
                }
                Log.d("###########",entry.key)
                Log.d("###########", entry.value.toString())
                val stringPrediction = entry.key
                val probabilityPrediction = entry.value

                when (count) {
                    1 -> {
                        setImage(image1,stringPrediction)
                        setText(output1, stringPrediction)
                        setText(finalactivity, stringPrediction)
                        setTextInt(probability1, probabilityPrediction)
                        insertToHistoryDB()
                    }
                    2 -> {
                        setImage(image2,stringPrediction)
                        setText(output2, stringPrediction)
                        setTextInt(probability2, probabilityPrediction)
                    }
                    3 -> {
                        setImage(image3,stringPrediction)
                        setText(output3, stringPrediction)
                        setTextInt(probability3, probabilityPrediction)
                    }
                    4 -> {
                        setImage(image4,stringPrediction)
                        setText(output4, stringPrediction)
                        setTextInt(probability4, probabilityPrediction)
                    }
                }

                count += 1
            }
            counter = 150
        }
    }

    private fun insertToHistoryDB() {

        val hd:HistoryData = HistoryData()

        date = hd.getDateTime()
        val UserName:String = user_name
        val Activity: String = finalactivity.text.toString().trim()
        val date: String = date

        hd.setDate(date)
        hd.setName(UserName)
        hd.setActivity(Activity)
        mySQLite.insertHistory(hd)
    }


    fun setupCharts() {
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

    private fun set_respeck_input(x: Float, y: Float, z: Float, x1: Float, y1: Float, z1: Float){
        this.respeck_Input[0] = x
        this.respeck_Input[1] = y
        this.respeck_Input[2] = z
        this.respeck_Input[3] = x1
        this.respeck_Input[4] = y1
        this.respeck_Input[5] = z1
    }
    private fun set_thingy_input(x: Float, y: Float, z: Float, x1: Float, y1: Float, z1: Float){
        this.thingy_Input[0] = x
        this.thingy_Input[1] = y
        this.thingy_Input[2] = z
        this.thingy_Input[3] = x1
        this.thingy_Input[4] = y1
        this.thingy_Input[5] = z1
    }

    private fun setImage(image:ImageView,name:String){
        if (name == "Sitting" || name == "Sitting bent forward" || name == "Sitting bent backward"){
            image.setImageDrawable(resources.getDrawable(com.specknet.pdiotapp.R.drawable.sit))
        }
        else if (name == "Walking at normal speed"){
            image.setImageDrawable(resources.getDrawable(com.specknet.pdiotapp.R.drawable.walk))
        }
        else if (name == "Lying down on back" || name == "Lying down right" || name == "Lying down on left" || name == "Lying down on stomach"){
            image.setImageDrawable(resources.getDrawable(com.specknet.pdiotapp.R.drawable.lying))
        }
        else if (name == "Movement"){
            image.setImageDrawable(resources.getDrawable(com.specknet.pdiotapp.R.drawable.walk))
        }
        else if (name == "Standing"){
            image.setImageDrawable(resources.getDrawable(com.specknet.pdiotapp.R.drawable.standing))
        }
        else if (name == "Running"){
            image.setImageDrawable(resources.getDrawable(com.specknet.pdiotapp.R.drawable.running))
        }
        else if (name == "Climbing stairs"){
            image.setImageDrawable(resources.getDrawable(com.specknet.pdiotapp.R.drawable.upstair))
        }
        else if (name == "Descending stairs"){
            image.setImageDrawable(resources.getDrawable(com.specknet.pdiotapp.R.drawable.downstair))
        }
    }

//    val INDEX_TO_NAME_MAPPING = mapOf(
//        0 to "Sitting",
//        1 to "Walking at normal speed",
//        2 to "Lying down on back",
//        3 to "Desk work",
//        4 to "Sitting bent forward",
//        5 to "Sitting bent backward",
//        6 to "Lying down right",
//        7 to "Lying down left",
//        8 to "Lying down on stomach",
//        9 to "Movement",
//        10 to "Standing",
//        11 to "Running",
//        12 to "Climbing stairs",
//        13 to "Descending stairs"
//    )

    //    val four_activity_map = mapOf(
//        0 to "Sitting/Standing",
//        1 to "Walking",
//        2 to "Running",
//        3 to "Lying Down"
//    )
    private fun get_thingy_model_outputs(concise: Boolean, thingyIfInput: FloatArray): FloatArray {
        if(concise){
            return FloatArray(1*6){0.toFloat()}
        }
        else{
            val thingyModel = Thingy.newInstance(this)
            val inputFeatures = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
            inputFeatures.loadArray(thingyIfInput)
            val outputs = thingyModel.process(inputFeatures)
            thingyModel.close()
            return outputs.outputFeature0AsTensorBuffer.floatArray
        }
    }
    private fun get_respeck_model_outputs(concise:Boolean,respeckTfInput:FloatArray): FloatArray {
        if(concise){
            val pdiotModel = Cnn4.newInstance(this)
            val inputFeatures = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
            inputFeatures.loadArray(respeckTfInput)

            val outputs = pdiotModel.process(inputFeatures)
            pdiotModel.close()
            return outputs.outputFeature0AsTensorBuffer.floatArray
        }
        else{
            val pdiotModel = Cnn14.newInstance(this)
            val inputFeatures = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
            inputFeatures.loadArray(respeckTfInput)

            val outputs = pdiotModel.process(inputFeatures)
            pdiotModel.close()
            return outputs.outputFeature0AsTensorBuffer.floatArray
        }
    }
}
