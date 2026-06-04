package com.punchlist.app.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.punchlist.app.data.model.Comment
import com.punchlist.app.data.model.PunchItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Generates a multi-page PDF for a single PunchItem and returns the cached File.
// Page size: US Letter (612 x 792 pts at 72 dpi).

object PunchItemPdfGenerator {

    private const val PAGE_WIDTH = 612
    private const val PAGE_HEIGHT = 792
    private const val MARGIN = 48f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2

    fun generate(context: Context, item: PunchItem, comments: List<Comment>, projectName: String): File {
        val doc = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var pageNumber = 1
        var page = startPage(doc, pageNumber)
        var canvas = page.canvas
        var y = MARGIN

        // ── Header ──────────────────────────────────────────────────────────
        paint.color = Color.parseColor("#1565C0")
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 72f, paint)

        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PUNCHLIST REPORT", MARGIN, 46f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        val dateStr = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
        canvas.drawText(dateStr, PAGE_WIDTH - MARGIN - paint.measureText(dateStr), 46f, paint)

        y = 90f

        // ── Project + Title ─────────────────────────────────────────────────
        paint.color = Color.parseColor("#424242")
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("PROJECT: $projectName", MARGIN, y, paint)
        y += 20f

        paint.color = Color.parseColor("#1565C0")
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        y = drawWrappedText(canvas, item.title, MARGIN, y, paint, CONTENT_WIDTH)
        y += 6f

        // ── Status + Priority pills ──────────────────────────────────────────
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT

        val statusColor = Color.parseColor(item.status.hexColor)
        drawPill(canvas, item.status.label, statusColor, MARGIN, y, paint)

        val priorityColor = Color.parseColor(item.priority.hexColor)
        drawPill(canvas, item.priority.label, priorityColor, MARGIN + 100f, y, paint)

        if (item.sku.isNotBlank()) {
            paint.color = Color.parseColor("#546E7A")
            paint.textSize = 11f
            canvas.drawText("SKU: ${item.sku}", MARGIN + 220f, y + 14f, paint)
        }
        y += 36f

        y = sectionDivider(canvas, y, paint)

        // ── Detail fields ───────────────────────────────────────────────────
        if (item.issueDescription.isNotBlank()) {
            y = labeledBlock(canvas, "ISSUE DESCRIPTION", item.issueDescription, y, paint)
        }
        if (item.workRequired.isNotBlank()) {
            y = labeledBlock(canvas, "WORK REQUIRED", item.workRequired, y, paint)
        }

        // Two-column meta row
        val leftMeta = buildList {
            if (item.location.isNotBlank()) add("Location" to item.location)
            if (item.assignedToUserName.isNotBlank()) add("Assigned To" to item.assignedToUserName)
            add("Created By" to item.createdByUserName)
        }
        val rightMeta = buildList {
            item.createdAt?.let { add("Created" to it.toDisplayDate()) }
            item.dueDate?.let { add("Due Date" to it.toDisplayDate()) }
            add("Priority" to item.priority.label)
        }
        val metaStartY = y
        var leftY = y
        var rightY = y
        val halfWidth = CONTENT_WIDTH / 2 - 8f

        for ((label, value) in leftMeta) {
            leftY = metaField(canvas, label, value, MARGIN, leftY, paint, halfWidth)
        }
        for ((label, value) in rightMeta) {
            rightY = metaField(canvas, label, value, MARGIN + CONTENT_WIDTH / 2 + 8f, rightY, paint, halfWidth)
        }
        y = maxOf(leftY, rightY) + 12f

        y = sectionDivider(canvas, y, paint)

