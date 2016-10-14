package com.adfonic.tasks.combined.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping
public class IndexController {
    /*
        @Autowired
        @Qualifier("domainSerializerProperties")
        private Properties properties;

        public static final FastDateFormat FDF = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSSZ");

        @RequestMapping(value = "properties", method = RequestMethod.GET)
        public void properties(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
            properties.store(httpResponse.getWriter(), null);
        }
    */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public void index(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("text/html");
        PrintWriter writer = httpResponse.getWriter();
        writer.println("<html>");
        writer.println("<ul>");
        writer.println("<li><a href='vui#!/xaudit'>Creative Audit</a></li>");
        writer.println("<li><a href='status'>Status</a></li>");
        writer.println("</ul>");
        writer.println("</html>");
    }

}
