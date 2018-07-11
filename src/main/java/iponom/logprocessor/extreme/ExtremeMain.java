package iponom.logprocessor.extreme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import iponom.logprocessor.Utils;

import static iponom.logprocessor.Utils.PREFIX;
import static iponom.logprocessor.Utils.getSkipCount;

/**
 * @author Ilya.Ponomarev
 * @version 1.0 / 15.06.2018
 */
public class ExtremeMain {

    /*
    0 file - added dynamically
    1 id
    2 timestamp
    3 interval
    4 number
     */

    private static final String PATH = PREFIX + "log/";

    private static final String CLIENT = "client";
    private static final String[] SERVICES = {"SERVICE-1", "SERVICE-2", "SERVICE-3"};
    private static final String[] STORAGES = {"STORAGE-1", "STORAGE-2", "STORAGE-3", "STORAGE-4", "STORAGE-5", "STORAGE-6", "STORAGE-7", "STORAGE-8", "STORAGE-9"};

    private static final List<String> ALL_LOGS = new ArrayList<>();

    static {
        ALL_LOGS.add(CLIENT);
        ALL_LOGS.addAll(Arrays.asList(SERVICES));
        ALL_LOGS.addAll(Arrays.asList(STORAGES));
    }

    public static void main(String[] args) throws Exception {
        processMaxSessionsFromFile(CLIENT);
//        for (String name : ALL_LOGS) {
//            processMaxSessionsFromFile(name);
//        }
    }

    private static void processMaxSessionsFromFile(String name) throws Exception {
        Collection<String> badSessions = findBadSessions(name);
        Map<String, List<String[]>> map = new HashMap<>();
        for (String log : ALL_LOGS) {
            Map<String, List<String[]>> logMap = processSessions(badSessions, log + ".log");
            for (String session : logMap.keySet()) {
                if (map.containsKey(session)) {
                    map.put(session, Stream.of(map.get(session), logMap.get(session)).flatMap(Collection::stream).collect(Collectors.toList()));
                } else {
                    map.put(session, logMap.get(session));
                }
            }
        }
        Files.createDirectory(Paths.get(PATH + name));
        map.keySet().forEach(session -> toFile(session, map.get(session), name));
        System.out.println(name + " processed");
    }

    private static void toFile(String session, List<String[]> strings, String name) {
        Stream<String> result = strings.stream()
                .sorted(Comparator.comparing(arr -> new Long(arr[2]) ) )
                .map(arr -> arr[0] + "," + arr[2] + "," + arr[3]+ "," + arr[4]);
        try {
            Files.write(Paths.get(PATH  + name + "/" + session + ".log"), (Iterable<String>)result::iterator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, List<String[]>> processSessions(Collection sessions, String log) throws Exception {
        Path path = Paths.get(PATH + log);
        try (Stream<String> stream = Files.lines(path)) {
            return stream.map(s -> log + "," + s).map(s -> s.split(","))
                    .filter(arr -> sessions.contains(arr[1])) //arr.length == 6 &&
                    .collect(Collectors.groupingBy((String[] arr) -> arr[1]));
        }
    }

    private static Collection<String> findBadSessions(String name) throws Exception{
        Path path = Paths.get(PATH + name + ".log");
        try (Stream<String> stream = Files.lines(path)) {
            return stream.map(s -> s.split(","))
                    .filter(arr -> arr.length == 4 && Utils.isLong(arr[1]) && Utils.isLong(arr[2]))
                    .skip(getSkipCount(name, Utils.BASE)) //skip some first bad results
                    // to reduce sorting time
                    .filter(arr -> new Long(arr[2]) > 500) // get all intervals more then 500 ms
                    .sorted( Comparator.comparing(s -> (- new Long(s[2]) ) ) )
                    // almost session count
                    .limit(20)
                    // map to print
                    //.map(arr ->  "1" + "," + arr[2] + "," + arr[3] + "," + arr[4])
                    .map(arr ->  arr[0])
                    //.distinct()
                    .collect(Collectors.toSet());

        }
    }

}
