package com.adfonic.tools.beans.campaign.targeting;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.segment.SegmentDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.geotarget.GeotargetDto;
import com.adfonic.dto.geotarget.GeotargetTypeDto;
import com.adfonic.dto.geotarget.LocationTargetDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.location.LocationService;
import com.adfonic.tools.beans.data.NoMatchDataBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;
import com.adfonic.tools.util.ExcelUtils;

@Component
@Scope("view")
public class CampaignTargetingLocationMBean extends GenericAbstractBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTargetingLocationMBean.class);

    private static final String JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_UPLOAD_CODES = "accordionAgrupationTabs:uploadCodes";
    private static final String JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_UPLOAD_COORDINATES = "accordionAgrupationTabs:uploadCoordinates";
    private static final String JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_INC_COUNTRY = "accordionAgrupationTabs:inc-country";
    private static final String JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_POSTRAD = "accordionAgrupationTabs:in-postrad";
    private static final String JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_POSTCODE = "accordionAgrupationTabs:in-postcode";
    private static final String JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_RAD = "accordionAgrupationTabs:in-rad";
    private static final String JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_LON = "accordionAgrupationTabs:in-lon";
    private static final String JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_NAM = "accordionAgrupationTabs:in-nam";
    private static final String JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_LAT = "accordionAgrupationTabs:in-lat";

    private static final String MESSAGE_KEY_TARGETING_LOCATION_CODE_ERROR = "page.campaign.targeting.location.code.error.message";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCODE = "page.campaign.targeting.location.coordinates.message.nocode";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NORADIUS = "page.campaign.targeting.location.coordinates.message.noradius";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_INVALID_LONGITUDE = "page.campaign.targeting.location.coordinates.message.invalid.longitude";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOLONGITUDE = "page.campaign.targeting.location.coordinates.message.nolongitude";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_INVALID_LATITUDE = "page.campaign.targeting.location.coordinates.message.invalid.latitude";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOLATITUDE = "page.campaign.targeting.location.coordinates.message.nolatitude";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NONAME = "page.campaign.targeting.location.coordinates.message.noname";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_POSTCODES_FILE_ERROR = "page.campaign.targeting.location.postcodes.file.error.message";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_FILE_ERROR = "page.campaign.targeting.location.coordinates.file.error.message";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_FILE_MAX_ERROR = "page.campaign.targeting.location.coordinates.file.max.error.message";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCOUNTRY = "page.campaign.targeting.location.coordinates.message.nocountry";
    private static final String MESSAGE_KEY_TARGETING_LOCATION_COUNTRY_OPTIONS = "page.campaign.targeting.location.targetbycountry.options.label.";

    private static final int MAX_LATITUDE = 90;
    private static final int MIN_LATITUDE = -90;
    private static final int MAX_LONGITUDE = 180;
    private static final int MIN_LONGITUDE = -180;
    private static final int MAX_LOCATION_COORDINATES_UPLOAD = 500;

    static final String TYPE_GEOTARGET = "GEO";
    static final String TYPE_COUNTRY = "COUNTRY";
    static final String TYPE_COORDINATES = "COORDINATES";
    static final String TYPE_POSTCODE = "POSTCODE";

    private static final int FILENAME_PARTS = 2;

    @Autowired
    private LocationService locationService;

    private CampaignDto campaignDto;
    private String locationTargetingType;
    private CountryDto geotargetingCountry;
    private List<CountryDto> geotargetCountriesItems;
    private GeotargetTypeDto geotargetingType;
    private List<GeotargetDto> geotargetsList = new ArrayList<GeotargetDto>(0);
    private List<GeotargetDto> geotargetsToAdd = new ArrayList<GeotargetDto>(0);
    private List<NoMatchDataBean> notRecognizedModels = new ArrayList<NoMatchDataBean>(0);
    private String geosAddList;
    private boolean countryListIsWhitelist;
    private List<CountryDto> countryList = new ArrayList<CountryDto>(0);
    private CountryDto selectedCountry = null;
    private CountryDto selectedCountryPostcode = null;
    private List<CountryDto> countriesForPostcode = null;
    private boolean changesNotSave = false;
    private boolean renderingForDialog = false;
    private List<LocationTargetDto> locationsList = new ArrayList<LocationTargetDto>();
    private List<LocationTargetDto> coordSelectedRows = null;
    private LocationTargetDto newLocation = new LocationTargetDto();
    private boolean gps = true;
    private String postCode;
    private BigDecimal postRadius;
    private Map<CountryDto, List<LocationTargetDto>> postCodesListMap = new HashMap<CountryDto, List<LocationTargetDto>>();
    private List<LocationTargetDto> postcodeSelectedRows = null;

    private Boolean locationListHasChanged = false;

    @Override
    public void init() {
        // do nothing
    }

    // -------
    // LOAD CAMPAIGN METHODS
    // -------

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = dto;
        if (campaignDto != null) {
            this.countryListIsWhitelist = campaignDto.getCurrentSegment().getCountryListIsWhitelist();
            this.countryList = new ArrayList<CountryDto>(campaignDto.getCurrentSegment().getCountries());

            if (campaignDto.getCurrentSegment().getGeotargetType() == null) {
                this.locationTargetingType = TYPE_COUNTRY;
            } else if (!campaignDto.getCurrentSegment().getGeotargetType().getType().equals(com.adfonic.domain.GeotargetType.RADIUS_TYPE)) {
                this.locationTargetingType = TYPE_GEOTARGET;
                loadGeoTargets();
            } else if (campaignDto.getCurrentSegment().getGeotargetType().getName().equals(com.adfonic.domain.GeotargetType.COORDINATES)) {
                this.locationTargetingType = TYPE_COORDINATES;
                loadLocations();
            } else {
                this.locationTargetingType = TYPE_POSTCODE;
                loadPostCodes();
            }

            this.gps = campaignDto.getCurrentSegment().isExplicitGPSEnabled();
        }
        LOGGER.debug("loadCampaignDto<--");
    }

    private void loadGeoTargets() {
        this.geotargetsList = new ArrayList<GeotargetDto>(campaignDto.getCurrentSegment().getGeotargets());
        this.geotargetingType = campaignDto.getCurrentSegment().getGeotargetType();
        if (geotargetingCountry == null) {
            if (CollectionUtils.isNotEmpty(getGeotargetsList())) {
                GeotargetDto geotarget = getGeotargetsList().iterator().next();
                geotarget = locationService.getGeotargetById(geotarget.getId());
                geotargetingCountry = geotarget.getCountry();
                geotargetingType = campaignDto.getCurrentSegment().getGeotargetType();
                LOGGER.debug("geotargetingCountry: " + geotargetingCountry + " geotargetingType: " + geotarget.getType());
            } else {
                geotargetingCountry = getCountryList().get(0);
            }
        }
    }

    private void loadLocations() {
        if (!getCountryList().isEmpty()) {
            this.selectedCountry = getCountryList().get(0);
        }
        if (!CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getLocationTargets())) {
            this.locationsList.clear();
            this.locationsList.addAll(campaignDto.getCurrentSegment().getLocationTargets());
            this.locationListHasChanged = false;
        }
    }

    private void loadPostCodes() {
        if (!getCountryList().isEmpty()) {  
            this.selectedCountryPostcode = getCountryList().get(0);
        }
        this.postCodesListMap.clear();
        if (!CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getLocationTargets())) {
            List<LocationTargetDto> postCodesList = getPostalCodesListBySelectedCountry();
            postCodesList.addAll(campaignDto.getCurrentSegment().getLocationTargets());
        }
    }

    // -------
    // PREPARE LOCATION TARGETING BEFORE SAVING INFORMATION
    // -------

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        campaignDto = dto;
        SegmentDto segment = campaignDto.getCurrentSegment();
        UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
        AdvertiserDto advertiserDto = userDto.getAdvertiserDto();
        if (locationTargetingType.equals(TYPE_GEOTARGET)) {
            prepareGeoTargets(dto, segment);
        } else if (locationTargetingType.equals(TYPE_COUNTRY)) {
            prepareCountry(dto, segment);
        } else if (locationTargetingType.equals(TYPE_COORDINATES)) {
            prepareLocations(dto, segment, advertiserDto);
        } else if (locationTargetingType.equals(TYPE_POSTCODE)) {
            preparePostCodes(dto, segment, advertiserDto);
        }
        
        // Populate explicit GPS option
        segment.setExplicitGPSEnabled(this.gps);

        // Countries list
        LOGGER.debug("countryList: " + getCountryList().size());
        Utils.fillSetWithList(getCountryList(), segment.getCountries());
        cleanBean();
        LOGGER.debug("prepareDto<--");
        return dto;
    }

    private void prepareGeoTargets(CampaignDto dto, SegmentDto segment) {
        if (geotargetingCountry != null) {
            LOGGER.debug("Geotargeting selected but no geotargets");
            getCountryList().clear();
            getCountryList().add(geotargetingCountry);
        }
        // Geotarget list
        Utils.fillSetWithList(getGeotargetsList(), segment.getGeotargets());
        dto.getCurrentSegment().setCountryListIsWhitelist(true);
        dto.getCurrentSegment().setGeotargetType(geotargetingType);
        segment.setExplicitGPSEnabled(false);
        segment.getLocationTargets().clear();
    }

    private void prepareCountry(CampaignDto dto, SegmentDto segment) {
        // Include exclude countries
        dto.getCurrentSegment().setCountryListIsWhitelist(countryListIsWhitelist);
        dto.getCurrentSegment().setGeotargetType(null);
        segment.setExplicitGPSEnabled(false);
        segment.getLocationTargets().clear();
        segment.getGeotargets().clear();
    }

    private void prepareLocations(CampaignDto dto, SegmentDto segment, AdvertiserDto advertiserDto) {
        getCountryList().clear();
        getCountryList().add(selectedCountry);
        dto.getCurrentSegment().setCountryListIsWhitelist(true);
        segment.getGeotargets().clear();
        segment.getLocationTargets().clear();
        for (LocationTargetDto lt : locationsList) {
            LocationTargetDto location = lt;
            if (lt.getId() == null) {
                location = locationService.createLocationTarget(advertiserDto, lt.getName(), lt.getLatitude(), lt.getLongitude(),
                        lt.getRadiusMiles());
                if (location != null) {
                    segment.getLocationTargets().add(location);
                }
            } else {
                segment.getLocationTargets().add(location);
            }
        }
        dto.getCurrentSegment().setGeotargetType(
                locationService.getGeotargetTypeByNameAndType(com.adfonic.domain.GeotargetType.COORDINATES,
                        com.adfonic.domain.GeotargetType.RADIUS_TYPE));
    }

    private void preparePostCodes(CampaignDto dto, SegmentDto segment, AdvertiserDto advertiserDto) {
        getCountryList().clear();
        getCountryList().add(selectedCountryPostcode);
        dto.getCurrentSegment().setCountryListIsWhitelist(true);
        segment.getGeotargets().clear();
        segment.getLocationTargets().clear();
        List<LocationTargetDto> postCodesList = getPostalCodesListBySelectedCountry();
        for (LocationTargetDto lt : postCodesList) {
            LocationTargetDto location = lt;
            if (lt.getId() == null) {
                location = locationService.createLocationTarget(advertiserDto, lt.getName(), lt.getLatitude(), lt.getLongitude(),
                        lt.getRadiusMiles());
                if (location != null) {
                    segment.getLocationTargets().add(location);
                }
            } else {
                segment.getLocationTargets().add(location);
            }
        }
        List<GeotargetTypeDto> geoTypes = (List<GeotargetTypeDto>) locationService.getGeotargetingTypesForCountry(selectedCountryPostcode,
                true);
        dto.getCurrentSegment().setGeotargetType(geoTypes.get(0));
    }

    private List<LocationTargetDto> getPostalCodesListBySelectedCountry() {
        List<LocationTargetDto> postCodesList = this.postCodesListMap.get(this.selectedCountryPostcode);
        if (postCodesList == null) {
            postCodesList = new ArrayList<LocationTargetDto>();
            this.postCodesListMap.put(this.selectedCountryPostcode, postCodesList);
        }
        return postCodesList;
    }

    private void cleanBean() {
        if (!locationTargetingType.equals(TYPE_GEOTARGET)) {
            geotargetsList.clear();
        } else if (!locationTargetingType.equals(TYPE_COORDINATES)) {
            selectedCountry = null;
            this.locationsList.clear();
            this.locationListHasChanged = false;
        } else if (!locationTargetingType.equals(TYPE_POSTCODE)) {
            selectedCountryPostcode = null;
            this.postCodesListMap.clear();
        }
    }

    // -------
    // AUTOCOMPLETE METHODS
    // -------

    public Collection<CountryDto> completeCountry(String query) {
        return locationService.searchCountryByName(query);
    }

    public Collection<GeotargetDto> completeState(String query) {
        LOGGER.debug("searching for geotargets for country:type: " + getGeotargetingCountry() + ":" + getGeotargetingType());
        if (geotargetingCountry == null || geotargetingType == null) {
            return new ArrayList<GeotargetDto>();
        }
        return locationService.getGeotargetsByNameAndTypeAndIsoCode(query, geotargetingCountry.getIsoCode(), geotargetingType);
    }

    // -------
    // EVENTS METHODS
    // -------

    public void geotargetChanges(org.primefaces.event.SelectEvent event) {
        // screwfix add-all check
        GeotargetDto g = (GeotargetDto) event.getObject();
        if (g != null && "__All_Zones_SCFX".equals(g.getName())) {
            geotargetsList.clear();
            Collection<GeotargetDto> screwFix = locationService.getGeotargetsByTypeAndIsoCode("GB", locationService
                    .getGeotargetTypeByNameAndType(com.adfonic.domain.GeotargetType.COORDINATES,
                            com.adfonic.domain.GeotargetType.RADIUS_TYPE));
            for (GeotargetDto dto : screwFix) {
                if (!("__All_Zones_SCFX").equals(dto.getName())) {
                    geotargetsList.add(dto);
                }
            }
        }
        changesNotSave = true;
    }

    public void geotargetChanges(org.primefaces.event.UnselectEvent event) {
        LOGGER.debug("geotargetChanges event: " + event);
        changesNotSave = true;
    }

    public void addGeotargets(ActionEvent event) {
        LOGGER.debug("addGeotargets event: " + event);
        String[] ret = geosAddList.split("\r\n");
        for (int j = 0; j < ret.length; j++) {
            if (!StringUtils.isEmpty(ret[j])) {
                addGeoTarget(ret, j);
            }
        }
        LOGGER.debug("addGeotargets<--");
    }

    private void addGeoTarget(String[] ret, int j) {
        GeotargetDto geotarget = locationService.getGeotargetByNameAndTypeAndIsoCode(ret[j], getGeotargetingCountry().getIsoCode(),
                geotargetingType);
        if (geotarget != null && geotarget.getId() != null) {
            if (!geotargetsToAdd.contains(geotarget)) {
                LOGGER.debug(geotarget.getName() + " to add");
                geotargetsToAdd.add(geotarget);
            }
        } else if (!StringUtils.isEmpty(ret[j])) {
            LOGGER.debug(ret[j] + " not recognized");
            NoMatchDataBean data = new NoMatchDataBean();
            data.setName(ret[j]);
            notRecognizedModels.add(data);
        }
    }

    public void addGeotargetsToList(ActionEvent event) {
        LOGGER.debug("addGeotargetsToList event: " + event);
        LOGGER.debug("addGeotargetsToList-->");
        for (NoMatchDataBean bean : notRecognizedModels) {
            if (bean.getDto() != null && !geotargetsToAdd.contains(bean.getDto())) {
                geotargetsToAdd.add((GeotargetDto) bean.getDto());
            }
        }
        notRecognizedModels.clear();
        for (GeotargetDto geotarget : geotargetsToAdd) {
            if (!geotargetsList.contains(geotarget)) {
                LOGGER.debug(geotarget.getName() + " added");
                geotargetsList.add(geotarget);
            }
        }
        // Removing not in list Geotargets
        List<GeotargetDto> geosToRemove = new ArrayList<GeotargetDto>();
        for (GeotargetDto geo : geotargetsList) {
            if (!geotargetsToAdd.contains(geo)) {
                geosToRemove.add(geo);
            }
        }
        for (GeotargetDto geo : geosToRemove) {
            LOGGER.debug(geo.getName() + " removed");
            geotargetsList.remove(geo);
        }
        geotargetsToAdd.clear();
        LOGGER.debug("addGeotargetsToList<--");
    }

    public void uploadCoordinates(FileUploadEvent event) throws IOException {
        if (selectedCountry != null) {
            if (checkFileInfo(event)) {
                List<LocationTargetDto> locs = ExcelUtils.getLocationTargetFromExcel(event.getFile().getInputstream(), false);
                if (locs != null) {
                    if (this.locationsList.size() + locs.size() > MAX_LOCATION_COORDINATES_UPLOAD){
                        addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_UPLOAD_COORDINATES,
                                MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_FILE_MAX_ERROR, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_FILE_MAX_ERROR, new Integer(MAX_LOCATION_COORDINATES_UPLOAD).toString());
                    }else{
                        this.locationsList.addAll(locs);
                        this.locationListHasChanged = true;
                    }
                } else {
                    addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_INC_COUNTRY,
                            MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCOUNTRY, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCOUNTRY);
                }
            } else {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_UPLOAD_COORDINATES,
                        MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_FILE_ERROR, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_FILE_ERROR);
            }
        } else {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_INC_COUNTRY,
                    MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCOUNTRY, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCOUNTRY);
        }
    }

    public void uploadPostalcodes(FileUploadEvent event) throws IOException {
        if (checkFileInfo(event)) {
            List<LocationTargetDto> codes = ExcelUtils.getLocationTargetFromExcel(event.getFile().getInputstream(), true);
            if (codes != null) {
                for (LocationTargetDto lt : codes) {
                    addPostalCode(lt);
                }
            } else {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_UPLOAD_CODES,
                        MESSAGE_KEY_TARGETING_LOCATION_POSTCODES_FILE_ERROR, MESSAGE_KEY_TARGETING_LOCATION_POSTCODES_FILE_ERROR);
            }
        } else {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_UPLOAD_CODES,
                    MESSAGE_KEY_TARGETING_LOCATION_POSTCODES_FILE_ERROR, MESSAGE_KEY_TARGETING_LOCATION_POSTCODES_FILE_ERROR);
        }
    }

    private boolean checkFileInfo(FileUploadEvent event) {
        boolean isValid = false;
        if (event != null && event.getFile() != null) {
            isValid = ("application/vnd.ms-excel".equals(event.getFile().getContentType()) || (event.getFile().getFileName().split("\\.").length == FILENAME_PARTS && "xls"
                    .equals(event.getFile().getFileName().split("\\.")[1])));
        }
        return isValid;
    }

    private void addPostalCode(LocationTargetDto lt) {
        if (!StringUtils.isEmpty(lt.getName())) {
            LocationTargetDto locationDto = locationService.searchLocationFromCode(this.selectedCountryPostcode,
                    lt.getName().replaceAll(" ", "").toUpperCase());
            if (locationDto != null) {
                locationDto.setName(lt.getName());
                locationDto.setRadiusMiles(lt.getRadiusMiles());
                getPostalCodesListBySelectedCountry().add(locationDto);
            }
        }
    }

    public void cancelList(ActionEvent event) {
        LOGGER.debug("cancelList event: " + event);
        geotargetsToAdd.clear();
        notRecognizedModels.clear();
    }

    public void handleCountrySelection(SelectEvent event) {
        CountryDto c = (CountryDto) event.getObject();
        this.selectedCountry = c;
        getCampaignTargetingBean().updateOperators(TYPE_COORDINATES);
    }

    public void handleCountrySelection(UnselectEvent event) {
        LOGGER.debug("handleCountrySelection event: " + event);
        this.selectedCountry = null;
        getCampaignTargetingBean().updateOperators(TYPE_COORDINATES);
    }

    public boolean isValid() {
        if (locationTargetingType.equals(TYPE_COORDINATES) && this.selectedCountry == null) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_INC_COUNTRY,
                    MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCOUNTRY, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCOUNTRY);
            return false;
        }
        return true;
    }

    public void addCoordinatesLocation(ActionEvent event) {
        LOGGER.debug("addCoordinatesLocation event: " + event);
        if (selectedCountry != null) {
            if (StringUtils.isEmpty(newLocation.getName())) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_NAM,
                        MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NONAME, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NONAME);
            } else if (newLocation.getLatitude() == null) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_LAT,
                        MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOLATITUDE, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOLATITUDE);
            } else if (newLocation.getLatitude().doubleValue() > MAX_LATITUDE || newLocation.getLatitude().doubleValue() < MIN_LATITUDE) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_LAT,
                        MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_INVALID_LATITUDE,
                        MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_INVALID_LATITUDE);
            } else if (newLocation.getLongitude() == null) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_LON,
                        MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOLONGITUDE, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOLONGITUDE);
            } else if (newLocation.getLongitude().doubleValue() > MAX_LONGITUDE || newLocation.getLongitude().doubleValue() < MIN_LONGITUDE) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_LAT,
                        MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_INVALID_LONGITUDE,
                        MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_INVALID_LONGITUDE);
            } else if (newLocation.getRadiusMiles() == null) {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_RAD,
                        MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NORADIUS, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NORADIUS);
            } else {
                locationsList.add(0, newLocation);
                this.locationListHasChanged = true;
                newLocation = new LocationTargetDto();
            }
        } else {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_INC_COUNTRY,
                    MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCOUNTRY, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCOUNTRY);
        }
    }

    public void addPostCodeLocation(ActionEvent event) {
        LOGGER.debug("addPostCodeLocation event: " + event);
        if (StringUtils.isEmpty(postCode)) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_POSTCODE,
                    MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NONAME, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NOCODE);
        } else if (postRadius == null) {
            addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_POSTRAD,
                    MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NORADIUS, MESSAGE_KEY_TARGETING_LOCATION_COORDINATES_NORADIUS);
        } else {
            // The service requires postcode with no white spaces and uppercase
            String pc = this.postCode.replaceAll(" ", "").toUpperCase();
            LocationTargetDto locationDto = locationService.searchLocationFromCode(this.selectedCountryPostcode, pc);
            if (locationDto != null) {
                locationDto.setName(this.postCode);
                locationDto.setRadiusMiles(this.postRadius);
                getPostalCodesListBySelectedCountry().add(locationDto);
                postRadius = null;
                postCode = "";
            } else {
                addFacesMessage(FacesMessage.SEVERITY_ERROR, JSF_COMPONENT_ACCORDION_AGRUPATION_TABS_IN_POSTCODE,
                        MESSAGE_KEY_TARGETING_LOCATION_CODE_ERROR, MESSAGE_KEY_TARGETING_LOCATION_CODE_ERROR);
            }
        }
    }

    public void selectedRowListener(AjaxBehaviorEvent event) {
        LocationTargetDto loc = (LocationTargetDto) event.getComponent().getAttributes().get("rowDto");
        if (loc != null) {
            loc.setSelected((boolean) ((SelectBooleanCheckbox) event.getSource()).getValue());
        }
    }

    public void removeLocation(ActionEvent event) {
        LOGGER.debug("removeLocation event: " + event);
        if (coordSelectedRows != null && !coordSelectedRows.isEmpty()) {
            this.locationsList = deleteTargets(this.locationsList, this.coordSelectedRows);
            this.coordSelectedRows = null;
            this.locationListHasChanged = true;
        }
    }

    public void removePostCode(ActionEvent event) {
        LOGGER.debug("removePostCode event: " + event);
        if (postcodeSelectedRows != null && !postcodeSelectedRows.isEmpty()) {
            List<LocationTargetDto> updatedList = deleteTargets(getPostalCodesListBySelectedCountry(), this.postcodeSelectedRows);
            this.postCodesListMap.put(this.selectedCountryPostcode, updatedList);
            this.postcodeSelectedRows = null;
        }
    }

    private List<LocationTargetDto> deleteTargets(List<LocationTargetDto> sourceList, List<LocationTargetDto> toRemoveList) {
        List<LocationTargetDto> result = new ArrayList<LocationTargetDto>();
        result.addAll(sourceList);
        for (LocationTargetDto lt : toRemoveList) {
            if (result.contains(lt)) {
                result.remove(lt);
            }
        }
        return result;
    }

    public void updateMap(ActionEvent event) {
        LOGGER.debug("updateMap event: " + event);
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("gMapInitialize()");
        context.execute("deleteOverlays()");
        List<LocationTargetDto> locations;
        if (locationTargetingType.equals(TYPE_COORDINATES)) {
            locations = getCoordinatesCurrentPage();
        } else {
            locations = getPostalCodesListBySelectedCountry();
        }

        for (LocationTargetDto lt : locations) {
            double radiusInMeters = lt.getRadiusMiles().doubleValue() * Constants.METERS_IN_MILE;
            context.execute("addMarker(" + lt.getLatitude().doubleValue() + "," + lt.getLongitude().doubleValue() + ",\"" + lt.getName()
                    + "\"," + radiusInMeters + ")");
        }
    }

    private List<LocationTargetDto> getCoordinatesCurrentPage() {
        DataTable dataTable = (DataTable) getUIComponent("campaign-targeting:accordionAgrupationTabs:coordinateTable");
        int currentPage = dataTable.getPage();
        int numbersOfRow = dataTable.getRows();

        int initPosition = currentPage * numbersOfRow;
        int finalPosition = initPosition + numbersOfRow - 1;
        if (finalPosition >= this.locationsList.size()) {
            finalPosition = this.locationsList.size();
        }

        return locationsList.subList(initPosition, finalPosition);
    }

    // -------
    // METHODS TO RETRIEVE SUMMARY INFORMATION
    // -------

    public String getLocationSummary(boolean spaces) {
        if (campaignDto != null) {
            // Check if its saved or not..
            resetMyChanges();

            String space = "";
            if (spaces) {
                space = " ";
            }

            if (locationTargetingType == null) {
                return notSet();
            } else if (locationTargetingType.equals(TYPE_GEOTARGET)) {
                return getGeoTargetSummary(space);
            } else if (locationTargetingType.equals(TYPE_COUNTRY)) {
                return getCountrySummary(space);
            } else if (locationTargetingType.equals(TYPE_COORDINATES)) {
                return getCoordinatesSummary();
            } else if (locationTargetingType.equals(TYPE_POSTCODE)) {
                return getPostCodeSummary(space);
            }

        }
        return notSet();
    }

    private void resetMyChanges() {
        if (changesNotSave && !renderingForDialog) {
            // reset everything to default values....
            locationTargetingType = null;
            geotargetingCountry = null;
            if (campaignDto != null && campaignDto.getCurrentSegment() != null) {
                geotargetsList.clear();
                countryList.clear();
            }
        }
    }

    private String getGeoTargetSummary(String space) {
        StringBuilder message = new StringBuilder();
        if (getGeotargetingCountry() != null) {
            message.append(getGeotargetingCountry().getName());
        } else {
            message.append(notSet());
        }
        if (campaignDto.getCurrentSegment() != null && !CollectionUtils.isEmpty(geotargetsList)) {
            message.append(":").append(space);
            for (GeotargetDto g : geotargetsList) {
                message.append(g.getName()).append(",").append(space);
            }
            return message.substring(0, message.length() - (1 + space.length()));
        }
        return message.toString();
    }

    private String getCountrySummary(String space) {
        StringBuilder message = new StringBuilder();
        if (campaignDto.getCurrentSegment() != null && CollectionUtils.isEmpty(countryList)) {
            return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
        }
        if (!campaignDto.getCurrentSegment().getCountryListIsWhitelist()) {
            message.append("Exclude:").append(space);
        }
        if (!CollectionUtils.isEmpty(countryList)) {
            for (CountryDto g : countryList) {
                message.append(g.getName()).append(",").append(space);
            }
            return message.substring(0, message.length() - (1 + space.length()));
        }
        return message.toString();
    }

    private String getCoordinatesSummary() {
        StringBuilder message = new StringBuilder();
        if (selectedCountry != null) {
            message.append(selectedCountry.getName());
            message.append(" ");
            message.append(FacesUtils.getBundleMessage("page.campaign.targeting.location.summary.coordinates"));
        } else {
            message.append(notSet());
        }
        return message.toString();
    }

    private String getPostCodeSummary(String space) {
        StringBuilder message = new StringBuilder();
        if (selectedCountryPostcode != null) {
            message.append(selectedCountryPostcode.getName());
        } else {
            message.append(notSet());
        }
        List<LocationTargetDto> postCodesList = getPostalCodesListBySelectedCountry();
        if (campaignDto.getCurrentSegment() != null && !CollectionUtils.isEmpty(postCodesList)) {
            message.append(":").append(space);
            for (LocationTargetDto lt : postCodesList) {
                message.append(lt.getName()).append(",").append(space);
            }
            return message.substring(0, message.length() - (1 + space.length()));
        }
        return message.toString();
    }

    // -------
    // GETTERS AND SETTERS
    // -------

    public String getMatchingMessage() {
        return FacesUtils.getBundleMessage("page.commons.messages.itemsfound",
                Integer.toString(geotargetsToAdd.size() + notRecognizedModels.size()), Integer.toString(geotargetsToAdd.size()));
    }

    public String getNotMatchingMessage() {
        return FacesUtils.getBundleMessage("page.commons.messages.notmatch", Integer.toString(notRecognizedModels.size()));
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public String getLocationTargetingType() {
        if (locationTargetingType == null) {
            if (getCampaignDto() != null && !CollectionUtils.isEmpty(getGeotargetsList())) {
                locationTargetingType = TYPE_GEOTARGET;
            } else {
                locationTargetingType = TYPE_COUNTRY;
            }
        }
        return locationTargetingType;
    }

    public void setLocationTargetingType(String locationTargetingType) {
        this.locationTargetingType = locationTargetingType;
    }

    public List<LocationTargetDto> getPostcodeSelectedRows() {
        return postcodeSelectedRows;
    }

    public void setPostcodeSelectedRows(List<LocationTargetDto> postcodeSelectedRows) {
        this.postcodeSelectedRows = postcodeSelectedRows;
    }

    public String getAddButtonText() {
        if (CollectionUtils.isEmpty(geotargetsList)) {
            return FacesUtils.getBundleMessage("page.commons.buttons.add");
        } else {
            return FacesUtils.getBundleMessage("page.commons.buttons.update");
        }
    }

    public GeotargetTypeDto getGeotargetingType() {
        if (this.geotargetingType == null) {
            this.geotargetingType = this.campaignDto.getCurrentSegment().getGeotargetType();
            if (this.geotargetingType == null && CollectionUtils.isNotEmpty(getGeotargetTypesItems())) {
                this.geotargetingType = getGeotargetTypesItems().get(0);
            }
        }
        return this.geotargetingType;
    }

    public void setGeotargetingType(GeotargetTypeDto geotargetingType) {
        if (geotargetingType != null) {
            this.geotargetingType = geotargetingType;
        }
    }

    public boolean getCountriesEmpty() {
        return CollectionUtils.isEmpty(getCountryList());
    }

    public boolean isGeotargetListEmpty() {
        return CollectionUtils.isEmpty(getGeotargetsList());
    }

    public boolean isUsDMA() {
        if (getGeotargetingCountry() != null && "US".equals(getGeotargetingCountry().getIsoCode()) && getGeotargetingType() != null
                && "DMA".equals(this.getGeotargetingType().getType())) {
            return true;
        }
        return false;
    }

    public boolean isGbPostalTown() {
        if (getGeotargetingCountry() != null && "GB".equals(getGeotargetingCountry().getIsoCode()) && getGeotargetingType() != null
                && "POSTAL_CODE".equals(getGeotargetingType().getType())) {
            return true;
        }
        return false;
    }

    public List<GeotargetDto> getGeotargetsList() {
        if (this.geotargetsList == null) {
            this.geotargetsList = new ArrayList<GeotargetDto>();
        }
        return geotargetsList;
    }

    public void setGeotargetsList(List<GeotargetDto> geotargetsList) {
        this.geotargetsList = geotargetsList;
    }

    public boolean isCountryListIsWhitelist() {
        return countryListIsWhitelist;
    }

    public void setCountryListIsWhitelist(boolean countryListIsWhitelist) {
        this.countryListIsWhitelist = countryListIsWhitelist;
    }

    public List<CountryDto> getCountryList() {
        if (this.countryList == null) {
            this.countryList = new ArrayList<CountryDto>();
        }
        return countryList;
    }

    public void setCountryList(List<CountryDto> countryList) {
        this.countryList = countryList;
    }

    public boolean isChangesNotSave() {
        return changesNotSave;
    }

    public void setChangesNotSave(boolean changesNotSave) {
        this.changesNotSave = changesNotSave;
    }

    public boolean isRenderingForDialog() {
        return renderingForDialog;
    }

    public void setRenderingForDialog(boolean renderingForDialog) {
        this.renderingForDialog = renderingForDialog;
    }

    public List<GeotargetDto> getGeotargetsToAdd() {
        return geotargetsToAdd;
    }

    public void setGeotargetsToAdd(List<GeotargetDto> geotargetsToAdd) {
        this.geotargetsToAdd = geotargetsToAdd;
    }

    public List<NoMatchDataBean> getNotRecognizedModels() {
        return notRecognizedModels;
    }

    public void setNotRecognizedModels(List<NoMatchDataBean> notRecognizedModels) {
        this.notRecognizedModels = notRecognizedModels;
    }

    public List<CountryDto> getSelectedCountryCoordinates() {
        List<CountryDto> list = new ArrayList<>();
        if (selectedCountry != null) {
            list.add(selectedCountry);
        }
        return list;
    }

    public void setSelectedCountryCoordinates(List<CountryDto> selectedCountryCoordinates) {
        // this.selectedCountryCoordinates = selectedCountryCoordinates;
    }

    public List<LocationTargetDto> getLocationsList() {
        return locationsList;
    }

    public void setLocationsList(List<LocationTargetDto> locationsList) {
        this.locationsList = locationsList;
    }

    public LocationTargetDto getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(LocationTargetDto newLocation) {
        this.newLocation = newLocation;
    }

    public CountryDto getSelectedCountryPostcode() {
        if (selectedCountryPostcode == null) {
            selectedCountryPostcode = locationService.getCountryByIsoCode("GB");
        }
        return selectedCountryPostcode;
    }

    public void setSelectedCountryPostcode(CountryDto selectedCountryPostcode) {
        this.selectedCountryPostcode = selectedCountryPostcode;
    }

    public boolean isGps() {
        return gps;
    }

    public void setGps(boolean gps) {
        this.gps = gps;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public List<LocationTargetDto> getPostCodesList() {
        return getPostalCodesListBySelectedCountry();
    }

    public void setPostCodesList(List<LocationTargetDto> postCodesList) {
        this.postCodesListMap.put(selectedCountryPostcode, postCodesList);
    }

    public BigDecimal getPostRadius() {
        return postRadius;
    }

    public void setPostRadius(BigDecimal postRadius) {
        this.postRadius = postRadius;
    }

    public List<CountryDto> getCountriesForPostcode() {
        if (countriesForPostcode == null) {
            countriesForPostcode = new ArrayList<CountryDto>();
            countriesForPostcode.add(locationService.getCountryByIsoCode("GB"));
            countriesForPostcode.add(locationService.getCountryByIsoCode("US"));
        }
        return countriesForPostcode;
    }

    public void setCountriesForPostcode(List<CountryDto> countriesForPostcode) {
        this.countriesForPostcode = countriesForPostcode;
    }

    public String getGeosAddList() {
        geosAddList = "";
        for (GeotargetDto model : geotargetsList) {
            geosAddList += model.getName() + "\r\n";
        }
        return geosAddList;
    }

    public void setGeosAddList(String geosAddList) {
        this.geosAddList = geosAddList;
    }

    public List<LocationTargetDto> getCoordSelectedRows() {
        return coordSelectedRows;
    }

    public void setCoordSelectedRows(List<LocationTargetDto> coordSelectedRows) {
        this.coordSelectedRows = coordSelectedRows;
    }

    // the selection items for geotargeting
    // lazy load
    public List<CountryDto> getGeotargetCountriesItems() {
        if (CollectionUtils.isEmpty(this.geotargetCountriesItems)) {
            this.geotargetCountriesItems = (List<CountryDto>) locationService.getAllGeoTargetingCountries();
        }
        return this.geotargetCountriesItems;
    }

    public List<GeotargetTypeDto> getGeotargetTypesItems() {
        if (geotargetingCountry != null) {
            return (List<GeotargetTypeDto>) locationService.getGeotargetingTypesForCountry(geotargetingCountry);
        } else {
            return new ArrayList<GeotargetTypeDto>();
        }
    }

    public CountryDto getGeotargetingCountry() {
        if (this.geotargetingCountry == null) {
            if (CollectionUtils.isNotEmpty(getGeotargetsList())) {
                // check the first one
                GeotargetDto g = getGeotargetsList().iterator().next();
                this.geotargetingCountry = g.getCountry();
            } else {
                if (CollectionUtils.isNotEmpty(getGeotargetCountriesItems())) {
                    this.geotargetingCountry = getGeotargetCountriesItems().get(0);
                }
            }
        }
        return this.geotargetingCountry;
    }

    public void setGeotargetingCountry(CountryDto geotargetingCountry) {
        this.geotargetingCountry = geotargetingCountry;
    }

    public String getPostCodeLabel() {
        if ("GB".equals(selectedCountryPostcode.getIsoCode())) {
            return FacesUtils.getBundleMessage("page.campaign.targeting.location.postcode.label");
        } else {
            return FacesUtils.getBundleMessage("page.campaign.targeting.location.zipcode.label");
        }
    }

    /*
     * new types added directly in db won't have a friendly name
     */
    public String friendlyGeotargetTypeName(String geotargetType) {
        String iso = geotargetingCountry.getIsoCode();
        if (!StringUtils.isBlank(iso) || !StringUtils.isBlank(geotargetType)) {
            try {
                return FacesUtils.getBundleMessage(MESSAGE_KEY_TARGETING_LOCATION_COUNTRY_OPTIONS + iso + "." + geotargetType);
            } catch (java.util.MissingResourceException missing) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("No properties resource for ISO: " + MESSAGE_KEY_TARGETING_LOCATION_COUNTRY_OPTIONS + iso + "."
                            + geotargetType);
                }
            }
        }
        return null;
    }

    public Boolean getLocationListHasChanged() {
        return locationListHasChanged;
    }
    
    public void isLocationListHasChanged(Boolean locationListHasChanged) {
    	this.locationListHasChanged = locationListHasChanged;
    }
}
