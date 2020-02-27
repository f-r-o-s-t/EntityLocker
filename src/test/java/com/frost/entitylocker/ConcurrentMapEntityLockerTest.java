package com.frost.entitylocker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import com.frost.entitylocker.lockers.ConcurrentMapEntityLocker;
import com.frost.entitylocker.lockers.EntityLocker;
import com.frost.entitylocker.lockers.EntityLockerFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//-----hour---------------
//TODO make TestConfiguration more clean
//-----hour---------------
//TODO refactor validation tests
//-----hour---------------
//TODO check test names again
//TODO refactor and publish

public class ConcurrentMapEntityLockerTest {

  final int FIRST_ENTITY_ID  = 10;
  final int SECOND_ENTITY_ID = 11;

  EntityLocker<Integer> locker;

  @Before
  public void setUp() {
    locker = EntityLockerFactory.getConcurrentEntityLocker();
  }

  @Test(timeout = 100L)
  public void shouldLockAndUnlockProperly() throws Exception {
    locker.lockId(FIRST_ENTITY_ID);
    locker.unlockId(FIRST_ENTITY_ID);
  }

  @Test(timeout = 100L)
  public void shouldLockOnTimeAndUnlockProperly() throws Exception {
    assertTrue("Should get lock and return true", locker.tryLockId(FIRST_ENTITY_ID, 50, TimeUnit.MILLISECONDS));
    locker.unlockId(FIRST_ENTITY_ID);
  }

  @Test(timeout = 1000L)
  public void shouldLockANdUnlockOnTime() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    Thread t = new Thread(() -> {
      try {
        locker.lockId(FIRST_ENTITY_ID);
        latch.countDown();
        Thread.sleep(50L);
        locker.unlockId(FIRST_ENTITY_ID);
      } catch (InterruptedException ignored) {
      }
    });
    t.start();
    latch.await();
    boolean firstTry  = locker.tryLockId(FIRST_ENTITY_ID, 30L, TimeUnit.MILLISECONDS);
    boolean secondTry = locker.tryLockId(FIRST_ENTITY_ID, 50L, TimeUnit.MILLISECONDS);
    if (firstTry) locker.unlockId(FIRST_ENTITY_ID);
    if (secondTry) locker.unlockId(FIRST_ENTITY_ID);
    assertFalse("Should fail", firstTry);
    assertTrue("Should pass", secondTry);
  }

  @Test(timeout = 1000L)
  public void shouldWorkConcurrentlyWithDifferentEntities() throws Exception {
    CountDownLatch latch  = new CountDownLatch(2);
    AtomicInteger  result = new AtomicInteger(0);

    Thread t = new Thread(() -> {
      try {
        locker.lockId(SECOND_ENTITY_ID);
        latch.countDown();
        latch.await();
        result.incrementAndGet();
        locker.unlockId(SECOND_ENTITY_ID);
      } catch (InterruptedException ignored) {
      }
    });
    t.start();

    locker.lockId(FIRST_ENTITY_ID);
    latch.countDown();
    latch.await();
    result.incrementAndGet();
    locker.unlockId(FIRST_ENTITY_ID);
    t.join();
    assertEquals("Should be incremented twice in different threads", 2, result.get());
  }

  @Test(timeout = 1000L)
  public void shouldNotWorkConcurrentlyOnSameEntity() throws Exception {
    final int          THREAD_COUNT = 10;
    AtomicBoolean      isRunning    = new AtomicBoolean(false);
    AtomicBoolean      failed       = new AtomicBoolean(false);
    ExecutorService    service      = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch     latch        = new CountDownLatch(THREAD_COUNT);
    List<Future<Long>> results      = new ArrayList<>();

    for (int i = 0; i < THREAD_COUNT; i++) {
      results.add(service.submit(
          () -> {
            try {
              latch.countDown();
              latch.await();
              locker.lockId(FIRST_ENTITY_ID);
              if (isRunning.get()) failed.set(true);
              isRunning.set(true);
              Thread.sleep(10);
              isRunning.set(false);
              locker.unlockId(FIRST_ENTITY_ID);
            } catch (InterruptedException ignored) {
            }
            return 1L;
          }));
    }
    for (Future<Long> r : results) r.get();
    service.shutdown();
    assertFalse("Only one thread should work on one entity", failed.get());
  }

  @Test(timeout = 1000L)
  public void shouldAcquireReentrantLock() throws Exception {
    int result = 0;
    locker.lockId(FIRST_ENTITY_ID);
    result++;
    locker.lockId(FIRST_ENTITY_ID);
    result++;
    locker.unlockId(FIRST_ENTITY_ID);
    locker.unlockId(FIRST_ENTITY_ID);
    assertEquals("Should be incremented twice under the first and the second lock", 2, result);
  }

  @Test(timeout = 1000L)
  public void shouldThrowExceptionWhenInterruptMethodCalled() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    locker.lockId(FIRST_ENTITY_ID);

    Thread second = new Thread(() -> {
      try {
        locker.lockId(FIRST_ENTITY_ID);
      } catch (InterruptedException e) {
        latch.countDown();
      }
    });
    second.start();
    second.interrupt();
    latch.await();
  }

  @Test
  public void shouldCleanBackingMapAfterExecution() throws Exception {
    ConcurrentMap<Integer, Lock> map    = new ConcurrentHashMap<>();
    EntityLocker<Integer>        locker = new ConcurrentMapEntityLocker<>(map);
    locker.lockId(FIRST_ENTITY_ID);
    locker.unlockId(FIRST_ENTITY_ID);
    assertEquals("We should have clean backing map to avoid memory leaks", 0, map.size());
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfIdIsNullOnLock() throws InterruptedException {
    locker.lockId(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfIdIsNullOnUnlock() {
    locker.unlockId(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionIfTimeoutIsNegative() throws InterruptedException {
    locker.tryLockId(1, -1, TimeUnit.MILLISECONDS);
  }

}
