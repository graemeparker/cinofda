package com.adfonic.olap;

import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;

import mondrian.olap.Connection;
import mondrian.olap.Member;
import mondrian.olap.MondrianException;
import mondrian.olap.Position;
import mondrian.olap.Query;
import mondrian.olap.Result;

import com.adfonic.test.SpringTestBase;

public class TestOLAP extends SpringTestBase {
    private static class Locator {
        private int[] location;
        
        public Locator(int dimensions) {
            location = new int[dimensions];
        }
        
        public Object nextValue(Result result, int dimension) {
            Object out = result.getCell(location).getValue();
            ++location[dimension];
            return out;
        }
        
        public void reset(int dimension, int value) {
            location[dimension++] = value;
            while (dimension < location.length) {
                location[dimension++] = 0;
            }
        }
    }

//    @org.junit.Ignore
//    @Test public void testWTF() {
//        Result r;
//
//        r = executeMDX("with member [GmtTime].[slicer0] as 'Aggregate({([GmtTime].[2011].[3].[24] : [GmtTime].[2011].[3].[25])})' select NON EMPTY {NonEmptyCrossJoin(Distinct(Descendants([Advertiser].[6330].[11017], 1.0)), [Publisher].[Publication].Members)} ON COLUMNS, {[Measures].[IMPRESSIONS], [Measures].[CLICKS], [Measures].[CTR], [Measures].[ECPM_AD], [Measures].[ECPC_AD], [Measures].[CONVERSIONS], [Measures].[CONVERSION_PERCENT], [Measures].[COST_PER_CONVERSION], [Measures].[COST]} ON ROWS from [Ads] where [GmtTime].[slicer0]");
//        printResult(r, 2,9);
//
//        /*
//        // UK platform breakdown
//        r = executeMDX("select non empty Platform.children on 0, {Measures.PLATFORM_PERCENT_LOCATION} on 1 from Ads where Country.[150]");
//        printResult(r, 1,1);
//
//        // Top 5 devices worldwide
//	r = executeMDX("with member Measures.Percent as Measures.total / (Measures.total, Device.CurrentMember.Parent.Parent) select non empty TopCount(Device.Model.Members,5,Measures.Percent) on 0, Measures.Percent on 1 from Ads");
//        printResult(r,1,0);
//
//        // Top 5 countries
//	r = executeMDX("with member Measures.Percent as Measures.total / (Measures.total, Location.CurrentMember.Parent.Parent) select non empty TopCount(Location.Country.Members,5,Measures.Percent) on 0, Measures.Percent on 1 from Ads");
//        printResult(r, 1,1);
//        */
//    }

