package com.adfonic.presentation.reporting;

import java.util.Locale;
import java.util.TimeZone;

public interface AdvertiserReportService {

    public abstract void init(Locale userLocale, TimeZone companyTimeZone);
}
