package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.SavedWord
import com.example.ui.theme.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExportVocabularyDialog(
    savedWords: List<SavedWord>,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    // Prepare CSV Content
    val csvContent = remember(savedWords) {
        val sBuilder = StringBuilder()
        sBuilder.append("No,Word,Part of Speech,Translation,Source Movie,Status,Example Sentence,Sentence Translation\n")
        savedWords.forEachIndexed { index, word ->
            val cleanWord = escapeCsvField(word.word)
            val cleanPos = escapeCsvField(word.partOfSpeech)
            val cleanTranslation = escapeCsvField(word.translation)
            val cleanMovie = escapeCsvField(word.movieName)
            val cleanStatus = if (word.isLearned) "Learned" else "Studying"
            val cleanSentence = escapeCsvField(word.sentence)
            val cleanSentenceTrans = escapeCsvField(word.sentenceTranslation)
            sBuilder.append("${index + 1},$cleanWord,$cleanPos,$cleanTranslation,$cleanMovie,$cleanStatus,$cleanSentence,$cleanSentenceTrans\n")
        }
        sBuilder.toString()
    }

    // Prepare text to share
    val textToShare = remember(savedWords) {
        val sBuilder = StringBuilder()
        sBuilder.append("🎬 CineLingo - Офлайн суралцах үгс (${savedWords.size})\n\n")
        savedWords.forEachIndexed { index, word ->
            val statusEmoji = if (word.isLearned) "✅" else "📖"
            val posStr = if (word.partOfSpeech.isNotBlank()) " [${word.partOfSpeech.lowercase()}]" else ""
            sBuilder.append("${index + 1}. ${word.word}$posStr - ${word.translation} $statusEmoji\n")
            if (word.sentence.isNotEmpty()) {
                sBuilder.append("   └ Жишээ: ${word.sentence}\n")
            }
        }
        sBuilder.append("\n🎨 CineLingo ашиглан киногоор англи хэл сурцгаая!")
        sBuilder.toString()
    }

    // Launchers for saving files
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "CSV файлыг амжилттай хадгаллаа!", Toast.LENGTH_LONG).show()
                onDismissRequest()
            } catch (e: Exception) {
                Toast.makeText(context, "CSV хадгалахад алдаа гарлаа: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val pdfDocument = generatePdfDocument(context, savedWords)
                    pdfDocument.writeTo(outputStream)
                    pdfDocument.close()
                }
                Toast.makeText(context, "PDF файлыг амжилттай хадгаллаа!", Toast.LENGTH_LONG).show()
                onDismissRequest()
            } catch (e: Exception) {
                Toast.makeText(context, "PDF хадгалахад алдаа гарлаа: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                2.dp,
                Brush.linearGradient(listOf(NeonCyan, NeonMagenta)),
                RoundedCornerShape(24.dp)
            )
            .background(NeonDarkBg, RoundedCornerShape(24.dp)),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ОФФЛАЙН СУДЛАХ ХУУДАС",
                            color = NeonMagenta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Үгийн санг экспортлох",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Хаах",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Суралцаж буй бүх үгсийг гар утас болон компьютер дээрээ офлайн хэлбэрээр уншиж давтах файл бэлтгэх.",
                    color = TextGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Options list
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option 1: PDF Document
                    ExportOptionCard(
                        title = "PDF файл болгож хадгалах",
                        description = "Хэвлэх эсвэл PDF уншигч баримтаар офлайн уншина.",
                        icon = Icons.Default.Description,
                        accentColor = NeonCyan,
                        onClick = {
                            val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
                            val filename = "CineLingo_WordBank_${sdf.format(Date())}.pdf"
                            pdfLauncher.launch(filename)
                        },
                        tag = "export_pdf_option"
                    )

                    // Option 2: CSV Document
                    ExportOptionCard(
                        title = "CSV (Excel, Google Sheets) хадгалах",
                        description = "Excel, Google Sheets болон бусад программд нээж ашиглана.",
                        icon = Icons.Default.TableChart,
                        accentColor = NeonYellow,
                        onClick = {
                            val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
                            val filename = "CineLingo_WordBank_${sdf.format(Date())}.csv"
                            csvLauncher.launch(filename)
                        },
                        tag = "export_csv_option"
                    )

                    // Option 3: Share Text
                    ExportOptionCard(
                        title = "Түүвэр Текстээр хуваалцах",
                        description = "Жагсаалтыг найзууд руугаа чатаар илгээх эсвэл тэмдэглэлд хуулах.",
                        icon = Icons.Default.Share,
                        accentColor = NeonMagenta,
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, textToShare)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Англи үгсийн түүврийг хуваалцах")
                            context.startActivity(shareIntent)
                            onDismissRequest()
                        },
                        tag = "export_share_option"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Нийт ${savedWords.size} үгийн карт бэлэн байна",
                        color = NeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun ExportOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = NeonCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextGray,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// Escapes CSV cell to conform to RFC 4180
private fun escapeCsvField(field: String): String {
    val clean = field.replace("\"", "\"\"")
    return if (clean.contains(",") || clean.contains("\"") || clean.contains("\n")) {
        "\"$clean\""
    } else {
        clean
    }
}

