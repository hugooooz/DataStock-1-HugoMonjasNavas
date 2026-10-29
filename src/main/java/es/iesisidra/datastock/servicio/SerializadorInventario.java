package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

public class SerializadorInventario {

   
    public static void guardar(Inventario inventario, String ruta) {

        if (inventario.getProductos().isEmpty()) {
            System.out.println("El inventario esta vacio.");
            return;
        }

        if (ruta.trim().isEmpty()) {
            System.out.println("No has introducido ninguna ruta.");

            return;
        }

        File archivo = new File(ruta.trim());

        if (archivo.exists() && !archivo.isFile()) {
            System.out.println("La ruta no corresponde a un fichero.");
            
            return;
        }

        File carpeta = archivo.getAbsoluteFile().getParentFile();

        if (!carpeta.exists() || !carpeta.isDirectory()) {
            System.out.println("La carpeta de destino no existe.");

            return;
        }

        
        try (ObjectOutputStream escritor = new ObjectOutputStream(new FileOutputStream(archivo))) {

            ArrayList<Producto> productos = new ArrayList<>(inventario.getProductos());
            escritor.writeObject(productos);

            System.out.println("Inventario guardado correctamente.");
            System.out.println("Ruta: " + archivo.getAbsolutePath());
            System.out.println("Productos guardados: " + productos.size());

        } catch (IOException | SecurityException e) {

            System.out.println("Error al guardar el inventario: " + e.getMessage());
        }
    }

    public static List<Producto> recuperar(String ruta) {


        if (ruta.trim().isEmpty()) {
            System.out.println("No has introducido ninguna ruta.");
            return null;
        }

        File archivo = new File(ruta.trim());

        if (!archivo.exists()) {
            System.out.println("El fichero no existe.");
            return null;
        }
        if (!archivo.isFile()) {
            System.out.println("La ruta no es un fichero.");
            return null;
        }


        if (!archivo.canRead()) {
            System.out.println("No se puede leer el fichero.");
            return null;
        }

        try (ObjectInputStream lector = new ObjectInputStream(new FileInputStream(archivo))) {

            Object datos = lector.readObject();

            if (!(datos instanceof List<?>)) {
                System.out.println("El fichero no contiene una lista valida.");

                return null;
            }

            List<?> lista = (List<?>) datos;
            ArrayList<Producto> recuperados = new ArrayList<>();

            

            for (Object objeto : lista) {

                if (!(objeto instanceof Producto)) {
                    System.out.println("El fichero contiene datos incorrectos.");

                    return null;
                }

                Producto producto = (Producto) objeto;

                if (producto.getCodigo() == null || producto.getNombre() == null || producto.getPrecio() == null) {
                    System.out.println("El fichero contiene productos incompletos.");
                    return null;
                }

                String codigo = producto.getCodigo().trim().toUpperCase();
                String nombre = producto.getNombre().trim();

                if (!codigo.matches("[A-Z0-9-]{1,12}")) {
                    System.out.println("El fichero contiene un codigo incorrecto.");
                    return null;
                }

                if (nombre.length() < 3 || nombre.length() > 100) {
                    System.out.println("El fichero contiene un nombre incorrecto.");
                    return null;
                }

                if (producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("El fichero contiene un precio incorrecto.");
                    return null;

                }

                if (producto.getStock() < 0 || producto.getCategoriaId() <= 0 || producto.getProveedorId() <= 0) {
                    System.out.println("El fichero contiene datos incorrectos.");
                    return null;
                }

              

                for (Producto anterior : recuperados) {

                    if (anterior.getCodigo().equalsIgnoreCase(codigo)) {
                        System.out.println("El fichero contiene codigos duplicados.");


                        return null;
                    }
                }

                recuperados.add(producto);


            }

            System.out.println("Inventario recuperado correctamente.");
            System.out.println("Productos recuperados: " + recuperados.size());



            return recuperados;

        } catch (IOException | ClassNotFoundException | SecurityException e) {

            System.out.println("Error al recuperar el inventario: " + e.getMessage());
            return null;
        }
    }




    
}
