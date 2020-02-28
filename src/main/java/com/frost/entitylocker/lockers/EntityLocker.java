package com.frost.entitylocker.lockers;

import java.util.concurrent.TimeUnit;

/**
 * Reusable utility class that provides synchronization mechanism similar to row-level DB locking.
 * The class is supposed to be used by the components that are responsible for managing storage
 * and caching of different type of entities in the application.
 * EntityLocker itself does not deal with the entities, only with the IDs (primary keys) of the entities
 * <p>
 * Requirements:
 * 1. EntityLocker should support different types of entity IDs.
 * 2. EntityLocker’s interface should allow the caller to specify which entity does it want to work with (using entity ID), and designate the boundaries of the code that should have exclusive access to the entity (called “protected code”).
 * 3. For any given entity, EntityLocker should guarantee that at most one thread executes protected code on that entity. If there’s a concurrent request to lock the same entity, the other thread should wait until the entity becomes available.
 * 4. EntityLocker should allow concurrent execution of protected code on different entities.
 *
 * Bonus requirements (Implemented):
 * I. Allow reentrant locking.
 * II. Allow the caller to specify timeout for locking an entity.
 *
 * @param <T> type of Entity key
 */
public interface EntityLocker<T> {

  /**
   * Lock entity by id
   *
   * @param entityId entity id to lock
   * @throws InterruptedException if locking was interrupted
   * @throws NullPointerException in case entity id is null
   */
  void lockId(T entityId) throws InterruptedException;

  /**
   * @param entityId entity id to lock
   * @param timeout  the time to wait for the lock
   * @param timeUnit the time unit of the timeout argument
   * @return {@code true} if the lock was free and was acquired by the
   *         current thread, or the lock was already held by the current
   *         thread; and {@code false} if the waiting time elapsed before
   *         the lock could be acquired
   * @throws InterruptedException     if locking was interrupted
   * @throws NullPointerException     in case entity id is null
   * @throws IllegalArgumentException if timeout less than zero
   */
  boolean tryLockId(T entityId, long timeout, TimeUnit timeUnit) throws InterruptedException;

  /**
   * Unlock entity by id
   *
   * @param entityId entity id to unlock
   * @throws NullPointerException in case entity id is null
   */
  void unlockId(T entityId);

}

