package com.ivansm.sockets;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import com.ivansm.sockets.models.*;

public class Client {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Uso: java Client <host:puerto>");
            return;
        }

        String[] parts = args[0].split(":");
        if (parts.length != 2) {
            System.out.println("Formato incorrecto de host:puerto.");
            return;
        }

        try (Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Conectado a: " + socket.getRemoteSocketAddress());

            while (true) {
                System.out.println("\nIngresa la Peticion:");
                String line = reader.readLine();
                if (line == null || line.equalsIgnoreCase("exit")) {
                    break;
                }

                out.println(line);
                String response = in.readLine();
                if (response == null) {
                    System.out.println("El servidor cerró la conexión.");
                    break;
                }
                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append(response+"\n");
                while ((response = in.readLine()) != null) {
                    if (response.equals("END")) {
                        break;
                    }
                    responseBuilder.append(response+"\n");
                }
                String responseFinal = responseBuilder.toString();
                processResponse(line, responseFinal);
            }

        } catch (NumberFormatException e) {
            System.out.println("Puerto no válido: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.out.println("Host desconocido: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de IO: " + e.getMessage());
        }
    }

    private static void processResponse(String request, String response) {
        Class<?> clase = null;
        if (request.contains("users")) {
            clase = User.class;
        } else if (request.contains("posts")) {
            clase = Posts.class;
        } else{
            clase = request.getClass();
        }

        if (request.contains("XML")) {
            if (request.contains("/")) {
                xmlToObject(response, clase);
            } else {
                xmlToTable(response, clase);
            }
        } else {
            if (request.contains("/")) {
                jsonToObject(response, clase);
            } else if(request.contains("users")){
                jsonToTable(response, clase);
            } else{
                System.out.println(response);
            }

        }
        
    }

    private static void jsonToObject(String response, Class<?> clase) {
        Object obj = new Gson().fromJson(response, clase);
        System.out.println(obj.toString());
    }

    private static void xmlToObject(String response, Class<?> clase) {
        XStream xstream = new XStream();
        xstream.allowTypes(new Class[] { clase });
        xstream.alias(clase.getSimpleName().toLowerCase(), clase);
        Object obj = xstream.fromXML(response);
        System.out.println(obj.toString());
    }

    private static void jsonToTable(String response, Class<?> clase) {
        int MAX_ANCHO_COLUMNA = 25;
        Type listType = TypeToken.getParameterized(List.class, clase).getType();
        List<?> dataList = new Gson().fromJson(response, listType);

        if (dataList == null || dataList.isEmpty()) {
            System.out.println("No hay datos para mostrar.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        Field[] campos = clase.getDeclaredFields();

        int[] anchos = new int[campos.length];
        for (int i = 0; i < campos.length; i++) {
            anchos[i] = Math.min(campos[i].getName().length(), MAX_ANCHO_COLUMNA);
            for (Object item : dataList) {
                campos[i].setAccessible(true);
                try {
                    String valor = campos[i].get(item).toString();
                    anchos[i] = Math.max(anchos[i], Math.min(valor.length(), MAX_ANCHO_COLUMNA));
                } catch (IllegalAccessException e) {
                    anchos[i] = Math.max(anchos[i], "Acceso denegado".length());
                }
            }
        }

        for (int i = 0; i < campos.length; i++) {
            sb.append(String.format("%-" + anchos[i] + "s | ", campos[i].getName()));
        }
        sb.append("\n");

        for (int ancho : anchos) {
            char[] line = new char[ancho + 3];
            Arrays.fill(line, '-');
            sb.append(new String(line));
        }
        sb.append("\n");

        for (Object item : dataList) {
            for (int i = 0; i < campos.length; i++) {
                try {
                    String valor = campos[i].get(item).toString();
                    valor = ajustarTexto(valor, anchos[i]);
                    sb.append(String.format("%-" + anchos[i] + "s | ", valor));
                } catch (IllegalAccessException e) {
                    sb.append(String.format("%-" + anchos[i] + "s | ", "???"));
                }
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());

    }

    private static void xmlToTable(String response, Class<?> clase) {
        int MAX_ANCHO_COLUMNA = 25;

        XStream xstream = new XStream(new DomDriver());
        xstream.allowTypes(new Class[] { clase });
        xstream.alias(clase.getSimpleName().toLowerCase(), clase);

        List<?> dataList = (List<?>) xstream.fromXML(response);

        if (dataList == null || dataList.isEmpty()) {
            System.out.println("No hay datos para mostrar.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        Field[] campos = clase.getDeclaredFields();

        int[] anchos = new int[campos.length];
        for (int i = 0; i < campos.length; i++) {
            anchos[i] = Math.min(campos[i].getName().length(), MAX_ANCHO_COLUMNA);
            for (Object item : dataList) {
                campos[i].setAccessible(true);
                try {
                    String valor = campos[i].get(item).toString();
                    anchos[i] = Math.max(anchos[i], Math.min(valor.length(), MAX_ANCHO_COLUMNA));
                } catch (IllegalAccessException e) {
                    anchos[i] = Math.max(anchos[i], "Acceso denegado".length());
                }
            }
        }

        for (int i = 0; i < campos.length; i++) {
            sb.append(String.format("%-" + anchos[i] + "s | ", campos[i].getName()));
        }
        sb.append("\n");

        for (int ancho : anchos) {
            char[] line = new char[ancho + 3];
            Arrays.fill(line, '-');
            sb.append(new String(line));
        }
        sb.append("\n");

        for (Object item : dataList) {
            for (int i = 0; i < campos.length; i++) {
                try {
                    String valor = campos[i].get(item).toString();
                    valor = ajustarTexto(valor, anchos[i]);
                    sb.append(String.format("%-" + anchos[i] + "s | ", valor));
                } catch (IllegalAccessException e) {
                    sb.append(String.format("%-" + anchos[i] + "s | ", "???"));
                }
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    private static String ajustarTexto(String texto, int anchoMaximo) {
        if (texto.length() <= anchoMaximo) {
            return texto;
        } else if (anchoMaximo <= 3) {
            return texto.substring(0, anchoMaximo);
        } else {
            return texto.substring(0, anchoMaximo - 3) + "...";
        }
    }

}
