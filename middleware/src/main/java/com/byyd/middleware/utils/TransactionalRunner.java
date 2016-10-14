package com.byyd.middleware.utils;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.transaction.annotation.Transactional;

/**
 * Generic object that can be used to run or call things transactionally.
 * If you have an arbitrary chunk of code that you need to run inside a
 * transaction, simply put an instance of TransactionalRunner in your
 * app context, and make sure your context has &lt;tx:annotation-driven/&gt.
 * Then you can use this bean to invoke your Runnable or Callable, or to
 * invoke a method by name.
 *
 * Example usage:
 *
 * @Autowired
 * private TransactionalRunner transactionalRunner;
 *
 * public void doSomething() {
 *     transactionalRunner.runTransactional(new Runnable() {
 *             public void run() {
 *                 doSomethingTransactional();
 *             }
 *         });
 * }
 * 
 * private void doSomethingTransactional() {
 *     // This is where your logic goes
 * }
 */
public class TransactionalRunner {
    private static final transient Logger LOG = Logger.getLogger(TransactionalRunner.class.getName());
    
    @Transactional(readOnly=true)
    public void runTransactionalReadOnly(Runnable runnable) {
        runnable.run();
    }

    @Transactional(readOnly=true)
    public void runTransactionalReadOnly(Object target, String methodName) {
        try {
            target.getClass().getDeclaredMethod(methodName).invoke(target);
        } catch (NoSuchMethodException e) {
            throw unchecked(e);
        } catch (IllegalAccessException e) {
            throw unchecked(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw unchecked(e);
        }
    }

    @Transactional(readOnly=true)
    public <V> V callTransactionalReadOnly(Callable<V> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw unchecked(e);
        }
    }

    @Transactional(readOnly=false)
    public void runTransactional(Runnable runnable) {
        runnable.run();
    }

    @Transactional(readOnly=false)
    public void runTransactional(Object target, String methodName) {
        try {
            target.getClass().getDeclaredMethod(methodName).invoke(target);
        } catch (NoSuchMethodException e) {
            throw unchecked(e);
        } catch (IllegalAccessException e) {
            throw unchecked(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw unchecked(e);
        }
    }

    @Transactional(readOnly=false)
    public <V> V callTransactional(Callable<V> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw unchecked(e);
        }
    }

    /**
     * Spring only rolls back a transaction for an unchecked exception, so
     * this is our hack way of ensuring that whenever any of our transactional
     * methods encounter an exception, we can ensure that the transaction
     * get rolled back irrespective of whether the root cause is checked or
     * unchecked.
     */
    private static RuntimeException unchecked(Exception e) {
        if (e instanceof RuntimeException) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("unchecked is allowing: " + e.getClass().getName());
            }
            throw (RuntimeException)e;
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("unchecked is wrapping: " + e.getClass().getName());
            }
            throw new ForceRollbackUncheckedException(e);
        }
    }

    private static final class ForceRollbackUncheckedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private ForceRollbackUncheckedException(Exception e) {
            super(e);
        }
    }
}