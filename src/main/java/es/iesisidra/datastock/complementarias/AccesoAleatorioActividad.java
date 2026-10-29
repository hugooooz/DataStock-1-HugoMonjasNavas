//rf 08


package es.iesisidra.datastock.complementarias;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.ImportadorJSON;
import es.iesisidra.datastock.servicio.ImportadorXML;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.LectorConsola;

public class AccesoAleatorioActividad {

    private static final int TAMANO = 36;
    private static final String RUTA = "datos/salidas/stock.bin";


    public static void main(String[] args) {

        Inventario inventario = new Inventario();
        System.out.println("====== ACCESO ALEATORIO =====");
        System.out.println("1. Cargar productos desde JSON");
        System.out.println("2. Cargar productos desde XML");
        System.out.println("0. No cargar productos");
        int carga = LectorConsola.leerOpcion("Opcion: ", 0, 2);



        if (carga == 1) {

            String ruta = LectorConsola.leerTexto("Ruta JSON: ");
            ImportadorJSON.importar(ruta, inventario);

        }

        if (carga == 2) {


            String ruta = LectorConsola.leerTexto("Ruta XML: ");
            ImportadorXML.importar(ruta, inventario);
        }

        int opcion = -1;
        while (opcion != 0) {

            System.out.println("======= MENU ACCESO ALEATORIO =====");
            System.out.println("1. Generar stock.bin");
            System.out.println("2. Consultar registro por posicion");
            System.out.println("3. Modiicar stock por posicion");
            System.out.println("4. Mostrar todos los registros");
            System.out.println("0. salir");
            opcion = LectorConsola.leerOpcion("Opcion: ", 0, 4);



            switch (opcion) {

                case 1:
                    generar(inventario);
                    break;

                case 2:
                    consultar();
                    break;

                case 3:
                    modificar();
                    break;

                case 4:
                    mostrarTodos();
                    break;

                case 0:
                    System.out.println("Saliendo del programa......:)");
                    break;
            }


        }
    }


    private static void generar(Inventario inventario) {


        if (inventario.getProductos().isEmpty()) {
            System.out.println("El inventario esta vacio.");
            return;
        }

        File archivo = new File(RUTA);
        File carpeta = archivo.getAbsoluteFile().getParentFile();


        if (!carpeta.exists() || !carpeta.isDirectory()) {
            System.out.println("La carpeta de destino no existe");

            return;
        }

        if (archivo.exists()) {

            if (!archivo.isFile()) {
                System.out.println("La ruta no corresponde a un fichero");
                return;
            }

            boolean confirmar = LectorConsola.confirmar("El fichero ya existe, sobrescribir");


            if (!confirmar) {
                System.out.println("Operacion cancelada");
                return;
            }

        }

        ArrayList<Producto> productos = new ArrayList<>(inventario.getProductos());
        productos.sort(Comparator.comparing(Producto::getCodigo));



        try (RandomAccessFile fichero =new RandomAccessFile(archivo, "rw")) {

           
            fichero.setLength(0);
            for (Producto producto : productos) {

                String codigo = producto.getCodigo();
                while (codigo.length() < 12) {
                    codigo = codigo + " ";

                }

               
                fichero.writeChars(codigo);
                fichero.writeDouble(producto.getPrecio().doubleValue());
                fichero.writeInt(producto.getStock());
            }

            System.out.println("Fichero generado correctamente.");
            System.out.println(" Ruta: " + archivo.getAbsolutePath());
            System.out.println("Registros: " + productos.size());
            System.out.println("Tamano: " + fichero.length() + " bytes");

        } catch (IOException | SecurityException e) {
            System.out.println("Error al generar el fichero: " + e.getMessage());
        }
    }



    private static boolean comprobarFichero() {

        File archivo = new File(RUTA);

        if (!archivo.exists() || !archivo.isFile() || !archivo.canRead()) {
            System.out.println("El fichero no existe o no se puede leer.");


            return false;
        }

        if (archivo.length() % TAMANO != 0) {
            System.out.println("Fichero invalido: tamano incorrecto.");
            return false;
        }

        return true;
    }

    
    private static void leerRegistro(RandomAccessFile fichero)throws IOException {

        String codigo = "";
        for (int i = 0; i < 12; i++) {
            codigo =  codigo + fichero.readChar();

        }

        double precio = fichero.readDouble();
        int stock = fichero.readInt();

        System.out.println("Codigo: " + codigo.trim());
        System.out.println("Precio: " + precio + " euros");
        System.out.println("Stock:" + stock);
        System.out.println("----------------------------");
    }

  
    private static void consultar() {

        if (!comprobarFichero()) {
            return;
        }

        try (RandomAccessFile fichero = new RandomAccessFile(RUTA, "r")) {

            long total = fichero.length() / TAMANO;
            System.out.println("Registros: " + total);
            int posicion = LectorConsola.leerEntero("Posicion: ");


            if (posicion < 0 || posicion >= total) {
                System.out.println("Posicion fuera de rango.");

                return;
            }

          
            fichero.seek((long) posicion * TAMANO);
            leerRegistro(fichero);

        } catch (IOException | SecurityException e) {
            System.out.println("Error al consultar: " + e.getMessage());
        }


    }

    
    private static void modificar() {

        if (!comprobarFichero()) {
            return;
        }

        try (RandomAccessFile fichero=new RandomAccessFile(RUTA, "rw")) {

            long total = fichero.length() / TAMANO;
            System.out.println("Registros: " + total);
            int posicion = LectorConsola.leerEntero("Posicion: ");


            if (posicion < 0 || posicion >= total) {
                System.out.println("Posicion fuera de rango.");
                return;
            }

            
            long lugarStock = (long) posicion * TAMANO + 32;
            fichero.seek(lugarStock);
            int stockAnterior = fichero.readInt();

            System.out.println("Stock actual: " + stockAnterior);
            int stockNuevo = LectorConsola.leerEntero("Nuevo stock: ");



            if (stockNuevo < 0) {
                System.out.println("El stock no puede ser negativo");

                return;
            }

            
            fichero.seek(lugarStock);
            fichero.writeInt(stockNuevo);

            System.out.println("Stock actualizado: " + stockAnterior + " el nuevo: " + stockNuevo);

        } catch (IOException | SecurityException e) {

            System.out.println("Error al modificar: " + e.getMessage());
        }

    }

    
    private static void mostrarTodos() {

        if (!comprobarFichero()) {
            return;
        }

        try (RandomAccessFile fichero = new RandomAccessFile(RUTA, "r")) {

            long total = fichero.length() / TAMANO;
            System.out.print("Total registros: " + total);


            if (total == 0) {
                System.out.println("El fichero esta vacio!!");

            }

            for (int i = 0; i < total; i++) {

                System.out.println("Posicion: " + i);
                leerRegistro(fichero);
            }

        } catch (IOException | SecurityException e) {
            System.out.println("Error al mostrar registros " + e.getMessage());
        }


    }

}
