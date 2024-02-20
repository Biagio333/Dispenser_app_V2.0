package com.croen.dispenser_app_v20;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPClientConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.IOException;
import java.util.Arrays;

public class SincronizzaLibreriaSuDispenser {

    private static final String SERVER = "192.168.100.1";
    private static final int PORT = 21;
    private static final String USERNAME = "ftp";
    private static final String PASSWORD = "ftp";

    //private static final String SERVER = "90.130.70.73";
    //private static final int PORT = 21;
    //private static final String USERNAME = "anonymous";
    //private static final String PASSWORD = "anonymous";

    public void sincronizzaLibreria(String localDirectory, String cartellaRemota) {

        // Declare and initialize the ftpClient variable
        FTPClient ftpClient = new FTPClient();

        FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        ftpClient.configure  (config);

        try {
            ftpClient.connect(SERVER, PORT);
            ftpClient.login(USERNAME, PASSWORD);

            ftpClient.enterLocalPassiveMode();


            // Delete the /SD_new directory if it exists
            String dirToDelete = "/sd_new";
            FTPFile[] existingDirectories = ftpClient.listDirectories(dirToDelete);
            if (existingDirectories != null && existingDirectories.length > 0) {
                ftpClient.removeDirectory(dirToDelete);
                System.out.println("Directory removed: " + dirToDelete);
            }

            // Recreate the /SD_new directory
            ftpClient.makeDirectory(dirToDelete);
            System.out.println("Directory created: " + dirToDelete);

            //cancello sd_old se esiste
            dirToDelete = "/sd_old";
            existingDirectories = ftpClient.listDirectories(dirToDelete);
            if (existingDirectories != null && existingDirectories.length > 0) {
                ftpClient.removeDirectory(dirToDelete);
                System.out.println("Directory removed: " + dirToDelete);
            }

            // Upload the local directory to the /SD_new directory  on the server
            uploadDirectoryRecursively(ftpClient, localDirectory, dirToDelete);

            // Rename the /SD directory to /SD_old
            renameDirectoryIfExist(ftpClient, "/sd", "/sd_old");

            // Rename the /SD_new directory to /sd
            renameDirectoryIfExist(ftpClient, "/sd_new", "/sd");
            //cancello la cartella /SD_old
            ftpClient.removeDirectory("/sd_old");
            System.out.println("Directory removed: /sd_old");

            //sincronizzazione completata con ---- sopra e sotto
            System.out.println("---------------------------");
            System.out.println("Sincronizzazione completata");
            System.out.println("---------------------------");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Sincronizzazione FTP fallita.");
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //--------------------------------------------------------------------------------
private void uploadDirectoryRecursively(FTPClient ftpClient, String localPath, String remotePath) throws IOException {
    File localDir = new File(localPath);
    File[] files = localDir.listFiles();

    if (files != null && files.length > 0) {
        for (File file : files) {
            String remoteFilePath = remotePath + "/" + file.getName();
            if (file.isFile()) {
                // Check if file already exists on the server
                FTPFile[] existingFiles = ftpClient.listFiles(remoteFilePath);
                if (existingFiles.length == 0) { // If file does not exist, upload it
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ftpClient.storeFile(remoteFilePath, fis);
                        System.out.println("File copiato: " + remoteFilePath);
                    }

                }
                else
                {
                    System.out.println("File già esistente: " + remoteFilePath);
                }
            } else if (file.isDirectory()) {
                // Check if directory already exists on the server
                String[] existingDirectories = ftpClient.listNames(remoteFilePath);
                if (existingDirectories == null || existingDirectories.length == 0) { // If directory does not exist, create it
                    ftpClient.makeDirectory(remoteFilePath);
                    System.out.println("Cartella Creata: " + remoteFilePath);
                }
                else
                {
                    System.out.println("Cartella già esistente: " + remoteFilePath);
                }
                uploadDirectoryRecursively(ftpClient, file.getAbsolutePath(), remoteFilePath);
            }
        }
    }
}
//--------------------------------------------------------------------------------
    public void renameDirectoryIfExist(FTPClient ftpClient, String oldDirName, String newDirName) throws IOException {
    // Check if old directory exists
    FTPFile[] existingDirectories = ftpClient.listDirectories(oldDirName);
    if (existingDirectories != null && existingDirectories.length > 0) {
        // Rename the directory
        boolean renamed = ftpClient.rename(oldDirName, newDirName);
        if (renamed) {
            System.out.println("Directory renamed from: " + oldDirName + " to: " + newDirName);
        } else {
            System.out.println("Failed to rename directory: " + oldDirName);
        }
    } else {
        System.out.println("Directory does not exist: " + oldDirName);
    }
}
}