        // ── Comments ─────────────────────────────────────────────────────────
        if (comments.isNotEmpty()) {
            paint.color = Color.parseColor("#1565C0")
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("COMMENTS (${comments.size})", MARGIN, y, paint)
            y += 18f

            for (comment in comments) {
                if (y > PAGE_HEIGHT - MARGIN - 60f) {
                    doc.finishPage(page)
                    pageNumber++
                    page = startPage(doc, pageNumber)
                    canvas = page.canvas
                    y = MARGIN
                }

                paint.color = Color.parseColor("#1565C0")
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val byLine = "${comment.userName}  ·  ${comment.createdAt?.toRelativeString() ?: ""}"
                canvas.drawText(byLine, MARGIN, y, paint)
                y += 16f

                paint.color = Color.parseColor("#424242")
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 11f
                y = drawWrappedText(canvas, comment.text, MARGIN + 8f, y, paint, CONTENT_WIDTH - 8f)
                y += 12f
            }
        }

        // ── Footer ───────────────────────────────────────────────────────────
        paint.color = Color.parseColor("#BDBDBD")
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        val footer = "Generated by Punchlist  •  Page $pageNumber"
        canvas.drawText(footer, MARGIN, PAGE_HEIGHT - 20f, paint)

        doc.finishPage(page)

        // Write to cache
        val file = File(context.cacheDir, "punchlist_${item.id.take(8)}.pdf")
        doc.writeTo(file.outputStream())
        doc.close()
        return file
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun startPage(doc: PdfDocument, number: Int): PdfDocument.Page {
        val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create()
        return doc.startPage(info)
    }

    private fun drawPill(canvas: Canvas, text: String, color: Int, x: Float, y: Float, paint: Paint) {
        val textWidth = paint.measureText(text)
        val pillRect = RectF(x, y, x + textWidth + 24f, y + 20f)
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(pillRect, 10f, 10f, paint)
        paint.color = Color.WHITE
        paint.textSize = 10f
        canvas.drawText(text, x + 12f, y + 14f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun sectionDivider(canvas: Canvas, y: Float, paint: Paint): Float {
        paint.color = Color.parseColor("#E0E0E0")
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
        return y + 14f
    }

    private fun labeledBlock(canvas: Canvas, label: String, value: String, y: Float, paint: Paint): Float {
        var cy = y
        paint.color = Color.parseColor("#757575")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(label, MARGIN, cy, paint)
        cy += 14f
        paint.color = Color.parseColor("#212121")
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        cy = drawWrappedText(canvas, value, MARGIN, cy, paint, CONTENT_WIDTH)
        return cy + 10f
    }

    private fun metaField(canvas: Canvas, label: String, value: String, x: Float, y: Float, paint: Paint, maxWidth: Float): Float {
        var cy = y
        paint.color = Color.parseColor("#757575")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(label, x, cy, paint)
        cy += 14f
        paint.color = Color.parseColor("#212121")
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        cy = drawWrappedText(canvas, value, x, cy, paint, maxWidth)
        return cy + 8f
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, startY: Float, paint: Paint, maxWidth: Float): Float {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(test) <= maxWidth) {
                current = StringBuilder(test)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())

        var y = startY
        val lineHeight = paint.textSize * 1.4f
        for (line in lines) {
            canvas.drawText(line, x, y, paint)
            y += lineHeight
        }
        return y
    }
}

// Extension helpers so we can get hex strings from enum colors
private val com.punchlist.app.data.model.Status.hexColor: String
    get() = when (this) {
        com.punchlist.app.data.model.Status.OPEN -> "#2196F3"
        com.punchlist.app.data.model.Status.IN_PROGRESS -> "#FF9800"
        com.punchlist.app.data.model.Status.NEEDS_REVIEW -> "#9C27B0"
        com.punchlist.app.data.model.Status.COMPLETE -> "#4CAF50"
    }

private val com.punchlist.app.data.model.Priority.hexColor: String
    get() = when (this) {
        com.punchlist.app.data.model.Priority.LOW -> "#4CAF50"
        com.punchlist.app.data.model.Priority.MEDIUM -> "#FF9800"
        com.punchlist.app.data.model.Priority.HIGH -> "#F44336"
        com.punchlist.app.data.model.Priority.URGENT -> "#9C27B0"
    }
