package ru.job4j.tictactoy_kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.job4j.tictactoy_kotlin.adapter.CellAdapter
import ru.job4j.tictactoy_kotlin.constants.Keys
import ru.job4j.tictactoy_kotlin.databinding.ActivityMainBinding
import ru.job4j.tictactoy_kotlin.logic.DialogMaster
import ru.job4j.tictactoy_kotlin.logic.Logic
import ru.job4j.tictactoy_kotlin.model.Player
import kotlin.math.sqrt

/**
 * @author Daniil Stebnitsky
 */

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var opponentSelector: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var queuePrioritySelector: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var sizeSelector: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var alternationSelector: Switch
    private lateinit var recycler: RecyclerView
    private lateinit var xPlayerName: TextView
    private lateinit var oPlayerName: TextView
    private lateinit var xPlayerScore: TextView
    private lateinit var oPlayerScore: TextView
    private lateinit var resetScoreButton: ImageView
    private lateinit var restartButton: ImageView
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var logic: Logic

    private fun SharedPreferences.Editor.savePlayer(player: Player) {
        putString(Keys.PLAYER_NAME + player.id, player.name)
        putInt(Keys.PLAYER_SCORE + player.id, player.score)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        findViews()
        setPreferences()
        setEditor()
        setSelectorsState()
        setLogicState()
        checkAutoAlternationRegime()
        listenToSelectors()
        setRecycler()
        setResetScoreButton()
        setRestartButton()
        setOnNamesClickListener()
        updateTextViews()
    }

    override fun onPause() {
        super.onPause()
        editor.apply {
            putBoolean(Keys.OPP_SELECT, opponentSelector.isChecked)
            putBoolean(Keys.Q_SELECT, queuePrioritySelector.isChecked)
            putBoolean(Keys.SIZE_SELECT, sizeSelector.isChecked)
            putBoolean(Keys.ALTERNATION_SELECT, alternationSelector.isChecked)
            putBoolean(Keys.ALTERNATOR, logic.alternator)
            savePlayer(logic.player1)
            savePlayer(logic.player2)
        }.apply()
    }

    private fun findViews() {
        opponentSelector = binding.opponentSelector
        queuePrioritySelector = binding.queueSelector
        sizeSelector = binding.fieldSizeSelectorSwitch
        alternationSelector = binding.alternationSwitch
        xPlayerName = binding.player1NameTextView
        oPlayerName = binding.player2NameTextView
        xPlayerScore = binding.player1CountTextView
        oPlayerScore = binding.player2CountTextView
        recycler = binding.recycler
        resetScoreButton = binding.resetCountButtonImageView
        restartButton = binding.restartButtonImageView
    }

    private fun setOnNamesClickListener() {
        xPlayerName.setOnClickListener { DialogMaster(this).openDialog(xPlayerName,
                logic.getPlayerBySymbol(Keys.X)) }
        oPlayerName.setOnClickListener { DialogMaster(this).openDialog(oPlayerName,
                logic.getPlayerBySymbol(Keys.O)) }
    }

    private fun setPreferences() {
        preferences = getSharedPreferences(
            Keys.NAME, Context.MODE_PRIVATE)
    }

    @SuppressLint("CommitPrefEdits")
    private fun setEditor() {
        editor = preferences.edit()
    }

    private fun setRecycler() {
        val list = logic.field
        recycler.layoutManager =
            GridLayoutManager(this, sqrt(list.size.toDouble()).toInt())
        recycler.adapter = CellAdapter(list, logic, this)
    }

    private fun setSelectorsState() {
        preferences.apply {
            val isOppSelectorChecked = getBoolean(Keys.OPP_SELECT, false)
            opponentSelector.isChecked = isOppSelectorChecked
            alternationSelector.isChecked = getBoolean(Keys.ALTERNATION_SELECT, false)
            queuePrioritySelector.isChecked =
                    if (!alternationSelector.isChecked) getBoolean(Keys.Q_SELECT, false)
                    else !getBoolean(Keys.ALTERNATOR, true)
            queuePrioritySelector.isEnabled = isOppSelectorChecked
            sizeSelector.isChecked = getBoolean(Keys.SIZE_SELECT, false)
        }
    }

    private fun setLogicState() {
        logic = Logic(this, recycler)
        logic.apply {
            isAndroidInGame = opponentSelector.isChecked
            isFirstStepByAndroid = queuePrioritySelector.isChecked && queuePrioritySelector.isEnabled
            isItAutoAlternationRegime = alternationSelector.isChecked
            alternator = !preferences.getBoolean(Keys.ALTERNATOR, true)
            setField(sizeSelector.isChecked)
            setPlayersNames(
                    preferences.getString(Keys.PLAYER_NAME + player1.id, getString(R.string.player1)),
                    preferences.getString(Keys.PLAYER_NAME + player2.id, getString(R.string.player2))
            )
            setScore(preferences.getInt(Keys.PLAYER_SCORE + player1.id, 0),
                    preferences.getInt(Keys.PLAYER_SCORE + player2.id, 0))
        }
    }

    private fun checkAutoAlternationRegime() {
        logic.apply {
            if (isItAutoAlternationRegime && !isAndroidInGame) {
                replacePlayersSymbols()
                updateTextViews()
            }
        }
    }

    private fun listenToSelectors() {
        listenToOppSelector()
        listenToQueueSelector()
        listenToSizeSelector()
        listenToAlternationSelector()
    }

    private fun listenToOppSelector() {
        opponentSelector.setOnCheckedChangeListener { _, isChecked ->
            logic.apply {
                isAndroidInGame = isChecked
                queuePrioritySelector.isEnabled = isChecked
                if (isChecked) {
                    if (queuePrioritySelector.isChecked && checkCleanField()) {
                        isFirstStepByAndroid = isChecked
                        setPlayersSymbols()
                        androidFirstStep()
                        updateTextViews()
                    } else {
                        androidInGame()
                    }
                }
            }
        }
    }

    private fun listenToQueueSelector() {
        queuePrioritySelector.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                logic.apply {
                    isFirstStepByAndroid = isChecked
                    setPlayersSymbols()
                    if (checkCleanField() && isFirstStepByAndroid) {
                        androidFirstStep()
                        recycler.adapter?.notifyDataSetChanged()
                        updateTextViews()
                    }
                }
            }
        }
    }

    private fun listenToSizeSelector() {
        sizeSelector.setOnCheckedChangeListener { _, isChecked ->
            if (logic.checkCleanField()) {
                logic.setField(isChecked)
                setRecycler()
            }
        }
    }

    private fun listenToAlternationSelector() {
        alternationSelector.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (opponentSelector.isChecked) {
                    queuePrioritySelector.isChecked = !logic.isFirstStepByAndroid
                } else {
                    logic.replacePlayersSymbols()
                    updateTextViews()
                }
            }
        }
    }

    private fun setResetScoreButton() {
        resetScoreButton.setOnClickListener {
            logic.setScore(0, 0)
            logic.updateScore(xPlayerScore, oPlayerScore)
        }
    }

    private fun setRestartButton() {
        restartButton.setOnClickListener {
            setLogicState()
            updateTextViews()
            setRecycler()
        }
    }

    private fun updateTextViews() {
        logic.updateNames(xPlayerName, oPlayerName)
        logic.updateScore(xPlayerScore, oPlayerScore)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}