package mrtrujis.Rutix;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.BubbleIconFactory;
import com.google.maps.android.ui.IconGenerator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import mrtrujis.Rutix.Objetos.ChargeLines;
import mrtrujis.Rutix.Objetos.LastLocation;
import mrtrujis.Rutix.Objetos.Polylines;

public class Mapa extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnPolylineClickListener, GoogleMap.InfoWindowAdapter {
    private static final String TAG = Mapa.class.getName();
    private static int INTERVAL = 20000;
    private static int FAST_INTERVAL = 20000;
    private static final String RUT2 = "Ruta 23-Cedros";
    private static final String RUT4 = "Tigrebus-Mederos";
    private static final String RUTSTARBUCKS = "Ruta-Starbucks";
    private static final String PARADA_CERCANA = "Parada más cercana";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mIsLocationRequested = false;

    private GoogleMap mMap;

    private Location mLocation = null;

    Marker marker;
    Marker markerDos;
    int lastIndex;
    LatLng paradaCercana = null;
    Integer keyCoordsParada;
    Context context;
    //elimina solo los markers para volverlos a pintar, deja las polilineas en el mapa
    ArrayList<Marker> markersToClear = new ArrayList<Marker>();

    ArrayList<LatLng> poliLinea = new ArrayList<LatLng>();

    HashMap<Integer, Double> listraFiltrada = new HashMap<Integer, Double>();

