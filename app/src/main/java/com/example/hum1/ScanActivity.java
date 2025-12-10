package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;

import com.example.hum1.views.ViewApplicQR;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Активити для сканирования QR-кодов с помощью камеры устройства.
 * Использует библиотеку JourneyApps для считывания QR-кода.
 * После успешного сканирования запускает экран просмотра заявки с переданным идентификатором.
 */
public class ScanActivity extends AppCompatActivity {

    /**
     * Запускатель активности сканера QR-кодов.
     * Обрабатывает результат сканирования: если сканирование отменено,
     * показывает Toast с сообщением, иначе запускает активити просмотра заявки.
     */
    private final ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() == null) {
            Toast.makeText(this,
                    getString(R.string.scan_cancelled),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), ViewApplicQR.class);
            intent.putExtra("id", result.getContents());
            startActivity(intent);
            finish();
        }
    });

    /**
     * Создает активити, настраивает полноэкранный режим Edge-to-Edge,
     * скрывает ActionBar и запускает сканирование QR-кода.
     *
     * @param savedInstanceState сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Наведите камеру на QR-код");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);

        qrLauncher.launch(options);
    }
}
