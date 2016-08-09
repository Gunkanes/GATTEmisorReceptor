package es.magtel.idi.gattemisorreceptor.actividades;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import es.magtel.idi.gattemisorreceptor.R;

/**
 * Ejemplo de emisor receptor usando el protocolo gatt de bluetooth 4.0
 *
 * El emisor emite 1 servicio con dos caracteristicas una de lectura y otra de notificación.
 *
 * Para usar la app, usar dos moviles, uno poner en emisor y otro en receptor.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v){
        switch (v.getId()) {
            case R.id.botonEmisor:
                Intent i1 = new Intent(this,EmisorActivity.class);
                startActivity(i1);
                break;
            case R.id.botonReceptor:
                Intent i2 = new Intent(this,ReceptorActivity.class);
                startActivity(i2);
                break;
            case R.id.botonPruebaOrden:
                Intent i3 = new Intent(this,PruebaOrdenesActivity.class);
                startActivity(i3);
        }
    }
}