    HashMap<Integer, Marker> unidades = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Bundle extras = getIntent().getExtras();
        String ruta = extras.getString("ruta").toString();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(ruta);

        }

        initGoogleApiClient();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadLocationsFromCloud();
        mGoogleApiClient.connect();
        }

    @Override
    protected void onResume() {
        super.onResume();
        //con esto lo que hacemos es revisar si ya se estan pidiendo locations y si el google api client está conectado y se reanuda el startlocationupdates()
        if (!mIsLocationRequested && mGoogleApiClient.isConnected())
            startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsLocationRequested)
            stopLocationUpdates();
    }


    //AQUI COMIENZA EL MÉTODO PARA TOMAR LOS CAMIONCITOS DE FIREBASE

    private void loadLocationsFromCloud() {

        Bundle extras = getIntent().getExtras();
        String ruta = extras.getString("ruta").toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("rutas");
        Query groupQuery = myRef.child(ruta);

        groupQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //evitar null pointer exception
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onDataChange");
                LastLocation item = dataSnapshot.getValue(LastLocation.class);
                LatLng newLatLng = new LatLng(item.getmLatitud(), item.getmLongitud());
                Integer mNumUnidad = new Integer(item.getmNumUnidad());
                Boolean status = new Boolean(item.getmActivo());
                borrarMarkers(mNumUnidad);
                if (status==true) {
                 drawMarker(newLatLng, mNumUnidad);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange");

                LastLocation item = dataSnapshot.getValue(LastLocation.class);
                LatLng newLatLng = new LatLng(item.getmLatitud(), item.getmLongitud());
                Integer mNumUnidad = new Integer(item.getmNumUnidad());
                drawMarker(newLatLng, mNumUnidad);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onDataChange");

                LastLocation item = dataSnapshot.getValue(LastLocation.class);
                LatLng newLatLng = new LatLng(item.getmLatitud(), item.getmLongitud());
                Integer mNumUnidad = new Integer(item.getmNumUnidad());
                drawMarker(newLatLng, mNumUnidad);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //AQUI TERMINA EL MÉTODO PARA CARGAR LOS CAMIONCITOS DE FB

    private void loadPolylines(){
        Bundle extras = getIntent().getExtras();
        String rutaPoli = extras.getString("ruta").toString().toLowerCase().replaceAll("-","");
        String rutaPoliDos = rutaPoli.replace(" ","");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("polilineas");
        Query groupQuery = myRef.child(rutaPoliDos);

        groupQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    ChargeLines item = dsp.getValue(ChargeLines.class);
                    LatLng newLatLngList = new LatLng(item.getmLatitud(), item.getmLongitud());
                    poliLinea.add(newLatLngList);
                }
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.addAll(poliLinea);
                polylineOptions
                        .width(10)
                        .color(Color.BLUE)
                        .geodesic(true);
                mMap.addPolyline(polylineOptions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

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
        //cuando se ejecute este metodo, hemos logrado la conexion con la Api de Google
        //una vez que se haya conectado comienza el startlocationupdates()
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
//cuando este metodo se ejecute, entonces la conexión se habrá suspendido.
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //si este metodo se dispara, quiere decir quue ha fallado el intento de conexion con el api de google
    }

    @Override
    public void onLocationChanged(Location location) {
        //este metodo viene de LocationListener, permite detectar cambios significativos en cambio de ubicacion del usuario
        Log.d(TAG, "OnLocationChanged" + String.valueOf(location.getLatitude())
                + "longitud" + String.valueOf(location.getLongitude()));
        //aqui es donde se actualizan los textview de localizacion, aqui se actualiza la ubicacion del usuario
        //aplicamos UPDATE CAMERA FACTORY para que cada vez que se mueva la ubicacion regrese a su posicion
        if (mMap != null) {
            LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Double latit = location.getLatitude();
            Double longit = location.getLongitude();

            Polylines miPolilinea = new Polylines();
            Bundle extras = getIntent().getExtras();
            String ruta = extras.getString("ruta").toString();

            if (paradaCercana == null) {
                for (int i = 0; i < poliLinea.size(); i++) {
                    Double latitDos = poliLinea.get(i).latitude;
                    Double longitDos = poliLinea.get(i).longitude;
                    LatLng newLatLngDos = new LatLng(latitDos, longitDos);
                    Double distancia = distanceHaversine(latit, latitDos, longit, longitDos);
                    if (distancia < 600) {
                        listraFiltrada.put(i, distancia);
                    }
                }

                for (int i = 0; i < listraFiltrada.size(); i++) {
                    double min = Collections.min(listraFiltrada.values());
                    Integer key = (Integer) getKeyFromValue(listraFiltrada, min);
                    keyCoordsParada = key;
                    Double latitTtres = poliLinea.get(key).latitude;
                    Double longitTtres = poliLinea.get(key).longitude;
                    LatLng newLatLngTres = new LatLng(latitTtres, longitTtres);
                    paradaCercana = newLatLngTres;
                    drawMarkerDos(newLatLngTres);
                }


                if (mLocation == null) {
                    mLocation = location;
                    lastIndex = poliLinea.size() - 1;
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(new LatLng(location.getLatitude(), location.getLongitude()));

                    builder.include(poliLinea.get(0));
                    builder.include(poliLinea.get(lastIndex));

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
            }
        }

    }



    public Double distanceHaversine(Double φ1, Double φ2, Double λ1, Double λ2) {

        final Double Δφ = (φ2 - φ1) * 0.01745329252;
        final Double Δλ = (λ2 - λ1) * 0.01745329252;
        Double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double d = 6371000 * c;
        return d;

    }

    private void startLocationUpdates() {
        //este metodo inicia utilizando el api client y el createlocationreequest a tomar la ubicacion del usaurio
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //con esta sentencia se inicia el proceso de solicitud de coordenadas de ubicacion
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mIsLocationRequested = true;

        }
        isLocationEnabled();


    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mIsLocationRequested = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Bundle extras = getIntent().getExtras();
        String ruta = extras.getString("ruta").toString();

        //este metodo se dispara cuando el mapa está listo para mostrarse
        Log.d(TAG, "OnMapReady");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        loadPolylines();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //con esta sentencia se inicia el proceso de solicitud de coordenadas de ubicacion
            mMap.setMyLocationEnabled(true);
        }

    }
    //método para dibujar polilineas, esté método toma String de ruta y con el switch pinta la ruta especificada de cada ruta
    private void dibujarPoli() {
        Bundle extras = getIntent().getExtras();
        String ruta = extras.getString("ruta").toString();
        String rutaPoli = extras.getString("ruta").toString().toLowerCase().replaceAll("-","");
        String rutaPoliDos = rutaPoli.replace(" ","");

        switch (ruta) {

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
        }
    }

    private void drawMarker(LatLng pos, final Integer unidad) {

        final String num = unidad.toString();
        final String numfinal = "u" + num;
        Bundle bundle = getIntent().getExtras();
        final String ruta = bundle.getString("ruta").toString();
        final MarkerOptions markerOptions = new MarkerOptions().position(pos);
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.rutixxx));
        markerOptions.snippet("Unidad #: " + num);
        markerOptions.title(ruta);

        //BitmapDescriptorFactory.fromResource(R.mipmap.rutixxx)
        //BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("Hola"))

        if (mMap != null) {
            marker = mMap.addMarker(markerOptions);
            unidades.put(unidad,marker);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    public void onInfoWindowClick(Marker marker) {
                        String marcadorSeleccionado = marker.getSnippet();
                        final String marcadorNumero = "u" + marcadorSeleccionado.substring(10);

                        if (marker.getTitle().toString().equals(PARADA_CERCANA)) {
                            Toast.makeText(Mapa.this, "Parada más cercana", Toast.LENGTH_SHORT).show();
                        }

                        else {
                            Intent intent = new Intent(getApplicationContext(), MapaDos.class);
                            intent.putExtra("unidad", marcadorNumero);
                            intent.putExtra("ruta", ruta);
                            intent.putExtra("keyParada", keyCoordsParada);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                return false;
            }
        });
    }

    private void drawMarkerDos(LatLng pos) {
        MarkerOptions markerOptions = new MarkerOptions().position(pos);
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.paradadebus));
        markerOptions.title("Parada más cercana");

        markerDos = mMap.addMarker(markerOptions);
        markerDos.showInfoWindow();

    }

    private void borrarMarkers(Integer unidad) {
        if (unidades.containsKey(unidad)){
            marker = unidades.get(unidad);
            marker.remove();
            unidades.remove(unidad);
        }
    }

    private void borrarMarkersDos(){

        for (Marker marker: markersToClear){
            marker.remove();
        }
        markersToClear.clear();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(marker.getTitle());

        info.addView(title);

        return info;

    }

    public void isLocationEnabled() {

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);

        }
    }

    public static Object getKeyFromValue(HashMap hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;

            }
        }
        return null;
    }


    public void onBackPressed(){
                Intent intent = new Intent(Mapa.this,MainActivity.class);
                startActivity(intent);
        Toast.makeText(Mapa.this, "BAI", Toast.LENGTH_SHORT).show();
        finish();
            }
}





