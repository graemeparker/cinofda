package com.byyd.middleware.iface.dao.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.adfonic.domain.BusinessKey;
import com.byyd.middleware.iface.dao.FetchStrategy;

/**
 * This class is a typesafe version of the FetchStrategyImpl class meant to be used direcly (inline) within JPA implementations
 * of service-level classes. It allows the use of MetaModel classes directly, and their SingularAttributes and PluralAttributes members.
 * The advangtage of using this approach is that you'll get compilation errors when fields you are using in a FetchStrategy
 * are removed from the target Object, since the MetaModel classes get regenerated.
 *
 * Note that this class can only be used to defined the fetch strategy of ONE class (the class is parameterized).
 *
 * So, here's an example of inline use of the regular FetchStrategyImpl:
 *
 *         public RateCard getRateCardByBidType(BidType bidType) {
 *            FetchStrategyImpl fetchStrategy = new FetchStrategyImpl();
 *            fetchStrategy.addEagerlyLoadedFieldForClass(DefaultRateCard.class, "rateCard");
 *            DefaultRateCard defaultRateCard = getDefaultRateCardByBidType(bidType, fetchStrategy);
 *            if(defaultRateCard == null) {
 *                return null;
 *            }
 *            return defaultRateCard.getRateCard();
 *        }
 *
 * And here's the same logic using the FetchStrategyJpaImpl:
 *
 *         public RateCard getRateCardByBidType(BidType bidType) {
 *            FetchStrategyJpaImpl<DefaultRateCard> fetchStrategy = new FetchStrategyJpaImpl<DefaultRateCard>();
 *            fetchStrategy.addEagerlyLoadedFieldForClass(DefaultRateCard_.rateCard);
 *            DefaultRateCard defaultRateCard = getDefaultRateCardByBidType(bidType, fetchStrategy);
 *            if(defaultRateCard == null) {
 *                return null;
 *            }
 *            return defaultRateCard.getRateCard();
 *        }
 *
 * Note that the addEagerlyLoadedFieldForClass() methods can be passed a JoinType, but this time, it's the javax.persistence.criteria.JoinType type.
 *
 * @author Pierre Adriaans
 *
 * @param <T> the class whose fetch you want to parameterize.
 */
public class FetchStrategyJpaImpl<T extends BusinessKey> implements FetchStrategy {

    // List of SingularAttributes to eagerly fetch
    private final List<SingularAttribute<T, ?>> singularAttributes = new ArrayList<SingularAttribute<T, ?>>();
    // JoinTypes for SingularAttributes to eagerly fetch
    private final Map<SingularAttribute<T, ?>, JoinType> singularAttributesJoinTypes = new HashMap<SingularAttribute<T, ?>, JoinType>();

    // List of PluralAttributes to eagerly fetch
    private final List<PluralAttribute<T,? extends Collection<? extends BusinessKey>, ?>> pluralAttributes = new ArrayList<PluralAttribute<T,? extends Collection<? extends BusinessKey>, ?>>();
    // JoinTypes for PluralAttributes to eagerly fetch
    private final Map<PluralAttribute<T, ? extends Collection<? extends BusinessKey>, ?>, JoinType> pluralAttributesJoinTypes = new HashMap<PluralAttribute<T, ? extends Collection<? extends BusinessKey>, ?>, JoinType>();
    
    public FetchStrategyJpaImpl() {
        super();
    }

    /**
     * Adds a SingularAttributes with the default JoinType
     * @param singularAttribute the attribute to add
     * @return this
     */
    public FetchStrategyJpaImpl<T> addEagerlyLoadedFieldForClass(SingularAttribute<T, ?> singularAttribute) {
        return addEagerlyLoadedFieldForClass(singularAttribute, null);
    }
    /**
     * Adds a SingularAttributes with a specific JoinType
     * @param singularAttribute the attribute to add
     * @param joinType the desired JoinType
     * @return this
     */
    public FetchStrategyJpaImpl<T> addEagerlyLoadedFieldForClass(SingularAttribute<T, ?> singularAttribute, JoinType joinType) {
        singularAttributes.add(singularAttribute);
        if(joinType != null) {
            singularAttributesJoinTypes.put(singularAttribute, joinType);
        }
        return this;
    }
    /**
     * Adds a PluralAttribute with the default JoinType
     * @param pluralAttribute the attribute to add
     * @return this
     */
    public FetchStrategyJpaImpl<T> addEagerlyLoadedFieldForClass(PluralAttribute<T,? extends Collection<? extends BusinessKey>, ?> pluralAttribute) {
        return addEagerlyLoadedFieldForClass(pluralAttribute, null);
    }
    /**
     * Adds a PluralAttribute with a specific JoinType
     * @param pluralAttribute
     * @param joinType the desired JoinType
     * @return
     */
    public FetchStrategyJpaImpl<T> addEagerlyLoadedFieldForClass(PluralAttribute<T,? extends Collection<? extends BusinessKey>, ?> pluralAttribute, JoinType joinType) {
        pluralAttributes.add(pluralAttribute);
        if(joinType != null) {
            pluralAttributesJoinTypes.put(pluralAttribute, joinType);
        }
        return this;
    }
    /**
     * Getter.
     * @return the raw list
     */
    public List<SingularAttribute<T, ?>> getSingularAttributes() {
        return getSingularAttributes(false);
    }
    /**
     * Getter. Returns either the raw list, or a read-only version of it
     * @param unmodifiable specifies if the list has to be made read-only
     * @return if true, a read-only version of the list is returned, if false, the raw list is returned
     */
    public List<SingularAttribute<T, ?>> getSingularAttributes(boolean unmodifiable) {
        if(unmodifiable) {
            return Collections.unmodifiableList(singularAttributes);
        } else {
            return singularAttributes;
        }
    }
    /**
     *
     * @param singularAttribute
     * @return
     */
    public JoinType getJoinType(SingularAttribute<T, ?> singularAttribute) {
        return singularAttributesJoinTypes.get(singularAttribute);
    }
    /**
     * Getter.
     * @return the raw list
     */
    public List<PluralAttribute<T,? extends Collection<? extends BusinessKey>, ?>> getPluralAttributes() {
        return getPluralAttributes(false);
    }
    /**
     * Getter. Returns either the raw list, or a read-only version of it
     * @param unmodifiable specifies if the list has to be made read-only
     * @return if true, a read-only version of the list is returned, if false, the raw list is returned
     */
    public List<PluralAttribute<T,? extends Collection<? extends BusinessKey>, ?>> getPluralAttributes(boolean unmodifiable) {
        if(unmodifiable) {
            return Collections.unmodifiableList(pluralAttributes);
        } else {
            return pluralAttributes;
        }
    }
    /**
     * Getter. Returns either the raw list, or a read-only version of it
     * @param unmodifiable specifies if the list has to be made read-only
     * @return if true, a read-only version of the list is returned, if false, the raw list is returned
     */
    public JoinType getJoinType(PluralAttribute<T,? extends Collection<? extends BusinessKey>, ?> attribute) {
         return pluralAttributesJoinTypes.get(attribute);
     }

 }
