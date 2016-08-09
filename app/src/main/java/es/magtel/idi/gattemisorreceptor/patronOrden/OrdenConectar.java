package es.magtel.idi.gattemisorreceptor.patronOrden;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;

import java.util.ArrayList;

import es.magtel.idi.gattemisorreceptor.InvocadorOrdenes;

/**
 * Created by SAMUAN on 09/08/2016.
 * Orden para conectar un dispostivo
 */
public class OrdenConectar implements IOrden {

    private BluetoothDevice dispositivo;
    private Context context;
    private boolean autoconectar;
    private BluetoothGattCallback callback;
    private ArrayList<BluetoothGatt> clientesGatt;

    public OrdenConectar(BluetoothDevice dispositivo, Context context, boolean autoconectar, BluetoothGattCallback callback, ArrayList<BluetoothGatt> clientesGatt) {
        this.dispositivo = dispositivo;
        this.context = context;
        this.autoconectar = autoconectar;
        this.callback = callback;
        this.clientesGatt = clientesGatt;
    }

    @Override
    public Object ejecutar() {
        clientesGatt.add( dispositivo.connectGatt(context, autoconectar, callback));
        return null;
    }

}
