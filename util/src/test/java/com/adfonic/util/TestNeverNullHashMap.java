package com.adfonic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TestNeverNullHashMap {
    @Test
    public void test1() {
        Map<String, List<String>> nn = new NeverNullHashMap<String, List<String>>(ArrayList.class);
        System.out.println(nn.get("hey"));
        nn.get("hey").add("one");
        nn.get("hey").add("two");
        nn.get("hey").add("three");
        System.out.println(nn.get("hey"));
    }

    @Test
    public void test2() {
        Map<String, List<String>> nn = new NeverNullConcurrentHashMap<String, List<String>>(ArrayList.class);
        System.out.println(nn.get("hey"));
        nn.get("hey").add("one");
        nn.get("hey").add("two");
        nn.get("hey").add("three");
        System.out.println(nn.get("hey"));
    }
}
