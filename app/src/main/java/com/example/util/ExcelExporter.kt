package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.R
import com.example.model.WarehouseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelExporter {

    private data class ImagePlacement(
        val relId: String,
        val fileName: String,
        val imageBytes: ByteArray,
        val colZeroBased: Int,
        val rowZeroBased: Int,
        val picId: Int,
        val picName: String
    )

    suspend fun exportToExcelWithEmbeddedImages(
        context: Context,
        items: List<WarehouseItem>,
        fileNamePrefix: String = "KPN_Warehouse_Data"
    ): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val excelFile = File(exportDir, "${fileNamePrefix}_$timeStamp.xlsx")

        val imagePlacements = mutableListOf<ImagePlacement>()
        var imageCounter = 0

        // Process images for each row
        // Row 0 is header, so data rows start at row 1 (0-based)
        items.forEachIndexed { itemIdx, item ->
            val excelRowZeroBased = itemIdx + 1 // Row 1, 2, 3...

            // Col 11 (L): Foto Kode Barang
            if (item.fotoKodeUrl.isNotBlank()) {
                val bytes = loadOptimizedImageBytes(context, item.fotoKodeUrl)
                if (bytes != null) {
                    imageCounter++
                    imagePlacements.add(
                        ImagePlacement(
                            relId = "rId$imageCounter",
                            fileName = "image$imageCounter.jpg",
                            imageBytes = bytes,
                            colZeroBased = 11,
                            rowZeroBased = excelRowZeroBased,
                            picId = imageCounter + 1,
                            picName = "FotoKode_${item.kodeBarang}"
                        )
                    )
                }
            }

            // Col 12 (M): Foto Barang
            if (item.fotoBarangUrl.isNotBlank()) {
                val bytes = loadOptimizedImageBytes(context, item.fotoBarangUrl)
                if (bytes != null) {
                    imageCounter++
                    imagePlacements.add(
                        ImagePlacement(
                            relId = "rId$imageCounter",
                            fileName = "image$imageCounter.jpg",
                            imageBytes = bytes,
                            colZeroBased = 12,
                            rowZeroBased = excelRowZeroBased,
                            picId = imageCounter + 1,
                            picName = "FotoBarang_${item.kodeBarang}"
                        )
                    )
                }
            }

            // Col 13 (N): Bukti Foto Kendaraan (if available)
            if (item.fotoKendaraanUrl.isNotBlank()) {
                val bytes = loadOptimizedImageBytes(context, item.fotoKendaraanUrl)
                if (bytes != null) {
                    imageCounter++
                    imagePlacements.add(
                        ImagePlacement(
                            relId = "rId$imageCounter",
                            fileName = "image$imageCounter.jpg",
                            imageBytes = bytes,
                            colZeroBased = 13,
                            rowZeroBased = excelRowZeroBased,
                            picId = imageCounter + 1,
                            picName = "FotoKendaraan_${item.nomorKendaraan}"
                        )
                    )
                }
            }

            // Col 14 (O): Foto Bukti Masalah (if available)
            if (item.fotoBuktiKeteranganUrls.isNotEmpty()) {
                val firstBuktiUrl = item.fotoBuktiKeteranganUrls.firstOrNull { it.isNotBlank() }
                if (firstBuktiUrl != null) {
                    val bytes = loadOptimizedImageBytes(context, firstBuktiUrl)
                    if (bytes != null) {
                        imageCounter++
                        imagePlacements.add(
                            ImagePlacement(
                                relId = "rId$imageCounter",
                                fileName = "image$imageCounter.jpg",
                                imageBytes = bytes,
                                colZeroBased = 14,
                                rowZeroBased = excelRowZeroBased,
                                picId = imageCounter + 1,
                                picName = "FotoBukti_${item.kodeBarang}"
                            )
                        )
                    }
                }
            }
        }

        // Build the XLSX zip archive
        FileOutputStream(excelFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                // 1. [Content_Types].xml
                writeZipString(zos, "[Content_Types].xml", buildContentTypesXml(imagePlacements.isNotEmpty()))

                // 2. _rels/.rels
                writeZipString(zos, "_rels/.rels", buildRootRelsXml())

                // 3. xl/_rels/workbook.xml.rels
                writeZipString(zos, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml())

                // 4. xl/workbook.xml
                writeZipString(zos, "xl/workbook.xml", buildWorkbookXml())

                // 5. xl/styles.xml
                writeZipString(zos, "xl/styles.xml", buildStylesXml())

                // 6. xl/worksheets/sheet1.xml
                writeZipString(zos, "xl/worksheets/sheet1.xml", buildSheet1Xml(items, imagePlacements.isNotEmpty()))

                // 7. If images exist, write drawings & media
                if (imagePlacements.isNotEmpty()) {
                    writeZipString(zos, "xl/worksheets/_rels/sheet1.xml.rels", buildSheetRelsXml())
                    writeZipString(zos, "xl/drawings/drawing1.xml", buildDrawingXml(imagePlacements))
                    writeZipString(zos, "xl/drawings/_rels/drawing1.xml.rels", buildDrawingRelsXml(imagePlacements))

                    // Write each image file into xl/media/
                    imagePlacements.forEach { placement ->
                        writeZipBytes(zos, "xl/media/${placement.fileName}", placement.imageBytes)
                    }
                }
                zos.finish()
            }
        }

        excelFile
    }

    private fun loadOptimizedImageBytes(context: Context, pathOrUri: String, maxDim: Int = 360): ByteArray? {
        if (pathOrUri.isBlank()) return null
        return try {
            val rawBytes: ByteArray? = when {
                pathOrUri.contains("drawable/kpn_logo") -> {
                    try {
                        context.resources.openRawResource(R.drawable.kpn_logo).use { it.readBytes() }
                    } catch (_: Exception) {
                        null
                    }
                }
                pathOrUri.startsWith("android.resource://") || pathOrUri.startsWith("content://") -> {
                    context.contentResolver.openInputStream(Uri.parse(pathOrUri))?.use { it.readBytes() }
                }
                pathOrUri.startsWith("/") -> {
                    val file = File(pathOrUri)
                    if (file.exists() && file.canRead()) file.readBytes() else null
                }
                pathOrUri.startsWith("http://") || pathOrUri.startsWith("https://") -> {
                    val conn = java.net.URL(pathOrUri).openConnection()
                    conn.connectTimeout = 3500
                    conn.readTimeout = 3500
                    conn.getInputStream().use { it.readBytes() }
                }
                else -> {
                    val file = File(pathOrUri)
                    if (file.exists() && file.canRead()) {
                        file.readBytes()
                    } else {
                        context.contentResolver.openInputStream(Uri.parse(pathOrUri))?.use { it.readBytes() }
                    }
                }
            }

            if (rawBytes == null || rawBytes.isEmpty()) return null

            // Downsample and compress to 85% JPEG for optimal Excel rendering and compact file size
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return rawBytes

            var inSampleSize = 1
            while (bounds.outWidth / inSampleSize > maxDim || bounds.outHeight / inSampleSize > maxDim) {
                inSampleSize *= 2
            }

            val opts = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val bmp = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, opts) ?: return rawBytes
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            bmp.recycle()
            baos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun writeZipString(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        val bytes = content.toByteArray(Charsets.UTF_8)
        zos.write(bytes, 0, bytes.size)
        zos.closeEntry()
    }

    private fun writeZipBytes(zos: ZipOutputStream, entryName: String, data: ByteArray) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        zos.write(data, 0, data.size)
        zos.closeEntry()
    }

    private fun buildContentTypesXml(hasImages: Boolean): String {
        val drawingOverride = if (hasImages) {
            """  <Override PartName="/xl/drawings/drawing1.xml" ContentType="application/vnd.openxmlformats-officedocument.drawing+xml"/>"""
        } else ""

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="jpeg" ContentType="image/jpeg"/>
  <Default Extension="jpg" ContentType="image/jpeg"/>
  <Default Extension="png" ContentType="image/png"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
$drawingOverride
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""
    }

    private fun buildRootRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
    }

    private fun buildWorkbookRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
    }

    private fun buildWorkbookXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Data Gudang KPN" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""
    }

    private fun buildStylesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="3">
    <font><sz val="11"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><name val="Calibri"/></font>
  </fonts>
  <fills count="3">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF0052CC"/></patternFill></fill>
  </fills>
  <borders count="2">
    <border><left/><right/><top/><bottom/></border>
    <border>
      <left style="thin"><color rgb="FFD4D4D8"/></left>
      <right style="thin"><color rgb="FFD4D4D8"/></right>
      <top style="thin"><color rgb="FFD4D4D8"/></top>
      <bottom style="thin"><color rgb="FFD4D4D8"/></bottom>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="4">
    <!-- 0: center data text -->
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center" wrapText="1"/>
    </xf>
    <!-- 1: header bold white with blue background -->
    <xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center" wrapText="1"/>
    </xf>
    <!-- 2: left aligned data text -->
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="left" vertical="center" wrapText="1"/>
    </xf>
    <!-- 3: bold center data text -->
    <xf numFmtId="0" fontId="2" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center" wrapText="1"/>
    </xf>
  </cellXfs>
