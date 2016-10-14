<%@page import="com.adfonic.domain.cache.ext.AdserverDomainCache"%>
<%@page import="com.adfonic.domain.cache.AdserverDomainCacheManager"%>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="org.apache.commons.collections.CollectionUtils" %>
<%@ page import="org.apache.commons.lang.exception.ExceptionUtils" %>
<%@ page import="com.quova.data._1.Ipinfo" %>
<%@ page import="com.adfonic.adserver.impl.BasicTargetingEngineImpl" %>
<%@ page import="com.adfonic.domain.cache.DomainCache" %>
<%@ page import="com.adfonic.domain.cache.DomainCacheManager" %>
<%@ page import="com.adfonic.geo.*" %>
<%@ page import="com.adfonic.quova.QuovaClient" %>
<%@ page import="com.adfonic.util.IpAddressUtils" %>
<%
response.setHeader("Expires", "0");
response.setHeader("Pragma", "No-Cache");
%>
<%@ include file="include/defines.jsp" %>


<%
    DomainCacheManager domainCacheMgr = appContext.getBean(DomainCacheManager.class);
AdserverDomainCacheManager adserverDomainCacheManager = appContext.getBean(AdserverDomainCacheManager.class);
DomainCache domainCache = domainCacheMgr.getCache();
AdserverDomainCache adserverDomainCache = adserverDomainCacheManager.getCache();
String errorMessage = "";
Set<Long> geoTargetIds = null;
Set<LocationTargetDto> locationTargets = null;

String creativeId = request.getParameter("creativeId");
if ("".equals(creativeId)) {
	creativeId = null;
}
if (creativeId != null) {
	creativeId = creativeId.trim();
	CreativeDto creative = null;
	try{
		Long creativeIdLong = Long.parseLong(creativeId);
		creative = adserverDomainCache.getCreativeById(creativeIdLong);
	}catch(NumberFormatException nfe){
		//It can be external ID
		creative = adserverDomainCache.getCreativeByExternalID(creativeId);
	}

	if(creative == null){
		errorMessage = "No such creative found "+creativeId;
	}else{
		geoTargetIds = creative.getSegment().getGeotargetIds();	
		locationTargets = creative.getSegment().getLocationTargets();	
	}
	
}
%>
<head>
<style type="text/css">
      #map {
        width: 800px;
        height: 500px;
      }
    </style>

    <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>

    <script type="text/javascript">
      /**
       * Called on the initial page load.
       */
       var map;
      function init() {
   	        var mapCenter = new google.maps.LatLng(0, 0);
   	        map = new google.maps.Map(document.getElementById('map'), {
   	          'zoom': 1,
   	          'center': mapCenter,
   	          'mapTypeId': google.maps.MapTypeId.ROADMAP
   	        });
    		
    	        
    	  <% if (locationTargets != null) {
  		  	double topLattitude = -90;
  		  	double leftLongitude = 180;
  		  	double bottomLattitude = 90;
  		  	double rightLongitude = -180;
  			;
  			%>
  			//radius is in miles so multiple it my 1.6
  			//and google map expects radius in meters so multiply it by 1000
  			<%
  			for(LocationTargetDto oneLocationTarget:locationTargets){
  				if(oneLocationTarget.getLatitude() > topLattitude){
  					topLattitude = oneLocationTarget.getLatitude();
  				}
  				if(oneLocationTarget.getLatitude() < bottomLattitude){
  					bottomLattitude = oneLocationTarget.getLatitude();
  				}
  				if(oneLocationTarget.getLongitude() < leftLongitude){
  					leftLongitude = oneLocationTarget.getLongitude();
  				}
  				if(oneLocationTarget.getLongitude() > rightLongitude){
  					rightLongitude = oneLocationTarget.getLongitude();
  				}
  				%>
  				
  				createRadiusPoint(<%=oneLocationTarget.getLatitude() %>,<%=oneLocationTarget.getLongitude() %>,<%=oneLocationTarget.getRadius()*1.6*1000 %>);
  				<%
  				
  			}
  			%>
  			
  			var topLeftLatLng = new google.maps.LatLng(<%=topLattitude %>,<%=leftLongitude %>);
  			var bottomRightLatLng = new google.maps.LatLng(<%=bottomLattitude %>,<%=rightLongitude %>);
  			var latLngBounds = new google.maps.LatLngBounds(topLeftLatLng,bottomRightLatLng);
  			map.fitBounds(latLngBounds);
  			mapCenter = map.getCenter();
  			
  			var draggingMarker = new google.maps.Marker({
  	            position: mapCenter,
  	            title: 'Drag Me',
  	            map: map,
  	            draggable: true
  	          });
  	        
  	        google.maps.event.addListener(draggingMarker, 'drag', function() {
  	            updateMarkerPosition(draggingMarker.getPosition());
  	          });
  		<%  
  		
  	  } %>
    	  

      }
      function updateMarkerPosition(latLng) {
    	  document.getElementById('info').innerHTML = [
    	    latLng.lat(),
    	    latLng.lng()
    	  ].join(', ');
    	}
      function createRadiusPoint(lattitude,longitude,radiusInMiles) {
          // Create a draggable marker which will later on be binded to a
          // Circle overlay.
          var marker = new google.maps.Marker({
            map: map,
            position: new google.maps.LatLng(lattitude, longitude),
            draggable: false,
            title: 'Location'
          });

          // Add a Circle overlay to the map.
          var circle = new google.maps.Circle({
            map: map,
            radius: radiusInMiles
          });

          // Since Circle and Marker both extend MVCObject, you can bind them
          // together using MVCObject's bindTo() method.  Here, we're binding
          // the Circle's center to the Marker's position.
          // http://code.google.com/apis/maps/documentation/v3/reference.html#MVCObject
          circle.bindTo('center', marker, 'position');
          
          
        }
      
      // Register an event listener to fire when the page finishes loading.
      google.maps.event.addDomListener(window, 'load', init);
    </script>
</head>

<%@ include file="include/top.jsp" %>

<p>
<form method="get" action="<%= request.getRequestURI() %>">
<b>Creative Id:</b>
<input type="text" size=20 name="creativeId" value="<%= creativeId == null ? "" : creativeId %>"/>
<%= errorMessage %>
<input type="submit" value="Go"/>
</form>
<div id="map"></div>
<div id="info"></div>
</p>



