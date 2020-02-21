package com.frost.entitylocker.lockers;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EntityLocker thread safe implementation based on ConcurrentHashMap.
 *
 * @param <T> type of Entity key
 **/
public class ThreadSafeEntityLocker<T> implements EntityLocker<T> {

  private ConcurrentMap<T, Lock> lockingMap;

  public ThreadSafeEntityLocker() {
    lockingMap = new ConcurrentHashMap<>();
  }

  public ThreadSafeEntityLocker(ConcurrentMap<T, Lock> lockingMap) {
    this.lockingMap = lockingMap;
  }

  @Override
  public void lockId(T entityId) throws InterruptedException {
    Objects.requireNonNull(entityId, "Entity id must not be null");
    boolean locked;
    do {
      Lock lock = lockingMap.computeIfAbsent(entityId, (key) -> new ReentrantLock());
      lock.lockInterruptibly();
      locked = lock == lockingMap.get(entityId); //Check that we still use actual lock
      if (!locked) {
        lock.unlock();
      }
    } while (!locked);
  }

  @Override
  public void unlockId(T entityId) {
    Objects.requireNonNull(entityId, "Entity id must not be null");
    Lock lock = lockingMap.get(entityId);
    if (lock != null) {
      lockingMap.remove(entityId); // Remove used lock to avoid memory leaks
      lock.unlock();
    }
  }

}
