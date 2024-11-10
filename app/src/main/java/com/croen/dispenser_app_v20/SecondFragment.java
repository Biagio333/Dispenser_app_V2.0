package com.croen.dispenser_app_v20;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
//import android.net.ConnectivityManager;
//import android.net.Network;
//import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
//import android.net.wifi.ScanResult;
//import android.net.wifi.WifiManager;

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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;

//import android.provider.Settings;
import android.content.Intent;
import android.net.Uri;
import java.net.InetAddress;

import java.io.IOException;

import android.net.wifi.WifiInfo;


import java.net.URL;
import java.net.HttpURLConnection;

import android.content.Context;
import android.os.PowerManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.net.ftp.FTPReply;



public class SecondFragment extends Fragment {
    FTPClient client;
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
    String text_in_wiew = "";
    public int MS_Timer = 0;

    public boolean Request_select_lan_hotspot_dispenser = false;
    //timer
    private final int interval = 1000; // 1 secondo
    private Handler handler = new Handler(Looper.getMainLooper()); // Handler associato al thread principale

    private boolean InternetAvailable = false;
    private boolean DispenserAvailable = false;
    private int counter_rescan_internet = 0;

    boolean state_ip = false;
    boolean Ping_copleted = false;
    int ping_Number =0;
    private String IP_Dispenser = "192.168.100.1";
    private String IP_Internet  = "75.119.141.254";
    //public WifiManager wifiManager;
    private PowerManager.WakeLock wakeLock;