</styleSheet>"""
    }

    private fun buildSheet1Xml(items: List<WarehouseItem>, hasImages: Boolean): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()

        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheetViews>
    <sheetView tabSelected="1" workbookViewId="0"/>
  </sheetViews>
  <sheetFormatPr defaultRowHeight="20"/>
  <cols>
    <col min="1" max="1" width="6" customWidth="1"/>
    <col min="2" max="2" width="18" customWidth="1"/>
    <col min="3" max="3" width="14" customWidth="1"/>
    <col min="4" max="4" width="10" customWidth="1"/>
    <col min="5" max="5" width="24" customWidth="1"/>
    <col min="6" max="6" width="16" customWidth="1"/>
    <col min="7" max="7" width="16" customWidth="1"/>
    <col min="8" max="8" width="28" customWidth="1"/>
    <col min="9" max="9" width="18" customWidth="1"/>
    <col min="10" max="10" width="20" customWidth="1"/>
    <col min="11" max="11" width="18" customWidth="1"/>
    <col min="12" max="12" width="22" customWidth="1"/>
    <col min="13" max="13" width="22" customWidth="1"/>
    <col min="14" max="14" width="22" customWidth="1"/>
    <col min="15" max="15" width="22" customWidth="1"/>
  </cols>
  <sheetData>
""")

        // Header Row (Row 1)
        val headers = listOf(
            "No",
            "Kode Barang (货物编码)",
            "Tanggal (日期)",
            "Jumlah (数量)",
            "Lokasi (位置)",
            "Nomor Kendaraan (车牌号码)",
            "Kondisi (状态)",
            "Keterangan (备注)",
            "Petugas Input (录入员)",
            "Waktu Input (录入时间)",
            "Status Sync (同步状态)",
            "Foto Kode Barang (货物编码照片)",
            "Foto Barang (货物照片)",
            "Bukti Foto Kendaraan (车辆照片凭证)",
            "Foto Bukti Masalah (情况证明照片)"
        )

        sb.append("""    <row r="1" ht="32" customHeight="1">""")
        headers.forEachIndexed { colIdx, header ->
            val colLetter = getColumnLetter(colIdx)
            sb.append("""<c r="${colLetter}1" t="inlineStr" s="1"><is><t>${xmlEscape(header)}</t></is></c>""")
        }
        sb.append("</row>\n")

        // Data Rows (Row 2, 3...)
        items.forEachIndexed { itemIdx, item ->
            val rowNum = itemIdx + 2 // Row 2, 3...
            val dateFormatted = dateFormat.format(Date(item.createdAt))
            val kondisiStr = "${item.kondisi.titleId} / ${item.kondisi.titleZh}"
            val syncStr = "${item.syncState.labelId} / ${item.syncState.labelZh}"

            sb.append("""    <row r="$rowNum" ht="78" customHeight="1">""")

            // Col A: No
            sb.append("""<c r="A$rowNum" t="inlineStr" s="0"><is><t>${itemIdx + 1}</t></is></c>""")
            // Col B: Kode Barang
            sb.append("""<c r="B$rowNum" t="inlineStr" s="3"><is><t>${xmlEscape(item.kodeBarang)}</t></is></c>""")
            // Col C: Tanggal
            sb.append("""<c r="C$rowNum" t="inlineStr" s="0"><is><t>${xmlEscape(item.tanggal)}</t></is></c>""")
            // Col D: Jumlah
            sb.append("""<c r="D$rowNum" t="inlineStr" s="0"><is><t>${item.jumlah}</t></is></c>""")
            // Col E: Lokasi
            sb.append("""<c r="E$rowNum" t="inlineStr" s="2"><is><t>${xmlEscape(item.lokasi)}</t></is></c>""")
            // Col F: Nomor Kendaraan
            sb.append("""<c r="F$rowNum" t="inlineStr" s="0"><is><t>${xmlEscape(item.nomorKendaraan)}</t></is></c>""")
            // Col G: Kondisi
            sb.append("""<c r="G$rowNum" t="inlineStr" s="0"><is><t>${xmlEscape(kondisiStr)}</t></is></c>""")
            // Col H: Keterangan
            sb.append("""<c r="H$rowNum" t="inlineStr" s="2"><is><t>${xmlEscape(item.keterangan.ifBlank { "-" })}</t></is></c>""")
            // Col I: Petugas Input
            sb.append("""<c r="I$rowNum" t="inlineStr" s="0"><is><t>${xmlEscape(item.createdBy)}</t></is></c>""")
            // Col J: Waktu Input
            sb.append("""<c r="J$rowNum" t="inlineStr" s="0"><is><t>${xmlEscape(dateFormatted)}</t></is></c>""")
            // Col K: Status Sync
            sb.append("""<c r="K$rowNum" t="inlineStr" s="0"><is><t>${xmlEscape(syncStr)}</t></is></c>""")

            // Col L: Foto Kode Barang (Image placeholder text)
            val hasFotoKode = item.fotoKodeUrl.isNotBlank()
            sb.append("""<c r="L$rowNum" t="inlineStr" s="0"><is><t>${if (hasFotoKode) "" else "-"}</t></is></c>""")

            // Col M: Foto Barang
            val hasFotoBarang = item.fotoBarangUrl.isNotBlank()
            sb.append("""<c r="M$rowNum" t="inlineStr" s="0"><is><t>${if (hasFotoBarang) "" else "-"}</t></is></c>""")

            // Col N: Bukti Foto Kendaraan
            val hasFotoKendaraan = item.fotoKendaraanUrl.isNotBlank()
            sb.append("""<c r="N$rowNum" t="inlineStr" s="0"><is><t>${if (hasFotoKendaraan) "" else "-"}</t></is></c>""")

            // Col O: Foto Bukti Masalah
            val hasFotoBukti = item.fotoBuktiKeteranganUrls.isNotEmpty()
            sb.append("""<c r="O$rowNum" t="inlineStr" s="0"><is><t>${if (hasFotoBukti) "" else "-"}</t></is></c>""")

            sb.append("</row>\n")
        }

        sb.append("  </sheetData>\n")
        if (hasImages) {
            sb.append("""  <drawing r:id="rId1"/>""" + "\n")
        }
        sb.append("</worksheet>")

        return sb.toString()
    }

    private fun buildSheetRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing" Target="../drawings/drawing1.xml"/>
