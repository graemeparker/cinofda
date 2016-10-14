package com.byyd.elasticsearch.service;

import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import com.byyd.elasticsearch.model.Hit;
import com.byyd.elasticsearch.model.NestedObjectSearchExpression;
import com.byyd.elasticsearch.model.Pagination;
import com.byyd.elasticsearch.model.SearchExpression;
import com.byyd.elasticsearch.model.SearchResult;
import com.byyd.elasticsearch.model.SimpleSearchExpression;
import com.byyd.elasticsearch.model.SortingInfo;

public class ElasticSearchService {

    private static final transient Logger APP_LOG = Logger.getLogger(ElasticSearchService.class.getName());
    private static final transient Logger ES_PERF_LOGGER = Logger.getLogger(ElasticSearchService.class.getName() + ".performance");

    // Configuration
    private String clusterName;
    private String[] hosts;
    private Integer port;
    private String user;
    private String pwd;

    // Client
    private TransportClient esClient;
    
    public ElasticSearchService(String clusterName, String[] hosts, Integer port){
        this.clusterName=clusterName;
        this.hosts=hosts;
        this.port=port;
    }
    
    public ElasticSearchService(String clusterName, String[] hosts, Integer port, String user, String pwd){
        this.clusterName=clusterName;
        this.hosts=hosts;
        this.port=port;
        this.user=user;
        this.pwd=pwd;
        
        initialize();
    }

    public void initialize() {
        StopWatch stopWatch = null;
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch = new StopWatch();
            stopWatch.start();
        }
        
        // Creating transport client object
        APP_LOG.info("Instantiating ElasticSearch client...");
        Builder settingBuilder = ImmutableSettings.settingsBuilder();
        settingBuilder.put("cluster.name", clusterName);
        
        // Set credentials
        if (StringUtils.isNotEmpty(user)) {
            APP_LOG.info("Connecting to elasticsearch with user" + user);
            settingBuilder.put("shield.user", user + ":" + pwd);
        }

