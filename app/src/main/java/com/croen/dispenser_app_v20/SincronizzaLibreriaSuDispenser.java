package com.croen.dispenser_app_v20;


import com.jcraft.jsch.*;

import java.io.File;
import java.util.Vector;

public class SincronizzaLibreriaSuDispenser {

    private static final String SERVER = "192.168.100.1";
    private static final int PORT = 21;
    private static final String USERNAME = "ftp";
    private static final String PASSWORD = "ftp";

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


            System.out.println("OK" );
        } catch (JSchException  e) {
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


}
