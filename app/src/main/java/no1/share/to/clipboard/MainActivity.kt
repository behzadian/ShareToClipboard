package no1.share.to.clipboard

import android.app.Activity
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private var tvClipboardContent: TextView? = null
    private var ivClipboardImage: ImageView? = null
    private var tvClipboardType: TextView? = null
    private var btnRefresh: Button? = null
    private var layoutClipboard: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        tvClipboardContent = findViewById<TextView?>(R.id.tvClipboardContent)
        ivClipboardImage = findViewById<ImageView?>(R.id.ivClipboardImage)
        tvClipboardType = findViewById<TextView?>(R.id.tvClipboardType)
        btnRefresh = findViewById<Button?>(R.id.btnRefresh)
        layoutClipboard = findViewById<LinearLayout?>(R.id.layoutClipboard)

        // Set up refresh button
        btnRefresh!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                displayClipboardContent()
            }
        })

        // Display current clipboard content
        displayClipboardContent()
    }

    override fun onResume() {
        super.onResume()
        // Refresh clipboard content when activity resumes
        displayClipboardContent()
    }

    private fun displayClipboardContent() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?

        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            showEmptyClipboard()
            return
        }

        val clipData = clipboard.getPrimaryClip()
        if (clipData == null || clipData.getItemCount() == 0) {
            showEmptyClipboard()
            return
        }

        val item = clipData.getItemAt(0)


        // Reset visibility
        tvClipboardContent!!.setVisibility(View.GONE)
        ivClipboardImage!!.setVisibility(View.GONE)
        layoutClipboard!!.setVisibility(View.VISIBLE)

        // Check for text
        val text = item.getText()
        if (text != null && text.length > 0) {
            tvClipboardType!!.setText("Clipboard Type: Text")
            tvClipboardContent!!.setText(text.toString())
            tvClipboardContent!!.setVisibility(View.VISIBLE)
            return
        }

        // Check for URI (could be image or other media)
        val uri = item.getUri()
        if (uri != null) {
            var mimeType: String? = "Unknown"
            try {
                val type = getContentResolver().getType(uri)
                if (type != null) {
                    mimeType = type
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            tvClipboardType!!.setText("Clipboard Type: " + mimeType)

            // Try to load as image
            if (mimeType!!.startsWith("image/")) {
                try {
                    val bitmap = loadBitmapFromUri(uri)
                    if (bitmap != null) {
                        ivClipboardImage!!.setImageBitmap(bitmap)
                        ivClipboardImage!!.setVisibility(View.VISIBLE)
                        tvClipboardContent!!.setText("URI: " + uri.toString())
                        tvClipboardContent!!.setVisibility(View.VISIBLE)
                        return
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Show URI as text
            tvClipboardContent!!.setText("URI: " + uri.toString())
            tvClipboardContent!!.setVisibility(View.VISIBLE)
            return
        }

        // Nothing found
        showEmptyClipboard()
    }

    private fun showEmptyClipboard() {
        layoutClipboard!!.setVisibility(View.VISIBLE)
        tvClipboardType!!.setText("Clipboard is empty")
        tvClipboardContent!!.setText("Share something using 'Copy to Clipboard' to see it here!")
        tvClipboardContent!!.setVisibility(View.VISIBLE)
        ivClipboardImage!!.setVisibility(View.GONE)
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        try {
            val inputStream = getContentResolver().openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (inputStream != null) {
                inputStream.close()
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}