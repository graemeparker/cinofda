package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;

import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.consumer.ConsumerThreadId;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZKGroupDirs;
import kafka.utils.ZkUtils;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * 
 * https://cwiki.apache.org/confluence/display/KAFKA/Finding+Topic+and+Partition+Leader
 * http://stackoverflow.com/questions/24902301/how-to-get-topic-list-from-kafka-server-in-java
 * 
 * https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+SimpleConsumer+Example
 * http://cfchou.github.io/blog/2015/04/23/a-closer-look-at-kafka-offsetrequest/
 * 
 * 
 * Note, however, after 0.9.0, the kafka.tools.ConsumerOffsetChecker tool is deprecated and you should use the kafka.admin.ConsumerGroupCommand
 * https://github.com/apache/kafka/blob/trunk/core/src/main/scala/kafka/admin/ConsumerGroupCommand.scala
 * https://cwiki.apache.org/confluence/display/KAFKA/Consumer+Client+Re-Design
 * https://cwiki.apache.org/confluence/display/KAFKA/Kafka+0.9+Consumer+Rewrite+Design
 * 
 * https://cwiki.apache.org/confluence/display/KAFKA/System+Tools
 * 
 * https://github.com/apache/kafka/tree/0.8/core/src/main/scala/kafka/tools
 * https://github.com/apache/kafka/tree/trunk/core/src/main/scala/kafka/tools
 * kafka.tools.ExportZkOffsets
 * kafka.tools.ConsumerOffsetChecker
 * kafka.tools.JmxTool
 * kafka.admin.ListTopicCommand
 *
 * https://github.com/quantifind/KafkaOffsetMonitor/blob/master/src/main/scala/com/quantifind/kafka/core/ZKOffsetGetter.scala
 * https://github.com/quantifind/KafkaOffsetMonitor/blob/master/src/main/scala/com/quantifind/kafka/OffsetGetter.scala
 */
@Controller
@RequestMapping(path = KafkaDebugController.URL_CONTEXT)
public class KafkaDebugController {

    static final String URL_CONTEXT = "/adserver/kafka";

    final int sessionTimeoutMs = 10 * 1000;
    final int connectionTimeoutMs = 20 * 1000;

    private Properties appProperties;

    private final ZkClient zkClient;

    private final List<Broker> brokers;

    private final Map<Integer, SimpleConsumer> brokerId2consumer = new HashMap<Integer, SimpleConsumer>();

    @Autowired
    public KafkaDebugController(@Qualifier("adserverProperties") Properties appProperties) {
        String zkconnectString = appProperties.getProperty("KafkaLogger.zookeepers");
        zkClient = new ZkClient(zkconnectString, sessionTimeoutMs, connectionTimeoutMs, ZKStringSerializer.instance);
        brokers = JavaConversions.asJavaList(ZkUtils.getAllBrokersInCluster(zkClient));
        for (Broker broker : brokers) {
            SimpleConsumer consumer = new SimpleConsumer(broker.host(), broker.port(), 100000, 64 * 1024, "OffsetCheckBroker-" + broker.id());
            brokerId2consumer.put(broker.id(), consumer);
        }
    }

    /**
     * Let's be nice and clean up
     */
    @PreDestroy
    public void shutdown() {
        try {
            zkClient.close();
        } catch (Exception x) {
            //nothing
        }
        for (SimpleConsumer consumer : brokerId2consumer.values()) {
            try {
                consumer.close();
            } catch (Exception x) {
                //nothing
            }
        }
        brokerId2consumer.clear();
    }

    private String topicLink(String topic) {
        return "<a href='" + URL_CONTEXT + "/topic/" + topic + "'>" + topic + "</a>";
    }

    private String consumerLink(String group) {
        return "<a href='" + URL_CONTEXT + "/group/" + group + "'>" + group + "</a>";
    }

