package com.alvdela.horsegame

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.icu.util.TimeUnit
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class HorseGameActivity : AppCompatActivity() {

    private var timeHandler: Handler? = null
    private var chronometer: Runnable = object: Runnable{
        override fun run() {
            try {
                if (gaming){
                    seconds++
                    updateTimeView(seconds)
                }
            }finally {
                timeHandler!!.postDelayed(this, 1000L)
            }
        }
    }

    private lateinit var lyMessage: LinearLayout
    private lateinit var cell: ImageView
    private lateinit var optionsMessage: TextView
    private lateinit var movesView: TextView
    private lateinit var progressBar: ProgressBar

    private var cellSelectedX = 0
    private var cellSelectedY = 0

    private var checkMove = true
    private var gaming = false

    private lateinit var board: Array<IntArray>

    private var options = 0
    private var moves = 64
    private var nextBonus = 0
    private var bonus = 0
    private var seconds = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.horse_game)
        startGame()

        val shareButton = findViewById<ImageButton>(R.id.share_button)
        shareButton.setOnClickListener{
            shareGame()
        }
    }

    private fun shareGame() {
        val screenshotBitmap = captureScreenShot()

        // Create a URI for the screenshot bitmap
        val screenshotUri = getImageUri(this, screenshotBitmap)

        // Create a share intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this awesome app!")
            putExtra(Intent.EXTRA_STREAM, screenshotUri)
            type = "image/*"
        }

        // Start activity to share
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    // Function to get URI from bitmap
    private fun getImageUri(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Screenshot", null)
        return Uri.parse(path)
    }

    private fun captureScreenShot(): Bitmap {
        val rootView = window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(rootView.drawingCache)
        rootView.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun startGame(){
        gaming = true
        //Inicializar tablero
        initScreenGame()
        resetBoard()
        //Establecer primera posicion
        setFirstPosition()
        //Iniciar cronometro
        resetTime()
        startTime()
    }

    /**
     * Metodo que inicializa el tablero
     */
    private fun resetBoard() {

        board = Array(8) { IntArray(8) }
        for (i in 0..7){
            for (j in 0..7){
                board[i][j] = 0
                //print("${board[i][j]}")
            }
            //println()
        }
    }

    /**
     * Metodo que establece la primera posición del caballo
     */
    private fun setFirstPosition() {
        var x = 0
        var y = 0
        x = Random.nextInt(0,8)
        y = Random.nextInt(0,8)

        cellSelectedX = x
        cellSelectedY = y

        selectCell(x,y)
    }

    /**
     * Metodo que se ejecuta cuando se marca una casilla
     */
    private fun selectCell(x: Int, y: Int) {
        //Restamos lo movimientos que quedan
        moves--
        movesView = findViewById(R.id.tvMovesNumber)
        movesView.text = moves.toString()

        //Incrementamos lo que queda para que aparezca una casilla de bonus
        nextBonus++
        progressBar = findViewById(R.id.progressBar)
        progressBar.progress = nextBonus

        //Si llegan a la casilla de bonus, incrementamos ese bonus
        if(board[x][y] == 2){
            bonus++
        }
        board[x][y] = 1

        //Marcamos las casillas que se han seleccionado
        paintHorseCell(cellSelectedX,cellSelectedY,"previous_cell")

        cellSelectedX = x
        cellSelectedY = y

        //Limpiamos el tablero de proximas opciones
        clearOptions()

        //Marcamos la casilla en la que estamos
        paintHorseCell(x,y,"selected_cell")
        checkMove = true

        //Comprobamos las opciones que tiene el jugador
        checkOptions(x,y)

        //Comprobamos si el jugado gana o pierde
        if(moves > 0){
            checkNewBonus()
            checkGameOver()
        }else{
            showMessage("You win!", "Next Level", false)
        }

    }

    /**
     * Metodo que comprueba el final de una partida
     */
    private fun checkGameOver() {
        if(options == 0){
            if (bonus > 0){
                checkMove = false
                paintAllOptions()
            }else{
                gaming = false
                showMessage("Game over", "Try again", false)
            }
        }
    }

    private fun paintAllOptions() {
        for (i in 0..7){
            for (j in 0..7){
                if (board[i][j] != 1){
                    paintOption(i,j)
                }
                if (board[i][j] == 0){
                    board[i][j] = 9
                }
            }
        }
    }

    /**
     * Metodo que muestra el mensaje de final de partida
     */
    private fun showMessage(title: String, action: String, win: Boolean) {
        lyMessage = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.VISIBLE

        val tvTitleMessage = findViewById<TextView>(R.id.tvTitleMessage)
        tvTitleMessage.text = title

        val tvTimeData = findViewById<TextView>(R.id.tvTimeNumber)

        var score: String = ""
        score = if (win){
            tvTimeData.text.toString()
        }else{
            "Score: ${64 - moves} / 64"
        }
        val tvScoreMessage = findViewById<TextView>(R.id.tvScoreMessage)
        tvScoreMessage.text = score

        val tvAction = findViewById<TextView>(R.id.tvAction)
        tvAction.text = action
    }

    /**
     * Metodo que comprueba y crea si hay que crear una casilla de bonus
     */
    private fun checkNewBonus() {
        if(nextBonus == 5){
            var bonusCellX = 0
            var bonusCellY = 0

            var bonusCell = true
            var i = 0
            while (bonusCell || i < 300){
                bonusCellX = Random.nextInt(0,8)
                bonusCellY = Random.nextInt(0,8)

                i++
                if (board[bonusCellX][bonusCellY] == 0){
                    bonusCell = false
                }
            }
            if (board[bonusCellX][bonusCellY] == 0){
                board[bonusCellX][bonusCellY] = 2
                printBonusCell(bonusCellX,bonusCellY)
            }
            nextBonus = 0
        }
    }

    /**
     * Metodo que "crea" una casilla de bonus
     */
    private fun printBonusCell(x: Int, y: Int) {
        cell = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        cell.setImageResource(R.drawable.ic_trophy)
        progressBar = findViewById(R.id.progressBar)
        progressBar.progress = nextBonus
    }

    /**
     * Metodo que limpia el tablero de las opciones disponibles
     */
    private fun clearOptions() {
        options = 0
        for (i in 0..7){
            for (j in 0..7){
                if(board[i][j] == 9){
                    board[i][j] = 0
                    clearOption(i,j)
                }
                if (board[i][j] == 2){
                    clearOption(i,j)
                }
            }
        }
    }

    /**
     * Metodo que limpia una casilla visualmente
     */
    private fun clearOption(x: Int, y: Int) {
        cell = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        if (checkColorCell(x,y) == "black"){
            cell.setBackgroundColor(ContextCompat.getColor(this,resources.getIdentifier("black_cell","color",packageName)))
        }else{
            cell.setBackgroundColor(ContextCompat.getColor(this,resources.getIdentifier("white_cell","color",packageName)))
        }
    }

    /**
     * Metodo que dibuja la casilla en la que se encuentra el jugador
     */
    private fun paintHorseCell(x: Int, y: Int, color: String) {

        cell = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        cell.setBackgroundColor(ContextCompat.getColor(this,resources.getIdentifier(color, "color", packageName)))
        if (color == "selected_cell") cell.setImageResource(R.drawable.chess_knight)
        if (color == "previous_cell") cell.setImageResource(R.drawable.empty)
    }

    /**
     * Inicializa el tablero de juego y ajusta su tamaño
     */
    private fun initScreenGame() {
        setSizeBoard()
        hideMessage()
    }

    /**
     * Oculta el mensaje de fin de partida
     */
    private fun hideMessage() {
        lyMessage = findViewById(R.id.lyMessage)
        lyMessage.visibility = View.INVISIBLE
    }

    /**
     * Establece el tamaño de la pantalla
     */
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

    /**
     * Comprueba si el movimiento que realiza el jugador es legal
     */
    fun checkMove(view: View) {
        val name = view.tag.toString()
        val x = name.subSequence(1,2).toString().toInt()
        val y = name.subSequence(2,3).toString().toInt()

        if(checkCell(x,y)){
            selectCell(x,y)
        }
    }

    private fun checkCell(x: Int, y: Int): Boolean {

        var checkTrue = false

        val diffX = x - cellSelectedX
        val diffY = y - cellSelectedY

        if (checkMove){
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
        }else{
            if (board[x][y] != 1){
                bonus --
                checkTrue = true
                val tvBonusData = findViewById<TextView>(R.id.tvOptionsNumber)
                if (bonus > 0){
                    tvBonusData.text = "$options + $bonus"
                }else{
                    tvBonusData.text = "$options"
                }
            }
        }
        return checkTrue
    }


    /**
     * Comprueba las opciones que tiene disponible el jugador
     */
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
        if (bonus > 0){
            optionsMessage.text = "${options.toString()} + $bonus"
        }else{
            optionsMessage.text = options.toString()
        }
    }

    private fun checkNextMove(x: Int, y: Int, nextX: Int, nextY: Int) {
        var optionX = x + nextX
        var optionY = y + nextY

        if(optionX < 8 && optionY < 8 && optionX >= 0 && optionY >= 0){
            if (board[optionX][optionY] == 0 || board[optionX][optionY] == 2){
                options++
                paintOption(optionX,optionY)
            }
        }
    }

    /**
     * Muestra visualmente las opciones disponibles al jugador
     */
    private fun paintOption(optionX: Int, optionY: Int) {
        cell = findViewById(resources.getIdentifier("c$optionX$optionY", "id", packageName))

        if(board[optionX][optionY] == 0)board[optionX][optionY] = 9

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

    /* Aquí comienzan las funciones para gestionar el tiempo */
    private fun updateTimeView(seconds: Int) {
        val formattedTime = getFormattedTime(seconds)
        val tvTimeData = findViewById<TextView>(R.id.tvTimeNumber)
        tvTimeData.text = formattedTime
    }

    private fun getFormattedTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return "${if (minutes<10) "0" else ""}$minutes:" +
                "${if (seconds<10) "0" else ""}$seconds"
    }

    private fun resetTime(){
        timeHandler?.removeCallbacks(chronometer)
        seconds = 0

        var tvTimeData = findViewById<TextView>(R.id.tvTimeNumber)
        tvTimeData.text = "00:00"
    }

    private fun startTime(){
        timeHandler = Handler(Looper.getMainLooper())
        chronometer.run()
    }
}