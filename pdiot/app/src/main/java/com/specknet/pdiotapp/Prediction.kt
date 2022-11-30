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
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer



class Prediction : AppCompatActivity() {
    // global graph variables
    lateinit var dataSet_res_accel_x: LineDataSet
    lateinit var dataSet_res_accel_y: LineDataSet
    lateinit var dataSet_res_accel_z: LineDataSet

    lateinit var dataSet_thingy_accel_x: LineDataSet
    lateinit var dataSet_thingy_accel_y: LineDataSet
    lateinit var dataSet_thingy_accel_z: LineDataSet

    var time = 0f
    lateinit var allRespeckData: LineData

    lateinit var allThingyData: LineData

    lateinit var respeckChart: LineChart
    lateinit var thingyChart: LineChart

    // global broadcast receiver so we can unregister it
    lateinit var respeckLiveUpdateReceiver: BroadcastReceiver
    lateinit var thingyLiveUpdateReceiver: BroadcastReceiver
    lateinit var modelLiveUpdateReceiver: BroadcastReceiver
    lateinit var looperRespeck: Looper
    lateinit var looperThingy: Looper
    lateinit var looperModel: Looper

    val filterTestRespeck = IntentFilter(Constants.ACTION_RESPECK_LIVE_BROADCAST)
    val filterTestThingy = IntentFilter(Constants.ACTION_THINGY_BROADCAST)

    private lateinit var image1:ImageView
    private lateinit var image2:ImageView
    private lateinit var image3:ImageView
    private lateinit var image4:ImageView

    private lateinit var output1:TextView
    private lateinit var output2:TextView
    private lateinit var output3:TextView
    private lateinit var output4:TextView
    private lateinit var finalactivity:TextView

    private lateinit var probability1:TextView
    private lateinit var probability2:TextView
    private lateinit var probability3:TextView
    private lateinit var probability4:TextView

    private lateinit var detailseMode: Switch

    private lateinit var user_name:String
    private lateinit var date:String

    private lateinit var fbtHistory:FloatingActionButton
    private lateinit var fbtStep:FloatingActionButton

    private lateinit var mySQLite:MySQLite

    private val fourteenActivity = listOf("Sitting","Walking at normal speed","Lying down on back","Desk work","Sitting bent forward",
                                  "Sitting bent backward","Lying down right","Lying down left","Lying down on stomach","Movement",
                                  "Standing","Running","Climbing stairs","Descending stairs")

    private val fourActivity = listOf("Sitting/Standing","Walking","Running","Lying Down")
//    val thingyActivity = listOf("Sitting","Standing")

    private var respeckTfInput = FloatArray(50 * 6) { 0.toFloat() }
    private var thingyIfInput = FloatArray(50 * 6) { 0.toFloat() }
    private var respeck_Input = FloatArray(1*6){0.toFloat()}
    private var thingy_Input = FloatArray(1*6){0.toFloat()}
    private var counter = 0
//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        init()
        setUpButton()
        setupClickListeners()
        initImage()
        initText()

        Log.d("###","Before History Data")
        val hd:HistoryData = HistoryData()
        date = hd.getDateTime()

        mySQLite = MySQLite(this)

        val intent_from_main:Intent = getIntent()
        user_name= intent_from_main.getStringExtra("account_name").toString()
        Log.d("###","After History Data")
        setupCharts()
        set()
    }

    private fun setUpButton(){
        fbtHistory = findViewById(R.id.history_check)
        fbtStep = findViewById(R.id.step_account_check)
    }

    private fun initImage(){
        runOnUiThread {
            image1.setImageResource(R.drawable.standing)
            image2.setImageResource(R.drawable.standing)
            image3.setImageResource(R.drawable.standing)
            image4.setImageResource(R.drawable.standing)
        }
    }

    private fun initText(){
        output1.setText("Activity1")
        output2.setText("Activity2")
        output3.setText("Activity3")
        output4.setText("Activity4")
        probability1.setText("probability1")
        probability2.setText("probability2")
        probability3.setText("probability3")
        probability4.setText("probability4")
        finalactivity.setText("FinalResult")
    }



    private fun setupClickListeners() {
        fbtHistory.setOnClickListener {
            val intent = Intent(this,ViewHistoryActivity::class.java)
            intent.putExtra("name",user_name)
            intent.putExtra("date",date)
            startActivity(intent)
        }
        fbtStep.setOnClickListener{

            val walkingStep:Int = (mySQLite.getWalkingCount(date, user_name)+mySQLite.test1(date,user_name,"Walking at normal speed"))*3/2
            val runningStep:Int = mySQLite.getRunningCount(date,user_name)*3
            val stairStep:Int = mySQLite.test1(date,user_name,"Climbing stairs")+mySQLite.test1(date,user_name,"Descending stairs")

            val todayStep:Int = walkingStep+runningStep+stairStep

            AlertDialog.Builder(this).apply {
                //构建一个对话框
                setTitle("You have taken a total of")//title
                setMessage(todayStep.toString()+" steps today")//content
                setCancelable(false)
                setPositiveButton("OK"){
                        dialog, which ->
                }
                show()
            }
        }
    }


    fun set(){
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
                    updateGraph("thingy", x, y, z)

                }
            }
        }
        // register receiver on another thread
        val handlerThreadThingy = HandlerThread("bgThreadThingyLive")
        handlerThreadThingy.start()
        looperThingy = handlerThreadThingy.looper
        val handlerThingy = Handler(looperThingy)
        this.registerReceiver(thingyLiveUpdateReceiver, filterTestThingy, null, handlerThingy)

        modelLiveUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                Log.i("thread", "I am running on thread = " + Thread.currentThread().name)
