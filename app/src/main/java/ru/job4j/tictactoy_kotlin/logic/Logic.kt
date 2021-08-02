package ru.job4j.tictactoy_kotlin.logic

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import ru.job4j.tictactoy_kotlin.R
import ru.job4j.tictactoy_kotlin.model.Player
import ru.job4j.tictactoy_kotlin.model.Way
import kotlin.collections.ArrayList
import kotlin.math.sqrt

/**
 * @author Daniil Stebnitsky
 */

class Logic (private val activity: Activity,
             private val recycler: RecyclerView) {

    var queue = false
    var isAndroidInGame = false
    var isFirstStepByAndroid = false
    var isItAutoAlternationRegime = false
    var alternator = false
    var field = ArrayList<String>()
    var player1 = Player(1)
    var player2 = Player(2)
    private var size = 0
    private var side = 0
    private var isItEnd = false
    private var diagonalStep = 0
    private var reverseDiagonalStep = 0
    private var combination = ArrayList<Int>()
    private lateinit var xSequence: String
    private lateinit var oSequence: String

    private companion object Constants {
        private const val X = "X"
        private const val O = "O"
        private const val EMPTY = "\t"
        private const val BIG_FIELD_SIDE = 5
        private const val SMALL_FIELD_SIDE = 3
        private const val GREEN_COLOR = "#00EE00"
    }

    fun setField(whatSize: Boolean) {
        if (checkCleanField()) {
            generateFieldBySelectedSize(if (!whatSize) SMALL_FIELD_SIDE else BIG_FIELD_SIDE)
        }
        setParameters()
        androidFirstStep()
    }

    private fun setParameters() {
        size = field.size
        side = sqrt(size.toDouble()).toInt()
        xSequence = generateSequence(X)
        oSequence = generateSequence(O)
        diagonalStep = side + 1
        reverseDiagonalStep = side - 1
        setPlayersSymbols()
    }

    fun setPlayersSymbols() {
        player2.symbol = if (isFirstStepByAndroid) X else O
        player1.symbol = if (isFirstStepByAndroid) O else X
    }

    fun replacePlayersSymbols() {
        player2.symbol = if (alternator) X else O
        player1.symbol = if (alternator) O else X
    }

    fun setPlayersNames(name1: String?, name2: String?) {
        player1.name = name1 ?: ""
        player2.name = name2 ?: ""
    }

    fun setScore(p1Count: Int, p2Count: Int) {
        player1.score = p1Count
        player2.score = p2Count
    }

    fun updateNames(view1: TextView, view2: TextView) {
        view1.text = if (player1.symbol == X) player1.name else player2.name
        view2.text = if (player1.symbol == O) player1.name else player2.name
    }

    fun updateScore(view1: TextView, view2: TextView) {
        view1.text = if (player1.symbol == X) player1.score.toString() else player2.score.toString()
        view2.text = if (player1.symbol == O) player1.score.toString() else player2.score.toString()
    }

    fun getPlayerBySymbol(symbol: String): Player {
        return when {
            player1.symbol == symbol -> player1
            player2.symbol == symbol -> player2
            else -> Player(0)
        }
    }

    private fun winnerIs() {
        if (isWin()) {
            if (field[combination[0]] == X) {
                player1.score += if (player1.symbol == X) 1 else 0
                player2.score += if (player2.symbol == X) 1 else 0
            } else {
                player1.score += if (player1.symbol == O) 1 else 0
                player2.score += if (player2.symbol == O) 1 else 0
            }
        }
    }

    private fun generateFieldBySelectedSize(side: Int) {
        var count = 0
        field.clear()
        while (count < side * side) {
            field.add(EMPTY)
            count++
        }
    }

    private fun hasFree(): Boolean {
        return field.contains(EMPTY)
    }

    fun checkCleanField(): Boolean {
        return !field.contains(X)
    }

    private fun tryMark(id: Int): Boolean {
        val result = id < size && id > -1 && field[id] == EMPTY
        if (result) {
            field[id] = if (!queue) X else O
            queue = !queue
        }
        return result
    }

    fun cellMark(id: Int): String {
        var result = field[id]
        if (result == EMPTY && tryMark(id)) {
            result = field[id]
            if (isAndroidInGame) {
                androidInGame()
            }
        }
        tryToFinish(activity)
        return result
    }

    private fun tryToFinish(activity: Activity) {
        if ((isWin() || !hasFree()) && !isItEnd) {
            isItEnd = true
            winnerIs()
            paint()
            Handler(Looper.getMainLooper())
                .postDelayed({ finish(activity) }, 1200)
        }
    }

    private fun finish(activity: Activity) {
        activity.apply {
            val intent = Intent(activity.applicationContext, this.javaClass)
            finish()
            startActivity(intent)
        }
    }

    private fun isWin(): Boolean {
        checkHorizontal()
        checkVertical()
        checkDiagonal()
        return combination.size > 2
    }

    private fun generateSequence(symbol: String): String {
        var result = ""
        var count = 0
        while (count < side && count < BIG_FIELD_SIDE - 1) {
            result += symbol
            count++
        }
        return result
    }

    private fun paint() {
        for (index in combination) {
            recycler[index].findViewById<TextView>(R.id.symbol_textView)
                ?.setTextColor(Color.parseColor(GREEN_COLOR))
        }
    }

    fun androidFirstStep() {
        if (checkCleanField() && isFirstStepByAndroid) {
            field[(Math.random() * field.size).toInt()] = X
            queue = !queue
            recycler.adapter?.notifyDataSetChanged()
        }
    }

    fun androidInGame() {
        if ((queue && !isFirstStepByAndroid || !queue && isFirstStepByAndroid)
            && !isWin() && hasFree()) {
            recycler.findViewHolderForAdapterPosition(androidPower())
                ?.itemView?.performClick()
        }
    }

    private fun checkHorizontal(): ArrayList<ArrayList<Int>> {
        val result = ArrayList<ArrayList<Int>>()
        val size = if (side == SMALL_FIELD_SIDE) side else side * 2
        var i = 0
        while (result.size != size) {
            result.add(scan(i, 1))
            if (i == BIG_FIELD_SIDE * (BIG_FIELD_SIDE - 1)) {
                i = 1
                result.add(scan(i, 1))
            }
            i += side
        }
        return result
    }

    private fun checkVertical(): ArrayList<ArrayList<Int>> {
        val result = ArrayList<ArrayList<Int>>()
        var count = 0
        val threshold = if (side == SMALL_FIELD_SIDE) side else side * 2
        while (count < threshold) {
            result.add(scan(count, side))
            count++
        }
        return result
    }

    private fun checkDiagonal(): ArrayList<ArrayList<Int>> {
        val result = ArrayList<ArrayList<Int>>()
        result.apply {
            add(scan(0, diagonalStep))
            add(scan(side - 1, reverseDiagonalStep))
            if (side == BIG_FIELD_SIDE) {
                add(scan(1, diagonalStep))
                add(scan(5, diagonalStep))
                add(scan(6, diagonalStep))
                add(scan(3, reverseDiagonalStep))
                add(scan(8, reverseDiagonalStep))
                add(scan(9, reverseDiagonalStep))
            }
        }
        return result
    }

    private fun scan(start: Int, step: Int): ArrayList<Int> {
        val result = ArrayList<Int>()
        var set = ""
        var i = start
        while (i < size) {
            val s = field[i]
            set += s
            if (result.size < sequenceLength()) {
                result.add(i)
            }
            if (set == xSequence || set == oSequence) {
                combination = result
                break
            }
            i += step
        }
        return result
    }

    private fun androidPower(): Int {
        val allSequences = ArrayList<ArrayList<Int>>()
        allSequences.apply {
            addAll(checkHorizontal())
            addAll(checkVertical())
            addAll(checkDiagonal())
        }
        val ways = ArrayList<Way>()
        for (list in allSequences) {
            ways.add(calculatePriority(list))
        }
        return selectVariantByPriority(ways)
    }

    private fun selectVariantByPriority(list: ArrayList<Way>): Int {
        val priorityList = ArrayList<Int>()
        var countdown = sequenceLength()
        var iterator = sequenceLength()
        while (iterator >= 0) {
            for (way in list) {
                if (way.priority == countdown && way.cells.size > 0) {
                    priorityList.addAll(way.cells)
                    list.remove(way)
                    break
                }
            }
            if (priorityList.size == 0) {
                countdown--
            }
            iterator--
        }
        return if (priorityList.size == 1) priorityList[0]
        else checkMostPriorityCellsForMatches(priorityList, list)
    }

    private fun checkMostPriorityCellsForMatches(priorityList: ArrayList<Int>,
                                                 ways: ArrayList<Way>): Int {
        val variants = ArrayList<Int>()
        var countdown = sequenceLength() - 1
        while (countdown > 0 && variants.isEmpty()) {
            for (index in priorityList) {
                for (way in ways) {
                    if (way.priority == countdown && way.cells.contains(index)) {
                        variants.add(index)
                    }
                }
            }
            countdown--
        }
        return if (variants.isNotEmpty()) variants.random() else priorityList.random()
    }

    private fun calculatePriority(list: ArrayList<Int>): Way {
        val symbol = if (isFirstStepByAndroid) X else O
        var unionCount = 0
        var opponentCount = 0
        val cells = ArrayList<Int>()
        if (isWayFree(list)) {
            for (i in list) {
                when (field[i]) {
                    symbol -> {
                        unionCount ++
                        opponentCount--
                    }
                    EMPTY -> {
                        cells.add(i)
                    }
                    else -> {
                        opponentCount ++
                        unionCount--
                    }
                }
            }
        }
        return Way(if (unionCount == sequenceLength() - 1) unionCount + 1
        else unionCount.coerceAtLeast(opponentCount), cells)
    }

    private fun isWayFree(list: ArrayList<Int>): Boolean {
        var sequence = ""
        for (i in list) {
            sequence += field[i]
        }
        return sequence.contains(EMPTY)
    }

    private fun sequenceLength(): Int {
        return xSequence.length
    }
}