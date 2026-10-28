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



    private static void agregarCampo(Document documento, Element producto, String nombre, String valor) {

        Element campo = documento.createElement(nombre);
        campo.setTextContent(valor);
        producto.appendChild(campo);
    }
}
