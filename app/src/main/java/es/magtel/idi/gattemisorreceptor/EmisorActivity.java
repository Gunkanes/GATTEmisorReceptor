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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EmisorActivity extends AppCompatActivity {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser anunciador;

    private AdvertiseSettings settings;
    private AdvertiseData advertiseData;

    private BluetoothGattServer mGattServer;

    private SensorAcelerometro sensor;

    private BluetoothGattCharacteristic x; //caracteristicas para acelerometro
    private BluetoothGattCharacteristic y;
    private BluetoothGattCharacteristic z;

    private BluetoothGattCharacteristic ratio; //caracteristica para pulso del corazón.
    private Thread hilocorazon;
    private boolean ejecutarHilo = true;

    private int indicadorServicio = 0 ; // indica que servicio hay que añadir
    private ArrayList<BluetoothGattService> servicios = new ArrayList<>();

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

            //servidor
            prepararServer();

            sensor = new SensorAcelerometro(this);
            actualizarCorazon();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //creo anunciador
        anunciador = mBluetoothAdapter.getBluetoothLeAdvertiser();
        //comienzo a anunciar los servicios indicados.
        anunciador.startAdvertising(settings, advertiseData, advertiseCallback );

        sensor.activar(); //activo sensor acelerometro
    }

    @Override
    public void onPause(){
        anunciador.stopAdvertising(advertiseCallback);
        sensor.desactivar();
        super.onPause();
        ejecutarHilo = false;
    }

    private void prepararSettings(){
        settings = new AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
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
     * Construye un servicio de acelerometro
     *
     */
    private BluetoothGattService dameServicioAcelerometro(){
         x = new BluetoothGattCharacteristic(
                DatosGATT.ACELEROMETRO_CARAC_X,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
         y = new BluetoothGattCharacteristic(
                DatosGATT.ACELEROMETRO_CARAC_Y,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
         z = new BluetoothGattCharacteristic(
                DatosGATT.ACELEROMETRO_CARAC_Z,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        x.setValue("10".getBytes());
        y.setValue("20".getBytes());
        z.setValue("30".getBytes());

        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(DatosGATT.CCCD , BluetoothGattDescriptor.PERMISSION_READ|BluetoothGattDescriptor.PERMISSION_WRITE);
        byte[] valor = new byte[]{ 0x00, 0x01 };
        descriptor.setValue(valor);

        x.addDescriptor(descriptor);
        y.addDescriptor(descriptor);
        z.addDescriptor(descriptor);

        BluetoothGattService servicio = new BluetoothGattService( DatosGATT.ACELEROMETRO_SERVICIO, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        servicio.addCharacteristic(x);
        servicio.addCharacteristic(y);
        servicio.addCharacteristic(z);

        return servicio;
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
     * Construye servicio Heart Rate
     * Simulo el cambio del valor con un hilo llamado desde el método actualizar Corazon
     * @return
     */
    private BluetoothGattService dameServicioHeartRate(){
        ratio = new BluetoothGattCharacteristic(DatosGATT.HEART_RATE_CARAC_MEDIDA, BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        ratio.setValue("10".getBytes());

        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(DatosGATT.CCCD, BluetoothGattDescriptor.PERMISSION_READ|BluetoothGattDescriptor.PERMISSION_WRITE);
        byte[] valor = new byte[]{ 0x00, 0x01};
        descriptor.setValue(valor);

        ratio.addDescriptor(descriptor);

        BluetoothGattCharacteristic localizacion = new BluetoothGattCharacteristic(DatosGATT.HEART_RATE_CARAC_LOCATION, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
        localizacion.setValue("muñeca".getBytes());

        BluetoothGattCharacteristic puntoControl = new BluetoothGattCharacteristic(DatosGATT.HEART_RATE_CARAC_POINT, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
        puntoControl.setValue("por defecto".getBytes());

        BluetoothGattService servicio = new BluetoothGattService( DatosGATT.HEART_RATE_SERVICIO, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        servicio.addCharacteristic(ratio);
        servicio.addCharacteristic(localizacion);
        servicio.addCharacteristic(puntoControl);
        return servicio;
    }

    // En un hilo aparte realiza el cambio de valor de los latidos de corazón.
    private void actualizarCorazon(){
        hilocorazon = new Thread(){
            int valor = 0;
            Random random = new Random();

            @Override
            public void run(){
                while(ejecutarHilo){
                    valor = 60 + random.nextInt(60);
                    try{
                        sleep(1000);       //pausa
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    List<BluetoothDevice> dispositivosconectados = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
                    if( dispositivosconectados != null){
                        ratio.setValue( String.valueOf(valor).getBytes());
                        if( dispositivosconectados.size() != 0){
                            for( BluetoothDevice device: dispositivosconectados){
                                mGattServer.notifyCharacteristicChanged(device, ratio, false);
                            }
                        }
                    }
                }
            }
        };
        hilocorazon.start();
    }

    //metodo ejecutado desde sensoracelerometro para indicar cambios en gravedad
    public void indicarGravedad( float valorx, float valory, float valorz){
        List<BluetoothDevice> connectedDevices  = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        if( connectedDevices != null){
            x.setValue( String.valueOf(valorx).getBytes() );
            y.setValue( String.valueOf(valory).getBytes() );
            z.setValue( String.valueOf(valorz).getBytes() );
            if(0 != connectedDevices.size()){
                for( BluetoothDevice device: connectedDevices){
                    Log.i("TAG","dispositivo "+device.getAddress());
                    mGattServer.notifyCharacteristicChanged(device,  x, false);
                    mGattServer.notifyCharacteristicChanged(device,  y, false);
                    mGattServer.notifyCharacteristicChanged(device,  z, false);
                }
            }
        }
    }

    /**
     * El server es el encargado de permitir la comunicación entre periferico y central.
     * Le añado los servicios que van a estar disponibles.
     */
    private void prepararServer(){
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);

        //limpiar servicios anteriores
        mGattServer.clearServices();

        //meto en lista de servicios a todos los que haya para ir añadiendolos al server gatt poco a poco.
        servicios.add( dameServicioPodometro() );
        servicios.add( dameServicioAcelerometro() );
        servicios.add( dameServicioHeartRate() );

        mGattServer.addService( servicios.get(indicadorServicio) ); //añado al primer servicio -> cuando se reciba el evento de servicio añadido se añadirán los siguientes.
        indicadorServicio = 1;

        //problema al añadir servicios, da null pointer, cuando servicio esta ok.
        // Supongo fallo al intentar añadirlos demasiado rapido.
        //cambio a añadir uno cuando se recibe notificación del anterior.

    }

    //añade un servicio de los que hay en la lista.
    private void añadirOtroServicioSiHay(){
        if( indicadorServicio < servicios.size() ) {
            mGattServer.addService(servicios.get(indicadorServicio));
            indicadorServicio++;
        }
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
            Log.d("GattServer", "Nuestro servicio gatt fue añadido. "+status+" servicio "+service.toString());
            super.onServiceAdded(status, service);

            añadirOtroServicioSiHay();
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.d("GattServer", "caracteristica leida");
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
            Log.d("TAG","Device "+device.getName()+" "+device.getAddress()+" status "+status);
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

