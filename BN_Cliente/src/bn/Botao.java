package bn;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Botao implements Serializable {

    private JButton botao; // botoes do oceano
    private int x; // posicao do array no oceano
    private int y;
    private boolean contemNavio; // botão utilizado por navio
    private Navio navio;
    private boolean parteNavioDestruida;

    public Botao(Container c, int x, int y) {
        botao = new JButton();
        c.add(botao); // adiciona botao no container recebido
        this.x = x;
        this.y = y;
        contemNavio = false;
        parteNavioDestruida = false;
    }

    public void addEventoBotaoInimigo(Cliente cliente) {
        Tabuleiro tabuleiro = cliente.getTabuleiro();
        botao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!tabuleiro.getOceanoInimigoClicavel()) {
                    return;
                } else {
                    // 07
                    cliente.enviarAtq(x, y);
                    System.out.println("no botao x e y " + x + " " + y);

                    if (contemNavio && !parteNavioDestruida) {
                        cliente.getExecAudio().execute(new Audio("Navio"));
                        botao.setBackground(Color.red);
                        destroiNavio(cliente);
                        parteNavioDestruida = true;

                        // 08
                        //cliente.enviarMsg("navioDestruido");
                        // 09
                        cliente.enviarMsg("continuaAtq");
                        return; // acertou um alvo e joga de novo
                    } else {
                        cliente.getExecAudio().execute(new Audio("Agua"));
                        tabuleiro.switchOceanoInimigo();
                        if (!parteNavioDestruida) {
                            botao.setBackground(Color.darkGray);
                        }
                        tabuleiro.switchTurnoLabel();

                        // 09
                        cliente.enviarMsg("fimDeTurno");
                    }
                }
            }
        });
    }

    public void addEventoBotaoJogador(Cliente cliente) {
        Tabuleiro tabuleiro = cliente.getTabuleiro();
        botao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                if (!cliente.getAntesDoCLick()) {
                    System.out.println("nao pode receber atq");
                    return;
                } else {
                    if (contemNavio && !parteNavioDestruida) {
                        cliente.getExecAudio().execute(new Audio("Navio"));
                        botao.setBackground(Color.red);
                        parteNavioDestruida = true;
                        //08
                        // Cliente => Servidor
                        // envia msg "fimDeJogo" ou "continuaJogo"
                    } else {
                        cliente.getExecAudio().execute(new Audio("Agua"));

                        // 08
                        botao.setBackground(Color.darkGray);
                        tabuleiro.switchTurnoLabel();
                    }
                    tabuleiro.setRecebendoAtq(false);
                }
            }
        });
    }

    public void destroiNavio(Cliente cliente) {
        System.out.println("funcao destroiNavio");
        if (navio.destruirNavio()) {
            System.out.println("navio destruido ********************" + navio.getNCanos() + " canos");
            JLabel tmpLabel = cliente.getTabuleiro().getNaviosInimigosDestruidos()[navio.getIndice()];
            tmpLabel.setText(navio.getNCanos() + " cano(s)");
        }
    }

    public void setNavio(Navio n) {
        navio = n;
    }

    public void setContemNavio(boolean valor) {
        contemNavio = valor;
    }

    public boolean getContemNavio() {
        return contemNavio;
    }

    public JButton getBotao() {
        return botao;
    }

    public void setBotao(JButton botao) {
        this.botao = botao;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
