package com.byyd.elasticsearch.model;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

    private Long tookInMillis;
    private Long totalHits;
    private List<Hit> hits;

    public SearchResult(Long tookInMillis, Long totalHits) {
        super();
        this.tookInMillis = tookInMillis;
        this.totalHits = totalHits;
        hits = new ArrayList<Hit>(totalHits.intValue());
    }

    public Long getTookInMillis() {
        return tookInMillis;
    }

    public Long getTotalHits() {
        return this.totalHits;
    }

    public List<Hit> getHits() {
        return this.hits;
    }

    public void addHit(Hit hit) {
        if (hit != null) {
            this.hits.add(hit);
        }
    }
}
