package bn;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class JMenu extends javax.swing.JFrame {

    private final Cliente cliente;
    private JEspera jEspera;
    private Executor exec;
    private final int PORTA_SERVIDOR = 5000;

    public JMenu(Cliente c) {
        //TocarAudio som = new TocarAudio() {};
        //Thread t1 = new Thread(som);
        //t1.start();
        setTitle("Batalha Naval Online");
        setVisible(true);
        setSize(800, 600);
        setLocationRelativeTo(null);
        initComponents();
        cliente = c;
        cliente.setFrameAtual(this);
        //cliente.getAudio().getAtual().stop();
        cliente.setAudio(new Audio("Menu"));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(800, 600));
        setResizable(false);
        getContentPane().setLayout(null);

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jButton1.setText("Play Game");
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                executorTabuleiro(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button1CollorRed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button1CollorBlack(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1);
        jButton1.setBounds(80, 180, 220, 45);

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jButton2.setText("Options");
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setFocusPainted(false);
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button2CollorRed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button2CollorBlack(evt);
            }
        });
        getContentPane().add(jButton2);
        jButton2.setBounds(80, 220, 210, 53);

        jButton3.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jButton3.setText("Quit Game");
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setFocusPainted(false);
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                quitGame(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button3CollorRed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button3CollorBlack(evt);
            }
        });
        getContentPane().add(jButton3);
        jButton3.setBounds(100, 280, 170, 31);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bn/res/JMenu.jpg"))); // NOI18N
        getContentPane().add(jLabel1);
        jLabel1.setBounds(0, 0, 820, 570);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button1CollorBlack(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button1CollorBlack
        jButton1.setForeground(Color.black);
    }//GEN-LAST:event_button1CollorBlack

    private void button1CollorRed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button1CollorRed
        jButton1.setForeground(Color.red);
    }//GEN-LAST:event_button1CollorRed

    private void button2CollorBlack(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button2CollorBlack
        jButton2.setForeground(Color.black);
    }//GEN-LAST:event_button2CollorBlack

    private void button2CollorRed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button2CollorRed
        jButton2.setForeground(Color.red);
    }//GEN-LAST:event_button2CollorRed

    private void button3CollorBlack(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button3CollorBlack
        jButton3.setForeground(Color.black);
    }//GEN-LAST:event_button3CollorBlack

    private void button3CollorRed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button3CollorRed
        jButton3.setForeground(Color.red);
    }//GEN-LAST:event_button3CollorRed

    private void executorTabuleiro(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_executorTabuleiro
        boolean conexaoOk = true;
        try {
            cliente.setSocket(new Socket("localhost", PORTA_SERVIDOR));
            cliente.setOutput(new DataOutputStream(cliente.getSocket().getOutputStream()));
            cliente.setInput(new DataInputStream(cliente.getSocket().getInputStream()));

        } catch (IOException ex) {
            conexaoOk = false;
            cliente.erroConexaoDialog("Erro de conexão com o servidor, tente novamente!");
        }
        if (conexaoOk) {
            dispose();
            exec = Executors.newFixedThreadPool(1);
            jEspera = new JEspera(cliente);
            jEspera.setTitle("Batalha Naval Online");
            jEspera.setVisible(true);
            jEspera.setSize(800, 600);
            jEspera.setLocationRelativeTo(null);
            exec.execute(jEspera);
        }
    }//GEN-LAST:event_executorTabuleiro

    private void quitGame(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quitGame
        System.exit(1);
    }//GEN-LAST:event_quitGame

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    public JEspera getJEspera() {
        return jEspera;
    }
}
