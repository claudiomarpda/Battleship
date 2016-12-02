package bn;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Audio extends Thread {

    AudioClip atual;
    String str;

    public Audio(String str) {
        this.str = str;
        
            try {
                atual = Applet.newAudioClip(
                        new File("src\\bn\\audio\\" + str + ".wav").toURL());
            } catch (MalformedURLException ex) {
                Logger.getLogger(Audio.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        
    }

    @Override
    public void run() {
        if (str.equals("Agua") || str.equals("Navio")) {
            atual.play();
        } else {
            atual.loop();
        }
    }

    public AudioClip getAtual() {
        return atual;
    }

    /*public static void main(String[] args) {
     new Audio("win").start();
     }*/
}
