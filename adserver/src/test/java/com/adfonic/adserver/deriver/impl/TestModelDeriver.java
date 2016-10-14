package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.ddr.DdrService;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.test.AbstractAdfonicTest;

@SuppressWarnings("serial")
public class TestModelDeriver extends AbstractAdfonicTest {
    private DeriverManager deriverManager;
    private DdrService ddrService;
    private ModelDeriver modelDeriver;
    private TargetingContext context;
    private DomainCache domainCache;

    @Before
    public void initTests() {
        deriverManager = new DeriverManager();
        ddrService = mock(DdrService.class);
        modelDeriver = new ModelDeriver(deriverManager, ddrService);
        context = mock(TargetingContext.class, "context");
        domainCache = mock(DomainCache.class);
    }

    @Test
    public void test01_getAttribute_code_coverage_MODEL() {
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(null));
            }
        });
        assertNull(modelDeriver.getAttribute(TargetingContext.MODEL, context));
    }

    @Test
    public void test02_getAttribute_code_coverage_DEVICE_PROPERTIES() {
        expect(new Expectations() {
            {
                oneOf(ddrService).getDdrProperties(context);
                will(returnValue(null));
            }
        });
        Object actual = modelDeriver.getAttribute(TargetingContext.DEVICE_PROPERTIES, context);
        assertEquals(actual, Collections.EMPTY_MAP);
    }

    @Test
    public void test03_getAttribute_code_coverage_DEVICE_IS_ROBOT_CHECKER_OR_SPAM() {
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(null));
            }
        });
        assertFalse((Boolean) modelDeriver.getAttribute(TargetingContext.DEVICE_IS_ROBOT_CHECKER_OR_SPAM, context));
    }

    @Test
    public void test04_getAttribute_code_coverage_invalid_attribute() {
        assertNull(modelDeriver.getAttribute("junk", context));
    }

    @Test
    public void test05_deriveModel_null_props() {
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(null));
            }
        });
        assertNull(modelDeriver.deriveModel(context));
    }

    @Test
    public void test06_deriveModel_no_id() {
        final Map<String, String> props = new HashMap<String, String>();
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(props));
            }
        });
        assertNull(modelDeriver.deriveModel(context));
    }

    @Test
    public void test07_deriveModel_no_modelByExternalId() {
        final String deviceID = randomAlphaNumericString(10);
        final Map<String, String> props = new HashMap<String, String>() {
            {
                put("id", deviceID);
            }
        };
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(props));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getModelByExternalID(deviceID);
                will(returnValue(null));
            }
        });
        assertNull(modelDeriver.deriveModel(context));
    }

    @Test
    public void test08_deriveModel_found() {
        final String deviceID = randomAlphaNumericString(10);
        final Map<String, String> props = new HashMap<String, String>() {
            {
                put("id", deviceID);
            }
        };
        final ModelDto model = mock(ModelDto.class, "model");
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(props));
                allowing(context).getDomainCache();
                will(returnValue(domainCache));
                oneOf(domainCache).getModelByExternalID(deviceID);
                will(returnValue(model));
                allowing(model).getId();
                will(returnValue(randomLong()));
                allowing(model).getExternalID();
                will(returnValue(deviceID));
            }
        });
        assertEquals(model, modelDeriver.deriveModel(context));
    }

    @Test
    public void test09_deriveModelProperties_null_props() {
        expect(new Expectations() {
            {
                oneOf(ddrService).getDdrProperties(context);
                will(returnValue(null));
            }
        });
        Map<String, String> actual = modelDeriver.deriveModelProperties(context);
        assertEquals(actual, Collections.emptyMap());
    }

    @Test
    public void test10_deriveModelProperties_no_displayWidth_or_displayHeight() {
        final Map<String, String> props = new HashMap<String, String>() {
            {
                put("id", randomAlphaNumericString(10)); // needs to be non-empty
            }
        };
        expect(new Expectations() {
            {
                oneOf(ddrService).getDdrProperties(context);
                will(returnValue(props));
            }
        });
        assertEquals(props, modelDeriver.deriveModelProperties(context));
    }

    @Test
    public void test11_deriveModelProperties_usableDisplayStuff_already_there() {
        final String usableDisplayWidth = randomAlphaNumericString(10);
        final String usableDisplayHeight = randomAlphaNumericString(10);
        final Map<String, String> props = new HashMap<String, String>() {
            {
                put("usableDisplayWidth", usableDisplayWidth);
                put("usableDisplayHeight", usableDisplayHeight);
            }
        };
        expect(new Expectations() {
            {
                oneOf(ddrService).getDdrProperties(context);
                will(returnValue(props));
            }
        });
        assertEquals(props, modelDeriver.deriveModelProperties(context));
    }

    @Test
    public void test12_deriveModelProperties_usableDisplayStuff_added() {
        final String displayWidth = randomAlphaNumericString(10);
        final String displayHeight = randomAlphaNumericString(10);
        final Map<String, String> props = new HashMap<String, String>() {
            {
                put("displayWidth", displayWidth);
                put("displayHeight", displayHeight);
            }
        };
        expect(new Expectations() {
            {
                oneOf(ddrService).getDdrProperties(context);
                will(returnValue(props));
            }
        });
        assertEquals(props, modelDeriver.deriveModelProperties(context));
        assertEquals(displayWidth, props.get("usableDisplayWidth"));
        assertEquals(displayHeight, props.get("usableDisplayHeight"));
    }

    @Test
    public void test13_deriveDeviceIsRobotCheckerOrSpam_no_props() {
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(null));
            }
        });
        assertFalse(modelDeriver.deriveDeviceIsRobotCheckerOrSpam(context));
    }

    @Test
    public void test14_deriveDeviceIsRobotCheckerOrSpam_none_true() {
        final Map<String, String> props = new HashMap<String, String>();
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(props));
            }
        });
        assertFalse(modelDeriver.deriveDeviceIsRobotCheckerOrSpam(context));
    }

    @Test
    public void test15_deriveDeviceIsRobotCheckerOrSpam_isRobot() {
        final Map<String, String> props = new HashMap<String, String>() {
            {
                put("isRobot", "1");
            }
        };
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(props));
            }
        });
        assertTrue(modelDeriver.deriveDeviceIsRobotCheckerOrSpam(context));
    }

    @Test
    public void test16_deriveDeviceIsRobotCheckerOrSpam_isChecker() {
        final Map<String, String> props = new HashMap<String, String>() {
            {
                put("isChecker", "1");
            }
        };
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(props));
            }
        });
        assertTrue(modelDeriver.deriveDeviceIsRobotCheckerOrSpam(context));
    }

    @Test
    public void test17_deriveDeviceIsRobotCheckerOrSpam_isSpam() {
        final Map<String, String> props = new HashMap<String, String>() {
            {
                put("isSpam", "1");
            }
        };
        expect(new Expectations() {
            {
                oneOf(context).getAttribute(TargetingContext.DEVICE_PROPERTIES);
                will(returnValue(props));
            }
        });
        assertTrue(modelDeriver.deriveDeviceIsRobotCheckerOrSpam(context));
    }

    @Test
    public void test27_isDevicePropertySet_null() {
        Map<String, String> props = new HashMap<String, String>();
        assertFalse(ModelDeriver.isDevicePropertySet(props, "foo"));
    }

    @Test
    public void test28_isDevicePropertySet_zero() {
        Map<String, String> props = new HashMap<String, String>() {
            {
                put("foo", "0");
            }
        };
        assertFalse(ModelDeriver.isDevicePropertySet(props, "foo"));
    }

    @Test
    public void test29_isDevicePropertySet_nonzero() {
        Map<String, String> props = new HashMap<String, String>() {
            {
                put("foo", "anything other than just 0");
            }
        };
        assertTrue(ModelDeriver.isDevicePropertySet(props, "foo"));
    }

    @Test
    public void test30_isDevicePropertySet_zero() {
        Map<String, String> props = new HashMap<String, String>() {
            {
                put("foo", "false");
            }
        };
        assertFalse(ModelDeriver.isDevicePropertySet(props, "foo"));
    }

    @Test
    public void test31_isDevicePropertySet_zero() {
        Map<String, String> props = new HashMap<String, String>() {
            {
                put("foo", "true");
            }
        };
        assertTrue(ModelDeriver.isDevicePropertySet(props, "foo"));
    }

}