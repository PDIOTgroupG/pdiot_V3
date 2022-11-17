package com.specknet.pdiotapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sung2063.tableau_library.graph.PieGraphView
import com.sung2063.tableau_library.graph.handler.PieGraphHandler
import com.sung2063.tableau_library.graph.model.PieGraphModel

class ViewHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_history)

        val pieGraphView: PieGraphView = findViewById(R.id.pie_graph_view)

        // [1] Create the data
        val dataList = arrayListOf(
            PieGraphModel("Category 1", 20f, "#ed6234"),
            PieGraphModel("Category 2", 40f, "#ebdf38"),
            PieGraphModel("Category 3", 60f, "#81e82c"),
            PieGraphModel("Category 4", 35f, "#2784d6"),
            PieGraphModel("Category 5", 15f, "#7225c4")
        )

        // [2] Set Handler and link with the view
        pieGraphView.setHandler(PieGraphHandler(dataList))



    }
}