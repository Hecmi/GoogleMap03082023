package com.example.googlemap03082023;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.googlemap03082023.WebService.Asynchtask;
import com.example.googlemap03082023.WebService.WebService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, Asynchtask {
    GoogleMap mapa;
    PolylineOptions lineas;
    ArrayList<MarkerOptions> marcadores;
    Double suma_distancia = 0.00;
    TextView txtDistancia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        txtDistancia = (TextView)findViewById(R.id.txtDistancia);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;

        //Cambiar tipo de mapa
        mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mapa.getUiSettings().setZoomControlsEnabled(true);

        //Mover el mapa a una ubicaciòn
        CameraUpdate camUpd1 =CameraUpdateFactory
                        .newLatLngZoom(new LatLng(40.6898, -74.0448), 18);
        mapa.moveCamera(camUpd1);

        /*LatLng madrid = new LatLng(40.417325, -3.683081);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(madrid)
                .zoom(19)
                .bearing(25) //noreste arriba
                .tilt(70) //punto de vista de la cámara 70 grados
                .build();
        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);
        mapa.animateCamera(camUpd3);*/

        marcadores = new ArrayList<>();
        mapa.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        LatLng punto = new LatLng(latLng.latitude, latLng.longitude);
        MarkerOptions marcador = new MarkerOptions();
        marcador.position(latLng);
        marcador.title("Punto");

        mapa.addMarker(marcador);
        marcadores.add(marcador);

        if(marcadores.size() == 6){
            PolylineOptions lineas = new PolylineOptions();

            for (int i = 0; i < marcadores.size(); i++){
                lineas.add(marcadores.get(i).getPosition());

                //Validaciones:
                //El punto actual debe tomar el punto siguiente si es posible, caso contrario
                //regresa a la posición inicial.

                if (i + 1 < marcadores.size() - 1) {
                    ejcutar_ws_latlng(marcadores.get(i).getPosition(), marcadores.get(i + 1).getPosition());
                }
                else{
                    ejcutar_ws_latlng(marcadores.get(i).getPosition(), marcadores.get(0).getPosition());
                }
            }

            lineas.add(marcadores.get(0).getPosition());

            lineas.width(8);
            lineas.color(Color.RED);
            mapa.addPolyline(lineas);
            marcadores.clear();
        }
    }

    private void ejcutar_ws_latlng(LatLng lat_lon_origen, LatLng lat_lon_destino){
        String origen = "origins=" + lat_lon_origen.latitude + "," +  lat_lon_origen.longitude;
        String destinos = "destinations=" + lat_lon_destino.latitude + "%2C" + lat_lon_destino.longitude;

        Map<String, String> datos = new HashMap<String, String>();
        WebService ws= new WebService("https://maps.googleapis.com/maps/api/distancematrix/json?units=meters&"
                + origen + "&"
                + destinos + "&key=AIzaSyDMmRXHBYOjJyXZruXemR11tl7uiJ2T_Q8",
                datos, MainActivity.this, MainActivity.this);
        ws.execute("GET");
    }

    @Override
    public void processFinish(String result) throws JSONException {
        JSONObject jObject = new JSONObject(result);
        JSONArray jArrayFila = jObject.getJSONArray("rows");
        JSONObject jObjectElementos = jArrayFila.getJSONObject(0);

        JSONArray jArrayElemento = jObjectElementos.getJSONArray("elements");
        for (int i = 0; i < jArrayElemento.length(); i++){
            JSONObject jObjectDistancia = jArrayElemento.getJSONObject(i);
            JSONObject jObjectValue = jObjectDistancia.getJSONObject("distance");

            suma_distancia += Double.parseDouble(jObjectValue.getString("value"));
        }

        txtDistancia.setText("La distancia entre los puntos es: " + suma_distancia.toString() + " metros");
        Log.i("RESULTADO_FN", suma_distancia.toString());
        //Toast.makeText(this.getApplicationContext(), "RESULTADO "+ suma_distancia, Toast.LENGTH_SHORT).show();
    }
}