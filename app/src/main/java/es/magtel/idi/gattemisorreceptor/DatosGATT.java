package es.magtel.idi.gattemisorreceptor;

import java.util.HashMap;
import java.util.UUID;

/**
 * Almacena los uuid de cada servicio y característica a usar en la aplicación.
 *
 * Para informarse usar la página : https://developer.bluetooth.org/gatt/services/Pages/ServicesHome.aspx
 * Están todos los servicios con sus uuid y describe las características , indicando si son de lectura, escritura, notificación , etc.
 *
 * Created by SAMUAN on 18/07/2016.
 */
public class DatosGATT {

    //  servicios pulsera
    public static final UUID BATTERY_SERVICIO = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_CARAC_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public static final UUID HEART_RATE_SERVICIO = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    public static final UUID HEART_RATE_CARAC_MEDIDA = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb"); //  caracteristica medida ratio corazón
    public static final UUID HEART_RATE_CARAC_LOCATION = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");//  característica localización sensor cuerpo
    public static final UUID HEART_RATE_CARAC_POINT= UUID.fromString("00002A39-0000-1000-8000-00805f9b34fb");  //  caracteristica punto control.

    public static final UUID HEALTH_THERMOMETER_SERVICIO = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
    public static final UUID HEALTH_THERMOMETER_CARAC_MEDIDA = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb");  //  característica medida temperatura
    public static final UUID HEALTH_THERMOMETER_CARAC_TIPO = UUID.fromString("00002A1D-0000-1000-8000-00805f9b34fb");//  caracteristica tipo temperatura,
    public static final UUID HEALTH_THERMOMETER_CARAC_INTERMEDIA = UUID.fromString("00002A1E-0000-1000-8000-00805f9b34fb");
    public static final UUID HEALTH_THERMOMETER_CARAC_INTERVALO = UUID.fromString("00002A21-0000-1000-8000-00805f9b34fb");

    public static final UUID PODOMETRO_SERVICIO = UUID.fromString("0000A020-0000-1000-8000-00805f9b34fb");
    public static final UUID PODOMETRO_CARAC_PASOS = UUID.fromString("0000A021-0000-1000-8000-00805f9b34fb");
    public static final UUID PODOMETRO_CARAC_TIEMPO = UUID.fromString("0000A022-0000-1000-8000-00805f9b34fb");
    public static final UUID PODOMETRO_CARAC_RESET = UUID.fromString("0000A023-0000-1000-8000-00805f9b34fb");

    public static final UUID STATUS_SERVICIO = UUID.fromString("0000A000-0000-1000-8000-00805f9b34fb");
    public static final UUID STATUS_CARAC_LEER = UUID.fromString("0000A001-0000-1000-8000-00805f9b34fb");
    public static final UUID STATUS_CARAC_ESCRIBIR = UUID.fromString("0000A002-0000-1000-8000-00805f9b34fb");
    public static final UUID STATUS_CARAC_TEMP = UUID.fromString("0000A003-0000-1000-8000-00805f9b34fb");

    // servicios medallón
    public static final UUID ACELEROMETRO_SERVICIO = UUID.fromString("0000A030-0000-1000-8000-00805f9b34fb");
    public static final UUID ACELEROMETRO_CARAC_X = UUID.fromString("0000A031-0000-1000-8000-00805f9b34fb");
    public static final UUID ACELEROMETRO_CARAC_Y = UUID.fromString("0000A032-0000-1000-8000-00805f9b34fb");
    public static final UUID ACELEROMETRO_CARAC_Z = UUID.fromString("0000A033-0000-1000-8000-00805f9b34fb");

