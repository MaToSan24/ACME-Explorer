package com.example.acmeexplorer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.acmeexplorer.entity.Trip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Splash extends AppCompatActivity {

    private final int DURACION = 1000;
    public ArrayList<Trip> trips = new ArrayList<>();
    List<String> thumbUrls = new ArrayList<>();
    String[] capitalesEuropeas = {"Londres", "París", "Madrid", "Berlín", "Roma", "Atenas", "Lisboa", "Ámsterdam", "Viena",
            "Dublín", "Estocolmo", "Copenhague", "Oslo", "Helsinki", "Bruselas", "Varsovia", "Praga",
            "Budapest", "Bratislava", "Liubliana", "Zagreb", "Belgrado", "Bucarest", "Sofía", "Kiev"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            String apiSecret = this.getString(R.string.API_SECRET);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.unsplash.com/search/photos?page=1&query=european%20city%20travel&orientation=landscape")
                    .addHeader("Authorization", apiSecret)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    for (int i = 0; i < 10; i++) {
                        thumbUrls.add("https://picsum.photos/300/300?random=" + i);
                    }
                    e.printStackTrace();
                    generarViajes();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject responseBody = new JSONObject(response.body().string());
                        JSONArray results = responseBody.getJSONArray("results");
                        for (int i=0; i<10; i++) {
                            String url = results.getJSONObject(i).getJSONObject("urls").getString("regular");
                            thumbUrls.add(url);
                        }
                        generarViajes();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
//            generarViajes();
        }, DURACION);
    }

    public String generarDescripcion() {
        StringBuilder builder = new StringBuilder();
        builder.append("En este viaje tendrás la oportunidad de explorar la zona, admirar los paisajes y experimentar la cultura local. ")
                .append("Ya sea que estés buscando aventura o relajación, este viaje tiene algo para todos. ")
                .append("¡No te pierdas esta oportunidad de crear recuerdos que durarán toda la vida!");
        return builder.toString();
    }

    private void generarViajes() {

        // Generate 100 random trips
        for (int i = 0; i < 10; i++) {
            final double random = Math.random();
            Trip trip = new Trip(
                    UUID.randomUUID().toString(),
                    capitalesEuropeas[(int) (random * capitalesEuropeas.length)],
//                    "Descripción del viaje a " + capitalesEuropeas[(int) (random * capitalesEuropeas.length)],
                    generarDescripcion(),
                    (int) (50 + random * 450),
                    thumbUrls.get(i),
//                    "https://picsum.photos/300/300?random=" + i,
                    Calendar.getInstance(),
                    Calendar.getInstance(),
                    false
            );
            trip.getFechaInicio().add(Calendar.DAY_OF_MONTH, (int) (7 - random * 5));
            trip.getFechaFin().add(Calendar.DAY_OF_MONTH, (int) (9 + random * 5));
            trips.add(trip);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("filterStartDate", LocalDate.now().plusDays(1).toString());
        editor.putString("filterEndDate", LocalDate.now().plusDays(14).toString());
        editor.putInt("filterMinPrice", 0);
        editor.putInt("filterMaxPrice", 1000);
        editor.apply();

        Intent intent = new Intent(Splash.this, MainActivity.class);
        intent.putParcelableArrayListExtra("Trips", trips);
        startActivity(intent);

        finish();
    }
}