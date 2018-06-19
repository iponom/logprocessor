package iponom.logprocessor.extreme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import iponom.logprocessor.Utils;

/**
 * @author Ilya.Ponomarev
 * @version 1.0 / 15.06.2018
 */
public class ExtremeMain {

    /*
    0 node
    1 thread
    2 id
    3 timestamp
    4 interval
     */

    private static final String PATH = "2018-06-18-2/log/";

    private static final String CLIENT = "client.log";
    private static final String[] SERVICES = {"SERVICE-1.log", "SERVICE-2.log", "SERVICE-3.log"};
    private static final String[] STORAGES = {"STORAGE-1.log", "STORAGE-2.log", "STORAGE-3.log", "STORAGE-4.log", "STORAGE-5.log", "STORAGE-6.log", "STORAGE-7.log", "STORAGE-8.log", "STORAGE-9.log"};

    private static final List<String> ALL_LOGS = new ArrayList<>();
    static {
        ALL_LOGS.add(CLIENT);
        ALL_LOGS.addAll(Arrays.asList(SERVICES));
        ALL_LOGS.addAll(Arrays.asList(STORAGES));
    }

    public static void main(String[] args) throws Exception {
        Collection<String> badSessions = findBadSessions();
       // badSessions.stream().forEach(ExtremeMain::processSession);

        //badSessions.forEach(System.out::println);
        //trim();

        Map<String, List<String[]>> map = new HashMap<>();
        for (String log : ALL_LOGS) {
            Map<String, List<String[]>> logMap = processSessions(badSessions, log);
            for (String session : logMap.keySet()) {
                if (map.containsKey(session)) {
                    map.put(session, Stream.of(map.get(session), logMap.get(session)).flatMap(Collection::stream).collect(Collectors.toList()));
                } else {
                    map.put(session, logMap.get(session));
                }
            }
        }
        map.keySet().forEach(session -> toFile(session, map.get(session)));

    }

    private static void toFile(String session, List<String[]> strings) {
        Stream<String> result = strings.stream()
                .sorted(Comparator.comparing(arr -> new Long(arr[3]) ) )
                .map(arr -> arr[0] + "," + arr[3] + "," + arr[4]);
        try {
            Files.write(Paths.get(PATH  + "sessions/" + session + ".log"), (Iterable<String>)result::iterator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, List<String[]>> processSessions(Collection sessions, String log) throws Exception {
        Path path = Paths.get(PATH + log);
        try (Stream<String> stream = Files.lines(path)) {
            return stream.map(s -> s.split(","))
                    .filter(arr -> arr.length == 5 && sessions.contains(arr[2]))
                    .collect(Collectors.groupingBy((String[] arr) -> arr[2]));
        }
    }

    private static Collection<String> findBadSessions() throws Exception{
        Path path = Paths.get(PATH + CLIENT);
        try (Stream<String> stream = Files.lines(path)) {
            return stream.map(s -> s.split(","))
                    .filter(arr -> arr.length == 5 && Utils.isLong(arr[3]) && Utils.isLong(arr[4]))
                    // to reduce sorting time
                    .filter(arr -> new Long(arr[4]) > 1000)
                    .sorted( Comparator.comparing(s -> (- new Long(s[4]) ) ) )
                    // almost session count
                    .limit(10)
                    // map to print
                    //.map(arr ->  "1" + "," + arr[2] + "," + arr[3] + "," + arr[4])
                    .map(arr ->  arr[2])
                    //.distinct()
                    .collect(Collectors.toSet());

        }
    }

    // remove thread column
    private static void trim() throws Exception {
        Path path = Paths.get(PATH + CLIENT);
        try (Stream<String> stream = Files.lines(path)) {
            Stream<String> result = stream
                    //.map(s -> s.split(","))
                    //.filter(arr -> arr.length == 5 && isLong(arr[3]) && isLong(arr[4]))
                    .map(str -> str.replace("client,$", "CLIENT-00,"));
            Files.write(Paths.get(PATH + "client1.log"), (Iterable<String>)result::iterator);

        }
    }

}
