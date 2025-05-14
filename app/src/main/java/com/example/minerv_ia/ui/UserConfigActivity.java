package com.example.minerv_ia.ui;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.minerv_ia.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Actividad de configuración del usuario y su IA asociada.
 * Permite modificar datos personales, seleccionar preferencias y almacenar información en Firestore.
 */
public class UserConfigActivity extends AppCompatActivity {

    //------------DATOS USUARIO----------------------------
    /** Nombre del usuario autenticado. */
    String usuarioAutenticado = "";
    /** Nombre de pila del usuario. */
    String nombrePilaUsuario = "";
    /** Ciudad de residencia del usuario. */
    String ciudadUsuario = "";

    /** Enumeración para representar el género del usuario. */
    enum GeneroUsuario {Masculino, Femenino, Otro}

    /** Edad del usuario. */
    int edadUsuario = 0;
    /** Descripción sobre el usuario. */
    String acercaUsuario = "";

    //------------DATOS IA----------------------------
    /** Nombre de la inteligencia artificial (IA). */
    String nombreIA = "";
    /** Edad de la inteligencia artificial (IA). */
    int edadIA = 0;

    /**
     * Enumeración para definir el estilo de comunicación de la IA.
     */
    public enum estiloIA {
        AMIGABLE,       // Conversación cálida y empática
        PROFESIONAL,    // Formal y directo
        DIVERTIDA,      // Humor y respuestas entretenidas
        MOTIVADORA,     // Anima y da consejos positivos
        SERIA,          // Concisa y sin rodeos
        FILOSOFICA,     // Reflexiva y con profundidad
        CIENTIFICA,     // Precisa y basada en datos
        CREATIVA,       // Estilo artístico e imaginativo
        SARCASTICA,     // Respuestas con ironía y humor seco
        NEUTRA          // Objetiva y equilibrada
    }

    /** Descripción sobre la IA. */
    String acercaIA = "";

    // Componentes UI
    EditText nombrePilaUsuarioEditText, ciudadUsuarioEditText, acercaUsuarioEditText, nombreIAEditText,
            acercaIAEditText;
    Spinner sexoUsuarioSpinner, edadUsuarioSpinner, edadIASpinner, estiloIASpinner;
    Button salirButton, guardarButton, borrarHistorialButton;
    ImageButton ayudaButton;

    /** Base de datos Firestore para almacenar datos del usuario. */
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Variable booleana para controlar si se ha realizado algún guardado. */
    boolean datosGuardados = false;

    /** Preferencias compartidas para la configuración de la aplicación. */
    private SharedPreferences sharedPreferences;

