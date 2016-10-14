package com.adfonic.adserver.controller.commands;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.controller.ClickThroughController;

public class ClickThroughRequestCommandDirect extends RequestCommandBase implements ClickThroughRequestCommand {

    @Autowired
    protected ClickThroughController clickThroughController;

    @Override
    public String executeClickThroughCommand(String adSpaceExternalId, String impressionExternalId, Map<String, Object> queryMap) throws Exception {
        System.out.println("ClickThroughRequestCommandDirect,clickThroughController=" + clickThroughController);
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        if (queryMap != null) {
            httpServletRequest.addParameters(queryMap);
        }

        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        for (String oneHeader : httpServletResponse.getHeaderNames()) {
            System.out.println("Header : " + oneHeader + "=" + httpServletResponse.getHeader(oneHeader));
        }
        httpServletRequest.setMethod("GET");
        httpServletRequest.setRequestURI("/ct/" + adSpaceExternalId + "/" + impressionExternalId);

        //final ModelAndView mav = handle(httpServletRequest, httpServletResponse);
        //System.out.println("mav = "+mav);
        clickThroughController.handleClickThroughRequest(httpServletRequest, httpServletResponse, adSpaceExternalId, impressionExternalId, null);
        String redirectUrl = httpServletResponse.getRedirectedUrl();

        //System.out.println("redirectUrl=" + redirectUrl);

        if (httpServletResponse.getStatus() != 200) {
            //If http error is returned convert it into Json response so that it will be easier to test
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", "httperror");
            jsonObject.put("errornumber", httpServletResponse.getStatus());
            jsonObject.put("errormsg", httpServletResponse.getErrorMessage());
            //System.out.println("status : httperror");
            //System.out.println("errornumber : "+ httpServletResponse.getStatus());
            //System.out.println("errormsg : "+ httpServletResponse.getErrorMessage());
            redirectUrl = jsonObject.toString();
        }
        return redirectUrl;
    }

}
