package com.frost.entitylocker;

import com.frost.entitylocker.lockers.EntityLocker;
import com.frost.entitylocker.lockers.OneThreadEntityLocker;
import com.frost.entitylocker.lockers.ThreadSafeEntityLocker;
import com.frost.entitylocker.lockers.ThreadUnsafeEntityLocker;

public class EntityLockerFactory {

  public static <T> EntityLocker<T> getOneThreadLocker() {
    return new OneThreadEntityLocker<>();
  }

  public static <T> EntityLocker<T> getThreadSafeEntityLocker() {
    return new ThreadSafeEntityLocker<>();
  }

  public static <T> EntityLocker<T> getThreadUnsafeEntityLocker() {
    return new ThreadUnsafeEntityLocker<>();
  }
}
