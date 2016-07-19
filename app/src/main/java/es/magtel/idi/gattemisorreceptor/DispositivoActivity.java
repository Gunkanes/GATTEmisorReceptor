package es.magtel.idi.gattemisorreceptor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DispositivoActivity extends AppCompatActivity {

    private TextView estado;
    private TextView direccion;
    private TextView datos;

    private BluetoothGatt mBluetoothGatt;

    private static final String ACCION_GATT_CONECTAR = "es.magte.idi.gattemisorreceptor.ACCION_GATT_CONECTAR";
    private static final String ACCION_GATT_DESCONECTAR = "es.magte.idi.gattemisorreceptor.ACCION_GATT_DESCONECTAR";
    private static final String ACCION_GATT_SERVICIO_DESCUBIERTO = "es.magte.idi.gattemisorreceptor.ACCION_GATT_SERVICIO_DESCUBIERTO";

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =   new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private ExpandableListView mGattServicesList;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivo);

        direccion = (TextView) findViewById(R.id.device_address);
        estado = (TextView) findViewById(R.id.connection_state);
        datos = (TextView) findViewById(R.id.data_value);

        Bundle bundle = getIntent().getExtras();
        if( bundle != null) {
            int cual = bundle.getInt("dispositivo");
            BluetoothDevice device = Adaptador.dispositivos.get(cual);
            direccion.setText(device.getName() + " " +  device.getAddress() );

            mBluetoothGatt =  device.connectGatt(this, false, mGattCallback);

            mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
            mGattServicesList.setOnChildClickListener(servicesListClickListner);
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filtro = new IntentFilter();
        filtro.addAction(ACCION_GATT_CONECTAR);
        filtro.addAction(ACCION_GATT_DESCONECTAR);
        filtro.addAction(ACCION_GATT_SERVICIO_DESCUBIERTO);
        this.registerReceiver(receptorGATT,filtro);
    }

    @Override
    public void onPause(){
        super.onPause();
        this.unregisterReceiver(receptorGATT);
        close();
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState){
            if( newState == BluetoothProfile.STATE_CONNECTED){
                Log.i("TAG","conectado a gatt server");
                mBluetoothGatt.discoverServices();
                broadcastUpdate(ACCION_GATT_CONECTAR);
            }
            else if( newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i("TAG","desconectado de gatt server");
                broadcastUpdate(ACCION_GATT_DESCONECTAR);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            if( status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("TAG", "servicio descubierto");
                List<BluetoothGattService> lista = gatt.getServices();
                Log.i("TAG", "cuantos servicios " + lista.size());
                broadcastUpdate(ACCION_GATT_SERVICIO_DESCUBIERTO);
            }else{
                Log.w("TAG","onServicesDiscovered received: "+status);
            }
        }

        // Se llama de manera asincrona al leer una caracteristica del server.
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            if( status == BluetoothGatt.GATT_SUCCESS){
                Log.i("CARACTERISTICA", characteristic.toString());
                byte[]  datos = characteristic.getValue();
                try {
                    String algo = new String( datos,"UTF-8");
                    Log.i("CARACTERISTICA", "UUID "+characteristic.getUuid().toString()+" datos: "+algo);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        /* se llama al enviarse una notificación de cambio por parte del emisor. */
        @Override
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            try {
                String algo = new String(characteristic.getValue(), "UTF-8");
                Log.i("CARACTERISTICA", " UUID " + characteristic.getUuid().toString() + " dato " + algo);
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }

        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private final BroadcastReceiver receptorGATT = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if( action.equals(ACCION_GATT_CONECTAR) ){
                estado.setText("conectado");
            } else if (action.equals(ACCION_GATT_DESCONECTAR)){
                clearUI();
                estado.setText("desconectado");
            } else if (action.equals(ACCION_GATT_SERVICIO_DESCUBIERTO)){
                mostrarServicios();
            }
        }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }

    private void mostrarServicios(){
        List<BluetoothGattService> servicios = mBluetoothGatt.getServices();
        datos.setText(" Servicios encontrados : "+servicios.size());

        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData  = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : servicios) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put( LIST_NAME, unknownServiceString);
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =          new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =          gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =             new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(      LIST_NAME, unknownCharaString);
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,  int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =    mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                setCharacteristicNotification(  mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            setCharacteristicNotification(  characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if( characteristic == null) Log.e("TAG","caracteristica es null");
        if( mBluetoothGatt == null) Log.e("TAG","bluetoothgatt es null");
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,  boolean enabled) {
        if ( mBluetoothGatt == null) {
            Log.w("TAG", "BluetoothAdapter not initialized");
            return;
        }

        //Solo notificación en las características que lo soporten
        if( characteristic.getUuid().equals(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb") ) ){
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor( UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb"));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE );
            mBluetoothGatt.writeDescriptor(descriptor);
        }

    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

}
