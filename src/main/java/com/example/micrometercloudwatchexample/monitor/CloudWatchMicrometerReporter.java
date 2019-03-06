package com.example.micrometercloudwatchexample.monitor;

import java.io.File;
import java.util.Properties;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.example.micrometercloudwatchexample.config.MicrometerProperties;
import io.micrometer.cloudwatch.CloudWatchConfig;
import io.micrometer.cloudwatch.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;

@Component
public class CloudWatchMicrometerReporter {

    private static Logger logger = LoggerFactory.getLogger(CloudWatchMicrometerReporter.class);

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

    public void monitoringByMicrometer() throws Exception {
        
        logger.info("Start monitoringByMicrometer method. ");
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

        logger.info("Before Sleep. ");

        Thread.sleep(60000);

        logger.info("End monitoringByMicrometer method. ");

    }

}
