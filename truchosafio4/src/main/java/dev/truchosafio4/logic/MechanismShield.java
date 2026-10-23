package dev.truchosafio4.logic;

/** Persistent membership, independent of chunk loading or the boss moving away. */
public final class MechanismShield {
    public static final int TOTAL=14;
    private int livingMask;
    public int mask(){return livingMask;}
    public void restore(int mask){livingMask=mask & ((1<<TOTAL)-1);}
    public void add(int slot){if(slot>=0 && slot<TOTAL)livingMask|=1<<slot;}
    public boolean destroy(int slot){
        if(slot<0 || slot>=TOTAL)return false;
        int old=livingMask;livingMask &= ~(1<<slot);return old!=livingMask;
    }
    public int remaining(){return Integer.bitCount(livingMask);}
    public boolean protects(){return livingMask!=0;}
}
