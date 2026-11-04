package org.libreoffice

import android.content.Intent
import android.os.SystemClock
import android.view.WindowManager
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mydocvault.utils.getMimeTypeForFileName
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfficeReaderTest {
    @Test fun opensFormattedDocumentsSheetsAndSlides() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        for (name in listOf("Field notes.docx", "Inventory.xlsx", "Reading room.pptx", "Letter.odt")) {
            val file = File(context.cacheDir, name)
            try {
                instrumentation.context.assets.open(name).use { input -> file.outputStream().use { input.copyTo(it) } }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(context, LibreOfficeMainActivity::class.java)
                    .setDataAndType(uri, getMimeTypeForFileName(name))
                    .putExtra(Intent.EXTRA_TITLE, name)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                ActivityScenario.launch<LibreOfficeMainActivity>(intent).use { scenario ->
                    val deadline = SystemClock.elapsedRealtime() + 120_000
                    var ready = false
                    while (!ready && SystemClock.elapsedRealtime() < deadline) {
                        scenario.onActivity { activity ->
                            assertTrue(activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0)
                            val provider = activity.tileProvider
                            ready = provider != null && provider.isReady && provider.pageWidth > 0 && provider.pageHeight > 0
                            if (ready) {
                                when (file.extension) {
                                    "xlsx" -> assertTrue(provider.isSpreadsheet)
                                    "pptx" -> assertTrue(provider.isPresentation)
                                    else -> assertTrue(provider.isTextDocument)
                                }
                            }
                        }
                        if (!ready) SystemClock.sleep(250)
                    }
                    assertTrue("LibreOffice failed to lay out $name", ready)
                }
            } finally {
                file.delete()
            }
        }
    }
}
