package com.adfonic.tools.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.adfonic.tools.beans.util.Constants;

public class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    public enum FileUtilsEnum {

        DASHBOARD_TABLE_CFG_XAXIS_JS(Constants.DASHBOARD_TABLE_CFG_XAXIS_JS, false), DASHBOARD_TABLE_CFG_YAXIS_JS(
                Constants.DASHBOARD_TABLE_CFG_YAXIS_JS, false), DASHBOARD_TABLE_CFG_HIGHLIGHTER_JS(
                        Constants.DASHBOARD_TABLE_CFG_HIGHLIGHTER_JS, false), DASHBOARD_TABLE_CFG_GRID_JS(Constants.DASHBOARD_TABLE_CFG_GRID_JS,
                false), DASHBOARD_TABLE_CFG_SERIES_JS(Constants.DASHBOARD_TABLE_CFG_SERIES_JS, true);

        private FileUtilsEnum(String path, boolean multiple) {
            this.multiple = multiple;
            this.path = path;
        }

        private String path;
        private boolean multiple;

        public String getPath() {
            return path;
        }

        public boolean isMultiple() {
            return multiple;
        }

    }

    public static String getFile(FileUtilsEnum fileEnum, Map<String, String> map) {
        String theString = "";
        if (fileEnum.isMultiple()) {
            // at least with multiple files there should be the one with the 0
            boolean continueLooping = true;
            int k = 0;
            while (continueLooping) {
                Resource resourceLoad = new ClassPathResource(fileEnum.getPath() + "[" + k + "].js");
                InputStream inputStream;
                try {
                    inputStream = resourceLoad.getInputStream();
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(inputStream, writer, "UTF-8");
                    theString = theString + "\n" + writer.toString();
                    k++;
                } catch (IOException e) {
                    // LOGGER.info("Error finding resource - " +
                    // fileEnum.getPath());
                    // end looping
                    continueLooping = false;
                }
            }

        } else {
            Resource resourceLoad = new ClassPathResource(fileEnum.getPath());
            InputStream inputStream;
            try {
                inputStream = resourceLoad.getInputStream();
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, "UTF-8");
                theString = writer.toString();
            } catch (IOException e) {
                LOGGER.info("Error finding resource - " + fileEnum.getPath());
            }
        }

        StringTemplate st = new StringTemplate(theString);
        st.setAttributes(map);
        theString = st.toString();

        return theString;
    }

}
