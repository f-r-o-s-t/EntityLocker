package com.frost.entitylocker;

@FunctionalInterface //TODO think on antorher name
interface CodeToExecute {
  void run() throws Exception;
}
