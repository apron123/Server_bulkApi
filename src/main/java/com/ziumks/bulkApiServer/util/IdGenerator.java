package com.ziumks.bulkApiServer.util;

import java.util.UUID;

public class IdGenerator {

    private final static char[] digits = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'a' , 'b' ,
            'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
            'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
            'o' , 'p' , 'q' , 'r' , 's' , 't' ,
            'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    private final static char[] digits64 = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', '~', '-'
    };


    private IdGenerator() {
        throw new IllegalStateException("Utility class cannot be Instance");
    }

    public static String getPrefixedUUID64(String prefix){
        if(!prefix.contains("_")){
            prefix += "_";
        }
        return prefix + getUUID64();
    }

    public static String getUUID64() {
        UUID uuid = UUID.randomUUID();
        String newUuid = procUUID64(uuid.getMostSignificantBits(), 6)+procUUID64(uuid.getLeastSignificantBits(), 6);
        return newUuid;
    }

    private static String procUUID64(long i, int shift) {
        char[] buf = new char[64];
        int charPos = 64;
        int radix = 1 << shift;
        long mask = radix - 1L;
        long number = i;

        do {
            buf[--charPos] = digits64[(int) (number & mask)];
            number >>>= shift;
        } while (number != 0);

        return new String(buf, charPos, (64 - charPos));
    }


    public static String getPrefixedUUID32(String prefix){
        if(!prefix.contains("_")){
            prefix += "_";
        }
        return prefix + getUUID32();
    }


    public static String getUUID32() {
        UUID uuid = UUID.randomUUID();
        char[] buffer = new char[32];
        long msbs = uuid.getMostSignificantBits();
        long lsbs = uuid.getLeastSignificantBits();
        long mask = 0x0f;
        int shift;
        for (int i = 0; i < 16; i++) {
            shift = (15 - i) * 4;
            buffer[i] = digits[(int)(((msbs & (mask << shift)) >>> shift))];
        }
        for (int i = 0; i < 16; i++) {
            shift = (15 - i) * 4;
            buffer[i + 16] = digits[(int)(((lsbs & (mask << shift)) >>> shift))];
        }

        return new String(buffer);
    }


}
