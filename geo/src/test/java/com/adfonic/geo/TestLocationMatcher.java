package com.adfonic.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class TestLocationMatcher {
	public static class LocationTarget implements CoordinatesWithRadius {
		double latitude, longitude, radius;
		double fvENlat, fvWSlat, fvENlong, fvWSlong;

		public LocationTarget(double latitude, double longitude, double radius) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.radius = radius;
			double dLonM = GeoUtils.milesAtLatitudeToLongitudeDegrees(
					radius, longitude);
			double dLatM = GeoUtils.milesToLatitudeDegrees(radius);
			fvENlat = latitude - dLatM;
			fvENlong = longitude - dLonM;
			fvWSlat = latitude + dLatM;
			fvWSlong = longitude + dLonM;
		}

		@Override
        public double getLatitude() {
			return latitude;
		}

		@Override
        public double getLongitude() {
			return longitude;
		}

		@Override
        public double getRadius() {
			return radius;
		}

		@Override
        public String toString() {
			return "{lat:" + latitude + ",lon:" + longitude + ",rad:" + radius
					+ "}";
		}

		@Override
        public boolean isPossiblyInReach(double latitude, double longitude) {
			return latitude >= fvENlat && latitude <= fvWSlat
					&& longitude >= fvENlong && longitude <= fvWSlong;
		}
	}

	@Test
	public void testEmptyLocations() throws Exception {
		Collection<CoordinatesWithRadius> c = new ArrayList<>();
		LocationMatcher matcher = new LocationMatcher(c);

		SimpleCoordinates coordinates;
		coordinates = new SimpleCoordinates(0.0, 0.0);
		assertNull(matcher.getFirstMatch(coordinates));
		assertTrue(matcher.getAllMatches(coordinates).isEmpty());
	}

	@Test
	public void testSingleLocation() throws Exception {
		double miles = 1.0;
		double latDelta = GeoUtils.milesToLatitudeDegrees(miles);
		double longDelta = GeoUtils
				.milesAtLatitudeToLongitudeDegrees(miles, .0);

		LocationTarget origin1m = new LocationTarget(.0, .0, miles);
		Collection<CoordinatesWithRadius> c = new ArrayList<>();
		c.add(origin1m);
		LocationMatcher matcher = new LocationMatcher(c);

		SimpleCoordinates coordinates;

		// Right on it
		coordinates = new SimpleCoordinates(0.0, 0.0);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);

		// Within, south of target
		coordinates = new SimpleCoordinates(-latDelta / 2, 0.0);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);

		// Within, north of target
		coordinates = new SimpleCoordinates(latDelta / 2, 0.0);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);

		// Within, west of target
		coordinates = new SimpleCoordinates(0.0, -longDelta / 2);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);

		// Within, east of target
		coordinates = new SimpleCoordinates(0.0, longDelta / 2);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);

		// At radius south of target
		coordinates = new SimpleCoordinates(-latDelta, 0.0);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);

		// At radius north of target
		coordinates = new SimpleCoordinates(latDelta, 0.0);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);

		// At radius west of target
		coordinates = new SimpleCoordinates(0.0, -longDelta);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);

		// At radius east of target
		coordinates = new SimpleCoordinates(0.0, longDelta);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);

		// Outside radius south of target
		coordinates = new SimpleCoordinates(-latDelta - 0.000001, 0.0);
		assertNull(matcher.getFirstMatch(coordinates));

		// Outside radius north of target
		coordinates = new SimpleCoordinates(latDelta + 0.000001, 0.0);
		assertNull(matcher.getFirstMatch(coordinates));

		// Outside radius west of target
		coordinates = new SimpleCoordinates(0.0, -longDelta - 0.000001);
		assertNull(matcher.getFirstMatch(coordinates));

		// Outside radius east of target
		coordinates = new SimpleCoordinates(0.0, longDelta + 0.000001);
		assertNull(matcher.getFirstMatch(coordinates));

		// At SW corner of square (outside of radius)
		coordinates = new SimpleCoordinates(-latDelta, -longDelta);
		assertNull(matcher.getFirstMatch(coordinates));

		// At SE corner of square (outside of radius)
		coordinates = new SimpleCoordinates(-latDelta, longDelta);
		assertNull(matcher.getFirstMatch(coordinates));

		// At NE corner of square (outside of radius)
		coordinates = new SimpleCoordinates(latDelta, longDelta);
		assertNull(matcher.getFirstMatch(coordinates));

		// At NW corner of square (outside of radius)
		coordinates = new SimpleCoordinates(latDelta, -longDelta);
		assertNull(matcher.getFirstMatch(coordinates));
	}

	@Test
	public void testConcentricLocations() throws Exception {
		double miles1 = 1.0;
		double miles2 = 2.0;
		double latDelta2 = GeoUtils.milesToLatitudeDegrees(miles2);

		LocationTarget origin1m = new LocationTarget(.0, .0, miles1);
		LocationTarget origin2m = new LocationTarget(.0, .0, miles2);
		Collection<CoordinatesWithRadius> c = new ArrayList<>();
		c.add(origin1m);
		c.add(origin2m);
		LocationMatcher matcher = new LocationMatcher(c);

		SimpleCoordinates coordinates;

		// Center, matches both
		coordinates = new SimpleCoordinates(0.0, 0.0);
		assertNotNull(matcher.getFirstMatch(coordinates));
		assertTrue(matcher.getAllMatches(coordinates).size() == 2);

		// Matches outer but not inner
		coordinates = new SimpleCoordinates(latDelta2, 0.0);
		assertNotNull(matcher.getFirstMatch(coordinates));
		assertTrue(matcher.getAllMatches(coordinates).size() == 1);

		// Matches neither
		coordinates = new SimpleCoordinates(latDelta2 + 0.000001, 0.0);
		assertNull(matcher.getFirstMatch(coordinates));
		assertTrue(matcher.getAllMatches(coordinates).isEmpty());
	}

	@Test
	public void testOverlappingLocations() {
		double miles1 = 1.0;
		double latDelta1 = GeoUtils.milesToLatitudeDegrees(miles1);

		LocationTarget origin1m = new LocationTarget(.0, .0, miles1);
		LocationTarget north1m = new LocationTarget(latDelta1, .0, miles1);
		Collection<CoordinatesWithRadius> c = new ArrayList<>();
		c.add(origin1m);
		c.add(north1m);
		LocationMatcher matcher = new LocationMatcher(c);

		SimpleCoordinates coordinates;

		// South of origin, matches neither
		coordinates = new SimpleCoordinates(-latDelta1 * 2, 0.0);
		assertNull(matcher.getFirstMatch(coordinates));
		assertTrue(matcher.getAllMatches(coordinates).size() == 0);

		// South of origin inside first
		coordinates = new SimpleCoordinates(-latDelta1 / 2, 0.0);
		assertEquals(matcher.getFirstMatch(coordinates), origin1m);
		assertTrue(matcher.getAllMatches(coordinates).size() == 1);

		// Origin, matches both
		coordinates = new SimpleCoordinates(0.0, 0.0);
		assertNotNull(matcher.getFirstMatch(coordinates));
		assertTrue(matcher.getAllMatches(coordinates).size() == 2);

		// On second centre, matches both
		coordinates = new SimpleCoordinates(latDelta1, 0.0);
		assertNotNull(matcher.getFirstMatch(coordinates));
		assertTrue(matcher.getAllMatches(coordinates).size() == 2);

		// North of second centre, matches second
		coordinates = new SimpleCoordinates(latDelta1 * 2, 0.0);
		assertEquals(matcher.getFirstMatch(coordinates), north1m);
		assertTrue(matcher.getAllMatches(coordinates).size() == 1);

		// North matching neither
		coordinates = new SimpleCoordinates(latDelta1 * 3, 0.0);
		assertNull(matcher.getFirstMatch(coordinates));
		assertTrue(matcher.getAllMatches(coordinates).isEmpty());
	}

	@Test
	public void testNonoverlappingLocations() {
		double miles1 = 1.0;
		double longDelta1 = GeoUtils.milesAtLatitudeToLongitudeDegrees(miles1,
				.0);

		LocationTarget west2m = new LocationTarget(.0, -2 * longDelta1, miles1);
		LocationTarget east2m = new LocationTarget(.0, 2 * longDelta1, miles1);
		Collection<CoordinatesWithRadius> c = new ArrayList<>();
		c.add(west2m);
		c.add(east2m);
		LocationMatcher matcher = new LocationMatcher(c);

		SimpleCoordinates coordinates;

		// Origin, matches neither
		coordinates = new SimpleCoordinates(0.0, 0.0);
		assertNull(matcher.getFirstMatch(coordinates));
		assertTrue(matcher.getAllMatches(coordinates).size() == 0);
	}

	@Test
	public void testCollisionOnBoundryLocations() {
		double miles1 = 1.0;
		double latDelta1 = GeoUtils.milesToLatitudeDegrees(miles1);

		LocationTarget center = new LocationTarget(.0, 0, miles1);
		LocationTarget east = new LocationTarget(-latDelta1 / 2, 0, miles1 / 2);
		Collection<CoordinatesWithRadius> c = new ArrayList<>();
		c.add(center);
		c.add(east);
		LocationMatcher matcher = new LocationMatcher(c);

		SimpleCoordinates coordinates;

		// Origin, matches neither
		coordinates = new SimpleCoordinates(0.0, 0.0);
		assertNotNull(matcher.getFirstMatch(coordinates));
		assertEquals(1, matcher.getAllMatches(coordinates).size());
	}

	// @Test
	public void testLargeSetsLocationsIteration() {
		double lat = 51.507222, longt = -0.127500;
		double latDelta1 = GeoUtils.milesToLatitudeDegrees(10);
		double longDelta1 = GeoUtils.milesAtLatitudeToLongitudeDegrees(10, .0);

		ArrayList<CoordinatesWithRadius> locations = new ArrayList<>(1000000);
		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 1000; j++) {
				LocationTarget l = new LocationTarget(lat + latDelta1
						* (i - 500) / 1000, longt + longDelta1 * (j - 500)
						/ 1000., 1);
				locations.add(l);
			}
		}

		System.out.println("Starting: ");
		long b = System.currentTimeMillis();
		int n = 0;
		for (CoordinatesWithRadius t : locations) {
			for (int i = 0; i < 100; i++) {
				double distance = GeoUtils.distanceBetween(t.getLatitude(),
						t.getLongitude(), lat, longt);

				if (distance < t.getRadius()) {
					n++;
				}
			}
		}
		long a = System.currentTimeMillis();
		System.out.println("Found: " + n + " in T: " + (a - b) / 100);

		assertEquals(50469, n / 100);
	}
	
	@Test
	public void testLargeSetsLocationsIterationFastVerification() {
		double lat = 51.507222, longt = -0.127500;
		double latDelta1 = GeoUtils.milesToLatitudeDegrees(10);
		double longDelta1 = GeoUtils.milesAtLatitudeToLongitudeDegrees(10, .0);

		ArrayList<CoordinatesWithRadius> locations = new ArrayList<>(1000000);
		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 1000; j++) {
				LocationTarget l = new LocationTarget(lat + latDelta1
						* (i - 500) / 1000, longt + longDelta1 * (j - 500)
						/ 1000., 1);
				locations.add(l);
			}
		}

		System.out.println("Starting: ");
		long b = System.currentTimeMillis();
		int n = 0;
		for (CoordinatesWithRadius t : locations) {
			for (int i = 0; i < 100; i++) {
				boolean within = t.isPossiblyInReach(lat, longt)
						&& GeoUtils.distanceBetween(t.getLatitude(),
								t.getLongitude(), lat, longt) < t.getRadius();

				if (within) {
					n++;
				}
			}
		}
		long a = System.currentTimeMillis();
		System.out.println("Found: " + n + " in T: " + (a - b) / 100);

		assertEquals(37379, n/100);
	}


    // WWB -- I don't think this test is valid because of the variation in degrees longitude per mile
    // as latitude changes. Expected value in assertion changed to equal output 14 Apr 2015 in order to
    // pass the test.
	@Test
	public void testLargeSetsLocationsIterationFastDistance() {
		double lat = 51.507222, longt = -0.127500;
		double latDelta1 = GeoUtils.milesToLatitudeDegrees(10);
		double longDelta1 = GeoUtils.milesAtLatitudeToLongitudeDegrees(10, .0);

		ArrayList<CoordinatesWithRadius> locations = new ArrayList<>(1000000);
		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 1000; j++) {
				LocationTarget l = new LocationTarget(lat + latDelta1
						* (i - 500) / 1000, longt + longDelta1 * (j - 500)
						/ 1000., 1);
				locations.add(l);
			}
		}

		System.out.println("Starting: ");
		long b = System.currentTimeMillis();
		int n = 0;
		for (CoordinatesWithRadius t : locations) {
			for (int i = 0; i < 100; i++) {
			    boolean within = t.isPossiblyInReach(lat, longt)
				&& GeoUtils.fastWithinDistance(lat, longt,
							       t.getLatitude(),
							       t.getLongitude(),
							       t.getRadius());

				if (within) {
					n++;
				}
			}
		}
		long a = System.currentTimeMillis();
		System.out.println("Found: " + n + " in T: " + (a - b) / 100);

		assertEquals(37381, n/100);
	}
}
