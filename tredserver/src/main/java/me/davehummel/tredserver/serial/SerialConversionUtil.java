package me.davehummel.tredserver.serial;

/**
 * Created by dmhum_000 on 9/19/2015.
 */
public interface SerialConversionUtil {

    static int get32Int(byte[] bytes,int start){
        return (bytes[start+3] << (Byte.SIZE * 3))
                | ((bytes[start+2]  & 0xFF) << (Byte.SIZE * 2))
                | ((bytes[start+1]  & 0xFF) << (Byte.SIZE))
                | (bytes[start]  & 0xFF);
    }

    static long getU32Int(byte[] bytes,int start){
        return (((long)(bytes[start+3]& 0xFF) << (Byte.SIZE * 3))
                | ((bytes[start+2]  & 0xFF) << (Byte.SIZE * 2))
                | ((bytes[start+1]  & 0xFF) << (Byte.SIZE))
                | (bytes[start]  & 0xFF)
                ) & 0xFFFFFFFFL;
    }

    static long get64Int(byte[] bytes,int start){
        return (bytes[start+7] << (Byte.SIZE * 7))|((bytes[start+6]  & 0xFF) << (Byte.SIZE * 6))
                | ((bytes[start+5]  & 0xFF) << (Byte.SIZE * 5))
                | ((bytes[start+4]  & 0xFF) << (Byte.SIZE * 4))
                | ((bytes[start+3]  & 0xFF) << (Byte.SIZE * 3))
                | ((bytes[start+2]  & 0xFF) << (Byte.SIZE * 2))
                | ((bytes[start+1]  & 0xFF) << (Byte.SIZE))
                | (bytes[start]  & 0xFF);
    }

    static int getU16Int(byte[] bytes,int start){
        return (((bytes[start+1]  & 0xFF) << (Byte.SIZE))
                | (bytes[start]  & 0xFF)
                )& 0xFFFF;
    }

    static void getU16IntArray(byte[] bytes, int start, int[] dest){
        for (int i = 0 ; i < dest.length ; i ++){
            dest[i] = getU16Int(bytes,start+i*2);
        }
    }

    static int get16Int(byte[] bytes,int start){
        return ( ((bytes[start+1]  & 0xFF) << (Byte.SIZE))
                | (bytes[start]  & 0xFF));
    }

    static void get16IntArray(byte[] bytes, int start, int[] dest){
        for (int i = 0 ; i < dest.length ; i ++){
            dest[i] = get16Int(bytes,start+i*2);
        }
    }

    static int getU8Int(byte[] bytes,int start){
        return (bytes[start]  & 0xFF);
    }

    static byte get8Int(byte[] bytes,int start){
        return (bytes[start]);
    }

    static float getFloat(byte[] bytes,int start){
        float val = Float.intBitsToFloat(get32Int(bytes,start));
        return val;
    }

    static double getDouble(byte[] bytes,int start){
        double val = Double.longBitsToDouble(get64Int(bytes,start));
        return val;
    }
}
