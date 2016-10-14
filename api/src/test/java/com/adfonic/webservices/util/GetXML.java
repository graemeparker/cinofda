package com.adfonic.webservices.util;

import java.io.IOException;
import java.io.InputStream;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;

import com.adfonic.webservices.util.WSFixture.Format;

public class GetXML extends AbstractGetHandler implements Get {

    public GetXML(WSFixture fixture) {
        super(fixture);
    }


    @Override
    protected Format getFormat() {
        return (Format.XML);
    }


    @Override
    protected <T> T parseResponseEntity(InputStream entity, Class<T> clazz) throws IOException {
        Document doc;
        try {
            doc = new Builder().build(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);// TODO - make it IOException
        }
        // remove empty elements - they cause XMLSer to create empty arrays[] and try to assign
        System.out.println("Response.Message.Raw: " + doc.toXML());
        recDelEmpty(doc.getRootElement());
        String xmlStr = doc.toXML();
        XMLSerializer xmlSer = new XMLSerializer();

        JSON json = (JSON) xmlSer.read(xmlStr);

        String xmlMasgContent;

        if (json.isArray()) {
            JSONArray jsonArr = (JSONArray) json;

            if (clazz.isArray()) {
                xmlMasgContent = jsonArr.toString();
            } else {
                // TODO - see if possible to make the above check together with this
                xmlMasgContent = (jsonArr.isEmpty()) ? "{}" : jsonArr.get(0).toString();
            }
        } else {// like masg-error
            JSONObject jsonObj = (JSONObject) json;
            xmlMasgContent = jsonObj.toString();
        }

        System.out.println("Response.Message.XML.Converted: " + xmlMasgContent);
        // return(gson.fromJson(jsonArr.get(0).toString(), clazz));
        return (gson.fromJson(xmlMasgContent, clazz));
    }


    private void recDelEmpty(Node node) {
        if ((node.getChildCount() == 0) && ("".equals(node.getValue()))) {
            node.getParent().removeChild(node);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            recDelEmpty(node.getChild(i));
        }
    }

}
