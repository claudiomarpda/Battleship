package bn;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor implements Runnable {

    private ExecutorService executor;
    private Jogador[] jogadores;
    private ReentrantLock gameLock;
    private int jogadorAtual;
    private final Semaphore conexoesOk = new Semaphore(0);
    private int nJogadoresPosicionados = 0; // contador
    private Botao[][] b0, b1;
    private String x, y;
    private boolean primeiroAtq = true;
    private Socket socket1;
    private Socket socket2;
    private final Semaphore semaforoPings = new Semaphore(0);
    private final Semaphore jogadoresPosicionados = new Semaphore(0);
    private boolean fimDeJogo = false;

    public Servidor(Socket socket1, Socket socket2) {

        gameLock = new ReentrantLock();
        executor = Executors.newFixedThreadPool(4);
        jogadorAtual = new Random().nextInt(2);

        this.socket1 = socket1;
        this.socket2 = socket2;
        jogadores = new Jogador[2];

        jogadores[0] = new Jogador(socket1, 0);
        jogadores[1] = new Jogador(socket2, 1);

        executor.execute(jogadores[0]);
        executor.execute(jogadores[1]);
    }

    @Override
    public void run() {
        try {
            semaforoPings.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("run do servidor");
        int turno = 0;
        boolean alive = true;
        while (alive) {
            try {
                // 01 ping
                jogadores[turno].pingSocket.setSoTimeout(5000);
                jogadores[turno].pingInput.readBoolean();

                // 02 pong
                jogadores[turno].pingOutput.writeBoolean(true);
                turno = (turno + 1) % 2;

            } catch (SocketTimeoutException ex) {
                alive = false;
                System.err.println("Erro time out");
                encerrarConexoes();
                return;
            } catch (IOException ex) {
                alive = false;
                System.err.println("io exception run servidor");
                encerrarConexoes();
                return;
                //Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void encerrarConexoes() {
        try {
            for (int i = 0; i <= 0; i++) {
                if (jogadores[i] != null) {
                    if (jogadores[i].conexao != null) {
                        jogadores[i].conexao.close();
                    }
                    if (jogadores[i].input != null) {
                        jogadores[i].input.close();
                    }
                    if (jogadores[i].output != null) {
                        jogadores[i].output.close();
                    }
                    if (jogadores[i].pingInput != null) {
                        jogadores[i].pingInput.close();
                    }
                    if (jogadores[i].pingOutput != null) {
                        jogadores[i].pingOutput.close();
                    }
                    if (jogadores[i].pingSocket != null) {
                        jogadores[i].pingSocket.close();
                    }
                }
            }
            if (socket1 != null) {
                socket1.close();
            }
            if (socket2 != null) {
                socket2.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setPings(Socket s0, Socket s1) {
        try {
            jogadores[0].pingSocket = s0;
            jogadores[1].pingSocket = s1;
            jogadores[0].pingOutput = new DataOutputStream((s0.getOutputStream()));
            jogadores[0].pingInput = new DataInputStream(s0.getInputStream());
            jogadores[1].pingOutput = new DataOutputStream(s1.getOutputStream());
            jogadores[1].pingInput = new DataInputStream((s1.getInputStream()));
        } catch (IOException ex) {
            encerrarConexoes();
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        semaforoPings.release();
    }

    private class Jogador implements Runnable {

        private Socket conexao;
        private final int numeroJogador;
        private DataInputStream input;
        private DataOutputStream output;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private Socket pingSocket;
        private DataInputStream pingInput;
        private DataOutputStream pingOutput;
        private boolean jogadoresConectados = true;
        private int pontosDeVida;

        public Jogador(Socket socket, int numero) {
            conexao = socket;
            numeroJogador = numero;
            pontosDeVida = 13;

            try {
                input = new DataInputStream(conexao.getInputStream());
                output = new DataOutputStream(conexao.getOutputStream());
                this.oos = new ObjectOutputStream(output);
                this.ois = new ObjectInputStream(input);

            } catch (IOException ex) {
                System.out.println("CLIENTE DOWN NO CONSTRUTOR DO JOGADOR ##########");
                encerrarConexoes();
            }
        }

        public String recebeMsg() throws IOException {
            return input.readUTF();
        }

        public void enviarMsg(String x, String y) throws IOException {
            output.writeUTF(x);
            output.flush();
            output.writeUTF(y);
            output.flush();
        }

        public void enviarMsg(String s) throws IOException {
            output.writeUTF(s);
            output.flush();
        }

        public void enviarObjeto(Object ob) throws IOException {
            oos.writeObject(ob);
            oos.flush();
        }

        public void verificarConexoes() throws IOException, InterruptedException {
            if (numeroJogador == 0) { //primeiro jogador a inicializar o jogo
                gameLock.lock(); //garante exclusão mútua
                try {
                    if (input.readUTF().equals("Liberar tabuleiro")) { //se jogador 0 confirmar nick conexões passa a ser true
                        System.out.println("recebeu conexao do 1 cliente");
                        conexoesOk.release();
                    }
                } finally {
                    gameLock.unlock();
                }
                return;
            }
            while (numeroJogador == 1) //segundo jogador a inicializar o jogo
            {
                if (input.readUTF().equals("Liberar tabuleiro")) { //se jogador 1 confirmar nick
                    System.out.println("recebeu conexao do 2 cliente");
                    conexoesOk.acquire();
                    jogadores[0].enviarMsg("conexoesOk"); //envia mensagem ao cliente, inicializar tabuleiro do jogador 0
                    jogadores[1].enviarMsg("conexoesOk"); //envia mensagem ao cliente, inicializar tabuleiro do jogador 1
                    return;
                }
            }
        }

        public void verificarTabuleiros() throws IOException, InterruptedException {

            System.out.println("$$$$$$$$$$ PRINT 1");
            if (input.readUTF().equals("jogadorPosicionado")) {
                try {
                    if (numeroJogador == 0) {
                        b0 = (Botao[][]) ois.readObject();
                    } else if (numeroJogador == 1) {
                        b1 = (Botao[][]) ois.readObject();
                    }
                    nJogadoresPosicionados++;
                    if (nJogadoresPosicionados == 2) {
                        jogadoresPosicionados.release();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();

                }
            }
            if (numeroJogador == 1) {
                while (nJogadoresPosicionados != 2) {
                    jogadoresPosicionados.acquire();
                }
                jogadores[0].enviarMsg("oponentePosicionado");
                jogadores[0].enviarObjeto(b1);
                jogadores[1].enviarMsg("oponentePosicionado");
                jogadores[1].enviarObjeto(b0);
            }

        }

        public void receberEnviarNick() throws IOException {
            String nick = input.readUTF();
            if (numeroJogador == 0) {
                jogadores[1].enviarMsg(nick); // jogador 0 envia nick ao 1
            } else {
                jogadores[0].enviarMsg(nick); // jogador 1 envia nick ao 0
            }
        }

        public int reduzirPontosDeVida() {
            return --pontosDeVida;
        }

        @Override
        public void run() {
            try {

                // 01
                output.writeInt(numeroJogador);
                output.flush();

                // 02
                verificarConexoes();
                System.out.println("conexoes verificadas " + numeroJogador);

                // 03
                receberEnviarNick();

                // 04
                verificarTabuleiros();
                System.out.println("posicoes verificadas " + numeroJogador);

                // 05
                if (input.readUTF().equals("gO")) {
                    output.writeInt(jogadorAtual); // jogador inicial
                    System.out.println("enviou o jogadorAtual " + jogadorAtual);
                    output.flush();
                }

            } catch (IOException ex) {
                System.out.println("CLIENTE DOWN NA SINCRONIZACAO DE CONEXOES");
                encerrarConexoes();
            } catch (InterruptedException ex) {
                encerrarConexoes();
            }

            // CONTROLA TURNO DOS JOGADORES
            while (jogadoresConectados) {
                try {

                    // 06
                    if (jogadorAtual == numeroJogador) {
                        if (primeiroAtq) {
                            output.writeUTF("play");
                            primeiroAtq = false;
                        }

                        // 07
                        x = input.readUTF();
                        y = input.readUTF();
                        System.out.println("recebeu x e y" + x + " " + y);
                        jogadores[(jogadorAtual + 1) % 2].enviarMsg(x, y);
                        System.out.println("enviou msg");

                        // 08
                        // Botao.addEventoBotaoJogador
                        // 09 
                        // "atqContinua" ou "fimDeTurno"
                        if (input.readUTF().equals("fimDeTurno")) {
                            jogadores[(jogadorAtual + 1) % 2].enviarMsg("fimDeTurno");
                            jogadorAtual = (jogadorAtual + 1) % 2;
                            primeiroAtq = true;
                            System.out.println("fim de turno");
                        } else { // "continuaAtq"
                            jogadores[(jogadorAtual + 1) % 2].enviarMsg("atqContinua");
                            if (jogadores[(jogadorAtual + 1) % 2].reduzirPontosDeVida() == 0) {
                                fimDeJogo = true;
                                jogadores[(jogadorAtual + 1) % 2].enviarMsg("youLose", "youLose");
                                jogadores[jogadorAtual].enviarMsg("youWin");
                                System.out.println("GAME OVER FOR" + jogadores[(jogadorAtual + 1) % 2].numeroJogador);
                            }
                            System.out.println("joga outra vez");
                        }
                    } else {
                        // 06
                        output.writeUTF("recebeAtq");

                        // 08
                        // Botao addEventoBotaoJogador
                        // "fimDeJogo" ou "continuaJogo"
                        // 09
                        input.readUTF(); // aguarda "fimDeTurno"
                    }

                } catch (IOException ex) {
                    jogadoresConectados = false;
                    System.out.println("CLIENTE DOWN NA SINCRONIZACAO DE TURNOS");
                    encerrarConexoes();
                    //Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