    // cccd
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static HashMap<UUID, String> mapaNombres = new HashMap<>();
    static
    {
        mapaNombres.put(PODOMETRO_SERVICIO, "Podometro" );
        mapaNombres.put(PODOMETRO_CARAC_PASOS, "Podometro pasos" );
        mapaNombres.put(PODOMETRO_CARAC_TIEMPO, "Podometro Tiempo" );
        mapaNombres.put(PODOMETRO_CARAC_RESET, "Podometro Reset");

        mapaNombres.put(ACELEROMETRO_SERVICIO, "Acelerometro");
        mapaNombres.put(ACELEROMETRO_CARAC_X, "Acelerometro x");
        mapaNombres.put(ACELEROMETRO_CARAC_Y, "Acelerometro y");
        mapaNombres.put(ACELEROMETRO_CARAC_Z, "Acelerometro z");

        mapaNombres.put(HEART_RATE_SERVICIO, "Pulsómetro");
        mapaNombres.put(HEART_RATE_CARAC_MEDIDA, "Pulsómetro medida");
        mapaNombres.put(HEART_RATE_CARAC_LOCATION, "Pulsómetro localización");
        mapaNombres.put(HEART_RATE_CARAC_POINT, "Pulsómetro punto control");

        mapaNombres.put(BATTERY_SERVICIO, "Batería");
        mapaNombres.put(BATTERY_CARAC_LEVEL, "Batería nivel");

        mapaNombres.put(HEALTH_THERMOMETER_SERVICIO, "Health thermometer");
        mapaNombres.put(HEALTH_THERMOMETER_CARAC_MEDIDA, "Health medida");
        mapaNombres.put(HEALTH_THERMOMETER_CARAC_TIPO, "Health tipo");
        mapaNombres.put(HEALTH_THERMOMETER_CARAC_INTERMEDIA, "Health intermedia");
        mapaNombres.put(HEALTH_THERMOMETER_CARAC_INTERVALO, "Health intervalo");

        mapaNombres.put(STATUS_SERVICIO, "Status");
        mapaNombres.put(STATUS_CARAC_LEER, "Status leer");
        mapaNombres.put(STATUS_CARAC_ESCRIBIR, "Status escribir");
        mapaNombres.put(STATUS_CARAC_TEMP, "Status temp");
    }

    /**
     * Para un identificador devuelve el nombre del servicio o caracteristica
     * @param uuid
     * @return
     */
    public static String dameNombre(UUID uuid){
        return mapaNombres.get(uuid);
    }

}

/***
 * Datos de servicios de la pulsera y el medallón. ASIST-E
 *
 * La conexión con la pulsera se hace usando bluetooth LE directamente sin necesidad de autenticación y se puede usar un tag nfc con el nombre y la dirección mac para que la aplicación en el móvil se conecte a los dispositivos.

 El la pulsera tenemos 5 servicios, de los cuales 3 son standard:

 Custom:

 *STATUS -UUID 0000a000   -Tiene 3 subservicios
 **RO Read Status -UUID 0000a001 -es un byte distribuido así: led azul, led verde, led rojo, evento tap (darle un golpecito), evento botón, bateria cargando, cargador conectado, batería baja.
 **WO Write Status -UUID 0000a002 -Permite cambiar los valores de los leds, limpiar los eventos de tap y de botón. Los ultimos 3 bits se ignoran.
 **RO Temperatura MCU -UUID 0000a003 -Lee la temperatura del microcontrolador, hay que dividirla por 4 para obtener el valor en grados C.

 *Podómetro  -UUID 0000A020 tiene 3 subservicios
 **RO Pasos -UUID 0000a021 - cantidad de pasos realizada, son 4 bytes.
 **RO Tiempo Caminando -UUID 0000a022 - tiempo andando en milisegundos, son 4 bytes.
 **WO Reset -UUID 0000a023 - si se escribe aquí 0x01 se reinician los contadores de pasos

 Standard:

 *Heart Rate
 *Health thermometer
 *Battery Service

 En cuanto al medallón, en este tenemos 3 servicios y ninguno es standard:

 *STATUS -UUID 0000a000:  el mismo de la pulsera
 *Podómetro  -UUID 0000A020: el mismo de la pulsera
 *Acelerómetro:  Este servicio aún no esta totalmente definido, para poderlo definir necesito información:

 he definido ya el servicio para los datos del acelerómetro:

 *Acelerómetro  -UUID 0000A030 tiene 3 subservicios
 **RO eje X -UUID 0000a031 - cantidad de pasos realizada, son 10 bytes. (5 muestras de 16 bits)
 **RO eje Y -UUID 0000a032 - cantidad de pasos realizada, son 10 bytes. (5 muestras de 16 bits)
 **RO eje Z -UUID 0000a033 - cantidad de pasos realizada, son 10 bytes. (5 muestras de 16 bits)

 Este servicio se actualiza cada medio segundo, por lo cual tenemos 10 muestras por segundo.
 También cuenta con notificación, es decir que el cliente recibe los datos cuando se actualiza el servicio.
 El fondo de escala es de -8G a 8G y cada muestra es de 16bits que se agrupan en un array así:

 muestra (t-5), muestra (t-4),muestra (t-3),muestra (t-2),muestra (t-1),muestra (t-0).


 *
 *
 * */