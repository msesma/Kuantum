package eu.sesma.kuantum

import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import eu.sesma.kuantum.cuanto.model.QAData


//https://github.com/PhilJay/MPAndroidChart/wiki/Getting-Started

class Graph {
    fun drawResult(barChart: BarChart, data: QAData) {
        val counts = data.counts ?: return
        if (counts.isEmpty()) return

        val total = counts.values.sum()
        val items = counts.map { "|${it.key}\u27E9" to it.value.toFloat() / total }
        val entries = items.mapIndexed() { index, value -> BarEntry(index.toFloat(), value.second) }
        val labels = items.map { it.first }

        val formatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                return labels[value.toInt()]
            }
        }

        barChart.description.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.granularity = 1f // minimum axis-step (interval) is 1
        xAxis.valueFormatter = formatter

        val yAxisL = barChart.axisLeft
        yAxisL.axisMinimum = 0f
        yAxisL.axisMaximum = 1f

        val yAxisR = barChart.axisRight
        yAxisR.axisMinimum = 0f
        yAxisR.axisMaximum = 1f

        val dataSet = BarDataSet(entries, barChart.context.getString(R.string.probability))
        dataSet.color = Color.YELLOW
        barChart.data = BarData(dataSet)
        barChart.invalidate()
    }
}