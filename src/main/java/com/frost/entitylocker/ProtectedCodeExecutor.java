package com.frost.entitylocker;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.frost.entitylocker.lockers.EntityLocker;

/**
 * Class provides method for running protected with blocking entity id using backing locker
 *
 * @param <T> type of Entity key
 */
public class ProtectedCodeExecutor<T> {

  private EntityLocker<T> locker;

  /**
   * Constructs code runner with backing locker
   *
   * @param locker that used for locking entity by id
   */
  public ProtectedCodeExecutor(EntityLocker<T> locker) {
    Objects.requireNonNull(locker, "Locker must be not null");
    this.locker = locker;
  }

  /**
   * Run protected code on Entity, guarantees that only one thread works with entity with id in each moment of time
   *
   * @param entityId entity id to lock
   * @param body     code that should be executed in protected mode
   * @throws InterruptedException if locking was interrupted
   * @throws ExecutionException   code execution throws exception
   * @throws NullPointerException in case entity id is null
   */
  public void runProtectedCodeOnEntity(T entityId, ProtectedCode body) throws ExecutionException, InterruptedException {
    locker.lockEntity(entityId);
    try {
      body.run();
    } catch (Exception e) {
      throw new ExecutionException(e);
    } finally {
      locker.unlockEntity(entityId);
    }
  }

}
