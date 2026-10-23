package dev.truchosafio4.logic;

/** Runnable without Minecraft, exercising the production shield state machine. */
public final class MechanismShieldTest {
    private static int checks;
    private static void check(boolean value){checks++;if(!value)throw new AssertionError("check "+checks);}
    public static void main(String[] args){
        MechanismShield shield=new MechanismShield();
        check(!shield.protects());
        for(int slot=0;slot<14;slot++){shield.add(slot);check(shield.remaining()==slot+1);}
        check(shield.protects());check(!shield.destroy(-1));check(!shield.destroy(14));
        MechanismShield restored=new MechanismShield();restored.restore(shield.mask());
        check(restored.remaining()==14);
        for(int slot=13;slot>=0;slot--){
            check(restored.destroy(slot));check(!restored.destroy(slot));
            check(restored.remaining()==slot);check(restored.protects()==(slot>0));
        }
        restored.restore(-1);check(restored.remaining()==14);
        restored.restore(1<<20);check(!restored.protects());
        System.out.println("PASS: "+checks+" shield assertions (14 mechanisms, persistence, duplicates, invalid slots)");
    }
}
