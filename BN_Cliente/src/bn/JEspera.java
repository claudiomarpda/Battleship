package bn;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JEspera extends javax.swing.JFrame implements Runnable {

    private final Cliente cliente;
    private String nick = "";
    private ExecutorService exec;
    private Tabuleiro t;

    public JEspera(Cliente c) {
        initComponents();
        cliente = c;
        cliente.setFrameAtual(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(800, 600));
        setMinimumSize(new java.awt.Dimension(800, 600));
        setPreferredSize(new java.awt.Dimension(800, 600));
        setResizable(false);
        getContentPane().setLayout(null);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel2.setText("Aguardando oponente...");
        jLabel2.setVisible(false);
        getContentPane().add(jLabel2);
        jLabel2.setBounds(260, 60, 290, 29);

        jButton1.setBackground(new java.awt.Color(204, 255, 255));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setText("Ok");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1);
        jButton1.setBounds(470, 150, 60, 30);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel1.setText("Nick:");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(230, 150, 70, 30);

        jTextField1.setBackground(new java.awt.Color(233, 249, 249));
        jTextField1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField1);
        jTextField1.setBounds(300, 150, 160, 30);
        getContentPane().add(jLabel3);
        jLabel3.setBounds(0, 0, 0, 600);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bn/res/JEspera.jpg"))); // NOI18N
        jLabel4.setText("jLabel4");
        jLabel4.setMaximumSize(new java.awt.Dimension(800, 600));
        jLabel4.setMinimumSize(new java.awt.Dimension(800, 600));
        jLabel4.setPreferredSize(new java.awt.Dimension(800, 600));
        getContentPane().add(jLabel4);
        jLabel4.setBounds(0, 0, 800, 600);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        nick = jTextField1.getText();
        if (nick.matches("\\s+") || nick.matches("\\s+ \\S+ \\s*") || nick.equals("")
                || nick.matches("\\s* \\S+ \\s+")) {
            jTextField1.setText(null);
            return;
        } else {
            nick = jTextField1.getText();
            jTextField1.setEnabled(false);
            jLabel2.setVisible(true);
            jButton1.setEnabled(false);
            cliente.setAguardandoConexao(false);
            System.out.println("construtor do JEspera");
            cliente.getSemaforoBotaoOk().release();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run() {
        System.out.println("inicio do run JEspera");
        try {
            cliente.getConexoesOk().acquire();
        } catch (InterruptedException ex) {
            cliente.erroConexaoDialog(nick);
            Logger.getLogger(JEspera.class.getName()).log(Level.SEVERE, null, ex);

        }

        System.out.println("conexoes ok released jEspera");
        dispose();
        cliente.setNick(nick);

        cliente.newKeepAlive();

        //03
        cliente.getSemaforoNick().release();
        dispose();
        t = new Tabuleiro(cliente);
        exec = Executors.newFixedThreadPool(1);
        exec.execute(t);
    }

    public Tabuleiro getTabuleiro() {
        return t;
    }
}
