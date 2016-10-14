package com.adfonic.tasks.config.combined;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.KryoManager;
import com.adfonic.tasks.combined.AdSpaceDormantizer;
import com.adfonic.tasks.combined.BlacklistPublicationsTask;
import com.adfonic.tracker.VideoViewAdEventLogic;
import com.adfonic.tracker.jdbc.TrackerMultiServiceJdbcImpl;
import com.adfonic.util.ConfUtils;

@Configuration
@ComponentScan(basePackageClasses = AdSpaceDormantizer.class)
@ImportResource("adfonic-mmx-context.xml")
public class CombinedTasksSpringConfig {

    private static final String BLACKLIST_FILENAME = "byyd-domain-blacklist.csv";

    @Bean
    public JavaMailSender mailSender(@Value("${email.outbound.host}") String host, @Value("${email.outbound.port}") int port, @Value("${email.outbound.protocol}") String protocol,
            @Value("${email.outbound.username}") String username, @Value("${email.outbound.password}") String password) {

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setProtocol(protocol);
        sender.setUsername(username);
        sender.setPassword(password);
        return sender;
    }

    /**
     * Implements ClickService, InstallService, ConversionService, VideoViewService
     */
    @Bean
    public TrackerMultiServiceJdbcImpl trackerMultiServiceJdbcImpl(@Qualifier(ConfUtils.TRACKER_DS) DataSource dataSource) {
        return new TrackerMultiServiceJdbcImpl(dataSource);
    }

    @Bean
    public AdEventFactory adEventFactory() {
        return new AdEventFactory(new KryoManager());
    }

    @Bean
    public VideoViewAdEventLogic videoViewAdEventLogic(AdEventFactory adEventFactory) {
        return new VideoViewAdEventLogic(adEventFactory);
    }

    @Bean
    public BlacklistPublicationsTask blacklistPublicationsTask(@Qualifier(ConfUtils.TOOLS_JDBC_TEMPLATE) JdbcTemplate jdbc) throws IOException {
        return new BlacklistPublicationsTask(jdbc, BLACKLIST_FILENAME);
    }

}
