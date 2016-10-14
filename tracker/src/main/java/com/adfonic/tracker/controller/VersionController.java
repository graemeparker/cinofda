package com.adfonic.tracker.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VersionController extends AbstractTrackerController {

    @RequestMapping("/v")
    @ResponseBody
    public Map<String,Object> version() {
        Map<String,Object> versions = new HashMap<String, Object>();
        Package[] localPackages = Package.getPackages();
        List<Package> adfonicPackages = new ArrayList<>();
        for (int i = 0; i < localPackages.length; i++) {
            if (localPackages[i].getName().contains("com.adfonic.")) {
                Package packaze = localPackages[i];
                adfonicPackages.add(packaze);
                if(packaze.getImplementationVersion() != null) {
                    versions.put(packaze.getName(), packaze.getImplementationVersion());
                } else {
                    addVersion(versions, packaze);
                }
            }
        }
        return versions;
    }

    private void addVersion(Map<String, Object> versions, Package packaze) {
        Properties prop = new Properties();
        try {
            prop.load(getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
            versions.put(packaze.getName(), prop.getProperty("Implementation-Version"));
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }
}
