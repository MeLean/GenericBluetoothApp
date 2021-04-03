package com.milen.bluetoothapp.ui

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.milen.bluetoothapp.R
import com.milen.bluetoothapp.utils.beGone
import com.milen.bluetoothapp.utils.beVisible
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.Executors

const val PATH = "#"
const val BRICK = "b"
const val EXIT = "E"
const val VISITED = "X"

const val ITEM_SEPARATOR = " "
const val RESULT_SEPARATOR = "; "



class MainActivity : AppCompatActivity() {
    private val defaultMaze = arrayOf(
        arrayOf(PATH, PATH, PATH, BRICK, PATH, PATH, PATH),
        arrayOf(BRICK, BRICK, PATH, BRICK, PATH, BRICK, PATH),
        arrayOf(PATH, PATH, PATH, PATH, PATH, PATH, PATH),
        arrayOf(PATH, BRICK, BRICK, BRICK, BRICK, BRICK, PATH),
        arrayOf(PATH, PATH, PATH, PATH, PATH, PATH, EXIT)
    )
    private val curRes = Stack<String>()
    private val mazeResults = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        maze_start.setText(stringifyMaze(defaultMaze))

        find_path_btn.setOnClickListener {
            loading_progress.beVisible()
            curRes.clear()
            mazeResults.clear()
            hideSofKeyboard(maze_start)
            it.requestFocus()
            loadMazeFromUi()
        }
    }

    private fun hideSofKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    private fun loadMazeFromUi() {
        val mazeStr = maze_start.text.toString()
        val someRunnable = Runnable {
            val maze = extractMazeFromString(mazeStr)


            findPath(maze, 0, 0)

            this@MainActivity.runOnUiThread {
                if (mazeResults.isNotEmpty()) {
                    val shortest = mazeResults.minBy { it.length }
                    maze_result.text = stringifyMaze(maze, shortest)
                    result_string.text = shortest
                } else {
                    maze_result.text = ""
                    result_string.text = getString(R.string.no_path_found)
                }

                loading_progress.beGone()
            }
        }

        when (isValidInput(mazeStr)) {
            true -> Executors.newSingleThreadExecutor().execute(someRunnable)
            else -> complain(getString(R.string.not_valid_input))
        }
    }

    private fun isValidInput(mazeStr: String): Boolean {

        return if (!mazeStr.contains(EXIT)) {
            false
        } else {
            //TODO make more detailed validation
            val carsLeft = mazeStr.replace("""[$PATH$BRICK$EXIT\s\n]""".toRegex(), "")
            carsLeft.isEmpty()
        }
    }

    private fun extractMazeFromString(mazeStr: String): Array<Array<String>> {
        val enteredRows = mazeStr.trim().split("\n")
        val result: MutableList<Array<String>> = mutableListOf()
        for (i in enteredRows.indices) {
            val row = enteredRows[i].trim().split(ITEM_SEPARATOR).toTypedArray()
            result.add(i, row)
        }
        return result.toTypedArray()
    }

    private fun complain(msg: String) {
        loading_progress.beGone()
        Snackbar.make(maze_start, msg, Snackbar.LENGTH_LONG).show()
    }

    private fun findPath(maze: Array<Array<String>>, row: Int, col: Int) {

        if (!isNextStepAvailable(maze, row, col)) {
            return
        }

        if (maze[row][col] == EXIT) {
            mazeResults.add(
                curRes.joinToString(separator = RESULT_SEPARATOR)
            )
            return
        }

        maze[row][col] = VISITED
        addResultCoordinate(row, col)

        findPath(maze, row, col + 1) // right
        findPath(maze, row + 1, col) // down
        findPath(maze, row, col - 1) // left
        findPath(maze, row - 1, col) // up

        maze[row][col] = PATH
        removeLastCoordinateFromResult()
    }

    private fun stringifyMaze(
        maze: Array<Array<String>>,
        strResult: String? = null
    ): String {
        strResult?.let {
            val resultArray = strResult.split(RESULT_SEPARATOR)

            for (item in resultArray) {
                val itemArr = item.split(ITEM_SEPARATOR)
                maze[itemArr[0].toInt()][itemArr[1].toInt()] = VISITED
            }
        }

        val sb = StringBuilder()
        for (list in maze) {
            for (item in list) {
                sb.append("$item$ITEM_SEPARATOR")
            }
            sb.append("\n")
        }

        return sb.toString()
    }

    private fun removeLastCoordinateFromResult() {
        curRes.pop()
    }

    private fun addResultCoordinate(row: Int, col: Int) {
        curRes.add("$row$ITEM_SEPARATOR$col")
    }

    private fun isNextStepAvailable(maze: Array<Array<String>>, row: Int, col: Int): Boolean {
        val isInMaze = row < maze.size && col < maze[0].size && col >= 0 && row >= 0
        if (!isInMaze) {
            return false
        }

        val isAccessibleField = maze[row][col] == PATH || maze[row][col] == EXIT
        return isInMaze && isAccessibleField
    }
}
