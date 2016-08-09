package es.magtel.idi.gattemisorreceptor.adaptadoresListas;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import es.magtel.idi.gattemisorreceptor.InvocadorOrdenes;
import es.magtel.idi.gattemisorreceptor.R;

/**
 * Created by SAMUAN on 09/08/2016.
 *
 */
public class AdaptadorPruebaOrden extends RecyclerView.Adapter<AdaptadorPruebaOrden.MiAdaptadorViewHolder> implements ItemClickListener {

    private Context context;
    public static List<Dispositivo> dispositivos = new LinkedList<>();
    public InvocadorOrdenes miinvocador;

    public AdaptadorPruebaOrden(Context context) {

        this.context = context;
        miinvocador = new InvocadorOrdenes(context);
    }

    public static class MiAdaptadorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView nombre;
        public TextView direccion;
        public ItemClickListener listener;

        public MiAdaptadorViewHolder(View v, ItemClickListener listener){
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
    public MiAdaptadorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_item_device, parent, false);
        return new MiAdaptadorViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(MiAdaptadorViewHolder holder, int position) {
        BluetoothDevice device = dispositivos.get(position).getDispositivo();
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
    public void insertar(BluetoothDevice device, long tiempo){
        limpiarAntiguos();
        Dispositivo undispositivo = new Dispositivo(device, tiempo);
        if( !estaDispositivo(undispositivo)) {
            dispositivos.add(undispositivo);
            this.notifyDataSetChanged();
        }
    }

    private boolean estaDispositivo(Dispositivo undispositivo){
        boolean salida = false;
        for (Dispositivo dispositivo:dispositivos ) {
            if( dispositivo.getDispositivo().equals( undispositivo.getDispositivo()) ) salida = true;
        }
        return salida;
    }

    private void limpiarAntiguos(){
        LinkedList<Dispositivo> listaborrado = new LinkedList<>();
        long tiempoactual = System.currentTimeMillis();
        for (Dispositivo undispositivo:dispositivos ) {
            long tiempopasado = undispositivo.getTiempo();
            if (tiempoactual > tiempopasado + 10000){
                listaborrado.add(undispositivo);
            }
        }
        for (Dispositivo undispositivo:listaborrado ) {
            dispositivos.remove(undispositivo);
        }
        this.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position) {

        miinvocador.recibirDispositivo( dispositivos.get(position).getDispositivo() );

    }


    class Dispositivo {
        private BluetoothDevice dispositivo;
        private long tiempo;

        public Dispositivo(BluetoothDevice dispositivo, long tiempo) {
            this.dispositivo = dispositivo;
            this.tiempo = tiempo;
        }

        public BluetoothDevice getDispositivo() {
            return dispositivo;
        }

        public void setDispositivo(BluetoothDevice dispositivo) {
            this.dispositivo = dispositivo;
        }

        public long getTiempo() {
            return tiempo;
        }

        public void setTiempo(long tiempo) {
            this.tiempo = tiempo;
        }
    }


}


