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
import org.apache.commons.net.ftp.FTP;
public class SincronizzaLibreriaSuDispenser {

    private static final String SERVER = "192.168.100.1";
    //private static final String SERVER = "192.168.1.136";
    private static final int PORT = 21;
    private static final String USERNAME = "ftp";
    private static final String PASSWORD = "ftp";

    //private static final String SERVER = "90.130.70.73";
    //private static final int PORT = 21;
    //private static final String USERNAME = "anonymous";
    //private static final String PASSWORD = "anonymous";

    public void sincronizzaLibreria(String localDirectory, String cartellaRemota) {
        boolean close_connection =false;
        // Declare and initialize the ftpClient variable
        FTPClient ftpClient = new FTPClient();

        FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        ftpClient.configure  (config);

        try {
            ftpClient.setConnectTimeout(10000); // Imposta il timeout di connessione
            ftpClient.connect(SERVER, PORT);
            ftpClient.login(USERNAME, PASSWORD);

             //ftpClient.enterLocalPassiveMode();
            ftpClient.enterLocalActiveMode();
             ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // Se si sta caricando un file binario, come ad esempio un'immagine
             ftpClient.setBufferSize(-1024);

             close_connection =true;

            // Delete the /SD_new directory if it exists
            FTPFile[] existingDirectories;
            String dirToDelete = "/sd_new";
            try {
                ftpClient.changeWorkingDirectory("/");
                existingDirectories = ftpClient.listDirectories(dirToDelete);
                // Gestisci l'elenco delle directory qui...
            } catch (IOException e) {
                e.printStackTrace();
                // Gestisci l'eccezione e prova a riconnetterti al server FTP prima di riprovare
            }
            //String dirToDelete = "/sd_new";
            //FTPFile[] existingDirectories = ftpClient.listDirectories(dirToDelete);

            if (doesDirectoryExist(ftpClient, dirToDelete))  {
                deleteDirectoryRecursively(ftpClient,dirToDelete);
                System.out.println("Directory removed: " + dirToDelete);
            }
            existingDirectories = ftpClient.listDirectories("/");
            // Recreate the /SD_new directory
            ftpClient.makeDirectory(dirToDelete);
            System.out.println("Directory created: " + dirToDelete);

            //cancello sd_old se esiste
            dirToDelete = "/sd_old";
            if (doesDirectoryExist(ftpClient, dirToDelete)) {
                deleteDirectoryRecursively(ftpClient,dirToDelete);
                System.out.println("Directory removed: " + dirToDelete);
            }
            existingDirectories = ftpClient.listDirectories("/");

            // Upload the local directory to the /SD_new directory  on the server
            uploadDirectoryRecursively(ftpClient, localDirectory, "/sd_new");

            ftpClient.changeWorkingDirectory("/");
            try {
                // Mette in pausa l'esecuzione del thread corrente per 5 secondi
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Gestisce l'eccezione
                e.printStackTrace();
            }
            existingDirectories = ftpClient.listDirectories("/");
            // Rename the /SD directory to /SD_old
            renameDirectoryIfExist(ftpClient, "/sd", "/sd_old");
            try {
                // Mette in pausa l'esecuzione del thread corrente per 5 secondi
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Gestisce l'eccezione
                e.printStackTrace();
            }
            // Rename the /SD_new directory to /sd
            renameDirectoryIfExist(ftpClient, "/sd_new", "/sd");
            try {
                // Mette in pausa l'esecuzione del thread corrente per 5 secondi
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Gestisce l'eccezione
                e.printStackTrace();
            }
            //cancello la cartella /SD_old
            deleteDirectoryRecursively(ftpClient,"/sd_old");
            System.out.println("Directory removed: /sd_old");
            existingDirectories = ftpClient.listDirectories("/");

            //sincronizzazione completata con ---- sopra e sotto
            System.out.println("---------------------------");
            System.out.println("Sincronizzazione completata");
            System.out.println("---------------------------");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("---------------------------");
            System.out.println("Sincronizzazione FTP fallita.");
            System.out.println("---------------------------");
        } finally {
            try {
                if (close_connection==true) {
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
        //imposto il trsferimento di un file x volta


        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    // Mette in pausa l'esecuzione del thread corrente per 5 secondi
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // Gestisce l'eccezione
                    e.printStackTrace();
                }
                String remoteFilePath = remotePath + "/" + file.getName();
                if (file.isFile()) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            // Mette in pausa l'esecuzione del thread corrente per 5 secondi
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            // Gestisce l'eccezione
                            e.printStackTrace();
                        }
                        try{
                            ftpClient.changeWorkingDirectory(remotePath);
                            break;
                        } catch (IOException e) {
                            System.err.println("Errore durante il cambio della directory di lavoro: " + remotePath);
                            e.printStackTrace();
                        }
                    }
                    for (int i = 0; i < 10; i++) { // Tentare l'upload fino a tre volte
                        try {
                            // Mette in pausa l'esecuzione del thread corrente per 5 secondi
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            // Gestisce l'eccezione
                            e.printStackTrace();
                        }



                            try {
                                // Mette in pausa l'esecuzione del thread corrente per 5 secondi
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                // Gestisce l'eccezione
                                e.printStackTrace();
                            }

                        try (FileInputStream fis = new FileInputStream(file)) {

                            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                            if (ftpClient.storeFile(remoteFilePath, fis)) {

                                System.out.println("File copied: " + remoteFilePath);
                                break;
                            } else {
                                try {
                                    // Mette in pausa l'esecuzione del thread corrente per 5 secondi
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    // Gestisce l'eccezione
                                    e.printStackTrace();
                                }
                                System.out.println("Failed to upload file: " + remoteFilePath);
                                ftpClient.deleteFile(remoteFilePath);
                            }

                        } catch (IOException e) {

                            System.err.println("Errore durante l'upload del file: " + remoteFilePath);
                            e.printStackTrace();
                        }
                    }
                } else if (file.isDirectory()) {
                    // Check if directory already exists on the server
                    for (int i = 0; i < 10; i++) {
                        boolean dirExists = ftpClient.changeWorkingDirectory(remoteFilePath);
                        if (!dirExists) { // If directory does not exist, create it

                            if (ftpClient.makeDirectory(remoteFilePath)) {
                                System.out.println("Directory created: " + remoteFilePath);
                            } else {
                                System.out.println("Failed to create directory: " + remoteFilePath);

                            }
                        } else break;
                    }
                    // Recursively upload the contents of the directory
                    uploadDirectoryRecursively(ftpClient, file.getAbsolutePath(), remoteFilePath);
                }
            }
        }
    }

    private boolean fileExists(FTPClient ftpClient, String filePath) throws IOException {
        FTPFile[] existingFiles = ftpClient.listFiles(filePath);
        return existingFiles != null && existingFiles.length > 0;
    }

    private boolean directoryExists(FTPClient ftpClient, String dirPath) throws IOException {
        String[] existingDirectories = ftpClient.listNames(dirPath);
        return existingDirectories != null && existingDirectories.length > 0;
    }
