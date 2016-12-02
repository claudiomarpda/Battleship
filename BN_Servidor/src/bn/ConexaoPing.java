/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bn;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rene
 */
public class ConexaoPing extends Thread {

    private Socket p1, p2;
    private ServerSocket pingServerSocket;

    public ConexaoPing(Socket p11, Socket p21, ServerSocket server) {
        p1 = p11;
        p2 = p21;
        pingServerSocket = server;
    }

    @Override
    public void run() {

        Executor exec = Executors.newCachedThreadPool();

        Servidor servidor = new Servidor(p1, p2);
        exec.execute(servidor);
        try {

            Socket pingS1 = pingServerSocket.accept();
            Socket pingS2 = pingServerSocket.accept();
            servidor.setPings(pingS1, pingS2);
        } catch (IOException ex) {
            Logger.getLogger(ConexaoPing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
