package com.frost.entitylocker.lockers;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EntityLocker thread safe implementation based on ConcurrentHashMap.
 *
 * @param <T> type of Entity key
 **/
public class ConcurrentMapEntityLocker<T> implements EntityLocker<T> {

  private final static long                   NO_WAITING = -1;
  private              ConcurrentMap<T, Lock> lockingMap;

  public ConcurrentMapEntityLocker() {
    lockingMap = new ConcurrentHashMap<>();
  }

  public ConcurrentMapEntityLocker(ConcurrentMap<T, Lock> lockingMap) {
    this.lockingMap = lockingMap;
  }

  @Override
  public void lockId(T entityId) throws InterruptedException {
    lockIdInternal(entityId, NO_WAITING);
  }

  @Override
  public boolean tryLockId(T entityId, long timeout, TimeUnit timeUnit) throws InterruptedException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Timeout should be greater than zero");
    }
    return lockIdInternal(entityId, timeUnit.toNanos(timeout));
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

  private boolean lockIdInternal(T entityId, long nanoseconds) throws InterruptedException {
    Objects.requireNonNull(entityId, "Entity id must not be null");
    boolean locked;
    long    timeout = System.nanoTime() + nanoseconds; //We use System.nanoTime() because System.currentTimeMillis() can produce negative time difference
    do {
      Lock lock = lockingMap.computeIfAbsent(entityId, (key) -> new ReentrantLock());
      if (nanoseconds == NO_WAITING) {
        lock.lockInterruptibly(); //We use lockInterruptibly to be able to handle interrupt method on the thread
      } else {
        if (!lock.tryLock(timeout - System.nanoTime(), TimeUnit.NANOSECONDS)) {
          return false; //If we can't acquire the lock than just return false immediately
        }
      }
      locked = lock == lockingMap.get(entityId); //Check that we still use the actual lock
      if (!locked) { // Release this lock if it was replaced in the backing map
        lock.unlock();
      }
    } while (!locked); // Wait until we lock entity id successfully
    return true;
  }

}
