package com.frost.entitylocker.lockers;

/**
 * EntityLocker implementation that doesn't use locking, added for testing.
 *
 * @param <T> type of Entity key
 */
public class ThreadUnsafeEntityLocker<T> implements EntityLocker<T> {

  @Override
  public void lockId(T entityId) {
    //Do nothing, added just for testing
  }

  @Override
  public void unlockId(T entityId) {
    //Do nothing, added just for testing
  }
}
