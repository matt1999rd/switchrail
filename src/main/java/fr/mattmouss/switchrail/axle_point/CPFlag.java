package fr.mattmouss.switchrail.axle_point;

public enum CPFlag {
    FROM_OUTSIDE(1),
    ADD_AXLE(2),
    BIDIRECTIONAL(4);
    final int mask;
    CPFlag(int mask){
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }
}
