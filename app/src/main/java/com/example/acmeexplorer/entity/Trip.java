package com.example.acmeexplorer.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Trip implements Parcelable {

    private String id;
    private String ciudad;
    private String descripcion;
    private Integer precio;
    private String urlImagen;
    private Calendar fechaInicio;
    private Calendar fechaFin;
    private Boolean seleccionado;

    public Trip(String id, String ciudad, String descripcion, Integer precio, String urlImagen, Calendar fechaInicio, Calendar fechaFin, Boolean seleccionado) {
        this.id = id;
        this.ciudad = ciudad;
        this.descripcion = descripcion;
        this.precio = precio;
        this.urlImagen = urlImagen;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.seleccionado = false;
    }

    public Trip() {
        id = "";
        ciudad = "";
        descripcion = "";
        precio = 0;
        urlImagen = "";
        fechaInicio = Calendar.getInstance();
        fechaFin = Calendar.getInstance();
        seleccionado = false;
    }

    protected Trip(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getPrecio() {
        return precio;
    }

    public void setPrecio(Integer precio) {
        this.precio = precio;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public Calendar getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Calendar fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Calendar getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Calendar fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    @Override
    public String toString() {
        DateFormat formatter = new SimpleDateFormat("dd/M/yy");
        return "Trip{" +
                "id='" + id + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", urlImagen='" + urlImagen + '\'' +
                ", fechaInicio=" + formatter.format(fechaInicio.getTime()) +
                ", fechaFin=" + formatter.format(fechaFin.getTime()) +
                ", seleccionado=" + seleccionado +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(ciudad);
        parcel.writeString(descripcion);
        if (precio == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(precio);
        }
        parcel.writeString(urlImagen);
        parcel.writeLong(fechaInicio.getTimeInMillis());
        parcel.writeLong(fechaFin.getTimeInMillis());
        parcel.writeByte((byte) (seleccionado == null ? 0 : seleccionado ? 1 : 2));
    }

    public void readFromParcel(Parcel in) {
        id = in.readString();
        ciudad = in.readString();
        descripcion = in.readString();
        if (in.readByte() == 0) {
            precio = null;
        } else {
            precio = in.readInt();
        }
        urlImagen = in.readString();
        fechaInicio = Calendar.getInstance();
        fechaInicio.setTimeInMillis(in.readLong());
        fechaFin = Calendar.getInstance();
        fechaFin.setTimeInMillis(in.readLong());
        byte tmpSeleccionado = in.readByte();
        seleccionado = tmpSeleccionado == 0 ? null : tmpSeleccionado == 1;
    }
}
