package com.adfonic.servlet;

import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adfonic.domain.Asset;

/**
 * Shows an Asset by using its external ID.
 */
public class ShowAssetServlet extends BaseServlet {
    protected Logger logger = Logger.getLogger(getClass().getName());

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

        Asset asset = null;
        byte[] data = null;

        logger.fine("displaying: " + uuid);

        asset = assetManager.getAssetByExternalId(uuid);

        if (asset == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            // Note: in order to allow the asset to be viewed by the
            // asset approval tool, we do not restrict access to it.
            // Fortunately the asset UUID is suitably difficult to spoof.
            data = asset.getData();
            if (data == null) {
                response.sendError(HttpServletResponse.SC_NO_CONTENT);
            }
            else {
                response.setContentType(asset.getContentType().getMIMEType());
                response.setContentLength(data.length);
                OutputStream out = response.getOutputStream();
                out.write(data);
            }
        }
    }
}
