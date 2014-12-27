package eu.laprell.timetable.utils;

import eu.laprell.timetable.database.TimeUnit;

/**
 * Created by david on 09.11.14.
 */
public class ArrayUtils {

    public static String[] explode(String delimiter, String content) {
        if(content == null || content.length() == 0)
            return new String[0];

        return content.split(delimiter);
    }

    public static long[] explodeLong(String delemiter, String content) {
        String[] s = explode(delemiter, content);
        long[] l = new long[s.length];
        for(int i = 0;i < l.length;i++)
            l[i] = Long.parseLong(s[i]);
        return l;
    }

    public static String implode(String delimiter, String[] content) {
        StringBuilder b = new StringBuilder();

        for(int i = 0;i < content.length;i++) {
            if(i != 0) b.append(delimiter);
            b.append(content[i]);
        }

        return b.toString();
    }

    public static String implode(String delimiter, long[] l) {
        String[] s = new String[l.length];
        for(int i = 0;i < l.length;i++)
            s[i] = String.valueOf(l[i]);
        return implode(delimiter, s);
    }

    public static String[] convertIntToStringArray(int[] input) {
        String[] output = new String[input.length];
        
        for(int i = 0;i < input.length;i++){
            output[i] = String.valueOf(input[i]);
        }

        return output;
    }

    public static long[] expandByOne(long[] in) {
        long[] out = new long[in.length + 1];
        System.arraycopy(in, 0, out, 0, in.length);
        return out;
    }

    public static int getTimeUnitAfter(TimeUnit t, TimeUnit[] all) {
        for (int i = 0; i < all.length;i++) {
            if(all[i].isAfter(t))
                return i;
        }
        return -1;
    }
}
