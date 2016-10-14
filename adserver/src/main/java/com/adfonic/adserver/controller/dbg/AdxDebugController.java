package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adserver.rtb.dec.AdXEncUtil;
import com.adfonic.adserver.rtb.mapper.AdXMapper;

@Controller
@RequestMapping(AdxDebugController.URL_CONTEXT)
public class AdxDebugController {

    public static final String URL_CONTEXT = "/adserver/adx";

    @Autowired
    private AdXEncUtil encoder;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public void formView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        printForm(writer);
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    private void printForm(PrintWriter writer) {
        writer.println("Convert from/into AdX protobuf TextFormat");
        writer.println("<form method='POST' action='" + URL_CONTEXT + "' accept-charset='UTF-8'>");
        writer.println("IP Address: <input name='ipAddress' size='40' />");
        writer.println("<br/>");
        writer.println("Device Id: <input name='deviceId' size='40' />");
        writer.println("<input type='submit' value='Convert'/>");
        writer.println("</form>");
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void formPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @RequestParam(name = "ipAddress", required = false) String ipAddress,
            @RequestParam(name = "deviceId", required = false) String deviceId) throws Exception {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        printForm(writer);
        writer.println("<hr/>");
        try {
            if (StringUtils.isNotBlank(ipAddress)) {
                if (ipAddress.length() > 15) {
                    // ...in case of some rubbish
                    writer.println("Wrong IP value: " + ipAddress);
                } else if (ipAddress.indexOf('.') != -1) {
                    String encodedIp = AdXMapper.ipToProtoText(ipAddress);
                    writer.println(ipAddress + " = " + encodedIp);
                } else {
                    String decodedIp = AdXMapper.ipFromProtoText(ipAddress);
                    writer.println(ipAddress + " = " + decodedIp);
                }
            }
            if (StringUtils.isNotBlank(deviceId)) {
                if (deviceId.length() == 36) {
                    UUID uuid = UUID.fromString(deviceId);
                    byte[] bytes = ByteBuffer.wrap(new byte[16]).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
                    String encryptedId = AdXMapper.escapeBytes(encoder.getCrypter().encryptAdvertisingId(bytes));
                    writer.println(deviceId + " = encrypted_advertising_id: " + encryptedId + ", advertising_id: " + AdXMapper.escapeBytes(bytes));
                } else if (deviceId.length() < 60) {
                    // \313\333\371\314m\215M\032\253\203\037\213\306g$\365
                    ByteBuffer bb = ByteBuffer.wrap(AdXMapper.unescapeBytes(deviceId).toByteArray());
                    UUID uuid = new UUID(bb.getLong(), bb.getLong());
                    String rawDeviceId = uuid.toString();
                    writer.println("advertising_id: " + deviceId + " = " + rawDeviceId);
                } else if (/*deviceId.charAt(0) == 'V' && */deviceId.length() > 60) {
                    // V\271\2468\000\016\333\217\n\333r\243\001\f\366\234\307\343\342\222\234\303P\a2E\200\355)\300\323\200+\f\237\337
                    String rawDeviceId = encoder.getCrypter().decryptAdvertisingId(AdXMapper.unescapeBytes(deviceId).toByteArray());
                    writer.println("encrypted_advertising_id: " + deviceId + " = " + rawDeviceId);
                }

            }
        } catch (Exception x) {
            writer.println("<pre>");
            x.printStackTrace(writer);
            writer.println("</pre>");
        }
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

}
