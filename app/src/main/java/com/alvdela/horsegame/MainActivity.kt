package com.alvdela.horsegame

import android.graphics.Point
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var lyMessage: LinearLayout
    private lateinit var cell: ImageView

    private var cellSelectedX = 0
    private var cellSelectedY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initScreenGame()
        setFirstPosition()
    }

    private fun setFirstPosition() {
        var x = 0
        var y = 0
        x = (0..7).random()
        y = (0..7).random()

        cellSelectedX = x
        cellSelectedY = y

        selectCell(x,y)
    }

    private fun selectCell(x: Int, y: Int) {

        paintHorseCell(cellSelectedX,cellSelectedY,"previous_cell")

        cellSelectedX = x
        cellSelectedY = y

        paintHorseCell(x,y,"selected_cell")

    }

    private fun paintHorseCell(x: Int, y: Int, color: String) {

        cell = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        cell.setBackgroundColor(ContextCompat.getColor(this,resources.getIdentifier(color, "color", packageName)))
        if (color == "selected_cell") cell.setImageResource(R.drawable.chess_knight)
        if (color == "previous_cell") cell.setImageResource(R.drawable.empty)
    }

    private fun initScreenGame() {
        setSizeBoard()
        hideMessage()
    }

    private fun hideMessage() {
        lyMessage = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.INVISIBLE
    }

    private fun setSizeBoard() {

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x

        val widthDp = (width / resources.displayMetrics.density)

        val widthCell = (widthDp)/8

        for(i in 0..7){
            for(j in 0..7){
                cell = findViewById(resources.getIdentifier("c$i$j", "id", packageName))

                var height = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    widthCell,
                    resources.displayMetrics
                ).toInt()
                var width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,widthCell,resources.displayMetrics).toInt()

                cell.layoutParams = TableRow.LayoutParams(width, height)
            }
        }
    }

    private fun checkMove(view: View) {}
}