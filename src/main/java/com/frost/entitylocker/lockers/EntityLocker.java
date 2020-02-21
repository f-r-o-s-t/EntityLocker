package com.frost.entitylocker.lockers;

/**
 * reusable utility class that provides synchronization mechanism similar to row-level DB locking.
 * The class is supposed to be used by the components that are responsible for managing storage
 * and caching of different type of entities in the application.
 * EntityLocker itself does not deal with the entities, only with the IDs (primary keys) of the entities
 *
 * @param <T> type of Entity key
 */
public interface EntityLocker<T> {

  /**
   * Lock entity by id
   *
   * @param id entity id to lock
   * @throws InterruptedException if locking was interrupted
   * @throws NullPointerException in case entity id is null
   */
  void lockEntity(T id) throws InterruptedException;

  /**
   * Unlock entity by id
   *
   * @param id entity id to unlock
   * @throws NullPointerException in case entity id is null
   */
  void unlockEntity(T id);

}

