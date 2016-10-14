package com.adfonic.tasks;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import com.adfonic.tasks.combined.BlacklistPublicationsTask;

public class BlacklistPublicationTaskTest {

    @Test
    public void testHostAndDomain() {
        BlacklistPublicationsTask task = build("drama.net");

        Assertions.assertThat(task.isBlacklisted(0, "drama.net")).isTrue();
        Assertions.assertThat(task.isBlacklisted(0, "www.drama.net")).isTrue();
        Assertions.assertThat(task.isBlacklisted(0, "even.more.drama.net")).isTrue(); // depth of domain nesting is not limited...
        Assertions.assertThat(task.isBlacklisted(0, "http://www.drama.net/path?param=value")).isTrue();
        // verify that no subsequences are matched in domain or tld
        Assertions.assertThat(task.isBlacklisted(0, "gooddrama.net")).isFalse();
        Assertions.assertThat(task.isBlacklisted(0, "drama.ne")).isFalse();
    }

    @Test
    public void testTopLevelDomain() {
        BlacklistPublicationsTask task = build("mangatown.co");
        // verify that no subsequences are matched in domain or tld
        Assertions.assertThat(task.isBlacklisted(0, "mangatown.com")).isFalse();
        Assertions.assertThat(task.isBlacklisted(0, "www.mangatown.com")).isFalse();

        Assertions.assertThat(task.isBlacklisted(0, "http://mangatown.co/anime")).isTrue();
        Assertions.assertThat(task.isBlacklisted(0, "https://www.mangatown.co/yoshimi")).isTrue();
    }

    private BlacklistPublicationsTask build(String... blacklist) {
        List<String> list = Arrays.asList(blacklist);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        return new BlacklistPublicationsTask(jdbcTemplate, list);
    }
}
