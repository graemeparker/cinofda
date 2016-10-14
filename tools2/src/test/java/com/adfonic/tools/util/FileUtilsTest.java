package com.adfonic.tools.util;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileUtilsTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void doGetFileTestxaxis() {
        // Map<String, String> map = new HashMap<String, String>(0);
        // map.put("xaxisMin", "0");
        // map.put("xaxisMax", "50");
        // map.put("xaxisTicketOptionsFormatString", "'%d%'");
        // map.put("xaxisTickInterval", "10");
        // String str = FileUtils.getFile(
        // FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_XAXIS_JS, map);
        // Assert.assertNotNull("String file should not be null", str);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("min: 0") != -1);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("max: 50") != -1);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("formatString: '%d%'") != -1);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("tickInterval: 10") != -1);
    }

    @Test
    public void doGetFileTestyaxis() throws IOException {
        // Map<String, String> map = new HashMap<String, String>(0);
        // map.put("yaxisMin", "1339369200000");
        // map.put("yaxisMax", "1339801200000");
        // map.put("yaxisTicketOptionsFormatString", "'%a'");
        // map.put("yaxisTickInterval", "'1 day'");
        // String str = FileUtils.getFile(
        // FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_YAXIS_JS, map);
        // Assert.assertNotNull("String file should not be null", str);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("min: 1339369200000") != -1);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("max: 1339801200000") != -1);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("formatString: '%a'") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("tickInterval: '1 day'") != -1);
    }

    @Test
    public void doGetFileTestcfgGrid() throws IOException {
        // Map<String, String> map = new HashMap<String, String>(0);
        // map.put("gridDrawGridlines", "false");
        // map.put("gridBackground", "'#ffffff'");
        // map.put("gridBorderWidth", "0");
        // map.put("gridShadow", "false");
        // String str = FileUtils.getFile(
        // FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_GRID_JS, map);
        // Assert.assertNotNull("String file should not be null", str);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("drawGridlines: false") != -1);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("background: '#ffffff'") != -1);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("borderWidth: 0") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("shadow: false") != -1);
    }

    @Test
    public void doGetFileTestcfgHighLighter() throws IOException {
        // Map<String, String> map = new HashMap<String, String>(0);
        // map.put("useAxesFormattersBoolean", "false");
        // map.put("formatDateItemNo", "1'");
        // map.put("markerRendererShadowBoolean", "false'");
        // map.put("formatDateFormat", "'d M yy'");
        // map.put("markerRendererColor", "'#444444'");
        // map.put("markerRendererStyle", "'circle'");
        // map.put("markerRendererLineWidth", "3");
        // map.put("markerRendererSize", "12");
        // map.put("markerRendererAlpha", "1");
        // map.put("markerRendererBgColor", "'#ffffff'");
        //
        // String str = FileUtils
        // .getFile(
        // FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_HIGHLIGHTER_JS,
        // map);
        // Assert.assertNotNull("String file should not be null", str);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("useAxesFormatters = false") != -1);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("itemNo: 1") != -1);
        // Assert.assertTrue("String file should have this partial string",
        // str.indexOf("dateFormat: 'd M yy'") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("shadow: false") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("color: '#444444'") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("style: 'circle'") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("lineWidth: 3") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("size: 12") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("alpha: 1") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("bgColor: '#ffffff'") != -1);
    }

    @Test
    public void doGetFileTestcfgSeriesMultipleFiles() throws IOException {
        // Map<String, String> map = new HashMap<String, String>(0);
        // map.put("lineWidth_0", "4");
        // map.put("shadow_0", "false");
        // map.put("color_0", "'#9acb3c'");
        // map.put("markerOptionsSize_0", "10");
        // map.put("markerOptionsShadowBoolean_0", "false");
        // //
        // String str = FileUtils.getFile(
        // FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_SERIES_JS, map);
        // Assert.assertNotNull("String file should not be null", str);
        //
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("series[0].lineWidth = 4") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("series[0].shadow = false") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("series[0].color = '#9acb3c'") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("size: 10") != -1);
        // Assert.assertTrue("String file should have this partial stringl",
        // str.indexOf("shadow: false") != -1);
        //
    }

}
