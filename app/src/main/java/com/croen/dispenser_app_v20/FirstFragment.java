package com.croen.dispenser_app_v20;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;



import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.croen.dispenser_app_v20.databinding.FragmentFirstBinding;

import android.webkit.WebSettings;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.OutputStream;
import java.io.PrintStream;
import it.sauronsoftware.ftp4j.FTPClient;
import java.io.File;


public class FirstFragment extends Fragment {



    private FragmentFirstBinding binding;
    private WebView webView;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        webView = rootView.findViewById(R.id.webView_1);
        // Abilita l'esecuzione di JavaScript (se necessario)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Carica l'URL desiderato
        webView.loadUrl("https://croen.org");


        //aggiorno cartella dal server ftp



        return rootView;

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}