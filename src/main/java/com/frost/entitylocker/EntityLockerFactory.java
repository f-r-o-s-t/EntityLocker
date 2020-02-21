package com.frost.entitylocker;

import com.frost.entitylocker.lockers.EntityLocker;
import com.frost.entitylocker.lockers.SingleThreadEntityLocker;
import com.frost.entitylocker.lockers.ThreadSafeEntityLocker;
import com.frost.entitylocker.lockers.ThreadUnsafeEntityLocker;

/**
 * Class with utils factory methods for creating EntityLockers
 */
public class EntityLockerFactory {

  public static <T> EntityLocker<T> getSingleThreadLocker() {
    return new SingleThreadEntityLocker<>();
  }

  public static <T> EntityLocker<T> getThreadSafeEntityLocker() {
    return new ThreadSafeEntityLocker<>();
  }

  public static <T> EntityLocker<T> getThreadUnsafeEntityLocker() {
    return new ThreadUnsafeEntityLocker<>();
  }
}
