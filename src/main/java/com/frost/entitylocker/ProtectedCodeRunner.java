package com.frost.entitylocker;

import java.util.concurrent.ExecutionException;

import com.frost.entitylocker.lockers.EntityLocker;

public class ProtectedCodeRunner<T> {

  private EntityLocker<T> locker = null;

  public ProtectedCodeRunner(EntityLocker<T> locker) {
    this.locker = locker;
  }

  public void runCriticalCodeOnEntity(T id, CodeToExecute body) throws ExecutionException, InterruptedException {
    locker.lockEntity(id);
    try {
      body.run();
    } catch (Exception e) {
      throw new ExecutionException(e);
    } finally {
      locker.unlockEntity(id);
    }
  }

}
