package com.modsim.util;

/**
 * Useful helper class for dealing with binary data
 * @author aw12700
 *
 */
public class BinData {

    public static final byte LOW = 0;
    public static final byte HIGH = 1;
    public static final byte NOCON = 2;

    //private byte[] val = new byte[4];
    public byte val = 0;
    public byte mask = 0;

    /**
     * Copy binary data
     */
    public BinData(BinData d) {
        val = d.val;
        mask = d.mask;
    }

    /**
     * New binary data.
     */
    public BinData(byte val, byte mask) {
        this.val = val;
        this.mask = mask;
    }

    /**
     * New binary data.
     */
    public BinData(int b3, int b2, int b1, int b0) {
        mask = 0xF;
        val = (byte) (((b3&1)<<3) | ((b2&1)<<2) | ((b1&1)<<1) | (b0&1));
    }

    /**
     * New binary data.
     */
    public BinData(boolean bool3, boolean bool2, boolean bool1, boolean bool0) {
        mask = 0xF;
        int b3 = bool3 ? 1 : 0;
        int b2 = bool2 ? 1 : 0;
        int b1 = bool1 ? 1 : 0;
        int b0 = bool0 ? 1 : 0;
        val = (byte) (((b3&1)<<3) | ((b2&1)<<2) | ((b1&1)<<1) | (b0&1));
    }

    /**
     * New binary data.
     */
    public BinData(byte[] d) {
        this(   d.length >= 3 ? d[3] : NOCON,
                d.length >= 2 ? d[2] : NOCON,
                d.length >= 1 ? d[1] : NOCON,
                d.length >= 0 ? d[0] : NOCON );
    }

    /**
     * New binary data.
     * All bits set to NOCON
     */
    public BinData() {
        val = 0;
        mask = 0;
    }

    /**
     * New binary data (treats int as unsigned)
     */
    public BinData(int i) {
        setUInt(i);
    }

    /**
     * Sets (all bits) to a boolean
     */
    public void setBool(boolean b) {
        val = (byte)(b ? 0xF : 0);
        mask = 0xF;
    }

    /**
     * Sets (individual bits) to a boolean
     */
    public void setBool(boolean bool3, boolean bool2, boolean bool1, boolean bool0) {
        mask = 0xF;
        int b3 = bool3 ? 1 : 0;
        int b2 = bool2 ? 1 : 0;
        int b1 = bool1 ? 1 : 0;
        int b0 = bool0 ? 1 : 0;
        val = (byte) (((b3&1)<<3) | ((b2&1)<<2) | ((b1&1)<<1) | (b0&1));
    }

    /**
     * Sets to an unsigned binary number
     * (Ignores integer's sign)
     */
    public void setUInt(int n) {
        val = (byte) (n&0xF);
        mask = 0xF;
    }

    /**
     * Sets to a signed integer
     * (Uses int's highest bit for bit 3)
     */
    public void setInt(int n) {
        val = (byte) (n&7);
        val |= (byte) ((n < 0 ? 1 : 0) << 3);
        mask = 0xF;
    }

    /**
     * Gets the array of bits.
     */
    public byte[] getAll() {
        byte[] ret = new byte[4];
        for (int i = 0; i < 4; i++) {
            ret[i] = getBit(i);
        }
        return ret;
    }

    /**
     * Gets as an unsigned integer
     * Ignores mask (!)
     */
    public int getUInt() {
        int n = val;
        return n;
    }

    /**
     * Retrieve a binary bit
     * @param bit Bit index
     * @return Bit value (0, 1, or NOCON)
     */
    public byte getBit(int bit) {
        byte b = (byte) ((val >> bit) & 1);
        byte m = (byte) ((mask >> bit) & 1);
        return m == 1 ? b : NOCON;
    }

    public boolean getBooleanBit(int bit) {
        return getBit(bit) == HIGH;
    }

    /**
     * Set a binary bit
     * @param bit Bit index
     * @param v New Bit value (0, 1, or NOCON)
     */
    public void setBit(int bit, int v) {
        byte b = (byte) (1 << bit);
        byte bv = (byte) ((v&1) << bit);

        // Set bit
        if (v == NOCON) {
            mask &= ~b;
        }
        else {
            mask |= b;
            val &= ~b;
            val |= bv;
        }
    }

    /**
     * Resolve a binary bit
     * @param bit Bit index
     * @param v Bit value (no change if NOCON)
     */
    public void resolveBit(int bit, int v) {
        if (v == NOCON) return;
        setBit(bit, v);
    }

    public void setBooleanBit(int bit, boolean v) {
        setBit(bit, v ? HIGH : LOW);
    }

    /**
     * Equality test
     */
    public boolean equals(BinData d) {
        if ((val & mask) == (d.val & d.mask) && mask == d.mask) return true;
        else return false;
    }

    /**
     * Pull resolution
     * Sets NOCON bits in this to match corresponding bits in pull
     */
    public void resolvePull(BinData pull) {
        val &= mask; // clear NOCON bits
        val |= (~mask) & pull.val; // insert pull bits
        mask = 0xF;
    }

    /**
     * Formats to string
     */
    @Override
    public String toString() {
        String s = "";
        for (int i = 3; i >= 0; i--) {
            int b = getBit(i);
            switch (b) {
            case HIGH:
            case LOW:
                s = s + b;
                break;
            case NOCON:
                s = s + "x";
                break;
            }
        }
        return s;
    }

    /**
     * Set to match given data
     * @param v
     */
    public void set(BinData v) {
        val = v.val;
        mask = v.mask;
    }

    public static int mergeBits(byte b0, byte b1) {
        if (b0 == 2 && b1 == 2) {
            return 2;
        }
        else if (b0 == 2) {
            return b1;
        }
        else if (b1 == 2) {
            return b0;
        }
        else {
            return b0 | b1;
        }
    }
}
