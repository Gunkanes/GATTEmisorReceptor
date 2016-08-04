package es.magtel.idi.gattemisorreceptor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ReceptorActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;

    private ArrayList<ScanFilter> mScanFilters=new ArrayList<>();
    private ScanSettings mScanSettings;
    private ScanCallback mScanCallback ;

    private BluetoothManager bluetoothManager;

    private Adaptador adaptador;


     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptor);

        adaptador = new Adaptador(this);
        LinearLayoutManager lManager = new LinearLayoutManager(this);
        RecyclerView recycler = (RecyclerView) findViewById(R.id.reciclador);
        recycler.setLayoutManager(lManager);
        recycler.setAdapter(adaptador);

        // Initializes Bluetooth adapter.
        bluetoothManager =  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e("TAG","bluetooth desconectado o no existe");
        }else{
            setScanSettings();

            ArrayList<String> listaUUID = new ArrayList<>();
            listaUUID.add("0000180a-0000-1000-8000-00805f9b34fb");
            //listaUUID.add("");
            setScanFilter(listaUUID);

            setScanCallback();

            //arranco scan en onresume

        }


    }

    @Override
    public void onResume(){
        super.onResume();
        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback); // sin filtro
    }

    @Override
    public void onPause(){
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        super.onPause();
    }

    /**
     * Método utilizado para establecer los datos de scansettings
     */
    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        mScanSettings = mBuilder.build();
    }

    private void setScanFilter(ArrayList<String> listaUUID) {
        Log.e("TAG","setScanFilter");
        // Recorremos el ArrayList para crear un filtro para cada uno de los UUIDs
        for (int i=0;i<listaUUID.size();i++) {
            Log.e("TAG","setScanFilter");
            // Datos fabricante
            byte[] datosFabricante = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            // Máscara de datos
            byte[] mascaraDatos = new byte[]{0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0};
            // Copiamos el UUID al array para eliminar todos los "-". Parche para la API21
            System.arraycopy(hexStringToByteArray(listaUUID.get(i).replace("-", "")), 0, datosFabricante, 2, 16);
            // Generamos el filtro y lo añadimos a la lista. El valor 76 identifica al fabricante de los beacons
            ScanFilter mScanFilter=new ScanFilter.Builder().setManufacturerData(76, datosFabricante, mascaraDatos).build();
            mScanFilters.add(mScanFilter);
        }
    }

    private void setScanCallback(){
        this.mScanCallback = new ScanCallback(){

            public void onScanResult(int callbackType, ScanResult result) {
                //detectado bluetooth. capturar información

                Log.i("SCAN","Dato escaneado "+result.toString());

                BluetoothDevice device = result.getDevice();
                int rssi = result.getRssi();
                ScanRecord scanRecord = result.getScanRecord();
                byte[] scanDatos = scanRecord.getBytes();
                Log.i("SCAN"," device "+ device.toString());

                adaptador.insertar(device, System.currentTimeMillis());
                Log.i("TAG","Cuantos hay "+ adaptador.getItemCount());


            }

            public void onBatchScanResults(List<ScanResult> results) {
                //recibido un grupo de señales de bluetooth.

                Log.i("SCAN","scaneo batch "+results.size());

                BluetoothDevice device;
                int rssi;
                ScanRecord scanRecord;
                byte[] scanDatos=null;
                for (ScanResult res : results) {
                    device=res.getDevice();
                    rssi=res.getRssi();
                    scanRecord=res.getScanRecord();
                    try {
                        scanDatos = scanRecord.getBytes();
                    }
                    catch (NullPointerException e)
                    {
                        Log.e("Error:", e.getMessage());
                    }
                    if (scanDatos!=null ) {

                    }
                }
            }

            public void onScanFailed(int errorCode) {
                Log.e("TAG", "Error de escaneo: "+String.valueOf(errorCode));
            }
        };
    }


    /**
     * Método utilizado para la creación de filtros de detección de beacons.
     *
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


}

