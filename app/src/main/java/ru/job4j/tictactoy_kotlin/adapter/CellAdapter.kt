package ru.job4j.tictactoy_kotlin.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.job4j.tictactoy_kotlin.R
import ru.job4j.tictactoy_kotlin.logic.Logic

/**
 * @author Daniil Stebnitsky
 */

class CellAdapter(private val list: ArrayList<String>,
                  private val logic: Logic,
                  private val activity: Activity) :
    RecyclerView.Adapter<CellAdapter.CellHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellHolder {
        return with(LayoutInflater.from(parent.context)) {
            CellHolder(inflate(R.layout.cell_element, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CellHolder, position: Int) {
        setView(holder.itemView, position)
    }

    private fun setView(view: View, position: Int) {
        val symbol = view.findViewById<TextView>(R.id.symbol_textView)
        symbol.text = list[position]
        symbol.textSize = setSymbolSize()
        view.apply {
            id = position
            layoutParams = setCellParams(layoutParams)
            setOnClickListener { symbol.text = logic.cellMark(id) }
        }
    }

    private fun setCellParams(params: ViewGroup.LayoutParams?): ViewGroup.LayoutParams? {
        val dp = activity.resources.displayMetrics.density
        val cellSize = (if (itemCount < 25) dp * 92 else dp * 60).toInt()
        params?.height = cellSize
        params?.width = cellSize
        return params
    }

    private fun setSymbolSize(): Float {
        val dp = activity.resources.displayMetrics.density
        return if (itemCount < 25) dp * 25 else dp * 20
    }

    class CellHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}