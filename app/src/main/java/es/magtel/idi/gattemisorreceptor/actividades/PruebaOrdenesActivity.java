package es.magtel.idi.gattemisorreceptor.actividades;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import es.magtel.idi.gattemisorreceptor.R;
import es.magtel.idi.gattemisorreceptor.adaptadoresListas.AdaptadorPruebaOrden;

public class PruebaOrdenesActivity extends AppCompatActivity {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanSettings mScanSettings;
    private ScanCallback mScanCallback ;

    private AdaptadorPruebaOrden adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba_ordenes);

        // Initializes Bluetooth adapter.
        bluetoothManager =  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        setScanSettings();
        setScanCallback();

        adaptador = new AdaptadorPruebaOrden(this);
        LinearLayoutManager lManager = new LinearLayoutManager(this);
        RecyclerView recycler = (RecyclerView) findViewById(R.id.recicladorPruebaOrdenes);
        recycler.setLayoutManager(lManager);
        recycler.setAdapter(adaptador);


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

    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        mScanSettings = mBuilder.build();
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




}
