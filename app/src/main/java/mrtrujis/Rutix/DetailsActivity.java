package mrtrujis.Rutix;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Bundle extras  = getIntent().getExtras();
        if(extras != null){
            String textViewDueno = extras.getString("Dueno");
            TextView tvdueno = (TextView) findViewById(R.id.tvdueno);
            tvdueno.setText(textViewDueno);
        }
    }
}