    @RequestMapping(path = "")
    public void index(HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();

        writer.println(DbgUiUtil.HTML_OPEN);

        writer.println("<h3>Kafka Brokers</h3>");
        writer.println(brokers);

        List<String> consumers = zkClient.getChildren(ZkUtils.ConsumersPath());
        writer.println("<h3>Consumer Groups</h3>");
        writer.println("<ul>");
        for (String group : consumers) {
            writer.println("<li>" + consumerLink(group) + "</li>");
            writer.println("<ul>");
            List<String> topicsConsumed = getTopicsByConsumerGroup(group);
            for (String topic : topicsConsumed) {
                writer.println("<li>" + topicLink(topic) + "</li>");
            }
            writer.println("</ul>");
            writer.println("</li>");
        }
        writer.println("</ul>");

        List<String> topics = JavaConversions.seqAsJavaList(ZkUtils.getAllTopics(zkClient));
        writer.println("<h3>Kafka Topics</h3>");
        writer.println("<ul>");
        for (String topic : topics) {
            writer.println("<li>" + topicLink(topic) + "</li>");
        }
        writer.println("</ul>");

        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    @RequestMapping(path = "/topic/{topic:.+}")
    public void topics(@PathVariable("topic") String topic, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        httpResponse.setContentType("text/html");
        writer.println(DbgUiUtil.HTML_OPEN);

        writer.print("<h2>Topic " + topic + "</h2>");
        List<TopicReplicaInfo> topicReplicaInfos = getTopicReplicaInfo(topic);
        writer.println("Partitions: <ul>");
        for (TopicReplicaInfo info : topicReplicaInfos) {
            writer.print("<li>");
            writer.print("Partition: " + info.partitionId + ", Leader: " + info.leader + ", Assigned Replicas: " + info.assignedReplicas + ", InSync Replicas: "
                    + info.inSyncReplicas);
            writer.println("</li>");
        }
        writer.println("</ul>");

        List<String> consumerGroups = zkClient.getChildren(ZkUtils.ConsumersPath());
        for (String group : consumerGroups) {
            //List<String> topics = getTopicsByConsumerGroup(group);
            Map<String, scala.collection.immutable.List<ConsumerThreadId>> consumersPerTopic = JavaConversions.asJavaMap(ZkUtils.getConsumersPerTopic(zkClient, group, false));
            for (Entry<String, scala.collection.immutable.List<ConsumerThreadId>> entry : consumersPerTopic.entrySet()) {
                String eTopic = entry.getKey();
                if (eTopic.equals(topic)) {
                    writer.println("<h3>Consumer group: " + group + "</h3>");
                    List<ConsumerThreadId> consumeThreads = JavaConversions.asJavaList(entry.getValue());
                    writer.println("Threads: <ul>");
                    for (ConsumerThreadId consumerThreadId : consumeThreads) {
                        writer.print("<li>");
                        writer.println(consumerThreadId.consumer() + ", " + consumerThreadId.threadId());
                        writer.println("</li>");
                    }
                    writer.println("</ul>");
                    List<GroupTopicOffsetInfo> infos = getGroupTopicOffsetInfo(group, topic);
                    writeTopicStats(writer, group, topic, infos);

                }
            }
        }

        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    @RequestMapping(path = "/group/{consumerGroup:.+}")
    public void offsets(@PathVariable("consumerGroup") String consumerGroup, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        httpResponse.setContentType("text/html");
        writer.println(DbgUiUtil.HTML_OPEN);
        // kafka.tools.ConsumerOffsetChecker

        List<String> consumerGroups = JavaConversions.asJavaList(ZkUtils.getChildren(zkClient, ZkUtils.ConsumersPath()));
        if (StringUtils.isNotBlank(consumerGroup)) {
            ZKGroupDirs groupDirs = new ZKGroupDirs(consumerGroup);
            if (!zkClient.exists(groupDirs.consumerGroupDir())) {
                writer.println("Consumer group " + consumerGroup + " not found in " + consumerGroups);
                return;
            }
            printGroupTopicInfo(zkClient, writer, consumerGroup);

        } else {
            // Print all consumers and offsets
            writer.println("Exising consumer groups: " + consumerGroups);
            writer.println("<br/>");
            for (String group : consumerGroups) {
                printGroupTopicInfo(zkClient, writer, group);
            }
        }

        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    private void printGroupTopicInfo(ZkClient zkClient, PrintWriter writer, String group) {

        List<String> topicList = getTopicsByConsumerGroup(group);
        //List<String> topicList = zkClient.getChildren(groupDirs.consumerGroupDir() + "/owners");
        writer.print("<h3>Group " + group + " is consuming  " + topicList.size() + " topics</h3>");
        for (String topic : topicList) {
            List<GroupTopicOffsetInfo> infos = getGroupTopicOffsetInfo(group, topic);
            writeTopicStats(writer, group, topic, infos);
        }
        /*
        //ZkUtils.getPartitionsForTopics(zkClient, topicList);

        //List<String> topicsList = getTopicsList(zkClient, consumerGroup);
        for (String topic : topicList) {
            List<String> bidPidList = getBrokeridPartition(zkClient, group, topic);
            for (String bidPid : bidPidList) {
                ZKGroupTopicDirs zkGrpTpDir = new ZKGroupTopicDirs(group, topic);
                String offsetPath = zkGrpTpDir.consumerOffsetDir() + "/" + bidPid;
                Tuple2<Option<String>, Stat> result = ZkUtils.readDataMaybeNull(zkClient, offsetPath);
                if (result != null) {
                    Option<String> offsetVal = result._1();
                    Stat stat = result._2;
                    writer.println(offsetPath + " > Offset: " + offsetVal.get() + ", Stat: " + stat);
                } else {
                    writer.println("Not found: " + offsetPath);
                }
            }
        }
        */
    }

    private void writeTopicStats(PrintWriter writer, String group, String topic, List<GroupTopicOffsetInfo> infos) {
        long totalLag = 0;
        for (GroupTopicOffsetInfo oinfo : infos) {
            totalLag += (oinfo.logSize - oinfo.offset);
        }
        writer.print("<strong>Total lag " + totalLag + "</strong>");

        writer.print("<table>");
        writer.print("<th><tr>");
        writer.print("<td>Topic</td>");
        writer.print("<td>Group</td>");
        writer.print("<td>Partition</td>");
        writer.print("<td>Owner</td>");
        writer.print("<td>LogSize</td>");
        writer.print("<td>Offset</td>");
        writer.print("<td>Created</td>");
        writer.print("<td>Modified</td>");
        writer.print("</tr></th>");
        for (GroupTopicOffsetInfo oinfo : infos) {
            writer.print("<tr>");
            writer.print("<td>" + topicLink(oinfo.topic) + "</td>");
            writer.print("<td>" + consumerLink(oinfo.group) + "</td>");
            writer.print("<td>" + oinfo.partition + "</td>");
            writer.print("<td>" + oinfo.owner + "</td>");
            writer.print("<td>" + oinfo.logSize + "</td>");
            writer.print("<td>" + oinfo.offset + "</td>");
            writer.print("<td>" + DbgUiUtil.format(oinfo.created) + "</td>");
            writer.print("<td>" + DbgUiUtil.format(oinfo.modified) + "</td>");
            writer.print("</tr>");
        }
        writer.print("<table>");
    }

    private List<GroupTopicOffsetInfo> getGroupTopicOffsetInfo(String group, String topic) {
        Map<String, Seq<Object>> map = JavaConversions.asJavaMap(ZkUtils.getPartitionsForTopics(zkClient, JavaConversions.asScalaBuffer(Arrays.asList(topic)).seq()));
        //System.out.println(JavaConversions.seqAsJavaList(map.get(topic)));
        List<Integer> partitions = (List) JavaConversions.seqAsJavaList(map.get(topic));
        //Collections.sort(partitions);
        List<GroupTopicOffsetInfo> retval = new ArrayList<GroupTopicOffsetInfo>(partitions.size());
        for (Integer partition : partitions) {
            GroupTopicOffsetInfo info = getGroupTopicOffsetInfo(group, topic, partition);
            retval.add(info);
        }
        return retval;
    }

    private GroupTopicOffsetInfo getGroupTopicOffsetInfo(String group, String topic, int partition) {

        String offsetPath = ZkUtils.ConsumersPath() + "/" + group + "/offsets/" + topic + "/" + partition;
        Tuple2<String, Stat> result = ZkUtils.readData(zkClient, offsetPath);

        Long offset = Long.valueOf(result._1());
        Stat stat = result._2;

        Tuple2<Option<String>, Stat> ownerResult = ZkUtils.readDataMaybeNull(zkClient, ZkUtils.ConsumersPath() + "/" + group + "/owners/" + topic + "/" + partition);
        String owner = ownerResult._1.get();

        long logSize = queryLogSize(topic, partition);

        Date created = new Date(stat.getCtime());
        Date modified = new Date(stat.getMtime());
        return new GroupTopicOffsetInfo(group, topic, partition, offset, logSize, owner, created, modified);
    }

    private long queryLogSize(String topic, int partition) {

        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);

        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(kafka.api.OffsetRequest.LatestTime(), 1));

        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(), "OffsetCheckRequest");

        Integer brokerId = (Integer) ZkUtils.getLeaderForPartition(zkClient, topic, partition).get();
        SimpleConsumer consumer = brokerId2consumer.get(brokerId);
        OffsetResponse response = consumer.getOffsetsBefore(request);
        return response.offsets(topic, partition)[0];
    }

