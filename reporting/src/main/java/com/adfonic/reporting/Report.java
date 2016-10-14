package com.adfonic.reporting;

import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVWriter;

import com.adfonic.reporting.Metric.Type;
import com.adfonic.util.CurrencyUtils;

@SuppressWarnings("rawtypes")
public class Report {
	
    private static final transient Logger LOG = Logger.getLogger(Report.class.getName());
	
    public class Column {
        private int index;
        private String header;
        private Class dataType;
        private Format format;
        private boolean showTotal;

        Column(int index, String header, Class dataType, Format format, boolean showTotal) {
            this.index = index;
            this.header = header;
            this.dataType = dataType;
            this.format = format;
            this.showTotal = showTotal;
        }

        public int getIndex() { return index; }
        public String getHeader() { return header; }
        public Class getDataType() { return dataType; }
        public Format getFormat() { return format; }
        public void setFormat(Format format) { this.format = format; }
        public boolean getShowTotal() { return showTotal; }

        public String formatValue(Object value) {
            if (value == null) {
                return "";
            }
            if (format == null) {
                return value.toString();
            }
            String output = null;
            try {
                output = format.format(value);
            } catch (IllegalArgumentException e) {
                // Be lenient in case there's some weird data
                output = value.toString();
            }
            return output;
        }

        /**
         * Output suitable for CSV.  Currency rounded to 2 fractional digits,
         * all other non-integer numbers to 4.
         */
        public String formatRawValue(Object value) {
            if (value == null) {
                return "";
            }
            if (format == null) {
                return value.toString();
            }
            String output = null;
            if (dataType == Integer.class) {
            	// Safer to output to a long, also totals returns a long
            	if (value instanceof String){ return (String) value; }
                output = String.valueOf(((Number) value).longValue());
            } else if (dataType == Long.class) {
                output = String.valueOf(((Number) value).longValue());
            } else if (dataType == Float.class || dataType == Double.class) {
                double dv = ((Number) value).doubleValue();
                // Treat infinite/uncomputable values as zero
                if (Double.isInfinite(dv) || Double.isNaN(dv)) {
                    dv = .0d;
                }
                BigDecimal bd = new BigDecimal(dv);
                if (format == CurrencyUtils.CURRENCY_FORMAT_USD) {
                    bd = bd.setScale(2, RoundingMode.HALF_UP);
                } else {
                    bd = bd.setScale(4, RoundingMode.HALF_UP);
                }
                output = bd.toPlainString();
            } else {
                try {
                    output = format.format(value);
                } catch (IllegalArgumentException e) {
                    // Be lenient in case there's some weird data
                    output = value.toString();
                }
            }
            return output;
        }

        /**
         * This routine does its best to coerce the input (which must
         * be an instance of java.lang.Number, in any case, to the requested
         * data type.
         */
        public Number calculateTotal() {
            if ((dataType == null) || (!Number.class.isAssignableFrom(dataType))) return null;
            long tl = 0l;
            float tf = .0f;
            double td = .0;
            BigDecimal tbd = BigDecimal.ZERO;

            for (Row r : rows) {
                Number num = (Number) r.getObject(index);
                if (num == null) continue;
                if (dataType == Integer.class) {
//                    ti += num.intValue();   // Possible over flow here, changing hold longs in total for ticket AD-5
                	tl += num.intValue();
                } else if (dataType == Long.class) {
                    tl += num.longValue();
                } else if (dataType == Float.class) {
                    tf += num.floatValue();
                } else if (dataType == Double.class) {
                    td += num.doubleValue();
                } else if (dataType == BigDecimal.class) {
                    double dv = num.doubleValue();
                    // Treat infinite/uncomputable values as zero
                    if (Double.isInfinite(dv) || Double.isNaN(dv)) {
                        dv = .0d;
                    }
                    tbd = tbd.add(new BigDecimal(dv));
                }
            }
            if (dataType == Integer.class) {
                return tl; // AD-5 return a long when summing integers
            } else if (dataType == Long.class) {
                return tl;
            } else if (dataType == Float.class) {
                return tf;
            } else if (dataType == Double.class) {
                return td;
            } else if (dataType == BigDecimal.class) {
                return tbd;
            }
            return null;
        }
        
        public boolean isPercentageColumn() {
            boolean isPercentage = false;
            if (this instanceof PercentColumn){
                isPercentage = true;
            }else{
                Metric metric = null;
                try{
                    metric = Metric.valueOf(header);
                } catch(IllegalArgumentException ex) {
                    // do nothing, the column is not a metric
                }
                
                if (metric!=null){
                    isPercentage = metric.getType() == Type.PERCENT;
                }
            }
            return isPercentage;
        }
    }

    /** Special class that can deal with percent total calculations relative to other columns. */
    public class PercentColumn extends Column {
        private String numerator;
        private String divisor;
        private double factor;

        PercentColumn(int index, String header, Format format, String numerator, String divisor, double factor, boolean showTotal) {
            super(index, header, Double.class, format, showTotal);
            this.numerator = numerator;
            this.divisor = divisor;
            this.factor = factor;
        }

        public String getNumerator() {
            return numerator;
        }

        @Override
        public Number calculateTotal() {
            Number num = getColumn(numerator).calculateTotal();
            Number div = getColumn(divisor).calculateTotal();
            if (num == null || div == null || div.doubleValue() == .0) {
                return 0.0;
            }
            return factor * num.doubleValue() / div.doubleValue();
        }
    }

