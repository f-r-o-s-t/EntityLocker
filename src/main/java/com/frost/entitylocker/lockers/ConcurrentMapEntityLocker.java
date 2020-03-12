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
 * @param <T> type of Entity id
 **/
public class ConcurrentMapEntityLocker<T> implements EntityLocker<T> {

  private final static long                         NO_WAITING = -1;
  private final        ConcurrentMap<T, EntityLock> lockingMap;

  /**
   * Constructs new ConcurrentMapEntityLocker based on default ConcurrentMap implementation
   */
  public ConcurrentMapEntityLocker() {
    lockingMap = new ConcurrentHashMap<>();
  }

  /**
   * Constructs new ConcurrentMapEntityLocker based on provided map
   *
   * @param concurrentMap map that will be used for storing locks
   */
  ConcurrentMapEntityLocker(ConcurrentMap<T, EntityLock> concurrentMap) {
    this.lockingMap = concurrentMap;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void lockId(T entityId) throws InterruptedException {
    lockIdInternal(entityId, NO_WAITING);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean tryLockId(T entityId, long timeout, TimeUnit timeUnit) throws InterruptedException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Timeout should be greater than zero");
    }
    return lockIdInternal(entityId, timeUnit.toNanos(timeout));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unlockId(T entityId) {
    Objects.requireNonNull(entityId, "Entity id must not be null");
    EntityLock entityLock = lockingMap.get(entityId);
    if (entityLock != null) {
      if (entityLock.owner != Thread.currentThread()) {
        throw new IllegalMonitorStateException();
      }
      if (entityLock.count == 1) {
        lockingMap.remove(entityId); // Remove used lock to avoid memory leaks
      } else {
        entityLock.count--;
      }
      entityLock.lock.unlock();
    } else {
      throw new IllegalMonitorStateException();
    }
  }

  private boolean lockIdInternal(T entityId, long nanoseconds) throws InterruptedException {
    Objects.requireNonNull(entityId, "Entity id must not be null");
    boolean locked;
    long    timeout = System.nanoTime() + nanoseconds; //We use System.nanoTime() because System.currentTimeMillis() can produce negative time difference
    do {
      EntityLock entityLock = lockingMap.computeIfAbsent(entityId, (key) -> new EntityLock());
      Lock       lock       = entityLock.lock;
      if (nanoseconds == NO_WAITING) {
        lock.lockInterruptibly(); //We use lockInterruptibly to be able to handle interrupt method on the thread
      } else {
        if (!lock.tryLock(timeout - System.nanoTime(), TimeUnit.NANOSECONDS)) {
          return false; //If we can't acquire the lock than just return false immediately
        }
      }
      entityLock.count++;
      entityLock.owner = Thread.currentThread();
      locked = entityLock == lockingMap.get(entityId); //Check that we still use the actual lock
      if (!locked) { // Release this lock if it was replaced in the backing map
        lock.unlock();
      }
    } while (!locked); // Wait until we lock entity id successfully
    return true;
  }

  static class EntityLock {

    final Lock lock;
    Thread owner;
    int    count;

    public EntityLock() {
      this.lock = new ReentrantLock();
      this.owner = Thread.currentThread();
      this.count = 0;
    }
  }

}