    private List<String> getTopicsByConsumerGroup(String group) {
        try {
            return zkClient.getChildren(ZkUtils.ConsumersPath() + "/" + group + "/offsets");
        } catch (ZkNoNodeException znx) {
            // Happens when consumer group does not listen on any existing topic (misconfiguration most likely)
            return Collections.emptyList();
        }
    }

    /**
     * This is quite unperformant method...
     */
    private List<String> getConsumerGroupsByTopic(String topic) {
        List<String> consumers = zkClient.getChildren(ZkUtils.ConsumersPath());
        List<String> retval = new ArrayList<String>();
        for (String consumer : consumers) {
            List<String> topics = getTopicsByConsumerGroup(consumer);
            if (topics.contains(topic)) {
                retval.add(consumer);
            }
        }
        return retval;
    }

    /**
     * https://github.com/apache/kafka/blob/0.8/core/src/main/scala/kafka/admin/ListTopicCommand.scala
     */
    private void listTopics(final ZkClient zkClient, PrintWriter writer) {
        Seq<String> seqTopics = ZkUtils.getAllTopics(zkClient);
        List<String> topics = JavaConversions.seqAsJavaList(seqTopics);
        writer.println("Topics: " + topics);
        for (String topic : topics) {
            scala.collection.Map<String, scala.collection.Map<Object, Seq<Object>>> topicPartitionAssignment = ZkUtils.getPartitionAssignmentForTopics(zkClient, JavaConversions
                    .asScalaBuffer(Arrays.asList(topic)).seq());
            java.util.Map<Object, Seq<Object>> partition2replicas = JavaConversions.mapAsJavaMap(JavaConversions.mapAsJavaMap(topicPartitionAssignment).get(topic));
            for (Entry<Object, Seq<Object>> entry : partition2replicas.entrySet()) {
                Integer partitionId = (Integer) entry.getKey();
                List<Object> assignedReplicas = JavaConversions.asJavaList(entry.getValue());
                List<Object> inSyncReplicas = JavaConversions.asJavaList(ZkUtils.getInSyncReplicasForPartition(zkClient, topic, partitionId));
                Option<Object> leader = ZkUtils.getLeaderForPartition(zkClient, topic, partitionId);
                writer.println("Topic: " + topic + ", partition: " + partitionId + ", leader: " + leader.get() + ", replicas: " + assignedReplicas + ", inSyncReplicas: "
                        + inSyncReplicas);
            }
        }
    }

