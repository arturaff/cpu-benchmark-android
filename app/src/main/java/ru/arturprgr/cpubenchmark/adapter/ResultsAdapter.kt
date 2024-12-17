package ru.arturprgr.cpubenchmark.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.arturprgr.cpubenchmark.R
import ru.arturprgr.cpubenchmark.databinding.LayoutResultBinding
import ru.arturprgr.cpubenchmark.data.Result
import java.text.DecimalFormat

class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
    private val list = arrayListOf<Result>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(result: Result) = with(LayoutResultBinding.bind(itemView)) {
            textDevice.text = result.device
            textResult.text = "Результат: ${DecimalFormat("###,###.##").format(result.result)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_result, parent, false
            )
        )

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    fun addResult(result: Result) {
        list.add(result)
        notifyItemRangeChanged(result.index, list.size)
    }

    fun getList(): ArrayList<Result> = list
}