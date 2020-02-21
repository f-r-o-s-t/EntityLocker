package com.frost.entitylocker;

/**
 * Interface for running code in protected mode
 */
@FunctionalInterface
interface ProtectedCode {

  /**
   * Override this method to specify the code that should be executed in protected mode
   * @throws Exception
   */
  void run() throws Exception;
}
