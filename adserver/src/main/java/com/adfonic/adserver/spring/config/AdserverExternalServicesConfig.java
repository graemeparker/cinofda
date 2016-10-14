package com.adfonic.adserver.spring.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;

import com.adfonic.ddr.amazon.AmazonS3Service;
import com.adfonic.ddr.deviceatlas.DdrDeviceAtlasS3ServiceImpl;
import com.adfonic.http.ApiClient;
import com.adfonic.quova.QuovaClient;
import com.adfonic.retargeting.redis.DeviceDataRedisReader;
import com.adfonic.retargeting.redis.GeoAudienceReader;
import com.adfonic.retargeting.redis.GeoAudienceRedisReader;
import com.adfonic.retargeting.redis.ThreadLocalClientFactory;
import com.adfonic.util.LoadBalancingHttpClient;
import com.byyd.adsquare.v2.AmpApiClient;
import com.byyd.adsquare.v2.AmpConfiguredClient;
import com.byyd.adsquare.v2.EnrichmentApiClient;
import com.byyd.factual.FactualOnPremHttpClient;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;

@Configuration
public class AdserverExternalServicesConfig {

    // Factual DMP - Geolocation/DeviceId -> audiences (via selectors)

    @Bean(destroyMethod = "close")
    public FactualOnPremHttpClient factualClient(@Value("${factual.connectionString}") String connectionString, @Value("${factual.connectTimeout:300}") int connectTimeout,
            @Value("${factual.readTimeout:100}") int readTimeout, @Value("${factual.poolTargetSize:30}") int poolTargetSize,
            @Value("${factual.poolTtlSeconds:60}") int poolTtlSeconds, @Value("${factual.failThreshold:10}") int failThreshold,
            @Value("${factual.failLockdownMillis:30000}") int failLockdownMillis) {
        return new FactualOnPremHttpClient(connectionString, connectTimeout, readTimeout, poolTargetSize, poolTtlSeconds, failThreshold, failLockdownMillis);
    }

    // Adsquare DMP Enrichment API - Geolocation/DeviceId -> audiences (via selectors)
    @Bean(destroyMethod = "close")
    public EnrichmentApiClient adsquareEnrichClient(@Value("${adsquare.enrich.connectionString}") String connectionString, //
            @Value("${adsquare.enrich.connectTimeoutMs}") int conectTimeout, @Value("${adsquare.enrich.readTimeoutMs}") int readTimeout, //
            @Value("${adsquare.enrich.poolTargetMax}") int poolTargetMax, @Value("${adsquare.enrich.poolTtlSeconds}") int poolTtlSeconds,//
            @Value("${adsquare.enrich.failThreshold}") int failThreshold, @Value("${adsquare.enrich.failLockdownMs}") int failLockdownMs) {
        ApiClient apiClient = new ApiClient("adsq-rich", connectionString, conectTimeout, readTimeout, poolTargetMax, poolTtlSeconds, failThreshold, failLockdownMs);
        return new EnrichmentApiClient(apiClient);
    }

    // Adsquare DMP Amp API
    @Bean(destroyMethod = "close")
    public AmpConfiguredClient adsquareAmpClient(@Value("${adsquare.amp.connectionString}") String connectionString, //
            @Value("${adsquare.amp.connectTimeoutMs}") int conectTimeout, @Value("${adsquare.amp.readTimeoutMs}") int readTimeout, //
            @Value("${adsquare.amp.poolTargetMax}") int poolTargetMax, @Value("${adsquare.amp.poolTtlSeconds}") int poolTtlSeconds,//
            @Value("${adsquare.amp.failThreshold}") int failThreshold, @Value("${adsquare.amp.failLockdownMs}") int failLockdownMs,//
            @Value("${adsquare.amp.username}") String username, @Value("${adsquare.amp.password}") String password, @Value("${adsquare.amp.dspId}") String dspId) {
        ApiClient apiClient = new ApiClient("adsq-amp", connectionString, conectTimeout, readTimeout, poolTargetMax, poolTtlSeconds, failThreshold, failLockdownMs);
        return new AmpConfiguredClient(new AmpApiClient(apiClient), username, password, dspId);
    }

    // Quova/Neustar - IP address -> geografic information

    @Bean
    public QuovaClient quovaClient(@Qualifier("quovaLoadBalancingHttpClient") LoadBalancingHttpClient httpClient, @Value("${QuovaClient.baseUri}") String quovaBaseUri) {
        QuovaClient client = new QuovaClient(httpClient, quovaBaseUri);
        return client;
    }

