package es.magtel.idi.gattemisorreceptor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmisorActivity extends AppCompatActivity {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser anunciador;

    private AdvertiseSettings settings;
    private AdvertiseData advertiseData;

    private BluetoothGattServer mGattServer;
    private BluetoothGattService miservicio;

    private static final String CADENA = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String SOFTWARE_REVISION_STRING ="00002A28-0000-1000-8000-00805f9b34fb";
    private static final String  CHAR_NOTIFY = "0000fff4-0000-1000-8000-00805f9b34fb";



    private static boolean ejecutar = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emisor);

        // Initializes Bluetooth adapter.
        mBluetoothManager =  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e("TAG","Bluetooth no se ha iniciado ");
        }else {

            //configuración
            prepararSettings();

            //anuncio
            prepararData();

            //preparar servicio
            prepararServicio();

            //servidor
            prepararServer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //creo anunciador
        anunciador = mBluetoothAdapter.getBluetoothLeAdvertiser();
        //comienzo a anunciar los servicios indicados.
        anunciador.startAdvertising(settings, advertiseData, advertiseCallback );
        ejecutar = true;
    }

    @Override
    public void onPause(){
        anunciador.stopAdvertising(advertiseCallback);
        ejecutar =false;
        super.onPause();
    }

    private void prepararSettings(){
        settings = new AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setConnectable(true)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
        .build();
    }

    private void prepararData(){
        advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build();
    }

    /**
     * Un servicio está hecho de caracteristicas y otros servicios
     * Creo el servicio , creo una caracteristica y añado la caracteristica al servicio.
     */
    private void prepararServicio(){
        miservicio = new BluetoothGattService(UUID.fromString(CADENA), BluetoothGattService.SERVICE_TYPE_PRIMARY );

        BluetoothGattCharacteristic softwareVerCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(SOFTWARE_REVISION_STRING),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        softwareVerCharacteristic.setValue("3.1.2".getBytes());

        final BluetoothGattCharacteristic notifyCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_NOTIFY),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        BluetoothGattDescriptor descriptor  = new BluetoothGattDescriptor(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb"),BluetoothGattDescriptor.PERMISSION_READ);
        notifyCharacteristic.addDescriptor(descriptor);

        notifyCharacteristic.setValue("100");

        //Código para enviar datos diferentes cada cierto tiempo en el servicio de notificación

        Thread thread = new Thread() {
            int i = 0;

            @Override
            public void run() {

                while(ejecutar) {

                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {}

                    List<BluetoothDevice> connectedDevices  = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);

                    if(null != connectedDevices)
                    {
                        Log.i("TAG","hay dispositivos");
                        notifyCharacteristic.setValue(String.valueOf(i).getBytes());

                        if(0 != connectedDevices.size()){
                            for( BluetoothDevice device: connectedDevices){
                                Log.i("TAG","dispositivo "+device.getAddress());
                                mGattServer.notifyCharacteristicChanged(device,  notifyCharacteristic, false);
                            }
                        }
                    }
                    i++;
                    Log.i("I","el vlaor de i es "+i+String.valueOf(i).getBytes().toString());
                }
            }
        };

        thread.start();



        miservicio.addCharacteristic(notifyCharacteristic);
        miservicio.addCharacteristic(softwareVerCharacteristic);
    }

    /**
     * Construye un servicio podómetro.
     * @return un servicio
     */
    private BluetoothGattService dameServicioPodometro(){
        BluetoothGattCharacteristic pasos = new BluetoothGattCharacteristic(
                DatosGATT.PODOMETRO_CARAC_PASOS,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        pasos.setValue("55".getBytes());

        BluetoothGattCharacteristic tiempo = new BluetoothGattCharacteristic(
                DatosGATT.PODOMETRO_CARAC_TIEMPO,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        tiempo.setValue("ayer".getBytes());

        BluetoothGattService servicio =  new BluetoothGattService( DatosGATT.PODOMETRO_SERVICIO, BluetoothGattService.SERVICE_TYPE_PRIMARY );
        servicio.addCharacteristic(pasos);
        servicio.addCharacteristic(tiempo);
        return servicio;
    }

    /**
     * El server es el encargado de permitir la comunicación entre periferico y central.
     * Le añado los servicios que van a estar disponibles.
     */
    private void prepararServer(){
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);

        //limpiar servicios anteriores
        mGattServer.clearServices();

        mGattServer.addService(miservicio);
        mGattServer.addService( dameServicioPodometro() );
    }

    /**
     * Respuesta  al crear anunciador.
     */
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d("advertisecallback"," Advertise Callback onstartsuccess");
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e("advertisecallback"," Advertise Callback onstartfailure "+errorCode);
            super.onStartFailure(errorCode);
        }
    };

    /**
     * Indicador de estados del server.
     */
    private final BluetoothGattServerCallback mGattServerCallback   = new BluetoothGattServerCallback(){

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState){
            Log.d("GattServer", "Our gatt server connection state changed, new state ");
            Log.d("GattServer", Integer.toString(newState));
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.d("GattServer", "Our gatt server service was added.");
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.d("GattServer", "Our gatt characteristic was read.");
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,   characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d("GattServer", "We have received a write request for one of our hosted characteristics");
            //Log.d("GattServer", "data = "+ value.toString());
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status)
        {
            Log.d("GattServer", "onNotificationSent");
            super.onNotificationSent(device, status);
            Log.d("TAG","DEvice "+device.getName()+" "+device.getAddress()+" status "+status);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.d("GattServer", "Our gatt server descriptor was read.");
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, descriptor.getValue() );
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d("GattServer", "Our gatt server descriptor was written.");
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.d("GattServer", "Our gatt server on execute write.");
            super.onExecuteWrite(device, requestId, execute);
        }

    };



}

