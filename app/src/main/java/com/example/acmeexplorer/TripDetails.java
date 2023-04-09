package com.example.acmeexplorer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.acmeexplorer.entity.Trip;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

public class TripDetails extends AppCompatActivity {

    Trip trip;
    ImageView imageViewImagen, imageViewSeleccionar;
    TextView textViewCiudad, textViewPrecio, textViewDuracion, textViewDescripcion;
    MaterialButton materialButtonComprar;
    Animation fadeInAnimation, fadeOutAnimation, scaleAnimation;
    FirebaseDatabaseService firebaseDatabaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_details);

        trip = getIntent().getParcelableExtra("Trip");
        firebaseDatabaseService = FirebaseDatabaseService.getServiceInstance();

        imageViewImagen = findViewById(R.id.imageViewImagen);
        imageViewSeleccionar = findViewById(R.id.imageViewSeleccionar);
        textViewCiudad = findViewById(R.id.textViewCiudad);
        textViewPrecio = findViewById(R.id.textViewPrecio);
        textViewDuracion = findViewById(R.id.textViewDuracion);
        textViewDescripcion = findViewById(R.id.textViewDescripcion);
        materialButtonComprar = findViewById(R.id.materialButtonComprar);

        Button buttonMaps = findViewById(R.id.buttonMaps);
        buttonMaps.setOnClickListener(view -> {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("Trip", trip);
            startActivity(intent);
        });

        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);

        materialButtonComprar.setVisibility(trip.getSeleccionado() ? View.VISIBLE : View.GONE);

        materialButtonComprar.setOnClickListener(view -> {
            Snackbar.make(view, "¡Compra realizada!", Snackbar.LENGTH_LONG).show();
        });

        textViewCiudad.setText("Viaje a " + trip.getCiudad());
        textViewPrecio.setText("Precio: " + trip.getPrecio() + "€");
        textViewDescripcion.setText("Descripción: " + trip.getDescripcion());
        imageViewSeleccionar.setImageResource(trip.getSeleccionado() ? R.drawable.selected : R.drawable.notselected);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        textViewDuracion.setText("Del " + sdf.format(trip.getFechaInicio().getTime()) + " al " + sdf.format(trip.getFechaFin().getTime()));

        Picasso.get().load(trip.getUrlImagen()).placeholder(R.drawable.placeholder).into(imageViewImagen);

        imageViewSeleccionar.setOnClickListener(view -> {
            trip.setSeleccionado(!trip.getSeleccionado());

            materialButtonComprar.setVisibility(trip.getSeleccionado() ? View.VISIBLE : View.GONE);
            materialButtonComprar.startAnimation(trip.getSeleccionado() ? fadeInAnimation : fadeOutAnimation);

            firebaseDatabaseService.upsertTrip(trip, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    Snackbar.make(view, "¡Error al guardar el viaje!", Snackbar.LENGTH_SHORT).show();
                }
            });

            if (trip.getSeleccionado()) {
                imageViewSeleccionar.startAnimation(fadeInAnimation);
                imageViewSeleccionar.setImageResource(R.drawable.selected);
                Snackbar.make(view, "¡Viaje seleccionado!", Snackbar.LENGTH_SHORT).show();
            } else {
                imageViewSeleccionar.setImageResource(R.drawable.notselected);
                Snackbar.make(view, "¡Viaje deseleccionado!", Snackbar.LENGTH_SHORT ).show();
            }

            Intent resultadoIntent = new Intent();
            resultadoIntent.putExtra("viajeSeleccionado", trip);
            setResult(Activity.RESULT_OK, resultadoIntent);
        });

        imageViewSeleccionar.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                imageViewSeleccionar.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                imageViewSeleccionar.invalidate();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                imageViewSeleccionar.setColorFilter(0x00000000, PorterDuff.Mode.SRC_ATOP);
                imageViewSeleccionar.invalidate();
            }
            return false;
        });

    }
}