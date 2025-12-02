package com.example.examapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {

    // Codice di richiesta per il permesso di archiviazione
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    // Array di permessi di archiviazione
    private static final String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Controlla se il permesso di ARCHIVIAZIONE è stato concesso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Il permesso non è stato concesso, richiedilo
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            // Il permesso è già stato concesso, avvia SignupActivity e chiudi SplashActivity
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // Gestisci il risultato della richiesta di permesso
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Il permesso di ARCHIVIAZIONE è stato concesso, avvia SignupActivity e chiudi SplashActivity
                Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Il permesso è stato negato, mostra un messaggio Toast e chiudi l'applicazione
                Toast.makeText(this, "Permesso negato", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
