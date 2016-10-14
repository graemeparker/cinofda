package com.adfonic.presentation.location;

import static org.junit.Assert.fail;

import javax.sql.DataSource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.dto.geotarget.PostalCodeReferenceDto;
import com.adfonic.presentation.location.impl.PostalCodeReferenceDaoImpl;
import com.adfonic.test.AbstractAdfonicTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/adfonic-presentation2-datasource-context.xml" })
@Ignore
public class PostalCodeReferenceDaoImplTest extends AbstractAdfonicTest {

    @Autowired
    ApplicationContext context;

    PostalCodeReferenceDao postalCodeReferenceDao;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    	postalCodeReferenceDao = new PostalCodeReferenceDaoImpl();
        ((PostalCodeReferenceDaoImpl) postalCodeReferenceDao).setDataSource((DataSource) context.getBean("readOnlyDataSource"));
    }

    @After
    public void tearDown() throws Exception {
    	postalCodeReferenceDao = null;
    }

    @Test
    public void testGetLatLonFromPostalCode() {
    	try {
	    	Long countryId = 150L;
	    	String postalCode = "E32AA";
	    	
	    	PostalCodeReferenceDto dto = postalCodeReferenceDao.getLatLonFromPostalCode(countryId, postalCode);
	    	
	    	if(dto == null) {
	    		System.out.println("Null DTO returned");
	    	} else {
	    		System.out.println(dto.getPostalCode() + " - " + dto.getLatitude() + " - " + dto.getLongitude());
	    	}
    	} catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
    	}
    }
}
