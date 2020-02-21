package com.frost.entitylocker.lockers;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EntityLocker implementation based on one lock for all ids, added for testing.
 *
 * @param <T> type of Entity key
 */
public class SingleThreadEntityLocker<T> implements EntityLocker<T> {

  final Lock lock = new ReentrantLock();

  @Override
  public void lockId(T entityId) throws InterruptedException {
    lock.lockInterruptibly();
  }

  @Override
  public void unlockId(T entityId) {
    lock.unlock();
  }
}
