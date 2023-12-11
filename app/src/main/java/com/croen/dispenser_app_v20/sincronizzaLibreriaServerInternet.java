package com.croen.dispenser_app_v20;
import com.jcraft.jsch.*;

import java.io.File;
import java.util.Vector;

public class sincronizzaLibreriaServerInternet {

    private static final String SERVER = "75.119.141.254";
    private static final int PORT = 22;
    private static final String USERNAME = "croen";
    private static final String PASSWORD = ";]/'E!5xt6Epv;rB";

    public void sincronizzaLibreria(String localDirectory, String cartellaRemota) {
        int a=0;
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;


        String cartellaLocale = localDirectory;

        try {
            // Apri una sessione SFTP
            session = jsch.getSession(USERNAME, SERVER, PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // Apri il canale SFTP
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            ricorsivaSincronizzazione(sftpChannel, cartellaRemota, cartellaLocale);
            System.out.println("OK" );
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            System.out.println("KO" );
        } finally {
            // Chiudi la connessione SFTP
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private void ricorsivaSincronizzazione(ChannelSftp sftpChannel, String cartellaRemota, String cartellaLocale) throws SftpException {
        // Cambia la directory remota
        sftpChannel.cd(cartellaRemota);

        // Ottieni un elenco di file nella directory remota
        Vector<ChannelSftp.LsEntry> filesRemoti = sftpChannel.ls(".");

        // Sincronizza i file dalla directory remota alla directory locale
        for (ChannelSftp.LsEntry entry : filesRemoti) {
            if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                // Ignora le directory speciali "." e ".."
                if (entry.getAttrs().isDir()) {
                    String nuovaCartellaLocale = cartellaLocale + "/" + entry.getFilename();
                    creaNuovaCartellaLocale(nuovaCartellaLocale);  // Crea la cartella se non esiste
                    // Se è una directory, ricorsivamente sincronizza i file al suo interno
                    ricorsivaSincronizzazione(sftpChannel, entry.getFilename(), cartellaLocale + "/" + entry.getFilename());
                } else {
                    // Se è un file, sincronizzalo solo se non esiste localmente o è diverso
                    String nomeFileRemoto = entry.getFilename();
                    String percorsoLocale = cartellaLocale + "/" + nomeFileRemoto;
                    if (!fileEsisteLocalmente(percorsoLocale) || fileDiverso(percorsoLocale, entry)) {
                        sftpChannel.get(nomeFileRemoto, percorsoLocale);
                        // Imposta la data di modifica del file locale in base alla data di modifica del file remoto
                        File fileLocale = new File(percorsoLocale);
                        fileLocale.setLastModified(entry.getAttrs().getMTime() * 1000L); // Converte da secondi a millisecondi
                        System.out.println("Sincronizzato: " + nomeFileRemoto);
                    }
                    else{
                        System.out.println("Gia Sincronizzato: " + nomeFileRemoto);

                    }
                }
            }
        }

        // Torna alla directory padre dopo aver sincronizzato tutti i file e le sottodirectory
        sftpChannel.cd("..");
    }

    private void creaNuovaCartellaLocale(String cartellaLocale) {
        File nuovaCartella = new File(cartellaLocale);
        if (!nuovaCartella.exists() && !nuovaCartella.mkdirs()) {
            throw new RuntimeException("Impossibile creare la cartella locale: " + cartellaLocale);
        }
    }


private boolean fileEsisteLocalmente(String percorsoLocale) {
        File fileLocale = new File(percorsoLocale);
        return fileLocale.exists();
    }

    private boolean fileDiverso(String percorsoLocale, ChannelSftp.LsEntry entryRemota) {
        File fileLocale = new File(percorsoLocale);

        // Verifica se il file esiste localmente
        if (!fileLocale.exists()) {
            return true; // Se il file non esiste localmente, consideralo diverso
        }

        // Verifica la data di modifica
        long dataLocale = fileLocale.lastModified();
        long dataRemota = entryRemota.getAttrs().getMTime() * 1000L; // Converte da secondi a millisecondi

        // Verifica la dimensione del file
        long dimensioneLocale = fileLocale.length();
        long dimensioneRemota = entryRemota.getAttrs().getSize();

        // Confronta data e dimensione per determinare se il file è diverso
        return (dataLocale != dataRemota || dimensioneLocale != dimensioneRemota);
    }

}
