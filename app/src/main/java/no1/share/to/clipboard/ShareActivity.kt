package no1.share.to.clipboard

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ShareActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = getIntent()
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if (type.startsWith("text/")) {
                handleTextShare(intent)
            } else if (type.startsWith("image/") || type.startsWith("video/") || type.startsWith("audio/")) {
                handleMediaShare(intent)
            } else {
                handleGenericShare(intent)
            }
        }

        // Close the activity after processing
        finish()
    }

    private fun handleMediaShare(intent: Intent) {
        val mediaUri = intent.parcelable<Uri>(Intent.EXTRA_STREAM)

        if (mediaUri != null) {
            // Copy the actual media content (URI) to clipboard
            // This allows pasting the media into apps that support it
            copyMediaToClipboard(mediaUri, intent.type)
            showToast("Media copied to clipboard")
        } else {
            showToast("No media found to copy")
        }
    }


    private fun handleTextShare(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

        if (sharedText != null) {
            copyToClipboard("Shared Text", sharedText)
            showToast("Text copied to clipboard")
        } else if (sharedSubject != null) {
            copyToClipboard("Shared Subject", sharedSubject)
            showToast("Subject copied to clipboard")
        } else {
            showToast("No text found to copy")
        }
    }

    private fun copyMediaToClipboard(uri: Uri, mimeType: String?) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        if (mimeType != null && mimeType.startsWith("image/")) {
            // For images, load the bitmap and save it to our internal cache
            // This allows us to share a URI that won't expire and won't show in Gallery
            try {
                val bitmap = loadBitmapFromUri(uri)
                if (bitmap != null) {
                    val savedUri = saveBitmapToCache(bitmap)
                    if (savedUri != null) {
                        val clip = ClipData.newUri(contentResolver, "Shared Image", savedUri)
                        clipboard.setPrimaryClip(clip)
                        return
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback: copy URI reference directly
        val clip = ClipData.newUri(contentResolver, "Shared Media", uri)
        clipboard.setPrimaryClip(clip)
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(cacheDir, "shared")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_image.png")
            if (file.exists()) {
                file.delete()
            }
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            FileProvider.getUriForFile(this, "$packageName.file_provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // ADD this helper method:
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun handleGenericShare(intent: Intent) {
        // Try to get text first
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            copyToClipboard("Shared Content", sharedText)
            showToast("Content copied to clipboard")
            return
        }

        // Try to get URI
        val uri = intent.parcelable<Uri>(Intent.EXTRA_STREAM)
        if (uri != null) {
            copyToClipboard("Shared URI", uri.toString())
            showToast("URI copied to clipboard")
            return
        }

        showToast("No shareable content found")
    }

    private fun copyToClipboard(label: String?, text: String?) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
