package com.example.acmeexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.acmeexplorer.entity.Trip;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private ImageView imageViewViajesDisponibles, imageViewViajesSeleccionados;
    private ArrayList<Trip> trips;
    private ArrayList<Trip> tripsSeleccionados;
    private ActivityResultLauncher<Intent> launcherDisponibles;
    private ActivityResultLauncher<Intent> launcherSeleccionados;
    private TextView textViewUser;
    private MaterialButton buttonLogout;
    private CardView cardViewBorrarViajes, cardViewCargarViajes;
    private FirebaseDatabaseService firebaseDatabaseService;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabaseService = FirebaseDatabaseService.getServiceInstance();
        getTripsFromFirebase();

        imageViewViajesDisponibles = findViewById(R.id.imageViewViajesDisponibles);
        imageViewViajesSeleccionados = findViewById(R.id.imageViewViajesSeleccionados);
        cardViewBorrarViajes = findViewById(R.id.cardViewBorrarViajes);
        cardViewCargarViajes = findViewById(R.id.cardViewCargarViajes);
        textViewUser = findViewById(R.id.textViewUser);
        buttonLogout = findViewById(R.id.buttonLogout);
        Picasso.get().load("https://cdn-icons-png.flaticon.com/512/826/826070.png").placeholder(R.drawable.placeholder).resize(250, 250).into(imageViewViajesDisponibles);
        Picasso.get().load("https://cdn-icons-png.flaticon.com/512/5744/5744322.png").placeholder(R.drawable.placeholder).resize(250, 250).into(imageViewViajesSeleccionados);

        launcherDisponibles = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    getTripsFromFirebase();
                }
            }
        );

        launcherSeleccionados = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    getTripsFromFirebase();
                }
            }
        );

        cardViewBorrarViajes.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Eliminar viajes");
            builder.setMessage("¿Estás seguro de que deseas eliminar todos los viajes?");
            builder.setPositiveButton("Sí", (dialog, which) -> {
                firebaseDatabaseService.deleteAllTrips((error, ref) -> {
                    if (error != null) {
                        Toast.makeText(MainActivity.this, "Error al eliminar viajes", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(v.getContext(), "Viajes eliminados", Toast.LENGTH_SHORT).show();
                getTripsFromFirebase();
            });
            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();
        });

        cardViewCargarViajes.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Cargar viajes");
            builder.setMessage("¿Estás seguro de que deseas cargar nuevos viajes?");
            builder.setPositiveButton("Sí", (dialog, which) -> {
                trips = getIntent().getParcelableArrayListExtra("Trips");

                for (Trip trip : trips) {
                    firebaseDatabaseService.upsertTrip(trip, (error, ref) -> {
                        if (error != null) {
                            Toast.makeText(MainActivity.this, "Error al cargar viajes", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                Toast.makeText(v.getContext(), "Viajes cargados", Toast.LENGTH_SHORT).show();
                getTripsFromFirebase();
            });
            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();
        });

        textViewUser.setText("Conectado como: " + mAuth.getCurrentUser().getEmail());

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            firebaseDatabaseService.setUserId(null);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putParcelableArrayListExtra("Trips", trips);
            startActivity(intent);
            finish();
        });
    }

    public void verViajesDisponibles(View view) {
        Intent intent = new Intent(MainActivity.this, ListadoViajes.class);
        intent.putParcelableArrayListExtra("Trips", trips);
        launcherDisponibles.launch(intent);
    }

    public void verViajesSeleccionados(View view) {
        Intent intent = new Intent(MainActivity.this, ListadoViajes.class);
        intent.putParcelableArrayListExtra("Trips", tripsSeleccionados);
        intent.putExtra("Seleccionados", true);
        launcherSeleccionados.launch(intent);
    }

    public void getTripsFromFirebase() {
        firebaseDatabaseService.getTrips().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trips = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Trip trip = Trip.fromMap((Map<String, Object>) dataSnapshot.getValue());
                    trips.add(trip);
                }

                if (trips.size() > 0) {
                    cardViewBorrarViajes.setVisibility(View.VISIBLE);
                    cardViewCargarViajes.setVisibility(View.INVISIBLE);
                } else {
                    cardViewBorrarViajes.setVisibility(View.INVISIBLE);
                    cardViewCargarViajes.setVisibility(View.VISIBLE);
                }

                tripsSeleccionados = (ArrayList<Trip>) trips.stream().filter(Trip::getSeleccionado).collect(Collectors.toList());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}