package com.ronen.taz;

import static android.content.Intent.getIntent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ImageActivity extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = findViewById(R.id.imageView);

        String password = getIntent().getStringExtra("password");
        try {
            byte[] decryptedBytes = decryptImage(password);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.length);
            if (bitmap.getWidth() > bitmap.getHeight()) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                imageView.setImageBitmap(rotatedBitmap);
                bitmap.recycle();
            } else {
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Wrong password or decryption failed", Toast.LENGTH_LONG).show();
            Log.e("ADADA", e.getMessage());
            finish();
        }
    }

    public byte[] readRawResourceAsBytes(int rawResId) throws IOException {
        InputStream is = this.getResources().openRawResource(rawResId);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } finally {
            is.close();
        }
    }


    private byte[] decryptImage(String password) throws Exception {
        byte[] fileBytes = readRawResourceAsBytes(R.raw.image);

        byte[] keyBytes = new byte[16];
        byte[] passwordBytes = Arrays.copyOf(password.getBytes(StandardCharsets.UTF_8), 16);
        int length = Math.min(passwordBytes.length, keyBytes.length);
        System.arraycopy(passwordBytes, 0, keyBytes, 0, length);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decryptedBytes = cipher.doFinal(fileBytes);
        return decryptedBytes;
    }
}
