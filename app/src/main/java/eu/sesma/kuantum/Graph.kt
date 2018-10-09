package eu.sesma.kuantum

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.ImageView
import eu.sesma.kuantum.cuanto.model.QAData


class Graph {
    fun drawResult(imageView: ImageView, data: QAData) {
        val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawData(canvas, data)

        imageView.setImageBitmap(bitmap)
    }

    private fun drawData(canvas: Canvas, data: QAData) {

        val paint = Paint()
        val linePaint = Paint()
        linePaint.color = Color.YELLOW
        linePaint.strokeWidth = 1f

//        canvas.drawRect()
//        canvas.drawText()
    }
}