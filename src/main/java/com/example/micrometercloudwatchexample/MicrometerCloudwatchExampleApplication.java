package com.example.micrometercloudwatchexample;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.example.micrometercloudwatchexample.config.MicrometerProperties;
import com.example.micrometercloudwatchexample.monitor.CloudWatchReporter;
import io.micrometer.cloudwatch.CloudWatchConfig;
import io.micrometer.cloudwatch.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;

@SpringBootApplication
public class MicrometerCloudwatchExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicrometerCloudwatchExampleApplication.class, args);

        // monitoringByJmx();

        try {
            monitoringByMicrometer();
        } catch (Exception e) {
            System.out.println("EXCEPTION !!!");
        }

    }

    private static void monitoringByJmx() {
        System.out.println("INFO :  " + "START MONITORING.");

        String containerId = System.getenv("HOSTNAME");
        if (containerId == null) {
            containerId = "localhost";
        }

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectInstance> mbeans = server.queryMBeans(null, null);
        mbeans.forEach(mbean -> System.out.println("MBEAN :  " + mbean.toString()));

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture handler = scheduler.scheduleAtFixedRate(new CloudWatchReporter(containerId),
                0, 1, TimeUnit.MINUTES);

        for (int i = 0; i < 6; i++) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(60000);
                    } catch (Exception e) {
                        System.out.println("ERROR MESSAGE (1): " + e.getMessage());
                        // ignore
                    }
                }
            }).start();
            try {
                Thread.sleep(30000);
            } catch (Exception e) {
                System.out.println("ERROR MESSAGE (2): " + e.getMessage());
                // ignore
            }
        }
        handler.cancel(false);
        System.out.println("exit");
        System.exit(0);
    }

    private static AmazonCloudWatchAsync cloudWatch =
            AmazonCloudWatchAsyncClientBuilder.standard().withRegion("ap-northeast-1").build();
    private static Clock clock = Clock.SYSTEM;
    private static CloudWatchConfig config = new CloudWatchConfig() {
        private Properties properties = new MicrometerProperties().DEFAULT;

        @Override
        public String get(String key) {
            return properties.getProperty(key);
        }
    };

    private static Runnable sleep = () -> {
        try {
            Thread.sleep(60000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private static void monitoringByMicrometer() throws Exception {
        CloudWatchMeterRegistry registry = new CloudWatchMeterRegistry(config, clock, cloudWatch);

        // io.micrometer.core.instrument.binder.* に定義されているBinderをBindする
        new ClassLoaderMetrics().bindTo(registry);
        new DiskSpaceMetrics(new File("/usr/local/")).bindTo(registry);
        // TODO: これも取りたい気もするけど、executorServiceってどうやって指定するの？
        // new ExecutorServiceMetrics(executorService, executorServiceName, tags);
        new JvmGcMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        // TODO: managerってなに？
        // new TomcatMetrics(manager, tags);
        // TODO: DataSourceはもうちょっと工夫が必要そう。
        // https://matsumana.info/blog/2016/02/06/spring-boot-hikaricp-metrics/
        // new PostgreSQLDatabaseMetrics(postgresDataSource, database);
        // TODO: え？EntityManager単位でメトリクス切るの？
        // new HibernateMetrics(entityManagerFactory, entityManagerFactoryName, tags)
        // TODO: LogbackMetricsってなに？
        // new LogbackMetrics().bindTo(registry);

        IntStream.range(1, 20).forEach(n -> new Thread(sleep).run());

        Thread.sleep(60000);
    }

}

