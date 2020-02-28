package com.frost.entitylocker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.frost.entitylocker.executors.CodeExecutor;
import com.frost.entitylocker.executors.ProtectedCodeExecutor;
import com.frost.entitylocker.lockers.EntityLocker;
import com.frost.entitylocker.lockers.EntityLockerFactory;
import com.frost.entitylocker.utils.TestConfiguration;
import com.frost.entitylocker.utils.TestConfigurationBuilder;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class EntityLockerValidationTests {

  private static TestConfigurationBuilder DEFAULT_BUILDER              = new TestConfigurationBuilder().withSleep(true).withFactor(100);
  public static  TestConfiguration        SLEEP_AND_CHECK_RESULTS      = DEFAULT_BUILDER.withTryLock(false).withCheckResult(true).build();
  public static  TestConfiguration        SLEEP_AND_DONT_CHECK_RESULTS = DEFAULT_BUILDER.withTryLock(false).withCheckResult(false).build();

  public static TestConfiguration TRYLOCK_SLEEP_AND_CHECK_RESULTS      = DEFAULT_BUILDER.withTryLock(true).withCheckResult(true).build();
  public static TestConfiguration TRYLOCK_SLEEP_AND_DONT_CHECK_RESULTS = DEFAULT_BUILDER.withTryLock(true).withCheckResult(false).build();

  public static TestConfigurationBuilder STRESS_TEST_CONFIG_BUILDER = new TestConfigurationBuilder()
      .withArraySize(100)
      .withCheckResult(true)
      .withFactor(10000)
      .withSleep(false)
      .withThreadCount(10);

  /**
   * For test we just suppose and check that concurrent execution at least twice faster than single thread implementation and quite slower than unsafe execution.
   * In reality on my machine:
   * unsafe execution time: 1103ms
   * single thread execution time: 11613ms
   * concurrent execution time: 1224ms
   */
  @Test(timeout = 60000)
  public void checkExecutionTimeTest() {
    System.out.println("------------Execution time when lockId method is used----------------");
    long unsafeTime = measureExecutionTime(() -> runValidationTest(EntityLockerFactory.getUnsafeEntityLocker(),
        SLEEP_AND_DONT_CHECK_RESULTS)); //We don't check results for Unsafe implementation
    System.out.println("unsafe execution time: " + unsafeTime + "ms");
    long singleTime = measureExecutionTime(() -> runValidationTest(EntityLockerFactory.getSequentialEntityLocker(),
        SLEEP_AND_CHECK_RESULTS));
    System.out.println("single thread execution time: " + singleTime + "ms");
    long concurrentTime = measureExecutionTime(() -> runValidationTest(EntityLockerFactory.getConcurrentEntityLocker(),
        SLEEP_AND_CHECK_RESULTS));
    System.out.println("concurrent execution time: " + concurrentTime + "ms");
    assertTrue("Single thread should be at least twice slower than concurrent execution", singleTime > concurrentTime * 2);
    assertTrue("Concurrent execution should be slower than unsafe execution", concurrentTime > unsafeTime);
  }

  /**
   * Same as checkExecutionTimeTest but use tryLock method to acquire lock on Id
   */
  @Test(timeout = 60000)
  public void checkExecutionTimeWithTryLockTest() {
    System.out.println("------------Execution time when tryLockId method is used----------------");
    long unsafeTime = measureExecutionTime(() -> runValidationTest(EntityLockerFactory.getUnsafeEntityLocker(),
        TRYLOCK_SLEEP_AND_DONT_CHECK_RESULTS)); //We don't check results for Unsafe implementation
    System.out.println("unsafe execution time: " + unsafeTime + "ms");
    long singleTime = measureExecutionTime(() -> runValidationTest(EntityLockerFactory.getSequentialEntityLocker(),
        TRYLOCK_SLEEP_AND_CHECK_RESULTS));
    System.out.println("single thread execution time: " + singleTime + "ms");
    long concurrentTime = measureExecutionTime(() -> runValidationTest(EntityLockerFactory.getConcurrentEntityLocker(),
        TRYLOCK_SLEEP_AND_CHECK_RESULTS));
    System.out.println("concurrent execution time: " + concurrentTime + "ms");
    assertTrue("Single thread should be at least twice slower than concurrent execution", singleTime > concurrentTime * 2);
    assertTrue("Concurrent execution should be slower than unsafe execution", concurrentTime > unsafeTime);
  }

  /**
   * Just run 10 threads that concurrently updating array using ProtectedCodeRunner backed by our ThreadSafeEntityLock.
   * Each thread increments each element of array 10000(factor) times using lockId method without sleeping between.
   * Than we check that each element of array equals to threadCount*factor
   */
  @Test(timeout = 5000)
  public void runConcurrentStressTest() throws Exception {
    TestConfiguration config = STRESS_TEST_CONFIG_BUILDER.withTryLock(false).build();
    runValidationTest(EntityLockerFactory.getConcurrentEntityLocker(), config);
  }

  /**
   * Same as runConcurrentStressTest but uses tryLock method to acquire lock on entity id
   */
  @Test(timeout = 10000)
  public void runTryLockConcurrentStressTest() throws Exception {
    TestConfiguration config = STRESS_TEST_CONFIG_BUILDER.withTryLock(true).build();
    runValidationTest(EntityLockerFactory.getConcurrentEntityLocker(), config);
  }

  public boolean runValidationTest(EntityLocker<Integer> locker, TestConfiguration config) throws ExecutionException, InterruptedException {
    final int[]                    arr      = new int[config.arraySize];
    ExecutorService                service  = Executors.newFixedThreadPool(config.threadCount);
    List<Future<Long>>             results  = new ArrayList<>();
    ProtectedCodeExecutor<Integer> executor = new ProtectedCodeExecutor<>(locker);

    // Run working threads
    for (int i = 0; i < config.threadCount; i++) {
      Callable<Long> updatingCode = config.tryLock ? new TryLockTestCode(config, executor, arr) :
          new LockTestCode(config, executor, arr);
      results.add(service.submit(updatingCode));
    }

    // waiting results
    for (Future<Long> f : results) {
      f.get();
    }
    service.shutdown();

    int[] expected = new int[config.arraySize];
    Arrays.fill(expected, config.threadCount * config.factor);

    // check the results if needed
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
      throw new RuntimeException(e);
    }
  }

  abstract static class AbstractTestCode implements Callable<Long> {

    protected TestConfiguration     config;
    protected CodeExecutor<Integer> executor;
    protected int[]                 arr;

    public AbstractTestCode(TestConfiguration config, CodeExecutor<Integer> executor, int[] arr) {
      this.config = config;
      this.executor = executor;
      this.arr = arr;
    }
  }

  static class TryLockTestCode extends AbstractTestCode {

    public TryLockTestCode(TestConfiguration config, CodeExecutor<Integer> executor, int[] arr) {
      super(config, executor, arr);
    }

    @Override
    public Long call() throws Exception {
      Queue<Integer> queue = new LinkedList<>();
      for (int j = 0; j < config.arraySize * config.factor; j++) {
        queue.add(j % config.arraySize);
      }
      while (!queue.isEmpty()) {
        final int currId = queue.poll();
        boolean executed = executor.tryToExecute(currId, 1L, () -> {
          arr[currId]++;
          if (config.withSleep) {
            Thread.sleep(1);
          }
        });
        if (!executed) {
          queue.add(currId);
        }
      }
      return 0L;
    }
  }

  static class LockTestCode extends AbstractTestCode {

    public LockTestCode(TestConfiguration config, CodeExecutor<Integer> executor, int[] arr) {
      super(config, executor, arr);
    }

    @Override
    public Long call() throws Exception {
      for (int j = 0; j < config.arraySize * config.factor; j++) {
        final int currId = j % config.arraySize;
        executor.execute(currId, () -> {
          arr[currId]++;
          if (config.withSleep) {
            Thread.sleep(1);
          }
        });
      }
      return 0L;
    }
  }

}
