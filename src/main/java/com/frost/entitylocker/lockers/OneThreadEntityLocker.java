package com.frost.entitylocker.lockers;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.frost.entitylocker.lockers.EntityLocker;

/**
 * EntityLocker implementation based on one lock for all ids, added for testing.
 * @param <T> type of Entity key
 */
public class OneThreadEntityLocker<T> implements EntityLocker<T> {

  final Lock lock = new ReentrantLock();

  @Override
  public void lockEntity(T id) throws InterruptedException {
    lock.lockInterruptibly();
  }

  @Override
  public void unlockEntity(T id) {
    lock.unlock();
  }
}
