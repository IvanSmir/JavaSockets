package com.ivansm.sockets;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivansm.sockets.models.*;
import com.thoughtworks.xstream.XStream;

 public class Eslave {
    private static String url = "https://jsonplaceholder.typicode.com/";
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Uso: java Eslave <host:puerto>");
            return;
        }

        String[] parts = args[0].split(":");
        if (parts.length != 2) {
            System.out.println("Formato incorrecto de host:puerto.");
            return;
        }

        try (Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Conectado al proxy: " + socket.getRemoteSocketAddress());
            out.println("Esclavo Conectado");
            
            String request = null;

            while (true) {
                request = in.readLine();
                if(!(request == null)){
                    System.out.println("Peticion recibida");
                    if (request.contains("ESTADO")) {
                        out.println(true);
                    }else{
                    handleRequest(request, out);

                    }
                }
            }   

        } catch (NumberFormatException e) {
            System.out.println("Puerto no válido: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.out.println("Host desconocido: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de IO: " + e.getMessage());
        }
    }

    private static void handleRequest(String request, PrintWriter out) {
        String[] parts = request.split(" ");
        if (parts.length < 2) {
            out.println("Solicitud no válida.");
            out.println("END");
            return;
        }

        String method = parts[0];
        String resource = parts[1];
        String format = parts.length == 3 ? parts[2] : "JSON";

        if (!method.equals("GET")) {
            out.println("Solo se admiten peticiones GET.");
            out.println("END");
            return;
        }

        if (resource.startsWith("users")) {
            handleUsersRequest(resource, format, out);
        } else if (resource.startsWith("posts")) {
            handlePostsRequest(resource, format, out);
        } else {
            out.println("Recurso no encontrado.");
            out.println("END");
        }
    }

    private static void handleUsersRequest(String resource, String format, PrintWriter out) {
        String[] resourceParts = resource.split("/");
        if (resourceParts.length == 2) {
            String userData = fetchDataFromUrl(url + "users/" + resourceParts[1]);
            if (userData != null) {
                User user = gson.fromJson(userData, User.class);
                sendResponse(user, format, out);
            } else {
                out.println("Usuario no encontrado.");
                out.println("END");
            }
        } else {
            String usersData = fetchDataFromUrl(url + "users");
            if (usersData != null) {
                Type userListType = new TypeToken<List<User>>() {
                }.getType();
                List<User> users = gson.fromJson(usersData, userListType);
                sendResponse(users, format, out);
            } else {
                out.println("Error al obtener datos de usuarios.");
                out.println("END");
            }
        }
    }

    private static void handlePostsRequest(String resource, String format, PrintWriter out) {
        String[] resourceParts = resource.split("/");
        if (resourceParts.length == 2) {
            String postData = fetchDataFromUrl(url + "posts/" + resourceParts[1]);
            if (postData != null) {
                Posts post = gson.fromJson(postData, Posts.class);
                sendResponse(post, format, out);
            } else {
                out.println("Post no encontrado.");
                out.println("END");
            }
        } else {
            String postsData = fetchDataFromUrl(url + "posts");
            if (postsData != null) {
                Type postsListType = new TypeToken<List<Posts>>() {
                }.getType();
                List<Posts> posts = gson.fromJson(postsData, postsListType);
                sendResponse(posts, format, out);
            } else {
                out.println("Error al obtener datos de posts.");
                out.println("END");
            }
        }
    }

    private  static String fetchDataFromUrl(String urlString) {
        StringBuilder sb = new StringBuilder();
        try {
            URI uri = new URI(urlString);
            URL u = uri.toURL();
            URLConnection urlconnect = u.openConnection();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(urlconnect.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            System.out.println("Error al obtener datos de URL: " + urlString + " - " + e.getMessage());
            return null;
        }
    }

    private static void sendResponse(Object obj, String format, PrintWriter out) {
        
        String response = format.equals("XML") ? convertToXML(obj) : convertToJSON(obj);
        out.println(response);
        out.println("END");

    }

    private static String convertToJSON(Object obj) {
        return gson.toJson(obj);
    }

    private static String convertToXML(Object obj) {
        XStream xstream = new XStream();
        xstream.allowTypes(new Class[] { obj.getClass() });
        xstream.alias(obj.getClass().getSimpleName().toLowerCase(), obj.getClass());
        return xstream.toXML(obj);
    }
}
