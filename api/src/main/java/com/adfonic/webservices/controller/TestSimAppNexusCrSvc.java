package com.adfonic.webservices.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class TestSimAppNexusCrSvc extends AbstractAdfonicWebService{
    private static final transient Logger LOG = Logger.getLogger(TestSimAppNexusCrSvc.class.getName());

    //wire it may be
    private String memberId="696";
    
    private static final AtomicInteger creativeIdSeq = new AtomicInteger(1000);
    
    private static final ConcurrentMap<String, String> creativeStatusMap=new ConcurrentHashMap<String, String>();
    private static final ConcurrentMap<String, String> creativeAdmMap=new ConcurrentHashMap<String, String>();

    
    @RequestMapping(value = "/creative.sim/{memberId}", method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public String create(@RequestBody String request, @PathVariable String memberId) throws JsonProcessingException, IOException{
        LOG.info("Request: "+request);
        verifyMember(memberId);
        String creativeId=""+creativeIdSeq.incrementAndGet();
        creativeStatusMap.put(creativeId, "pending");
        creativeAdmMap.put(creativeId, getAdm(request));
        return response(creativeId);
    }


    @RequestMapping(value = "/creative.sim/{memberId}/{creativeId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String get(@PathVariable String memberId, @PathVariable String creativeId){
        LOG.info("creative id: "+creativeId+ "\nCreative id: "+creativeId);
        verifyMember(memberId);
        if(!creativeStatusMap.containsKey(creativeId)){
            throw new RuntimeException("bad get; no such key");
        }
        
        return response(creativeId);
    }
    
    @RequestMapping(value = "/creative.sim/{memberId}/{creativeId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String update(@RequestBody String request, @PathVariable String memberId, @PathVariable String creativeId) throws JsonProcessingException, IOException{
        LOG.info("creative id: "+creativeId+ "\nRequest: "+request);
        verifyMember(memberId);
        if(!creativeStatusMap.containsKey(creativeId)){
            throw new RuntimeException("bad get; no such key");
        }

        String existingStuff=response(creativeId);
        creativeStatusMap.put(creativeId, "pending");
        creativeAdmMap.put(creativeId, getAdm(request));
        return existingStuff;
    }

    final ObjectMapper objMapper=new ObjectMapper();
    
    private String getAdm(String jsonReq) throws JsonProcessingException, IOException{
        JsonNode rootNode=objMapper.readTree(jsonReq);
        JsonNode creativeNode=rootNode.get("creative");
        return creativeNode.get("content").getValueAsText();
    }

    @RequestMapping(value = "/changestat.sim/{creativeId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String status(@PathVariable String creativeId, @RequestParam(required=true) String status){
        if(!creativeStatusMap.containsKey(creativeId)){
            throw new RuntimeException("bad get; no such key");
        }

        return response(creativeId, creativeStatusMap.put(creativeId, status));
    }

    @RequestMapping(value = "/auditdisplay.sim/{creativeId}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String auditDisplay(@PathVariable String creativeId){
        String adm=creativeAdmMap.get(creativeId);
        
        if(adm==null){
            throw new RuntimeException("bad get; no such key");
        }

        return adm;
    }

    private void verifyMember(String member){
        if(!memberId.equals(member)){
            throw new RuntimeException("member id "+member+" not supported!");
        }
    }

    private String response(String creativeRef, String status){
        return "{ \"response\": { \"id\": " + creativeRef + ", \"status\": \"OK\", \"creative\": { \"audit_status\": \"" + status + "\", \"id\": " + creativeRef + " }}}";
    }

    private String response(String creativeRef){
        return response(creativeRef, creativeStatusMap.get(creativeRef));
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.OK)
    public void handleAllExceptions(Throwable e, HttpServletRequest request, Writer writer, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");
        writer.write("{\"exception\": \"" + e.getMessage() + "\"}");// empty response indicates no bid
    }

}
