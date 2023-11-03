import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.util.*;

import com.google.gson.Gson;

class User {
    int id;
    String name;
    String email;
    int age;

    public User(int i, String n, String e, int a) {
        this.id = i;
        this.name = n;
        this.email = e;
        this.age = a;
    }

}

class Post {
    int id;
    int idClient;
    LocalDate date;
    String body;

    public Post(int i, int ic, String b) {
        this.id = i;
        this.idClient = ic;
        this.date = LocalDate.now();
        this.body = b;
    }
}

public class Server {
    private static Map<Integer, User> users = new HashMap<>();
    private static Map<Integer, Post> posts = new HashMap<>();
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        createdata();
        try (ServerSocket serverSocket = new ServerSocket(7777)) {
            System.out.println("Iniciando Server" + serverSocket.getLocalPort());
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
                System.out.println("Error al manejar la conexión del cliente:");
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private void handleRequest(String request, PrintWriter out) {
            String[] parts = request.split(" ");
            System.out.println(parts);
            if (parts.length < 2) {
                out.println("Solicitud no válida.");
                return;
            }

            String method = parts[0];
            String resource = parts[1];
            String format = parts.length == 3 ? parts[2] : "JSON";

            if (!method.equals("GET")) {
                out.println("Solo se admiten peticiones GET.");
                return;
            }

            if (resource.startsWith("users")) {
                String[] resourceParts = resource.split("/");
                if (resourceParts.length == 2) {
                    int userId = Integer.parseInt(resourceParts[1]);
                    User user = users.get(userId);
                    if (user != null) {
                        String response = format.equals("XML") ? convertToXML(user) : convertToJSON(user);
                        out.println(response);
                    } else {
                        out.println("Usuario no encontrado.");
                    }
                } else {
                    String response = format.equals("XML") ? convertToXML(users) : convertToJSON(users);
                    out.println(response);
                }
            } else if (resource.startsWith("posts")) {
                String[] resourceParts = resource.split("/");
                if (resourceParts.length == 2) {
                    int postId = Integer.parseInt(resourceParts[1]);
                    Post post = posts.get(postId);
                    if (post != null) {
                        String response = format.equals("XML") ? convertToXML(post) : convertToJSON(post);
                        out.println(response);
                    } else {
                        out.println("Post no encontrado.");
                    }
                } else {
                    String response = format.equals("XML") ? convertToXML(posts) : convertToJSON(posts);
                    out.println(response);
                }
            } else {
                out.println("Recurso no encontrado.");
            }

        }

        private String convertToJSON(Object obj) {
            return gson.toJson(obj);
        }

        private String convertToXML(Object obj) {
            return "";
        }

    }

    private static void createdata() {
        for (int i = 0; i < 5; i++) {
            User newUser = new User((i + 1), "nombre" + i, "email" + i, i * 5);
            users.put((i + 1), newUser);
            Post newpPost = new Post(i + 1, i + 1, "contenido" + 1);
            posts.put(i + 1, newpPost);
        }
    }
}
