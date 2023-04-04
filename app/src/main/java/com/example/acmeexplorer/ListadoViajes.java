package com.example.acmeexplorer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.acmeexplorer.entity.Trip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ListadoViajes extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Trip> trips;
    private ArrayList<Trip> filteredTrips;
    private ActivityResultLauncher<Intent> launcherDetalleViaje;
    private Intent intentDetalleViaje;
    private Integer selectedPosition;
    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    LocalDate filterStartDate;
    LocalDate filterEndDate;
    Integer filterMinPrice;
    Integer filterMaxPrice;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_viajes);

        trips = getIntent().getParcelableArrayListExtra("Trips");
        filteredTrips = new ArrayList<>(trips);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        filterStartDate = LocalDate.parse(sharedPreferences.getString("filterStartDate", LocalDate.now().plusDays(1).toString()));
        filterEndDate = LocalDate.parse(sharedPreferences.getString("filterEndDate", LocalDate.now().plusDays(14).toString()));
        filterMinPrice = sharedPreferences.getInt("filterMinPrice", 0);
        filterMaxPrice = sharedPreferences.getInt("filterMaxPrice", 1000);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        ViajesAdapter adapter = new ViajesAdapter(this, filteredTrips);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        launcherDetalleViaje = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Trip viajeSeleccionado = result.getData().getParcelableExtra("viajeSeleccionado");
                        filteredTrips.set(selectedPosition, viajeSeleccionado);
                        Integer tripIndex = trips.stream().filter(t -> t.getId().equals(viajeSeleccionado.getId())).findFirst().map(trips::indexOf).get();
                        trips.set(tripIndex, viajeSeleccionado);
                        adapter.notifyDataSetChanged(); // Actualiza la vista del elemento seleccionado
                    }
                }
        );

        RecyclerView.OnItemTouchListener selector4ListGrid = new RecyclerView.OnItemTouchListener() {
            private static final int SCROLL_THRESHOLD = 10; // You can adjust this value to control how much movement is allowed before it's considered a scroll
            private float startX, startY, endX, endY;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = e.getX();
                        startY = e.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        endX = e.getX();
                        endY = e.getY();
                        float dx = endX - startX;
                        float dy = endY - startY;
                        if (Math.abs(dx) < SCROLL_THRESHOLD && Math.abs(dy) < SCROLL_THRESHOLD) {
                            View view = rv.findChildViewUnder(e.getX(), e.getY());
                            if (view != null) {
                                int position = rv.getChildAdapterPosition(view);
                                selectedPosition = position; // Almacena la posición del elemento seleccionado
                                intentDetalleViaje = new Intent(getApplicationContext(), TripDetails.class);
                                intentDetalleViaje.putExtra("Trip", filteredTrips.get(position));
                                launcherDetalleViaje.launch(intentDetalleViaje);
                                return true; // Return true to consume the event and prevent scrolling
                            }
                        }
                    break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        };

        recyclerView.addOnItemTouchListener(selector4ListGrid);
        recyclerView.setNestedScrollingEnabled(false);

        Intent intent = new Intent();
        intent.putExtra("Trips", trips);
        setResult(Activity.RESULT_OK, intent);

        FloatingActionButton fab = findViewById(R.id.fabFilter);
        fab.setOnClickListener(view -> showFilterDialog(adapter));
    }

    private void showFilterDialog(ViajesAdapter adapter) {
        // AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflar la vista de diálogo desde un archivo de diseño personalizado
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter_trips, null);
        builder.setView(dialogView);

        // Obtener referencias a los elementos de la vista de diálogo
        DatePicker startDateDatePicker = dialogView.findViewById(R.id.datePickerFechaInicio);
        DatePicker endDateDatePicker = dialogView.findViewById(R.id.datePickerFechaFin);
        EditText minPriceEditText = dialogView.findViewById(R.id.editTextPrecioMin);
        EditText maxPriceEditText = dialogView.findViewById(R.id.editTextPrecioMax);

        // Configurar los valores iniciales de los campos de entrada
        startDateDatePicker.updateDate(filterStartDate.getYear(), filterStartDate.getMonthValue() - 1, filterStartDate.getDayOfMonth());
        endDateDatePicker.updateDate(filterEndDate.getYear(), filterEndDate.getMonthValue() - 1, filterEndDate.getDayOfMonth());
        minPriceEditText.setText(String.valueOf(filterMinPrice));
        maxPriceEditText.setText(String.valueOf(filterMaxPrice));

        // Configurar los botones de diálogo
        builder.setPositiveButton("Aplicar", (dialog, which) -> {

            // Obtener los valores ingresados en los campos de entrada

            try {
                filterStartDate = formatter.parse(startDateDatePicker.getDayOfMonth() + "/" + (startDateDatePicker.getMonth() + 1) + "/" + startDateDatePicker.getYear()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                filterEndDate = formatter.parse(endDateDatePicker.getDayOfMonth() + "/" + (endDateDatePicker.getMonth() + 1) + "/" + endDateDatePicker.getYear()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            filterMinPrice = Integer.parseInt(minPriceEditText.getText().toString().equals("") ? "0" : minPriceEditText.getText().toString());
            filterMaxPrice = Integer.parseInt(maxPriceEditText.getText().toString().equals("") ? String.valueOf(Integer.MAX_VALUE) : maxPriceEditText.getText().toString());

            if (!errorsExist(filterStartDate, filterEndDate, filterMinPrice, filterMaxPrice)) {
                // Aplicar el filtro a la lista de viajes
                filteredTrips = filterTrips(trips, filterStartDate, filterEndDate, filterMinPrice, filterMaxPrice);

                // Actualizar la lista de viajes mostrada en la pantalla
                adapter.setTrips(filteredTrips);

                // Guardar los valores ingresados en los campos de entrada
                editor.putString("filterStartDate", filterStartDate.toString());
                editor.putString("filterEndDate", filterEndDate.toString());
                editor.putInt("filterMinPrice", filterMinPrice);
                editor.putInt("filterMaxPrice", filterMaxPrice);
                editor.apply();

                Toast.makeText(this, "Filtro aplicado", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Limpiar", (DialogInterface.OnClickListener) (dialog, which) -> {
            // Limpiar los valores ingresados en los campos de entrada
            filterStartDate = LocalDate.now().plusDays(1);
            filterEndDate = LocalDate.now().plusDays(14);
            filterMinPrice = 0;
            filterMaxPrice = 1000;

            // Restablecer la lista de viajes original en la pantalla
            filteredTrips = new ArrayList<>(trips);
            adapter.setTrips(filteredTrips);

            // Guardar los valores por defecto en los campos de entrada
            editor.putString("filterStartDate", filterStartDate.toString());
            editor.putString("filterEndDate", filterEndDate.toString());
            editor.putInt("filterMinPrice", filterMinPrice);
            editor.putInt("filterMaxPrice", filterMaxPrice);
            editor.apply();

            Toast.makeText(this, "Filtro limpiado", Toast.LENGTH_SHORT).show();
        });

        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean errorsExist(LocalDate fechaInicio, LocalDate fechaFin, Integer precioMin, Integer precioMax) {
        if (precioMin > precioMax) {
            Toast.makeText(this, "El precio mínimo no puede ser mayor al precio máximo", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            Toast.makeText(this, "La fecha de inicio no puede ser posterior a la fecha de fin", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (fechaInicio.isBefore(LocalDate.now())) {
            Toast.makeText(this, "La fecha de inicio no puede ser anterior a la fecha actual", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (fechaFin.isBefore(LocalDate.now())) {
            Toast.makeText(this, "La fecha de fin no puede ser anterior a la fecha actual", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private ArrayList<Trip> filterTrips(ArrayList<Trip> trips, LocalDate fechaInicio, LocalDate fechaFin, Integer precioMin, Integer precioMax) {
        ArrayList<Trip> filteredTrips = new ArrayList<>();

        for (Trip trip : trips) {
            boolean fechaInicioOk = true;
            boolean fechaFinOk = true;
            boolean precioOk = true;

            if (fechaInicio != null && trip.getFechaInicio().toInstant().truncatedTo(ChronoUnit.DAYS).compareTo(fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.DAYS)) < 0) {
                fechaInicioOk = false;
            }

            if (fechaFin != null && trip.getFechaFin().toInstant().truncatedTo(ChronoUnit.DAYS).compareTo(fechaFin.atStartOfDay(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.DAYS)) > 0) {
                fechaFinOk = false;
            }

            if (fechaFin != null && trip.getFechaFin().toInstant().truncatedTo(ChronoUnit.DAYS).compareTo(fechaFin.atStartOfDay(ZoneId.systemDefault()).toInstant()) > 0) {
                fechaFinOk = false;
            }

            if (precioMin != null && trip.getPrecio() < precioMin) {
                precioOk = false;
            }

            if (precioMax != null && trip.getPrecio() > precioMax) {
                precioOk = false;
            }

            if (fechaInicioOk && fechaFinOk && precioOk) {
                filteredTrips.add(trip);
            }
        }

        return filteredTrips;
    }
}

class ViajesAdapter extends RecyclerView.Adapter<ViajesAdapter.ViajesViewHolder> {

    private ArrayList<Trip> trips;
    private LayoutInflater inflater;
    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


    public ViajesAdapter(Context context, ArrayList<Trip> trips) {
        inflater = LayoutInflater.from(context);
        this.trips = trips;
    }

    @NonNull
    @Override
    public ViajesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.trip_item, parent, false);
        return new ViajesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViajesViewHolder holder, int position) {
        Trip currentTrip = trips.get(position);
        holder.textViewCiudad.setText(currentTrip.getCiudad());
        holder.textViewPrecio.setText(currentTrip.getPrecio().toString() + "€");
        holder.textViewFechaInicio.setText("Ida: " + formatter.format(currentTrip.getFechaInicio().getTime()));
        holder.textViewFechaFin.setText("Vuelta: " + formatter.format(currentTrip.getFechaFin().getTime()));
        Picasso.get().load(currentTrip.getUrlImagen()).resize(250, 250).placeholder(R.drawable.placeholder).into(holder.imageViewFoto);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public void setTrips(ArrayList<Trip> trips) {
        this.trips = trips;
        notifyDataSetChanged();
    }

    public class ViajesViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewCiudad;
        public TextView textViewPrecio;
        public TextView textViewFechaInicio;
        public TextView textViewFechaFin;
        public ImageView imageViewFoto;

        public ViajesViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCiudad = itemView.findViewById(R.id.textViewCiudad);
            textViewPrecio = itemView.findViewById(R.id.textViewPrecio);
            textViewFechaInicio = itemView.findViewById(R.id.textViewFechaInicio);
            textViewFechaFin = itemView.findViewById(R.id.textViewFechaFin);
            imageViewFoto = itemView.findViewById(R.id.imageViewFoto);
        }
    }
}

