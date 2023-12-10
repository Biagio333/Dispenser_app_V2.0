package com.croen.dispenser_app_v20;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.croen.dispenser_app_v20.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.sauronsoftware.ftp4j.FTPClient;


public class MainActivity extends AppCompatActivity {
    private static final int CODICE_PERMESSO_INTERNET = 1;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    FTPClient client ;
    Executor executor = Executors.newSingleThreadExecutor();

    File localDirectory;
    public int MS_Timer=0;
    //timer
    private final int interval = 1000; // 1 secondo
    private Handler handler = new Handler(Looper.getMainLooper()); // Handler associato al thread principale
    private Runnable runnable = new Runnable() {
        //questo gira di continuo ogni secondo metto tutta la logica x il download
        public void run() {

            switch (MS_Timer){
                case 0:
                    // Controlla la connessione Internet
                    if (isInternetAvailable()) {
                        binding.toolbar.getMenu().findItem(R.id.action_download).setEnabled(true);

                    } else {
                        binding.toolbar.getMenu().findItem(R.id.action_download).setEnabled(false);
                    }
                    break;

                case 2:
                    // Controlla la connessione Internet
                    if (isInternetAvailable()) {
                        Toast.makeText(MainActivity.this, "Connessione Internet disponibile sincronizza file", Toast.LENGTH_SHORT).show();
                        MS_Timer = 5;
                    } else {
                        Toast.makeText(MainActivity.this, "Connessione Internet non disponibile", Toast.LENGTH_SHORT).show();
                        //ceck c'e' internet dispenser
                    }
                break;

                case 5:
                    showYesNoDownloadFromServer();
                    MS_Timer = 300;
                    break;

                case 10:
                    binding.toolbar.getMenu().findItem(R.id.action_download).setEnabled(false);
                    binding.toolbar.getMenu().findItem(R.id.action_upload).setEnabled(false);
                    // Crea un'istanza di ExecutorService
                    ExecutorService executorService = Executors.newSingleThreadExecutor();

                    SincronizzazioneTask sincronizzazioneTask = new SincronizzazioneTask(executorService, new sincronizzaLibreriaServerInternet());


                    Future<Void> future = sincronizzazioneTask.sincronizza();

                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (future.isDone()) {
                                // Il task è completato, esegui azioni post-completamento nel thread UI principale
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "File sincronizzati dal server.", Toast.LENGTH_SHORT).show();
                                    binding.toolbar.getMenu().findItem(R.id.action_download).setEnabled(true);
                                    binding.toolbar.getMenu().findItem(R.id.action_upload).setEnabled(true);
                                    MS_Timer = 30;
                                });

                                // Chiudi il timer
                                timer.cancel();
                            }
                        }
                    }, 0, 500); // 500 ms di ritardo tra un controllo e l'altro
                    MS_Timer = 300;
                    break;

                    //-------- aspetto un dispenser per download ---------
                case 30:
                    List<android.net.wifi.ScanResult> wifiList =getAvailableWifiList();
                    if (wifiList != null) {
                        for (android.net.wifi.ScanResult result : wifiList) {
                            // Puoi ottenere informazioni su ciascuna rete Wi-Fi, ad esempio:
                            String ssid = result.SSID;
                            String bssid = result.BSSID;
                            int signalStrength = result.level;

                            // Fai qualcosa con le informazioni, ad esempio, visualizzale in un Toast
                            String message = "SSID: " + ssid + "\nBSSID: " + bssid + "\nSegnale: " + signalStrength + " dBm";
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            MS_Timer = 300;
                        }
                    }
                    break;
            }



            // Ripeti il runnable dopo l'intervallo
            handler.postDelayed(this, interval);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Verifica se il permesso è già stato concesso
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            // Il permesso è già stato concesso, esegui l'azione che richiede il permesso
            // ...
        } else {
            // Richiedi il permesso
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, CODICE_PERMESSO_INTERNET);
        }
        // Inizializza il percorso della cartella
        localDirectory = new File(getFilesDir(), "SD");
        // Crea la cartella se non esiste
        if (!localDirectory.exists()) {
            boolean success = localDirectory.mkdirs();
            if (success) {
                Log.d("MainActivity", "Cartella creata con successo: " + localDirectory.getAbsolutePath());
            } else {
                Log.e("MainActivity", "Impossibile creare la cartella: " + localDirectory.getAbsolutePath());
            }
        } else {
            Log.d("MainActivity", "La cartella esiste già: " + localDirectory.getAbsolutePath());
        }
        // Richiedi il permesso a tempo di esecuzione
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODICE_PERMESSO_INTERNET);
        // Inizia il timer
        handler.postDelayed(runnable, interval);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_download) {
            MS_Timer =10;
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_upload) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Metodo per controllare la disponibilità della connessione Internet
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return isNetworkAvailable(connectivityManager);
            } else {
                // Versioni precedenti a Marshmallow
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isNetworkAvailable(ConnectivityManager connectivityManager) {
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    //-------------- classe per tred conessione server remoto internet  ----------
    private class SincronizzazioneTask {

        private final ExecutorService executorService;
        private final sincronizzaLibreriaServerInternet sincronizzatore;

        public SincronizzazioneTask(ExecutorService executorService, sincronizzaLibreriaServerInternet sincronizzatore) {
            this.executorService = executorService;
            this.sincronizzatore = sincronizzatore;
        }

        public Future<Void> sincronizza() {
            Callable<Void> callable = () -> {
                // Chiamare il metodo di sincronizzazione della tua classe
                sincronizzatore.sincronizzaLibreria(localDirectory.getAbsolutePath(),"/home/croen/SD");
                return null;
            };

            return executorService.submit(callable);
        }
    }

    private List<android.net.wifi.ScanResult> getAvailableWifiList() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiManager.startScan();

            return wifiManager.getScanResults();
        }
        return null;
    }

    private void showYesNoDownloadFromServer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Internet avaible");
        builder.setMessage("Internet is avaible, do you wont download File from server ?");

        // Pulsante "Sì"
        builder.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Azioni da eseguire quando l'utente clicca su "Sì"
                MS_Timer =10; //scarico dal server
                dialog.dismiss(); // Chiudi il dialogo se necessario
            }
        });

        // Pulsante "No"
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Azioni da eseguire quando l'utente clicca su "No"
                MS_Timer =30; //faccio scansioni rete
                dialog.dismiss(); // Chiudi il dialogo se necessario
            }
        });

        // Mostra il dialogo
        builder.show();
    }
}