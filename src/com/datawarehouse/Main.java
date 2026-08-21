package com.datawarehouse;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    private static final String CARPETA_LOCAL = "data";

    public static void main(String[] args) {

        Session session = null;
        ChannelSftp channelSftp = null;

        try {

            System.out.println("======================================");
            System.out.println("       DESCARGADOR DE VENTAS");
            System.out.println("======================================");
            System.out.println();

            // Cargar variables de entorno
            Dotenv dotenv = Dotenv.load();

            String host = dotenv.get("HOST");
            String user = dotenv.get("USER");
            String archivoRemoto = dotenv.get("ARCHIVO_REMOTO");
            int port = Integer.parseInt(dotenv.get("PORT"));

            // Crear carpeta local
            Path carpeta = Path.of(CARPETA_LOCAL);

            if (!Files.exists(carpeta)) {
                Files.createDirectories(carpeta);
            }

            // Crear objeto JSch
            JSch jsch = new JSch();

            // Crear sesión SSH
            session = jsch.getSession(user, host, port);

            // Pedir contraseña
            System.out.print("Ingrese la contraseña de Ubuntu: ");

            Scanner scanner = new Scanner(System.in);

            String password = scanner.nextLine();

            session.setPassword(password.getBytes());

            // Evitar problemas con la primera conexión
            session.setConfig("StrictHostKeyChecking", "no");

            System.out.println();
            System.out.println("Conectando al servidor Ubuntu...");

            // Conectar por SSH
            session.connect();

            System.out.println("Conexión establecida.");
            System.out.println();

            // Crear canal SFTP
            channelSftp = (ChannelSftp) session.openChannel("sftp");

            channelSftp.connect();

            System.out.println("Iniciando descarga...");

            // Ruta local
            String archivoLocal =
                    CARPETA_LOCAL + "/ventas.csv";

            // Descargar archivo
            channelSftp.get(
                    archivoRemoto,
                    archivoLocal
            );

            System.out.println();
            System.out.println("Archivo descargado correctamente.");
            System.out.println("Ubicación:");

            System.out.println(
                    Path.of(archivoLocal)
                            .toAbsolutePath()
            );

            System.out.println();
            System.out.println("======================================");
            System.out.println("       DATOS DEL ARCHIVO CSV");
            System.out.println("======================================");

            try (BufferedReader reader = Files.newBufferedReader(Path.of(archivoLocal))) {

                String linea;

                while ((linea = reader.readLine()) != null) {
                    System.out.println(linea);
                }

            } catch (IOException e) {
                System.out.println("Error al leer el archivo CSV: " + e.getMessage());
            }

        } catch (Exception e) {

            System.out.println();
            System.out.println("ERROR durante la descarga:");
            System.out.println(e.getMessage());

        } finally {

            // Cerrar SFTP
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }

            // Cerrar SSH
            if (session != null && session.isConnected()) {
                session.disconnect();
            }

            System.out.println();
            System.out.println("Conexión cerrada.");
            System.out.println("======================================");
        }
    }
}