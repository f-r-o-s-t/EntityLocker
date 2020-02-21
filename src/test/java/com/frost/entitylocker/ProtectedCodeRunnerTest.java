package com.frost.entitylocker;

import com.frost.entitylocker.lockers.EntityLocker;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProtectedCodeRunnerTest {

  @Test
  public void shouldRunCriticalCodeOnEntity() throws Exception {
    EntityLocker<Integer>        locker = EntityLockerFactory.getThreadSafeEntityLocker();
    ProtectedCodeRunner<Integer> runner = new ProtectedCodeRunner<>(locker);
    boolean[] executed = new boolean[1];
    runner.runCriticalCodeOnEntity(1, () -> {
      executed[0] = true;
    });
    assertTrue(executed[0]);
  }

}
