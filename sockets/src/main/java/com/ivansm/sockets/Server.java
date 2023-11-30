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
    private static List<Slaves> servidoresEsclavos = new ArrayList<>();
    private static int ultimoServidorUtilizado = -1;


    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(7777)) {
            System.out.println("Iniciando Server en puerto " + serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                Conexion conexion = new Conexion(socket);
                conexion.start();
            }
        }
    }

    static class Slaves {
        int id;
        Conexion slave;
        int pedidos;
        Boolean estado;
        Boolean conexion;
        BufferedReader in;
        PrintWriter out;

        public Slaves(int id, Conexion slave) {
            this.slave = slave;
            this.pedidos = 0;
            this.estado = false;
            this.id = id;
            this.conexion = true;
            try {
                this.in = new BufferedReader(new InputStreamReader(slave.socket.getInputStream()));
                this.out = new PrintWriter(slave.socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Error al abrir los streams: " + e.getMessage());
            }
        }
    }

    static class Conexion extends Thread {
        private Socket socket;

        public Conexion(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String line;
                
                    while ((line = in.readLine()) != null) {
                    if (line.contains("Esclavo")) {
                        Slaves slave = new Slaves(servidoresEsclavos.size() + 1, this);
                        servidoresEsclavos.add(slave);
                        System.out.println("Esclavo con id " + slave.id + " conectado");
                        break;
                    } else {
                        handleRequest(line, out);
                    }
                }
                
                
            } catch (IOException e) {
                System.out.println("Error al manejar la conexión del cliente: " + e.getMessage());
            }
        }

        private void handleRequest(String request, PrintWriter out) {
            Boolean activos = false;
            for (Slaves s : servidoresEsclavos) {
                        try {
                            PrintWriter outServer = s.out;

                            BufferedReader in = s.in;
                            outServer.println("ESTADO");
                            String response = in.readLine();
                            if (response == null) {
                                s.conexion = false;
                            } else {
                                s.conexion = true;
                                activos = true;
                            }
                        } catch (IOException e) {
                            s.conexion = false;
                            activos = false;
                        }
                    }
            if (!request.contains("estado")) {
                System.out.println(activos);
                if (!servidoresEsclavos.isEmpty() && activos) {
                        System.out.println("prueba2");
                        System.out.println(ultimoServidorUtilizado);
                        System.out.println(servidoresEsclavos.size());
                        System.out.println("prueba");
                        if (ultimoServidorUtilizado >= servidoresEsclavos.size()-1) {
                            System.out.println("entre");
                            ultimoServidorUtilizado = -1;
                        }
                        System.out.println(ultimoServidorUtilizado);
                        for (int i = ultimoServidorUtilizado+1; i<servidoresEsclavos.size(); i++) {
                            System.out.println(i);
                            Slaves s = servidoresEsclavos.get(i);
                            if (!s.estado  && s.conexion ) {
                                BufferedReader in = s.in;
                                PrintWriter outServer = s.out;
                                s.estado = true;
                                try {
                                    outServer.println(request);
                                    String response = in.readLine();
                                    if (response == null) {
                                        System.out.println("El servidor cerró la conexión.");
                                        break;
                                    }
                                    StringBuilder responseBuilder = new StringBuilder();
                                    responseBuilder.append(response);
                                    while ((response = in.readLine()) != null) {
                                        if (response.equals("END")) {
                                            break;
                                        }
                                        responseBuilder.append(response);

                                    }
                                    String responseFinal = responseBuilder.toString();
                                    sendResponse(responseFinal, out);
                                    s.estado = false;
                                    s.pedidos++;
                                } catch (IOException e) {
                                    System.out.println("Error al manejar la conexión del esclavo: " + e.getMessage()+"\n");
                                    e.printStackTrace();
                                }
                                ultimoServidorUtilizado++;
                                return;
                            }
                            ultimoServidorUtilizado++;
                        }
                        
                    System.out.println("xd3");
                }
                handleRequest2(request, out);
            } else {
                out.println("Lista de Esclavos: ");
                for (Slaves slaves : servidoresEsclavos) {
                    out.println("Esclavo id: " + slaves.id + ", Peticiones: " + slaves.pedidos + ", Estado: "
                            + (slaves.conexion? "Conectado":"Desconectado") + "\n");
                }
                out.println("END");
            }

        }

        private void handleRequest2(String request, PrintWriter out) {
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
            System.out.println(response);
            out.println(response);
            out.println("END");
        }

        private void sendResponse(String response, PrintWriter out) {
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