    /**
     * Método llamado al crear la actividad. Inicializa la UI y recupera los datos del usuario.
     *
     * @param savedInstanceState Estado de la instancia anterior, si existe.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_config);

        // Recupera los datos del Intent dentro del onCreate
        usuarioAutenticado = getIntent().getStringExtra("usuario");

        // Configuración de SharedPreferences para evitar repetir mensaje de bienvenida
        sharedPreferences = getSharedPreferences("HomePrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("mensajeMostrado", true).apply();

        // Configurar el Toolbar como la ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar_config);
        setSupportActionBar(toolbar);

        // Establece el título de la barra de acción con el usuario autenticado
        Objects.requireNonNull(getSupportActionBar()).setTitle("user: " + usuarioAutenticado);

        // Asignación de componentes
        nombrePilaUsuarioEditText = findViewById(R.id.nombrePilaUsuarioEditText);
        ciudadUsuarioEditText = findViewById(R.id.ciudadUsuarioEditText);
        edadUsuarioSpinner = findViewById(R.id.edadUsuarioSpinner);
        sexoUsuarioSpinner = findViewById(R.id.sexoUsuarioSpinner);
        acercaUsuarioEditText = findViewById(R.id.acercaUsuarioEditText);

        nombreIAEditText = findViewById(R.id.nombreIAEditText);
        edadIASpinner = findViewById(R.id.edadIASpinner);
        estiloIASpinner = findViewById(R.id.estiloIASpinner);
        acercaIAEditText = findViewById(R.id.acercaIAEditText);

        salirButton = findViewById(R.id.salirButton);
        guardarButton = findViewById(R.id.guardarButton);
        ayudaButton = findViewById(R.id.ayudaButton);
        borrarHistorialButton = findViewById(R.id.borrarHistorialButton);

        // Configurar Spinners
        configurarSpinner();

        // Cargar datos del usuario desde la base de datos si existen
        cargarDatos();

        // En versión 1.0 no se permite elegir edad ni nombre de la IA
        nombreIAEditText.setText("Pedro");
        nombreIAEditText.setFocusable(false);
        nombreIAEditText.setEnabled(false); // Bloquea edición del nombre IA

        // Establecer la edad de la IA en 40 y deshabilitar cambios
        edadIASpinner.setSelection(40 - 16); // Ajusta el índice para que esté fijada en 40 años
        edadIASpinner.setEnabled(false); // Bloquea cambios
        edadIASpinner.setClickable(false);

        // Configurar funcionalidad de botones
        ayudaButton.setOnClickListener(v -> mostrarAyuda());
        salirButton.setOnClickListener(v -> salirAHome());
        guardarButton.setOnClickListener(v -> guardarDatos());
        borrarHistorialButton.setOnClickListener(v -> borrarHistorialAmigo());
    }




    /**
     * Configura los spinners de la interfaz de usuario para seleccionar el género, edad y estilo de la IA.
     * Este método establece los adaptadores para tres spinners: uno para seleccionar el género de usuario,
     * otro para seleccionar la edad (entre 16 y 100 años) y otro para seleccionar el estilo de la IA.
     */
    public void configurarSpinner() {
        // Adaptador para el Spinner de género
        ArrayAdapter<GeneroUsuario> generoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, GeneroUsuario.values());
        generoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexoUsuarioSpinner.setAdapter(generoAdapter);

