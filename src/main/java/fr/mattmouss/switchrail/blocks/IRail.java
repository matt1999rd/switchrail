package fr.mattmouss.switchrail.blocks;

public interface IRail {
    /***
     * enleve le block si le block en dessous est de l'air
     */
    public void verifyBlockUnderneath();
}
