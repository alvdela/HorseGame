package com.alvdela.horsegame

import android.graphics.Point
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var lyMessage: LinearLayout
    private lateinit var cell: ImageView
    private lateinit var optionsMessage: TextView
    private lateinit var movesView: TextView

    private var cellSelectedX = 0
    private var cellSelectedY = 0

    private lateinit var board: Array<IntArray>

    private var options = 0
    private var moves = 64
    private var nestBonus = 4
    private var bonus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initScreenGame()
        resetBoard()
        setFirstPosition()
    }

    private fun resetBoard() {

        board = Array(8) { IntArray(8) }
        for (i in 0..7){
            for (j in 0..7){
                board[i][j] = 0
                print("${board[i][j]}")
            }
            println()
        }
    }

    private fun setFirstPosition() {
        var x = 0
        var y = 0
        x = Random.nextInt(0,8)
        y = Random.nextInt(0,8)

        cellSelectedX = x
        cellSelectedY = y

        selectCell(x,y)
    }

    private fun selectCell(x: Int, y: Int) {

        moves--
        movesView = findViewById(R.id.tvMovesNumber)
        movesView.text = moves.toString()

        board[x][y] = 1

        paintHorseCell(cellSelectedX,cellSelectedY,"previous_cell")

        cellSelectedX = x
        cellSelectedY = y

        clearOptions()

        paintHorseCell(x,y,"selected_cell")

        checkOptions(x,y)

        if(moves > 0){
            checkNewBonus()
            checkGameOver()
        }else{
            chechSuccesfull()
        }

    }

    private fun chechSuccesfull() {
        TODO("Not yet implemented")
    }

    private fun checkGameOver() {
        TODO("Not yet implemented")
    }

    private fun checkNewBonus() {
        if(moves%nestBonus == 0){
            var bonusCellX = 0
            var bonusCellY = 0

            var bonusCell = true
            var i = 0
            while (bonusCell || i < 200){
                bonusCellX = Random.nextInt(0,8)
                bonusCellY = Random.nextInt(0,8)

                i++
                if (board[bonusCellX][bonusCellY] == 0){
                    bonusCell = false
                }
            }
            board[bonusCellX][bonusCellY] = 2
            printBonusCell(bonusCellX,bonusCellY)
        }
    }

    private fun printBonusCell(x: Int, y: Int) {
        cell = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        cell.setImageResource(R.drawable.ic_trophy)
    }

    private fun clearOptions() {
        options = 0
        for (i in 0..7){
            for (j in 0..7){
                if(board[i][j] == 9){
                    board[i][j] = 0
                    clearOption(i,j)
                }
            }
        }
    }

    private fun clearOption(x: Int, y: Int) {
        cell = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        if (checkColorCell(x,y) == "black"){
            cell.setBackgroundColor(ContextCompat.getColor(this,resources.getIdentifier("black_cell","color",packageName)))
        }else{
            cell.setBackgroundColor(ContextCompat.getColor(this,resources.getIdentifier("white_cell","color",packageName)))
        }
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

                val height = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    widthCell,
                    resources.displayMetrics
                ).toInt()
                val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,widthCell,resources.displayMetrics).toInt()

                cell.layoutParams = TableRow.LayoutParams(width, height)
            }
        }
    }

    fun checkMove(view: View) {
        val name = view.tag.toString()
        val x = name.subSequence(1,2).toString().toInt()
        val y = name.subSequence(2,3).toString().toInt()

        if(checkCell(x,y)){
            selectCell(x,y)
        }
    }

    private fun checkCell(x: Int, y: Int): Boolean {

        val diffX = x - cellSelectedX
        val diffY = y - cellSelectedY

        var checkTrue = false

        if (diffX == 1 && diffY == 2){
            checkTrue = true
        }
        if (diffX == 1 && diffY == -2){
            checkTrue = true
        }
        if (diffX == 2 && diffY == 1){
            checkTrue = true
        }
        if (diffX == 2 && diffY == -1){
            checkTrue = true
        }
        if (diffX == -1 && diffY == 2){
            checkTrue = true
        }
        if (diffX == -1 && diffY == -2){
            checkTrue = true
        }
        if (diffX == -2 && diffY == 1){
            checkTrue = true
        }
        if (diffX == -2 && diffY == -1){
            checkTrue = true
        }

        if(board[x][y] == 1){
            checkTrue = false
        }

        return checkTrue
    }


    private fun checkOptions(x: Int, y:Int){

        checkNextMove(x,y,1,2)
        checkNextMove(x,y,2,1)
        checkNextMove(x,y,1,-2)
        checkNextMove(x,y,2,-1)
        checkNextMove(x,y,-1,2)
        checkNextMove(x,y,-2,1)
        checkNextMove(x,y,-1,-2)
        checkNextMove(x,y,-2,-1)

        optionsMessage = findViewById(R.id.tvOptionsNumber)
        optionsMessage.text = options.toString()
    }

    private fun checkNextMove(x: Int, y: Int, nextX: Int, nextY: Int) {
        var optionX = x + nextX
        var optionY = y + nextY

        if(optionX < 8 && optionY < 8 && optionX >= 0 && optionY >= 0){
            if (board[optionX][optionY] == 0 || board[optionX][optionY] == 2){
                options++
                paintOptions(optionX,optionY)
            }
        }
    }

    private fun paintOptions(optionX: Int, optionY: Int) {
        cell = findViewById(resources.getIdentifier("c$optionX$optionY", "id", packageName))
        board[optionX][optionY] = 9

        if (checkColorCell(optionX,optionY) == "black"){
            cell.setBackgroundResource(R.drawable.option_black)
        }else{
            cell.setBackgroundResource(R.drawable.option_white)
        }
    }

    private fun checkColorCell(x: Int, y: Int): String{
        val blackColumn = arrayOf(0,2,4,6)
        val blackRow = arrayOf(1,3,5,7)

        if ((blackColumn.contains(x) && blackColumn.contains(y))
            || blackRow.contains(x) && blackRow.contains(y)){
            return "black"
        }
        return "white"
    }
}