package com.frost.entitylocker.lockers;

/**
 * Class with utils factory methods for creating EntityLockers
 */
public class EntityLockerFactory {

  public static <T> EntityLocker<T> getSequentialEntityLocker() {
    return new SequentialEntityLocker<>();
  }

  public static <T> EntityLocker<T> getConcurrentEntityLocker() {
    return new ConcurrentMapEntityLocker<>();
  }

  public static <T> EntityLocker<T> getUnsafeEntityLocker() {
    return new UnsafeEntityLocker<>();
  }
}
