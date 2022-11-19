package com.specknet.pdiotapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.specknet.pdiotapp.bean.HistoryData
import com.sung2063.tableau_library.graph.PieGraphView
import com.sung2063.tableau_library.graph.handler.PieGraphHandler
import com.sung2063.tableau_library.graph.model.PieGraphModel

class ViewHistoryActivity : AppCompatActivity() {
    private lateinit var mySQLite:MySQLite

    private lateinit var date:String
    private lateinit var user_name:String
    private lateinit var editText: EditText
    private lateinit var txv:TextView
    private lateinit var button: Button
    private lateinit var pieGraphView: PieGraphView

    var WalkingPercent:Float = 0.0f
    var SittingPercent:Float = 0.0f
    var RunningPercent:Float = 0.0f
    var LyingPercent:Float = 0.0f

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
        pieGraphView.setVisibility(View.INVISIBLE)
    }

    private fun setUpView(){
        editText = findViewById(R.id.editTextDate)
        txv = findViewById(R.id.textView3)
        button = findViewById(R.id.view_button)
        pieGraphView = findViewById(R.id.pie_graph_view)
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
                    pieGraphView.setVisibility(View.VISIBLE)

                    val TotalRowInGivenDate:Int = mySQLite.test(date, user_name)
                    val TotalRowWalking:Int = mySQLite.test1(date,user_name,"Walking")
                    val TotalRowSitting:Int = mySQLite.test1(date,user_name,"Sitting/Standing")
                    val TotalRowRunning:Int = mySQLite.test1(date,user_name,"Running")
                    val TotalRowLyingDown:Int = mySQLite.test1(date,user_name,"Lying Down")

                    if (TotalRowInGivenDate!=0){
                        WalkingPercent = (TotalRowWalking*100)/TotalRowInGivenDate.toFloat()
                        SittingPercent = (TotalRowSitting*100)/TotalRowInGivenDate.toFloat()
                        RunningPercent = (TotalRowRunning*100)/TotalRowInGivenDate.toFloat()
                        LyingPercent = (TotalRowLyingDown*100)/TotalRowInGivenDate.toFloat()
                    }

                    val pieGraphView: PieGraphView = findViewById(R.id.pie_graph_view)

                    // [1] Create the data
                    val dataList = arrayListOf(
                        PieGraphModel("Sitting/Standing", SittingPercent, "#ed6234"),
                        PieGraphModel("Walking", WalkingPercent, "#ebdf38"),
                        PieGraphModel("Running", RunningPercent, "#81e82c"),
                        PieGraphModel("Lying Down", LyingPercent, "#2784d6")
                    )
                    // [2] Set Handler and link with the view
                    pieGraphView.setHandler(PieGraphHandler(dataList))
                    Toast.makeText(this, "Activities Percentage", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "There is no history on that date/Input date in correct format,Please try again", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun  initGraph(){
        val pieGraphView: PieGraphView = findViewById(R.id.pie_graph_view)

        // [1] Create the data
        val dataList = arrayListOf(
            PieGraphModel("Sitting/Standing", SittingPercent, "#ed6234"),
            PieGraphModel("Walking", WalkingPercent, "#ebdf38"),
            PieGraphModel("Running", RunningPercent, "#81e82c"),
            PieGraphModel("Lying Down", LyingPercent, "#2784d6")
        )
        // [2] Set Handler and link with the view
        pieGraphView.setHandler(PieGraphHandler(dataList))
    }

}