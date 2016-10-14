package com.adfonic.servlet;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.domain.UploadedContent;
import com.adfonic.domain.UploadedContent_;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

/**
 * Shows an Asset by using its external ID.  Should validate that the
 * currently logged in user.company == asset.creative.campaign.company.
 */
public class ShowUploadedContentServlet extends BaseServlet {
    
    @Autowired
    private CommonManager commonManager;
    

    private static final FetchStrategy UPLOADED_CONTENT_FS = new FetchStrategyBuilder()
        .addInner(UploadedContent_.contentType)
        .build();

    public void handleRequest(HttpServletRequest request,
                              HttpServletResponse response)
        throws javax.servlet.ServletException,
               java.io.IOException
    {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String uuid = pathInfo.substring(1);

        UploadedContent content = null;
        byte[] data = null;

        content = commonManager.getUploadedContentByExternalId(uuid, UPLOADED_CONTENT_FS);

        if (content == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Note: in order to allow the content to be viewed by the
        // admin tool, we do not restrict access to it.
        // Fortunately the UUID is suitably difficult to spoof.

        data = content.getData();
        if (data == null) {
            response.sendError(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        response.setContentType(content.getContentType().getMIMEType());
        response.setContentLength(data.length);
        OutputStream out = response.getOutputStream();
        out.write(data);
    }
}
