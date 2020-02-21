package com.frost.entitylocker;

public class TestConfiguration {

  static TestConfiguration SLEEP_AND_CHECK_RESULTS      = new TestConfiguration(true, true, 100);
  static TestConfiguration SLEEP_AND_DONT_CHECK_RESULTS = new TestConfiguration(false, true, 100);

  boolean checkResult;
  boolean withSleep;
  int     factor;
  int     threadCount;
  int     arraySize;

  public TestConfiguration(boolean checkResult, boolean withSleep, int factor, int threadCount, int arraySize) {
    this.checkResult = checkResult;
    this.withSleep = withSleep;
    this.factor = factor;
    this.threadCount = threadCount;
    this.arraySize = arraySize;
  }

  public TestConfiguration(boolean checkResult, boolean withSleep, int factor, int threadCount) {
    this(checkResult, withSleep, factor, threadCount, 10);
  }

  public TestConfiguration(boolean checkResult, boolean withSleep, int factor) {
    this(checkResult, withSleep, factor, 10);
  }
}
