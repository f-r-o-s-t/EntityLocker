package com.frost.entitylocker;

/**
 * Interface for running code in protected mode
 */
@FunctionalInterface
public interface ProtectedCode {

  /**
   * Override this method to specify the code that should be executed in protected mode
   *
   * @throws Exception if unable to compute a result
   */
  void run() throws Exception;
}
