package com.adfonic.util;

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;

import org.junit.Test;

public class TestUrlCompressor {
    private static final String[] TEST_URLS = {
            "http://www.google.com",
            "http://www.google.com/foo/bar/whatever/s89asdf8asdf/18JJJ.gif",
            "http://rrmprod.amobee.com/upsteed/actionpage?as=95&t=1311416446242",
            "http://rrmprod.amobee.com/upsteed/actionpage?as=95&t=1311416446242&h=2221907&pl=1&u=a7b81c",
            "http://rrmprod.amobee.com/upsteed/actionpage?as=95&t=1311416446242&h=2221907&pl=1&u=a7b81cb6-20cd-40a8-a5f8-1895af3",
            "http://rrmprod.amobee.com/upsteed/actionpage?as=95&t=1311416446242&h=2221907&pl=1&u=a7b81cb6-20cd-40a8-a5f8-1895af36cbc7&isu=false&i=166.132.164.157",
            "http://rrmprod.amobee.com/upsteed/actionpage?as=95&t=1311416446242&h=2221907&pl=1&u=a7b81cb6-20cd-40a8-a5f8-1895af36cbc7&isu=false&i=166.132.164.157&monitor=1&a=1999715",
            "http://click8.aditic.net/index_external.php?vid=4e2b",
            "http://click8.aditic.net/index_external.php?vid=4e2b1cb02eb09321200613",
            "http://click8.aditic.net/index_external.php?vid=4e2b1cb02eb09321200613&pid=154d70f6182bdda&idte",
            "http://click8.aditic.net/index_external.php?vid=4e2b1cb02eb09321200613&pid=154d70f6182bdda&idte=1&rr=aHR0cDovL2Mudy5ta2",
            "http://click8.aditic.net/index_external.php?vid=4e2b1cb02eb09321200613&pid=154d70f6182bdda&idte=1&rr=aHR0cDovL2Mudy5ta2hvai5jb20vYy5hc20vMy90L2",
            "http://click8.aditic.net/index_external.php?vid=4e2b1cb02eb09321200613&pid=154d70f6182bdda&idte=1&rr=aHR0cDovL2Mudy5ta2hvai5jb20vYy5hc20vMy90L2JocC9wbzkvMS8xYS9jMy91",
            "http://click8.aditic.net/index_external.php?vid=4e2b1cb02eb09321200613&pid=154d70f6182bdda&idte=1&rr=aHR0cDovL2Mudy5ta2hvai5jb20vYy5hc20vMy90L2JocC9wbzkvMS8xYS9jMy91LzAvMC8wL3gvNTg2ODEwODI",
            "http://click8.aditic.net/index_external.php?vid=4e2b1cb02eb09321200613&pid=154d70f6182bdda&idte=1&rr=aHR0cDovL2Mudy5ta2hvai5jb20vYy5hc20vMy90L2JocC9wbzkvMS8xYS9jMy91LzAvMC8wL3gvNTg2ODEwODItMDEzMS0xMDAwLWYzOTgtMD",
            "http://click8.aditic.net/index_external.php?vid=4e2b1cb02eb09321200613&pid=154d70f6182bdda&idte=1&rr=aHR0cDovL2Mudy5ta2hvai5jb20vYy5hc20vMy90L2JocC9wbzkvMS8xYS9jMy91LzAvMC8wL3gvNTg2ODEwODItMDEzMS0xMDAwLWYzOTgtMDAwNDEwMWEwMDAzLzEvODgyZDAxMTA=&rc=MC4wNQ==",
            "@#&*$&*#&*#@(&*#@*(&%&*(#@%&*(@#%&*(@#$&@#^$&*@#^$*&@#^$&*@#$h", "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^", };

    @Test
    public void test() throws Exception {
        for (String url : TEST_URLS) {
            System.out.println(url);

            byte[] compressed = UrlCompressor.compress(url);
            showResults("UrlCompressor.compress", url, compressed);
            String decompressed = UrlCompressor.uncompress(compressed);
            assertEquals(url, decompressed);
        }
    }

    private static void showResults(String method, String str, byte[] compressed) {
        showResults(method, str, compressed.length);
    }

    private static void showResults(String method, String str, int compressedLength) {
        System.out.println(method + ", " + str.length() + " -> " + compressedLength + " (" + (compressedLength < str.length() ? "-" : "+")
                + percentDifference(str.length(), compressedLength) + ")");
    }

    private static String percentDifference(int before, int after) {
        return NumberFormat.getPercentInstance().format((double) Math.abs(before - after) / (double) before);
    }
}
