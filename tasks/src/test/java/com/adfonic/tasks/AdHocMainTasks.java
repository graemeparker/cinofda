package com.adfonic.tasks;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.adfonic.tasks.combined.BlacklistPublicationsTask;

public class AdHocMainTasks {

    public static void main(String[] args) {
        try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("adfonic-toolsdb-context.xml")) {
            DataSource dataSource = context.getBean(DataSource.class);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            File file = new File("/Users/mvanek/Downloads/byyd-domain-blacklist.csv");
            BlacklistPublicationsTask task = new BlacklistPublicationsTask(jdbcTemplate, file);
            task.execute();
            System.out.println("Press key...");
            System.in.read();
            context.close();

        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
