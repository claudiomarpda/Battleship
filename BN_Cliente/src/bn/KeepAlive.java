package bn;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeepAlive extends Thread {

    private final int PORTA_PING = 5005;

    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Cliente cliente;
    private boolean alive = true;

    public KeepAlive(Cliente c) {
        cliente = c;
        try {
            socket = new Socket("localhost", PORTA_PING);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream((socket.getInputStream()));
        } catch (IOException ex) {
            System.err.println("erro ao obter socket KeepAlive");
            cliente.erroConexaoDialog("Erro de conexão com o servidor, tente novamente!");
            Logger.getLogger(KeepAlive.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        while (alive) {
            try {
                Thread.sleep(4000);

                // 01 ping
                socket.setSoTimeout(12000);
                dos.writeBoolean(true);

                // 02 pong
                socket.setSoTimeout(12000);
                System.out.print("ping ");
                dis.readBoolean();
                System.out.println("pong");

                if (cliente.getTesteBotaoComecar()) {
                    cliente.getSemComecar().release();
                    cliente.setTesteBotaoComecar(false);
                }

            } catch (SocketTimeoutException ex) {
                alive = false;
                cliente.erroConexaoDialog("Oponente   desconectado,   reinicie   o   jogo.");

                System.err.println("time out run keep alive");
            } catch (InterruptedException ex) {
            } catch (IOException ex) {
            }
        }
    }

}
