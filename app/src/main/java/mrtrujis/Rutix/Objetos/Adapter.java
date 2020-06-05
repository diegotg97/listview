package mrtrujis.Rutix.Objetos;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mrtrujis.Rutix.DetailsActivity;
import mrtrujis.Rutix.MainActivity;
import mrtrujis.Rutix.Mapa;
import mrtrujis.Rutix.R;

import static mrtrujis.Rutix.R.id.layoutruta;
import static mrtrujis.Rutix.R.id.textViewnombre;

/**
 * Created by end user on 08/07/2017.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.CochesviewHolder> implements View.OnClickListener {

    List<rutas> rutass;

    public Adapter(ArrayList<rutas> rutass) {
        this.rutass = rutass;
    }

    @Override
    public CochesviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_recycler, parent, false);
        CochesviewHolder holder = new CochesviewHolder(v);

        v.setOnClickListener(this);
        return holder;

    }

    @Override
    public void onBindViewHolder(CochesviewHolder holder, int position) {
        rutas rutas = rutass.get(position);
        holder.textViewnombre.setText(rutas.getNombre());


        //eventos al dar click
        holder.setOnClickcListeners();

    }

    @Override
    public int getItemCount() {
        return rutass.size();

    }



    @Override
    public void onClick(View view) {

    }



    public static class CochesviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //contexto
        Context context;
        TextView textViewnombre;
        //boton
        ImageButton butttonbuscar;
        View layoutruta;


        public CochesviewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            textViewnombre = (TextView) itemView.findViewById(R.id.textViewnombre);
            butttonbuscar = (ImageButton) itemView.findViewById(R.id.butttonbuscar);
            layoutruta = itemView.findViewById(R.id.layoutruta);

        }

        void setOnClickcListeners() {
            butttonbuscar.setOnClickListener(this);
            textViewnombre.setOnClickListener(this);
            layoutruta.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.butttonbuscar:
                    Intent intent = new Intent(context, Mapa.class);
                    intent.putExtra("ruta",textViewnombre.getText());
                    context.startActivity(intent);
                    break;
                case R.id.textViewnombre:
                    Intent intentDos = new Intent(context, Mapa.class);
                    intentDos.putExtra("ruta",textViewnombre.getText());
                    context.startActivity(intentDos);
                    break;
                case R.id.layoutruta:
                    Intent intentTres = new Intent(context, Mapa.class);
                    intentTres.putExtra("ruta",textViewnombre.getText());
                    context.startActivity(intentTres);
                    break;

            }
        }

    }


    public void setFilter(ArrayList<rutas> rutass){
        this.rutass=new ArrayList<>();
        this.rutass.addAll(rutass);
        notifyDataSetChanged();

    }
}
