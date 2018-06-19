csv file log processing
=======================

Fields:
1. node
2. thread
3. id
4. timestamp
5. interval


java logging example
--------------------

```
public class SomeClass {
    private static final Logger profiler = LoggerFactory.getLogger("iponom.profiler");
    
    public void someMethod() {
        final long startTime = System.currentTimeMillis();
        // some code
        profiler.trace(restSessionInfo.getId() + ","  + startTime + "," + (System.currentTimeMillis() - startTime));
    }
}
```


logback example
---------------

```
<configuration scan="true" scanPeriod="120 seconds">
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>./client.log</file>
        <encoder>
            <pattern>CLIENT-00,%thread,%msg%n</pattern>
        </encoder>
    </appender>
    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <discardingThreshold>0</discardingThreshold>
        <queueSize>10000</queueSize>
        <appender-ref ref="FILE" />
    </appender>
    <root level="error">
        <appender-ref ref="ASYNC" />
    </root>
    <logger name="iponom.profiler" level="trace"/>
</configuration>
```
