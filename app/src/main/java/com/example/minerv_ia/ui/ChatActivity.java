package com.example.minerv_ia.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minerv_ia.data.utils.MessageAdapter;
import com.example.minerv_ia.data.utils.MessageModel;
import com.example.minerv_ia.R;
import com.example.minerv_ia.databinding.ActivityChatBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;

import com.example.minerv_ia.data.utils.AutomatizacionesIA;
import com.example.minerv_ia.data.utils.UtilidadesIA;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

/**
 * Actividad principal que gestiona la interacción del usuario con la IA a través de un chat.
 * Permite enviar mensajes al chat y recibir respuestas de la IA.
 * La actividad varía su comportamiento dependiendo del tipo de IA seleccionada.
 */
public class ChatActivity extends AppCompatActivity {

    /**
     * Listado de mensajes en la conversación, almacenando tanto los mensajes enviados por el usuario como las respuestas de la IA.
     */
    private ArrayList<MessageModel> messageList = new ArrayList<>();

    /**
     * Adaptador utilizado para mostrar los mensajes en el RecyclerView.
     */
    private MessageAdapter messageAdapter;

    /**
     * Cliente HTTP utilizado para realizar las solicitudes a la API de la IA.
     */
    private OkHttpClient client;

    /**
     * Variable que almacena el tipo de IA personalizada que el usuario ha seleccionado para interactuar.
     */
    private String IAPersonalizada;

    /**
     * Variable para almacenar la respuesta recibida de la IA en formato de texto.
     */
    private String respuestaIA = "";

    /**
     * Almacena los datos del usuario autenticado que pueden ser utilizados durante la conversación con la IA.
     */
    private String datosDelUsuario;

    /**
     * Define el tipo de IA seleccionada por el usuario, lo que cambia el comportamiento de la interacción.
     */
    private int tipoIA;

    /**
     * Variable para asignación de componentes por binding
     */
    ActivityChatBinding binding;


