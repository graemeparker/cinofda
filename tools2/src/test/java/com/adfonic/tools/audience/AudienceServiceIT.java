package com.adfonic.tools.audience;

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.audience.DMPAttributeDto;
import com.adfonic.dto.audience.DMPSelectorDto;
import com.adfonic.dto.audience.DMPVendorDto;
import com.adfonic.dto.audience.FirstPartyAudienceDeviceIdsUploadHistoryDto;
import com.adfonic.dto.audience.FirstPartyAudienceDto;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.dto.resultwrapper.DeviceIdsValidated;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.presentation.audience.sort.FirstPartyAudienceDeviceIdsUploadHistorySortBy;
import com.adfonic.presentation.deviceidentifier.DeviceIdentifierService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/adfonic-tools2-context.xml" })
public class AudienceServiceIT {

    @Autowired
    private AudienceService audienceService;
    @Autowired
    private DeviceIdentifierService deviceIdentifierService;

    @Test
    public void testAudienceService() {

        DMPVendorDto vendor = audienceService.getDMPVendorByName("DMP VENDOR 1", false);
        assertNotNull(vendor);

        System.out.println(vendor.getName());

        List<DMPAttributeDto> attributes = audienceService.getDMPAttributesForDMPVendor(vendor, false);
        for (DMPAttributeDto attribute : attributes) {
            System.out.println("--> " + attribute.getName());
            List<DMPSelectorDto> selectors = audienceService.getDMPSelectorsForDMPAttribute(attribute, false);
            for (DMPSelectorDto selector : selectors) {
                System.out.println("----> " + selector.getName());
            }
        }

        Long count = null;
        count = audienceService.getMuidSegmentSize(1L);
        System.out.println("Count 1: " + count);
        count = audienceService.getMuidSegmentSize(2L);
        System.out.println("Count 2: " + count);
        count = audienceService.getMuidSegmentSize(3L);
        System.out.println("Count 3: " + count);
        count = audienceService.getMuidSegmentSize(4L);
        System.out.println("Count 4: " + count);
        count = audienceService.getMuidSegmentSize(6L);
        System.out.println("Count 6: " + count);
    }

    @Test
    public void testParsing() {
        FileInputStream xls = null;
        FileInputStream xlsx = null;
        FileInputStream csv = null;
        Map<String, String> map = null;
        DeviceIdsValidated devicesValidated = null;
        try {
            DeviceIdentifierTypeDto idType = deviceIdentifierService
                    .getDeviceIdentifierTypeBySystemName(DeviceIdentifierType.SYSTEM_NAME_HIFA);
            FirstPartyAudienceDto fpa = new FirstPartyAudienceDto();
            System.out.println("---------------------------------------------------------------------------------------");
            System.out.println("-- XLS --------------------------------------------------------------------------------");
            System.out.println("---------------------------------------------------------------------------------------");
            xls = new FileInputStream("c:/adfonic/temp/tools2/ids.xls");
            devicesValidated = audienceService.validateDeviceIdsFileUpload("testParsing", idType, "ids.xls",
                    AudienceService.DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLS, xls);
            map = audienceService.processDeviceIdsFileUpload("testParsing", fpa, devicesValidated.getIdsValidated(), devicesValidated.getDevicesRead(),
                    devicesValidated.getDevicesValidated(), "ids.xls", idType);
            String status = map.get(AudienceService.DEVICE_IDS_UPLOAD_STATUS);
            if (status.equals(AudienceService.DEVICE_IDS_UPLOAD_STATUS_FAILURE)) {
                System.out.println(map.get(AudienceService.DEVICE_IDS_UPLOAD_STATUS_STACK_TRACE));
            }
            System.out.println("---------------------------------------------------------------------------------------");
            System.out.println("-- XLSX -------------------------------------------------------------------------------");
            System.out.println("---------------------------------------------------------------------------------------");
            xlsx = new FileInputStream("c:/adfonic/temp/tools2/ids.xlsx");
            devicesValidated = audienceService.validateDeviceIdsFileUpload("testParsing", idType, "ids.xlsx",
                    AudienceService.DEVICE_IDS_UPLOAD_CONTENT_TYPE_EXCEL_XLSX, xlsx);
            map = audienceService.processDeviceIdsFileUpload("testParsing", fpa, devicesValidated.getIdsValidated(), devicesValidated.getDevicesRead(),
                    devicesValidated.getDevicesValidated(), "ids.xlsx", idType);
            status = map.get(AudienceService.DEVICE_IDS_UPLOAD_STATUS);
            if (status.equals(AudienceService.DEVICE_IDS_UPLOAD_STATUS_FAILURE)) {
                System.out.println(map.get(AudienceService.DEVICE_IDS_UPLOAD_STATUS_STACK_TRACE));
            }
            System.out.println("---------------------------------------------------------------------------------------");
            System.out.println("-- CSV --------------------------------------------------------------------------------");
            System.out.println("---------------------------------------------------------------------------------------");
            csv = new FileInputStream("c:/adfonic/temp/tools2/ids.csv");
            devicesValidated = audienceService.validateDeviceIdsFileUpload("testParsing", idType, "ids.csv",
                    AudienceService.DEVICE_IDS_UPLOAD_CONTENT_TYPE_CSV, csv);
            map = audienceService.processDeviceIdsFileUpload("testParsing", fpa, devicesValidated.getIdsValidated(), devicesValidated.getDevicesRead(),
                    devicesValidated.getDevicesValidated(), "ids.csv", idType);
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
        } finally {
            if (xls != null) {
                try {
                    xls.close();
                } catch (Exception e) {
                }
            }
            if (xlsx != null) {
                try {
                    xlsx.close();
                } catch (Exception e) {
                }
            }
            if (csv != null) {
                try {
                    csv.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @Test
    public void testUploadHistoryQuery() {
        try {
            AudienceDto audience = audienceService.getAudienceDtoById(7L);
            if (audience != null) {
                FirstPartyAudienceDto fpa = audience.getFirstPartyAudience();
                Long count = audienceService.countFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(fpa);
                System.out.println("Count: " + count);
                List<FirstPartyAudienceDeviceIdsUploadHistoryDto> uploads = audienceService
                        .getFirstPartyAudienceDeviceIdsUploadHistoriesForFirstPartyAudience(fpa,
                                new FirstPartyAudienceDeviceIdsUploadHistorySortBy(
                                        FirstPartyAudienceDeviceIdsUploadHistorySortBy.Field.DATE_TIME_UPLOAD, false));
                if (!CollectionUtils.isEmpty(uploads)) {
                    for (FirstPartyAudienceDeviceIdsUploadHistoryDto upload : uploads) {
                        System.out.println(upload.getFilename() + " - " + upload.getDateTimeUploaded());
                    }
                }
            }
        } catch (Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
        }
    }
}
