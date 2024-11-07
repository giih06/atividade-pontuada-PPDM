package com.example.google_maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    // Request Code para ser utilizado no gerenciamento das permissões
    private static final int REQUEST_LOCATION_UPDATES = 1;

    // Objetos da API de localização
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private GoogleMap mMap;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(this);

        // Iniciar o fragmento do mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configuração da solicitação de localização
        mLocationRequest = new LocationRequest.Builder(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // Alta precisão
                .build();

        // Callback de localização
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    updateLocation(locationResult.getLastLocation());
                }
            }
        };

        // Inicializando o FusedLocationProviderClient
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Habilita a localização no mapa
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true); // Para exibir a localização no mapa
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_UPDATES);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.map_button) {
            mapButton();
        }
    }

    public void mapButton() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Inicia as atualizações de localização
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } else {
            // Solicita permissão se necessário
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_UPDATES);
        }
    }

    private void updateLocation(Location location) {
        if (mMap != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

            // Atualizar ou criar marcador
            if (mMarker == null) {
                mMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title("Sua localização"));
            } else {
                mMarker.setPosition(userLocation);
            }

            float currentZoom = 15.0f; // Nível de zoom inicial

            // Configurar o bearing (direção) e mover a câmera
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(userLocation)  // Centraliza na nova localização
                    .zoom(currentZoom)     // Mantém o nível de zoom
                    .bearing(location.getBearing()) // Define a direção do usuário
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_UPDATES) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapButton();
            } else {
                Toast.makeText(this, "Sem permissão para mostrar atualizações da sua localização", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
