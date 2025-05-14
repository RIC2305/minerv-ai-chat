package com.example.minerv_ia.ui;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.minerv_ia.R;
import com.example.minerv_ia.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

/**
 * Actividad principal (HomeActivity) que gestiona la interfaz después de la autenticación.
 * Permite la navegación entre diferentes tipos de IA e ir hacia la configuración del usuario.
 */
public class HomeActivity extends AppCompatActivity {

    /* Tipologías IA para manejo de datos */
    /* Tipo 1 : IA Amigo */
    /* Tipo 2 : IA Automatizaciones */
    /* Tipo 3 : IA Doctor */
    /* Tipo 4 : IA Abogado */
    /* Tipo 5 : IA Mecánico */
    /* Tipo n : IA añadidas con posterioridad */

    /** Enlace a la vista mediante View Binding. */
    private ActivityHomeBinding binding;

    /** Nombre del usuario autenticado. */
    private String usuarioAutenticado;

    /** Preferencias compartidas para persistencia de datos. */
    private SharedPreferences sharedPreferences;

    /** Recursos de imagen para las IA Profesionales. */
    private final int[] recursosImagenesIAProfesional = {
            R.mipmap.avatar_doctor_round,
            R.mipmap.avatar_abogado_round,
            R.mipmap.avatar_mecanico_round
    };

    /** Recursos de texto para las IA Profesionales. */
    private final int[] recursosTextoIAProfesional = {
            R.string.seleccion_ia_tipo_profesional_medico,
            R.string.seleccion_ia_tipo_profesional_abogado,
            R.string.seleccion_ia_tipo_profesional_mecanico
    };

    /** Indice actual para los arrays de imagen y texto de IA Profesional. */
    private int indice = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("HomePrefs", Context.MODE_PRIVATE);

        // Recupera el nombre del usuario autenticado
        usuarioAutenticado = getIntent().getStringExtra("usuario");
        boolean esNuevoUsuario = getIntent().getBooleanExtra("esNuevoUsuario", false);
        boolean mensajeMostrado = sharedPreferences.getBoolean("mensajeMostrado", false);

        Log.d("HomeActivity", "Usuario autenticado: " + usuarioAutenticado);
        Log.d("HomeActivity", "esNuevoUsuario: " + esNuevoUsuario);
        Log.d("HomeActivity", "mensajeMostrado: " + mensajeMostrado);

        // Muestra un mensaje de bienvenida si aplica
        if (esNuevoUsuario && !mensajeMostrado) {
            Toast.makeText(this, "¡Bienvenido! Configura tu perfil.", Toast.LENGTH_LONG).show();
        } else if (!esNuevoUsuario && !mensajeMostrado) {
            Toast.makeText(this, "¡Me alegra que estés de vuelta!", Toast.LENGTH_LONG).show();
        }
        sharedPreferences.edit().putBoolean("mensajeMostrado", true).apply();

        // Configuración de la interfaz
        Toolbar toolbar = findViewById(R.id.toolbar_auth);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Chats");

        // Configuración de botones y eventos
        configurarInterfaz();
    }

    /**
     * Configura los botones de la interfaz y sus eventos correspondientes.
     */
    private void configurarInterfaz() {
        Log.d("HomeActivity", "Configurando interfaz de usuario...");

        ImageButton irAConfiguracionButton = findViewById(R.id.btnIrAConfigActivity);
        ImageButton botonImagenIAProfesional = findViewById(R.id.btnProfesionIA);
        TextView textoIAProfesional = findViewById(R.id.txtDescripcionProfesionIA);
        ImageButton botonFlechaIzquierda = findViewById(R.id.btnFlechaIzquierda);
        ImageButton botonFlechaDerecha = findViewById(R.id.btnFlechaDerecha);
        ImageButton botonImagenIAAmigo = findViewById(R.id.btnAmigoIA);
        ImageButton botonImagenIAAutomata = findViewById(R.id.btnAutomataIA);
        Button cerrarSesionButton = findViewById(R.id.btnCerrarSesion);

        actualizarImagenTextoIaProfesional(botonImagenIAProfesional, textoIAProfesional, indice);

        irAConfiguracionButton.setOnClickListener(v -> irAConfiguracion());
        botonFlechaIzquierda.setOnClickListener(v -> cambiarIAProfesional(-1, botonImagenIAProfesional, textoIAProfesional));
        botonFlechaDerecha.setOnClickListener(v -> cambiarIAProfesional(1, botonImagenIAProfesional, textoIAProfesional));
        botonImagenIAProfesional.setOnClickListener(v -> irAChatIA(indice + 3));
        botonImagenIAAmigo.setOnClickListener(v -> irAChatIA(1));
        botonImagenIAAutomata.setOnClickListener(v -> irAChatIA(2));
        cerrarSesionButton.setOnClickListener(v -> cerrarSesion());

        Log.d("HomeActivity", "Interfaz configurada correctamente.");
    }

    /**
     * Cierra la sesión del usuario y lo redirige a la pantalla de autenticación.
     */
    private void cerrarSesion() {
        Log.d("HomeActivity", "Cerrando sesión...");

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Log.d("HomeActivity", "Sesión cerrada correctamente.");
    }

    /**
     * Redirige al usuario a la pantalla de configuración.
     */
    private void irAConfiguracion() {
        Intent intent = new Intent(this, UserConfigActivity.class);
        intent.putExtra("usuario", usuarioAutenticado);
        startActivity(intent);
    }

    /**
     * Cambia la imagen y descripción de la IA Profesional.
     *
     * @param cambio Valor que determina si avanza o retrocede en la lista.
     * @param botonIaProfesional Botón de imagen de la IA Profesional.
     * @param textoIaProfesional Texto descriptivo de la IA Profesional.
     */
    private void cambiarIAProfesional(int cambio, ImageButton botonIaProfesional, TextView textoIaProfesional) {
        Log.d("HomeActivity", "Cambiando IA Profesional. Cambio: " + cambio);

        indice = (indice + cambio + recursosImagenesIAProfesional.length) % recursosImagenesIAProfesional.length;
        actualizarImagenTextoIaProfesional(botonIaProfesional, textoIaProfesional, indice);
    }

    /**
     * Actualiza la imagen y el texto de la IA Profesional seleccionada.
     *
     * @param botonIaProfesional Botón de imagen de la IA Profesional.
     * @param textoIaProfesional Texto descriptivo de la IA Profesional.
     * @param indice Indice actual de la IA Profesional.
     */
    private void actualizarImagenTextoIaProfesional(ImageButton botonIaProfesional, TextView textoIaProfesional, int indice) {
        Log.d("HomeActivity", "Actualizando imagen y texto de IA Profesional. Indice: " + indice);

        botonIaProfesional.setImageResource(recursosImagenesIAProfesional[indice]);
        botonIaProfesional.setContentDescription(getString(recursosTextoIAProfesional[indice]));
        textoIaProfesional.setText(recursosTextoIAProfesional[indice]);
    }

    /**
     * Redirige al usuario a la pantalla de chat con el tipo de IA seleccionado.
     *
     * @param tipoIA Tipo de IA con la que el usuario desea interactuar.
     */
    private void irAChatIA(int tipoIA) {
        Log.d("HomeActivity", "Redirigiendo a chat con IA de tipo: " + tipoIA);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("tipoIA", tipoIA);
        intent.putExtra("usuario", usuarioAutenticado);
        startActivity(intent);
    }
}