package com.frost.entitylocker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import com.frost.entitylocker.lockers.EntityLocker;
import com.frost.entitylocker.lockers.ThreadSafeEntityLocker;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

// ----- hour ---------
//TODO Added to Git and do first commit
//TODO comment difficult parts
// ----- hour ---------
//TODO refactor solution tests && do what guys want to see robutness, culture of code and architecture
//TODO write documentation
//TODO publish solution until 11PM Friday

public class ThreadSafeEntityLockerTest {

  final int FIRST_ENTITY_ID = 10;
  final int SECOND_ENTITY_ID = 11;

  EntityLocker<Integer> locker;

  @Before
  public void setUp() {
    locker = EntityLockerFactory.getThreadSafeEntityLocker();
  }

  @Test(timeout = 100L)
  public void shouldLockAndUnlockProperly() throws Exception {
    locker.lockEntity(FIRST_ENTITY_ID);
    locker.unlockEntity(FIRST_ENTITY_ID);
  }

  @Test(timeout = 1000L)
  public void shouldWorkConcurrentlyWithDifferentEntities() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    AtomicInteger result = new AtomicInteger(0);

    Thread t = new Thread(() -> {
      try {
        locker.lockEntity(SECOND_ENTITY_ID);
        latch.countDown();
        latch.await();
        result.incrementAndGet();
        locker.unlockEntity(SECOND_ENTITY_ID);
      } catch (InterruptedException ignored) {
      }
    });
    t.start();

    locker.lockEntity(FIRST_ENTITY_ID);
    latch.countDown();
    latch.await();
    result.incrementAndGet();
    locker.unlockEntity(FIRST_ENTITY_ID);
    t.join();
    assertEquals("Should be incremented twice in different threads", 2, result.get());
  }

  @Test(timeout = 1000L)
  public void shouldNotWorkConcurrentlyOnSameEntity() throws Exception {
    final int THREAD_COUNT = 10;
    AtomicBoolean isRunning = new AtomicBoolean(false);
    AtomicBoolean failed = new AtomicBoolean(false);
    ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch     latch   = new CountDownLatch(THREAD_COUNT);
    List<Future<Long>> results = new ArrayList<>();

    for (int i = 0; i < THREAD_COUNT; i++) {
      results.add(service.submit(
          () -> {
            try {
              latch.countDown();
              latch.await();
              locker.lockEntity(FIRST_ENTITY_ID);
              if (isRunning.get()) failed.set(true);
              isRunning.set(true);
              Thread.sleep(10);
              isRunning.set(false);
              locker.unlockEntity(FIRST_ENTITY_ID);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            return 1L;
          }));
    }
    for (Future<Long> r : results) r.get();
    service.shutdown();
    assertFalse("Only one thread should work on one entity", failed.get());
  }

  @Test(timeout = 1000L)
  public void shouldAcquireReentrantLocking() throws Exception {
    int result = 0;
    locker.lockEntity(FIRST_ENTITY_ID);
    result++;
    locker.lockEntity(FIRST_ENTITY_ID);
    result++;
    locker.unlockEntity(FIRST_ENTITY_ID);
    locker.unlockEntity(FIRST_ENTITY_ID);
    assertEquals("Should be incremented twice under the first and the second lock", 2, result);
  }

  @Test(timeout = 1000L)
  public void shouldExitWhenInterruptMethodCalled() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    locker.lockEntity(FIRST_ENTITY_ID);

    Thread second = new Thread(() -> {
      try {
        locker.lockEntity(FIRST_ENTITY_ID);
      } catch (InterruptedException e) {
        latch.countDown();
      }
    });
    second.start();
    second.interrupt();
    latch.await();
  }

  @Test
  public void shouldCleanTheMapAfterExecution() throws Exception {
    ConcurrentMap<Integer, Lock> map = new ConcurrentHashMap<>();
    EntityLocker<Integer> locker = new ThreadSafeEntityLocker<>(map);
    locker.lockEntity(FIRST_ENTITY_ID);
    locker.unlockEntity(FIRST_ENTITY_ID);
    assertEquals("We should have clean backmap to avoid memory leaks", 0, map.size());
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfIdIsNullOnLock() throws InterruptedException {
    locker.lockEntity(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfIdIsNullOnUnlock() throws InterruptedException {
    locker.unlockEntity(null);
  }

}
