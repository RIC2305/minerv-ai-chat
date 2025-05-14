package com.example.minerv_ia.data.utils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.minerv_ia.R;
import java.util.ArrayList;

/**
 * Adapter para mostrar una lista de mensajes en un RecyclerView.
 * El adaptador gestiona la presentación de los mensajes en el chat,
 * mostrando mensajes enviados por el usuario y por el bot en diferentes secciones.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    /**
     * Lsita de mensajes a mostrar por el RecyclerView
     */
    private ArrayList<MessageModel> messageList;

    /**
     * Constructor del adaptador que recibe la lista de mensajes.
     *
     * @param messageList Lista de mensajes que se van a mostrar en el RecyclerView.
     */
    public MessageAdapter(ArrayList<MessageModel> messageList) {
        this.messageList = messageList;
    }

    /**
     * Crea y retorna un nuevo ViewHolder para el RecyclerView.
     *
     * @param parent El ViewGroup al que el nuevo ViewHolder se asociará.
     * @param viewType El tipo de vista (no se utiliza en este caso, pero se incluye por herencia).
     * @return Un nuevo ViewHolder que contiene la vista inflada del item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout para un solo mensaje (item_message.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Asocia los datos del mensaje a las vistas del ViewHolder correspondiente.
     *
     * @param holder El ViewHolder al que se le asignará la información del mensaje.
     * @param position La posición del mensaje en la lista.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener el mensaje en la posición actual
        MessageModel message = messageList.get(position);

        // Verificar si el mensaje es enviado por el usuario o por el bot
        if (message.getSentBy().equals(MessageModel.SENT_BY_ME)) {
            // Si el mensaje es del usuario, mostrar el chat a la derecha
            holder.rightChat.setVisibility(View.VISIBLE);
            holder.rightText.setText(message.getMessage());
            holder.leftChat.setVisibility(View.GONE); // Ocultar el chat de la izquierda
        } else {
            // Si el mensaje es del bot, mostrar el chat a la izquierda
            holder.leftChat.setVisibility(View.VISIBLE);
            holder.leftText.setText(message.getMessage());
            holder.rightChat.setVisibility(View.GONE); // Ocultar el chat de la derecha
        }
    }

    /**
     * Retorna el número total de elementos en la lista de mensajes.
     *
     * @return El número de mensajes en la lista.
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * Clase ViewHolder que contiene las vistas para un solo mensaje en el RecyclerView.
     * El ViewHolder gestiona los elementos visuales de un mensaje, incluyendo los chats de usuario y bot.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // LinearLayouts que contienen los mensajes del usuario y del bot
        LinearLayout leftChat, rightChat;
        // TextViews que contienen el texto de los mensajes del usuario y del bot
        TextView leftText, rightText;

        /**
         * Constructor del ViewHolder. Inicializa las vistas correspondientes a los mensajes.
         *
         * @param itemView La vista que contiene los elementos del mensaje (incluye los LinearLayouts y TextViews).
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referenciar los LinearLayouts y TextViews
            leftChat = itemView.findViewById(R.id.leftChat);
            rightChat = itemView.findViewById(R.id.rightChat);
            leftText = itemView.findViewById(R.id.leftText);
            rightText = itemView.findViewById(R.id.rightText);
        }
    }
}