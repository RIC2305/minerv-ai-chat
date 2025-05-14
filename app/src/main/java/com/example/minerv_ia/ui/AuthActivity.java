package com.example.minerv_ia.ui;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.minerv_ia.R;
import com.example.minerv_ia.databinding.ActivityAuthBinding;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;

/**
 * Actividad de autenticación que permite a los usuarios registrarse e iniciar sesión.
 * También integra Firebase Analytics para el seguimiento de eventos.
 */
public class AuthActivity extends AppCompatActivity {

    /** Enlace a la vista mediante View Binding. */
    private ActivityAuthBinding binding;

    /** Instancia de Firebase Analytics para el seguimiento de eventos. */
    private FirebaseAnalytics mFirebaseAnalytics;

    /** Almacena el correo del usuario autenticado. */
    private String usuarioAutenticado = "";

    /** Almacena la contraseña ingresada por el usuario. */
    private String password = "";

    /**
     * Método llamado al crear la actividad.
     * Inicializa la interfaz, la barra de herramientas y Firebase Analytics.
     *
     * @param savedInstanceState Estado guardado de la instancia anterior, si existe.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        // Configurar el Toolbar como la ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_auth);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");

        // Inicializar Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Registrar un evento de Firebase Analytics
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Evento 01");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Evento de Inicio de Minervia");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        // Iniciar configuración de autenticación
        setup();
    }

    /**
     * Configura los botones de registro, inicio de sesión y autenticación con Google.
     */
    private void setup() {
        // Configurar botón de registro
        binding.signUpButton.setOnClickListener(v -> {
            usuarioAutenticado = binding.emailEditText.getText().toString();
            password = binding.passwordEditText.getText().toString();

            if (!usuarioAutenticado.isEmpty() && !password.isEmpty()) {
                if (password.length() < 6) {
                    showAlert("La contraseña debe tener al menos 6 caracteres");
                    return;
                }

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(usuarioAutenticado, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showHomeActivity(usuarioAutenticado, password, true); // Nuevo usuario
                                Log.d("Auth", "Usuario registrado con éxito");
                            } else {
                                Log.e("Auth", "Error al registrar usuario", task.getException());
                                showAlert(Objects.requireNonNull(task.getException()).getMessage());
                            }
                        });
            } else {
                showAlert("Por favor, ingrese un correo electrónico y una contraseña");
            }
        });

        // Configurar botón de inicio de sesión
        binding.loginButton.setOnClickListener(v -> {
            usuarioAutenticado = binding.emailEditText.getText().toString();
            password = binding.passwordEditText.getText().toString();

            if (!usuarioAutenticado.isEmpty() && !password.isEmpty()) {
                if (password.length() < 6) {
                    showAlert("La contraseña debe tener al menos 6 caracteres");
                    return;
                }

                FirebaseAuth.getInstance().signInWithEmailAndPassword(usuarioAutenticado, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showHomeActivity(usuarioAutenticado, password, false); // Usuario existente
                                Log.d("Auth", "Inicio de sesión exitoso");
                            } else {
                                Log.e("Auth", "Error al iniciar sesión", task.getException());
                                showAlert(Objects.requireNonNull(task.getException()).getMessage());
                            }
                        });
            } else {
                showAlert("Por favor, ingrese un correo electrónico y una contraseña");
            }
        });

        // Configurar botón de Inicio de Sesión con Google
        binding.googleButton.setOnClickListener(v -> showAlert("Esta opción aún no está disponible."));
    }

    /**
     * Muestra un cuadro de diálogo de alerta con un mensaje específico.
     *
     * @param mensaje Mensaje a mostrar en el cuadro de diálogo.
     */
    private void showAlert(String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Inicia la actividad principal de la aplicación (HomeActivity) pasando los datos del usuario.
     *
     * @param usuario Correo del usuario autenticado.
     * @param password Contraseña del usuario.
     * @param esNuevoUsuario Indica si el usuario es nuevo o ya estaba registrado.
     */
    private void showHomeActivity(String usuario, String password, boolean esNuevoUsuario) {
        // Pruebas: Restablecer preferencias compartidas para mensajes
        SharedPreferences sharedPreferences = getSharedPreferences("HomePrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("mensajeMostrado", false).apply();

        // Crear intent para abrir la actividad principal
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("usuario", usuario);               // Pasa el nombre de usuario
        intent.putExtra("password", password);             // Pasa la contraseña
        intent.putExtra("esNuevoUsuario", esNuevoUsuario); // Se pasa el booleano
        Log.d("showHomeActivity", "Usuario: " + usuario + ", Nuevo Usuario: " + esNuevoUsuario);
        startActivity(intent);
    }
}
