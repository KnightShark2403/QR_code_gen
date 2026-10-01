package com.example.qr_code_gen;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;
    private ImageView qrImage;

    private Bitmap qrBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Connect XML components
        inputText = findViewById(R.id.inputText);
        qrImage = findViewById(R.id.qrImage);

        Button generateButton = findViewById(R.id.generateButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button shareButton = findViewById(R.id.shareButton);
        Button clearButton = findViewById(R.id.clearButton);

        // Generate QR
        generateButton.setOnClickListener(v -> generateQRCode());

        // Save QR
        saveButton.setOnClickListener(v -> saveQRCode());

        // Share QR
        shareButton.setOnClickListener(v -> shareQRCode());

        // Clear
        clearButton.setOnClickListener(v -> clearQRCode());
    }

    // =========================
    // GENERATE QR CODE
    // =========================

    private void generateQRCode() {

        String text = inputText.getText().toString().trim();

        if (text.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter text or a URL",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            MultiFormatWriter writer = new MultiFormatWriter();

            BitMatrix matrix = writer.encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    800,
                    800
            );

            int width = matrix.getWidth();
            int height = matrix.getHeight();

            qrBitmap = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.RGB_565
            );

            for (int x = 0; x < width; x++) {

                for (int y = 0; y < height; y++) {

                    qrBitmap.setPixel(
                            x,
                            y,
                            matrix.get(x, y)
                                    ? Color.BLACK
                                    : Color.WHITE
                    );
                }
            }

            qrImage.setImageBitmap(qrBitmap);

            Toast.makeText(
                    this,
                    "QR Code generated!",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (WriterException e) {

            Toast.makeText(
                    this,
                    "Error generating QR code",
                    Toast.LENGTH_SHORT
            ).show();

            e.printStackTrace();
        }
    }

    // =========================
    // SAVE QR CODE
    // =========================

    private void saveQRCode() {

        if (qrBitmap == null) {

            Toast.makeText(
                    this,
                    "Generate a QR code first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            ContentValues values = new ContentValues();

            values.put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "QR_Code_" + System.currentTimeMillis() + ".png"
            );

            values.put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/png"
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                values.put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES
                                + "/QR Generator"
                );
            }

            Uri imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            );

            if (imageUri != null) {

                OutputStream outputStream =
                        getContentResolver()
                                .openOutputStream(imageUri);

                qrBitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        outputStream
                );

                if (outputStream != null) {
                    outputStream.close();
                }

                Toast.makeText(
                        this,
                        "QR Code saved!",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Could not save QR code",
                    Toast.LENGTH_SHORT
            ).show();

            e.printStackTrace();
        }
    }

    // =========================
    // SHARE QR CODE
    // =========================

    private void shareQRCode() {

        if (qrBitmap == null) {

            Toast.makeText(
                    this,
                    "Generate a QR code first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            String path = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    qrBitmap,
                    "QR Code",
                    "Generated QR Code"
            );

            Uri imageUri = Uri.parse(path);

            Intent shareIntent =
                    new Intent(Intent.ACTION_SEND);

            shareIntent.setType("image/*");

            shareIntent.putExtra(
                    Intent.EXTRA_STREAM,
                    imageUri
            );

            startActivity(
                    Intent.createChooser(
                            shareIntent,
                            "Share QR Code"
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Could not share QR code",
                    Toast.LENGTH_SHORT
            ).show();

            e.printStackTrace();
        }
    }

    // =========================
    // CLEAR
    // =========================

    private void clearQRCode() {

        inputText.setText("");

        qrImage.setImageDrawable(null);

        qrBitmap = null;

        Toast.makeText(
                this,
                "Cleared",
                Toast.LENGTH_SHORT
        ).show();
    }
}