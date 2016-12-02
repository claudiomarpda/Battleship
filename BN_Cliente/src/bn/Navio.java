package bn;

import java.io.Serializable;

public class Navio implements Serializable {

    private final int nCanos;
    private int ataquesRecebidos;  //************************
    private int indice;

    public Navio(int canos, int i) {
        nCanos = canos;
        ataquesRecebidos = 0;
        indice = i;
    }

    //***********************
    public boolean destruirNavio() {
        return ++ataquesRecebidos == nCanos;
    }

    public int getNCanos() {
        return nCanos;
    }

    public int getIndice() {
        return indice;
    }
}
