package com.frost.entitylocker.utils;

public class TestConfiguration {

  public final boolean checkResult;
  public final boolean withSleep;
  public final int     factor;
  public final int     threadCount;
  public final int     arraySize;
  public final boolean tryLock;

  public TestConfiguration(boolean tryLock, boolean checkResult, boolean withSleep, int factor, int threadCount, int arraySize) {
    this.tryLock = tryLock;
    this.checkResult = checkResult;
    this.withSleep = withSleep;
    this.factor = factor;
    this.threadCount = threadCount;
    this.arraySize = arraySize;
  }

  public TestConfiguration(boolean tryLock, boolean checkResult, boolean withSleep, int factor, int threadCount) {
    this(tryLock, checkResult, withSleep, factor, threadCount, 10);
  }

  public TestConfiguration(boolean tryLock, boolean checkResult, boolean withSleep, int factor) {
    this(tryLock, checkResult, withSleep, factor, 10);
  }
}
