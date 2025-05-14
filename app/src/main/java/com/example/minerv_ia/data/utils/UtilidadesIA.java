package com.example.minerv_ia.data.utils;
import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Esta clase proporciona métodos para manejar los datos relacionados con los usuarios y las IA en una aplicación.
 * Permite cargar datos del usuario desde Firestore, cargar características específicas de una IA
 * según su tipo, y generar prompts personalizados para interactuar con la IA.
 */
public class UtilidadesIA {

    /**
     * Tipologías IA para manejo de datos:
     * Tipo 1 : IA Amigo
     * Tipo 2 : IA Automatizaciones
     * Tipo 3 : IA Doctor
     * Tipo 4 : IA Abogado
     * Tipo 5 : IA Mecánico
     * Tipo n : IA añadidas con posterioridad
     */
    int tipoIA;
    static String prompt = "";

    String IAPersonalizada = "";

    String datosUsuario = "";

    String usuarioAutenticado;
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Constructor para inicializar la clase con el tipo de IA y el usuario autenticado.
     *
     * @param tipoIA el tipo de IA (1-5)
     * @param usuarioAutenticado el identificador del usuario autenticado
     */
    public UtilidadesIA(int tipoIA, String usuarioAutenticado) {
        this.tipoIA = tipoIA;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    // Métodos getter y setter

    /**
     * Obtiene el identificador del usuario autenticado.
     *
     * @return el identificador del usuario
     */
    public String getUsuarioAutenticado() {
        return usuarioAutenticado;
    }

    /**
     * Establece el identificador del usuario autenticado.
     *
     * @param usuarioAutenticado el nuevo identificador del usuario
     */
    public void setUsuarioAutenticado(String usuarioAutenticado) {
        this.usuarioAutenticado = usuarioAutenticado;
    }

    /**
     * Obtiene la instancia de la base de datos de Firestore.
     *
     * @return la instancia de FirebaseFirestore
     */
    public static FirebaseFirestore getDb() {
        return db;
    }

    /**
     * Establece la instancia de la base de datos de Firestore.
     *
     * @param db la nueva instancia de FirebaseFirestore
     */
    public static void setDb(FirebaseFirestore db) {
        UtilidadesIA.db = db;
    }

    /**
     * Carga los datos del usuario desde Firestore y notifica al listener con los resultados.
     *
     * @param listener el listener que recibirá los datos del usuario
     */
    public void cargarDatosDelUsuario(UsuarioListener listener) {
        if (this.usuarioAutenticado != null) {
            DocumentReference usuarioRef = db.collection("usuarios").document(usuarioAutenticado);

            usuarioRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        // Obtener datos del usuario
                        String nombrePilaUsuario = documentSnapshot.getString("nombrePila");
                        nombrePilaUsuario = (nombrePilaUsuario != null) ? nombrePilaUsuario : "";

                        String ciudadUsuario = documentSnapshot.getString("ciudad");
                        ciudadUsuario = (ciudadUsuario != null) ? ciudadUsuario : "";

                        String acercaUsuario = documentSnapshot.getString("acercaUsuario");
                        acercaUsuario = (acercaUsuario != null) ? acercaUsuario : "";

                        String generoUsuario = documentSnapshot.getString("genero");
                        generoUsuario = (generoUsuario != null) ? generoUsuario : "";

                        Long edadUsuarioLong = documentSnapshot.getLong("edad");
                        int edadUsuario = (edadUsuarioLong != null) ? edadUsuarioLong.intValue() : 16;

                        // Datos de la IA
                        String nombreIA = documentSnapshot.getString("nombreIA");
                        nombreIA = (nombreIA != null) ? nombreIA : "Pedro";

                        Long edadIALong = documentSnapshot.getLong("edadIA");
                        int edadIA = (edadIALong != null) ? edadIALong.intValue() : 40;

                        String estiloIA = documentSnapshot.getString("estiloIA");
                        estiloIA = (estiloIA != null) ? estiloIA : "";

                        String acercaIA = documentSnapshot.getString("acercaIA");
                        acercaIA = (acercaIA != null) ? acercaIA : "";

                        // Crear mensaje con datos del usuario
                        String promptDatosUsuario = "Información del usuario: \n"
                                + "Nombre: " + nombrePilaUsuario + "\n"
                                + "Ciudad: " + ciudadUsuario + "\n"
                                + "Acerca del usuario: " + acercaUsuario + "\n"
                                + "Género: " + generoUsuario + "\n"
                                + "Edad: " + edadUsuario + "\n\n"
                                + "Información de la IA: \n"
                                + "Nombre de la IA: " + nombreIA + "\n"
                                + "Edad de la IA: " + edadIA + "\n"
                                + "Estilo de la IA: " + estiloIA + "\n"
                                + "Acerca de la IA: " + acercaIA;

                        Log.d("DatosUsuario", promptDatosUsuario);
                        setDatosUsuario(promptDatosUsuario);

                        // Notificar al listener con los datos recuperados
                        listener.onDatosUsuarioRecibidos(promptDatosUsuario);
                    } else {
                        Log.d("UserConfig", "No se encontraron datos para el usuario.");
                        listener.onDatosUsuarioRecibidos(""); // Retorna string vacío si no hay datos
                    }
                } else {
                    Log.e("UserConfig", "Error al recuperar los datos del usuario", task.getException());
                    listener.onDatosUsuarioRecibidos(""); // Retorna string vacío en caso de error
                }
            });
        } else {
            Log.d("FirebaseAuth", "No hay usuario autenticado.");
            listener.onDatosUsuarioRecibidos(""); // Retorna string vacío si no hay usuario autenticado
        }
    }

    /**
     * Carga las características de la IA desde Firestore y notifica al listener con los resultados.
     *
     * @param listener el listener que recibirá los datos de la IA
     */
    public void cargarCaracteristicasIA(IAListener listener) {
        String documentoConTipoIA = "tipo" + this.tipoIA;
        DocumentReference modelosIARef = db.collection("modelosIA").document(documentoConTipoIA);

        modelosIARef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    // Obtener los valores de Firestore
                    String nombreIA = documentSnapshot.getString("nombre");
                    nombreIA = (nombreIA != null) ? nombreIA : "Desconocido";  // Evitar valores nulos

                    String caracteristicaIA = documentSnapshot.getString("caracteristica");
                    caracteristicaIA = (caracteristicaIA != null) ? caracteristicaIA : "Sin información";
                    String IApersonalizada = nombreIA + " " + caracteristicaIA;

                    //La línea siguiente se ha añadido...no le llega el get..
                    setIAPersonalizada(getIApersonalizada() + IApersonalizada);
                    listener.onIARecibida(IApersonalizada); // Notificar a través del callback

                } else {
                    Log.d("CaracteristicasIA", "No se encontraron datos para el tipo de IA: " + documentoConTipoIA);
                    listener.onIARecibida(""); // Enviar un string vacío si no hay datos
                }
            } else {
                Log.e("CaracteristicasIA", "Error al recuperar los datos de Firestore", task.getException());
                listener.onIARecibida(""); // Enviar un string vacío en caso de error
            }
        });
    }

    /**
     * Crea un prompt personalizado basado en el tipo de IA.
     *
     * @return el prompt creado para la IA según su tipo
     */
    public String crearPromptIA() {
        switch (this.tipoIA) {
            case 1: // AMIGO
                prompt = "Hola, soy tu IA Amigo. ¿En qué puedo ayudarte hoy?";
                break;
            case 2: // AUTOMATIZACIONES
                prompt = "Bienvenido a IA Automatizaciones. ¿Qué proceso deseas optimizar?";
                break;
            case 3: // DOCTOR
                prompt = "Soy IA Doctor. ¿Cuál es tu síntoma o consulta médica?";
                break;
            case 4: // ABOGADO
                prompt = "IA Abogado a tu servicio. ¿Tienes dudas legales que resolver?";
                break;
            case 5: // MECANICO
                prompt = "Soy IA Mecánico. ¿Cuál es el problema con tu vehículo?";
                break;
            default:
                prompt = "Soy una IA personalizada. ¿Cómo puedo asistirte?";
                break;
        }
        return prompt;
    }

    // Métodos getter y setter

    /**
     * Obtiene el tipo de IA.
     *
     * @return el tipo de IA
     */
    public int getTipoIA() {
        return tipoIA;
    }

    /**
     * Establece el tipo de IA.
     *
     * @param tipoIA el nuevo tipo de IA
     */
    public void setTipoIA(int tipoIA) {
        this.tipoIA = tipoIA;
    }

    /**
     * Obtiene el prompt actual.
     *
     * @return el prompt actual
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Establece un nuevo prompt.
     *
     * @param prompt el nuevo prompt
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * Obtiene el texto personalizado de la IA.
     *
     * @return el texto personalizado de la IA
     */
    public String getIApersonalizada() {
        return IAPersonalizada;
    }

    /**
     * Establece un texto personalizado para la IA.
     *
     * @param IAPersonalizada el nuevo texto personalizado
     */
    public void setIAPersonalizada(String IAPersonalizada) {
        this.IAPersonalizada = IAPersonalizada;
    }

    /**
     * Obtiene los datos del usuario.
     *
     * @return los datos del usuario
     */
    public String getDatosUsuario() {
        return datosUsuario;
    }

    /**
     * Establece los datos del usuario.
     *
     * @param datosUsuario los nuevos datos del usuario
     */
    public void setDatosUsuario(String datosUsuario) {
        this.datosUsuario = datosUsuario;
    }

    /**
     * Interfaz para recibir la respuesta de la IA con las características.
     */
    public interface IAListener {
        /**
         * Método para recibir las características de la IA.
         *
         * @param IApersonalizada las características personalizadas de la IA
         */
        void onIARecibida(String IApersonalizada);
    }

    /**
     * Interfaz para recibir los datos del usuario.
     */
    public interface UsuarioListener {
        /**
         * Método para recibir los datos del usuario.
         *
         * @param datosUsuario los datos del usuario
         */
        void onDatosUsuarioRecibidos(String datosUsuario);
    }
}