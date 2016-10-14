package com.adfonic.adserver.controller.commands;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import com.adfonic.adserver.controller.AdController;
import com.adfonic.adserver.view.JsonAdView;

public class AdRequestCommandDirect extends RequestCommandBase implements AdRequestCommand {

    @Autowired
    protected AdController adController;

    @Override
    public String executeGetAdCommand(String adSpaceExternalId, Map<String, Object> queryMap) throws Exception {

        createDiagnosticUrl(adSpaceExternalId, queryMap);

        System.out.println("AdRequestCommandDirect,adController=" + adController);
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        if (queryMap != null) {
            httpServletRequest.addParameters(queryMap);
        }

        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        ModelMap modelMap = new ModelMap();

        httpServletRequest.setMethod("GET");
        httpServletRequest.setRequestURI("/ad/" + adSpaceExternalId);

        //final ModelAndView mav = handle(httpServletRequest, httpServletResponse);
        //System.out.println("mav = "+mav);
        adController.handleRequest(httpServletRequest, httpServletResponse, modelMap, adSpaceExternalId);
        JsonAdView jsonAdView = new JsonAdView();
        jsonAdView.render(modelMap, httpServletRequest, httpServletResponse);

        String returnResponse = httpServletResponse.getContentAsString();
        //System.out.println("Response from renderee = "+returnResponse);
        for (String oneHeader : httpServletResponse.getHeaderNames()) {
            System.out.println(oneHeader + "=" + httpServletResponse.getHeader(oneHeader));
        }
        //System.out.println("httpServletResponse.getStatus()="+httpServletResponse.getStatus());
        //System.out.println("httpServletResponse.getErrorMessage()="+httpServletResponse.getErrorMessage());

        if (httpServletResponse.getStatus() != 200) {
            //If http error is returned convert it into Json response so that it will be easier to test
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", "httperror");
            jsonObject.put("errornumber", httpServletResponse.getStatus());
            jsonObject.put("errormsg", httpServletResponse.getErrorMessage());
            returnResponse = jsonObject.toString();
        }
        return returnResponse;
    }

}
