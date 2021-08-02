package ru.job4j.tictactoy_kotlin.model

/**
 * @author Daniil Stebnitsky
 */

data class Player(val id: Int, var name: String = "", var score: Int = 0, var symbol: String = "")