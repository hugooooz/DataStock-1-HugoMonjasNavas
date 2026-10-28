package es.iesisidra.datastock.ficheros;

import java.io.File;

public class ExploradorFicheros {

    public static void explorar(String ruta) {

        try {

            if (ruta.trim().isEmpty()) {
                System.out.println("No has escrito ninguna ruta.");
                return;
            }

            File archivo = new File(ruta.trim());

            
            if (!archivo.exists()) {
                System.out.println("La ruta no existe.");
                return;
            }
            if (!archivo.canRead()) {
                System.out.println("No tienes permiso para leer esta ruta.");
                return;
            }

            if (archivo.isFile()) {
                System.out.println("Es un fichero.");
                System.out.println("Nombre: " + archivo.getName());
                System.out.println("Ruta absoluta: " + archivo.getAbsolutePath());
                System.out.println("Tamaño: " + archivo.length() + " bytes");
                return;
            }


            if (archivo.isDirectory()) {

                File[] elementos = archivo.listFiles();

                if (elementos == null) {
                    System.out.println("No se puede acceder al contenido de la carpeta.");
                    return;
                }

                if (elementos.length == 0) {
                    System.out.println("La carpeta esta vacia.");
                }

                int ficheros = 0;
                int carpetas = 0;

                for (File elemento : elementos) {

                    if (elemento.isFile()) {
                        System.out.println("[FICHERO] " + elemento.getName() + " - " + elemento.length() + " bytes");
                        ficheros++;

                    } else if (elemento.isDirectory()) {
                        System.out.println("[DIR] " + elemento.getName());
                        carpetas++;
                    }



                }

                System.out.println("Total ficheros: " + ficheros + " | Directorios: " + carpetas);
            }



        } catch (SecurityException e) {
            System.out.println("Error!!! no tienes permiso para acceder a esta ruta.");
        }

    }




}