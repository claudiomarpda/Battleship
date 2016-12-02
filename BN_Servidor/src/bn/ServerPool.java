package bn;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ServerPool {

    public static void main(String[] args) {
        final int PORTA_SERVIDOR = 5000;
        final int PORTA_PING = 5005;
        Executor exec = Executors.newCachedThreadPool();
        try {
            ServerSocket servidorSocket = new ServerSocket(PORTA_SERVIDOR);
            ServerSocket pingServerSocket = new ServerSocket(PORTA_PING);

            while (true) {
                System.out.println("Server on aguardando conexoes.");
                Socket playerS1 = servidorSocket.accept();
                Socket playerS2 = servidorSocket.accept();
                exec.execute(new ConexaoPing(playerS1, playerS2, pingServerSocket));
                System.out.println("servidor on");
            }
        } catch (IOException e) {
            System.out.println("Pool Server DOWN!"); // nao deve acontecer
            e.printStackTrace();
        }
    }
}
