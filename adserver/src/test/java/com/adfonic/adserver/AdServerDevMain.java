package com.adfonic.adserver;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

import kafka.admin.AdminUtils;
import kafka.api.TopicMetadata;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.TestUtils;
import kafka.utils.Time;
import kafka.zk.EmbeddedZookeeper;
import net.anthavio.aspect.ApiPolicyOverride;
import net.byyd.archive.model.v1.AdAction;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.adfonic.util.ActiveMqUtil;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.Pair;

/**
 * 
 * @author mvanek
 * 
 * 
 * You need to set adfonic.config.home system property to directory where your adserver configuration 
 * files (adfonic-adserver.properties, ...) can be found
 * 
 * Similarily adfonic.data.home and adfonic.cache.home
 * 
 * Use -Dcache.prefer.local=true to skip download of cache files from S3 on startup
 * Use -Dactivemq.skip=true to skip start of embedded ActiveMQ
 * 
 */
@ApiPolicyOverride
public class AdServerDevMain {

    public static void main(String[] args) {
        try {

            LogManager.getLogManager().reset();
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
            //CassandraDaemon cassandraDaemon = new CassandraDaemon();
            //cassandraDaemon.init(null);
            //cassandraDaemon.start();

            if (System.getProperty("skip.activemq") == null) {
                ActiveMqUtil.ensureActiveMq("tcp://localhost:" + 61616);
            }

            Properties appProperties = ConfUtils.checkAppProperties("adserver");
            KafkaEmbedder kafka;
            if (!Boolean.valueOf(appProperties.getProperty("KafkaLogger.disabled"))) {
                kafka = new KafkaEmbedder(appProperties);
                kafka.createTopics(appProperties);
            }

            System.setProperty(Globals.CATALINA_HOME_PROP, "./target/tomcat");
            System.setProperty("hornetq.embedded.persistenceEnabled", "false"); //adfonic-tracker-context.xml

            Pair<Tomcat, Context> pair = startTomcat();
            Tomcat tomcat = pair.first;
            Context context = pair.second;

            Connector[] connectors = tomcat.getService().findConnectors();
            System.out.println("Tomcat started: " + Arrays.asList(connectors));
            tomcat.getServer().await();

        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Cannot use TomcatUtil as AdServer's Tomcat needs more configuration
     */
    private static Pair<Tomcat, Context> startTomcat() throws Exception {
        Tomcat tomcat = new Tomcat();

        tomcat.addUser("adfonic", "adfon1c");
        tomcat.addRole("adfonic", "internal"); //adserver /internal context 
        tomcat.addUser("app-support", "b6ataIYF0Fh1tN4");
        tomcat.addRole("app-support", "internal");

        Service service = tomcat.getService();

        int httpPort = 8888;
        String syspropHttpPort = System.getProperty("http.port");
        if (StringUtils.isNotBlank(syspropHttpPort)) {
            httpPort = Integer.parseInt(syspropHttpPort);
        }

        Connector defaultConnector = new Connector(Http11NioProtocol.class.getName());
        defaultConnector.setPort(httpPort);
        defaultConnector.setEnableLookups(false);
        defaultConnector.setProperty("connectionTimeout", "500");
        //connector.setMaxPostSize(8000);
        //connector.setProperty("connectionUploadTimeout", "2048");
        //connector.setExecutor
        //connector.setProperty("socket.rxBufSize", "2048");
        //connector.setProperty("socket.appReadBufSize", "2048");
        //connector.setProperty("socketBuffer", "2048");
        service.addConnector(defaultConnector);

        Connector httpsConnector = new Connector(Http11NioProtocol.class.getName());
        httpsConnector.setPort(httpPort + 1);
        httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        httpsConnector.setAttribute("keystoreFile", System.getProperty("user.dir") + "/etc/localhost.jks");
        httpsConnector.setAttribute("keystorePass", "secret");
        httpsConnector.setAttribute("keyAlias", "localhost");
        httpsConnector.setAttribute("clientAuth", "false");
        httpsConnector.setAttribute("sslProtocol", "TLS");
        httpsConnector.setAttribute("SSLEnabled", true);
        service.addConnector(httpsConnector);

        tomcat.setConnector(defaultConnector);
        tomcat.setPort(httpPort);

        //httpsConnector.setService(service);

        String webAppBaseDir = new File("src/main/webapp").getAbsolutePath();
        StandardContext context = (StandardContext) tomcat.addWebapp("/", webAppBaseDir);
        context.setFailCtxIfServletStartFails(true);
        System.out.println("Configuring Webapp with basedir: " + webAppBaseDir);

        tomcat.start();

        if (context.getState() != LifecycleState.STARTED) {
            // Failed webapp start -> Stop Tomcat and JVM
            System.out.println("Shutdown because Webapp is " + context.getState());
            //kafka.close();
            System.exit(1);
        }

        return Pair.of(tomcat, context);
    }

    static class KafkaEmbedder implements Closeable {

        private final String zkConnect;
        //private final ZooKeeperLocal zookeeper;
        private final EmbeddedZookeeper zookeeper;

        private final List<KafkaServer> brokers = new ArrayList<KafkaServer>();

        /**
         * https://gist.github.com/vmarcinko/e4e58910bcb77dac16e9 - EmbeddedKafkaCluster
         * https://gist.github.com/asmaier/6465468 - KafkaProducerTest
         */
        public KafkaEmbedder(Properties adserverProperties) throws IOException, InterruptedException {
            zkConnect = "localhost:2181";
            zookeeper = new EmbeddedZookeeper(zkConnect);
            //zookeeper = new ZooKeeperLocal(zkConnect);
            System.out.println("Zookeper started: " + zookeeper.connectString());

            // Usually only one broker will be used for local tests, but if we need to test something deep-in-kafka...
            String[] brokerAddresses = adserverProperties.getProperty("KafkaLogger.brokers").split(",");
            for (int i = 0; i < brokerAddresses.length; ++i) {
                String brokerAddress = brokerAddresses[i];
                String[] hostAndPort = brokerAddress.split(":");
                // String host = hostAndPort[0]; // We do not need hostname. It is allways localhost
                String brokerPort = hostAndPort[1];
                Properties props = TestUtils.createBrokerConfig(i, Integer.parseInt(brokerPort), true);
                props.setProperty("zookeeper.connect", zookeeper.connectString());
                KafkaConfig config = new KafkaConfig(props);
                KafkaServer broker = TestUtils.createServer(config, SystemTime.instance);
                //KafkaServer broker = startBroker(zkConnect, i, brokerPort);
                brokers.add(broker);
                System.out.println("Kafka broker started. id: " + i + ", port: " + brokerPort + ", status: " + broker.brokerState().currentState());
            }

        }

        public void createTopics(Properties adserverProperties) {
            System.out.println("Ensuring Kafka topics existence");
            //ZkClient zkClient = new ZkClient(zookeeper.connectString(), 1000);
            ZkClient zkClient = new ZkClient(zookeeper.connectString(), 5000, 1000, ZKStringSerializer.instance);

            String kafkaTopicPrefix = adserverProperties.getProperty("KafkaLogger.topicprefix");
            String clusterShard = adserverProperties.getProperty("KafkaLogger.shard");
            String environment = adserverProperties.getProperty("KafkaLogger.environment");
            String kafkaTopicPostfix = adserverProperties.getProperty("KafkaLogger.posfix");
            AdAction[] values = AdAction.values();
            for (AdAction value : values) {
                String topicName = kafkaTopicPrefix + "." + value.getShortName() + "_" + clusterShard + "_" + environment + "_" + kafkaTopicPostfix;
                //
                Properties topicConfig = new Properties();
                TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(topicName, zkClient);
                if (topicMetadata.partitionsMetadata().isEmpty()) {
                    AdminUtils.createTopic(zkClient, topicName, 1, 1, topicConfig);
                    TestUtils.waitUntilMetadataIsPropagated(scala.collection.JavaConversions.asScalaBuffer(brokers), topicName, 0, 5000);
                    System.out.println("Kafka topic created: " + topicName);
                }
                //String[] arguments = new String[] { "--topic", topicName, "--partitions", "1", "--replication-factor", "1" };
                //TopicCommand.createTopic(zkClient, new TopicCommand.TopicCommandOptions(arguments));

                //} catch (TopicExistsException tcx) {
                //    System.out.println("Kafka topic found: " + topicName);
                //}
            }
            zkClient.close();
        }

        private KafkaServer startBroker(String zkServers, int i, String port) {
            // https://kafka.apache.org/08/configuration.html#brokerconfigs
            // https://www.codatlas.com/github.com/apache/kafka/trunk/core/src/test/scala/unit/kafka/utils/TestUtils.scala?line=178
            Properties brokerProps = new Properties();
            brokerProps.setProperty("zookeeper.connect", zkServers);
            brokerProps.setProperty("broker.id", String.valueOf(i)); // must be unique
            brokerProps.setProperty("log.dirs", "target/kafka/log-" + i);
            brokerProps.setProperty("host.name", "localhost");
            brokerProps.setProperty("port", port);
            brokerProps.put("log.flush.interval.messages", "1");
            brokerProps.put("replica.socket.timeout.ms", "1500");
            KafkaConfig brokerConfig = new KafkaConfig(brokerProps);
            KafkaServer broker = new KafkaServer(brokerConfig, new SystemTime());
            broker.startup();
            return broker;
        }

        @Override
        public void close() {
            for (KafkaServer broker : brokers) {
                broker.shutdown();
                System.out.println("Kafka broker stopped. id: " + broker.socketServer().brokerId() + ", port: " + broker.socketServer().port());
            }
            //zookeeper.close();
            zookeeper.shutdown();
            System.out.println("Zookeeper stopped");
        }

    }

    /**
     * I can't find anything different form EmbeddedZookeeper and still this does not work! Fuck Scala!
     */
    static class ZooKeeperLocal implements Closeable {

        private static final int DEFAULT_PORT = 2181;
        private static final String DEFAULT_HOST = "localhost";

        private final ZooKeeperServer server;
        private final NIOServerCnxnFactory factory;
        private final String hostname;
        private final int clientPort;

        public ZooKeeperLocal(String connectString) throws IOException, InterruptedException {
            this(connectString.split(":")[0], Integer.parseInt(connectString.split(":")[1]), "target/zookeeper");
        }

        public ZooKeeperLocal() throws IOException, InterruptedException {
            this(DEFAULT_HOST, DEFAULT_PORT, "target/zookeeper");
        }

        public ZooKeeperLocal(String host, int port, String baseDir) throws IOException, InterruptedException {
            int tickTime = 500;
            File logDir = new File(baseDir + "/log");
            File snapshotDir = new File(baseDir + "/snapshot");
            server = new ZooKeeperServer(snapshotDir, logDir, tickTime);
            factory = new NIOServerCnxnFactory();
            InetSocketAddress socketAddr = new InetSocketAddress(host, port);
            factory.configure(socketAddr, 16);
            factory.startup(server);
            hostname = host;
            clientPort = server.getClientPort();

        }

        public String connectString() {
            return hostname + ":" + clientPort;
        }

        public int getClientPort() {
            return clientPort;
        }

        @Override
        public void close() {
            server.shutdown();
            factory.shutdown();
        }

    }

    static class SystemTime implements Time {

        public static final SystemTime instance = new SystemTime();

        @Override
        public long milliseconds() {
            return System.currentTimeMillis();
        }

        @Override
        public long nanoseconds() {
            return System.nanoTime();
        }

        @Override
        public void sleep(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                // no stress
            }
        }

    }

    public static class ZKStringSerializer implements ZkSerializer {

        private static final Charset utf8 = Charset.forName("utf-8");

        public static final ZKStringSerializer instance = new ZKStringSerializer();

        @Override
        public byte[] serialize(Object data) throws ZkMarshallingError {
            return ((String) data).getBytes(utf8);
        }

        @Override
        public Object deserialize(byte[] bytes) throws ZkMarshallingError {
            if (bytes != null) {
                return new String(bytes, utf8);
            }
            return null;
        }

    }
}
