package com.frost.entitylocker;

import com.frost.entitylocker.lockers.EntityLocker;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ProtectedCodeExecutorTest {

  @Test
  public void shouldRunProtectedCodeOnEntity() throws Exception {
    EntityLocker<Integer>          locker   = EntityLockerFactory.getThreadSafeEntityLocker();
    ProtectedCodeExecutor<Integer> executor = new ProtectedCodeExecutor<>(locker);
    boolean[]                      executed = new boolean[1];
    executor.runProtectedCodeOnEntity(1, () -> {
      executed[0] = true;
    });
    assertTrue(executed[0]);
  }

}
