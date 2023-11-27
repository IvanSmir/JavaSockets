package com.ivansm.sockets;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.Type;

import com.ivansm.sockets.models.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.thoughtworks.xstream.XStream;

public class Server {
    private static String url = "https://jsonplaceholder.typicode.com/";
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(7777)) {
            System.out.println("Iniciando Server en puerto " + serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                new ConexionCliente(socket).start();
            }
        }
    }

    static class ConexionCliente extends Thread {
        private Socket socket;

        public ConexionCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                String line;
                while ((line = in.readLine()) != null) {
                    handleRequest(line, out);
                }
            } catch (IOException e) {
                System.out.println("Error al manejar la conexión del cliente: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar el socket: " + e.getMessage());
                }
            }
        }

        private void handleRequest(String request, PrintWriter out) {
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

        private void handleUsersRequest(String resource, String format, PrintWriter out) {
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
            Type userListType = new TypeToken<List<User>>() {}.getType();
            List<User> users = gson.fromJson(usersData, userListType);
            sendResponse(users, format, out);
        } else {
            out.println("Error al obtener datos de usuarios.");
            out.println("END");
        }
    }
}

private void handlePostsRequest(String resource, String format, PrintWriter out) {
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
            Type postsListType = new TypeToken<List<Posts>>() {}.getType();
            List<Posts> posts = gson.fromJson(postsData, postsListType);
            sendResponse(posts, format, out);
        } else {
            out.println("Error al obtener datos de posts.");
            out.println("END");
        }
    }
}
        private String fetchDataFromUrl(String urlString) {
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

        private void sendResponse(Object obj, String format, PrintWriter out) {
            String response = format.equals("XML") ? convertToXML(obj) : convertToJSON(obj);
            out.println(response);
            out.println("END");
        }

        private String convertToJSON(Object obj) {
            return gson.toJson(obj);
        }

        private String convertToXML(Object obj) {
            XStream xstream = new XStream();
            xstream.allowTypes(new Class[] { obj.getClass() });
            xstream.alias(obj.getClass().getSimpleName().toLowerCase(), obj.getClass());
            return xstream.toXML(obj);
        }
    }
}