</Relationships>"""
    }

    private fun buildDrawingXml(placements: List<ImagePlacement>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xdr:wsDr xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
""")

        placements.forEach { p ->
            sb.append("""  <xdr:twoCellAnchor editAs="oneCell">
    <xdr:from>
      <xdr:col>${p.colZeroBased}</xdr:col>
      <xdr:colOff>38100</xdr:colOff>
      <xdr:row>${p.rowZeroBased}</xdr:row>
      <xdr:rowOff>38100</xdr:rowOff>
    </xdr:from>
    <xdr:to>
      <xdr:col>${p.colZeroBased + 1}</xdr:col>
      <xdr:colOff>-38100</xdr:colOff>
      <xdr:row>${p.rowZeroBased + 1}</xdr:row>
      <xdr:rowOff>-38100</xdr:rowOff>
    </xdr:to>
    <xdr:pic>
      <xdr:nvPicPr>
        <xdr:cNvPr id="${p.picId}" name="${xmlEscape(p.picName)}"/>
        <xdr:cNvPicPr>
          <a:picLocks noChangeAspect="1"/>
        </xdr:cNvPicPr>
      </xdr:nvPicPr>
      <xdr:blipFill>
        <a:blip xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" r:embed="${p.relId}"/>
        <a:stretch>
          <a:fillRect/>
        </a:stretch>
      </xdr:blipFill>
      <xdr:spPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
        </a:xfrm>
        <a:prstGeom prst="rect">
          <a:avLst/>
        </a:prstGeom>
      </xdr:spPr>
    </xdr:pic>
    <xdr:clientData/>
  </xdr:twoCellAnchor>
""")
        }

        sb.append("</xdr:wsDr>")
        return sb.toString()
    }

    private fun buildDrawingRelsXml(placements: List<ImagePlacement>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
""")
        placements.forEach { p ->
            sb.append("""  <Relationship Id="${p.relId}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="../media/${p.fileName}"/>""" + "\n")
        }
        sb.append("</Relationships>")
        return sb.toString()
    }

    private fun getColumnLetter(colIndexZeroBased: Int): String {
        var n = colIndexZeroBased
        var result = ""
        while (n >= 0) {
            result = ('A'.code + (n % 26)).toChar() + result
            n = (n / 26) - 1
        }
        return result
    }

    private fun xmlEscape(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    fun shareExportedFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Export Data Gudang KPN Warehouse (.xlsx)")
            putExtra(
                Intent.EXTRA_TEXT,
                "Terlampir data ekspor gudang KPN Warehouse dalam format Excel (.xlsx) dengan foto fisik asli barang yang tertanam langsung di tabel (Powered by Andre)."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Buka / Bagikan File Excel (打开/分享 Excel)"))
    }
}
