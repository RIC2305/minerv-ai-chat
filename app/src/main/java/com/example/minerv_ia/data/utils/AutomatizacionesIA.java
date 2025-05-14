package com.example.minerv_ia.data.utils;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.util.Log;

/**
 * Clase que contiene métodos para interactuar con aplicaciones del sistema, como el calendario
 * y la agenda de contactos. Estos métodos permiten automatizar la apertura de estas aplicaciones
 * en el dispositivo del usuario.
 */
public class AutomatizacionesIA {

    // Etiqueta para los logs
    private static final String TAG = "AutomatizacionesIA";

    /**
     * Método para abrir la aplicación de calendario predeterminada en el dispositivo.
     * Este método utiliza un Intent con la acción `Intent.ACTION_MAIN` y la categoría
     * `Intent.CATEGORY_APP_CALENDAR` para intentar abrir la aplicación de calendario.
     *
     * Si no se encuentra una aplicación de calendario en el dispositivo, se captura
     * la excepción `ActivityNotFoundException` y se registra un error en el log.
     *
     * @param context El contexto de la aplicación, utilizado para iniciar la actividad.
     */
    public static void abrirCalendario(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent); // Intentar abrir la aplicación de calendario
        } catch (ActivityNotFoundException e) {
            // Si no se encuentra la aplicación de calendario, registrar el error
            Log.e(TAG, "No se encontró una aplicación de calendario instalada.", e);
        }
    }

    /**
     * Método para abrir la aplicación de agenda de contactos predeterminada en el dispositivo.
     * Este método utiliza un Intent con la acción `Intent.ACTION_VIEW` y la URI `content://contacts/people/`
     * para intentar abrir la agenda de contactos del dispositivo.
     *
     * Si no se encuentra una aplicación de contactos en el dispositivo, se registra un error en el log.
     *
     * @param context El contexto de la aplicación, utilizado para iniciar la actividad.
     */
    public static void abrirAgendaContactos(Context context) {
        // Crear un Intent para abrir la agenda de contactos
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people/"));

        // Verificar si hay una aplicación de contactos disponible en el dispositivo
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            // Si está disponible, abrir la agenda de contactos
            context.startActivity(intent);
        } else {
            // Si no hay ninguna aplicación de contactos disponible, registrar el error
            Log.e(TAG, "No se pudo abrir la agenda de contactos.");
        }
    }
}
