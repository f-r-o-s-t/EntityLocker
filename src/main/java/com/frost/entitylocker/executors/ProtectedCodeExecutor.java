package com.frost.entitylocker.executors;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.frost.entitylocker.lockers.EntityLocker;
import com.frost.entitylocker.lockers.EntityLockerFactory;

/**
 * Implementaion of CodeExecutor that uses EntityLocker for running protected code
 *
 * @param <T> type of Entity key
 */
public class ProtectedCodeExecutor<T> implements CodeExecutor<T> {

  private EntityLocker<T> locker;

  /**
   * Constructs code executor base on default implementation of thread safe EntityLocker
   */
  public ProtectedCodeExecutor() {
    this(EntityLockerFactory.getConcurrentEntityLocker());
  }

  /**
   * Constructs code executor based on provided locker
   *
   * @param locker locker that used for locking entity by id
   * @throws NullPointerException in case locker is null
   */
  public ProtectedCodeExecutor(EntityLocker<T> locker) {
    Objects.requireNonNull(locker, "Locker must be not null");
    this.locker = locker;
  }

  @Override
  public void execute(T entityId, ProtectedCode code) throws ExecutionException, InterruptedException {
    locker.lockId(entityId);
    executeInternal(entityId, code);
  }

  @Override
  public boolean tryToExecute(T entityId, long milliseconds, ProtectedCode code) throws ExecutionException, InterruptedException {
    if (milliseconds < 0) {
      throw new IllegalArgumentException("Milliseconds should be greater than zero");
    }
    boolean locked = locker.tryLockId(entityId, milliseconds, TimeUnit.MILLISECONDS);
    if (locked) {
      executeInternal(entityId, code);
    }
    return locked;
  }

  private void executeInternal(T entityId, ProtectedCode code) throws ExecutionException {
    try {
      code.run();
    } catch (Exception e) {
      throw new ExecutionException(e);
    } finally {
      locker.unlockId(entityId);
    }
  }

}
