package no1.share.to.clipboard;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import java.io.InputStream;
import java.io.IOException;

public class ShareActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                handleTextShare(intent);
            } else if (type.startsWith("image/") || type.startsWith("video/") || type.startsWith("audio/")) {
                handleMediaShare(intent);
            } else {
                handleGenericShare(intent);
            }
        }

        // Close the activity after processing
        finish();
    }
    
    private void handleMediaShare(Intent intent) {
        Uri mediaUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        if (mediaUri != null) {
            // Copy the actual media content (URI) to clipboard
            // This allows pasting the media into apps that support it
            copyMediaToClipboard(mediaUri, intent.getType());
            showToast("Media copied to clipboard");
        } else {
            showToast("No media found to copy");
        }
    }


    private void handleTextShare(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

        if (sharedText != null) {
            copyToClipboard("Shared Text", sharedText);
            showToast("Text copied to clipboard");
        } else if (sharedSubject != null) {
            copyToClipboard("Shared Subject", sharedSubject);
            showToast("Subject copied to clipboard");
        } else {
            showToast("No text found to copy");
        }
    }

    private void copyMediaToClipboard(Uri uri, String mimeType) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (mimeType != null && mimeType.startsWith("image/")) {
            // For images, load the bitmap and copy it to clipboard
            // This creates a persistent copy that won't disappear
            try {
                Bitmap bitmap = loadBitmapFromUri(uri);
                if (bitmap != null) {
                    ClipData clip = ClipData.newPlainText("image", "");
                    clipboard.setPrimaryClip(clip);

                    // Use the newer API to set bitmap if available
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        ClipData clipData = ClipData.newPlainText("", "");
                        ClipDescription clipDescription = new ClipDescription("image", new String[]{"image/*"});
                        clipboard.setPrimaryClip(new ClipData(clipDescription, new ClipData.Item(uri)));
                    }

                    // For better compatibility, save to MediaStore and copy that URI
                    String savedUri = MediaStore.Images.Media.insertImage(
                            getContentResolver(),
                            bitmap,
                            "clipboard_image",
                            "Image from clipboard"
                    );

                    if (savedUri != null) {
                        ClipData clip2 = ClipData.newUri(getContentResolver(), "Shared Media", Uri.parse(savedUri));
                        clipboard.setPrimaryClip(clip2);
                    }

                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Fallback: copy URI reference
        ClipData clip = ClipData.newUri(getContentResolver(), "Shared Media", uri);
        clipboard.setPrimaryClip(clip);
    }

    // ADD this helper method:
    private Bitmap loadBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleGenericShare(Intent intent) {
        // Try to get text first
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            copyToClipboard("Shared Content", sharedText);
            showToast("Content copied to clipboard");
            return;
        }

        // Try to get URI
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri != null) {
            copyToClipboard("Shared URI", uri.toString());
            showToast("URI copied to clipboard");
            return;
        }

        showToast("No shareable content found");
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
