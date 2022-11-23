package com.specknet.pdiotapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.specknet.pdiotapp.bean.HistoryData
import com.sung2063.tableau_library.graph.PieGraphView
import com.sung2063.tableau_library.graph.handler.PieGraphHandler
import com.sung2063.tableau_library.graph.model.PieGraphModel
import com.sung2063.tableau_library.progress.DotProgressView
import com.sung2063.tableau_library.progress.handler.DotProgressHandler
import com.sung2063.tableau_library.progress.model.DotProgressModel

class ViewHistoryActivity : AppCompatActivity() {
    private lateinit var mySQLite:MySQLite

    private lateinit var date:String
    private lateinit var user_name:String
    private lateinit var editText: EditText
    private lateinit var txv:TextView
    private lateinit var button: Button
    private lateinit var pieGraphView: PieGraphView
    private lateinit var dotGraphView: DotProgressView

    var WalkingPercent:Float = 0.0f
    var SittingPercent:Float = 0.0f
    var RunningPercent:Float = 0.0f
    var LyingPercent:Float = 0.0f
    var StairPercent:Float = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_history)
        setUpView()
        initGraph()
        setupClickListeners()

        mySQLite = MySQLite(this)
        val intent_from_predict: Intent = getIntent()
        val name: String = intent_from_predict.getStringExtra("name").toString()
        user_name = name
