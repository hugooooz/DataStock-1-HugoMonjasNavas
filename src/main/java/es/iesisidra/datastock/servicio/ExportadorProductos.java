package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import es.iesisidra.datastock.modelo.Producto;

public class ExportadorProductos {

    private static boolean comprobar(Inventario inventario, String ruta) {

        if (inventario.getProductos().isEmpty()) {
            System.out.println("El inventario esta vacio.");
            return false;
        }


        if (ruta.trim().isEmpty()) {
            System.out.println("No has introducido ninguna ruta.");
            return false;
        }

        File archivo = new File(ruta.trim());


        if (archivo.exists() && archivo.isDirectory()) {
            System.out.println("La ruta corresponde a una carpeta.");

            return false;
        }


        File carpeta = archivo.getAbsoluteFile().getParentFile();

        if (!carpeta.exists() || !carpeta.isDirectory()) {
            System.out.println("La carpeta de destino no existe.");
            return false;
        }

        if (!carpeta.canWrite()) {
            System.out.println("No tienes permiso para escribir en la carpeta.");
            return false;
        }

        return true;
    }

    public static void exportarJSON(Inventario inventario, String ruta) {

        if (!comprobar(inventario, ruta)) {
            return;
        }

        
        ArrayList<Producto> productos = new ArrayList<>(inventario.getProductos());
        productos.sort(Comparator.comparing(Producto::getCodigo));
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject json = new JsonObject();
        json.add("productos", gson.toJsonTree(productos));
        try (FileWriter escritor = new FileWriter(ruta.trim(), StandardCharsets.UTF_8)) {



            gson.toJson(json, escritor);

            System.out.println("Fichero JSON generado correctamente.");
            System.out.println("Ruta: " + new File(ruta.trim()).getAbsolutePath());
            System.out.println("Productos exportados: " + productos.size());

        } catch (IOException e) {
            System.out.println("Error al exportar JSON: " + e.getMessage());
        }
    }


    public static void exportarXML(Inventario inventario, String ruta) {

        if (!comprobar(inventario, ruta)) {
            return;
        }

        
        ArrayList<Producto> productos = new ArrayList<>(inventario.getProductos());
        productos.sort(Comparator.comparing(Producto::getCodigo));

        try {

            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder lector = fabrica.newDocumentBuilder();
            Document documento = lector.newDocument();
            Element catalogo = documento.createElement("catalogo");
            documento.appendChild(catalogo);
            

            for (Producto producto : productos) {

                Element elemento = documento.createElement("producto");
                catalogo.appendChild(elemento);



                agregarCampo(documento, elemento, "codigo", producto.getCodigo());
                agregarCampo(documento, elemento, "nombre", producto.getNombre());
                agregarCampo(documento, elemento, "precio", producto.getPrecio().toString());
                agregarCampo(documento, elemento, "stock", String.valueOf(producto.getStock()));
                agregarCampo(documento, elemento, "categoriaId", String.valueOf(producto.getCategoriaId()));
                agregarCampo(documento, elemento, "proveedorId", String.valueOf(producto.getProveedorId()));
            }

            TransformerFactory fabricaEscritura = TransformerFactory.newInstance();
            fabricaEscritura.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabricaEscritura.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            fabricaEscritura.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            Transformer escritor = fabricaEscritura.newTransformer();

            escritor.setOutputProperty(OutputKeys.INDENT, "yes");
            escritor.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            File archivo = new File(ruta.trim());

            escritor.transform(
                    new DOMSource(documento),
                    new StreamResult(archivo)
            );

            System.out.println("Fichero XML generado correctamente.");
            System.out.println("Ruta: " + archivo.getAbsolutePath());
            System.out.println("Productos exportados: " + productos.size());

        } catch (ParserConfigurationException | TransformerException e) {
            System.out.println("Error al exportar XML: " + e.getMessage());
        }


    }



    private static void agregarCampo(Document documento, Element producto, String nombre, String valor) {

        Element campo = documento.createElement(nombre);
        campo.setTextContent(valor);
        producto.appendChild(campo);
    }
}
