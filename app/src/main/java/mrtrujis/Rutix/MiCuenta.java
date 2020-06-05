package mrtrujis.Rutix;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

public class MiCuenta extends AppCompatActivity {

    TextView txtNombre;
    ImageView imagenUsuario;
    String facebookUserId = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_cuenta);
        txtNombre = (TextView) findViewById(R.id.txtNombre);
        imagenUsuario = (ImageView) findViewById(R.id.imagenUsuario);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){

            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photUrl = user.getPhotoUrl();
            String uid = user.getUid();
            txtNombre.setText(name);
            Log.d("","holi"+photUrl.toString());
        }else{
            goLoginScreen();
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.mipmap.rutixtab4);


        for (UserInfo profile: user.getProviderData()){
            if (FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())){
                facebookUserId = profile.getUid();
            }
        }


        Picasso.with(this).load("https://graph.facebook.com/" + facebookUserId + "/picture?height=500").resize(250,250).into(imagenUsuario);




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


}
