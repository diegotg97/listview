package mrtrujis.Rutix.Objetos;

/**
 * Created by end user on 28/12/2017.
 */

public class ChargeLines {

    private Double mLatitud;
    private Double mLongitud;

    public ChargeLines(){

    }

    public ChargeLines(Double mLatitud,Double mLongitud){
        this.mLatitud = mLatitud;
        this.mLongitud = mLongitud;

    }

    public Double getmLatitud() {
        return mLatitud;
    }

    public void setmLatitud(Double mLatitud) {
        this.mLatitud = mLatitud;
    }

    public Double getmLongitud() {
        return mLongitud;
    }

    public void setmLongitud(Double mLongitud) {
        this.mLongitud = mLongitud;
    }
}
