package iponom.logprocessor.shorty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static iponom.logprocessor.Utils.PREFIX;
import static iponom.logprocessor.Utils.isLong;

/**
 * @author Ilya.Ponomarev
 * @version 1.0 / 07.06.2018
 */
public class ShortMain {

    static class Container {
        String name;
        long time;
        long count;
        String firstLine;
        String lastLine;

        public Container(String name) {
            this.name = name;
        }

        static Container combine(Container first, Container second) {
            Container container = new Container(first.name);
            container.time = first.time + second.time;
            container.count = first.count + second.count;
            return container;
        }

        void print() {
            System.out.println("=======================================");
            System.out.println(time + " seconds " + name + " total time");
            System.out.println(count + " " + name + " total count");
        }

        long getExecTime() {
            return getTimestamp(lastLine) - getTimestamp(firstLine);
        }

        long getTimestamp(String line) {
            return new Long(line.split(",")[3]);
        }
    }

    private static final String PATH = PREFIX + "log/";

    /*
    0 node
    1 id
    2 timestamp
    3 interval
     */

    public static void main(String[] args) throws Exception {
        Container client = processFile("client.log", 2000, 1500, 1000, 500, 200);
        Container server = new Container("service");
        for (int i = 1; i < 4; i++) {
            server = Container.combine(server, processFile("SERVICE-" + i + ".log", 1000, 500, 200));
        }
        Container storage = new Container("storage");
        for (int i = 1; i < 10; i++) {
            storage = Container.combine(storage, processFile("STORAGE-" + i + ".log", 700, 500, 200));
        }
        //client.print();
        //server.print();
        //storage.print();
        //long execTime = client.getExecTime() / 1000;
        //System.out.println("=======================================");
        //System.out.println((execTime / 60) + " minutes execution time");
        //System.out.println((client.count / execTime) + " update calls per second");
    }

    private static Container processFile(String logFile, long... intervals) throws Exception {
        Container container = new Container("client");
        System.out.println("=========== " + logFile + "==============");
        Path path = Paths.get(PATH + logFile);
        try (Stream<String> stream = Files.lines(path)) {
            List<Long> list = stream
                    .skip(2000) // TODO remove ?
                    .map(s -> s.split(","))
                    .filter(arr -> arr.length == 6 && "1".equals(arr[5]) && isLong(arr[3]) && isLong(arr[4]) && new Long(arr[4]) > 200) //arr.length == 6 &&
                    .map(arr -> new Long(arr[4]))
                    //.sorted(Comparator.comparing(s -> (-s)))
                    .collect(Collectors.toList());
            //System.out.println("list created");
            //container.count = list.size();
            //container.time = list.stream().collect(Collectors.summingLong(Long::longValue)) / 1000;
            for (long interval: intervals) {
                System.out.println(list.stream().filter(s -> s > interval).count() + " more then " + interval + " ms");
            }
            //print longest intervals
            //list.stream().limit(count).forEach(s -> System.out.println("" + s));

//            try (Stream<String> stream2 = Files.lines(path)) {
//                container.firstLine = stream2.findFirst().get();
//            }
//            try (Stream<String> stream3 = Files.lines(path)) {
//                container.lastLine = stream3.skip(container.count - 1).findFirst().get();
//            }

            return container;
        }
    }

}
