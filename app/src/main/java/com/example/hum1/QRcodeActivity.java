package com.example.hum1;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;


/**
 * Активити для генерации и отображения QR-кода по переданному тексту.
 * Получает строку из Intent и создает QR-код, который отображается в ImageView.
 */
public class QRcodeActivity extends AppCompatActivity {
    ImageView qrCodeIV;

    /**
     * Вызывается при создании активности.
     * Инициализирует интерфейс, получает строку из Intent и запускает генерацию QR-кода.
     *
     * @param savedInstanceState сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qrcode);
        qrCodeIV = findViewById(R.id.idIVQrcode);
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String id = bundle.getString("id");
        generateQRCode(id);
    }

    /**
     * Генерирует QR-код из переданного текста и отображает его в ImageView.
     *
     * @param text строка, которая будет закодирована в QR-код
     */
    void generateQRCode(String text)
    {
        BarcodeEncoder barcodeEncoder
                = new BarcodeEncoder();
        try {

            Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeIV.setImageBitmap(bitmap);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
    }
}