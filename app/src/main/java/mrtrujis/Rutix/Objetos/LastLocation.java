package mrtrujis.Rutix.Objetos;

/**
 * Created by end user on 23/07/2017.
 */

public class LastLocation {
    private Double mLatitud;
    private Double mLongitud;
    private Integer mNumUnidad;
    private Double vel;
    private Double mVelProm;
    private Boolean mActivo;

    public LastLocation(){

    }

    public LastLocation(Double lat, Double lon, Integer mNumUnidad, Double vel, Double mVelProm, Boolean mActivo){
        this.mLatitud = lat;
        this.mLongitud = lon;
        this.mNumUnidad = mNumUnidad;
        this.vel = vel;
        this.mVelProm = vel;
        this.mActivo = mActivo;
    }

    public Double getmLongitud(){ return mLongitud; }
       public void setmLongitud(Double mLongitud){this.mLongitud=mLongitud;}

    public Double getmLatitud(){return mLatitud;}
        public void setmLatitud(Double mLatitud){this.mLatitud = mLatitud; }


    public void setmNumUnidad(Integer mNumUnidad){
        this.mNumUnidad=mNumUnidad;

    }

    public Integer getmNumUnidad(){

        return mNumUnidad;
    }

    public Double getVel() {
        if (vel == null){
        return 0.0;
        }
        return vel;
    }
       public void setVel(Double vel){this.vel = vel;}

    public Double getmVelProm() {
        if (mVelProm == null){
            return 0.0;
        }
        return mVelProm;
    }

    public void setmVelProm(Double mVelProm) {
        this.mVelProm = mVelProm;
    }

    public Boolean getmActivo(){return mActivo;}
     public void setmActivo(Boolean mActivo){this.mActivo = mActivo;}

}