    Runnable runnable = new Runnable() {
        //questo gira di continuo ogni secondo metto tutta la logica x il download
        @Override
        public void run() {

            switch (MS_Timer) {
                case 0:

                    if (ping_Number == 0){
                        ping_Number =1;
                        // Esegui isReachable in un thread separato
                        new Thread(() -> {
                            MS_Timer=2;
                            state_ip = isReachable(IP_Dispenser);
                            // Usa Handler per aggiornare il thread principale
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Log.d("cip", String.valueOf(state_ip));
                                Ping_copleted=true;
                                try {
                                    if (state_ip == true) {
                                        DispenserAvailable = true;

                                        getView().findViewById(R.id.button_upload).setEnabled(true);
                                        getView().findViewById(R.id.button_update_firmware).setEnabled(true);
                                    } else {
                                        DispenserAvailable = false;
                                        getView().findViewById(R.id.button_upload).setEnabled(false);
                                        getView().findViewById(R.id.button_update_firmware).setEnabled(false);


                                    }
                                }
                                catch (NullPointerException ex){

                                }
                            });
                        }).start();
                    }
                    else{
                        ping_Number =0;
                        new Thread(() -> {
                            MS_Timer=2;
                            state_ip = isReachable(IP_Internet);
                            // Usa Handler per aggiornare il thread principale
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Log.d("cip", String.valueOf(state_ip));
                                Ping_copleted=true;
                                try{
                                    if (state_ip==true){
                                        getView().findViewById(R.id.button_download).setEnabled(true);

                                    }
                                    else{
                                        getView().findViewById(R.id.button_download).setEnabled(false);
                                    }
                                }
                                catch (NullPointerException ex){

                                }

                            });
                        }).start();
                    }
                    break;

                case 1:
                    Request_select_lan_hotspot_dispenser = false;
                   // InternetAvailable=MyisInternetAvailable();



 /*                   if (wifiManager != null) {
                        wifiManager.startScan();

                        List<ScanResult> scanResults = wifiManager.getScanResults();
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid2 = "";
                        if (wifiInfo != null) {
                            ssid2 = wifiInfo.getSSID(); // Get the SSID of the currently connected WiFi network
                        }

                        if (ssid2.equals("\"Dispenser2HotSpot\""))
                        {
                            DispenserAvailable=true;
                            getView().findViewById(R.id.button_download).setEnabled(false);
                        }
                        else {
                            DispenserAvailable=false;
                            MS_Timer = 2;
                        }
                        if (DispenserAvailable) {
                            getView().findViewById(R.id.button_upload).setEnabled(true);
                            getView().findViewById(R.id.button_update_firmware).setEnabled(true);
                        } else {
                            getView().findViewById(R.id.button_upload).setEnabled(false);
                            getView().findViewById(R.id.button_update_firmware).setEnabled(false);
                        }
                    }*/

                    break;

                case 2:
                    if ( Ping_copleted == true){
                        Ping_copleted=false;
                        MS_Timer=0;
                    }

                    break;


                case 5:
                    //showYesNoDownloadFromServer();
                    //MS_Timer = 300;
                    break;
                case 10:


           /*         if (wifiManager != null) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        if (wifiInfo != null) {
                            disconnectFromWifiNetwork();
                            MS_Timer = 12;
                        }
                    }*/
                    MS_Timer = 12;
                    break;

                case 12:

                    //binding.toolbar.getMenu().findItem(R.id.action_download).setEnabled(false);
                    //binding.toolbar.getMenu().findItem(R.id.action_upload).setEnabled(false);
                    // Crea un'istanza di ExecutorService
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    SecondFragment.SincronizzazioneTask sincronizzazioneTask = new SecondFragment.SincronizzazioneTask(executorService, new sincronizzaLibreriaServerInternet());
                    Future<Void> future = sincronizzazioneTask.sincronizza();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(() -> {
                                textView = getView().findViewById(R.id.textView2);
                                // Chiamata al metodo per scrivere il testo nella TextView
                                textView.setText(byteArrayOutputStream.toString());
                                if (future.isDone()) {
                                    // Il task è completato, esegui azioni post-completamento nel thread UI principale
                                    Toast.makeText(getActivity(), "File sincronizzati dal server.", Toast.LENGTH_SHORT).show();
                                    getView().findViewById(R.id.button_download).setEnabled(false);
                                    getView().findViewById(R.id.button_second).setEnabled(false);
                                    getView().findViewById(R.id.button_upload).setEnabled(false);
                                    getView().findViewById(R.id.button_update_firmware).setEnabled(false);
                                    textView = getView().findViewById(R.id.textView2);
                                    // Chiamata al metodo per scrivere il testo nella TextView

                                    textView.setText(byteArrayOutputStream.toString());
                                    System.setOut(originalSystemOut);
                                    MS_Timer = 0;


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
                        MS_Timer = 40;

                    break;
                //---------- Mi sono collegato al dispenser hotspot ------------
                case 40:
                    // Crea un'istanza di ExecutorService
                    ExecutorService executorService_disp = Executors.newSingleThreadExecutor();
                    SecondFragment.SincronizzazioneTask_esp32 sincronizzazioneTask_disp = new SecondFragment.SincronizzazioneTask_esp32(executorService_disp, new SincronizzaLibreriaSuDispenser());
                    Future<Void> future_disp = sincronizzazioneTask_disp.sincronizza();
                    Timer timer_disp = new Timer();
                    timer_disp.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(() -> {
                                textView = getView().findViewById(R.id.textView2);
                                // Chiamata al metodo per scrivere il testo nella TextView
                                textView.setText(byteArrayOutputStream.toString());
                                if (future_disp.isDone()) {
                                    // Il task è completato, esegui azioni post-completamento nel thread UI principale
                                    Toast.makeText(getActivity(), "File sincronizzati dal server.", Toast.LENGTH_SHORT).show();
                                    getView().findViewById(R.id.button_download).setEnabled(false);
                                    getView().findViewById(R.id.button_second).setEnabled(false);
                                    getView().findViewById(R.id.button_upload).setEnabled(false);
                                    getView().findViewById(R.id.button_update_firmware).setEnabled(false);
                                    textView = getView().findViewById(R.id.textView2);
                                    // Chiamata al metodo per scrivere il testo nella TextView

                                    textView.setText(byteArrayOutputStream.toString());
                                    System.setOut(originalSystemOut);
                                    MS_Timer = 0;
                                    // Chiudi il timer
                                    timer_disp.cancel();
                                    // disconnectFromWifiNetwork() ;

                                }
                            });
                        }
                    }, 0, 500); // 500 ms di ritardo tra un controllo e l'altro
                    MS_Timer = 300;
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
        MS_Timer = 0;
        handler.postDelayed(runnable, interval);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getContext())) {
        //        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
        //        startActivityForResult(intent, 200);
        //    }

       /* wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);*/
        PowerManager powerManager = (PowerManager) requireActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp::MyWakelockTag");
        wakeLock.acquire(); // Mantiene il dispositivo sveglio



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
                getView().findViewById(R.id.button_update_firmware).setEnabled(false);
                textView = getView().findViewById(R.id.textView2);
                textView.setText("Download in corso...");

            }

        });

        binding.buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MS_Timer=30;
                System.setOut(printStream);
                getView().findViewById(R.id.button_download).setEnabled(false);
                getView().findViewById(R.id.button_second).setEnabled(false);
                getView().findViewById(R.id.button_upload).setEnabled(false);
                getView().findViewById(R.id.button_update_firmware).setEnabled(false);
                textView = getView().findViewById(R.id.textView2);
                textView.setText("UpLoad in corso...");


            }
        });

        binding.buttonUpdateFirmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MS_Timer=300;
                System.setOut(printStream);
                getView().findViewById(R.id.button_download).setEnabled(false);
                getView().findViewById(R.id.button_second).setEnabled(false);
                getView().findViewById(R.id.button_upload).setEnabled(false);
                getView().findViewById(R.id.button_update_firmware).setEnabled(false);
                textView = getView().findViewById(R.id.textView2);
                textView.setText("UpLoad firmware in corso...");


                //new Thread(new Runnable() {
                //    @Override
                //    public void run() {
                //        String serverURL = "http://192.168.100.1/update";
                //        File file  = new File(getActivity().getFilesDir(), "firmware.bin");
                //        uploadFile(serverURL, file);
                //    }
                //}).start();

                new Thread(() -> {
                    // Esegui l'upload del file
                    String uploadUrl = "http://192.168.100.1/update";
                    File file  = new File(getActivity().getFilesDir(), "firmware.bin");
                    uploadFile(uploadUrl, file);
                    // Usa Handler per aggiornare il thread principale
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Log.d("cip", "Fine tread update firmware");
                        textView.setText("UpLoad firmware Completed");
                        MS_Timer=0;
                    });

                }).start();

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        MS_Timer=0;
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release(); // Rilascia il WakeLock quando il fragment viene distrutto
        }
    }





    //-------------- classe per tred conessione server esp32  ----------
    private class SincronizzazioneTask_esp32 {

        private final ExecutorService executorService;
        private final SincronizzaLibreriaSuDispenser sincronizzatore;

        public SincronizzazioneTask_esp32(ExecutorService executorService, SincronizzaLibreriaSuDispenser sincronizzatore) {
            this.executorService = executorService;
            this.sincronizzatore = sincronizzatore;
        }

        public Future<Void> sincronizza() {
            Callable<Void> callable = () -> {
                // Chiamare il metodo di sincronizzazione della tua classe
                sincronizzatore.sincronizzaLibreria(localDirectory.getAbsolutePath(),"/");
                return null;
            };

            return executorService.submit(callable);
        }
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


/*public boolean MyisInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }*/

 /*   @RequiresApi(api = Build.VERSION_CODES.Q)
    public void connectToWifiNetwork(String ssid, String password) {
        WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build();

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);
            }
        };

        connectivityManager.requestNetwork(networkRequest, networkCallback);
    }*/

    public static void uploadFile(String uploadUrl, File file) {
        String boundary = "===" + System.currentTimeMillis() + "===";
        String LINE_FEED = "\r\n";
        String TWO_HYPHENS = "--";

        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        FileInputStream fileInputStream = null;

        try {
            // Apri una connessione al server
            URL url = new URL(uploadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setConnectTimeout(10000); // 10 secondi di timeout per la connessione
            connection.setReadTimeout(15000);    // 15 secondi di timeout per la lettura


            // Ottieni l'output stream della connessione
            outputStream = new DataOutputStream(connection.getOutputStream());

            // Inizia la parte del form data
            outputStream.writeBytes(TWO_HYPHENS + boundary + LINE_FEED);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"update\"; filename=\"" + file.getName() + "\"" + LINE_FEED);
            outputStream.writeBytes("Content-Type: " + HttpURLConnection.guessContentTypeFromName(file.getName()) + LINE_FEED);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + LINE_FEED);
            outputStream.writeBytes(LINE_FEED);

            // Leggi il file e scrivilo nell'output stream
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);

            }

            // Chiudi la parte del form data
            outputStream.writeBytes(LINE_FEED);
            outputStream.writeBytes(TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_FEED);

            // Ottieni la risposta dal server
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("File uploaded successfully!");
            } else {
                System.out.println("Error uploading file: " + responseCode);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Chiudi risorse
            try {
                if (outputStream != null) outputStream.close();
                if (fileInputStream != null) fileInputStream.close();
                if (connection != null) connection.disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isReachable(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            boolean ris =inetAddress.isReachable(1000); // timeout in millisecondi
            return ris;
        } catch (IOException e) {
            int a=0;
            e.printStackTrace();
            return false;
        }
    }

 /*   public void disconnectFromWifiNetwork() {
    ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivityManager != null) {
        connectivityManager.bindProcessToNetwork(null);
    }
}*/
}