package mrtrujis.Rutix;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import mrtrujis.Rutix.Objetos.Feed;

public class FeedBack extends AppCompatActivity {

    RadioGroup radioGroup;
    RadioButton radioExcelente, radioBueno, radioMalo, radioButton;
    EditText editText;

    public String radio = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){

            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photUrl = user.getPhotoUrl();
            String uid = user.getUid();
        }else{
            goLoginScreen();
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.mipmap.rutixtab4);

        radioExcelente = (RadioButton) findViewById(R.id.radioExcelente);
        radioBueno = (RadioButton) findViewById(R.id.radioBueno);
        radioMalo = (RadioButton) findViewById(R.id.radioMalo);
        editText = (EditText) findViewById(R.id.editText);
    }



    private String radioChecked(){
    if (radioExcelente.isChecked()){
        radio = "Excelente";
    }
    if (radioBueno.isChecked()){
        radio = "Bueno";
    }
    if (radioMalo.isChecked()){
        radio = "Malo";
    }
      return radio;
    }

    private void sendData() {
        Bundle extras = getIntent().getExtras();
        String ruta = extras.getString("ruta");
        String num = extras.getString("unidad");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photUrl = user.getPhotoUrl();
            String uid = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("FeedBack").child(ruta).child(num);
            DatabaseReference newPostRef = ref.push();
            newPostRef.setValue(new Feed(radioChecked(),editText.getText().toString(),email, name, uid));
        } else goLoginScreen();

    }

    public void Enviar(View view) {
        sendData();
        Intent intent = new Intent(getApplicationContext(),Thanks.class);
                startActivity(intent);
                finish();


    }

    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}

