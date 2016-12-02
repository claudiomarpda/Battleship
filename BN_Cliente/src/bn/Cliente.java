package bn;

/*
 Soundtrack from BOF III and IV
 */

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Cliente implements Runnable {

    private JMenu menu;
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private boolean aguardandoConexao = true;
    private final ExecutorService exec;
    private final Semaphore semaforoBotaoOk = new Semaphore(0);
    private final Semaphore jogadorPosicionado = new Semaphore(0);
    private final Semaphore oponentePosicionado = new Semaphore(0);
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Tabuleiro tabuleiro;
    private int numeroJogador;
    private String nick;
    private String nickOponente;
    private final Semaphore semaforoNick = new Semaphore(0);
    private final Semaphore semaforoNickOponente = new Semaphore(0);
    private final Semaphore semaforoTurnoInicial = new Semaphore(0);
    private final Semaphore passarTurno = new Semaphore(0);
    private final Semaphore semaforoRun = new Semaphore(1);
    private boolean antesDoClick = false;
    private int x, y;
    private int pontosDeVida;
    private KeepAlive keepAlive;
    private Socket pingSocket;
    private final Semaphore conexoesOk = new Semaphore(0);
    private JFrame frameAtual;
    private boolean testeBotaoComecar = false;
    private Semaphore semComecar = new Semaphore(0);
    private boolean fimDeJogo = false;
    private final ExecutorService execAudio;
    private Audio audio;

    public void setAudio(Audio a) {
        audio = a;
        execAudio.execute(audio);
    }

    public Audio getAudio() {
        return audio;
    }

    public JMenu getMenu() {
        return menu;
    }

    public ExecutorService getExecAudio() {
        return execAudio;
    }

    public void ex(Audio a) {
        execAudio.execute(a);
    }

    public void encerrarConexao() {
        fimDeJogo = true;
        try {
            if (oos != null) {
                oos.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Semaphore getSemComecar() {
        return semComecar;
    }

    public void setFrameAtual(JFrame f) {
        frameAtual = f;
    }

    public void setPingSocket(Socket socket) {
        pingSocket = socket;
    }

    public void newKeepAlive() {
        keepAlive = new KeepAlive(this);
        exec.execute(keepAlive);
    }

    public boolean getTesteBotaoComecar() {
        return testeBotaoComecar;
    }

    public void setTesteBotaoComecar(boolean teste) {
        this.testeBotaoComecar = teste;
    }

    public Cliente() {

        pontosDeVida = 13;

        execAudio = Executors.newCachedThreadPool();
        menu = new JMenu(this);

        exec = Executors.newFixedThreadPool(2);

        exec.execute(this);

    }

    @Override
    public void run() {
        try {
            // aguarda digitar nick e clicar em ok
            while (aguardandoConexao) {
                semaforoBotaoOk.acquire();
            }

            this.oos = new ObjectOutputStream(output);
            this.ois = new ObjectInputStream(input);

            // 01
            numeroJogador = input.readInt(); // recebe numero do jogador      

            // 02
            output.writeUTF("Liberar tabuleiro");
            output.flush();
            if (input.readUTF().equals("conexoesOk")) {
                conexoesOk.release();
            }

            // 03
            semaforoNick.acquire(); // aguarda nick
            output.writeUTF(nick); // envia nick 
            output.flush();
            nickOponente = input.readUTF(); // recebe nick oponente   
            semaforoNickOponente.release();

            // 04
            semaforoRun.acquire();
            posicionarTabuleiros();
            semaforoRun.release();

            // atualizar botoes jogador
            semaforoRun.acquire();
            tabuleiro.atualizarBotoes1();
            semaforoRun.release();

            // 05
            output.writeUTF("gO");
            output.flush();
            tabuleiro.setTurno(input.readInt());
            System.out.println("antes do turno começar");
            // *********** CONTROLA TURNO DOS JOGADORES ***********
            String str, s1, s2;
            while (true) {

                // 06
                str = input.readUTF();
                if (str.equals("youWin")) {
                    audio.getAtual().stop();
                    setAudio(new Audio("Win"));
                    tabuleiro.fimDeJogoDialog("YOU WIN");
                } else if (str.equals("youLoose")) {
                    audio.getAtual().stop();
                    setAudio(new Audio("Lose"));
                    tabuleiro.fimDeJogoDialog("YOU LOOSE");
                }

                if (str.equals("play")) {
                    tabuleiro.switchOceanoInimigo();
                    System.out.println("recebeu autorizacao pra atacar");
                } else {

                    tabuleiro.setRecebendoAtq(true);
                    if (str.equals("recebeAtq")) {
                        System.out.println("vai receber atq");
                        do {

                            tabuleiro.setRecebendoAtq(true);

                            if (str.equals("recebe+Atq")) {
                                System.out.println("recebe atq de novo");
                            }

                            // 07
                            s1 = input.readUTF();
                            s2 = input.readUTF();

                            if (s1.equals("youLose") && s2.equals("youLose")) {
                                audio.getAtual().stop();
                                setAudio(new Audio("Lose"));
                                tabuleiro.fimDeJogoDialog("YOU LOSE");
                            } else {
                                x = Integer.parseInt(s1);
                                y = Integer.parseInt(s2);
                                System.out.println("recebi atq " + x + " " + y);

                                antesDoClick = true;
                                tabuleiro.getBotoes1()[x][y].getBotao().doClick();
                                antesDoClick = false;
                            }

                            // 08
                            // Botao addEventoBotaoJogador: envia "fimDeJogo" ou "continuaJogo"
                            // 09
                            // "atqContinua" ou "fimDeTurno"
                            str = input.readUTF();
                            if (str.equals("youLose")) {
                                audio.getAtual().stop();
                                setAudio(new Audio("Lose"));
                                tabuleiro.fimDeJogoDialog("YOU LOSE");
                            }
                            System.out.println("str atualizada: " + str);
                        } while (str.equals("atqContinua"));

                        // 09
                        output.writeUTF("fimDeTurno");
                    }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            erroConexaoDialog("Erro de conexão com o servidor, tente novamente!");

        }
    }

    public void reduzirPontosDeVida() throws IOException {
        pontosDeVida--;
        if (pontosDeVida == 0) {  // encerra partida
            enviarMsg("fimDeJogo");
        } else {
            enviarMsg("continuaJogo");
            System.out.println("pontos de vida " + pontosDeVida);
        }
    }

    public void enviarAtq(int x, int y) {
        try {
            System.out.println("no cliente enviar atq " + x + " " + y);
            output.writeUTF(String.valueOf(x));
            output.flush();
            output.writeUTF(String.valueOf(y));
            output.flush();
        } catch (IOException ex) {
            erroConexaoDialog("Erro de conexão com o servidor, tente novamente!");
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void enviarMsg(String msg) {
        try {
            output.writeUTF(msg);
            output.flush();
        } catch (IOException ex) {
            erroConexaoDialog("Erro de conexão com o servidor, tente novamente!");
        }
    }

    public void posicionarTabuleiros() throws IOException {
        try {
            jogadorPosicionado.acquire();
            output.writeUTF("jogadorPosicionado");
            output.flush();
            tabuleiro = menu.getJEspera().getTabuleiro();
            Botao[][] botoes = tabuleiro.getBotoes1();
            oos.writeObject(botoes);
            oos.flush();

            if (input.readUTF().equals("oponentePosicionado")) {
                oponentePosicionado.release(); // libera o run do tabuleiro
                System.out.println("oponente ta posicionado");
                botoes = (Botao[][]) ois.readObject(); // recebe tabuleiro inimigo
                tabuleiro.criaTabuleiroInimigo(botoes);
            }
            tabuleiro.revalidate();
            tabuleiro.repaint();

        } catch (InterruptedException | ClassNotFoundException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void erroConexaoDialog(String msg) {
        if (!fimDeJogo) {

            JDialog dialog = new JDialog();
            Container cDialog = dialog.getContentPane();
            cDialog.setLayout(new FlowLayout());
            cDialog.setBackground(Color.darkGray);

            JLabel strLabel = new JLabel(msg);
            strLabel.setForeground(Color.white);
            cDialog.add(strLabel);

            JButton b = new JButton("Ok");
            b.addActionListener((ActionEvent e) -> {
                dialog.dispose();
                if (frameAtual != menu) {
                    frameAtual.dispose();
                    new Cliente();
                }
            });

            cDialog.add(b);
            dialog.setUndecorated(true); // retira opcoes de janela - [] x
            dialog.setModal(true);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setSize(310, 60);
            dialog.setVisible(true);
            dialog.setResizable(false);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOutput(DataOutputStream output) {
        this.output = output;
    }

    public void setInput(DataInputStream input) {
        this.input = input;
    }

    public boolean getAntesDoCLick() {
        return antesDoClick;
    }

    public Semaphore getPassarTurno() {
        return passarTurno;
    }

    public Semaphore getSemaforoTurnoInicial() {
        return semaforoTurnoInicial;
    }

    public Semaphore getSemaforoNickOponente() {
        return semaforoNickOponente;
    }

    public Semaphore getSemaforoNick() {
        return semaforoNick;
    }

    public int getNumeroJogador() {
        return numeroJogador;
    }

    public Semaphore getSemaforoBotaoOk() {
        return semaforoBotaoOk;
    }

    public Semaphore getJogadorPosicionado() {
        return jogadorPosicionado;
    }

    public Semaphore getOponentePosicionado() {
        return oponentePosicionado;
    }

    public String getNick() {
        return nick;
    }

    public String getNickOponente() {
        return nickOponente;
    }

    public void setNick(String s) {
        nick = s;
    }

    public Semaphore getConexoesOk() {
        return conexoesOk;
    }

    public void setAguardandoConexao(boolean valor) {
        aguardandoConexao = valor;
    }

    public boolean getAguardandoConexao() {
        return aguardandoConexao;
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public static void main(String[] args) {
        new Cliente();
    }
}
