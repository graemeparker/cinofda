package com.adfonic.adserver.stoppages;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class StoppagesFromDbTest extends AbstractAdfonicTest{

    @Test(expected = IOException.class)
    public void throwIOExceptionIfThereAreSqlProblems() throws Exception {

        final DataSource datasource = mock(DataSource.class);
        expect(new Expectations() {{
            oneOf (datasource).getConnection(); will(throwException(new IOException()));
        }});
        StoppagesService stoppages = new StoppagesFromDb(datasource);

        assertNotNull(stoppages.getAdvertiserStoppages());
        assertNotNull(stoppages.getCampaignStoppages());

    }

    @Test
    public void throwTimeoutExceptionIfMoreThanOneSecondDelay() throws SQLException {

        final DataSource datasource = mock(DataSource.class);
        expect(new Expectations() {{
            oneOf (datasource).getConnection(); will(new Action() {
                @Override
                public Object invoke(Invocation invocation) throws Throwable {
                    Thread.sleep(1500);
                    return null;
                }

                @Override
                public void describeTo(Description description) {

                }
            });
        }});
        StoppagesService stoppages = new StoppagesFromDb(datasource);


        try {
            assertNotNull(stoppages.getAdvertiserStoppages());
            fail();
        } catch (IOException e) {
            boolean isTimeoutException = (e.getCause().getClass() == TimeoutException.class);
            assertThat(isTimeoutException, is(true));
        }


    }
}
