package com.example.micrometercloudwatchexample;

import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.example.micrometercloudwatchexample.monitor.CloudWatchMicrometerReporter;
import com.example.micrometercloudwatchexample.monitor.CloudWatchReporter;

@SpringBootApplication
public class MicrometerCloudwatchExampleApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(MicrometerCloudwatchExampleApplication.class, args);
        
        CloudWatchMicrometerReporter reporter = ctx.getBean(CloudWatchMicrometerReporter.class);
        try {
            reporter.monitoringByMicrometer();
        } catch(Exception e) {
            System.out.println(e);
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

}

