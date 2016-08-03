package es.magtel.idi.gattemisorreceptor;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * Adaptador para lista. Muestra los dispositivos detectados
 *
 * Created by SAMUAN on 18/07/2016.
 */
public class Adaptador extends RecyclerView.Adapter<Adaptador.AdaptadorViewHolder> implements ItemClickListener{

    private Context context;
    public static List<BluetoothDevice> dispositivos = new LinkedList<>();

    public Adaptador(Context context) {
        this.context = context;
    }

    public static class AdaptadorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView nombre;
        public TextView direccion;
        public ItemClickListener listener;

        public AdaptadorViewHolder(View v, ItemClickListener listener){
            super(v);
            nombre = (TextView) v.findViewById(R.id.device_name);
            direccion = (TextView) v.findViewById(R.id.device_address);
            this.listener = listener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v, getAdapterPosition());
        }
    }

    @Override
    public AdaptadorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_item_device, parent, false);
        return new AdaptadorViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(AdaptadorViewHolder holder, int position) {
        BluetoothDevice device = dispositivos.get(position);
        holder.nombre.setText(device.getName());
        holder.direccion.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return dispositivos.size();
    }

    /**
     * Añado un dispositivo a la lista si no está ya en la lista.
     * @param device
     */
    public void insertar(BluetoothDevice device){
        if( !dispositivos.contains(device)) {
            dispositivos.add(device);
            this.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(context, DispositivoActivity.class);
        intent.putExtra("dispositivo",position);
        context.startActivity(intent);
    }

}