    /** Special class that can deal with averages */
    public class AverageColumn extends Column {
        private int index;
        private Double factor;

        AverageColumn(int index, String header, Double factor, Format format, boolean showTotal) {
            super(index, header, Integer.class, format, showTotal);
            this.index = index;
            this.factor = factor;
        }

        @Override
        public Number calculateTotal() {
            double sumOfAverages = 0;
            int numOfAverages = 0;
            for (Row r : rows) {
                Number num = (Number) r.getObject(index);
                if (num == null) {
                    continue;
                }else if (num instanceof Double && ((Double)num).isInfinite()) {
                    continue;
                }
                sumOfAverages += num.doubleValue();
                ++numOfAverages;
            }
            return factor * sumOfAverages / numOfAverages;
        }
    }

    public class Row {
        private Object[] data;
        private String id;
        
        Row(Object[] data) {
            this.data = data;
            id = UUID.randomUUID().toString();
        }

        public Object getObject(int index) {
            return data[index];
        }
        
        public Object getData(int index) {
            Object value = null; 
            if ((columns.get(index).isPercentageColumn()) &&
               (!((PercentColumn) columns.get(index)).getNumerator().equals(Metric.COST.name()))){
                BigDecimal bd = new BigDecimal(((Number) data[index]).doubleValue());
                value = bd.multiply(new BigDecimal(100)).doubleValue();
            }else{
                value = data[index];
            }
            return value;
        }

        public String getFormattedObject(int index) {
            return columns.get(index).formatValue(data[index]);
        }

        public String[] getCells() {
              String[] cells = new String[columns.size()];
              int i = 0;
              for (Column c : columns) {
            	  try {
                    cells[i] = c.formatValue(data[i]);
                   i++;
            	  } catch (Exception e){
            		  cells[i] = "missing data";
            	  }
              }
              return cells;
        }

        public String[] getRawCells() {
            String[] cells = new String[columns.size()];
            int i = 0;
            for (Column c : columns) {
            	try {
                  cells[i] = c.formatRawValue(data[i]);
            	} catch (Exception e){
            		cells[i] = "missing data";
            	}
                i++;
            }
            return cells;
        }
        
        public String getCellFormattedValue(Integer i){
            String value = null;
            try {
                value = columns.get(i).formatValue(data[i]);
            } catch (Exception e){
                value = "missing data";
            }
            return value;
        }
        
        public String getId() {
        	return id;
        }
    }

    private String name;
    private List<Column> columns;
    private List<Row> rows;

    public Report(String name) {
        this.name = name;
        this.columns = new ArrayList<Column>();
        this.rows = new ArrayList<Row>();
    }
    
    public String getName() {
        return name;
    }

    /** Makes a column that cannot be totaled and uses the default format. */
    public void addColumn(String header) {
        addColumn(header, null, null, false);
    }

    /** Makes a column that may be totaled and is formatted with the given format object. */
    public void addColumn(String header, Class dataType, Format format, boolean showTotal) {
        columns.add(new Column(columns.size(), header, dataType, format, showTotal));
        rows.clear();
    }

    /** Makes a column that may be totaled in reference to other columns. */
    public void addPercentColumn(String header, String numerator, String divisor, double factor, Format format, boolean showTotal) {
        columns.add(new PercentColumn(columns.size(), header, format, numerator, divisor, factor, showTotal));
    }

    /** Makes a column that returns the correct averaged values. */
    public void addAverageColumn(String header, Class dataType, double factor, Format format, boolean showTotal) {
        columns.add(new AverageColumn(columns.size(), header, factor, format, showTotal));
        rows.clear();
    }

    // Returns the first column that has a matching header
    public Column getColumn(String header) {
        for (Column c : columns)  {
            if (c.getHeader().equals(header)) {
                return c;
            }
        }
        return null;
    }

    public void addRow(Object... rowData) {
        rows.add(new Row(rowData));
        total = null;
    }

    public List<Row> getRows() { return rows; }
    public List<Column> getColumns() { return columns; }

    protected Row total;

    /**
     * Returns a row containing the totals (where computable).
     * The number of cells in a total row may be different from the
     * table proper as not all columns can or should be totaled.
     */
    public Row getTotal() {
        if (total == null) {
            // Lazy instantiation
            Object[] data = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).getShowTotal()) {
                	try {
                      data[i] = columns.get(i).calculateTotal();
                	} catch (Exception e){
                		LOG.severe(name + " getTotals (" + columns.get(i).header + "): " + e);
                		data[i] = new Integer(0);
                	}
                }
            }
            total = new Row(data);
        }
        return total;
    }

    protected Row headers;

    /** Retrieves the list of column headers as a Row. */
    public Row getHeaders() {
        if (headers == null) {
            Object[] data = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                data[i] = columns.get(i).getHeader();
            }
            headers = new Row(data);
        }
        return headers;
    }

    public void writeCSV(Writer writer) {
        CSVWriter csv = new CSVWriter(writer);
        // Write headers
        csv.writeNext(getHeaders().getCells());

        // Write data
        for (Row row : rows) {
            csv.writeNext(row.getRawCells());
        }
        // Write totals
        csv.writeNext(getTotal().getRawCells());
    }
}
