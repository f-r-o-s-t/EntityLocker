package com.frost.entitylocker.executors;

import java.util.concurrent.ExecutionException;

/**
 * Interface provides methods for running protected code on entity
 *
 * @param <T> type of Entity key
 */
public interface CodeExecutor<T> {

  /**
   * Run protected code using entity id, guarantees that only one thread works with entity id in each moment of time
   *
   * @param entityId entity id to lock
   * @param code     code that should be executed in protected mode
   * @throws InterruptedException if locking was interrupted
   * @throws ExecutionException   if the execution threw an exception
   * @throws NullPointerException in case entityId is null
   */
  void execute(T entityId, ProtectedCode code) throws ExecutionException, InterruptedException;

  /**
   * Trying to execute protected code using entity id within the given waiting time,
   * guarantees that only one thread works with entity id in each moment of time.
   *
   * @param entityId     entity id to lock
   * @param milliseconds the time to wait
   * @param code         code that should be executed in protected mode
   * @return {@code true} if the code was executed and {@code false} if the waiting time elapsed before
   *         we have possibility to execute code in protected mode
   * @throws InterruptedException     if locking was interrupted
   * @throws ExecutionException       if the execution threw an exception
   * @throws NullPointerException     in case entityId is null
   * @throws IllegalArgumentException if milliseconds count less than zero
   */
  boolean tryToExecute(T entityId, long milliseconds, ProtectedCode code) throws ExecutionException, InterruptedException;
}