    private List<TopicReplicaInfo> getTopicReplicaInfo(String topic) {
        scala.collection.Map<String, scala.collection.Map<Object, Seq<Object>>> topicPartitionAssignment = ZkUtils.getPartitionAssignmentForTopics(zkClient, JavaConversions
                .asScalaBuffer(Arrays.asList(topic)).seq());
        java.util.Map<Object, Seq<Object>> partition2replicas = JavaConversions.mapAsJavaMap(JavaConversions.mapAsJavaMap(topicPartitionAssignment).get(topic));
        List<TopicReplicaInfo> retval = new ArrayList<TopicReplicaInfo>(partition2replicas.size());
        for (Entry<Object, Seq<Object>> entry : partition2replicas.entrySet()) {
            Integer partitionId = (Integer) entry.getKey();
            List<Integer> assignedReplicas = (List) JavaConversions.asJavaList(entry.getValue());
            List<Integer> inSyncReplicas = (List) JavaConversions.asJavaList(ZkUtils.getInSyncReplicasForPartition(zkClient, topic, partitionId));
            Option<Object> leader = ZkUtils.getLeaderForPartition(zkClient, topic, partitionId);
            TopicReplicaInfo info = new TopicReplicaInfo(topic, partitionId, (Integer) leader.get(), assignedReplicas, inSyncReplicas);
            retval.add(info);

        }
        return retval;
    }

    private static List<String> getBrokeridPartition(ZkClient zkClient, String group, String topic) {
        Seq<String> seq = ZkUtils.getChildrenParentMayNotExist(zkClient, "/consumers/" + group + "/offsets/" + topic);
        return JavaConversions.asJavaList(seq);
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

    static class TopicReplicaInfo {
        final String topic;
        final Integer partitionId;
        final Integer leader;
        final List<Integer> assignedReplicas;
        final List<Integer> inSyncReplicas;

        public TopicReplicaInfo(String topic, Integer partitionId, Integer leader, List<Integer> assignedReplicas, List<Integer> inSyncReplicas) {
            this.topic = topic;
            this.partitionId = partitionId;
            this.leader = leader;
            this.assignedReplicas = assignedReplicas;
            this.inSyncReplicas = inSyncReplicas;
        }
    }

    static class GroupTopicOffsetInfo {
        final String group;
        final String topic;
        final Integer partition;
        final Long offset;
        final Long logSize;
        final String owner;
        final Date created;
        final Date modified;

        public GroupTopicOffsetInfo(String group, String topic, Integer partition, Long offset, Long logSize, String owner, Date created, Date modified) {
            this.group = group;
            this.topic = topic;
            this.partition = partition;
            this.offset = offset;
            this.logSize = logSize;
            this.owner = owner;
            this.created = created;
            this.modified = modified;
        }
    }
}
