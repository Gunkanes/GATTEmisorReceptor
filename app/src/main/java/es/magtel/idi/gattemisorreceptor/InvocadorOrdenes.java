package es.magtel.idi.gattemisorreceptor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import es.magtel.idi.gattemisorreceptor.patronOrden.GestorDeOrdenes;
import es.magtel.idi.gattemisorreceptor.patronOrden.IOrden;
import es.magtel.idi.gattemisorreceptor.patronOrden.OrdenConectar;
import es.magtel.idi.gattemisorreceptor.patronOrden.OrdenDescubrir;
import es.magtel.idi.gattemisorreceptor.patronOrden.OrdenLeer;

/**
 * Created by SAMUAN on 09/08/2016.
 *
 * crea ordenes y las lanza.
 */
public class InvocadorOrdenes {

    private ArrayList<BluetoothDevice> dispositivos;
    private ArrayList<BluetoothGatt> clientesGatt;
    private ArrayList<BluetoothGattCharacteristic> caracteristicas;

    private Handler handler = new Handler();

    private GestorDeOrdenes gestorDeOrdenes;
    private Context context;

    public InvocadorOrdenes(Context context){
        this.context = context;

        dispositivos = new ArrayList<>();
        clientesGatt = new ArrayList<>();

        gestorDeOrdenes = new GestorDeOrdenes();
    }

    public void recibirDispositivo(BluetoothDevice dispo){
        dispositivos.add(dispo);
        int posi = dispositivos.indexOf(dispo);
        IOrden orden = new OrdenConectar(dispo, context, false, mGattCallback, clientesGatt);
        gestorDeOrdenes.add(orden);
        gestorDeOrdenes.ejecutarSiguiente();
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            for( BluetoothGattCharacteristic carac : caracteristicas){
                int propiedad = carac.getProperties();
                if( propiedad == BluetoothGattCharacteristic.PROPERTY_READ){
                    IOrden orden = new OrdenLeer( clientesGatt.get(0) , carac );
                    gestorDeOrdenes.add(orden);
                }
            }
            gestorDeOrdenes.ejecutarSiguiente();
            handler.postDelayed(this, 8000);
        }
    };

    private void ejecutarLecturaTemporizada() {

        //cada cierto tiempo realizar lectura de caracteristicas
        handler.postDelayed(runnable, 2000);
    }



    //callback de el objeto bluetoothgatt
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
            if( newState == BluetoothProfile.STATE_CONNECTED){
                Log.i("TAG","conectado a gatt server");

               // mBluetoothGatt.discoverServices();
                IOrden orden2 = new OrdenDescubrir(  clientesGatt.get(0)   );
                gestorDeOrdenes.add(orden2);
            }
            else if( newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i("TAG","desconectado de gatt server");
                //reconectar...?
                //dispositivo.connectGatt(DispositivoActivity.this, false, mGattCallback);

            }
            gestorDeOrdenes.ejecutarSiguiente();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            if( status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("TAG", "servicios descubierto");
                List<BluetoothGattService> lista = gatt.getServices();
                Log.i("TAG", "cuantos servicios " + lista.size());

                for (BluetoothGattService servicio : lista ) {
                    List<BluetoothGattCharacteristic> listacarac = servicio.getCharacteristics();
                    for(BluetoothGattCharacteristic carac: listacarac){
                        caracteristicas.add( carac );
                    }
                }
                ejecutarLecturaTemporizada();

            }else{
                Log.w("TAG","onServicesDiscovered received: "+status);
            }

            gestorDeOrdenes.ejecutarSiguiente();
        }

        // Se llama de manera asincrona al leer una caracteristica del server.
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            Log.i("CARACTERISTICA","lectura status "+status);
            if( status == BluetoothGatt.GATT_SUCCESS){
                Log.i("CARACTERISTICA", characteristic.toString());
                byte[]  datos = characteristic.getValue();
                try {
                    String algo = new String( datos,"UTF-8");
                    Log.i("CARACTERISTICA", " READ UUID "+characteristic.getUuid().toString()+" datos: "+algo);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            gestorDeOrdenes.ejecutarSiguiente();
            //colaLectura.liberarYEjectutarSiguiente();
        }

        /* se llama al enviarse una notificación de cambio por parte del emisor. */
        @Override
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            try {
                String algo = new String(characteristic.getValue(), "UTF-8");

                String nombre = DatosGATT.dameNombre( characteristic.getUuid() );
                if( nombre == null) nombre= characteristic.getUuid().toString();

                Log.i("CARACTERISTICA", "NOTIFY UUID " + nombre + " dato " + algo);

            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
            Log.e("TAG","onDescriptorwrite status "+status);
        }

    };


}
