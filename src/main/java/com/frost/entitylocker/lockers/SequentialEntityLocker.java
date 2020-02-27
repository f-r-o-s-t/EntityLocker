package com.frost.entitylocker.lockers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EntityLocker implementation based on one lock for all ids, added for testing.
 *
 * @param <T> type of Entity key
 */
public class SequentialEntityLocker<T> implements EntityLocker<T> {

  private Lock lock;

  public SequentialEntityLocker() {
    this(new ReentrantLock());
  }

  public SequentialEntityLocker(Lock lock) {
    this.lock = lock;
  }

  @Override
  public void lockId(T entityId) throws InterruptedException {
    lock.lockInterruptibly();
  }

  @Override
  public boolean tryLockId(T entityId, long timeout, TimeUnit timeUnit) throws InterruptedException {
    return lock.tryLock(timeout, timeUnit);
  }

  @Override
  public void unlockId(T entityId) {
    lock.unlock();
  }
}
