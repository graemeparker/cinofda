package com.byyd.adsquare.v2;

import java.util.List;

/**
 * 
 * @author mvanek
 * 
 * Missing or invalid X-AUTH-TOKEN http header
{"timestamp":1453482546617,"status":401,"error":"Unauthorized","message":"Unauthorized","path":"/business-service/amp/v1/auth/refreshToken"}
{"timestamp":1453481978212,"status":500,"error":"Internal Server Error","exception":"javax.ws.rs.NotSupportedException","message":"org.springframework.web.util.NestedServletException: Request processing failed; nested exception is javax.ws.rs.NotSupportedException: Cannot consume content type","path":"/business-service/amp/v1/auth/login"}

 * Wrong field in request
 * 
{ "httpStatus": 400,
    "failures": [ {
        "propertyPath": "username",
        "message": "not a well-formed email address",
        "messageCode": "{org.hibernate.validator.constraints.Email.message}"
    } ]
}
 * Nonexisting username / password
{
  "httpStatus": 400,
  "message": "Bad credentials",
  "messageCode": "login.badcredentials"
}
 * Broken JSON
{
  "httpStatus": 500,
  "message": "Unexpected character ('k' (code 107)): was expecting double-quote to start field name\n at [Source: HttpInputOverHTTP@b01c146; line: 1, column: 4]"
}
 *
 */
public class AmpErrorVector {

    private Integer httpStatus;

    private String message;

    private String messageCode;

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public List<AdsqrApiFailure> getFaliures() {
        return faliures;
    }

    public void setFaliures(List<AdsqrApiFailure> faliures) {
        this.faliures = faliures;
    }

    private List<AdsqrApiFailure> faliures;

    public static class AdsqrApiFailure {

        private String propertyPath;

        private String message;

        private String messageCode;

        public String getPropertyPath() {
            return propertyPath;
        }

        public void setPropertyPath(String propertyPath) {
            this.propertyPath = propertyPath;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessageCode() {
            return messageCode;
        }

        public void setMessageCode(String messageCode) {
            this.messageCode = messageCode;
        }

    }

}
