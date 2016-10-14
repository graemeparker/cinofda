package com.adfonic.reporting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.time.StopWatch;

import com.adfonic.olap.MondrianDataSource;

import mondrian.olap.Connection;
import mondrian.olap.Member;
import mondrian.olap.MondrianException;
import mondrian.olap.Position;
import mondrian.olap.Query;
import mondrian.olap.Result;

public class OLAPQuery {
    private static final transient Logger LOG = Logger.getLogger(OLAPQuery.class.getName());
    
    protected Locale locale;
    protected List<Parameter> parameters;
    protected List<Metric> metrics;
    protected List<Parameter> slicers;

    @Override
    public String toString() {
        return "OLAPQuery[locale=" + locale + ",parameters=" + parameters + ",metrics=" + metrics + ",slicers=" + slicers + "]";
    }

    public OLAPQuery(Locale locale) {
        this.locale = locale;
        this.parameters = new ArrayList<Parameter>();
        this.metrics = new ArrayList<Metric>();
        this.slicers = new ArrayList<Parameter>();
    }

    public void addParameters(Parameter... parameters) {
        for (Parameter p : parameters) {
            this.parameters.add(p);
        }
    }

    public void addMetrics(Metric... metrics) {
        for (Metric m : metrics) {
            this.metrics.add(m);
        }
    }

    public void addSlicers(Parameter... slicers) {
        for (Parameter p : slicers) {
            this.slicers.add(p);
        }
    }

