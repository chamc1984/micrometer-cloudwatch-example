package com.example.micrometercloudwatchexample.config;

import java.util.Properties;

public class MicrometerProperties {

  public Properties DEFAULT;

    public MicrometerProperties() {
      DEFAULT = new Properties();
      DEFAULT.setProperty("cloudwatch.enabled", "true");
      DEFAULT.setProperty("cloudwatch.namespace", "MICROMETER");
      DEFAULT.setProperty("cloudwatch.step", "PT1M");
      DEFAULT.setProperty("cloudwatch.batchSize", "1");
  }
}