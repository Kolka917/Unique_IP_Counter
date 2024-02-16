package org.ecwid_by_lightspeed.helper;


/**
 * Кастомная структура данных на основе BitSet для учета уникальности
 */
public class CustomBitSetHelper {
    private final byte[] data;

    public CustomBitSetHelper() {
        this.data = new byte[(Integer.MAX_VALUE / 4) + 1];
    }

    public void set(int num) {
        int byteIndex;
        int bitOffset;
        if (num < 0) {
            long numLong = (long) Integer.MAX_VALUE - num;
            byteIndex = (int) (numLong / 8);
            bitOffset = (int) (numLong % 8);
        } else {
            byteIndex = (num / 8);
            bitOffset = (num % 8);
        }

        data[byteIndex] |= (1 << bitOffset);

    }

    public boolean isSet(int num) {
        int byteIndex;
        int bitOffset;
        if (num < 0) {
            long numLong = (long) Integer.MAX_VALUE - num;
            byteIndex = (int) (numLong / 8);
            bitOffset = (int) (numLong % 8);
        } else {
            byteIndex = (num / 8);
            bitOffset = (num % 8);
        }

        return ((data[byteIndex] & 0xff) & (1 << bitOffset)) != 0;
    }

    public long countSetBits() {
        long count = 0;
        for (byte value : data) {
            count += Long.bitCount(value & 0xFF);
        }
        return count;
    }
}

