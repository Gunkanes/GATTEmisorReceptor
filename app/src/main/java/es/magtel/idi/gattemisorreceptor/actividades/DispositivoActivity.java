package es.magtel.idi.gattemisorreceptor.actividades;

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
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import es.magtel.idi.gattemisorreceptor.DatosGATT;
import es.magtel.idi.gattemisorreceptor.R;
import es.magtel.idi.gattemisorreceptor.adaptadoresListas.Adaptador;

public class DispositivoActivity extends AppCompatActivity {

    private TextView estado;
    private TextView direccion;
    private TextView datos;

    private BluetoothGatt mBluetoothGatt;

    private static final String ACCION_GATT_CONECTAR = "es.magte.idi.gattemisorreceptor.ACCION_GATT_CONECTAR";
    private static final String ACCION_GATT_DESCONECTAR = "es.magte.idi.gattemisorreceptor.ACCION_GATT_DESCONECTAR";
    private static final String ACCION_GATT_SERVICIO_DESCUBIERTO = "es.magte.idi.gattemisorreceptor.ACCION_GATT_SERVICIO_DESCUBIERTO";

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =   new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";

    private ExpandableListView mGattServicesList;

    private BluetoothDevice dispositivo;

    private HashMap<BluetoothGattCharacteristic, Boolean> mapaCaracteristicasNotificacion = new HashMap<>();

