package com.byyd.middleware.iface.dao.jpa;

import javax.persistence.metamodel.Attribute;

import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;

public final class FetchStrategyBuilder {
    private final FetchStrategyImpl impl;
    private boolean built = false;

    public FetchStrategyBuilder() {
        this.impl = new FetchStrategyImpl();
    }
    
    public FetchStrategyBuilder(FetchStrategy fs) {
        this.impl = (FetchStrategyImpl) fs;
    }

    public FetchStrategyBuilder addInner(Attribute<?,?> attribute) {
        assertNotBuiltYet();
        impl.addEagerlyLoadedFieldForClass(getClassInWhichAttributeLives(attribute), attribute.getName(), JoinType.INNER);
        return this;
    }

    public FetchStrategyBuilder addLeft(Attribute<?,?> attribute) {
        assertNotBuiltYet();
        impl.addEagerlyLoadedFieldForClass(getClassInWhichAttributeLives(attribute), attribute.getName(), JoinType.LEFT);
        return this;
    }

    public FetchStrategyBuilder addRight(Attribute<?,?> attribute) {
        assertNotBuiltYet();
        impl.addEagerlyLoadedFieldForClass(getClassInWhichAttributeLives(attribute), attribute.getName(), JoinType.RIGHT);
        return this;
    }

    public FetchStrategyBuilder addAllInner(Class<?> clazz) {
        assertNotBuiltYet();
        impl.allEager(clazz, JoinType.INNER);
        return this;
    }

    public FetchStrategyBuilder addAllLeft(Class<?> clazz) {
        assertNotBuiltYet();
        impl.allEager(clazz, JoinType.LEFT);
        return this;
    }

    public FetchStrategyBuilder addAllRight(Class<?> clazz) {
        assertNotBuiltYet();
        impl.allEager(clazz, JoinType.RIGHT);
        return this;
    }

    public FetchStrategyBuilder remove(Attribute<?,?> attribute) {
        assertNotBuiltYet();
        impl.removeEagerlyLoadedFieldForClass(getClassInWhichAttributeLives(attribute), attribute.getName());
        return this;
    }

    public FetchStrategyBuilder recursive(Attribute<?,?> attribute) {
        return recursive(attribute, true);
    }

    public FetchStrategyBuilder nonRecursive(Attribute<?,?> attribute) {
        return recursive(attribute, false);
    }

    public FetchStrategyBuilder recursive(Attribute<?,?> attribute, boolean recursive) {
        assertNotBuiltYet();
        impl.setRecursiveProcessingForField(getClassInWhichAttributeLives(attribute), attribute.getName(), recursive);
        return this;
    }

    public FetchStrategyBuilder recursive(Class<?> clazz) {
        return recursive(clazz, true);
    }

    public FetchStrategyBuilder nonRecursive(Class<?> clazz) {
        return recursive(clazz, false);
    }

    public FetchStrategyBuilder recursive(Class<?> clazz, boolean recursive) {
        assertNotBuiltYet();
        impl.setRecursiveProcessingForClass(clazz, recursive);
        return this;
    }

    public FetchStrategy build() {
        assertNotBuiltYet();
        built = true; // prevent subsequent modifications or build calls
        return impl;
    }

    private Class<?> getClassInWhichAttributeLives(Attribute<?,?> attribute) {
        return attribute.getDeclaringType().getJavaType();
    }

    private void assertNotBuiltYet() {
        if (built) {
            throw new IllegalStateException("Already built...this simple builder doesn't support modify-after-build");
        }
    }
}