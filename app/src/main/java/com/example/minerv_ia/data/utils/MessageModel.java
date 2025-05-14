package com.example.minerv_ia.data.utils;


public class MessageModel {

    /**
     * Tipo de mensaje enviado por el usuario.
     */
    public static final String SENT_BY_ME = "me";

    /**
     * Tipo de mensaje enviado por el bot.
     */
    public static final String SENT_BY_BOT = "bot";

    /**
     * Contenido del mensaje.
     */
    private String message;

    /**
     * Indica quién envió el mensaje: "me" o "bot".
     */
    private String sentBy;

    /**
     * Constructor para crear un nuevo mensaje.
     *
     * @param message El contenido del mensaje.
     * @param sentBy El origen del mensaje (si fue enviado por el usuario o el bot).
     */
    public MessageModel(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    /**
     * Obtiene el contenido del mensaje.
     *
     * @return El contenido del mensaje.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Establece el contenido del mensaje.
     *
     * @param message El contenido del mensaje a establecer.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Obtiene el origen del mensaje (quién lo envió).
     *
     * @return El origen del mensaje (SENT_BY_ME o SENT_BY_BOT).
     */
    public String getSentBy() {
        return sentBy;
    }

    /**
     * Establece el origen del mensaje.
     *
     * @param sentBy El origen del mensaje (SENT_BY_ME o SENT_BY_BOT).
     */
    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    /**
     * Verifica si el mensaje fue enviado por el usuario.
     *
     * @return True si el mensaje fue enviado por el usuario (SENT_BY_ME),
     *         False si el mensaje fue enviado por el bot (SENT_BY_BOT).
     */
    public boolean isSentByUser() {
        return SENT_BY_ME.equals(this.sentBy);
    }
}