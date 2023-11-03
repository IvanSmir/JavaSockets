import java.io.*;
import java.net.*;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Client {
    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        if (args.length != 1) {
            System.out.println("Uso: java WebClient <host:puerto>");
            return;
        }
        String[] parts = args[0].split(":");
        try (
                Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ) {
            System.out.println("Conectado a:" + socket.getRemoteSocketAddress());
            System.out.println("Conectado a:" + socket.getRemoteSocketAddress());
            try {
                while (true) {
                    System.out.println("Ingresa la Peticion:");
                    String line = reader.readLine();
                    if (line == null) { 
                        break;
                    }
                    out.println(line);
                    String response = in.readLine();
                    if (response == null) {
                        System.out.println("El servidor cerró la conexión.");
                        break;
                    };
                    System.out.println(response);
                }
            } catch (SocketException e) {
                System.out.println("Conexión interrumpida: " + e.getMessage());
            }

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
        }

    }
    private static void printObjeto(JsonObject obj){
    }
}
