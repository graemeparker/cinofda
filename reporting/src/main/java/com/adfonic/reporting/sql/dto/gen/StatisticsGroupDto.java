package com.adfonic.reporting.sql.dto.gen;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Non-leaf node in pub stats tree. Hence shared by multiple rows - thread safe for intended 
 * use but not very strictly since the read-only view of the returned collection getTaggedSet 
 * can iterate simultaneously with a write.
 * 
 * Returns insertion order after row mapping etc. 
 * 
 * Locks are just to support a hypothetical scenario where row mapping is multi-threaded.
 * In normal case it is sequential and without contention it'll optimize to avoid overhead   
 */
public class StatisticsGroupDto extends AbstractTaggedDto implements TaggedTagGroup, Serializable {

    private static final long serialVersionUID = 3L;


    public StatisticsGroupDto(Tag tag) {
        super(tag);
        taggedMap = new LinkedHashMap<>();
    }

    private final Map<Tag, Tagged> taggedMap;


    @Override
    public synchronized Collection<Tagged> getTaggedSet() {
        return Collections.unmodifiableCollection(taggedMap.values());
    }


    public StatisticsGroupDto getChildGroupByTag(Tag tag) {
        return (StatisticsGroupDto) createAndPutIfAbsent(tag, null);
    }


    public void addTagged(Tagged tagged) {
        getCanonical(tagged);
    }
    
    
    private Tagged getCanonical(Tagged tagged) {
        return createAndPutIfAbsent(tagged.getTag(), tagged);
    }
    
    private synchronized Tagged createAndPutIfAbsent(Tag tag, Tagged tagged){
        Tagged candidateTag = taggedMap.get(tag);
        if (candidateTag == null) {
            candidateTag = tagged == null ? new StatisticsGroupDto(tag)
                                          : tagged;
            taggedMap.put(tag, candidateTag);// no need to check return
        }
        return candidateTag;
    }

}
