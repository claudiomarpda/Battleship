package bn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Tabuleiro extends JFrame implements Runnable {

    private enum Direcao {

        V, H
    }; // posicao vertical ou horizontal para posicionamento dos navios

    private boolean construindoNavio; // utilizado no posicionamento de navios
    private Navio[] navios;
    private int navioAtual;
    private boolean oceanoClicavel; // limita interaçao do usuario 
    private Botao[][] botoes1; // botoes jogador
    private Botao[][] botoes2; // botoes inimigo
    private Direcao direcao;
    private JButton[] jBarcos;
    private JLabel turnoLabel, nickOeste, nickLeste;
    private Cliente cliente;
    private JButton botaoComecar;
    private Semaphore naviosPosicionados;
    private Container c6; // tabuleiro leste
    private Container c2; // tabuleiro oeste
    private Container c; // container principal
    private Container c4; // container sul: jBarcos e naviosInimigoLabel
    private final ImageIcon turnoVermelho = new ImageIcon(getClass().getClassLoader().getResource("bn\\res\\redsignal.png"));
    private final ImageIcon turnoVerde = new ImageIcon(getClass().getClassLoader().getResource("bn\\res\\greensignal.png"));
    private boolean oceanoInimigoClicavel = false;
    private int turnoAtual;
    private boolean recebendoAtq = false;
    private JLabel inimigoDestruidoText;
    private JLabel[] navioInimigoDestruidoLabels;
    private JDialog waitdialog;

    public void fimDeJogoDialog(String msg) {
        JDialog dialog = new JDialog();
        Container cDialog = dialog.getContentPane();
        cDialog.setLayout(new FlowLayout());
        cDialog.setBackground(Color.black);

        JButton b = new JButton(msg);
        b.setFont(new Font("Tahoma", Font.BOLD, 30));
        b.setBorder(BorderFactory.createLineBorder(Color.black));

        if (msg.equals("YOU WIN")) {
            b.setBackground(Color.yellow);
        } else {
            b.setBackground(Color.red);
        }

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (msg.equals("YOU WIN")) {
                    b.setBackground(new Color(242, 247, 94));
                } else {
                    b.setBackground(new Color(250, 120, 100));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (msg.equals("YOU WIN")) {
                    b.setBackground(Color.yellow);
                } else {
                    b.setBackground(Color.red);
                }
                //b.setBackground(UIManager.getColor("control"));
            }
        });

        b.addActionListener((ActionEvent e) -> {
            dialog.dispose();
            this.dispose();
            new Cliente();
            cliente.encerrarConexao();
        });
        cDialog.add(b);
        dialog.setUndecorated(true); // retira opcoes de janela - [] x
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.setResizable(false);
    }

    public JLabel[] getNaviosInimigosDestruidos() {
        return navioInimigoDestruidoLabels;
    }

    public boolean getOceanoClicavel() {
        return oceanoClicavel;
    }

    public void setRecebendoAtq(boolean valor) {
        recebendoAtq = valor;
    }

    public boolean getRecebendoAtq() {
        return recebendoAtq;
    }

    public boolean getOceanoInimigoClicavel() {
        return oceanoInimigoClicavel;
    }

    public void switchOceanoInimigo() {
        oceanoInimigoClicavel = !oceanoInimigoClicavel;
    }

    public void switchTurnoLabel() {
        turnoAtual = (turnoAtual + 1) % 2;
        if (turnoAtual == 0) {
            turnoLabel.setIcon(turnoVermelho);
        } else {
            turnoLabel.setIcon(turnoVerde);
        }
    }

    public void atualizarBotoes1() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                botoes1[j][i].addEventoBotaoJogador(cliente);
            }
        }
    }

    public void setTurno(int turnoN) {
        turnoAtual = turnoN;
        if (turnoN == 0) {
            turnoLabel.setIcon(turnoVermelho);
        } else {
            turnoLabel.setIcon(turnoVerde);
        }
    }

    @Override
    public void run() {
        System.out.println("run do tabuleiro");
        try {
            naviosPosicionados.acquire();

            System.out.println("naviosPosicionados");
            cliente.getJogadorPosicionado().release(); // libera o cliente

            cliente.getOponentePosicionado().acquire();

            botaoComecar.setBackground(Color.yellow);
            botaoComecar.setEnabled(true);

        } catch (InterruptedException ex) {
            Logger.getLogger(Tabuleiro.class.getName()).log(Level.SEVERE, null, ex);
        }

        //***********************************
        addNaviosInimigoLabel();
    }

    public void addNaviosInimigoLabel() {
        c4.removeAll();
        c4.setLayout(new GridLayout(1, 6));
        inimigoDestruidoText = new JLabel("Inimigos Destruídos:");
        inimigoDestruidoText.setFont(new Font("NI", Font.BOLD, 14));
        c4.add(inimigoDestruidoText);

        for (int i = 0; i < 5; i++) {
            navioInimigoDestruidoLabels[i] = new JLabel("");
            c4.add(navioInimigoDestruidoLabels[i]);
        }
    }

    public void criaTabuleiroInimigo(Botao[][] botoes) {
        Container tmpC = null;
        if (cliente.getNumeroJogador() == 0) {
            tmpC = c6; // tabuleiro leste
        } else if (cliente.getNumeroJogador() == 1) {
            tmpC = c2; // tabuleiro oeste
        }
        tmpC.removeAll();
        tmpC.setLayout(new GridLayout(11, 10));
        for (char ch = 'A'; ch <= 'J'; ch++) {
            tmpC.add(new JButton(String.valueOf(ch))).setEnabled(false);
        }
        botoes2 = botoes;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Botao b = botoes2[j][i];
                JButton jb = botoes2[j][i].getBotao();

                if (jb.isBackgroundSet()) {
                    jb.setBackground(null);
                }
                tmpC.add(jb);
                b.addEventoBotaoInimigo(cliente);
            }
        }
    }

    public Botao[][] getBotoes1() {
        return botoes1;
    }

    // adiciona evento aos botoes de seleçao dos navios
    private void selecionarNavios() {
        for (JButton b : jBarcos) {
            b.addActionListener((ActionEvent ae) -> {
                construindoNavio = false;
                if (navioAtual <= 3) {
                    selecionarDirecao(); // opcao de horizontal e vertical
                }
                oceanoClicavel = true; // torna os botoes do oceano clicaveis
                setCursorNavio(); // muda o cursor do mouse
            });
        }
    }

    public int percorreIndice(int i) {
        if (cliente.getNumeroJogador() == 0) {
            return i - 1;
        } else {
            return i + 1;
        }
    }

    public boolean verificaEspacoHorizontal(int i) {
        if (cliente.getNumeroJogador() == 0) {
            if (i + 1 - navios[navioAtual].getNCanos() >= 0) {
                return true;
            }
        } else if (i - 1 + navios[navioAtual].getNCanos() <= 9) {
            return true;
        }
        return false;
    }

    // recebe o botao auxiliar e o array de botoes
    private void escolherPosicao(Botao b, Botao[][] botoes) {
        construindoNavio = true;
        int count = 0; // verificar se ha espaço para o navio na posicao selecionada
        int i; // indice de percorrimento
        int cX = b.getX(); // coordenadas do botao auxiliar
        int cY = b.getY();

        if (direcao == Direcao.H) {// direção será selecionada no openDialog
            i = cX;
            //soma indice mais um e iguala ao botão e subtrai numero de canos do navio atual

            if (/*i+1 - navios[navioAtual].getNCanos() >= 0 */verificaEspacoHorizontal(i)) { // ha espaço pro navio?
                while (count++ < navios[navioAtual].getNCanos()) { // loopa n vezes num de canos
                    if (botoes[i][cY].getContemNavio()) { // existe navio na posicao?
                        msgDialog("Posição utilizada! Selecione navio novamente.");
                        return;
                    }
                    i = percorreIndice(i); // --  ou ++ no indice
                }
                count = 0;
                i = cX;
                while (count++ < navios[navioAtual].getNCanos()) { // loopa n vezes num de canos
                    botoes[i][cY].setContemNavio(true);
                    botoes[i][cY].setNavio(navios[navioAtual]);
                    if (i < cX) {
                        botoes[i][cY].getBotao().doClick();// clica nas demais posições do navio
                    }
                    botoes[i][cY].getBotao().setBackground(Color.ORANGE); // pinta posicao
                    i = percorreIndice(i);
                    System.out.println("pintou" + navios[navioAtual].getNCanos());
                }
            } else {
                msgDialog("Posicionamento invalido! Selecione navio novamente.");
                return;
            }

        } else if (direcao == Direcao.V) {
            i = cY;
            if (i - 1 + navios[navioAtual].getNCanos() <= 9) {
                while (count++ < navios[navioAtual].getNCanos()) {
                    if (botoes[cX][i++].getContemNavio()) {
                        msgDialog("Posição utilizada! Selecione navio novamente.");
                        return;
                    }
                }
                count = 0;
                i = cY;
                while (count++ < navios[navioAtual].getNCanos()) {
                    botoes[cX][i].setContemNavio(true);
                    botoes[cX][i].setNavio(navios[navioAtual]);
                    if (i > cY) {
                        botoes[cX][i].getBotao().doClick();
                    }
                    botoes[cX][i].getBotao().setBackground(Color.ORANGE);
                    i++;
                    System.out.println("pintou" + navios[navioAtual].getNCanos());
                }
            } else {
                msgDialog("Posicionamento invalido! Selecione navio novamente.");
                return;
            }
        }
        jBarcos[navioAtual].setEnabled(false); // desabilita botao atual
        if (navioAtual <= 3) {
            jBarcos[navioAtual + 1].setEnabled(true);
        }
        //navios[navioAtual].setUtilizado(true);
        navioAtual++;
        construindoNavio = false;
        if (navioAtual == 5) {
            naviosPosicionados.release();
            try {
                cliente.setTesteBotaoComecar(true);
                cliente.setTesteBotaoComecar(cliente.getSemComecar().tryAcquire(6000, TimeUnit.MILLISECONDS));

            } catch (InterruptedException ex) {
                Logger.getLogger(Tabuleiro.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (cliente.getTesteBotaoComecar()) {
                aguardaOponenteDialog();
            } else {
                cliente.erroConexaoDialog("Erro de conexão com o servidor, tente novamente!");
            }

        }
    }

    // muda o cursor do mouse
    private void setCursorNavio() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage(getClass().getClassLoader().getResource("bn\\res\\barco.png"));
        Point hotSpot = new Point(0, 0);
        Cursor cursor = toolkit.createCustomCursor(image, hotSpot, "Navio");
        setCursor(cursor);
    }

    public JDialog getdialog() {
        return waitdialog;
    }

    public JButton getcomecar() {
        return botaoComecar;
    }

    private void aguardaOponenteDialog() {

        waitdialog = new JDialog();
        Container cDialog = waitdialog.getContentPane();
        cDialog.setLayout(new FlowLayout());
        cDialog.setBackground(Color.darkGray);

        JLabel strLabel = new JLabel("O tabuleiro de ataque será liberado quando o inimigo posicionar os navios!");
        strLabel.setForeground(Color.white);
        cDialog.add(strLabel);

        botaoComecar.setFont(new Font("Começar!", Font.BOLD, 16));
        botaoComecar.setEnabled(true);
        botaoComecar.addActionListener((ActionEvent e) -> {
            repaint();
            revalidate();
            waitdialog.dispose();
        });
        cDialog.add(botaoComecar);

        waitdialog.setUndecorated(true); // retira opcoes de janela - [] x
        waitdialog.pack();
        waitdialog.setModal(true);
        waitdialog.setLocationRelativeTo(null);
        waitdialog.setSize(450, 60);
        waitdialog.setVisible(true);
        waitdialog.setResizable(false);

    }

    private void msgDialog(String msg) {
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

    // janela de escolher posicao (H / V) do navio
    private void selecionarDirecao() {
        JDialog dialog = new JDialog();
        Container cDialog = dialog.getContentPane();
        cDialog.setLayout(new FlowLayout());
        cDialog.setBackground(Color.black);

        JButton vertButton = new JButton("Vertical");
        JButton horiButton = new JButton("Horizontal");

        // atribui a posicao vertical a direcao
        vertButton.addActionListener((ActionEvent e) -> {
            direcao = Direcao.V;
            dialog.dispose();
        });
        // atribui a posicao horizontal a direcao
        horiButton.addActionListener((ActionEvent e) -> {
            direcao = Direcao.H;
            dialog.dispose();
        });
        // adiciona botoes ao container dialog
        cDialog.add(vertButton);
        cDialog.add(horiButton);

        dialog.setUndecorated(true); // retira opcoes de janela
        dialog.setModal(true);
        dialog.pack();
        Container tmpC = cliente.getNumeroJogador() == 0 ? c2 : c6;
        dialog.setLocationRelativeTo(tmpC);
        dialog.setSize(190, 37);
        dialog.setVisible(true);
        dialog.setResizable(false);
    }

    public Tabuleiro(Cliente cliente) {

        super("Batalha Naval Online");
        jBarcos = new JButton[5]; // navios para selecao
        oceanoClicavel = false; // oceano desativado 
        construindoNavio = false; // evita q um mesmo metodo seja chamado 
        navios = new Navio[5];
        navios[0] = new Navio(4, 0);
        navios[1] = new Navio(3, 1);
        navios[2] = new Navio(3, 2);
        navios[3] = new Navio(2, 3);
        navios[4] = new Navio(1, 4);
        navioAtual = 0;
        this.cliente = cliente;
        botaoComecar = new JButton("Começar!");
        naviosPosicionados = new Semaphore(0);
        turnoLabel = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("bn\\res\\graysignal.png")));
        navioInimigoDestruidoLabels = new JLabel[5];
        cliente.setFrameAtual(this);

        c = getContentPane();// recebe objeto Container principal
        c2 = new JPanel();
        Container c3 = new JPanel();
        c4 = new JPanel();
        c4.setPreferredSize(new Dimension(150, 41));
        Container c5 = new JPanel();
        c6 = new JPanel();
        Container c7 = new JPanel();
        //Define layout
        c.setLayout(new BorderLayout(5, 5));// Define tipo layout com distancia entre botoes(5, 5)
        c2.setLayout(new GridLayout(11, 10));// Layout de linhas e colunas (matriz) e (numeros)
        c3.setLayout(new GridLayout(11, 1));// Barra A a J 
        c4.setLayout(new GridLayout(1, 5));
        c6.setLayout(new GridLayout(11, 10));
        c7.setLayout(new GridLayout(1, 14));

        c.add(BorderLayout.CENTER, c2); // Preenche layout Center com novo layout Grid;
        c.add(BorderLayout.SOUTH, c4);
        c.add(BorderLayout.WEST, c3);// Preenche layout West com novo layout Grid;
        c.add(BorderLayout.EAST, c6);
        c.add(BorderLayout.NORTH, c7);

        c7.add(new JLabel(""));
        c7.add(new JLabel(""));
        c7.add(new JLabel(""));
        c7.add(new JLabel(""));

        // 03
        try {
            cliente.getSemaforoNickOponente().acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Tabuleiro.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (cliente.getNumeroJogador() == 0) {
            nickOeste = new JLabel(cliente.getNick());
            nickLeste = new JLabel(cliente.getNickOponente());
        } else {
            nickOeste = new JLabel(cliente.getNickOponente());
            nickLeste = new JLabel(cliente.getNick());
        }
        cliente.getSemaforoNick().release();

        nickOeste.setForeground(Color.red);
        nickOeste.setFont(new Font("Arial Black", Font.BOLD, 20));
        c7.add(nickOeste);
        c7.add(new JLabel(""));
        c7.add(new JLabel(""));
        c7.add(new JLabel(""));
        c7.add(turnoLabel);
        c7.add(new JLabel(""));
        c7.add(new JLabel(""));
        c7.add(new JLabel(""));
        Color customColor = new Color(0, 139, 0);
        nickLeste.setForeground(customColor);
        nickLeste.setFont(new Font("Arial Black", Font.BOLD, 20));
        c7.add(nickLeste);
        c7.add(new JLabel(""));
        c7.add(new JLabel(""));
        c7.add(new JLabel(""));

        // botoes dos navios do jogador
        jBarcos[0] = (JButton) c4.add(new JButton(
                new ImageIcon(getClass().getClassLoader().getResource("bn\\res\\4canos.jpg"))));
        jBarcos[1] = (JButton) c4.add(new JButton(
                new ImageIcon(getClass().getClassLoader().getResource("bn\\res\\3canos.jpg"))));
        jBarcos[2] = (JButton) c4.add(new JButton(
                new ImageIcon(getClass().getClassLoader().getResource("bn\\res\\3canos.jpg"))));
        jBarcos[3] = (JButton) c4.add(new JButton(
                new ImageIcon(getClass().getClassLoader().getResource("bn\\res\\2canos.jpg"))));
        jBarcos[4] = (JButton) c4.add(new JButton(
                new ImageIcon(getClass().getClassLoader().getResource("bn\\res\\1cano.jpg"))));

        // desabilita todos botoes, exceto o primeiro
        for (int i = 1; i < 5; i++) {
            jBarcos[i].setEnabled(false);
        }
        // retira a borda dos botoes
        for (JButton b : jBarcos) {
            b.setBorderPainted(false);
        }
        // "" 1 2 3 4 5 6 7 8 9 10
        c3.add(new JButton("")).setEnabled(false);
        for (int j = 1; j < 11; j++) {
            String num = Integer.toString(j);
            c3.add(new JButton(num)).setEnabled(false);
        }

        // A B C D E F G H I J
        for (char ch = 'A'; ch <= 'J'; ch++) {
            c2.add(new JButton(String.valueOf(ch))).setEnabled(false);
            c6.add(new JButton(String.valueOf(ch))).setEnabled(false);
        }

        criaTabuleiroJogador();
        criaTabuleiroInimigo();

        // Frame
        setResizable(false);
        setSize(915, 590);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setBackground(customColor);

        selecionarNavios();

    } // FIM DO CONSTRUTOR

    public void criaTabuleiroInimigo() {
        Container tmpC = null;
        if (cliente.getNumeroJogador() == 0) {
            tmpC = c6; // tabuleiro leste
        } else if (cliente.getNumeroJogador() == 1) {
            tmpC = c2; // tabuleiro oeste
        }
        // cria tabuleiro 02
        JButton[][] b = new JButton[10][10];
        for (int p = 0; p < 10; p++) {
            for (int q = 0; q < 10; q++) {
                b[q][p] = new JButton();
                b[q][p].setEnabled(false);
                tmpC.add(b[q][p]);
            }
        }
    }

    public void criaTabuleiroJogador() {
        Container tmpC = null;
        if (cliente.getNumeroJogador() == 0) {
            tmpC = c2; // tabuleiro oeste
        } else if (cliente.getNumeroJogador() == 1) {
            tmpC = c6; // tabuleiro leste
        }

        botoes1 = new Botao[10][10]; // tabuleiro jogador
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                botoes1[j][i] = new Botao(tmpC, j, i);
                Botao botaoAux = botoes1[j][i];
                botaoAux.getBotao().addActionListener((ActionEvent ae) -> { // adiciona eventos de todos os botões
                    if (!oceanoClicavel) { // se o ocenao estiver desativado não inicia o evento
                        return;
                    } else if (!construindoNavio) {
                        System.out.println("nao construindo");
                        if (navioAtual > 4) {
                            return;
                        } else {
                            // botaoAxu = referencia da posição atual do FOR
                            // Botoes1 = receberá a referencia do botãoAux
                            escolherPosicao(botaoAux, botoes1);
                            oceanoClicavel = false;
                            setCursor(null);
                        }
                    } else {
                        System.out.println("ta construindo...");
                    }
                    System.out.println("no botao oceano listener");
                });
            }
        } // fim da instancia do tabuleiro 1
    }
} // FIM DA CLASSE TABULEIRO
