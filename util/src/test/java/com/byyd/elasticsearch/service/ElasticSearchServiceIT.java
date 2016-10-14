package com.byyd.elasticsearch.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.byyd.elasticsearch.model.Hit;
import com.byyd.elasticsearch.model.NestedObjectSearchExpression;
import com.byyd.elasticsearch.model.SearchExpression;
import com.byyd.elasticsearch.model.SearchResult;
import com.byyd.elasticsearch.model.SimpleSearchExpression;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-context-elasticsearch-tests-configuration.xml" })
public class ElasticSearchServiceIT {

    @Autowired
    private ElasticSearchService esService;

    @Test
    public void elasticSearchServiceShouldBeDefined() {
        assertNotNull(esService);
    }

    @Test
    public void shouldAllowSearchFilemoverDevices() {
        SearchExpression searchDevices = new SimpleSearchExpression("fileType", "DEVICES");
        
        List<SearchExpression> params = Collections.singletonList(searchDevices);

        // Call search
        SearchResult searchResult = esService.search("filemover", "file", params);
        assertNotNull(searchResult);

        // Check Hits
        List<Hit> hits = searchResult.getHits();
        assertNotNull(hits);
        assertTrue(!hits.isEmpty());

        // Check totals
        Long totalHits = searchResult.getTotalHits();
        assertNotNull(totalHits);
        assertTrue(totalHits > 0);
    }
    
    @Test
    public void testNestedObjectSearch() {
        List<SearchExpression> simpleExpressions = new ArrayList<>();
        List<SimpleSearchExpression> termsExpressions = new ArrayList<>();
        termsExpressions.add(new SimpleSearchExpression("audienceId", "aud2"));
        
        // TEST 1: give all files with are already linked to an audience
        NestedObjectSearchExpression nestedObjectSearchExpression1 = new NestedObjectSearchExpression("notifications", termsExpressions, false); 
        SearchResult searchResult1 = esService.searchWithNestedObject("tests", "file", simpleExpressions, nestedObjectSearchExpression1);
        showSearchResult(searchResult1);
        
        // TEST 2: give all files with are not linked to an audience
        simpleExpressions.add(new SimpleSearchExpression("name", "File1"));
        NestedObjectSearchExpression nestedObjectSearchExpression2 = new NestedObjectSearchExpression("notifications", termsExpressions, true); 
        SearchResult searchResult2 = esService.searchWithNestedObject("tests", "file", simpleExpressions, nestedObjectSearchExpression2);
        showSearchResult(searchResult2);
    }

    private void showSearchResult(SearchResult searchResult) {
        StringBuilder sb = new StringBuilder("Total hits: ").append(searchResult.getTotalHits());
        if (searchResult.getTotalHits()>0){
            sb.append("\nHits:\n");
            for(Hit hit : searchResult.getHits()){
                sb.append("\n").append(hit.toString());
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }
}