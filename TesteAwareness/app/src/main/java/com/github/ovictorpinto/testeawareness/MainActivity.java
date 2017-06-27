package com.github.ovictorpinto.testeawareness;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {
    
    private static final int PERMISSION_FINE_LOCATION = 200;
    private static final String TAG = "MainFenda";
    private static final String FENCE_RECEIVER_ACTION = "acaoFendaEncontrada";
    private PendingIntent myPendingIntent;
    private MyFenceReceiver myFenceReceiver;
    private GoogleApiClient mGoogleApiClient;
    public static final String FANCE_NAME = "NomeDaFence";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Awareness.API).build();
        mGoogleApiClient.connect();
        
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        myPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        myFenceReceiver = new MyFenceReceiver();
        registerReceiver(myFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myFenceReceiver);
    }
    
    private void criaFenda() {
        Log.d(TAG, "Criando fenda");
        double lat = -20.319537;
        double lon = -40.333316;
        AwarenessFence localtionFence = LocationFence.entering(lat, lon, 100);
        
        // Register the fence to receive callbacks.
        // The fence key uniquely identifies the fence.
        final ResultCallback<Status> resultCallback = new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    Log.i(TAG, "Fence was successfully registered.");
                } else {
                    Log.e(TAG, "Fence could not be registered: " + status);
                }
            }
        };
        final FenceUpdateRequest builder = new FenceUpdateRequest.Builder().addFence(FANCE_NAME, localtionFence, myPendingIntent).build();
        Awareness.FenceApi.updateFences(mGoogleApiClient, builder).setResultCallback(resultCallback);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_FINE_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            criaFenda();
        }
    }
    
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "Conectou googleApi");
        // Create a fence.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        } else {
            criaFenda();
        }
        
    }
    
    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Supendeu conexao googleApi");
    }
    
    // Handle the callback on the Intent.
    public static class MyFenceReceiver extends BroadcastReceiver {
        private static final String TAG = "MyFencerReceive";
        
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "Fenda encontrada!!!!");
            FenceState fenceState = FenceState.extract(intent);
            Log.e(TAG, fenceState.getFenceKey());
            
            if (TextUtils.equals(fenceState.getFenceKey(), FANCE_NAME)) {
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        Log.i(TAG, "Headphones are plugged in.");
                        break;
                    case FenceState.FALSE:
                        Log.i(TAG, "Headphones are NOT plugged in.");
                        break;
                    case FenceState.UNKNOWN:
                        Log.i(TAG, "The headphone fence is in an unknown state.");
                        break;
                }
            }
        }
    }
}
