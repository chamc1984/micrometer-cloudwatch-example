package com.example.micrometercloudwatchexample.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

public class CloudWatchReporter implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CloudWatchReporter.class);

    private AmazonCloudWatchAsync cloundwatch =
            AmazonCloudWatchAsyncClientBuilder.standard().withRegion("ap-northeast-1").build();

    private String id;

    private static String namespace = "SAMPLE-GRP/SAMPLE-APP";

    public CloudWatchReporter(String id) {
        this.id = id;
    }

    @Override
    public void run() {
        logger.debug("START Reporter run.");

        Dimension dimension = new Dimension().withName("Id").withValue(id);
        Date nowDate = Date
                .from(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()).toInstant());

        // MXBean
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        List<MemoryPoolMXBean> memoryPoolMXBeanList = ManagementFactory.getMemoryPoolMXBeans();

        // Metric
        Double threadCount = (double) threadMXBean.getThreadCount();
        Double heapUsage = (double) memoryMXBean.getHeapMemoryUsage().getUsed();

        MetricDatum metricThreadCount =
                new MetricDatum().withDimensions(dimension).withMetricName("thread_count")
                        .withUnit(StandardUnit.Count).withValue(threadCount).withTimestamp(nowDate);
        MetricDatum metricHeapUsage =
                new MetricDatum().withDimensions(dimension).withMetricName("heap_usage")
                        .withUnit(StandardUnit.Count).withValue(heapUsage).withTimestamp(nowDate);

        PutMetricDataRequest request = new PutMetricDataRequest().withNamespace(namespace)
                .withMetricData(metricThreadCount, metricHeapUsage);
        logger.debug("request = " + request.toString());

        cloundwatch.putMetricDataAsync(request);
        logger.debug("END Reporter run.");
    }
}
