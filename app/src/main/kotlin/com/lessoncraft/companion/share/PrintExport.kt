package com.lessoncraft.companion.share

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.lessoncraft.engine.Lesson
import com.lessoncraft.engine.LessonHtml
import java.io.File

/**
 * What Print and Export do once the plan allows them. Both work from the lesson's HTML, built on the device: Print uses Android's print framework
 * (including Save as PDF), Export shares an .html file through the system share sheet. Neither needs a network.
 */
object PrintExport {
    // The WebView must stay alive until the print job has been handed to the system.
    private var printing: WebView? = null

    fun print(context: Context, lesson: Lesson) {
        val web = WebView(context)
        printing = web
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val name = fileBase(lesson)
                val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                manager.print(name, view.createPrintDocumentAdapter(name), PrintAttributes.Builder().build())
                printing = null
            }
        }
        web.loadDataWithBaseURL(null, LessonHtml.render(lesson), "text/html", "utf-8", null)
    }

    fun export(context: Context, lesson: Lesson) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "${fileBase(lesson)}.html").apply { writeText(LessonHtml.render(lesson)) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, lesson.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share ${lesson.title}"))
    }

    private fun fileBase(l: Lesson) = l.title.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').take(60).ifEmpty { "lesson" }
}
