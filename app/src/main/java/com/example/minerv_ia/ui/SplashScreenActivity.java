package com.example.minerv_ia.ui;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.example.minerv_ia.R;


/**
 * Actividad que muestra una pantalla de presentación (splash screen) antes de iniciar la aplicación.
 * Se muestra durante 2 segundos antes de redirigir a la actividad de autenticación.
 */
public class SplashScreenActivity extends AppCompatActivity {


    /**
     * Método llamado al crear la actividad.
     *
     * @param savedInstanceState Estado guardado de la instancia anterior, si existe.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_splash_screen);

        // Hilo para mostrar la pantalla de presentación durante 2 segundos
        Thread myThread = new Thread(() -> {
            try {
                Thread.sleep(2000); // Pausa de 2 segundos antes de iniciar la actividad

                // Crear intent para iniciar la actividad de autenticación
                Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
                startActivity(intent); // Inicia la actividad principal

                finish(); // Finaliza la actividad de splash para que no vuelva atrás
            } catch (InterruptedException e) {
                e.printStackTrace(); // Manejo de excepción en caso de interrupción del hilo
            }
        });

        myThread.start(); // Inicia el hilo
    }
}
