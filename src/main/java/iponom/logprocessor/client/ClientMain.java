package iponom.logprocessor.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static iponom.logprocessor.Utils.PREFIX;

/**
 *
 * @author Ilya.Ponomarev
 * @version 1.0 / 04.04.2018
 */
public class ClientMain {

    private static final String SEP = ";";
    private static final String PATH = PREFIX + "result/";

    public static void main(String[] args) throws IOException {
        Stream<String> result = navigate(Paths.get(PATH));
        //result.forEach(System.out::println);
        Files.write(Paths.get(PATH + "/../out.csv"), result.filter(s -> !s.endsWith(";0;0;0.0;0.0")).collect(Collectors.toList()));
    }

    private static Stream<String> navigate(Path path) {
        if (Files.isDirectory(path)) {
            try {
                return Files.list(path).flatMap(ClientMain::navigate);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Stream.of(run(path));
        }
    }

    // 0. Time, 1. Number, 2. Average, 3. Max, 4. <200, 5. 200+, 6. 95.0, 7. 99.0, 8. 99.9
    private static String run(Path path) {
        String str = Paths.get(PATH).relativize(path).toString();
        try (Stream<String> stream = Files.lines(path)) {
            List<String[]> list = stream.skip(1).map(s -> s.split(",")).collect(Collectors.toList());
            long totalCount = list.stream()
                    .filter(arr -> isNumber(arr[1]))
                    .map(arr -> new Long(arr[1])).collect(Collectors.summingLong(Long::longValue));
            long moreThan200Count = list.stream()
                    .filter(arr -> isNumber(arr[5]))
                    .map(arr -> new Long(arr[5])).collect(Collectors.summingLong(Long::longValue));
            double average = list.stream()
                    .filter(arr -> isNumber(arr[2]))
                    .map(arr -> new Double(arr[2])).collect(Collectors.averagingDouble(Double::doubleValue));
            Double max = list.stream()
                    .filter(arr -> isNumber(arr[3]))
                    .map(arr -> new Double(arr[3])).max(Double::compareTo).orElseGet(() -> 0D);
            long moreThan20Percent = totalCount > 0 ? moreThan200Count * 100 / totalCount : 0;
            return  str + SEP + totalCount + SEP + moreThan200Count + SEP + average + SEP + max + SEP + moreThan20Percent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException nfe) {
            return false;
        }
    }
}
