package mrtrujis.Rutix;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import mrtrujis.Rutix.Objetos.Adapter;
import mrtrujis.Rutix.Objetos.rutas;

import static mrtrujis.Rutix.R.id.butttonbuscar;
import static mrtrujis.Rutix.R.id.textViewnombre;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String FIREBASE_REFERENCE = "rutas";


    RecyclerView rv;
    ArrayList<rutas> rutass;
    Adapter adapter;
    ImageButton buttonBuscar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            goLoginScreen();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.rutixtab4);
        rv = (RecyclerView) findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rutass = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(FIREBASE_REFERENCE);
        adapter = new Adapter(rutass);
        rv.setAdapter(adapter);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rutass.removeAll(rutass);
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {


                    rutas ruta1 = postSnapshot.getValue(rutas.class);
                    rutass.add(ruta1);

                }
            adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                adapter.setFilter(rutass);
                return true;
            }
        });


        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        try {
        ArrayList<rutas> listaFiltrada =filter(rutass,newText);
            adapter.setFilter(listaFiltrada);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

private ArrayList<rutas> filter(ArrayList<rutas> rutass, String texto){
ArrayList<rutas>listaFiltrada=new ArrayList<>();
    try {
        texto=texto.toLowerCase();

        for (rutas rutas: rutass){
            String rutas2 =rutas.getNombre().toLowerCase();
            if (rutas2.contains(texto)){
                listaFiltrada.add(rutas);
            }
        }

    }catch (Exception e){
        e.printStackTrace();
    }
    return listaFiltrada;
}

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        goLoginScreen();
    }

    public void miCuenta(View view) {
        Intent miCuenta = new Intent(this,MiCuenta.class);
        startActivity(miCuenta);
    }
}
