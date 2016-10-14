package com.adfonic.tools.beans.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.persistence.NonUniqueResultException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.model.ModelDto;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.dto.vendor.VendorDto;
import com.adfonic.dto.vendor.VendorInfoDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.model.ModelService;
import com.adfonic.presentation.reporting.model.ReportDefinition;
import com.adfonic.presentation.util.Constants;
import com.adfonic.presentation.vendor.VendorService;
import com.adfonic.tools.beans.data.NoMatchDataBean;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.export.NonMatchedDevicesReportDefinitionBuilder;

@Component
@Scope("view")
public class DeviceModelsMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ////////////////////
    // Constants
    // ////////////////////

    private static final String VENDOR_MODEL_OPTION_KEY = "page.commons.messages.option.vendormodels";

    // Pattern for all vendor models (case-insensitive match)
    private static final Pattern ALL_VENDOR_MODEL_PATTERN = Pattern.compile("\\A(?i:" + buildModelForVendor("([\\w\\s-._+]+)").getName()
            + ")\\Z");

    // ////////////////////
    // Models for the view
    // ////////////////////

    private List<ModelDto> selectedDeviceAndVendorModels = new ArrayList<>(0);
    private List<NoMatchDataBean> nonMatchedDeviceModels = new ArrayList<>(0);
    private List<ModelDto> addedDeviceModels = new ArrayList<>(0);
    private String deviceModelsText;

    private List<PlatformDto> platformsList;
    private DeviceGroupDto deviceGroupDto;

    private TimeZone userTimezone;

    @Autowired
    private ModelService modelService;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private CompanyService companyService;

    @Override
    @PostConstruct
    protected void init() throws Exception {
        this.userTimezone = companyService.getTimeZoneForAdvertiser(getUser().getAdvertiserDto());
    }

    /** Build ModelDto which represents all models from a vendor (capitalised) */
    public static ModelDto buildModelForVendor(String vendorName) {

        // Setup vendor model with name and id
        ModelDto vendorModel = new ModelDto();
        vendorModel.setPlatforms(Collections.<PlatformDto> emptySet());

        // Generate Random negative id based on vendor name hash
        String cleanedVendorName = WordUtils.capitalize(vendorName.toLowerCase());
        vendorModel.setId(Integer.valueOf(Math.abs(cleanedVendorName.hashCode()) * -1).longValue());
        vendorModel.setName(FacesUtils.getBundleMessage(VENDOR_MODEL_OPTION_KEY, cleanedVendorName));

        // Set the real vendor in the vendor model
        VendorDto vendor = new VendorDto();
        vendor.setName(vendorName);
        vendorModel.setVendor(vendor);

        return vendorModel;
    }

    /**
     * Say whether the model id is represent an all vendor model or a specific
     * model
     */
    public static boolean isVendorModelId(Long modelId) {
        return (modelId < 0) ? true : false;
    }

    /** Retrieve the vendor name if it was an all vendor model, else null */
    private static String getVendorNameIfAllVendorModel(String modelName) {
        Matcher m = ALL_VENDOR_MODEL_PATTERN.matcher(modelName);
        return (m.matches()) ? m.group(1) : null;
    }

    // ////////////////////
    // Action Handlers
    // ////////////////////

    public Collection<ModelDto> completeDeviceModels(String modelQuery) {

        // Query specific models
        List<ModelDto> queriedModelList = new ArrayList<ModelDto>(modelService.doQuery(modelQuery, platformsList, deviceGroupDto));

        // Query specific vendors
        String vendorQuery = (buildModelForVendor("_").getName().startsWith(modelQuery)) ? null : modelQuery;
        List<VendorInfoDto> vendors = vendorService.getVendorsByPlatformAndDeviceGroup(vendorQuery, platformsList, deviceGroupDto);

        // Add new model for representing all models from a vendor
        for (VendorInfoDto vendorInfo : vendors) {
            queriedModelList.add(0, buildModelForVendor(vendorInfo.getName()));
        }

        return queriedModelList;
    }

    public void clearSelectedDeviceAndVendorModels(ActionEvent event) {
        selectedDeviceAndVendorModels.clear();
    }

    public void addDeviceModels(ActionEvent event) {
        clearDeviceModels();

        String[] comas = deviceModelsText.split(",");
        for (int i = 0; i < comas.length; i++) {
            String[] ret = comas[i].split("\r\n");
            for (int j = 0; j < ret.length; j++) {

                String modelName = ret[j];
                if (!StringUtils.isEmpty(modelName)) {

                    ModelDto model = null;
                    try {
                        // Model search
                        model = modelService.getModelByName(modelName);
                    } catch (NonUniqueResultException e) {
                        model = null;
                    }

                    List<ModelDto> lModels = new ArrayList<ModelDto>();
                    // White space search is discarded for vendor + model as we
                    // add a white space for the search
                    if (model == null) {
                        if (!modelName.equals(" ")) {
                            lModels.addAll(modelService.getModelsByNameAndVendor(modelName));
                        }
                    } else {
                        // Unique model found
                        lModels.add(model);
                    }

                    String vendorName = getVendorNameIfAllVendorModel(modelName);
                    boolean isVendorModel = false;
                    if (vendorName != null) {
                        // All vendor model found
                        isVendorModel = true;
                        lModels.add(buildModelForVendor(vendorName));
                    }

                    if (!CollectionUtils.isEmpty(lModels)) {
                        for (ModelDto m : lModels) {

                            // Platform contained
                            boolean platformContained = false;
                            if (isVendorModel || CollectionUtils.isEmpty(platformsList)) {
                                platformContained = true;
                            } else {
                                for (PlatformDto p : m.getPlatforms()) {
                                    if (platformsList.contains(p)) {
                                        platformContained = true;
                                        break;
                                    }
                                }
                            }

                            // Device group contained
                            boolean deviceGroupContained = false;
                            if (isVendorModel || deviceGroupDto == null || deviceGroupDto.equals(m.getDeviceGroup())) {
                                deviceGroupContained = true;
                            } else {
                                deviceGroupContained = false;
                            }

                            // Matched model
                            if (!addedDeviceModels.contains(m) && platformContained && deviceGroupContained) {
                                addedDeviceModels.add(m);
                            }

                            // Not matched model
                            if (!platformContained || !deviceGroupContained) {
                                addNonMatchedDeviceModel(m.getName());
                            }
                        }
                    } else {
                        // Not matched model
                        addNonMatchedDeviceModel(modelName);
                    }
                }
            }
        }
    }

    public void addNonMatchedToSelectedDeviceModels(ActionEvent event) {
        for (NoMatchDataBean bean : nonMatchedDeviceModels) {
            if (bean.getDto() != null && !addedDeviceModels.contains(bean.getDto())) {
                addedDeviceModels.add((ModelDto) bean.getDto());
            }
        }

        for (ModelDto model : addedDeviceModels) {
            if (!getSelectedDeviceAndVendorModels().contains(model)) {
                // Model added
                selectedDeviceAndVendorModels.add(model);
            }
        }
        // Remove all selected devices which will not be added
        selectedDeviceAndVendorModels.retainAll(addedDeviceModels);

        clearDeviceModels();
    }

    public StreamedContent exportNonMatchedDeviceModelsToExcel() throws IOException {
        // Running report
        NonMatchedDevicesReportDefinitionBuilder<NoMatchDataBean> builder = new NonMatchedDevicesReportDefinitionBuilder<NoMatchDataBean>(
                userTimezone);
        ReportDefinition<NoMatchDataBean> reportDefinition = builder.build(nonMatchedDeviceModels);
        ByteArrayOutputStream osReport = (ByteArrayOutputStream) builder.getExcelReportingService().createReport(reportDefinition);

        return new DefaultStreamedContent(new ByteArrayInputStream(osReport.toByteArray()), Constants.CONTENT_TYPE_EXCEL_XLSX,
                "Non matched devices.xlsx");
    }

    // ////////////////////
    // Private methods
    // ////////////////////

    private void clearDeviceModels() {
        addedDeviceModels.clear();
        nonMatchedDeviceModels.clear();
    }

    private void addNonMatchedDeviceModel(String deviceModelName) {
        NoMatchDataBean data = new NoMatchDataBean();
        data.setName(deviceModelName);
        nonMatchedDeviceModels.add(data);
    }

    // ////////////////////
    // Getters / Setters
    // ////////////////////

    public List<ModelDto> getSelectedDeviceModels() {
        List<ModelDto> selectedDeviceModels = new ArrayList<>(0);
        for (ModelDto modelDto : getSelectedDeviceAndVendorModels()) {
            List<ModelDto> vendorModels = null;
            if (isVendorModelId(modelDto.getId())) {
                vendorModels = modelService.getModelsByVendorNameAndPlatformAndDeviceGroup(modelDto.getVendor().getName(), platformsList,
                        deviceGroupDto);
                selectedDeviceModels.addAll(vendorModels);
            } else {
                selectedDeviceModels.add(modelDto);
                // TODO (nice to have) add this model only if its vendor is not
                // among the selected vendor ids
            }
        }
        return selectedDeviceModels;
    }

    public void setSelectedDeviceModels(List<ModelDto> selectedDeviceModels) {
        selectedDeviceAndVendorModels.clear();
        selectedDeviceAndVendorModels.addAll(selectedDeviceModels);
    }

    /** Get selected device and vendor models while eliminating duplicates */
    public List<ModelDto> getSelectedDeviceAndVendorModels() {
        if (selectedDeviceAndVendorModels == null) {
            selectedDeviceAndVendorModels = new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(selectedDeviceAndVendorModels));
    }

    public void setSelectedDeviceAndVendorModels(List<ModelDto> selectedDeviceAndVendorModels) {
        this.selectedDeviceAndVendorModels = selectedDeviceAndVendorModels;
    }

    public String getDeviceModelsTextArea() {
        StringBuffer buffer = new StringBuffer();
        for (ModelDto modelDto : getSelectedDeviceAndVendorModels()) {
            if (!isVendorModelId(modelDto.getId())) {
                buffer.append(modelDto.getVendor().getName()).append(" ");
            }
            buffer.append(modelDto.getName()).append("\r\n");
        }
        deviceModelsText = buffer.toString();
        return deviceModelsText;
    }

    public void setDeviceModelsTextArea(String deviceModelsTextArea) {
        deviceModelsText = deviceModelsTextArea;
    }

    public List<ModelDto> getAddedDeviceModels() {
        return addedDeviceModels;
    }

    public List<NoMatchDataBean> getNonMatchedDeviceModels() {
        return nonMatchedDeviceModels;
    }

    public void setNonMatchedDeviceModels(List<NoMatchDataBean> nonMatchedDeviceModels) {
        this.nonMatchedDeviceModels = nonMatchedDeviceModels;
    }

    public void setPlatformsList(List<PlatformDto> platformsList) {
        this.platformsList = platformsList;
    }

    public void setDeviceGroupDto(DeviceGroupDto deviceGroupDto) {
        this.deviceGroupDto = deviceGroupDto;
    }

}