    /**
     * Método que se llama al crear la actividad. Inicializa la configuración inicial de la actividad,
     * establece los eventos de los botones y configura el RecyclerView para mostrar los mensajes.
     *
     * @param savedInstanceState El estado guardado de la actividad, si existe.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        client = new OkHttpClient();
        IAPersonalizada = "";
        datosDelUsuario = "";

        // Recupera los datos del Intent dentro del onCreate
        tipoIA = getIntent().getIntExtra("tipoIA", -1);
        String usuarioAutenticado = getIntent().getStringExtra("usuario");
        String datosUsuarioProcesados = "";
        Log.d("RESUMEN al INICIO de actividad", leerResumenConversacion(usuarioAutenticado));

        // Dependiendo del tipo de IA, se preparan los datos del usuario o de la IA
        if (tipoIA == 1) {
            prepararDatosUsuario(tipoIA, usuarioAutenticado, datosUsuarioProcesados);
        } else if (tipoIA == 2) {
            prepararIACaracteristicas(tipoIA, usuarioAutenticado, datosUsuarioProcesados);
        } else if (tipoIA == 3 || tipoIA == 4 || tipoIA == 5) {
            datosUsuarioProcesados = "";
            prepararIACaracteristicas(tipoIA, usuarioAutenticado, datosUsuarioProcesados);
        }

        // Carga la imagen correspondiente al tipo de IA seleccionada
        cargarImagenIA(tipoIA);

        // Configura el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_auth);
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        ImageButton botonEnviarMensaje = findViewById(R.id.btnSend);
        ImageButton botonVolverAHome = findViewById(R.id.btnVolverHome);
        RecyclerView recyclerView = findViewById(R.id.recyclerChat);

        // Configura el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        // Configura el Toolbar como la ActionBar
        setSupportActionBar(toolbar);
        tvToolbarTitle.setText(obtenerNombreTipoIA(tipoIA));

        //-----------------------------------------
        //              EVENTOS
        //-----------------------------------------
        // Evento para el botón de enviar mensaje
        botonEnviarMensaje.setOnClickListener(v -> {
            String messageText = binding.etMessage.getText().toString().trim();

            if (!messageText.isEmpty()) {
                // Agrega el mensaje del usuario al chat
                addToChat(messageText, MessageModel.SENT_BY_ME);

                // Llama a la API para obtener la respuesta de la IA
                callAPI(messageText);

                // Limpia el campo de texto
                binding.etMessage.setText("");
            } else {
                Toast.makeText(ChatActivity.this, "Por favor, escribe un mensaje.", Toast.LENGTH_SHORT).show();
            }
        });

        // Evento para el botón de volver a la pantalla principal (Home)
        botonVolverAHome.setOnClickListener(v -> {
            if (tipoIA == 1) {
                // Si el tipo de IA es 'Amigo', crea un resumen de la conversación
                crearArchivoResumen(usuarioAutenticado);
                solicitarResumenIA(usuarioAutenticado);
            } else {
                // Si no, se vuelve a la actividad principal
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }


    /**
     * Método para agregar un mensaje al chat en el hilo principal.
     * Este método se ejecuta en el hilo principal para garantizar que las actualizaciones de la interfaz de usuario
     * sean seguras. El mensaje se agrega a la lista de mensajes y se notifica al adaptador para actualizar el RecyclerView.
     *
     * @param question El contenido del mensaje que el usuario ha enviado o que se ha recibido.
     * @param sentBy   Indica quién envió el mensaje, puede ser "user" o "bot".
     */
    private void addToChat(final String question, final String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Crea un nuevo modelo de mensaje con el contenido y el remitente
                MessageModel newMessage = new MessageModel(question, sentBy);
                // Agrega el mensaje a la lista de mensajes
                messageList.add(newMessage);
                // Notifica al adaptador que los datos han cambiado para actualizar la vista
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Llama a la API de OpenAI usando OkHttp para obtener una respuesta basada en la pregunta proporcionada.
     * Este método crea un objeto JSON con los parámetros necesarios y hace una solicitud HTTP POST para obtener
     * la respuesta del modelo de IA, que luego se agrega al chat. Si la respuesta contiene comandos especiales,
     * como abrir el calendario o la agenda de contactos, se ejecutan automáticamente.
     *
     * @param question La pregunta que el usuario desea hacerle a la IA.
     */
    private void callAPI(final String question) {
        try {
            // Crear un objeto JSON con la pregunta y otros parámetros requeridos por la API de OpenAI
            JsonObject json = new JsonObject();
            json.addProperty("model", "gpt-3.5-turbo");  // Asegúrate de usar el modelo correcto (gpt-3.5-turbo o el que estés usando)

            // Agregar parámetros adicionales como temperature y max_tokens
            json.addProperty("temperature", 0.7);  // Controla la aleatoriedad (0.7 es un valor razonable)
            json.addProperty("max_tokens", 400);  // Número máximo de tokens en la respuesta

            // Crear un array para los mensajes
            JsonArray messages = new JsonArray();

            // Aquí, el rol "system" recibe el prompt personalizado que defines
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");  // Esto configura el comportamiento de la IA
            systemMessage.addProperty("content", IAPersonalizada);
            messages.add(systemMessage);

            // Agregar el historial de mensajes al cuerpo de la solicitud (si existe)
            for (MessageModel message : messageList) {
                JsonObject messageObject = new JsonObject();
                messageObject.addProperty("role", message.isSentByUser() ? "user" : "assistant");
                messageObject.addProperty("content", message.getMessage());
                messages.add(messageObject);
            }

            // Ahora, agregar el mensaje del usuario (con la pregunta que el usuario hace)
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", question);  // El contenido es la pregunta del usuario
            messages.add(userMessage);

            json.add("messages", messages);

            // Crear el RequestBody con el tipo de contenido como "application/json"
            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

            // Crear la solicitud HTTP POST con la URL de OpenAI y tu clave API
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")  // URL de la API de OpenAI
                    .addHeader("Authorization", "TU API KEY AQUI")  // MEJOR ESCONDE LA API KEY POR LOCAL PROPERTIES
                    .post(body)
                    .build();

            // Ejecutar la solicitud en un hilo de fondo
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Obtener la respuesta de la API
                        Response response = client.newCall(request).execute();
                        String responseBody = response.body().string();

                        // Imprimir la respuesta completa para debug
                        Log.d("API_RESPONSE", responseBody);

                        // Verificar si la respuesta es exitosa
                        if (response.isSuccessful()) {
                            // Usar Gson para convertir la respuesta JSON
                            Gson gson = new Gson();
                            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

                            // Verificar que la respuesta contiene el campo 'choices' correctamente
                            JsonArray choices = responseJson.getAsJsonArray("choices");
                            if (choices != null && choices.size() > 0) {
                                JsonObject choice = choices.get(0).getAsJsonObject();
                                JsonObject message = choice.getAsJsonObject("message");

                                if (message != null) {
                                    String botResponse = message.get("content").getAsString();

                                    // ANALIZAMOS LA RESPUESTA DE LA IA
                                    respuestaIA = botResponse; // Guardamos la última respuesta de la IA
                                    if (respuestaIA.equals("OK. Abriendo Calendario en el dispositivo...")) {
                                        AutomatizacionesIA.abrirCalendario(ChatActivity.this); // Abre el calendario principal
                                    }
                                    if (respuestaIA.equals("OK. Abriendo la agenda de contactos en el dispositivo...")) {
                                        AutomatizacionesIA.abrirAgendaContactos(ChatActivity.this); // Abre la agenda de contactos
                                    }
                                    // Agregar la respuesta del bot al chat
                                    addToChat(botResponse, MessageModel.SENT_BY_BOT);
                                } else {
                                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Respuesta del bot no encontrada.", Toast.LENGTH_SHORT).show());
                                }
                            } else {
                                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "No se recibió una respuesta válida.", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            // Si la respuesta no es exitosa, mostrar código de error
                            Log.e("API_ERROR", "Error HTTP: " + response.code() + " " + response.message());
                            runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error al contactar con la API: " + response.code(), Toast.LENGTH_SHORT).show());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        // Manejo de errores en la solicitud
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error al contactar con la API", Toast.LENGTH_SHORT).show());
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para obtener el nombre de la IA según el tipo proporcionado.
     * Este método devuelve un nombre representativo para el tipo de IA especificado.
     *
     * @param tipoIA El tipo de IA. Un número entero que indica el tipo de IA seleccionada.
     * @return El nombre correspondiente al tipo de IA. Si no se encuentra un tipo válido, devuelve "IA Desconocida".
     */
    public String obtenerNombreTipoIA(int tipoIA) {
        switch (tipoIA) {
            case 1:
                return "Pedro - En tus contactos";
            case 2:
                return "Especialista Automatizaciones";
            case 3:
                return "Alberto - Médico de Familia";
            case 4:
                return "Marina - Abogada Despacho";
            case 5:
                return "Juan - Taller";
            default:
                return "IA Desconocida";
        }
    }

    /**
     * Método para cargar la imagen correspondiente a la IA según el tipo proporcionado.
     * Este método selecciona y asigna una imagen a un ImageView basado en el tipo de IA.
     *
     * @param tipoIA El tipo de IA. Un número entero que indica el tipo de IA seleccionada.
     */
    private void cargarImagenIA(int tipoIA) {
        ImageView imgAssistantDynamic = findViewById(R.id.imgAssistantDynamic);

        switch (tipoIA) {
            case 1:
                imgAssistantDynamic.setImageResource(R.mipmap.avatar_amigo_round);
                break;
            case 2:
                imgAssistantDynamic.setImageResource(R.mipmap.avatar_automata_round);
                break;
            case 3:
                imgAssistantDynamic.setImageResource(R.mipmap.avatar_doctor_round);
                break;
            case 4:
                imgAssistantDynamic.setImageResource(R.mipmap.avatar_abogado_round);
                break;
            case 5:
                imgAssistantDynamic.setImageResource(R.mipmap.avatar_mecanico_round);
                break;
            default:
                imgAssistantDynamic.setImageResource(R.mipmap.ic_help_round);
                break;
        }
    }

    /**
     * Método que prepara los datos del usuario para ser utilizados por la IA.
     * Llama al método `cargarDatosDelUsuario` para obtener la información procesada del usuario
     * y luego pasa esa información junto con el tipo de IA a un método que configura las características de la IA.
     *
     * @param tipoIA                 El tipo de IA seleccionada.
     * @param usuarioAutenticado     El nombre del usuario autenticado.
     * @param datosUsuarioProcesados Datos adicionales procesados del usuario que se pasan a la IA.
     */
    private void prepararDatosUsuario(int tipoIA, String usuarioAutenticado, String datosUsuarioProcesados) {
        UtilidadesIA utilidadesIA = new UtilidadesIA(tipoIA, usuarioAutenticado);

        // Llamamos a cargarDatosDelUsuario con un callback
        utilidadesIA.cargarDatosDelUsuario(new UtilidadesIA.UsuarioListener() {
            @Override
            public void onDatosUsuarioRecibidos(String datosUsuario) {
                // Comprobamos si está llegando bien la información del usuario
                Log.d("Metodo en ChatActivity recuperando datos usuario", datosUsuario);

                if (!datosUsuario.isEmpty()) {
                    // Si tenemos datos, los guardamos para usarlos luego
                    String datosUsuarioProcesados = utilidadesIA.getDatosUsuario();
                    Log.d("Valor de datosUsuario en el Listener", datosUsuarioProcesados);
                    utilidadesIA.setDatosUsuario(datosUsuarioProcesados);
                    prepararIACaracteristicas(tipoIA, usuarioAutenticado, datosUsuarioProcesados);

                    // Aquí podrías hacer algo con los datos, como enviarlos a la IA
                    // callAPI(datosUsuarioProcesados);
                } else {
                    // Si no hay datos, puedes usar un mensaje predeterminado o manejar el caso de error
                    String mensajeError = "No podremos interactuar como amigos hasta que no te pases por el formulario y guardes mis datos..";
                    Log.d("Error en datos usuario", mensajeError);
                    addToChat(mensajeError, MessageModel.SENT_BY_BOT);
                }
            }
        });
    }

    /**
     * Método que prepara las características personalizadas de la IA a partir de los datos del usuario.
     * Llama a `cargarCaracteristicasIA` para obtener las características de la IA y luego las concatena con
     * los datos del usuario para crear un "prompt" final que se enviará a la IA. Dependiendo del tipo de IA,
     * este prompt se adapta antes de ser enviado.
     *
     * @param tipoIA                 El tipo de IA seleccionada.
     * @param usuarioAutenticado     El nombre del usuario autenticado.
     * @param datosUsuarioProcesados Los datos procesados del usuario que serán usados por la IA.
     */
    private void prepararIACaracteristicas(int tipoIA, String usuarioAutenticado, String datosUsuarioProcesados) {
        UtilidadesIA utilidadesIA = new UtilidadesIA(tipoIA, usuarioAutenticado);

        // Llamar a cargarCaracteristicasIA con un callback
        utilidadesIA.cargarCaracteristicasIA(new UtilidadesIA.IAListener() {
            @Override
            public void onIARecibida(String IApersonalizada) {
                // Comprobamos si la información de la IA llegó correctamente
                Log.d("Metodo en ChatActivity recuperando get", IApersonalizada);

                // Concatenamos los datos del usuario con la información personalizada de la IA
                String promptFinal = datosUsuarioProcesados + "\n\n" + IApersonalizada;
                IAPersonalizada = promptFinal;
                Log.d("Prompt Final antes de enviar a la API", promptFinal);

                // Llamamos a la API pasando el prompt personalizado
                if (!promptFinal.isEmpty()) {
                    Log.d("Prompt pasado a la IA", promptFinal);
                    // Sumamos a la instrucción el resumen
                    if (tipoIA == 2 || tipoIA == 3 || tipoIA == 4 || tipoIA == 5) {
                        callAPI(promptFinal);  // Pasamos el prompt final a la API
                    } else if (tipoIA == 1) {
                        promptFinal = promptFinal + "Recuerda el Resumen de la conversacion anterior : \n " + leerResumenConversacion(usuarioAutenticado) + obtenerFechaActual();
                        callAPI(promptFinal);  // Pasamos el prompt final a la API
                    }
                    Log.d("!!!!!prompt que recibe IA", promptFinal);
                } else {
                    // Si no hay datos, usamos un mensaje predeterminado
                    String mensajeIAInicial = "No tengo datos tuyos..Cuéntame un poco";
                    addToChat(mensajeIAInicial, MessageModel.SENT_BY_BOT); // Este mensaje lo verá el usuario
                }
            }
        });
    }


    /**
     * Método para solicitar un resumen de la conversación con la IA.
     * Genera un resumen de lo que el usuario ha compartido en la conversación, limitado a 200 caracteres.
     * Se utiliza el modelo de la IA para generar este resumen, y luego se muestra un cuadro de confirmación
     * para guardar el resumen generado.
     *
     * @param usuarioAutenticado El nombre del usuario autenticado, que se utiliza para identificar y guardar el resumen.
     */
    public void solicitarResumenIA(String usuarioAutenticado) {
        String promptOrdenDeResumen = "Genera un resumen de lo que te he contado de mi como usuario en no más de 200 caracteres, como si fueras el usuario, en primera persona, como si fuera el usuario quien lo hace, apuntando fechas si las hay, que empiece por Resumen: Hemos hablado de...";
        addToChat(promptOrdenDeResumen, MessageModel.SENT_BY_ME);
        Log.d("Variable Respuesta IA", respuestaIA);
        callAPI(promptOrdenDeResumen);

        // Mostrar un cuadro de confirmación para guardar el resumen
        mostrarDialogoConfirmacionGuardar(usuarioAutenticado);
    }

    /**
     * Muestra un cuadro de diálogo de confirmación para guardar el resumen generado.
     * Si el usuario acepta, el resumen es guardado; si rechaza, no se hace ninguna acción.
     *
     * @param usuarioAutenticado El nombre del usuario autenticado, utilizado para guardar el resumen.
     */
    private void mostrarDialogoConfirmacionGuardar(String usuarioAutenticado) {
        new AlertDialog.Builder(ChatActivity.this)
                .setTitle("Confirmar guardado")
                .setMessage("¿Quieres recordar nuestra conversación de hoy?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Si el usuario elige "Sí", guardar el resumen
                    if (respuestaIA != null && !respuestaIA.isEmpty()) {
                        escribirResumenConversacion(respuestaIA, usuarioAutenticado); // Guardar el resumen
                        Log.d("Confirmación", "Resumen guardado correctamente.");
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("Confirmación", "No hay respuesta para guardar.");
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Si el usuario elige "No", no hacer nada
                    Log.d("Confirmación", "El resumen no fue guardado.");
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    /**
     * Crea un archivo de resumen en el almacenamiento interno de la aplicación para el usuario autenticado.
     * Si el archivo ya existe, no se realiza ninguna acción; si no, se crea un nuevo archivo.
     *
     * @param usuarioAutenticado El nombre del usuario autenticado, utilizado para nombrar el archivo de resumen.
     */
    private void crearArchivoResumen(String usuarioAutenticado) {
        // Limpiar el nombre de usuario para evitar caracteres problemáticos
        String usuario = usuarioAutenticado.replaceAll("[^a-zA-Z0-9]", "_"); // Reemplaza caracteres especiales con "_"

        // Crear un nombre de archivo seguro
        String nombreArchivo = usuario + "_historial.txt";

        // Ruta del almacenamiento interno de la aplicación
        File archivo = new File(getFilesDir(), nombreArchivo);

        // Verificar si el archivo existe
        if (!archivo.exists()) {
            try {
                boolean creado = archivo.createNewFile();
                if (creado) {
                    Log.d("Archivo", "Archivo creado correctamente: " + archivo.getAbsolutePath());
                } else {
                    Log.e("Archivo", "No se pudo crear el archivo");
                }
            } catch (IOException e) {
                Log.e("Archivo", "Error al crear el archivo: " + e.getMessage(), e);
            }
        } else {
            Log.d("Archivo", "El archivo ya existe en: " + archivo.getAbsolutePath());
        }
    }

    /**
     * Lee el resumen de la conversación previamente guardado en el archivo de historial del usuario.
     * Si el archivo no existe o ocurre un error al leerlo, se devuelve una cadena vacía.
     *
     * @param usuarioAutenticado El nombre del usuario autenticado, utilizado para identificar el archivo de historial.
     * @return El resumen de la conversación, o una cadena vacía si no se pudo leer el archivo.
     */
    private String leerResumenConversacion(String usuarioAutenticado) {
        // Limpiar el nombre de usuario para evitar caracteres problemáticos
        String usuario = usuarioAutenticado.replaceAll("[^a-zA-Z0-9]", "_"); // Reemplaza caracteres especiales con "_"
        File archivo = new File(getFilesDir(), usuario + "_historial.txt");
        String resumenConversacion;

        if (archivo.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(archivo));
                StringBuilder contenido = new StringBuilder();
                String linea;

                // Leer el archivo línea por línea
                while ((linea = reader.readLine()) != null) {
                    contenido.append(linea).append("\n");
                }
                reader.close();

                resumenConversacion = contenido.toString();
                // Mostrar el contenido en Logcat o en un Toast
                Log.d("Contenido del archivo", contenido.toString());
                return resumenConversacion;

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Error al leer archivo", "No se pudo leer el archivo: " + e.getMessage());
                return "";
            }
        } else {
            Log.e("Archivo no encontrado", "No se encontró el archivo historial.txt");
            return ""; // Si el archivo no existe, retornar una cadena vacía
        }
    }

    /**
     * Escribe un resumen de la conversación en el archivo de historial del usuario.
     * Si el archivo no existe, se crea un nuevo archivo; de lo contrario, se sobrescribe el archivo existente.
     *
     * @param resumen            El resumen de la conversación que se desea guardar.
     * @param usuarioAutenticado El nombre del usuario autenticado, utilizado para nombrar el archivo de historial.
     */
    private void escribirResumenConversacion(String resumen, String usuarioAutenticado) {
        String usuario = usuarioAutenticado.replaceAll("[^a-zA-Z0-9]", "_");
        String nombreArchivo = usuario + "_historial.txt";
        File archivo = new File(getFilesDir(), nombreArchivo);

        try (FileWriter writer = new FileWriter(archivo)) { // Sobrescribir el archivo
            writer.write(resumen); // Escribir el nuevo resumen
            writer.flush(); // Asegurarse de que el contenido se escribe completamente
            Log.d("Archivo", "Resumen guardado correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Archivo", "Error al escribir el resumen: " + e.getMessage());
        }
    }

    /**
     * Método para obtener la fecha actual en formato de cadena.
     * Utiliza un formato específico de día, mes, año, hora, minutos y segundos.
     *
     * @return La fecha actual en formato "dd/MM/yyyy HH:mm:ss".
     */
    public String obtenerFechaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return "Fecha de hoy: " + sdf.format(new Date());
    }
}