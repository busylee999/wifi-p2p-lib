package com.busylee.network;

import org.hamcrest.Matcher;

/**
 * Created by busylee on 12.04.16.
 */
public class Assert {
  public static void assertEquals(int expected, int actual) {
    junit.framework.Assert.assertEquals(expected, actual);
  }

  public static void assertEquals(long expected, long actual) {
    junit.framework.Assert.assertEquals(expected, actual);
  }

  public static void assertEquals(Object expected, Object actual) {
    junit.framework.Assert.assertEquals(expected, actual);
  }

  public static void assertEquals(String reason, Object expected, Object actual) {
    junit.framework.Assert.assertEquals(reason, expected, actual);
  }

  public static void assertSame(Object expected, Object actual) {
    junit.framework.Assert.assertSame(expected, actual);
  }

  public static void assertTrue(boolean condition) {
    junit.framework.Assert.assertTrue(condition);
  }

  public static void assertTrue(String reason, boolean condition) {
    junit.framework.Assert.assertTrue(reason, condition);
  }

  public static void assertFalse(boolean condition) {
    junit.framework.Assert.assertFalse(condition);
  }

  public static void assertFalse(String reason, boolean condition) {
    junit.framework.Assert.assertFalse(reason, condition);
  }

  public static void fail() {
    junit.framework.Assert.fail();
  }

  public static void fail(String reason) {
    junit.framework.Assert.fail(reason);
  }

  public static void assertNotNull(Object object) {
    junit.framework.Assert.assertNotNull(object);
  }

  public static void assertNull(Object object) {
    junit.framework.Assert.assertNull(object);
  }

  public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
    org.hamcrest.MatcherAssert.assertThat(actual, matcher);
  }

  public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
    org.hamcrest.MatcherAssert.assertThat(reason, actual, matcher);
  }

  public static void assertThat(String reason, boolean assertion) {
    org.hamcrest.MatcherAssert.assertThat(reason, assertion);
  }
}