    private ColaLectura colaLectura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivo);

        direccion = (TextView) findViewById(R.id.device_address);
        estado = (TextView) findViewById(R.id.connection_state);
        datos = (TextView) findViewById(R.id.data_value);

        colaLectura = new ColaLectura();

        Bundle bundle = getIntent().getExtras();
        if( bundle != null) {
            int cual = bundle.getInt("dispositivo");
            dispositivo = Adaptador.dispositivos.get(cual).getDispositivo();
            direccion.setText(dispositivo.getName() + " " +  dispositivo.getAddress() );

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

        mBluetoothGatt =  dispositivo.connectGatt(this, false, mGattCallback);
    }

    @Override
    public void onPause(){
        super.onPause();
        this.unregisterReceiver(receptorGATT);
        close();
    }

    //callback de el objeto bluetoothgatt
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
                //reconectar...?
                //dispositivo.connectGatt(DispositivoActivity.this, false, mGattCallback);
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

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    // broadcast que recibe las acciones conectar , deconectar y servicio descubierto.
    // controlado en onresume y onpause
    // detectamos cuando existe conexión o no.
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
                //ejecutarLecturaTemporizada();
            }
        }
    };



    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }

    //muestra en pantalla los servicios descubiertos. da opción a activar las notifiaciones de las caracteristicas.
    //rellena el interfaz con los datos.
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

            String nombreServ = DatosGATT.dameNombre( gattService.getUuid());
            if( nombreServ != null){
                currentServiceData.put(LIST_NAME, nombreServ);
            }else{
                currentServiceData.put(LIST_NAME, unknownServiceString);
            }
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

                String nombreCarac = DatosGATT.dameNombre( gattCharacteristic.getUuid() );
                if (nombreCarac != null){
                    currentCharaData.put(LIST_NAME, nombreCarac);
                } else{
                    currentCharaData.put(LIST_NAME, unknownCharaString);
                }
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

                        if ( charaProp == BluetoothGattCharacteristic.PROPERTY_READ ){
                            Log.i("TAG"," property read ");
                            readCharacteristic(characteristic);
                        }

                        if( charaProp == BluetoothGattCharacteristic.PROPERTY_NOTIFY){
                            setCharacteristicNotification( characteristic, true);
                        }

                        if( charaProp == BluetoothGattCharacteristic.PROPERTY_WRITE){
                            writeCharacteristic( characteristic );
                        }

                        return true;
                    }
                    return false;
                }
    };

    private void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if( mBluetoothGatt == null){ Log.e("TAG","bluetoothgatt es null"); return; }
        Log.i("TAG","write characteristic");

        Log.i("WRITE","valor antes : "+characteristic.getValue());
        String valor = " algo a poner en server ";
        characteristic.setValue(valor.getBytes());
        Log.i("WRITE","valor despues : "+characteristic.getValue());
        mBluetoothGatt.writeCharacteristic( characteristic  );
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if( characteristic == null) Log.e("TAG","caracteristica es null");
        if( mBluetoothGatt == null) Log.e("TAG","bluetoothgatt es null");
        Log.e("TAG","orden de lectura "+characteristic.getUuid());
        boolean respuesta = mBluetoothGatt.readCharacteristic(characteristic);
        Log.e("TAG","orden de lectura "+respuesta);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,  boolean enabled) {
        if ( mBluetoothGatt == null) {
            Log.w("TAG", "BluetoothAdapter not initialized");
            return;
        }

        //Solo notificación en las características que lo soporten
        if( soportaNotificacion(characteristic)){
            //Si la caracteristica no está en el mapa la añado y la activo. Si está en el mapa miro si está activa para pararla o si está parada para activarla.
            if( mapaCaracteristicasNotificacion.containsKey(characteristic) ){
               Boolean estado = mapaCaracteristicasNotificacion.get(characteristic);
               if(estado){
                    mapaCaracteristicasNotificacion.put(characteristic,false);
                    mBluetoothGatt.setCharacteristicNotification(characteristic, false);
               }else{
                   mapaCaracteristicasNotificacion.put(characteristic, true);
                   mBluetoothGatt.setCharacteristicNotification(characteristic, true);
               }
            }else{
                mapaCaracteristicasNotificacion.put(characteristic,true);
                mBluetoothGatt.setCharacteristicNotification(characteristic,true);
            }
        }
    }

    private boolean soportaNotificacion( BluetoothGattCharacteristic carac){
        UUID uuid = carac.getUuid();
        if( uuid.equals( DatosGATT.ACELEROMETRO_CARAC_X)) return true;
        if( uuid.equals( DatosGATT.ACELEROMETRO_CARAC_Y)) return true;
        if( uuid.equals( DatosGATT.ACELEROMETRO_CARAC_Z)) return true;
        if( uuid.equals( DatosGATT.HEART_RATE_CARAC_MEDIDA)) return true;
        return false;
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }



    /****  version con timer para recopilar datos constantemente de caracteristicas de lectura. ****/
    //timer ejecuta cada cierto tiempo orden de lectura .
    //orden se almacena en cola para que se ejecute

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            //algo...
            for( int i = 0 ; i< mGattCharacteristics.size(); i++) {
                for (int j = 0; j < mGattCharacteristics.get(i).size(); j++) {
                    BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(i).get(j);
                    int propiedad = characteristic.getProperties();
                    if (propiedad == BluetoothGattCharacteristic.PROPERTY_READ) {
                        Log.i("TAG", " envio a cola propiedad ");
                        //readCharacteristic(characteristic);
                        colaLectura.ponerEnCola(characteristic);
                    }
                }
            }
            colaLectura.ejecutarSiguiente();
            handler.postDelayed(this, 5000);
        }
    };

    private void ejecutarLecturaTemporizada() {

        //cada cierto tiempo realizar lectura de caracteristicas
        handler.postDelayed(runnable, 2000);


    }


    /**
     * Gestiona la lectura de distintas caracteristicas.
     */
    class ColaLectura {

        private Boolean leyendo = false;
        private List<BluetoothGattCharacteristic> lista;

        ColaLectura(){
            lista = new LinkedList<>();
        }


        /**
         * Añade a la lista un objeto caracteristica para ser leido en su momento.
         *
         * @param characteristic
         */
        public void ponerEnCola(BluetoothGattCharacteristic characteristic ){
            lista.add(characteristic);
        }

        /**
         * Realiza la petición de leer la siguiente caracteristica de la lista.
         * LLamado por un temporizador ??? para leer cada cierto tiempo todas las caracteristicas de los distintos sensores.
         */
        public void ejecutarSiguiente(){
            if(lista.size()>0) {
                if (!leyendo) {
                    synchronized (leyendo) {
                        leyendo = true;
                    }
                    BluetoothGattCharacteristic carac = lista.remove(0);
                    boolean correcto = mBluetoothGatt.readCharacteristic(carac);
                    if (!correcto) {
                        synchronized (leyendo) {
                            leyendo = false;
                        }
                    }
                }
            }else{
                liberar();
            }
        }

        /**
         * Libera el bloqueo para que se pueda leer la siguiente caracteristica
         */
        public void liberar(){
            synchronized (leyendo){
                leyendo = false;
            }
        }

        public void liberarYEjectutarSiguiente(){
            liberar();
            ejecutarSiguiente();
        }
    }


}
