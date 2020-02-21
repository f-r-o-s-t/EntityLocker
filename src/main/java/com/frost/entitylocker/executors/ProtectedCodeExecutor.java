package com.frost.entitylocker.executors;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.frost.entitylocker.ProtectedCode;
import com.frost.entitylocker.lockers.EntityLocker;

/**
 * Provides methods for running protected code using backing locker
 *
 * @param <T> type of Entity key
 */
public class ProtectedCodeExecutor<T> {

  private EntityLocker<T> locker;

  /**
   * Constructs code runner
   *
   * @param locker locker that used for locking entity by id
   * @throws NullPointerException in case locker is null
   */
  public ProtectedCodeExecutor(EntityLocker<T> locker) {
    Objects.requireNonNull(locker, "Locker must be not null");
    this.locker = locker;
  }

  /**
   * Run protected code using entity id, guarantees that only one thread works with entity id in each moment of time
   *
   * @param entityId entity id to lock
   * @param code     code that should be executed in protected mode
   * @throws InterruptedException if locking was interrupted
   * @throws ExecutionException   if the execution threw an exception
   * @throws NullPointerException in case entityId is null
   */
  public void runProtectedCodeOnEntity(T entityId, ProtectedCode code) throws ExecutionException, InterruptedException {
    locker.lockId(entityId);
    try {
      code.run();
    } catch (Exception e) {
      throw new ExecutionException(e);
    } finally {
      locker.unlockId(entityId);
    }
  }

}