        // Adaptador para el Spinner de edad (16 a 100 años)
        List<Integer> edades = new ArrayList<>();
        for (int i = 16; i <= 100; i++) edades.add(i);
        ArrayAdapter<Integer> edadAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, edades);
        edadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edadUsuarioSpinner.setAdapter(edadAdapter);
        edadIASpinner.setAdapter(edadAdapter);  // Usa el mismo adaptador para la edad de la IA

        // Adaptador para el Spinner de estilo de IA
        ArrayAdapter<estiloIA> estiloAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, estiloIA.values());
        estiloAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        estiloIASpinner.setAdapter(estiloAdapter);
    }

    /**
     * Muestra un cuadro de diálogo de ayuda que explica cómo configurar el perfil del usuario y la IA.
     * El diálogo contiene instrucciones detalladas para completar los campos del formulario de configuración del usuario
     * y las opciones de configuración de la IA.
     */
    public void mostrarAyuda() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ayuda - Configuración del Usuario")
                .setMessage("CONSULTA AYUDA COMPLETA AQUÍ: \n\n" +
                        "https://drive.google.com/file/d/122tUOAIk6OiYwn2ozVAo0bzyNNN2X4cf/view \n\n" +
                        "En este formulario puedes configurar tu perfil y el de la IA:\n\n" +
                        "🟢 *Nombre:* Introduce tu nombre de pila.\n" +
                        "🟢 *Ciudad:* Ingresa tu ciudad de residencia.\n" +
                        "🟢 *Edad:* Selecciona tu edad en el menú desplegable.\n" +
                        "🟢 *Género:* Escoge tu género.\n" +
                        "🟢 *Acerca de ti:* Describe brevemente quién eres.\n\n" +
                        "👾 *Configuración de la IA:*\n" +
                        "🟣 *Nombre IA:* Define un nombre para la IA.\n" +
                        "🟣 *Edad IA:* Selecciona la edad de la IA.\n" +
                        "🟣 *Estilo IA:* Elige la personalidad de la IA.\n" +
                        "🟣 *Acerca de IA:* Describe cómo quieres que actúe la IA.\n\n" +
                        "Cuando termines, presiona *Guardar* para aplicar los cambios.")
                .setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Abrir Enlace", (dialog, which) -> {
                    String url = "https://drive.google.com/file/d/122tUOAIk6OiYwn2ozVAo0bzyNNN2X4cf/view";

                    // Crear el Intent para abrir la URL en un navegador
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    // Verifica que haya una actividad que pueda manejar el Intent
                    if (browserIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(this, "No hay un navegador disponible para abrir el enlace.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * Maneja el comportamiento del botón "Salir". Si el usuario tiene cambios sin guardar, muestra un cuadro de diálogo de confirmación.
     * Si el usuario elige "Salir", los datos se envían a la actividad principal (HomeActivity) y la actividad actual (UserConfigActivity) se finaliza.
     * Si los datos están guardados, simplemente se redirige a la actividad principal sin mostrar el cuadro de diálogo.
     */
    private void salirAHome() {
        if (!datosGuardados) {
            new AlertDialog.Builder(this)
                    .setTitle("Salir sin guardar")
                    .setMessage("Tienes cambios sin guardar. ¿Seguro que quieres salir?")
                    .setPositiveButton("Salir", (dialog, which) -> {
                        Intent intent = new Intent(UserConfigActivity.this, HomeActivity.class);
                        intent.putExtra("usuario", usuarioAutenticado);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            Intent intent = new Intent(UserConfigActivity.this, HomeActivity.class);
            intent.putExtra("usuario", usuarioAutenticado);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Maneja el comportamiento del botón "Guardar". Este método obtiene los datos del formulario de configuración de usuario y la IA,
     * los valida y luego los guarda en la base de datos Firestore bajo el identificador del usuario autenticado.
     * Los datos de usuario incluyen: nombre, ciudad, edad, género, y una descripción.
     * Los datos de la IA incluyen: nombre, edad, estilo y una descripción.
     * Si los datos se guardan correctamente, se muestra un mensaje de éxito, de lo contrario, un mensaje de error.
     */
    private void guardarDatos() {
        // Obtener los datos de los EditTexts (si están vacíos, se asigna cadena vacía)
        nombrePilaUsuario = nombrePilaUsuarioEditText.getText().toString().isEmpty() ? "" : nombrePilaUsuarioEditText.getText().toString();
        ciudadUsuario = ciudadUsuarioEditText.getText().toString().isEmpty() ? "" : ciudadUsuarioEditText.getText().toString();
        acercaUsuario = acercaUsuarioEditText.getText().toString().isEmpty() ? "" : acercaUsuarioEditText.getText().toString();

        // Obtener la edad del spinner de edad del usuario (manteniendo como Integer)
        edadUsuario = (Integer) edadUsuarioSpinner.getSelectedItem();  // Aquí seguimos con el tipo Integer sin conversión extra

        // Obtener el género seleccionado en el spinner (manteniendo como String)
        GeneroUsuario generoUsuario = (GeneroUsuario) sexoUsuarioSpinner.getSelectedItem();  // Esto se mantiene como Enum

        // Obtener los datos de la IA
        nombreIA = nombreIAEditText.getText().toString().isEmpty() ? "" : nombreIAEditText.getText().toString();
        acercaIA = acercaIAEditText.getText().toString().isEmpty() ? "" : acercaIAEditText.getText().toString();

        // Obtener la edad de la IA desde el spinner (manteniendo como Integer)
        edadIA = (Integer) edadIASpinner.getSelectedItem();  // Similar al anterior

        // Obtener el estilo de IA seleccionado en el spinner (manteniendo como Enum)
        estiloIA estilo = (estiloIA) estiloIASpinner.getSelectedItem();  // Lo mantenemos como Enum

        // Log para verificar los datos antes de hacer el push a Firestore
        Log.d("UserConfig", "Nombre Usuario: " + nombrePilaUsuario);
        Log.d("UserConfig", "Ciudad Usuario: " + ciudadUsuario);
        Log.d("UserConfig", "Edad Usuario: " + edadUsuario);
        Log.d("UserConfig", "Género Usuario: " + generoUsuario);
        Log.d("UserConfig", "Acerca del Usuario: " + acercaUsuario);

        Log.d("UserConfig", "Nombre IA: " + nombreIA);
        Log.d("UserConfig", "Edad IA: " + edadIA);
        Log.d("UserConfig", "Estilo IA: " + estilo);
        Log.d("UserConfig", "Acerca de la IA: " + acercaIA);

        // Crear un mapa de los campos que queremos guardar en Firestore
        Map<String, Object> usuarioData = new HashMap<>();
        usuarioData.put("nombrePila", nombrePilaUsuario);
        usuarioData.put("ciudad", ciudadUsuario);
        usuarioData.put("edad", edadUsuario);  // Guardamos como Integer
        usuarioData.put("genero", generoUsuario.toString());  // Guardamos como String
        usuarioData.put("acercaUsuario", acercaUsuario);

        usuarioData.put("nombreIA", nombreIA);
        usuarioData.put("edadIA", edadIA);  // Guardamos como Integer
        usuarioData.put("estiloIA", estilo.toString());  // Guardamos como String
        usuarioData.put("acercaIA", acercaIA);

        // Usar el identificador del usuario autenticado para crear el documento
        DocumentReference usuarioRef = db.collection("usuarios").document(usuarioAutenticado);

        // Guardar los datos en Firestore
        usuarioRef.set(usuarioData, SetOptions.merge())  // merge para no sobrescribir campos no proporcionados
                .addOnSuccessListener(aVoid -> {
                    // Si la operación es exitosa, mostrar un mensaje
                    Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    datosGuardados = true;
                })
                .addOnFailureListener(e -> {
                    // Si ocurre un error, mostrar un mensaje
                    Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                    Log.e("UserConfig", "Error al guardar los datos: ", e); // Log del error
                });
    }

    /**
     * Carga los datos del usuario desde la base de datos (Firestore) y los muestra en la interfaz de usuario.
     * Este método obtiene la información del usuario autenticado, incluyendo su nombre, ciudad, género, edad,
     * estilo de la IA, y otros detalles relacionados, y los asigna a los campos correspondientes en la interfaz.
     * También maneja situaciones donde los datos no se encuentran o no son válidos.
     */
    private void cargarDatos() {
        // Referencia al documento del usuario en Firestore usando el ID del usuario autenticado
        DocumentReference usuarioRef = db.collection("usuarios").document(usuarioAutenticado);

        // Recupera los datos del documento de Firestore
        usuarioRef.get().addOnSuccessListener(documentSnapshot -> {
            // Si el documento existe, se procede a asignar los datos
            if (documentSnapshot.exists()) {
                // Obtener y asignar los datos del usuario (nombre, ciudad, acerca de)
                nombrePilaUsuario = documentSnapshot.getString("nombrePila");
                ciudadUsuario = documentSnapshot.getString("ciudad");
                acercaUsuario = documentSnapshot.getString("acercaUsuario");

                // Establecer los valores en los campos de texto de la interfaz
                nombrePilaUsuarioEditText.setText(nombrePilaUsuario);
                ciudadUsuarioEditText.setText(ciudadUsuario);
                acercaUsuarioEditText.setText(acercaUsuario);

                // Obtener y asignar la edad del usuario, verificando que el dato no sea nulo
                Long edadUsuarioLong = documentSnapshot.getLong("edad");
                if (edadUsuarioLong != null) {
                    edadUsuario = edadUsuarioLong.intValue();  // Convertir a Integer
                    edadUsuarioSpinner.setSelection(edadUsuario - 16); // Ajustar el índice del spinner
                }

                // Obtener y asignar el género del usuario desde el documento
                String generoStr = documentSnapshot.getString("genero");
                if (generoStr != null) {
                    try {
                        // Intentar convertir el String a un valor de Enum GeneroUsuario
                        GeneroUsuario genero = GeneroUsuario.valueOf(generoStr);
                        sexoUsuarioSpinner.setSelection(genero.ordinal()); // Establecer el índice en el spinner
                    } catch (IllegalArgumentException e) {
                        // Si ocurre un error al convertir el String a Enum, se registra el error
                        Log.e("UserConfig", "Género no válido en Firestore: " + generoStr);
                    }
                }

                // Obtener y asignar el estilo de la IA desde el documento
                String estiloIAStr = documentSnapshot.getString("estiloIA");
                if (estiloIAStr != null) {
                    try {
                        // Intentar convertir el String a un valor de Enum estiloIA
                        estiloIA estilo = estiloIA.valueOf(estiloIAStr);
                        estiloIASpinner.setSelection(estilo.ordinal()); // Establecer el índice en el spinner
                    } catch (IllegalArgumentException e) {
                        // Si ocurre un error al convertir el String a Enum, se registra el error
                        Log.e("UserConfig", "Estilo de IA no válido en Firestore: " + estiloIAStr);
                    }
                }

                // Obtener y asignar "Acerca de la IA"
                acercaIA = documentSnapshot.getString("acercaIA");
                acercaIAEditText.setText(acercaIA);
            } else {
                Log.d("UserConfig", "No se encontraron datos para el usuario.");
            }
        }).addOnFailureListener(e -> {
            Log.e("UserConfig", "Error al cargar datos desde Firestore", e);
            Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Muestra un cuadro de diálogo de confirmación para borrar el historial de un amigo.
     * Si el usuario confirma, se elimina el historial del usuario autenticado.
     */
    private void borrarHistorialAmigo() {
        // Crear un AlertDialog para confirmar el borrado
        new AlertDialog.Builder(this)
                .setTitle("Confirmar borrado")
                .setMessage("¿Estás seguro de que deseas borrar el historial?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Acción al confirmar el borrado
                    Toast.makeText(getApplicationContext(), "Historial borrado con éxito", Toast.LENGTH_SHORT).show();
                    borrarArchivoUsuarioAutenticado(usuarioAutenticado);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Cerrar el diálogo sin hacer nada
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Elimina el archivo de historial del usuario autenticado desde el almacenamiento interno.
     * El archivo tiene un nombre basado en el nombre del usuario, y se elimina si existe.
     *
     * @param usuarioAutenticado El identificador del usuario cuyo historial se desea eliminar.
     */
    public void borrarArchivoUsuarioAutenticado(String usuarioAutenticado) {
        // Limpiar el nombre de usuario para evitar caracteres problemáticos
        String usuario = usuarioAutenticado.replaceAll("[^a-zA-Z0-9]", "_"); // Reemplaza caracteres especiales con "_"

        // Nombre del archivo a borrar. Recuerda que cada archivo es único por usuario
        String nombreArchivo = usuario + "_historial.txt";

        // Obtener la referencia al archivo en el almacenamiento interno
        File archivo = new File(getFilesDir(), nombreArchivo);

        // Verificar si el archivo existe y eliminarlo
        if (archivo.exists()) {
            if (archivo.delete()) {
                Log.d("Archivo", "Archivo eliminado correctamente: " + archivo.getAbsolutePath());
                Toast.makeText(this, "Historial eliminado correctamente.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Archivo", "No se pudo eliminar el archivo");
                Toast.makeText(this, "Error: No se pudo eliminar el historial.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("Archivo", "El archivo no existe: " + archivo.getAbsolutePath());
            Toast.makeText(this, "No hay historial para eliminar.", Toast.LENGTH_SHORT).show();
        }
    }
}