    @Bean
    public LoadBalancingHttpClient quovaLoadBalancingHttpClient(@Value("${QuovaClient.serverList}") String serverList, @Value("${QuovaClient.retryCount}") int retryCount,
            @Value("${QuovaClient.requestSentRetryEnabled}") boolean requestSentRetryEnabled, @Value("${QuovaClient.retryPrimaryIntervalMs}") long retryPrimaryIntervalMs,
            @Value("${QuovaClient.pool.connTtlMs}") int connTtlMs, @Value("${QuovaClient.pool.maxTotal}") int maxTotal,
            @Value("${QuovaClient.pool.defaultMaxPerRoute}") int defaultMaxPerRoute, @Value("${QuovaClient.useHttps}") boolean useHttps) {
        LoadBalancingHttpClient client = new LoadBalancingHttpClient(serverList, retryCount, requestSentRetryEnabled, retryPrimaryIntervalMs, connTtlMs, maxTotal,
                defaultMaxPerRoute, useHttps);
        return client;
    }

    // DMP Redis - device id -> audiences

    @Bean(name = "DmpRedisFactory")
    public ThreadLocalClientFactory dmpRedisFactory(@Value("${redis.dmp.serverlist}") String redisServerList, @Value("${redis.dmp.timeout:200}") int redisTimeout) {
        return new ThreadLocalClientFactory(redisServerList, redisTimeout);
    }

    @Bean
    public DeviceDataRedisReader deviceDataRedisReader(@Qualifier("DmpRedisFactory") ThreadLocalClientFactory clientFactory) {
        return new DeviceDataRedisReader(clientFactory);
    }

    // GEO Redis - geo location -> audiences

    @Bean(name = "GeoRedisFactory")
    public ThreadLocalClientFactory geoRedisFactory(@Value("${redis.geo.serverlist}") String redisServerList, @Value("${redis.geo.timeout:200}") int redisTimeout) {
        return new ThreadLocalClientFactory(redisServerList, redisTimeout);
    }

    @Bean
    public GeoAudienceReader geoAudienceReader(@Qualifier("GeoRedisFactory") ThreadLocalClientFactory clientFactory, @Value("${geohash.minlen:4}") int hashMinLen,
            @Value("${geohash.maxlen:7}") int hashMaxLen) {
        return new GeoAudienceRedisReader(clientFactory, hashMinLen, hashMaxLen);
    }

    // Aerospike/Citrusleaf (Impression cache)
    /*
        @Bean(destroyMethod = "close")
        public CitrusleafClient CitrusleafClient(@Value("${Citrusleaf.hostName}") String hostname, @Value("${Citrusleaf.port}") Integer port) {
            return new net.citrusleaf.CitrusleafClient(hostname, port);
        }
    */
    // Cassandra

    @Bean(name = "CassandraCluster", destroyMethod = "close")
    public Cluster cluster(@Value("${cassandra.contactpoints}") String contactpoints, @Value("${cassandra.port:9042}") int port,
            @Value("${cassandra.connectRetryMs:100}") int connectRetryMs) throws Exception {
        CassandraClusterFactoryBean factory = new CassandraClusterFactoryBean();
        factory.setContactPoints(contactpoints);
        factory.setPort(port);
        factory.setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE);
        factory.setReconnectionPolicy(new ConstantReconnectionPolicy(connectRetryMs));
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean(name = "AdsquareCassandraSession", destroyMethod = "close")
    public Session session(Cluster cluster, @Value("${adsquare.cassandra.keyspace}") String keyspace) throws Exception {
        CassandraSessionFactoryBean factory = new CassandraSessionFactoryBean();
        factory.setCluster(cluster);
        factory.setKeyspaceName(keyspace);
        factory.setConverter(converter());
        factory.setSchemaAction(SchemaAction.NONE);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean(name = "AdsquareCassandraTemplate")
    public CassandraTemplate adsquareCassandraTemplate(@Qualifier("AdsquareCassandraSession") Session session) {
        return new CassandraTemplate(session, converter());
    }

    @Bean
    public CassandraConverter converter() {
        return new MappingCassandraConverter(mappingContext());
    }

    @Bean
    public CassandraMappingContext mappingContext() {
        return new BasicCassandraMappingContext();
    }

    // Device atlas downloaded from S3

    @Bean
    public com.adfonic.ddr.amazon.AmazonS3Service ddrAmazonS3Service(@Value("${ddr.deviceatlas.s3.accessKey}") String s3AccessKey,
            @Value("${ddr.deviceatlas.s3.secretKey}") String s3SecretKey) {
        return new AmazonS3Service(s3AccessKey, s3SecretKey);
    }

    @Bean
    public DdrDeviceAtlasS3ServiceImpl ddrDeviceAtlasS3ServiceImpl(AmazonS3Service amazonS3Service,
            @Value("${ddr.deviceatlas.s3.bucket:byydtech.vir.adserverprod.resources}") String s3Bucket,
            @Value("${ddr.deviceatlas.s3.key:deviceatlas/DeviceAtlas.json.gzip}") String s3Key, @Value("${ddr.deviceatlas.s3.compressed:true}") Boolean s3Compressed) {
        return new DdrDeviceAtlasS3ServiceImpl(amazonS3Service, s3Bucket, s3Key, s3Compressed);
    }
}
