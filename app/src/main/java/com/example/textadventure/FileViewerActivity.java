package com.example.textadventure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

public class FileViewerActivity extends Activity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textView = new TextView(this);
        textView.setText("文件查看器\n\n此功能可以查看和管理下载的文件");
        textView.setPadding(50, 50, 50, 50);
        textView.setTextSize(16);

        setContentView(textView);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri fileUri = intent.getData();
            showFileOptions(fileUri);
        }
    }

    private void showFileOptions(final Uri fileUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("文件操作");
        builder.setMessage("选择要执行的操作:");

        builder.setPositiveButton("打开文件", (dialog, which) -> openFile(fileUri));
        builder.setNeutralButton("分享文件", (dialog, which) -> shareFile(fileUri));
        builder.setNegativeButton("取消", (dialog, which) -> finish());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openFile(Uri fileUri) {
        try {
            Intent openIntent = new Intent(Intent.ACTION_VIEW);

            String mimeType = getMimeType(fileUri.toString());
            if (mimeType == null) {
                mimeType = "*/*";
            }

            openIntent.setDataAndType(fileUri, mimeType);
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(openIntent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开文件: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void shareFile(Uri fileUri) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            String mimeType = getMimeType(fileUri.toString());
            if (mimeType == null) {
                mimeType = "*/*";
            }

            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "分享文件"));
        } catch (Exception e) {
            Toast.makeText(this, "无法分享文件: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}