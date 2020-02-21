package com.frost.entitylocker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
  public void runCriticalCodeOnEntityInParallel() {
    long unsafeTime = measureExecutionTime(() -> runTestOnLocker(EntityLockerFactory.getThreadUnsafeEntityLocker(),
        TestConfiguration.SLEEP_AND_DONT_CHECK_RESULTS));
    System.out.println("unsafe execution time: "+unsafeTime+"ms");
    long singleTime = measureExecutionTime(() -> runTestOnLocker(EntityLockerFactory.getOneThreadLocker(),
        TestConfiguration.SLEEP_AND_CHECK_RESULTS));
    System.out.println("single thread execution time: "+singleTime+"ms");
    long concurrentTime = measureExecutionTime(() -> runTestOnLocker(EntityLockerFactory.getThreadSafeEntityLocker(),
        TestConfiguration.SLEEP_AND_CHECK_RESULTS));
    System.out.println("concurrent execution time: "+concurrentTime+"ms");
    assertTrue("Single thread should be at least twice slower than concurrent execution", singleTime > concurrentTime*2);
  }

  /*
   Just run 10 threads that concurrently updating array using ProtectedCodeRunner backed by our ThreadSafeEntityLock.
   Each thread increments each element of array 10000(factor) times without sleeping between. Than we check
   that each element of array equals to threadCount*factor
  */
  @Test
  public void runStressConcurrentTest() throws Exception {
    TestConfiguration config = new TestConfiguration(true, false, 10000, 10, 100);
    runTestOnLocker(EntityLockerFactory.getThreadSafeEntityLocker(), config);
  }

  public static void runTestOnLocker(EntityLocker<Integer> locker, TestConfiguration config) throws InterruptedException, ExecutionException {
    final int[]        arr     = new int[config.arraySize];
    ExecutorService    service = Executors.newFixedThreadPool(config.threadCount);
    List<Future<Long>> results = new ArrayList<>();
    ProtectedCodeRunner<Integer> runner = new ProtectedCodeRunner<>(locker);

    for (int i = 0; i < config.threadCount; i++) {
      results.add(service.submit(() -> {
        for (int j = 0; j < config.arraySize * config.factor; j++) {
          final int curr = j % config.arraySize;
          runner.runCriticalCodeOnEntity(curr, () -> {
            arr[curr]++;
            if (config.withSleep) {
              try {
                Thread.sleep(1);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          });
        }
        return 0L;
      }));
    }
    for (Future<Long> f : results) f.get();
    service.shutdown();
    int[] expected = new int[config.arraySize];
    for (int i = 0; i < config.arraySize; i++) expected[i] = config.threadCount*config.factor;
    System.out.println(Arrays.toString(arr));
    if (config.checkResult) assertArrayEquals(expected, arr);
  }

  public static long measureExecutionTime(CodeToExecute body) {
    try {
      long current = System.currentTimeMillis();
      body.run();
      return System.currentTimeMillis() - current;
    } catch (Exception e) {
      return -1;
    }
  }

}
