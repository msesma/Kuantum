package eu.sesma.kuantum

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.ImageView
import eu.sesma.kuantum.cuanto.model.QAData


class Graph {
    fun drawResult(imageView: ImageView, data: QAData) {
        val counts = data.counts ?: return
        if (counts.isEmpty()) return

        val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawData(canvas, counts)

        imageView.setImageBitmap(bitmap)
    }

    private fun drawData(canvas: Canvas, counts: Map<String, Int>) {

        val linePaint = Paint()
        val recPaint = Paint()
        val smallPaint = Paint()
        linePaint.color = Color.BLACK
        linePaint.strokeWidth = 4f
        linePaint.textSize = 48f
        recPaint.color = Color.YELLOW
        smallPaint.textSize = 36f

        val itemWidth = canvas.width / (2 * counts.size + 1)
        val totalHeight = canvas.height.toFloat()
        val total = counts.values.sum()
        val items = counts.map { it.key to totalHeight * 0.9f * it.value / total }

        items.forEachIndexed { index, item ->
            val hPos = (2 * index + 1) * itemWidth.toFloat()
            canvas.drawRect(hPos, totalHeight * 0.9f, hPos + itemWidth, totalHeight * 0.9f - item.second, recPaint)

            val label1 = "|${item.first}\u27E9"
            val offset1 = (itemWidth - linePaint.measureText(label1)) / 2
            canvas.drawText(label1, hPos + offset1, totalHeight * 0.99f, linePaint)

            val label2 = "${(counts[item.first] ?: 1).toFloat() / total}"
            val offset2 = (itemWidth - linePaint.measureText(label2)) / 2
            canvas.drawText(label2, hPos + offset2, totalHeight * 0.09f, linePaint)
        }

        canvas.drawLine(0f, totalHeight * 0.9f, canvas.width.toFloat(), totalHeight * 0.9f, linePaint)
        canvas.drawLine(0f, totalHeight / 10, canvas.width.toFloat(), totalHeight / 10, linePaint)
        canvas.drawText("100%", 0f, totalHeight * 0.08f, smallPaint)
        canvas.drawText("  0%", 0f, totalHeight * 0.98f, smallPaint)
    }
}