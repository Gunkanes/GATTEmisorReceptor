package es.magtel.idi.gattemisorreceptor.patronOrden;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by SAMUAN on 09/08/2016.
 * Orden para indicar que se quiere o no recibir notificaciones.
 */
public class OrdenNotificar implements IOrden {

    private BluetoothGatt clienteGatt;
    private BluetoothGattCharacteristic caracteristica;
    private boolean activar;

    public OrdenNotificar(BluetoothGatt clienteGatt, BluetoothGattCharacteristic caracteristica, boolean activar) {
        this.clienteGatt = clienteGatt;
        this.caracteristica = caracteristica;
        this.activar = activar;
    }

    @Override
    public Object ejecutar() {
        clienteGatt.setCharacteristicNotification(caracteristica, activar);
        return null;
    }
}