// Generates PDF document offline
private fun generatePdfDocument(context: Context, savedWords: List<SavedWord>): PdfDocument {
    val pdfDocument = PdfDocument()

    // 1. Paints
    val titlePaint = Paint().apply {
        color = android.graphics.Color.rgb(15, 15, 20)
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val subtitlePaint = Paint().apply {
        color = android.graphics.Color.rgb(100, 110, 120)
        textSize = 10f
        isAntiAlias = true
    }
    val headerPaint = Paint().apply {
        color = android.graphics.Color.rgb(30, 40, 50)
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val bodyPaint = Paint().apply {
        color = android.graphics.Color.rgb(20, 20, 25)
        textSize = 10f
        isAntiAlias = true
    }
    val italicBodyPaint = Paint().apply {
        color = android.graphics.Color.rgb(110, 115, 125)
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        isAntiAlias = true
    }
    val statusLearnedPaint = Paint().apply {
        color = android.graphics.Color.rgb(0, 160, 90) // Beautiful green
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val statusStudyingPaint = Paint().apply {
        color = android.graphics.Color.rgb(220, 130, 0) // Orange/gold
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val gridPaint = Paint().apply {
        color = android.graphics.Color.rgb(225, 230, 235)
        strokeWidth = 0.5f
        isAntiAlias = true
    }
    val ribbonPaint = Paint().apply {
        color = android.graphics.Color.rgb(212, 11, 110) // print-safe magenta
        isAntiAlias = true
    }

    // A4 width and height in points: 595 x 842
    val pageW = 595
    val pageH = 842
    val rowsPerPage = 25
    val rowHeight = 24f

    val totalPages = if (savedWords.isEmpty()) 1 else Math.ceil(savedWords.size.toDouble() / rowsPerPage.toDouble()).toInt()
    
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val printedDate = sdf.format(Date())

    fun truncateText(text: String, paint: Paint, maxW: Float): String {
        if (paint.measureText(text) <= maxW) return text
        var result = text
        while (result.isNotEmpty() && paint.measureText("$result...") > maxW) {
            result = result.substring(0, result.length - 1)
        }
        return "$result..."
    }

    fun drawHeaderAndFooter(canvas: Canvas, pageNumber: Int) {
        // Draw Accent Top Ribbon
        canvas.drawRect(40f, 35f, 555f, 39f, ribbonPaint)

        // Draw title
        canvas.drawText("CineLingo - Офлайн суралцах үгс", 40f, 60f, titlePaint)
        
        // Subtitle info
        canvas.drawText("Суралцах хувийн картны жагсаалт | Экспортлосон огноо: $printedDate", 40f, 75f, subtitlePaint)
        
        // Header Divider
        canvas.drawLine(40f, 85f, 555f, 85f, gridPaint)
        
        // Table Columns headers
        canvas.drawText("#", 40f, 105f, headerPaint)
        canvas.drawText("АНГЛИ ҮГ (ХЭЛНИЙ АЙМАГ)", 70f, 105f, headerPaint)
        canvas.drawText("МОНГОЛ ОРЧУУЛГА", 220f, 105f, headerPaint)
        canvas.drawText("КИНО / СУРВАЛЖ", 380f, 105f, headerPaint)
        canvas.drawText("ТӨЛӨВ", 495f, 105f, headerPaint)
        
        // Table Header Underline
        canvas.drawLine(40f, 113f, 555f, 113f, gridPaint)

        // Draw Footer
        canvas.drawLine(40f, 792f, 555f, 792f, gridPaint)
        canvas.drawText("CineLingo - Киногоор Англи хэлийг ухаалгаар сурах нь", 40f, 807f, subtitlePaint)
        canvas.drawText("Хуудас $pageNumber / $totalPages", 485f, 807f, subtitlePaint)
    }

    var pageNum = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNum).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas

    drawHeaderAndFooter(canvas, pageNum)
    var currentY = 132f

    savedWords.forEachIndexed { index, word ->
        // Pagination logic
        if (currentY + rowHeight > 780f) {
            pdfDocument.finishPage(page)
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNum).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            drawHeaderAndFooter(canvas, pageNum)
            currentY = 132f
        }

        // 1. Number column
        canvas.drawText("${index + 1}", 40f, currentY, bodyPaint)

        // 2. English word with Part of Speech
        val pSpeech = if (word.partOfSpeech.isNotBlank()) " [${word.partOfSpeech.lowercase()}]" else ""
        val wordDisplay = truncateText(word.word + pSpeech, bodyPaint, 140f)
        canvas.drawText(wordDisplay, 70f, currentY, bodyPaint)

        // 3. Mongolian Translation
        val translationDisplay = truncateText(word.translation, bodyPaint, 150f)
        canvas.drawText(translationDisplay, 220f, currentY, bodyPaint)

        // 4. Source movie title
        val movieSource = truncateText(word.movieName.ifBlank { "Хадгалсан" }, italicBodyPaint, 110f)
        canvas.drawText(movieSource, 380f, currentY, italicBodyPaint)

        // 5. Learned Status
        if (word.isLearned) {
            canvas.drawText("Цээжилсэн", 495f, currentY, statusLearnedPaint)
        } else {
            canvas.drawText("Судалж буй", 495f, currentY, statusStudyingPaint)
        }

        // Gridline row separator
        canvas.drawLine(40f, currentY + 6f, 555f, currentY + 6f, gridPaint)
        currentY += rowHeight
    }

    pdfDocument.finishPage(page)
    return pdfDocument
}
