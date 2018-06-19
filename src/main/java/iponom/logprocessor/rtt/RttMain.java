package iponom.logprocessor.rtt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import iponom.logprocessor.Utils;

/**
 * @author Ilya.Ponomarev
 * @version 1.0 / 08.06.2018
 */
public class RttMain {

    private static final String PATH = "2018-06-09/rtt/";

    public static void main(String[] args) throws Exception {
        processFile("client53.log");
        processFile("client54.log");
        processFile("client55.log");
    }

    private static void processFile(String logFile) throws Exception {
        Path path = Paths.get(PATH + logFile);
        try (Stream<String> stream = Files.lines(path)) {
            List<Long> list = stream.filter(s -> Utils.isLong(s)).map(s -> new Long(s))
                    .sorted(Comparator.comparing(s -> (-s))).collect(Collectors.toList());
            System.out.println(logFile + " - " + list.stream().findFirst().orElseGet(() -> 0L));
            list.stream().limit(20).forEach(s -> System.out.println("" + s));
        }
    }


}
