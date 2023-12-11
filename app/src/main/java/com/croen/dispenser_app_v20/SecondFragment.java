package com.croen.dispenser_app_v20;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.croen.dispenser_app_v20.databinding.FragmentSecondBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.sauronsoftware.ftp4j.FTPClient;


public class SecondFragment extends Fragment {
    FTPClient client ;
    Executor executor = Executors.newSingleThreadExecutor();

    //per catturare output
    // Crea un'istanza di ByteArrayOutputStream per catturare l'output
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(byteArrayOutputStream);

    // Salva l'output standard originale
    PrintStream originalSystemOut = System.out;

    File localDirectory;
    private FragmentSecondBinding binding;
    private TextView textView;
    String text_in_wiew="";
    public int MS_Timer=0;
    //timer
    private final int interval = 1000; // 1 secondo
    private Handler handler = new Handler(Looper.getMainLooper()); // Handler associato al thread principale
    private Runnable runnable = new Runnable() {
        //questo gira di continuo ogni secondo metto tutta la logica x il download
        public void run() {

            switch (MS_Timer){
                case 0:



                    break;
                case 2:

                    // Controlla la connessione Internet
                    Button BuDownload = getView().findViewById(R.id.button_download); // Sostituisci con l'ID reale del tuo pulsante
                    if (isInternetAvailable()) {
                        BuDownload.setEnabled(true);
                    } else {
                        BuDownload.setEnabled(false);
                    }
                    break;

                case 5:
                    //showYesNoDownloadFromServer();
                    //MS_Timer = 300;
                    break;

                case 10:
                    //binding.toolbar.getMenu().findItem(R.id.action_download).setEnabled(false);
                    //binding.toolbar.getMenu().findItem(R.id.action_upload).setEnabled(false);
                    // Crea un'istanza di ExecutorService
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    SecondFragment.SincronizzazioneTask sincronizzazioneTask = new SecondFragment.SincronizzazioneTask(executorService, new sincronizzaLibreriaServerInternet());
                    Future<Void> future = sincronizzazioneTask.sincronizza();
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(() -> {
                            textView = getView().findViewById(R.id.textView2);
                            // Chiamata al metodo per scrivere il testo nella TextView
                            textView.setText(byteArrayOutputStream.toString());
                            if (future.isDone()) {
                                // Il task è completato, esegui azioni post-completamento nel thread UI principale
                                    Toast.makeText(getActivity(), "File sincronizzati dal server.", Toast.LENGTH_SHORT).show();
                                    getView().findViewById(R.id.button_download).setEnabled(true);
                                    getView().findViewById(R.id.button_second).setEnabled(true);
                                    getView().findViewById(R.id.button_upload).setEnabled(true);
                                    textView = getView().findViewById(R.id.textView2);
                                    // Chiamata al metodo per scrivere il testo nella TextView

                                    textView.setText(byteArrayOutputStream.toString());
                                    System.setOut(originalSystemOut);
                                    MS_Timer = 30;


                                // Chiudi il timer
                                timer.cancel();
                            }
                            });
                        }
                    }, 0, 500); // 500 ms di ritardo tra un controllo e l'altro
                    MS_Timer = 300;
                    break;

                //-------- aspetto un dispenser per download ---------
                case 30:
                    List<ScanResult> wifiList =getAvailableWifiList();
                    if (wifiList != null) {
                        for (android.net.wifi.ScanResult result : wifiList) {
                            // Puoi ottenere informazioni su ciascuna rete Wi-Fi, ad esempio:
                            String ssid = result.SSID;
                            String bssid = result.BSSID;
                            int signalStrength = result.level;

                            // Fai qualcosa con le informazioni, ad esempio, visualizzale in un Toast
                            String message = "SSID: " + ssid + "\nBSSID: " + bssid + "\nSegnale: " + signalStrength + " dBm";
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            MS_Timer = 300;
                        }
                    }
                    break;
            }



            // Ripeti il runnable dopo l'intervallo
            handler.postDelayed(this, interval);
        }
    };

    //----------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);

        // Inizializza il percorso della cartella
        localDirectory = new File(getActivity().getFilesDir(), "SD");
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


        // Inizia il timer
        MS_Timer = 2;
        handler.postDelayed(runnable, interval);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        text_in_wiew ="";
        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        binding.buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MS_Timer=10;
                System.setOut(printStream);
                getView().findViewById(R.id.button_download).setEnabled(false);
                getView().findViewById(R.id.button_second).setEnabled(false);
                getView().findViewById(R.id.button_upload).setEnabled(false);


            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        MS_Timer=0;
    }
    // Metodo per controllare la disponibilità della connessione Internet
    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiManager.startScan();

            return wifiManager.getScanResults();
        }
        return null;
    }

    private void showYesNoDownloadFromServer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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