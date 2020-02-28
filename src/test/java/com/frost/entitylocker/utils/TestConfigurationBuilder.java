package com.frost.entitylocker.utils;

public class TestConfigurationBuilder {

  private boolean checkResult;
  private boolean withSleep;
  private int     factor;
  private int     threadCount;
  private int     arraySize;
  private boolean tryLock;

  public TestConfigurationBuilder() {
  }

  public TestConfigurationBuilder withTryLock(boolean tryLock) {
    this.tryLock = tryLock;
    return this;
  }

  public TestConfigurationBuilder withSleep(boolean withSleep) {
    this.withSleep = withSleep;
    return this;
  }

  public TestConfigurationBuilder withCheckResult(boolean checkResult) {
    this.checkResult = checkResult;
    return this;
  }

  public TestConfigurationBuilder withFactor(int factor) {
    this.factor = factor;
    return this;
  }

  public TestConfigurationBuilder withThreadCount(int threadCount) {
    this.threadCount = threadCount;
    return this;
  }

  public TestConfigurationBuilder withArraySize(int size) {
    this.arraySize = size;
    return this;
  }

  public TestConfiguration build() {
    return new TestConfiguration(tryLock, checkResult, withSleep, factor, threadCount, arraySize);
  }

}
