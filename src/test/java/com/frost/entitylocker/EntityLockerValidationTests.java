package com.frost.entitylocker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.frost.entitylocker.lockers.EntityLocker;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class EntityLockerValidationTests {

  /*
   For test we just suppose and check that concurrent execution at least twice faster than single thread
   and slower than unsafe execution
   In reality on my machine:
   unsafe execution time: 1103ms
   single thread execution time: 11613ms
   concurrent execution time: 1224ms
  */
  @Test
  public void runConcurrentStressComparisionTest() {
    long unsafeTime = measureExecutionTime(() -> runValidationTest(EntityLockerFactory.getThreadUnsafeEntityLocker(),
        TestConfiguration.SLEEP_AND_DONT_CHECK_RESULTS));
    System.out.println("unsafe execution time: " + unsafeTime + "ms");
    long singleTime = measureExecutionTime(() -> runValidationTest(EntityLockerFactory.getOneThreadLocker(),
        TestConfiguration.SLEEP_AND_CHECK_RESULTS));
    System.out.println("single thread execution time: " + singleTime + "ms");
    long concurrentTime = measureExecutionTime(() -> runValidationTest(EntityLockerFactory.getThreadSafeEntityLocker(),
        TestConfiguration.SLEEP_AND_CHECK_RESULTS));
    System.out.println("concurrent execution time: " + concurrentTime + "ms");
    assertTrue("Single thread should be at least twice slower than concurrent execution", singleTime > concurrentTime * 2);
  }

  /*
   Just run 10 threads that concurrently updating array using ProtectedCodeRunner backed by our ThreadSafeEntityLock.
   Each thread increments each element of array 10000(factor) times without sleeping between. Than we check
   that each element of array equals to threadCount*factor
  */
  @Test
  public void runConcurrentStressTest() throws Exception {
    TestConfiguration config = new TestConfiguration(true, false, 10000, 10, 100);
    runValidationTest(EntityLockerFactory.getThreadSafeEntityLocker(), config);
  }

  public boolean runValidationTest(EntityLocker<Integer> locker, TestConfiguration config) throws ExecutionException, InterruptedException {
    final int[]                    arr     = new int[config.arraySize];
    ExecutorService                service = Executors.newFixedThreadPool(config.threadCount);
    List<Future<Long>>             results = new ArrayList<>();
    ProtectedCodeExecutor<Integer> runner  = new ProtectedCodeExecutor<>(locker);

    for (int i = 0; i < config.threadCount; i++) {
      results.add(service.submit(() -> {
        for (int j = 0; j < config.arraySize * config.factor; j++) {
          final int currId = j % config.arraySize;
          runner.runProtectedCodeOnEntity(currId, () -> {
            arr[currId]++;
            if (config.withSleep) {
              Thread.sleep(1);
            }
          });
        }
        return 0L;
      }));
    }
    for (Future<Long> f : results) {
      f.get();
    }
    service.shutdown();

    int[] expected = new int[config.arraySize];
    Arrays.fill(expected, config.threadCount * config.factor);

    if (config.checkResult) {
      assertArrayEquals(expected, arr);
    }
    return true;
  }

  public long measureExecutionTime(Callable<?> code) {
    try {
      long current = System.currentTimeMillis();
      code.call();
      return System.currentTimeMillis() - current;
    } catch (Exception e) {
      return -1;
    }
  }

}
