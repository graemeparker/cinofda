package com.adfonic.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;

/**
 * Matcher that captures an argument
 */
public final class AnyCapturingMatcher<T> extends BaseMatcher<T> {
    private final Matcher<T> anyMatcher;

    private T capturedArgument;

    public AnyCapturingMatcher(Class<T> clazz) {
        this.anyMatcher = Expectations.any(clazz);
    }

    public T getCapturedArgument() {
        return capturedArgument;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean matches(Object arg) {
        if (anyMatcher.matches(arg)) {
            capturedArgument = (T)arg;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void describeTo(Description arg) {
        anyMatcher.describeTo(arg);
    }
}
