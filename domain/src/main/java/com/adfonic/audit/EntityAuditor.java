package com.adfonic.audit;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.User;

public interface EntityAuditor {

    void onPostPersist(Object o);
    void onPostUpdate(Object o);
    void onPostRemove(Object o);

    void ensureBaselineDataPresence(Object o);

    /**
     * Expect an onPostUpdate call for a given object.  This works in concert
     * with the onPostUpdate() and invokePendingOnPostUpdates().  There are
     * some cases where JPA lifecycle hooks do NOT get invoked for certain
     * updates.  For example, if you update an object, but all you changed
     * were some collection values (i.e. Publication.statedCategories),
     * Hibernate does NOT actually invoke onPostUpdate for the Publication.
     * Bizarre!  In order to work around this, DAOs can "queue" the
     * expectation of an onPostUpdate() call for a given object, and then
     * after the merge() it can call the invokePendingPostUpdates() method
     * in order to ensure that for every object queued, we have called
     * onPostUpdate().
     */
    void expectOnPostUpdate(Object o);

    /**
     * Invoke onPostUpdate() for any object that was queued using the
     * expectOnPostUpdate() method, but for which the onPostUpdate() method
     * wasn't already invoked by the JPA provider.
     */
    void invokePendingOnPostUpdates();
    
    void bindContext(User user, AdfonicUser adfonicUser);
    void unbindContext();
    
    /**
     * Used to set/unset entity auditing on thread
     * @param enable
     * @return whether it was already enabled
     */
    boolean setEnabledForCurrentThread(boolean enable);
}
