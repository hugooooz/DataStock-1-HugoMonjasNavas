package es.iesisidra.datastock.servicio;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ImportadorJSON {
    public static void importar(String ruta, Inventario inventario) {

        File archivo = new File(ruta);

        if (!archivo.exists()) {
            System.out.println("El fichero no existe");
            return;
            
        }

        if(!archivo.isFile()){
            System.out.println("La ruta no es un fichero");
            return;
        }

        if (!archivo.canRead()) {
            System.out.println("No se puede leer el fichero");
            return;
        }


        
        try (FileReader lector = new FileReader(archivo)) {

            JsonObject json = JsonParser.parseReader(lector).getAsJsonObject();

            JsonArray productos = json.getAsJsonArray("productos");

            if (productos == null) {
                System.out.println("El JSON no contiene la lista de productos.");
                return;

            //El cartero mira la lista ysi resulta que la lista de productos esta invisible o no existe (null), dice: "¡Me estafaron, este pergamino no trae la lista!" y se va.
        }

        System.out.println("Productos encontrados: " + productos.size());

        } catch (IOException | JsonParseException | IllegalStateException e) {
            System.out.println("Error al leer el JSON: " + e.getMessage());
        }

    }    
}


/**
 * el cartero agarra una caja archivo y le pega un papelito con la direccion ruta que le di. El cartero viaja a esa dirección para revisar. El signo ! significa NO y el cartero va y ve que ahí no hay nada (la casa no existe), grita en la pantalla que el fichero no existe, pero si no se rinde osea si no usa el return, se pone feliz y avisa de que si esta???? si es asi, seguimos
 */
//que tipo de diferencia hay entre bigdeciaml y double