package es.magtel.idi.gattemisorreceptor.patronOrden;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by SAMUAN on 09/08/2016.
 * Orden para leer caracteristicas
 */
public class OrdenLeer implements IOrden {

    private BluetoothGatt clienteGatt;
    private BluetoothGattCharacteristic caracteristica;

    public OrdenLeer(BluetoothGatt clienteGatt, BluetoothGattCharacteristic caracteristica) {
        this.clienteGatt = clienteGatt;
        this.caracteristica = caracteristica;
    }

    @Override
    public Object ejecutar() {
        clienteGatt.readCharacteristic(caracteristica);
        return null;
    }
}
