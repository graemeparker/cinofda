package com.adfonic.adserver.rtb.util;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.adfonic.adserver.spring.config.AdserverStatusSpringConfig.AdServerResource;
import com.adfonic.util.status.BaseResourceCheck;
import com.adfonic.util.status.ResourceId;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;

/**
 * 
 * @author mvanek
 *
 */
public class SimpleCassandraCheck extends BaseResourceCheck<AdServerResource> {

    public static final String DEFAULT_QUERY = "SELECT now() FROM system.local";
    private final Session session;

    private final String query;

    public SimpleCassandraCheck(Session session, String query) {
        Objects.requireNonNull(session);
        this.session = session;
        Objects.requireNonNull(query);
        this.query = query;
    }

    public SimpleCassandraCheck(Session session) {
        this(session, DEFAULT_QUERY);
    }

    @Override
    public String doCheck(ResourceId<AdServerResource> resource) throws Exception {

        List<Row> all = session.execute(query).all();
        if (query != DEFAULT_QUERY) {
            return "OK";
        } else {
            Row row = all.get(0);
            return new Date(UUIDs.unixTimestamp(row.getUUID(0))).toString();
        }
    }
}
