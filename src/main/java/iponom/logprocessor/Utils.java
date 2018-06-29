package iponom.logprocessor;

/**
 * @author Ilya.Ponomarev
 * @version 1.0 / 07.06.2018
 */
public interface Utils {

    String PREFIX = "results/2018-06-28-3/";

    static boolean isLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch(NumberFormatException nfe) {
            return false;
        }
    }

    static long getSkipCount(String logFile, long base) {
        if (logFile.startsWith("client"))  return base;
        else if (logFile.startsWith("SERVER"))  return base / 3;
        else return base / 9;
    }

}