        Settings settings = settingBuilder.build();
        esClient = new TransportClient(settings);
        for (String host : hosts) {
            APP_LOG.info("Adding transport address to ElasticSearch: [" + host + ":" + port + "]");
            esClient = esClient.addTransportAddress(new InetSocketTransportAddress(host, port));
        }
        
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch.stop();
            ES_PERF_LOGGER.log(Level.INFO, "initialize() method took {0}ms", stopWatch.getTime());
        }
    }

    public Long count(String index, String type, List<SearchExpression> searchExpressions) {
        StopWatch stopWatch = null;
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch = new StopWatch();
            stopWatch.start();
        }
        
        CountRequestBuilder countRequestBuilder = esClient.prepareCount(index).setTypes(type);

        if (!searchExpressions.isEmpty()) {
            countRequestBuilder.setQuery(QueryBuilders.queryStringQuery(buildQueryString(searchExpressions)));
        }

        Long result = countRequestBuilder.get().getCount();
        if (APP_LOG.isLoggable(Level.FINE)) {
            APP_LOG.fine("ElasticSearch count executed: $searchExpressions. Count= " + result);
        }
        
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch.stop();
            ES_PERF_LOGGER.log(Level.INFO, "count({0}, {1}, {2}) method took {3}ms", new Object[]{index, type, searchExpressions, stopWatch.getTime()});
        }
        
        return result;
    }

    // Query string search
    public SearchResult search(String index, String type, List<SearchExpression> searchExpressions) {
        return search(index, type, searchExpressions, null, null);
    }

    public SearchResult search(String index, String type, List<SearchExpression> searchExpressions, List<SortingInfo> sortings) {
        return search(index, type, searchExpressions, sortings, null);
    }

    public SearchResult search(String index, String type, List<SearchExpression> searchExpressions, List<SortingInfo> sortings, Pagination pagination) {
        StopWatch stopWatch = null;
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch = new StopWatch();
            stopWatch.start();
        }
        
        QueryBuilder queryBuilder = null;
        if (!searchExpressions.isEmpty()) {
            queryBuilder = QueryBuilders.queryStringQuery(buildQueryString(searchExpressions));
        }

        SearchResult searchResult = search(index, type, sortings, pagination, queryBuilder);
        logQuery(searchExpressions, searchResult.getTotalHits());
        
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch.stop();
            ES_PERF_LOGGER.log(Level.INFO, "search({0}, {1}, {2}, {3}, {4}) method took {5}ms [{6}ms]", new Object[]{index, type, searchExpressions, sortings, pagination, stopWatch.getTime(), searchResult.getTookInMillis()});
        }
        
        return searchResult;
    }

    // Implement filtered search using default match_all query and applying filters to the result
    public SearchResult searchWithNestedObject(String index, String type, List<SearchExpression> searchExpressions, NestedObjectSearchExpression nestedObject) {
        return searchWithNestedObject(index, type, searchExpressions, nestedObject, null, null);
    }

    public SearchResult searchWithNestedObject(String index, String type, List<SearchExpression> searchExpressions, NestedObjectSearchExpression nestedObject, List<SortingInfo> sortings) {
        return searchWithNestedObject(index, type, searchExpressions, nestedObject, sortings, null);
    }

    public SearchResult searchWithNestedObject(String index, String type, List<SearchExpression> searchExpressions, NestedObjectSearchExpression nestedObject, List<SortingInfo> sortings, Pagination pagination) {
        StopWatch stopWatch = null;
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch = new StopWatch();
            stopWatch.start();
        }
        
        QueryBuilder filteredQueryBuilder = null;

        QueryBuilder queryBuilder = null;
        if (!searchExpressions.isEmpty()) {
            queryBuilder = QueryBuilders.queryStringQuery(buildQueryString(searchExpressions));
        } else {
            queryBuilder = QueryBuilders.matchAllQuery();
        }

        if (nestedObject != null) {
            filteredQueryBuilder = QueryBuilders.filteredQuery(queryBuilder, buildNestedFilter(nestedObject));
        }

        SearchResult searchResult = search(index, type, sortings, pagination, filteredQueryBuilder);
        logQuery(nestedObject, searchResult.getTotalHits());
        
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch.stop();
            ES_PERF_LOGGER.log(Level.INFO, "searchWithNestedObject({0}, {1}, {2}, {3}, {4}, {5}) method took {6}ms [{7}ms]", new Object[]{index, type, searchExpressions, nestedObject, sortings, pagination, stopWatch.getTime(), searchResult.getTookInMillis()});
        }
        
        return searchResult;
    }

    public SearchResult searchById(String index, String type, List<String> ids) {
        return searchById(index, type, ids, null, null);
    }

    public SearchResult searchById(String index, String type, List<String> ids, List<SortingInfo> sortings, Pagination pagination) {
        StopWatch stopWatch = null;
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch = new StopWatch();
            stopWatch.start();
        }
        
        SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch().setIndices(index).setTypes(type);
        if ((ids != null) && (!ids.isEmpty())) {
            searchRequestBuilder.setQuery(QueryBuilders.idsQuery().ids(ids.toArray(new String[ids.size()])));
        }

        // Setting sorting
        if (sortings != null) {
            addSearchSorts(searchRequestBuilder, sortings);
        }

        // Setting Pagination
        if (pagination != null) {
            searchRequestBuilder.setSize(pagination.getSize()).setFrom(pagination.getFrom());
        }

        SearchResponse searchResponse = searchRequestBuilder.get();
        logQuery(ids, searchResponse.getHits().getTotalHits());
        
        if (ES_PERF_LOGGER.isLoggable(Level.INFO)){
            stopWatch.stop();
            ES_PERF_LOGGER.log(Level.INFO, "searchById({0}, {1}, {2}, {3}, {4}) method took {5}ms [{6}ms]", new Object[]{index, type, ids, sortings, pagination, stopWatch.getTime(), searchResponse.getTookInMillis()});
        }

        return buildSearchResult(searchResponse);
    }

    // Private methods
    private String buildQueryString(List<SearchExpression> searchExpressions) {
        StringBuilder query = new StringBuilder();
        if (!searchExpressions.isEmpty()) {
            query.append(searchExpressions.get(0).evaluate());
            for (int i = 1; i < searchExpressions.size(); i++) {
                query.append("AND").append(searchExpressions.get(i).evaluate());
            }
        }
        if (APP_LOG.isLoggable(Level.FINE)) {
            APP_LOG.fine("Generating query string " + query.toString());
        }
        return query.toString();
    }

    private SearchResult search(String index, String type, List<SortingInfo> sortings, Pagination pagination, QueryBuilder queryBuilder) {
        SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch().setIndices(index).setTypes(type);

        if (queryBuilder != null) {
            searchRequestBuilder.setQuery(queryBuilder);
        }

        // Setting sorting
        if (sortings != null) {
            addSearchSorts(searchRequestBuilder, sortings);
        }

        // Setting Pagination
        if (pagination != null) {
            searchRequestBuilder.setSize(pagination.getSize()).setFrom(pagination.getFrom());
        }

        SearchResponse searchResponse = searchRequestBuilder.get();

        return buildSearchResult(searchResponse);
    }

    /**
     *  Make a nested filter in this way:
     *  "filter": {
     *      "not" : {      <= if nestedObjectSearchExpression.addNotFilter = true
     *          "nested": {
     *                   "path" : "audiences",   <= nestedObjectSearchExpression.nestedObjectname
     *                   "filter": {
     *                      "and": [   <= add all termsExpressions (nestedObjectSearchExpression.termsExpressions)
     *                          {"term" : {"audiences.audienceId" : "aud1"}},    
     *                          {"term" : {"audiences.sessionId" : "session1"}}
     *                      ]
     *                   }
     *          }
     *       }
     *   }
     */
    private FilterBuilder buildNestedFilter(NestedObjectSearchExpression nestedObjectSearchExpression) {
        // Creating term filters
        TermFilterBuilder[] terms = new TermFilterBuilder[nestedObjectSearchExpression.getTermsExpressions().size()];
        String fieldNamePrefix = nestedObjectSearchExpression.getNestedObjectname() + ".";
        for (int cnt = 0; cnt < terms.length; cnt++) {
            SimpleSearchExpression termExpression = nestedObjectSearchExpression.getTermsExpressions().get(cnt);
            terms[cnt] = FilterBuilders.termFilter(fieldNamePrefix + termExpression.getName(), termExpression.getValue());
        }

        // Creating nested filter
        FilterBuilder result = FilterBuilders.nestedFilter(nestedObjectSearchExpression.getNestedObjectname(), FilterBuilders.andFilter(terms));
        if (nestedObjectSearchExpression.getAddNotFilter()) {
            result = FilterBuilders.notFilter(result);
        }

        return result;
    }

    private void addSearchSorts(SearchRequestBuilder searchRequestBuilder, List<SortingInfo> sorts) {
        for (SortingInfo sort : sorts) {
            if (sort.getOrder() == com.byyd.elasticsearch.model.SortingInfo.SortingOrder.ASC) {
                searchRequestBuilder.addSort(sort.getField(), SortOrder.ASC);
            } else {
                searchRequestBuilder.addSort(sort.getField(), SortOrder.DESC);
            }
        }
    }

    private SearchResult buildSearchResult(SearchResponse searchResponse) {
        SearchResult searchResult = new SearchResult(searchResponse.getTookInMillis(), searchResponse.getHits().getTotalHits());
        for (SearchHit searchHit : searchResponse.getHits().hits()) {
            Hit hit = new Hit(searchHit.getId(), searchHit.getVersion(), searchHit.getSource());
            searchResult.addHit(hit);
        }

        return searchResult;
    }

    private void logQuery(Object params, long totalHits) {
        APP_LOG.fine(MessageFormat.format("ElasticSearch query executed using: {0}. Total hits= {1}", params, totalHits));
    }
}
