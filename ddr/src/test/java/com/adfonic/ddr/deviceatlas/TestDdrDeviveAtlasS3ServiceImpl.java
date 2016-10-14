package com.adfonic.ddr.deviceatlas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.crypto.URIReferenceException;

import mobi.mtld.da.exception.JsonException;

import org.jmock.Expectations;
import org.junit.Test;

import com.adfonic.ddr.amazon.AmazonS3Service;
import com.adfonic.test.AbstractAdfonicTest;

@SuppressWarnings("rawtypes")
public class TestDdrDeviveAtlasS3ServiceImpl extends AbstractAdfonicTest {
    
    private static final File FILE = new File("DeviceAtlas.json");
    
    @Test
    public void testLoadDataFirstTime() throws IOException, URIReferenceException, URISyntaxException, JsonException{
        final AmazonS3Service amazon3ServiceStub = mock(AmazonS3Service.class, "amazon3service");
        final DdrDeviceAtlasS3ServiceImpl ddrDeviceAtlasS3ServiceImplMock = mock(DdrDeviceAtlasS3ServiceImpl.class, "ddrDeviceAtlasS3ServiceImpl");
        ddrDeviceAtlasS3ServiceImplMock.amazonS3Service = amazon3ServiceStub;
        final Calendar cal = Calendar.getInstance();
        final Date intialLastModificationDate = cal.getTime(); 
        DdrDeviceAtlasS3ServiceImpl.dateRef.set(intialLastModificationDate);
        final HashMap<String, String> initialTree = new HashMap<String, String>();
        DdrDeviceAtlasS3ServiceImpl.treeRef.set(null);
        ddrDeviceAtlasS3ServiceImplMock.amazonS3Service = amazon3ServiceStub;
        cal.add(Calendar.HOUR, 1);
        final Date finalLastModificationDate = cal.getTime(); 
        
        expect(new Expectations() {{
            oneOf (amazon3ServiceStub).getObjectModificationDate(ddrDeviceAtlasS3ServiceImplMock.s3Bucket, ddrDeviceAtlasS3ServiceImplMock.s3Key); will(returnValue(finalLastModificationDate));
            oneOf (amazon3ServiceStub).downloadFile(ddrDeviceAtlasS3ServiceImplMock.s3Bucket, ddrDeviceAtlasS3ServiceImplMock.s3Key, ddrDeviceAtlasS3ServiceImplMock.s3Compressed); will(returnValue(FILE));
            oneOf (ddrDeviceAtlasS3ServiceImplMock).getTree(FILE);will(returnValue(initialTree));
            oneOf (ddrDeviceAtlasS3ServiceImplMock).getTreeRevision(initialTree); will(returnValue(0));
            oneOf (ddrDeviceAtlasS3ServiceImplMock).getTreeGeneration(initialTree); will(returnValue(""));
        }});

        assertNull(DdrDeviceAtlasS3ServiceImpl.treeRef.get());
        ddrDeviceAtlasS3ServiceImplMock.loadData();
        assertNotNull(DdrDeviceAtlasS3ServiceImpl.treeRef.get());
    }
    
    @Test
    public void testLoadDataSecondTimeNoUpdate() throws IOException, URIReferenceException, URISyntaxException, JsonException{
        final AmazonS3Service amazon3ServiceStub = mock(AmazonS3Service.class, "amazon3service");
        final DdrDeviceAtlasS3ServiceImpl ddrDeviceAtlasS3ServiceImplMock = mock(DdrDeviceAtlasS3ServiceImpl.class, "ddrDeviceAtlasS3ServiceImpl");
        ddrDeviceAtlasS3ServiceImplMock.amazonS3Service = amazon3ServiceStub;
        final Calendar cal = Calendar.getInstance();
        final Date intialLastModificationDate = cal.getTime(); 
        DdrDeviceAtlasS3ServiceImpl.dateRef.set(intialLastModificationDate);
        final HashMap<String, String> initialTree = new HashMap<String, String>();
        DdrDeviceAtlasS3ServiceImpl.treeRef = new AtomicReference<Map>(initialTree);
        
        expect(new Expectations() {{
            oneOf (amazon3ServiceStub).getObjectModificationDate(ddrDeviceAtlasS3ServiceImplMock.s3Bucket, ddrDeviceAtlasS3ServiceImplMock.s3Key); will(returnValue(intialLastModificationDate)); // Assign this date as future value (same than current)
        }});
        
        assertEquals(DdrDeviceAtlasS3ServiceImpl.treeRef.get(), initialTree);
        ddrDeviceAtlasS3ServiceImplMock.loadData();
        assertEquals(DdrDeviceAtlasS3ServiceImpl.treeRef.get(), initialTree);
    }
    
    @Test
    public void testLoadDataSecondTimeWithUpdate() throws IOException, URIReferenceException, URISyntaxException, JsonException{
        final AmazonS3Service amazon3ServiceStub = mock(AmazonS3Service.class, "amazon3service");
        final DdrDeviceAtlasS3ServiceImpl ddrDeviceAtlasS3ServiceImplMock = mock(DdrDeviceAtlasS3ServiceImpl.class, "ddrDeviceAtlasS3ServiceImpl");
        ddrDeviceAtlasS3ServiceImplMock.amazonS3Service = amazon3ServiceStub;
        final Calendar cal = Calendar.getInstance();
        final Date intialLastModificationDate = cal.getTime(); 
        DdrDeviceAtlasS3ServiceImpl.dateRef.set(intialLastModificationDate);
        final HashMap<String, String> initialTree = new HashMap<String, String>();
        DdrDeviceAtlasS3ServiceImpl.treeRef = new AtomicReference<Map>(initialTree);
        final HashMap<String, String> finalTree = new HashMap<String, String>();
        cal.add(Calendar.HOUR, 1);
        final Date finalLastModificationDate = cal.getTime();
        
        expect(new Expectations() {{
            oneOf (amazon3ServiceStub).getObjectModificationDate(ddrDeviceAtlasS3ServiceImplMock.s3Bucket, ddrDeviceAtlasS3ServiceImplMock.s3Key); will(returnValue(finalLastModificationDate)); 
            oneOf (amazon3ServiceStub).downloadFile(ddrDeviceAtlasS3ServiceImplMock.s3Bucket, ddrDeviceAtlasS3ServiceImplMock.s3Key, ddrDeviceAtlasS3ServiceImplMock.s3Compressed); will(returnValue(FILE));
            oneOf (ddrDeviceAtlasS3ServiceImplMock).getTree(FILE);will(returnValue(finalTree));
            oneOf (ddrDeviceAtlasS3ServiceImplMock).getTreeRevision(finalTree);
            oneOf (ddrDeviceAtlasS3ServiceImplMock).getTreeGeneration(finalTree);
        }});
        
        assertEquals(DdrDeviceAtlasS3ServiceImpl.dateRef.get(), intialLastModificationDate);
        assertEquals(DdrDeviceAtlasS3ServiceImpl.treeRef.get(), initialTree);
        ddrDeviceAtlasS3ServiceImplMock.loadData();
        assertEquals(DdrDeviceAtlasS3ServiceImpl.dateRef.get(), finalLastModificationDate);
        assertEquals(DdrDeviceAtlasS3ServiceImpl.treeRef.get(), finalTree);
    }
}