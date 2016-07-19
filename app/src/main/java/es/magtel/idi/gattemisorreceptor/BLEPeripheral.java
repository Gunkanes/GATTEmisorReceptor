package es.magtel.idi.gattemisorreceptor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;

/**
 * Created by SAMUAN on 15/07/2016.
 */
public class BLEPeripheral {

    private BluetoothManager mManager;
    private BluetoothAdapter mAdapter;
    private BluetoothLeAdvertiser mLeAdvertiser;
    private AdvertiseSettings.Builder settingbuilder;
    private AdvertiseData.Builder advBuilder;
    private BluetoothGattServer mGattServer;

    public void init(Context context){

    }

    public void stopAdvertise() {
    }

    public void startAdvertise() {
    }

    public class WriteCallback {
    }
}

/*

 public interface ConnectionCallback {
     void onConnectionStateChange(BluetoothDevice device, int newState);
 }
 ConnectionCallback mConnectionCallback;

 public interface WriteCallback {
     void onWrite(byte[] data);
 }

 WriteCallback mWriteCallback;

 public static boolean isEnableBluetooth(){
  return BluetoothAdapter.getDefaultAdapter().isEnabled();
 }

 public int init(Context context){

  if(null == mManager)
  {
   mManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);

   if(null == mManager)
    return -1;

   if(false == context.getPackageManager().
     hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
    return -2;
  }

  if(null == mAdapter)
  {
   mAdapter = mManager.getAdapter();

   if(false == mAdapter.isMultipleAdvertisementSupported())
    return -3;
  }

  if(null == settingBuilder)
  {
   settingBuilder = new AdvertiseSettings.Builder();
   settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
   settingBuilder.setConnectable(true);
   settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
  }

  if(null == advBuilder)
  {
   advBuilder = new AdvertiseData.Builder();
   mAdapter.setName("SimplePeripheral");
   advBuilder.setIncludeDeviceName(true);
  }


        if(null == mGattServer)
        {
         mGattServer = mManager.openGattServer(context, mGattServerCallback);

         if(null == mGattServer)
          return -4;

         addDeviceInfoService();
        }

  return 0;
 }

 public void setConnectionCallback(ConnectionCallback callback)
 {
  mConnectionCallback = callback;
 }

    public void close(){
  if(null != mLeAdvertiser)
   stopAdvertise();

  if(null != mGattServer)
   mGattServer.close();
  mGattServer = null;

  if(null != advBuilder)
   advBuilder = null;

  if(null != settingBuilder)
        settingBuilder = null;

  if(null != mAdapter)
        mAdapter = null;

  if(null != mManager)
        mManager = null;
 }


    public static String getAddress(){
        return BluetoothAdapter.getDefaultAdapter().getAddress();
    }

 private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {

  @Override
  public void onStartFailure(int errorCode){
   Log.d("advertise","onStartFailure");
  }

  @Override
  public void onStartSuccess(AdvertiseSettings settingsInEffect){
   Log.d("advertise","onStartSuccess");
  };
 };



 private void addDeviceInfoService()
 {
  if(null == mGattServer)
   return;

  final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
        final String SOFTWARE_REVISION_STRING = "00002A28-0000-1000-8000-00805f9b34fb";


  BluetoothGattService previousService =
           mGattServer.getService( UUID.fromString(SERVICE_DEVICE_INFORMATION));

     if(null != previousService)
         mGattServer.removeService(previousService);


        BluetoothGattCharacteristic softwareVerCharacteristic = new BluetoothGattCharacteristic(
          UUID.fromString(SOFTWARE_REVISION_STRING),
          BluetoothGattCharacteristic.PROPERTY_READ,
          BluetoothGattCharacteristic.PERMISSION_READ
          );

        BluetoothGattService deviceInfoService = new BluetoothGattService(
          UUID.fromString(SERVICE_DEVICE_INFORMATION),
          BluetoothGattService.SERVICE_TYPE_PRIMARY);


        softwareVerCharacteristic.setValue(new String("0.0.0").getBytes());

        deviceInfoService.addCharacteristic(softwareVerCharacteristic);
        mGattServer.addService(deviceInfoService);
 }




 public void setService(String read1Data, String read2Data, WriteCallback
   writeCallBack)
 {

  if(null == mGattServer)
   return ;

  stopAdvertise();

        final String  SERVICE_A = "0000fff0-0000-1000-8000-00805f9b34fb";
        final String  CHAR_READ1 = "0000fff1-0000-1000-8000-00805f9b34fb";
        final String  CHAR_READ2 = "0000fff2-0000-1000-8000-00805f9b34fb";
        final String  CHAR_WRITE = "0000fff3-0000-1000-8000-00805f9b34fb";
d



        BluetoothGattService previousService =
          mGattServer.getService( UUID.fromString(SERVICE_A));

     if(null != previousService)
         mGattServer.removeService(previousService);



        BluetoothGattCharacteristic read1Characteristic = new BluetoothGattCharacteristic(
          UUID.fromString(CHAR_READ1),
          BluetoothGattCharacteristic.PROPERTY_READ,
          BluetoothGattCharacteristic.PERMISSION_READ
          );

        BluetoothGattCharacteristic read2Characteristic = new BluetoothGattCharacteristic(
          UUID.fromString(CHAR_READ2),
          BluetoothGattCharacteristic.PROPERTY_READ,
          BluetoothGattCharacteristic.PERMISSION_READ
          );

        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
             UUID.fromString(CHAR_WRITE),
             BluetoothGattCharacteristic.PROPERTY_WRITE,
             BluetoothGattCharacteristic.PERMISSION_WRITE
             );




        read1Characteristic.setValue(read1Data.getBytes());
        read2Characteristic.setValue(read2Data.getBytes());
        mWriteCallback = writeCallBack;


        BluetoothGattService AService = new BluetoothGattService(
          UUID.fromString(SERVICE_A),
          BluetoothGattService.SERVICE_TYPE_PRIMARY);


        AService.addCharacteristic(read1Characteristic);
        AService.addCharacteristic(read2Characteristic);
        AService.addCharacteristic(writeCharacteristic);



        final BluetoothGattCharacteristic notifyCharacteristic = new BluetoothGattCharacteristic(
             UUID.fromString(CHAR_NOTIFY),
             BluetoothGattCharacteristic.PROPERTY_NOTIFY,
             BluetoothGattCharacteristic.PERMISSION_READ
             );


        notifyCharacteristic.setValue(new String("0"));
        AService.addCharacteristic(notifyCharacteristic);

        final Handler handler = new Handler();

        Thread thread = new Thread() {
         int i = 0;

            @Override
            public void run() {

                    while(true) {

                        try {
       sleep(1500);
      } catch (InterruptedException e) {}

                        handler.post(this);

                        List<BluetoothDevice> connectedDevices
                         = mManager.getConnectedDevices(BluetoothProfile.GATT);

                        if(null != connectedDevices)
                        {
                         notifyCharacteristic.setValue(String.valueOf(i).getBytes());

                         if(0 != connectedDevices.size())
                          mGattServer.notifyCharacteristicChanged(connectedDevices.get(0),
                            notifyCharacteristic, false);
                        }
                        i++;
                    }
            }
        };

        thread.start();


        mGattServer.addService(AService);
 }


 public void startAdvertise(String scanRespenseName)
 {
  mAdapter.setName(scanRespenseName);
        advBuilder.setIncludeDeviceName(true);

        startAdvertise();
 }

 public void startAdvertise()
 {
  if(null == mAdapter)
   return;

  if (null == mLeAdvertiser)
   mLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();

  if(null == mLeAdvertiser)
   return;

         mLeAdvertiser.startAdvertising(settingBuilder.build(),
           advBuilder.build(), mAdvCallback);
 }

 public void stopAdvertise()
 {
  if(null != mLeAdvertiser)
   mLeAdvertiser.stopAdvertising(mAdvCallback);

  mLeAdvertiser = null;
 }

}*/