package iponom.logprocessor.shorty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import iponom.logprocessor.Utils;

import static iponom.logprocessor.Utils.PREFIX;
import static iponom.logprocessor.Utils.getSkipCount;
import static iponom.logprocessor.Utils.isLong;

/**
 * @author Ilya.Ponomarev
 * @version 1.0 / 07.06.2018
 */
public class ShortMain {

    private static final String PATH = PREFIX + "log/";

    /*
    0 node
    1 id
    2 timestamp
    3 interval
     */

    public static void main(String[] args) throws Exception {
        processFile("client.log", 500, 400, 200);
        for (int i = 1; i < 4; i++) {
            processFile("SERVICE-" + i + ".log", 400, 200, 100);
        }
        for (int i = 1; i < 10; i++) {
            processFile("STORAGE-" + i + ".log", 400, 200, 100);
        }
    }

    private static void processFile(String logFile, long... intervals) throws Exception {
        long minInterval = Arrays.stream(intervals).min().orElse(0);
        System.out.println("=========== " + logFile + "==============");
        Path path = Paths.get(PATH + logFile);
        try (Stream<String> stream = Files.lines(path)) {
            List<Long> list = stream
                    .skip(getSkipCount(logFile, Utils.BASE))
                    .map(s -> s.split(","))
                    .filter(arr -> arr.length == 4 && "1".equals(arr[3]) && isLong(arr[1]) && isLong(arr[2]) && new Long(arr[2]) > minInterval)
                    .map(arr -> new Long(arr[2]))
                    .collect(Collectors.toList());
            for (long interval: intervals) {
                System.out.println(list.stream().filter(s -> s > interval).count() + " more then " + interval + " ms");
            }
        }
    }

}
