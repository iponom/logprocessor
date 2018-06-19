package iponom.logprocessor;

/**
 * @author Ilya.Ponomarev
 * @version 1.0 / 07.06.2018
 */
public interface Utils {

    static boolean isLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch(NumberFormatException nfe) {
            return false;
        }
    }

}
