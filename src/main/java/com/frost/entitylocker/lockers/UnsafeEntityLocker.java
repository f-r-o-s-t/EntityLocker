package com.frost.entitylocker.lockers;

import java.util.concurrent.TimeUnit;

/**
 * EntityLocker implementation that doesn't use locking, added for testing.
 *
 * @param <T> type of Entity key
 */
public class UnsafeEntityLocker<T> implements EntityLocker<T> {

  public UnsafeEntityLocker() {
  }

  @Override
  public void lockId(T entityId) {
    //Do nothing, added just for testing
  }

  @Override
  public boolean tryLockId(T entityId, long timeout, TimeUnit timeUnit) {
    //Do nothing, added just for testing
    return true;
  }

  @Override
  public void unlockId(T entityId) {
    //Do nothing, added just for testing
  }
}
