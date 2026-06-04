package com.punchlist.app.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import androidx.core.print.PrintHelper
import com.punchlist.app.data.model.Comment
import com.punchlist.app.data.model.PunchItem
import java.io.File

object PrintShareHelper {

    // ── Share PDF via Android share sheet ────────────────────────────────────
    // Opens the system share chooser so the user can pick Gmail, Outlook, Drive,
    // Messages, or any other app that accepts PDF files.

    fun sharePdf(context: Context, item: PunchItem, comments: List<Comment>, projectName: String) {
        val file = PunchItemPdfGenerator.generate(context, item, comments, projectName)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Punchlist Item: ${item.title}")
            putExtra(
                Intent.EXTRA_TEXT,
                buildEmailBody(item, comments, projectName)
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Share Punchlist Item")
        )
    }

    // ── Share plain text via email intent ────────────────────────────────────
    // Pre-fills the To, Subject, and Body fields if the user has an email client.

    fun shareViaEmail(context: Context, item: PunchItem, comments: List<Comment>, projectName: String) {
        val body = buildEmailBody(item, comments, projectName)
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, "Punchlist Item: ${item.title}")
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(emailIntent, "Send via email"))
    }

    // ── Print using Android PrintManager ─────────────────────────────────────
    // Renders the PDF and sends it to the system print dialog. Works with
    // Bluetooth printers, Google Cloud Print, PDF export, etc.

    fun print(context: Context, item: PunchItem, comments: List<Comment>, projectName: String) {
        val file = PunchItemPdfGenerator.generate(context, item, comments, projectName)
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        val jobName = "Punchlist - ${item.title}"
        printManager.print(
            jobName,
            PdfPrintDocumentAdapter(file),
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
        )
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun buildEmailBody(item: PunchItem, comments: List<Comment>, projectName: String): String {
        return buildString {
            appendLine("PUNCHLIST ITEM")
            appendLine("==============")
            appendLine("Project: $projectName")
            appendLine("Title: ${item.title}")
            appendLine("Status: ${item.status.label}")
            appendLine("Priority: ${item.priority.label}")
            if (item.sku.isNotBlank()) appendLine("SKU: ${item.sku}")
            if (item.location.isNotBlank()) appendLine("Location: ${item.location}")
            if (item.assignedToUserName.isNotBlank()) appendLine("Assigned To: ${item.assignedToUserName}")
            appendLine("Created By: ${item.createdByUserName}")
            item.createdAt?.let { appendLine("Created: ${it.toDisplayDate()}") }
            item.dueDate?.let { appendLine("Due: ${it.toDisplayDate()}") }

            if (item.issueDescription.isNotBlank()) {
                appendLine()
                appendLine("ISSUE DESCRIPTION")
                appendLine(item.issueDescription)
            }
            if (item.workRequired.isNotBlank()) {
                appendLine()
                appendLine("WORK REQUIRED")
                appendLine(item.workRequired)
            }

            if (comments.isNotEmpty()) {
                appendLine()
                appendLine("COMMENTS (${comments.size})")
                appendLine("-".repeat(30))
                for (comment in comments) {
                    appendLine("${comment.userName}: ${comment.text}")
                }
            }

            appendLine()
            appendLine("Sent from Punchlist")
        }
    }
}

// Minimal PrintDocumentAdapter that serves a pre-generated PDF file.
private class PdfPrintDocumentAdapter(private val file: File) :
    android.print.PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback,
        extras: android.os.Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        val info = android.print.PrintDocumentInfo.Builder(file.name)
            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out android.print.PageRange>,
        destination: android.os.ParcelFileDescriptor,
        cancellationSignal: android.os.CancellationSignal?,
        callback: WriteResultCallback
    ) {
        try {
            file.inputStream().use { input ->
                android.os.ParcelFileDescriptor.AutoCloseOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        }
    }
}
