package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import es.iesisidra.datastock.modelo.Producto;



public class ImportadorJSON {

    public static void importar(String ruta, Inventario inventario) {


        if (ruta.trim().isEmpty()) {
            System.out.println("No has introducido ninguna ruta.");
            return;
        }

        File archivo = new File(ruta.trim());

        if (!archivo.exists()) {
            System.out.println("El fichero no existe.");
            return;
        }


        if (!archivo.isFile()) {
            System.out.println("La ruta no es un fichero.");
            return;
        }



        if (!archivo.canRead()) {
            System.out.println("No se puede leer el fichero.");
            return;
        }



        try (FileReader lector = new FileReader(archivo)) {

            JsonElement contenido = JsonParser.parseReader(lector);
            if (!contenido.isJsonObject()) {
                System.out.println("El JSON no tiene una estructura correcta.");
                return;
            }

            JsonObject json = contenido.getAsJsonObject();
            JsonElement lista = json.get("productos");

            if (lista == null || !lista.isJsonArray()) {
                System.out.println("El JSON no contiene una lista de productos valida.");
                return;
            }



            JsonArray productos = lista.getAsJsonArray();
            int importados = 0;
            int rechazados = 0;
            System.out.println("Productos encontrados: " + productos.size());



            for (int i = 0; i < productos.size(); i++) {


                try {

                    if (!productos.get(i).isJsonObject()) {
                        throw new IllegalArgumentException("No es un producto valido.");
                    }

                    JsonObject producto = productos.get(i).getAsJsonObject();
                    String codigo = leerCampo(producto, "codigo").trim().toUpperCase();
                    String nombre = leerCampo(producto, "nombre").trim();
                    BigDecimal precio = new BigDecimal(leerCampo(producto, "precio"));


                    int stock = new BigDecimal(leerCampo(producto, "stock")).intValueExact();
                    int categoriaId = new BigDecimal(leerCampo(producto, "categoriaId")).intValueExact();
                    int proveedorId = new BigDecimal(leerCampo(producto, "proveedorId")).intValueExact();




                    if (codigo.isEmpty() || codigo.length() > 12 || !codigo.matches("[A-Z0-9-]+")) {
                        throw new IllegalArgumentException("Codigo incorrecto.");
                    }

                    if (nombre.length() < 3 || nombre.length() > 100) {
                        throw new IllegalArgumentException("Nombre incorrecto.");
                    }

                    if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("El precio debe ser mayor que 0.");
                    }

                    if (stock < 0) {
                        throw new IllegalArgumentException("El stock no puede ser negativo.");
                    }
                    if (categoriaId <= 0 || proveedorId <= 0) {
                        throw new IllegalArgumentException("Categoria o proveedor incorrecto.");
                    }

                    boolean duplicado = false;



                    for (Producto existente : inventario.getProductos()) {

                        if (existente.getCodigo().equalsIgnoreCase(codigo)) {
                            duplicado = true;
                        }
                    }

                    if (duplicado) {
                        throw new IllegalArgumentException("Codigo duplicado: " + codigo);
                    }

                    Producto nuevo = new Producto(
                            codigo,
                            nombre,
                            precio,
                            stock,
                            categoriaId,
                            proveedorId
                    );

                    inventario.getProductos().add(nuevo);
                    importados++;

                } catch (RuntimeException e) {

                    rechazados++;
                    System.out.println("Producto " + (i + 1) + " rechazado: " + e.getMessage());
                }
            }

          
            System.out.println("--------------------------------");
            System.out.println("Productos encontrados: " + productos.size());
            System.out.println("Productos importados: " + importados);
            System.out.println("Productos rechazados: " + rechazados);
            System.out.println("Importacion finalizada.");

        } catch (IOException | JsonParseException | IllegalStateException e) {

            System.out.println("Error al leer el JSON: " + e.getMessage());

        } catch (SecurityException e) {

            System.out.println("No tienes permiso para acceder al fichreo.");
        }
    }

    private static String leerCampo(JsonObject producto, String campo) {

        JsonElement dato = producto.get(campo);
        if (dato == null || !dato.isJsonPrimitive()) {

            throw new IllegalArgumentException("Falta o es incorrecto el campo: " + campo);
        }

        return dato.getAsString();
    }




}

