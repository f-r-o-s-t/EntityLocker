package com.frost.entitylocker.lockers;

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