//                val action = intent.action
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
    private fun init(){
        image1 = findViewById(R.id.image1)
        image2 = findViewById(R.id.image2)
        image3 = findViewById(R.id.image3)
        image4 = findViewById(R.id.image4)

        output1 = findViewById(R.id.output1)
        output2 = findViewById(R.id.output2)
        output3 = findViewById(R.id.output3)
        output4 = findViewById(R.id.output4)
        finalactivity = findViewById(R.id.finalActivity)

        probability1 = findViewById(R.id.prob1)
        probability2 = findViewById(R.id.prob2)
        probability3 = findViewById(R.id.prob3)
        probability4 = findViewById(R.id.prob4)
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
        unregisterReceiver(thingyLiveUpdateReceiver)
        looperRespeck.quit()
        looperThingy.quit()
    }


    private fun setImage(image:ImageView,name:String){
        runOnUiThread {
            if (name == "Sitting" || name == "Sitting bent forward" || name == "Sitting bent backward"||name == "Sitting/Standing"){
                Log.d("###########111","Sitting")
            image.setImageResource(R.drawable.sit)
//            image.setImageDrawable(resources.getDrawable(com.specknet.pdiotapp.R.drawable.ic_sitting))
            }
            else if (name == "Walking at normal speed"||name == "Walking"){
                Log.d("###########111","Walking")
                image.setImageResource(R.drawable.walk)
            }
            else if(name == "Desk work"){
                Log.d("###########111","Deskwork")
                image.setImageResource(R.drawable.desk)
            }
            else if (name == "Lying down on back" || name == "Lying down right" || name == "Lying down on left" || name == "Lying down on stomach"||name == "Lying Down"){
                Log.d("###########111","Lying")
                image.setImageResource(R.drawable.lying)
            }
            else if (name == "Movement"){
                Log.d("###########111","Movement")
                image.setImageResource(R.drawable.movement)
            }
            else if (name == "Standing"){
                Log.d("###########111","Standing")
                image.setImageResource(R.drawable.standing)
            }
            else if (name == "Running"){
                Log.d("###########111","Running")
                image.setImageResource(R.drawable.run)
            }
            else if (name == "Climbing stairs"){
                Log.d("###########111","Upstair")
                image.setImageResource(R.drawable.upstairs)
            }
            else if (name == "Descending stairs"){
                Log.d("###########111","Downstair")
                image.setImageResource(R.drawable.downstairs)
            }
            else{
                image.setImageResource(R.drawable.standing)
            }
        }
    }

    private fun get_thingy_model_outputs(details: Boolean, thingyIfInput: FloatArray): FloatArray {
        return if(details){
            val thingyModel = Thingy.newInstance(this)
            val inputFeatures = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
            inputFeatures.loadArray(thingyIfInput)
            val outputs = thingyModel.process(inputFeatures)
            thingyModel.close()
            outputs.outputFeature0AsTensorBuffer.floatArray
        } else{
            FloatArray(1*6){0.toFloat()}
        }
    }



    fun ModelInput(respeck_Input:FloatArray,thingy_Input:FloatArray){
        if (counter <= 294) {
            this.respeckTfInput[counter] = respeck_Input[0]
            this.respeckTfInput[counter + 1] = respeck_Input[1]
            this.respeckTfInput[counter + 2] = respeck_Input[2]
            this.respeckTfInput[counter + 3] = respeck_Input[3]
            this.respeckTfInput[counter + 4] = respeck_Input[4]
            this.respeckTfInput[counter + 5] = respeck_Input[5]

            Log.d("###########","Collected the respeck data")
            this.thingyIfInput[counter] = thingy_Input[0]
            this.thingyIfInput[counter + 1] = thingy_Input[1]
            this.thingyIfInput[counter + 2] = thingy_Input[2]
            this.thingyIfInput[counter + 3] = thingy_Input[3]
            this.thingyIfInput[counter + 4] = thingy_Input[4]
            this.thingyIfInput[counter + 5] = thingy_Input[5]
            Log.d("###########","Collected the thingy data")
            counter += 6
        }
        else if(counter>294){
            Log.d("###########","Make Prediction")
            detailseMode = findViewById(R.id.switch_detailsmodel)
            var activityList = listOf<String>()
            Log.d("###########","Windows is 150")
            val probabilityList = get_respeck_model_outputs(detailseMode.isChecked,respeckTfInput)
            val thingyProbability = get_thingy_model_outputs(detailseMode.isChecked,thingyIfInput)
            val activityToProbability = hashMapOf<String, Float>()
            Log.d("######SUccess","1s success")
            if (detailseMode.isChecked){
                // activityList = fourActivity
                activityList = fourteenActivity
            }
            else{
                // activityList = fourteenActivity
                activityList = fourActivity
            }

            for (i in activityList.indices){
                val activity = activityList[i]
                Log.d("############Activity#######",activity)
                Log.d("############Respeck Probability#######",probabilityList[i].toString())
                Log.d("############Thingy Probability 0#######",thingyProbability[0].toString())
                Log.d("############Thingy Probability 1#######",thingyProbability[1].toString())
//                if(activity == "Sitting"){
//                    activityToProbability[activity] = thingyProbability[0]
//                }
//                else if (activity == "Standing"){
//                    activityToProbability[activity] = thingyProbability[1]
//                }
//                else if (detailseMode.isChecked){
//                    activityToProbability[activity] = probabilityList[i]
//                }
//                else{
//                    activityToProbability[activity] = probabilityList[i]
//                }
                activityToProbability[activity] = probabilityList[i]
            }
            if(detailseMode.isChecked && thingyProbability[0]>0.5){
                val original_prob = activityToProbability["Sitting"]
                val extra_prob = activityToProbability["Standing"]
                activityToProbability["Sitting"] = original_prob!! + extra_prob!!
                activityToProbability["Standing"] = 0F
            }
            else if(detailseMode.isChecked && thingyProbability[1]>0.5){
                val original_prob = activityToProbability["Standing"]
                val extra_prob = activityToProbability["Sitting"]
                activityToProbability["Standing"] = original_prob!! + extra_prob!!
                activityToProbability["Sitting"] = 0F
            }

            val sortedMap = activityToProbability.toList().sortedByDescending { (_, value) -> value }.toMap()

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
                        setText(output1, stringPrediction)
                        setImage(image1,stringPrediction)
                        setText(finalactivity, stringPrediction)
                        setTextInt(probability1, probabilityPrediction)
                        insertToHistoryDB()
                    }
                    2 -> {
                        setText(output2, stringPrediction)
                        setImage(image2,stringPrediction)
                        setTextInt(probability2, probabilityPrediction)
                    }
                    3 -> {
                        setText(output3, stringPrediction)
                        setImage(image3,stringPrediction)
                        setTextInt(probability3, probabilityPrediction)
                    }
                    4 -> {
                        setText(output4, stringPrediction)
                        setImage(image4,stringPrediction)
                        setTextInt(probability4, probabilityPrediction)
                    }
                }
                count += 1
            }

//            for (i in 0..149){
//                respeckTfInput[i] = respeckTfInput[i+149]
//                thingyIfInput[i] = thingyIfInput[i+149]
//            }
            counter = 0
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
    private fun get_respeck_model_outputs(details:Boolean,respeckTfInput:FloatArray): FloatArray {
        if(details){
            val pdiotModel = Cnn14.newInstance(this)
            val inputFeatures = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
            inputFeatures.loadArray(respeckTfInput)

            val outputs = pdiotModel.process(inputFeatures)
            pdiotModel.close()
            return outputs.outputFeature0AsTensorBuffer.floatArray
        }
        else{
            val pdiotModel = Cnn4.newInstance(this)
            val inputFeatures = TensorBuffer.createFixedSize(intArrayOf(1, 50, 6), DataType.FLOAT32)
            inputFeatures.loadArray(respeckTfInput)

            val outputs = pdiotModel.process(inputFeatures)
            pdiotModel.close()
            return outputs.outputFeature0AsTensorBuffer.floatArray
        }
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