    protected Result executeImpl(Report report) {
        String mdx = renderMDX(report);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(mdx);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return executeMDX(mdx);
        } finally {
            stopWatch.stop();
            LOG.fine("Query took " + stopWatch + ": " + mdx);
        }
    }

    /**
     * Render the mdx for this OLAPQuery object. 
     * @param Report object to add columns to that are referenced by the mdx. May pass null, if you don't care about such things
     * @throws IllegalArgument exception if no parameters or metrics have been added
     * @return String of the MDX for this OLAPQuery, suitable to pass to executeMDX
     */
    public String renderMDX(Report report) {
        if (parameters.size() == 0) {
            throw new IllegalArgumentException("No parameters");
        }
        if (metrics.size() == 0) {
            throw new IllegalArgumentException("No metrics");
        }

        StringBuilder withClause = new StringBuilder();
        String axis0 = null;
        for (int i = 0; i < parameters.size(); i++)  {
            String paramMDX = parameters.get(i).getMDX();
            if (i == 0 && paramMDX != null) {
                axis0 = paramMDX;
            } else if(paramMDX != null) {
                axis0 = "NonEmptyCrossJoin(" + axis0 + "," + paramMDX + ")";
            }
            if (report != null) { parameters.get(i).addColumn(report, locale); }
            String withMember = parameters.get(i).getWithMember();
            if (withMember != null) {
                if (withClause.length() > 0) {
                    withClause.append(' ');
                } else {
                    withClause.append("with ");
                }
                withClause.append("member ")
                    .append(withMember);
            }
        }
        axis0 = "non empty {" + axis0 + "}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i = 0; i < metrics.size(); i++) {
            if (i > 0) { sb.append(','); }
            sb.append("Measures.").append(metrics.get(i).toString());
            if (report != null) { addColumn(report, metrics.get(i)); }
        }
        sb.append('}');
        String axis1 = sb.toString();

        StringBuilder slicer = new StringBuilder();
        for (int i = 0; i < slicers.size(); i++) {
            if (i > 0) {
                slicer.append(',');
            } else {
                slicer.append(" where (");
            }
            if (withClause.length() > 0) {
                withClause.append(' ');
            } else {
                withClause.append("with ");
            }
            withClause.append("member ")
                .append(slicers.get(i).getDimension())
                .append(".[slicer").append(i).append("]")
                .append(" as Aggregate({")
                .append(slicers.get(i).getMDX())
                .append("})");
            slicer.append(slicers.get(i).getDimension())
                .append(".[slicer").append(i).append("]");
        }
        if (slicers.size() > 0) {
            slicer.append(')');
            withClause.append(' ');
        }

        String mdx = new Formatter()
            .format("%sselect %s on 0, %s on 1 from Ads%s",
                    withClause.toString(), axis0, axis1, slicer.toString())
            .toString();
        return mdx;
    }

    public Map getResultAsMap() {
        Map outerMap = new LinkedHashMap();
        Result r = executeImpl(null);
        if (r == null) { return outerMap; }

        List<Position> byParameters = r.getAxes()[0].getPositions();
        Locator locator = new Locator(2);

        for (int i0 = 0; i0 < byParameters.size(); i0++) {
            Map currentMap = outerMap;
            locator.reset(0, i0);

            int p = 0;
            for (p = 0; p < parameters.size() - 1; p++) {
                Member m = byParameters.get(i0).get(p);
                Object obj = parameters.get(p).extractValue(m);

                if (currentMap.containsKey(obj)) {
                    currentMap = (Map) currentMap.get(obj);
                } else {
                    Map newMap = new LinkedHashMap();
                    currentMap.put(obj, newMap);
                    currentMap = newMap;
                }
            }
            // Last one
            Member m = byParameters.get(i0).get(p);
            Object currentKey = parameters.get(p).extractValue(m);

            List<Object> metricData = new ArrayList<Object>(metrics.size());
            for (Metric metric : metrics) {
                metricData.add(locator.nextValue(r, 1, metric));
            }

            currentMap.put(currentKey, metricData.toArray());
        }
        return outerMap;
    }

    public Report execute() {
        Report report = new Report("OLAPQuery");
        Result r = executeImpl(report);
        if (r == null) { return report; }

        List<Position> byParameters = r.getAxes()[0].getPositions();
        Locator locator = new Locator(2);

        for (int i0 = 0; i0 < byParameters.size(); i0++) {
            locator.reset(0, i0);
            List<Object> rowData = new ArrayList<Object>(report.getColumns().size() + metrics.size());

            for (int p = 0; p < parameters.size(); p++) {
                Member m = byParameters.get(i0).get(p);
                Object obj = parameters.get(p).extractValue(m);
                if (obj instanceof Object[]) {
                    for (Object o : (Object[]) obj) {
                        rowData.add(o);
                    }
                } else {
                    rowData.add(obj);
                }
            }
            for (Metric metric : metrics) {
                rowData.add(locator.nextValue(r, 1, metric));
            }
            report.addRow(rowData.toArray());
        }
        return report;
    }

    protected void addColumn(Report report, Metric metric) {
        NumberFormat format = metric.getFormat(locale);
        Class dataType = null;
        boolean showTotal = true;
        boolean showAsAverage = false;

        Metric numerator = null;
        Metric divisor = null;
        double factor = 1.0;

        switch (metric) {
        case REQUESTS:
        case IMPRESSIONS:
        case BEACONS:
        case CLICKS:
        case CONVERSIONS:
        case TOTAL_VIEWS:
        case COMPLETED_VIEWS:
            dataType = Integer.class;
            break;
        case AVERAGE_DURATION:
            dataType = Integer.class;
            showAsAverage = true;
            break;
        case FILL_RATE:
            numerator = Metric.IMPRESSIONS;
            divisor = Metric.REQUESTS;
            break;
        case CTR:
            numerator = Metric.CLICKS;
            divisor = Metric.IMPRESSIONS;
            break;
        case CONVERSION_PERCENT:
            numerator = Metric.CONVERSIONS;
            divisor = Metric.CLICKS;
            break;
        case DEVICE_PERCENT_REQUESTS:
        case DEVICE_PERCENT_IMPRESSIONS:
        case LOCATION_PERCENT_REQUESTS:
        case LOCATION_PERCENT_IMPRESSIONS:
        case PLATFORM_PERCENT_IMPRESSIONS:
        case ENGAGEMENT_SCORE:
        case Q1_PERCENT:
        case Q2_PERCENT:
        case Q3_PERCENT:
        case Q4_PERCENT:
            showTotal = false;
            break;
        case COST_PER_CONVERSION:
            numerator = Metric.COST;
            divisor = Metric.CONVERSIONS;
            break;
        case COST_PER_VIEW:
          numerator = Metric.COST;
          divisor = Metric.TOTAL_VIEWS;
          break;
        case COST:
        case PAYOUT:
            dataType = Double.class;
            break;
        case ECPM_PUB:
            numerator = Metric.PAYOUT;
            divisor = Metric.IMPRESSIONS;
            factor = 1000.0;
            break;
        case ECPC_PUB:
            numerator = Metric.PAYOUT;
            divisor = Metric.IMPRESSIONS;
            break;
        case ECPM_AD:
            numerator = Metric.COST;
            divisor = Metric.IMPRESSIONS;
            factor = 1000.0;
            break;
        case ECPC_AD:
            numerator = Metric.COST;
            divisor = Metric.CLICKS;
            break;
        }
        if (showAsAverage) {
            report.addAverageColumn(metric.toString(), dataType, factor, format, showTotal);
        } else if (numerator != null) {
            report.addPercentColumn(metric.toString(), numerator.toString(), divisor.toString(), factor, format, showTotal);
        } else {
            report.addColumn(metric.toString(), dataType, format, showTotal);
        }
    }

    protected Result executeMDX(String mdx) {
        Connection c = MondrianDataSource.getInstance().getConnection();
        try {
            Query q = null;
            try {
                q = c.parseQuery(mdx);
            } catch (MondrianException me) {
                // This is expected for companies that do not yet have any
                // records in AdEventLog.
                LOG.log(Level.INFO, "Failed to parse MDX query: " + mdx, me);
                return null; // with nothing in it
            }
            return c.execute(q);
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error closing Mondrian connection", e);
            }
        }
    }
}
