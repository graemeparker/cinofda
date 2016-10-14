package com.adfonic.adserver.simulation.model;

import java.util.List;

import com.adfonic.domain.cache.dto.adserver.ContentSpecDto;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.LanguageDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;

public class AdserverDataModel {

	private boolean rtbEnabled;

	private List<AdSpaceDto> adspaces;
	private List<PublicationDto> publications;
	private List<PublicationTypeDto> publicationTypes;
	private List<IntegrationTypeDto> integrationTypes;
	private List<CreativeDto> creatives;
	private List<CampaignDto> campaignes;
	private List<FormatDto> formats;
	private List<DisplayTypeDto> displayTypes;
	private List<DestinationDto> destinations;
	private List<AdvertiserDto> advertisers;
	private List<CompanyDto> companies;
	private List<CountryDto> countries;
	private List<PublisherDto> publishers;
	private List<DeviceIdentifierTypeDto> deviceIdTypes;
	private List<AssetDto> assets;
	private List<ModelDto> models;
	private List<LanguageDto> languages;

	private List<WeightedAdspaceEligibilityModel> weightedAdspaces;
	private List<SizebasedFormatModel> formatBySizes;
	private List<CreativeAssetAndContentModel> creativeAssetContents;
	private List<ComponentContentSpecMapModel> componentContentSpecMap;
	private List<ContentSpecDto> contentSpecs;

	public boolean isRtbEnabled() {
		return rtbEnabled;
	}

	public void setRtbEnabled(boolean rtbEnabled) {
		this.rtbEnabled = rtbEnabled;
	}

	public List<AdSpaceDto> getAdspaces() {
		return adspaces;
	}

	public void setAdspaces(List<AdSpaceDto> adspaces) {
		this.adspaces = adspaces;
	}

	public List<PublicationDto> getPublications() {
		return publications;
	}

	public void setPublications(List<PublicationDto> publications) {
		this.publications = publications;
	}

	public List<PublicationTypeDto> getPublicationTypes() {
		return publicationTypes;
	}

	public void setPublicationTypes(List<PublicationTypeDto> publicationTypes) {
		this.publicationTypes = publicationTypes;
	}

	public List<IntegrationTypeDto> getIntegrationTypes() {
		return integrationTypes;
	}

	public void setIntegrationTypes(List<IntegrationTypeDto> integrationTypes) {
		this.integrationTypes = integrationTypes;
	}

	public List<CreativeDto> getCreatives() {
		return creatives;
	}

	public void setCreatives(List<CreativeDto> creatives) {
		this.creatives = creatives;
	}

	public List<CampaignDto> getCampaignes() {
		return campaignes;
	}

	public void setCampaignes(List<CampaignDto> campaignes) {
		this.campaignes = campaignes;
	}

	public List<FormatDto> getFormats() {
		return formats;
	}

	public void setFormats(List<FormatDto> formats) {
		this.formats = formats;
	}

	public List<DisplayTypeDto> getDisplayTypes() {
		return displayTypes;
	}

	public void setDisplayTypes(List<DisplayTypeDto> displayTypes) {
		this.displayTypes = displayTypes;
	}

	public List<DestinationDto> getDestinations() {
		return destinations;
	}

	public void setDestinations(List<DestinationDto> destinations) {
		this.destinations = destinations;
	}

	public List<AdvertiserDto> getAdvertisers() {
		return advertisers;
	}

	public void setAdvertisers(List<AdvertiserDto> advertisers) {
		this.advertisers = advertisers;
	}

	public List<CompanyDto> getCompanies() {
		return companies;
	}

	public void setCompanies(List<CompanyDto> companies) {
		this.companies = companies;
	}

	public List<CountryDto> getCountries() {
		return countries;
	}

	public void setCountries(List<CountryDto> countries) {
		this.countries = countries;
	}

	public List<PublisherDto> getPublishers() {
		return publishers;
	}

	public void setPublishers(List<PublisherDto> publishers) {
		this.publishers = publishers;
	}

	public List<DeviceIdentifierTypeDto> getDeviceIdTypes() {
		return deviceIdTypes;
	}

	public void setDeviceIdTypes(List<DeviceIdentifierTypeDto> deviceIdTypes) {
		this.deviceIdTypes = deviceIdTypes;
	}

	public List<AssetDto> getAssets() {
		return assets;
	}

	public void setAssets(List<AssetDto> assets) {
		this.assets = assets;
	}

	public List<ModelDto> getModels() {
		return models;
	}

	public void setModels(List<ModelDto> models) {
		this.models = models;
	}

	public List<WeightedAdspaceEligibilityModel> getWeightedAdspaces() {
		return weightedAdspaces;
	}

	public void setWeightedAdspaces(
			List<WeightedAdspaceEligibilityModel> weightedAdspaces) {
		this.weightedAdspaces = weightedAdspaces;
	}

	public List<SizebasedFormatModel> getFormatBySizes() {
		return formatBySizes;
	}

	public void setFormatBySizes(List<SizebasedFormatModel> formatBySizes) {
		this.formatBySizes = formatBySizes;
	}

	public List<CreativeAssetAndContentModel> getCreativeAssetContents() {
		return creativeAssetContents;
	}

	public void setCreativeAssetContents(
			List<CreativeAssetAndContentModel> creativeAssetContents) {
		this.creativeAssetContents = creativeAssetContents;
	}

	public List<LanguageDto> getLanguages() {
		return languages;
	}

	public void setLanguages(List<LanguageDto> languages) {
		this.languages = languages;
	}

	public List<ComponentContentSpecMapModel> getComponentContentSpecMap() {
		return componentContentSpecMap;
	}

	public void setComponentContentSpecMap(
			List<ComponentContentSpecMapModel> componentContentSpecMap) {
		this.componentContentSpecMap = componentContentSpecMap;
	}

	public List<ContentSpecDto> getContentSpecs() {
		return contentSpecs;
	}

	public void setContentSpecs(List<ContentSpecDto> contentSpecs) {
		this.contentSpecs = contentSpecs;
	}
}
