package com.frost.entitylocker.lockers;

/**
 * EntityLocker implementation that doesn't use locking, added for testing.
 *
 * @param <T> type of Entity key
 */
public class ThreadUnsafeEntityLocker<T> implements EntityLocker<T> {

  @Override
  public void lockEntity(T id) throws InterruptedException {
    //Do nothing, added just for testing
  }

  @Override
  public void unlockEntity(T id) {
    //Do nothing, added just for testing
  }
}
