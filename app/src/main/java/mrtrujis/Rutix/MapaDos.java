package mrtrujis.Rutix;

import android.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import mrtrujis.Rutix.Objetos.LastLocation;
import mrtrujis.Rutix.Objetos.Polylines;

public class MapaDos extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final String TAG = MapaDos.class.getName();
    private static int INTERVAL = 5000;
    private static int FAST_INTERVAL = 5000;
    private static final String RUT2 = "Ruta 23-Cedros";
    private static final String RUT4 = "Tigrebus-Mederos";
    private static final String RUTSTARBUCKS = "Ruta-Starbucks";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mIsLocationRequested = false;
    private GoogleMap mMap;
    private Location mLocation = null;


    Marker marker;
    Marker markerDos;
    LatLng paradaCercana = null;
    LatLng marcadorFicticio = null;
    LatLng pos;
    String rutaSeleccionada;
    int lastIndex;
    int keyCoords;
    int keyCoordsParada;
    Double distanciaAcumulada = 0.0;
    Double tiempoEta;
    float tiempoEtaRed;
    Float tiempoEtaRedFinal;
    //elimina solo los markers para dejar las polilineas
    ArrayList<Marker> markersToClear = new ArrayList<Marker>();
    //hashmap parada.
    HashMap<Integer, Double> listraFiltrada = new HashMap<Integer, Double>();
    //hashmap marcador ficticio
    HashMap<Integer, Double> listraFiltradaDos = new HashMap<Integer, Double>();

    TextView tvVal, coordsVal, textViewEta;
    Switch idSwitch;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_dos);
        Bundle extras = getIntent().getExtras();
        String ruta = extras.getString("ruta");
        String numUnidad = extras.getString("unidad");

        tvVal = (TextView) findViewById(R.id.tvVal);
        coordsVal = (TextView) findViewById(R.id.coordsVal);
        textViewEta = (TextView) findViewById(R.id.textViewEta);
        idSwitch = (Switch) findViewById(R.id.idSwitch);
        //tvVal.setText(ruta);
        textViewEta.setText("Tiempo aproximado de llegada \n" + "calculando..."); //aqui se saca el tiempo aproximado de llegada


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        initGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        loadLocationsFromCloud();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsLocationRequested && mGoogleApiClient.isConnected())
            startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsLocationRequested && mGoogleApiClient.isConnected())
            stopLocationUpdates();

    }

    //cargamos la ubicacion del camion desde firebase cada
    private void loadLocationsFromCloud() {
        Bundle extras = getIntent().getExtras();
        final String ruta = extras.getString("ruta");
        String rutaPoli = extras.getString("ruta").toString().toLowerCase().replaceAll("-","");
        final String rutaPoliDos = rutaPoli.replace(" ","");
        rutaSeleccionada = ruta;
        String num = extras.getString("unidad");
        Integer keyParadaRecibida = extras.getInt("keyParada");
        keyCoordsParada = keyParadaRecibida;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("rutas");
        Query groupQuery = myRef.child(ruta).child(num);


        groupQuery.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "OnDataChange");
                LastLocation item = dataSnapshot.getValue(LastLocation.class);
                LatLng newLatLng = new LatLng(item.getmLatitud(), item.getmLongitud());
                Integer mNumUnidad = new Integer(item.getmNumUnidad());
                //  Double velocidad = new Double(item.getVel());
                Double velProm = new Double(item.getmVelProm());
                pos = newLatLng;
                Polylines miPolilinea = new Polylines();
                //el ejemplo de ETA solo funciona con Rutacedros, falta agregarselo a tigrebus

                if (velProm > 15) {
                    switch (ruta) {
                        case RUT2:
                            for (int i = 0; i < miPolilinea.dibujarDos().size(); i++) {
                                Double latitDos = miPolilinea.dibujarDos().get(i).latitude;
                                Double longitDos = miPolilinea.dibujarDos().get(i).longitude;
                                LatLng newLatLngDos = new LatLng(latitDos, longitDos);
                                Double distancia = distanceHaversine(newLatLng.latitude, latitDos, newLatLng.longitude, longitDos);
                                if (distancia < 0.6) {
                                    listraFiltradaDos.put(i, distancia);
                                }
                            }
                            for (int i = 0; i < listraFiltradaDos.size(); i++) {
                                double min = Collections.min(listraFiltradaDos.values());
                                Integer key = (Integer) getKeyFromValue(listraFiltradaDos, min);
                                keyCoords = key;

                                Double latitTres = miPolilinea.dibujarDos().get(key).latitude;
                                Double longitTres = miPolilinea.dibujarDos().get(key).longitude;
                                LatLng newLatLngTres = new LatLng(latitTres, longitTres);
                                marcadorFicticio = newLatLngTres;
                            }

                            if (keyCoords < keyCoordsParada) {
                                distanciaAcumulada = 0.0;
                                for (int i = keyCoords; i < keyCoordsParada; i++) {
                                    Double distancia = distanceHaversine(miPolilinea.dibujarDos().get(i + 1).latitude, miPolilinea.dibujarDos().get(i).latitude, miPolilinea.dibujarDos().get(i + 1).longitude, miPolilinea.dibujarDos().get(i).longitude);
                                    distanciaAcumulada = distanciaAcumulada + distancia;
                                }
                            } else {
                                distanciaAcumulada = 0.0;
                                for (int i = keyCoords; i > keyCoordsParada; i--) {
                                    //Toast.makeText(getApplicationContext(), keyCoords+" "+keyCoordsParada+" perro :'v", Toast.LENGTH_SHORT).show();
                                    Double distancia = distanceHaversine(miPolilinea.dibujarDos().get(i - 1).latitude, miPolilinea.dibujarDos().get(i).latitude, miPolilinea.dibujarDos().get(i - 1).longitude, miPolilinea.dibujarDos().get(i).longitude);
                                    distanciaAcumulada = distanciaAcumulada + distancia;
                                }   //aqui terminar obtencion de distancia del marcador ficticio a parada mas cercana
                            }
                    }
                    tiempoEta = (distanciaAcumulada / velProm) * 60;
                    //coordsVal.setText(tiempoEta.toString());
                    tiempoEtaRed = Math.round(tiempoEta);
                    tiempoEtaRedFinal = tiempoEtaRed;
                    //coordsVal.setText(distanciaAcumulada.toString());
                    coordsVal.setText(rutaPoliDos);
                    if (paradaCercana != null && pos != null) {

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(paradaCercana);
                        builder.include(pos);
                        LatLngBounds bounds = builder.build();
                        int padding = 200;

                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        mMap.moveCamera(cu);
                        LatLng nrLatLng = mMap.getProjection().getVisibleRegion().nearRight;
                        LatLng frLatLng = mMap.getProjection().getVisibleRegion().farRight;
                        LatLng nlLatLng = mMap.getProjection().getVisibleRegion().nearLeft;
                        LatLng flLatLng = mMap.getProjection().getVisibleRegion().farLeft;

                        builder.include(nrLatLng);
                        builder.include(frLatLng);
                        builder.include(nlLatLng);
                        builder.include(flLatLng);
                        if (tiempoEta < 1.00) {
                            textViewEta.setText("Tiempo aproximado de llegada \n" + "menos de un minuto...");
                        } else {
                            textViewEta.setText("Tiempo aproximado de llegada \n" + tiempoEtaRedFinal.toString().substring(0,1) + " min");
                        }

                    }   //cada que haya un cambio en la ubicacion del camion se hará un move para ajustar el zoom del mapa

                }
                borrarMarkers();
                drawMarker(newLatLng, mNumUnidad);//una vez terminado el proceso de ETA se borra el marcador y se vuelve a pintar en la nueva ubicacion
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    //aqui termina LOADLOCATIONSFROMCLOUD

    private void initGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

            createLocationRequest();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FAST_INTERVAL);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //cuando se ha establecido una conexion con la api de google
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //este método se ejecuta si la conexion no fue exitosa
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //si se dispara este metodo quiere decir que ha fallado el intento de conexion ocn api google
    }

    @Override
    public void onLocationChanged(Location location) {
        //este metodo nos permite saber cuando se ha detectado un cambio significativo en la ubicacion
        Log.d(TAG, "onLocationChanged latitud: " + String.valueOf(location.getLatitude())
                + "longitud: " + String.valueOf(location.getLongitude()));
        if (mMap != null) {
            LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Double latit = location.getLatitude();
            Double longit = location.getLongitude();
            Polylines miPolilinea = new Polylines();
            Bundle extras = getIntent().getExtras();
            String ruta = extras.getString("ruta").toString();
            if (paradaCercana == null) {
                switch (ruta) {
                    case RUT2:
                        for (int i = 0; i < miPolilinea.dibujarDos().size(); i++) {
                            Double latitDos = miPolilinea.dibujarDos().get(i).latitude;
                            Double longitDos = miPolilinea.dibujarDos().get(i).longitude;
                            LatLng newLatLngDos = new LatLng(latitDos, longitDos);
                            Double distancia = distanceHaversine(latit, latitDos, longit, longitDos);
                            if (distancia < 0.6) {
                                listraFiltrada.put(i, distancia);
                            }
                        }

                        for (int i = 0; i < listraFiltrada.size(); i++) {
                            double min = Collections.min(listraFiltrada.values());
                            Integer key = (Integer) getKeyFromValue(listraFiltrada, min);
                            keyCoordsParada = key;

                            Double latitTtres = miPolilinea.dibujarDos().get(key).latitude;
                            Double longitTtres = miPolilinea.dibujarDos().get(key).longitude;
                            LatLng newLatLngTres = new LatLng(latitTtres, longitTtres);
                            paradaCercana = newLatLngTres;
                            drawMarkerDos(newLatLngTres);
                        }

                        if (mLocation == null) {
                            mLocation = location;
                            lastIndex = miPolilinea.dibujarDos().size() - 1;
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();

                            builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
                            builder.include(marker.getPosition());

                            int padding = 100;

                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
                            mMap.moveCamera(cameraUpdate);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

                            LatLng nrLatLng = mMap.getProjection().getVisibleRegion().nearRight;
                            LatLng frLatLng = mMap.getProjection().getVisibleRegion().farRight;
                            LatLng nlLatLng = mMap.getProjection().getVisibleRegion().nearLeft;
                            LatLng flLatLng = mMap.getProjection().getVisibleRegion().farLeft;

                            builder.include(nrLatLng);
                            builder.include(frLatLng);
                            builder.include(nlLatLng);
                            builder.include(flLatLng);

                            cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
                            mMap.animateCamera(cameraUpdate);

                        }
                        break;
                    case RUT4:
                        for (int i = 0; i < miPolilinea.tigreBus().size(); i++) {
                            Double latitDos = miPolilinea.tigreBus().get(i).latitude;
                            Double longitDos = miPolilinea.tigreBus().get(i).longitude;
                            LatLng newLatLngDos = new LatLng(latitDos, longitDos);
                            Double distancia = distanceHaversine(latit, latitDos, longit, longitDos);
                            if (distancia < 0.6) {
                                listraFiltrada.put(i, distancia);
                            }
                        }

                        for (int i = 0; i < listraFiltrada.size(); i++) {
                            double min = Collections.min(listraFiltrada.values());
                            Integer key = (Integer) getKeyFromValue(listraFiltrada, min);

                            Double latitTtres = miPolilinea.tigreBus().get(key).latitude;
                            Double longitTtres = miPolilinea.tigreBus().get(key).longitude;
                            LatLng newLatLngTres = new LatLng(latitTtres, longitTtres);
                            paradaCercana = newLatLngTres;
                            drawMarkerDos(newLatLngTres);
                        }


                        if (mLocation == null) {
                            mLocation = location;
                            lastIndex = miPolilinea.tigreBus().size() - 1;
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();

                            builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
                            builder.include(miPolilinea.tigreBus().get(0));
                            builder.include(miPolilinea.tigreBus().get(lastIndex));

                            int padding = 100;

                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
                            mMap.moveCamera(cameraUpdate);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

                            LatLng nrLatLng = mMap.getProjection().getVisibleRegion().nearRight;
                            LatLng frLatLng = mMap.getProjection().getVisibleRegion().farRight;
                            LatLng nlLatLng = mMap.getProjection().getVisibleRegion().nearLeft;
                            LatLng flLatLng = mMap.getProjection().getVisibleRegion().farLeft;

                            builder.include(nrLatLng);
                            builder.include(frLatLng);
                            builder.include(nlLatLng);
                            builder.include(flLatLng);

                            cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
                            mMap.animateCamera(cameraUpdate);
                        }

                        break;
                }
            }
        }
    }
    public Double distanceHaversine(Double φ1, Double φ2, Double λ1, Double λ2) {

        final Double Δφ = (φ2 - φ1) * 0.01745329252;
        final Double Δλ = (λ2 - λ1) * 0.01745329252;
        Double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double d = 6371 * c;
        return d;
    }

    private void startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //con este proceso iniciamos la solicitud de coordenadas de ubicacion
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
            mIsLocationRequested = true;

        }
    }

    private void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        mIsLocationRequested = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "OnMapReady");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        dibujarPoli();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //con esta sentencia se inicia el proceso de solicitud de coordenadas de ubicacion
            mMap.setMyLocationEnabled(true);
        }
    }

    //método para dibujar polilineas, esté método toma String de ruta y con el switch pinta la ruta especificada de cada ruta
    private void dibujarPoli() {
        Bundle extras = getIntent().getExtras();
        String ruta = extras.getString("ruta").toString();
        switch (ruta){
            case RUT2:
                PolylineOptions polylineOptionsDos = new PolylineOptions();
                Polylines miLineaDos = new Polylines();
                polylineOptionsDos.addAll(miLineaDos.dibujarDos());
                polylineOptionsDos
                        .width(5)
                        .color(Color.argb(95,255,0,0))
                        .geodesic(true);
                mMap.addPolyline(polylineOptionsDos);
                break;
            case RUTSTARBUCKS:
                PolylineOptions polylineOptionsStarBucks = new PolylineOptions();
                Polylines miLineaStarBucks = new Polylines();
                polylineOptionsStarBucks.addAll(miLineaStarBucks.rutaStarBucks());
                polylineOptionsStarBucks
                        .width(5)
                        .color(Color.argb(95,255,0,0))
                        .geodesic(true);
                mMap.addPolyline(polylineOptionsStarBucks);
                break;

            case RUT4:
                PolylineOptions polylineOptionsCuatro = new PolylineOptions();
                Polylines miLineaCuatro = new Polylines();
                polylineOptionsCuatro.addAll(miLineaCuatro.tigreBus());
                polylineOptionsCuatro
                        .width(5)
                        .color(Color.argb(95,255,0,0))
                        .geodesic(true);
                mMap.addPolyline(polylineOptionsCuatro);
                break;
        }
    }

    public void drawMarker(LatLng pos, Integer unidad){
        String num = unidad.toString();
        Bundle bundle = getIntent().getExtras();
        String ruta = bundle.getString("ruta").toString();
        MarkerOptions markerOptions = new MarkerOptions().position(pos);
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.rutixxx));
        markerOptions.title(ruta);
        markerOptions.snippet("Unidad #: " + num );


        if (mMap != null) {
            marker  = mMap.addMarker(markerOptions);
            marker.showInfoWindow();
            markersToClear.add(marker);
        }
    }

    private void drawMarkerDos(LatLng pos) {
        MarkerOptions markerOptions = new MarkerOptions().position(pos);
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.paradadebus));
        markerOptions.title("Parada más cercana");

        markerDos = mMap.addMarker(markerOptions);
        markerDos.showInfoWindow();

    }
    private void borrarMarkers(){
        for (Marker marker: markersToClear){
            marker.remove();
        }
        markersToClear.clear();
    }

    public void calificar(View view) {
        Bundle extras = getIntent().getExtras();
        String ruta = extras.getString("ruta");
        String numUnidad = extras.getString("unidad");
        Intent intent = new Intent(getApplicationContext(), FeedBack.class);
        intent.putExtra("ruta",ruta);
        intent.putExtra("unidad",numUnidad);
        startActivity(intent);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        goLoginScreen();
    }

    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void regresar(View view) {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("¿Deseas regresar?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(),Mapa.class);
                intent.putExtra("ruta", rutaSeleccionada);
                startActivity(intent);
                finish();
            }
        });
    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          dialogInterface.cancel();
        }
    });
      AlertDialog alert = builder.create();
        alert.show();

    }


    public static Object getKeyFromValue(HashMap hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;

            }
        }
        return null;
    }


    public void localizarCamion(View view) {
        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pos,15);
        //mMap.animateCamera(cameraUpdate);
        mMap.setTrafficEnabled(true);
    }


    public void enableTraffic(View view) {
        if (view.getId() == R.id.idSwitch){
            if (idSwitch.isChecked()){
                mMap.setTrafficEnabled(true);
            }
            else {
                mMap.setTrafficEnabled(false);
            }
        }
    }
}
