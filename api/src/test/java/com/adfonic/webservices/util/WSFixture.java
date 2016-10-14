package com.adfonic.webservices.util;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class WSFixture {

    public enum Format {
        JSON, XML, ABSENT(null);
        private final String value;


        private Format() {
            value = toString().toLowerCase().intern();
        }


        private Format(String value) {
            this.value = value;
        }


        public String format() {
            return (value);
        }
    }

    public static final int CODE_INPUT_VALIDN=403; 
    public static final int CODE_INPUT_MISSIN=403; 
    public static final int CODE_VALIDN=403; 
    public static final int CODE_FORBDN=403; 
    public final static String respRootElm = "masg-response";
    public final static String errRootElm = "masg-error";

    private final static String UNQPFX = "Cust";

    // public static String F_HOST = "api.test.byyd.net";
    public static String F_HOST = "localhost";
    // public static int F_PORT = 8092;
    public static int F_PORT = 8080;
    // public static int F_PORT = 80;
    // public static String F_PFX = "/adfonic-webservices-1.0";
    public static String F_PFX = "/adfonic-webservices";
    // public static String F_PFX = "";

    private static long counter = 0;


    private String getUnique() {
        return (Long.toString(++counter));
    }

    private String pubPfx = UNQPFX + "Pub";


    public String getNxtPubName() {
        return (pubPfx + getUnique());
    }

    protected String hostName = F_HOST;
    protected String hostPort = Integer.toString(F_PORT);
    protected String prefix = F_PFX;

    public String userEmail = "adtstr+002@gmail.com";
    public String userDeveloperKey = "devkey_adtstr002";
    // public String userEmail = "anish.chandran@adfonic.com";
    // public String userDeveloperKey = "devkey";
    
    private WSclient ws;
    
    private void setWSclient(){
        Credentials cred=new Credentials(userEmail, userDeveloperKey);
        ws=new NwClient(hostName, hostPort, prefix, cred);
        //ws=new InProcessClient(cred);
    }

    /* Fixture - The values specified here can be deemed as existing. all public for obvious reasons */
    public String publication_1_id = "57adf62b-b05f-405c-aba2-70846cb447bc";
    // public String publication_1_id="851ac861-8b95-4904-a7eb-bf5893c1de36";
    public String publication_1_name = "PubCo_AndroidApp1";
    public int initial_pub_count = 1;

    //TODO - to recreate
    public String publication_pa_id="38bd7e7a-8356-42ec-91f8-07f9606836b9";

    public static final String statisticsUserEmail = "adtstr+005@gmail.com";
    public static final String statisticsUserDeveloperKey = "devkey_adtstr005";
    public String statsPub_1_id = "e4f5eb50-7798-4aca-a89e-691d051fa181";
    public String statsPub_1_statsStart="2012010300";
    public String statsPub_1_statsEnd="2012010316";
    
    Map<String, String> globalParamMap = new HashMap<String, String>();
    {
        globalParamMap.put("start", "2012010300");
        globalParamMap.put("end", "2012010316");
    }

    MultiGet noVerifyGet;
    List<AbstractGetHandler> getters = new ArrayList<AbstractGetHandler>();


    public WSFixture relaxComparisons() {
        noVerifyGet.setComparisonFlag(false);
        return (this);
    }


    public WSFixture specifyCandidateFormat(Format format) {
        noVerifyGet.specifyCandidateIndex(Format.XML.equals(format) ? 2 : 1);// default to json; initComponentGetters order
        return (this);
    }


    public WSFixture() {
        noVerifyGet = new MultiGet(this, initComponentGetters());
        setWSclient();
    }


    public WSFixture(String email, String devKey) {
        this.userEmail = email;
        this.userDeveloperKey = devKey;
        noVerifyGet = new MultiGet(this, initComponentGetters());
        setWSclient();
    }


    private AbstractGetHandler[] initComponentGetters() {
        // This add order is important for now; otherwise had to loop Format.values() and use reflection
        getters.add(new GetJSON(this));
        getters.add(new GetXML(this));
        return (getters.toArray(new AbstractGetHandler[0]));
    }


    public Get getTer(Format format) {
        return (getters.get(format.ordinal()));
    }


    /*
     * Call to this might be implied This method is the place to insert any backend data on top of the baseline so that the above fixture is correct
     */
    public void init() {

    }


    public Map<?, ?> getResponseMap(String response) throws JsonParseException, JsonMappingException, IOException {
        return (new ObjectMapper().readValue(response, Map.class));
    }


    public String buildUrl(String serviceUrl, Format format) throws Exception {
        String fmt = format.format();
        StringBuilder buffer = new StringBuilder();

        buffer//.append("http://")
                //.append(hostName).append(":")
                //.append(hostPort).append(prefix)
                .append(serviceUrl).append(fmt != null ? "." + fmt : "");

        return buffer.toString();
    }


    private String postForm(String url, Form form) throws Exception {
        System.out.println("Posting to URL:" + url);
        System.out.println("Form:\n" + form.getEntries());
        String response = ws.postForm(url, form);
        System.out.println("Response:\n" + response);
        return response;
    }


    public String postForm(String url, String user, String password, Form form) throws Exception {
        System.out.println("Posting to URL:" + url);
        System.out.println("Form:\n" + form.getEntries());
        String response = ws.postForm(url, user, password, form);
        System.out.println("Response:\n" + response);
        return response;
    }


    public String postBodyFmtSpecd(String path, String requestBody, Format fSnd, Format fRcv, int expectedStatus) throws Exception {
        String url = buildUrl(path, fRcv);
        return ws.post(url, requestBody, fSnd, fRcv, expectedStatus);
    }


    public String postBodyFmtSpecd(String path, String requestBody, Format fSnd, Format fRcv) throws Exception {
        return postBodyFmtSpecd(path, requestBody, fSnd, fRcv, 0);
    }

    
    public String putBodyFmtSpecd(String path, String requestBody, Format fSnd, Format fRcv, int expectedStatus) throws Exception {
        String url = buildUrl(path, fRcv);
        return ws.put(url, requestBody, fSnd, fRcv, expectedStatus);
    }


    private String get(String url) throws Exception {
        return get(url, userEmail, userDeveloperKey);
    }


    public String get(String url, String user, String password) throws Exception {
        System.out.println("Getting URL: " + url);
        String response = ws.get(url, user, password);
        System.out.println("Response:\n" + response);
        return response;
    }


    public com.adfonic.webservices.util.Response getResponse(String url, String user, String password) throws Exception {
        return ws.getResponse(url, user, password);
    }


    public String get(String serviceUrl, Format format, String user, String password) throws Exception {
        return get(buildUrl(serviceUrl, format), user, password);
    }


    public String getJSON(String path) throws Exception {
        return (get(buildUrl(path, Format.JSON)));
    }


    public String getXML(String path) throws Exception {
        return (get(buildUrl(path, Format.XML)));
    }


    public <T> T fGet(Class<T> clazz, String path, String... params) throws Exception {// TODO - handle in
        return (noVerifyGet.get(clazz, path, params));
    }


    private String getQueryString(String... params) {
        String sep = "?";
        String qStr = "";
        for (String p : params) {
            if (p.indexOf('=') == -1) {
                String pv = globalParamMap.get(p);
                if (pv != null) {
                    p += "=" + pv;
                }
            }
            qStr += sep + p;
            sep = "&";
        }
        return (qStr);
    }


    public String getJSON(String path, String... params) throws Exception {
        return get(buildUrl(path, Format.JSON) + getQueryString(params));
    }


    public String postURLENCforJSON(String path, Form form) throws Exception {
        return postForm(buildUrl(path, Format.JSON), form);
    }


    public String postJSONBodyForXML(String path, String requestBody, int expectedStatus) throws Exception {
        return postBodyFmtSpecd(path, requestBody, Format.JSON, Format.XML, expectedStatus);
    }


    public String postXMLBodyForXML(String path, String requestBody, int expectedStatus) throws Exception {
        return postBodyFmtSpecd(path, requestBody, Format.XML, Format.XML, expectedStatus);
    }


    public String postXMLBodyForXML(String path, String requestBody) throws Exception {
        return postXMLBodyForXML(path, requestBody, 0);
    }


    public String postXMLBodyForJSON(String path, String requestBody, int expectedStatus) throws Exception {
        return postBodyFmtSpecd(path, requestBody, Format.XML, Format.JSON, expectedStatus);
    }


    public String postXMLBodyForJSON(String path, String requestBody) throws Exception {
        return postXMLBodyForJSON(path, requestBody, 0);
    }


    public String postJSONBodyForJSON(String path, String requestBody, int expectedStatus) throws Exception {
        return postBodyFmtSpecd(path, requestBody, Format.JSON, Format.JSON, expectedStatus);
    }


    public String putXMLBodyForXML(String path, String requestBody, int expectedStatus) throws Exception {
        return (putBodyFmtSpecd(path, requestBody, Format.XML, Format.XML, expectedStatus));
    }


    public void delete(String path, Format format, int expectedStatus) throws Exception {
        String url = buildUrl(path, format);
        ws.delete(url, format, expectedStatus);
    }

    private Map<String, String> pubs = new HashMap<String, String>();
    private Set<String> managedOnlypubs = new HashSet<String>();


    private String createRawPublication() throws Exception {
        Form publication = new Form();
        String name = getNxtPubName();
        publication.set("name", name);
        publication.set("type", "MOBILE_SITE");
        publication.set("description", "descr for " + name);
        publication.set("reference", "");
        publication.set("url", "http://domain:90/path?query_string#fragment_id");
        publication.set("transparent", "true");
        publication.set("languages", "es");
        publication.set("requests", "1");
        publication.set("uniques", "1");
        publication.set("autoapprove", "false");
        String url = buildUrl("/publication/create", Format.JSON);
        String response = postForm(url, publication);
        return (response);
    }


    public String createAPublication() throws Exception {
        String response = createRawPublication();
        Map<?, ?> rm = getResponseMap(response);
        String extPubId = (String) ((Map<?, ?>) rm.get(respRootElm)).get("id");
        pubs.put(extPubId, response);
        return (extPubId);
    }


    public String getAPublication() throws Exception {
        if (pubs.isEmpty()) {
            String newPub = createAPublication();
            return (newPub);
        } else {
            List<String> pubsList = new ArrayList<String>(pubs.keySet());
            return (pubsList.get(new Random().nextInt(pubsList.size())));
        }
    }


    public void addToManagedPublications(String externalId) {
        managedOnlypubs.add(externalId);
    }


    public String getPublication(String externalId) {
        return (pubs.get(externalId));
    }


    /*
     * Destroy all created values in the fixture in the backend This method should destroy all values created in the backend by this fixture and leave the baseline as it was
     */
    public void destroy() {
        // for each item in pubs destroy
        // clear(pubs) in the backend
        // clear(managedOnlypubs) in the backend
    }


    public static WSFixture getStatisticUsersFixture() {
        WSFixture f = new WSFixture(statisticsUserEmail, statisticsUserDeveloperKey);
        // Get around for now
        f.relaxComparisons().specifyCandidateFormat(Format.XML);
        return (f);
    }

    public void verifyBasicContentInXMLResonse(String xmlResponse, String name, Object value){
        //TODO - for now just very simple verification
        if(!xmlResponse.contains(value.toString())){
            fail();
        }
    }

    public void verifyBasicContentInJSONResonse(String jsonResponse, String name, Object value){
        //TODO - for now just very simple verification
        if(!jsonResponse.contains(value.toString())){
            fail();
        }
    }

}
