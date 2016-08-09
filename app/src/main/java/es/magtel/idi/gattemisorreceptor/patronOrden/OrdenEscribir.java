package es.magtel.idi.gattemisorreceptor.patronOrden;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by SAMUAN on 09/08/2016.
 * Orden para escribir dato en una caracteristica
 */
public class OrdenEscribir implements IOrden {

    private BluetoothGatt clienteGatt;
    private BluetoothGattCharacteristic caracteristica;
    private byte[] dato;

    public OrdenEscribir(BluetoothGatt clienteGatt, BluetoothGattCharacteristic caracteristica, byte[] dato) {
        this.clienteGatt = clienteGatt;
        this.caracteristica = caracteristica;
        this.dato = dato;
    }

    @Override
    public Object ejecutar() {
        caracteristica.setValue(dato);
        clienteGatt.writeCharacteristic(caracteristica);
        return null;
    }
}