//--------------------------------------------------------------------------------
public boolean doesDirectoryExist(FTPClient ftpClient, String dirToCheck) throws IOException {
    FTPFile[] existingDirectories = ftpClient.listDirectories();
    if (dirToCheck.startsWith("/")) {
        dirToCheck = dirToCheck.substring(1);
    }
    String[] dirToCheckParts = dirToCheck.split("/");
    String dirToCheckName = dirToCheckParts[dirToCheckParts.length - 1];
    for (FTPFile dir : existingDirectories) {
        String dirname = dir.getName();
        if (dirname.equals(dirToCheckName)) {
            return true;
        }
    }
    return false;
}
    //--------------------------------------------------------------------------------
    public void deleteDirectoryRecursively(FTPClient ftpClient, String dirPath) throws IOException {
        boolean success = ftpClient.changeWorkingDirectory(dirPath);
        if (success) {
            FTPFile[] files = ftpClient.listFiles();
            if (files != null && files.length > 0) {
                for (FTPFile file : files) {
                    try {
                        // Mette in pausa l'esecuzione del thread corrente per 5 secondi
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // Gestisce l'eccezione
                        e.printStackTrace();
                    }
                    String fileName = file.getName();
                    if (fileName.equals(".") || fileName.equals("..")) {
                        continue; // Ignora le directory correnti e genitori
                    }
                    String path = dirPath + "/" + fileName;
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(ftpClient, path);
                    } else {
                        for (int i = 0; i < 10; i++) {
                            success = ftpClient.deleteFile(path);
                            if (!success) {
                                System.out.println("Impossibile eliminare il file: " + path);
                            }
                            else
                                break;
                        }
                    }
                }
            }
            for (int i = 0; i < 10; i++) {
                success = ftpClient.changeToParentDirectory(); // Torna alla directory genitore
                if (!success) {
                    System.out.println("Impossibile tornare alla directory genitore");
                }
                else
                    break;
            }
            for (int i = 0; i < 10; i++) {
                success = ftpClient.removeDirectory(dirPath);
                if (!success) {
                    System.out.println("Impossibile eliminare la directory: " + dirPath);
                }
                else
                    break;
            }
        } else {
            System.out.println("Impossibile cambiare la directory di lavoro a: " + dirPath);
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