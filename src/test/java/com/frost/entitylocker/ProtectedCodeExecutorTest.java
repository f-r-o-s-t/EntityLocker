package com.frost.entitylocker;

import com.frost.entitylocker.executors.ProtectedCodeExecutor;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ProtectedCodeExecutorTest {

  @Test
  public void shouldRunProtectedCodeOnEntity() throws Exception {
    ProtectedCodeExecutor<Integer> executor = new ProtectedCodeExecutor<>();
    boolean[]                      executed = new boolean[1];
    executor.execute(1, () -> executed[0] = true);
    assertTrue(executed[0]);
  }

  @Test
  public void shouldRunProtectedCodeOnEntityTryToExecuteREFACTOR() throws Exception {
    ProtectedCodeExecutor<Integer> executor = new ProtectedCodeExecutor<>();
    boolean[]                      executed = new boolean[1];
    executor.tryToExecute(1, 100, () -> executed[0] = true);
    assertTrue(executed[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionIfMillisecndsLessThanZero() throws Exception {
    ProtectedCodeExecutor<Integer> executor = new ProtectedCodeExecutor<>();
    executor.tryToExecute(1, -1, () -> System.out.println("Ignored"));
  }

}
