import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static Map<String, PrintWriter> clientMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345); // Port number
            System.out.println("Server waiting for connections...");

            while (true) {
                Socket socket = serverSocket.accept(); // Wait for client connection
                System.out.println("Client connected.");

                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private String clientName;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                out.println("Enter your name:");
                clientName = in.readLine();
                synchronized (clientMap) {
                    clientMap.put(clientName, out);
                }
                broadcast(clientName + " has joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(clientName + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    synchronized (clientMap) {
                        clientMap.remove(clientName);
                    }
                    broadcast(clientName + " has left the chat.");
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clientMap) {
                for (PrintWriter writer : clientMap.values()) {
                    writer.println(message);
                }
            }
        }
    }
}