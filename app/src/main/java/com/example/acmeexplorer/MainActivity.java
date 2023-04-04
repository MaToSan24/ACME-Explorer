package com.example.acmeexplorer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;

import com.example.acmeexplorer.entity.Trip;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private ImageView imageViewViajesDisponibles, imageViewViajesSeleccionados;
    private ArrayList<Trip> trips;
    private ArrayList<Trip> tripsSeleccionados;
    private ActivityResultLauncher<Intent> launcherDisponibles;
    private ActivityResultLauncher<Intent> launcherSeleccionados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewViajesDisponibles = findViewById(R.id.imageViewViajesDisponibles);
        imageViewViajesSeleccionados = findViewById(R.id.imageViewViajesSeleccionados);
        Picasso.get().load("https://cdn-icons-png.flaticon.com/512/826/826070.png").placeholder(R.drawable.placeholder).resize(250, 250).into(imageViewViajesDisponibles);
        Picasso.get().load("https://cdn-icons-png.flaticon.com/512/5744/5744322.png").placeholder(R.drawable.placeholder).resize(250, 250).into(imageViewViajesSeleccionados);

        trips = getIntent().getParcelableArrayListExtra("Trips");
        tripsSeleccionados = new ArrayList<>();

        launcherDisponibles = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    trips = result.getData().getParcelableArrayListExtra("Trips");
                    tripsSeleccionados = (ArrayList<Trip>) trips.stream().filter(Trip::getSeleccionado).collect(Collectors.toList());
                }
            }
        );

        launcherSeleccionados = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    ArrayList<Trip> tripsSeleccionadosModificados = result.getData().getParcelableArrayListExtra("Trips");
                    for (Trip trip : tripsSeleccionadosModificados) {
                        if (!trip.getSeleccionado()) {
                            tripsSeleccionados.remove(tripsSeleccionados.indexOf(tripsSeleccionados.stream().filter(t -> t.getId().equals(trip.getId())).findFirst().get()));
                            trips.stream().filter(t -> t.getId().equals(trip.getId())).findFirst().get().setSeleccionado(false);
                        }
                    }
                }
            }
        );
    }

    public void verViajesDisponibles(View view) {
        Intent intent = new Intent(MainActivity.this, ListadoViajes.class);
        intent.putParcelableArrayListExtra("Trips", trips);
        launcherDisponibles.launch(intent);
    }

    public void verViajesSeleccionados(View view) {
        Intent intent = new Intent(MainActivity.this, ListadoViajes.class);
        intent.putParcelableArrayListExtra("Trips", tripsSeleccionados);
        launcherSeleccionados.launch(intent);
    }
}