    void printResult(Result r, int numParams, int numMetrics) {
        System.out.println("Result = ");
        PrintWriter pw = new PrintWriter(System.out);
        r.print(pw);
        pw.flush();

	List<Position> byParameters = r.getAxes()[0].getPositions();
	Locator locator = new Locator(2);

	for (int i0 = 0; i0 < byParameters.size(); i0++) {
	    locator.reset(0, i0);
            //	    List<Object> rowData = new ArrayList<Object>(numParams + numMetrics);

	    for (int p = 0; p < numParams; p++) {
		Member m = byParameters.get(i0).get(p);
                System.out.println("Param " + p + " Value = " + m.getCaption());
                /*
		Object obj = parameters.get(p).extractValue(m, pm);
		if (obj instanceof Object[]) {
		    for (Object o : (Object[]) obj) {
			rowData.add(o);
		    }
		} else {
		    rowData.add(obj);
		}
                */
	    }
            /*
	    for (Metric metric : metrics) {
		rowData.add(locator.nextValue(r, 1, metric));
	    }
	    report.addRow(rowData.toArray());
            */
            for (int mc = 0; mc < numMetrics; mc++) {
                System.out.println("Metric " + mc + " Value = " + locator.nextValue(r,1));
            }
	}
    }
    /*
    @Test public void testCampaign() {
        executeMDX("with member Advertiser.[slicer0] as Aggregate({Advertiser.Campaign.[875]}) select non empty {AdvertiserTime.[2010].[8].[2]:AdvertiserTime.[2010].[8].[8]} on 0, {Measures.IMPRESSIONS,Measures.CLICKS,Measures.CTR,Measures.ECPM_AD,Measures.ECPC_AD,Measures.COST} on 1 from Ads where (Advertiser.[slicer0])");
    }

    @Test public void testPublication() {
        executeMDX("with member Advertiser.[slicer0] as Aggregate({Publisher.Publication.[1]}) select non empty {PublisherTime.[2010].[8].[2]:PublisherTime.[2010].[8].[8]} on 0, {Measures.REQUESTS, Measures.IMPRESSIONS,Measures.CLICKS,Measures.CTR,Measures.ECPM_PUB,Measures.ECPC_PUB,Measures.PAYOUT} on 1 from Ads where (Advertiser.[slicer0])");
    }

    @Test public void testCampaignByCountry() {
        executeMDX("with member Advertiser.[slicer0] as Aggregate({Advertiser.Company.[2871]}) select non empty {NonEmptyCrossJoin(Location.Country.members,AdvertiserTime.[2010].[8].[25]:AdvertiserTime.[2010].[8].[25])} on 0, {Measures.IMPRESSIONS,Measures.LOCATION_PERCENT_IMPRESSIONS,Measures.CLICKS,Measures.CTR,Measures.ECPM_AD,Measures.ECPC_AD,Measures.COST} on 1 from Ads where (Advertiser.[slicer0])");
    }

    @Test public void testCampaignByModel() {
        executeMDX("with member Advertiser.[slicer0] as Aggregate({Advertiser.Company.[2871]}) select non empty {NonEmptyCrossJoin(Device.Model.members,AdvertiserTime.[2010].[8].[25]:AdvertiserTime.[2010].[8].[25])} on 0, {Measures.IMPRESSIONS,Measures.DEVICE_PERCENT_IMPRESSIONS,Measures.CLICKS,Measures.CTR,Measures.ECPM_AD,Measures.ECPC_AD,Measures.COST} on 1 from Ads where (Advertiser.[slicer0])");
    }
    */
    /*
    @Test public void testPublicationByDeviceAndLocation() {
        executeMDX("with member Publisher.[slicer0] as Aggregate({Publisher.Publication.[1397]}) select non empty {NonEmptyCrossJoin(NonEmptyCrossJoin(Location.Country.members,Device.Model.members),PublisherTime.[2010].[8].[25]:PublisherTime.[2010].[8].[25])} on 0, {Measures.REQUESTS,Measures.IMPRESSIONS,Measures.FILL_RATE,Measures.CLICKS,Measures.CTR,Measures.ECPM_PUB,Measures.PAYOUT,Measures.DEVICE_PERCENT_REQUESTS} on 1 from Ads where (Publisher.[slicer0])");
    }
    */
    /*
    @Test public void testAdSlotReport() {
        executeMDX("with member PublisherTime.[slicer0] as Aggregate({PublisherTime.[2010].[9].[1]:PublisherTime.[2010].[9].[1]}) select non empty {Distinct(Descendants(Publisher.Company.[380],2))} on 0, {Measures.REQUESTS,Measures.IMPRESSIONS,Measures.FILL_RATE,Measures.CLICKS,Measures.CTR,Measures.ECPM_PUB,Measures.PAYOUT} on 1 from Ads where (PublisherTime.[slicer0])");
    }
    */
    @SuppressWarnings("deprecation")
    protected Result executeMDX(String mdx) {
	Connection c = MondrianDataSource.getInstance().getConnection();
        try {
            Query q = null;
            try {
                q = c.parseQuery(mdx);
            } catch (MondrianException me) {
                // This is expected for companies that do not yet have any
                // records in AdEventLog.
                logger.log(Level.INFO, "Failed to parse MDX query: " + mdx, me);
                return null; // with nothing in it
            }
            return c.execute(q);
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error closing Mondrian connection", e);
            }
        }
    }
}
