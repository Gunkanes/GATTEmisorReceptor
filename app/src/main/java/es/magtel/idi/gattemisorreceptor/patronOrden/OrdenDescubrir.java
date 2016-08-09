package es.magtel.idi.gattemisorreceptor.patronOrden;

import android.bluetooth.BluetoothGatt;

/**
 * Created by SAMUAN on 09/08/2016.
 * Orden para descubrir servicios de los dispositivos conectados
 */
public class OrdenDescubrir implements IOrden {

    private BluetoothGatt clienteGatt;

    public OrdenDescubrir(BluetoothGatt clienteGatt) {
        this.clienteGatt = clienteGatt;
    }

    @Override
    public Object ejecutar() {
        clienteGatt.discoverServices();
        return null;
    }
}