//        pieGraphView.setVisibility(View.INVISIBLE)
        dotGraphView.setVisibility(View.INVISIBLE)
    }

    private fun setUpView(){
        editText = findViewById(R.id.editTextDate)
        txv = findViewById(R.id.textView3)
        button = findViewById(R.id.view_button)
//        pieGraphView = findViewById(R.id.pie_graph_view)
        dotGraphView = findViewById(R.id.dot_progress_view)
    }


    private fun setupClickListeners() {
        button.setOnClickListener {
            val edt:String = editText.text.toString().trim()
            if (edt.isEmpty()){
                Toast.makeText(this, "Please input a date", Toast.LENGTH_SHORT).show()
            }else{
                date = editText.text.toString().trim()
                if (mySQLite.existsDate(date)){
                    editText.setVisibility(View.INVISIBLE)
                    txv.setVisibility(View.INVISIBLE)
                    button.setVisibility(View.INVISIBLE)

//                    pieGraphView.setVisibility(View.VISIBLE)
                    dotGraphView.setVisibility(View.VISIBLE)

                    var TotalRowInGivenDate:Int = mySQLite.test(date, user_name)
                    //WALKING
                    val TotalRowWalking:Int = mySQLite.test1(date,user_name,"Walking")+mySQLite.test1(date,user_name,"Walking at normal speed")
                    //SITTING AND STANDING
                    val TotalRowSitting:Int = mySQLite.test1(date,user_name,"Sitting/Standing")+mySQLite.test1(date,user_name,"Sitting")+
                            mySQLite.test1(date,user_name,"Sitting bent forward")+mySQLite.test1(date,user_name,"Sitting bent backward")+
                            mySQLite.test1(date,user_name,"Desk work")+mySQLite.test1(date,user_name,"Standing")
                    //running
                    val TotalRowRunning:Int = mySQLite.test1(date,user_name,"Running")
                    //lying
                    val TotalRowLyingDown:Int = mySQLite.test1(date,user_name,"Lying Down")+mySQLite.test1(date,user_name,"Lying down on back")+mySQLite.test1(date,user_name,"Lying down right")+
                            mySQLite.test1(date,user_name,"Lying down left")+mySQLite.test1(date,user_name,"Lying down on stomach")

                    val TotalRowStairs:Int = mySQLite.test1(date,user_name,"Climbing stairs")+mySQLite.test1(date,user_name,"Descending stairs")
                    TotalRowInGivenDate = TotalRowWalking+TotalRowSitting+TotalRowRunning+TotalRowLyingDown+TotalRowStairs
                    if (TotalRowInGivenDate!=0){
                        WalkingPercent = (TotalRowWalking*100)/TotalRowInGivenDate.toFloat()
                        SittingPercent = (TotalRowSitting*100)/TotalRowInGivenDate.toFloat()
                        RunningPercent = (TotalRowRunning*100)/TotalRowInGivenDate.toFloat()
                        LyingPercent = (TotalRowLyingDown*100)/TotalRowInGivenDate.toFloat()
                        StairPercent = (TotalRowStairs*100)/TotalRowInGivenDate.toFloat()
                    }


                    val dotProgressView: DotProgressView = findViewById(R.id.dot_progress_view)

                    // [1] Create the data
                    val dataList = mutableListOf(
                        DotProgressModel("Sitting/Standing", SittingPercent, "#cfaf25"),
                        DotProgressModel("Walking", WalkingPercent, "#25cfcc"),
                        DotProgressModel("Running", RunningPercent, "#6325cf"),
                        DotProgressModel("Lying Down", LyingPercent, "#cf2569"),
                        DotProgressModel("Climbing/Descending Stairs", StairPercent, "#cf2569")
                    )

                    // [2] Set Handler and link with the view
                    dotProgressView.setHandler(DotProgressHandler(dataList))

//                    val pieGraphView: PieGraphView = findViewById(R.id.pie_graph_view)
//
//                    // [1] Create the data
//                    val dataList = arrayListOf(
//                        PieGraphModel("Sitting/Standing", SittingPercent, "#ed6234"),
//                        PieGraphModel("Walking", WalkingPercent, "#ebdf38"),
//                        PieGraphModel("Running", RunningPercent, "#81e82c"),
//                        PieGraphModel("Lying Down", LyingPercent, "#2784d6")
//                    )
//                    // [2] Set Handler and link with the view
//                    pieGraphView.setHandler(PieGraphHandler(dataList))
                    AlertDialog.Builder(this).apply {
                        //构建一个对话框
                        setTitle("Activity distribution on the day")//title
                        setMessage(String.format("%.2f",WalkingPercent)+"% time you are Walking "
                                +String.format("%.2f",SittingPercent)+"% time you are Sitting/Standing "
                                +String.format("%.2f",RunningPercent)+"% time you are Running "
                                +String.format("%.2f",LyingPercent)+"% time you are Lying "
                                +String.format("%.2f",StairPercent)+"% time you are Climb/Descend Stairs ")
                        //content
                        setCancelable(false)
                        setPositiveButton("OK"){
                                dialog, which ->
                        }
                        show()
                    }
                    Toast.makeText(this, "Activities Percentage in one day", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "There is no history on that date/Input date in incorrect format,Please try again", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun  initGraph(){
        val dotProgressView: DotProgressView = findViewById(R.id.dot_progress_view)

        // [1] Create the data
        val dataList = mutableListOf(
            DotProgressModel("Category 1", 30f, "#cfaf25"),
            DotProgressModel("Category 2", 50f, "#25cfcc"),
            DotProgressModel("Category 3", 70f, "#6325cf"),
            DotProgressModel("Category 4", 95f, "#cf2569")
        )

        // [2] Set Handler and link with the view
        dotProgressView.setHandler(DotProgressHandler(dataList))


//        val pieGraphView: PieGraphView = findViewById(R.id.pie_graph_view)
//
//        // [1] Create the data
//        val dataList = arrayListOf(
//            PieGraphModel("Sitting/Standing", SittingPercent, "#ed6234"),
//            PieGraphModel("Walking", WalkingPercent, "#ebdf38"),
//            PieGraphModel("Running", RunningPercent, "#81e82c"),
//            PieGraphModel("Lying Down", LyingPercent, "#2784d6")
//        )
//        // [2] Set Handler and link with the view
//        pieGraphView.setHandler(PieGraphHandler(dataList))
    }

}