package com.byyd.celtra;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;

import com.adfonic.http.ApiClient;
import com.adfonic.http.DefaultHttpErrorCallback;
import com.adfonic.http.JacksonHttpExecutionCallback;
import com.byyd.celtra.CeltraAnalyticsRequest.Dimension;
import com.byyd.celtra.CeltraAnalyticsRequest.Direction;
import com.byyd.celtra.CeltraAnalyticsRequest.Filter;
import com.byyd.celtra.CeltraAnalyticsRequest.Metric;
import com.byyd.celtra.CeltraAnalyticsRequest.Operator;
import com.byyd.celtra.CeltraAnalyticsRequest.SortValue;
import com.byyd.celtra.CeltraAnalyticsRequest.Spec;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * https://hub.celtra.com/api/analytics
 *
 */
public class CeltraAnalyticsClient implements Closeable {

    static final ObjectMapper jackson = new ObjectMapper();
    {
        jackson.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Do NOT write null fields
        jackson.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jackson.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        jackson.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);
        jackson.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        jackson.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); // 2016-02-05 17:00:00
        /*
        SimpleModule mod = new SimpleModule();
        mod.addSerializer(Filter.class, new FilterSerializer());
        jackson.registerModule(mod);
        */
    }
    // Create Jackson readers/writers AFTER Jackson's ObjectMapper was configured
    private final ObjectWriter requestWriter = jackson.writerFor(CeltraAnalyticsRequest.class);

    private final String appId;

    private final String secretKey;

    private final Header authHeader;

    private final ApiClient apiClient;

    private final CeltraErrorCallback errorCallback = new CeltraErrorCallback();
    private final JacksonHttpExecutionCallback<CeltraAnalyticsResponse, CeltraApiException> reportExecCallback = new JacksonHttpExecutionCallback<CeltraAnalyticsResponse, CeltraApiException>(
            errorCallback, ApiClient.APPLICATION_JSON, HttpURLConnection.HTTP_OK, jackson.readerFor(CeltraAnalyticsResponse.class));

    private final JacksonHttpExecutionCallback<CeltraSubmitJobResponse, CeltraApiException> submitExecCallback = new JacksonHttpExecutionCallback<CeltraSubmitJobResponse, CeltraApiException>(
            errorCallback, ApiClient.APPLICATION_JSON, HttpURLConnection.HTTP_CREATED, jackson.readerFor(CeltraSubmitJobResponse.class));

    private final JacksonHttpExecutionCallback<CeltraCheckJobResponse, CeltraApiException> checkExecCallback = new JacksonHttpExecutionCallback<CeltraCheckJobResponse, CeltraApiException>(
            errorCallback, ApiClient.APPLICATION_JSON, HttpURLConnection.HTTP_OK, jackson.readerFor(CeltraCheckJobResponse.class));

    public CeltraAnalyticsClient(ApiClient apiClient, String appId, String secretKey) {
        Objects.requireNonNull(apiClient);
        this.apiClient = apiClient;
        Objects.requireNonNull(appId);
        this.appId = appId;
        Objects.requireNonNull(secretKey);
        this.secretKey = secretKey;
        String authValue = "Basic " + Base64.encodeBase64String((appId + ":" + secretKey).getBytes(Charset.forName("UTF-8")));
        this.authHeader = new BasicHeader("Authorization", authValue);
    }

    @Override
    public void close() {
        apiClient.close();
    }

    public void reset() {
        apiClient.reset();
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * You can provide the necessary information appended to the URL as query strings, or as a JSON payload.
     * 
     * Be sure to provide a format for the returned report. You can enter csv or json . HTML reports are not
     * supported by the asynchronous API.
     */
    public CeltraSubmitJobResponse submit(CeltraAnalyticsRequest request) {
        StringBuilder sb = new StringBuilder("/api/analytics/jobs");
        addParams(request, sb);
        HttpPost httpPost = new HttpPost(sb.toString());
        return apiClient.execute(httpPost, submitExecCallback, errorCallback);
    }

    /**
     * To check the status of the job, submit an HTTP GET request with the job ID appended
     */
    public CeltraCheckJobResponse check(String jobId) {
        HttpGet httpGet = new HttpGet("/api/analytics/jobs/" + jobId);
        return apiClient.execute(httpGet, checkExecCallback, errorCallback);
    }

    /**
     * Once the job status has changed to finished , submit a get request to the following URL to retrieve the report
     */
    public CeltraAnalyticsResponse retrieve(String jobId) {
        HttpGet httpGet = new HttpGet("/api/analytics/jobs/" + jobId + "/report");
        return apiClient.execute(httpGet, reportExecCallback, errorCallback);
    }

    public CeltraAnalyticsResponse execute(CeltraAnalyticsRequest request) {
        StringBuilder sb = new StringBuilder("/api/analytics");
        addParams(request, sb);
        HttpGet httpGet = new HttpGet(sb.toString());
        httpGet.setHeader(authHeader);
        return apiClient.execute(httpGet, reportExecCallback, errorCallback);
    }

    private void addParams(CeltraAnalyticsRequest request, StringBuilder sb) {
        Spec spec = request.getSpec();
        sb.append("?metrics=");
        for (Metric metric : spec.getMetrics()) {
            sb.append(metric).append(',');
        }
        sb.deleteCharAt(sb.length() - 1); // remove last comma

        sb.append("&dimensions=");
        for (Dimension dimension : spec.getDimensions()) {
            sb.append(dimension).append(',');
        }
        sb.deleteCharAt(sb.length() - 1); // remove last comma

        for (Filter filter : spec.getFilters()) {
            // TODO urlescaping values ? 
            sb.append("&filters.").append(filter.getField());

            List<String> operands = filter.getOperand();
            int opcnt = operands.size();
            Operator operator = filter.getOperator();
            // if operator not present, it default to in
            if ((operator == null || operator != Operator.in) && opcnt > 1) {
                throw new IllegalArgumentException("Filter for " + filter.getField() + " with operator " + operator + " cannot have " + opcnt + " operands");
            }
            if (operator != null) {
                sb.append('.').append(filter.getOperator());
            }
            sb.append('=');
            if (opcnt == 0) {
                // nothing XXX is is legal ???
            } else if (operands.size() == 1) {
                sb.append(operands.get(0));
            } else {
                sb.append(operands);
            }

        }

        List<SortValue> sorts = spec.getSorts();
        if (sorts != null) {
            sb.append("&sort=");
            for (SortValue sort : sorts) {
                if (sort.getDirection() == Direction.desc) {
                    sb.append('-');
                }
                sb.append(sort.getField()).append(',');
            }
            sb.deleteCharAt(sb.length() - 1); // remove last comma
        }
        Integer limit = spec.getLimit();
        if (limit != null) {
            sb.append("&limit=").append(limit);
        }
    }

    static class CeltraSubmitJobResponse {

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

    static class CeltraCheckJobResponse {

        private String id;

        private Spec spec;

        private String state;

        private String error;

        private Date creationTimestamp;

        private Date finishTimestamp;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Spec getSpec() {
            return spec;
        }

        public void setSpec(Spec spec) {
            this.spec = spec;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Date getCreationTimestamp() {
            return creationTimestamp;
        }

        public void setCreationTimestamp(Date creationTimestamp) {
            this.creationTimestamp = creationTimestamp;
        }

        public Date getFinishTimestamp() {
            return finishTimestamp;
        }

        public void setFinishTimestamp(Date finishTimestamp) {
            this.finishTimestamp = finishTimestamp;
        }

    }

    static class CeltraErrorCallback extends DefaultHttpErrorCallback<CeltraApiException> {

        @Override
        public CeltraApiException newException(String message) {
            return new CeltraApiException(message);
        }

        @Override
        public CeltraApiException newException(String message, Exception x) {
            return new CeltraApiException(message);
        }

    }
}
