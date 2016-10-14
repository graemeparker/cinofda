package com.byyd.middleware.account.service.jpa;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AdvertiserCloudInformation;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyResult;
import com.amazonaws.services.identitymanagement.model.CreateUserRequest;
import com.amazonaws.services.identitymanagement.model.CreateUserResult;
import com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteUserRequest;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest;
import com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.byyd.middleware.account.dao.AdvertiserCloudInformationDao;
import com.byyd.middleware.account.dao.AdvertiserDao;
import com.byyd.middleware.account.exception.AdvertiserCloudManagerException;
import com.byyd.middleware.account.service.AdvertiserCloudManager;
import com.byyd.middleware.iface.service.NotAutoScan;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;

@NotAutoScan
@Service("advertiserCloudManager")
public class AdvertiserCloudManagerJpaImpl extends BaseJpaManagerImpl implements AdvertiserCloudManager {
    
    private static final Logger LOG = Logger.getLogger(AdvertiserCloudManagerJpaImpl.class.getName());
    
    private static final String FOLDER_SEPARATOR = "/";

    @Autowired(required=false)
    private AdvertiserDao advertiserDao;
    
    @Autowired(required=false)
    private AdvertiserCloudInformationDao advertiserCloudInformationDao;
    
    @Autowired(required=false)
    @Qualifier("filemoverAWSCredentials")
    private AWSCredentials fileMoverCredentials;
    
    @Value("${filemover.bucket:byydfilemover}")
    private String fileMoverBucket;
    @Value("${filemover.env:dev}")
    private String fileMoverEnv;
    @Value("${filemover.users.prefix:_fm_}")
    private String fileMoverUserPrefix;
    @Value("${filemover.users.group:FileMover}")
    private String fileMoverGroup;
    
    private AmazonS3 s3Client = null;
    private AmazonIdentityManagement iamClient = null;
    
    public AdvertiserCloudManagerJpaImpl() {
    }
    
    @PostConstruct
    public void initialize(){
        if ((fileMoverCredentials==null) || 
            (StringUtils.isBlank(fileMoverCredentials.getAWSAccessKeyId())) ||
            (StringUtils.isBlank(fileMoverCredentials.getAWSSecretKey()))){
            LOG.info("AWS Credentials NOT found. We will try to connect to AWS using Instance Profile Credentials");
            s3Client = new AmazonS3Client();
            iamClient = new AmazonIdentityManagementClient();
        }else{
            LOG.log(Level.INFO, "Connecting to AWS using FileMover access key {0} and secret key", fileMoverCredentials.getAWSAccessKeyId());
            s3Client = new AmazonS3Client(fileMoverCredentials);
            iamClient = new AmazonIdentityManagementClient(fileMoverCredentials);
        }
        StringBuilder sb = new StringBuilder("AdvertiserCloudManager configuration:\n");
        sb.append("\t-File mover bucket: {0} \n");
        sb.append("\t-File mover env: {1} \n");
        sb.append("\t-Users prefix: {2} \n");
        sb.append("\t-Users group: {3} \n");
        LOG.log(Level.INFO, sb.toString(), new Object[]{fileMoverBucket, fileMoverEnv, fileMoverUserPrefix, fileMoverGroup});
    }
    
    //------------------------------------------------------------------------------------------
    // API to manage Cloud credentials 
    //------------------------------------------------------------------------------------------
    
    @Override
    public String getFileMoverBucketName(){
        return this.fileMoverBucket;
    }
    
    @Override
    @Transactional(readOnly=true)
    public AdvertiserCloudInformation getAdvertiserCloudInformation(Long advertiserId){
        AdvertiserCloudInformation advertiserCloudInformation = null;
        if (advertiserId!=null){
            advertiserCloudInformation = getAdvertiserCloudInformation(advertiserDao.getById(advertiserId));
        }
        return advertiserCloudInformation;
    }
    
    @Override
    @Transactional(readOnly=true)
    public AdvertiserCloudInformation getAdvertiserCloudInformation(Advertiser advertiser){
        AdvertiserCloudInformation advertiserCloudInformation = null;
        if (advertiser!=null){
            advertiserCloudInformation =  advertiserCloudInformationDao.getByAdvertiser(advertiser);
        }
        return advertiserCloudInformation;
    }
    
    @Override
    @Transactional(readOnly=false)
    public AdvertiserCloudInformation createAdvertiserCloudInformation(Advertiser advertiser) throws AdvertiserCloudManagerException{
        AdvertiserCloudInformation advertiserCloudInformation = null;
        
        try{
            // Get advertiser information from ToolsDB
            Advertiser persistedAdvetiser = null;
            if (advertiser!=null){
                persistedAdvetiser = advertiserDao.getById(advertiser.getId());
            }
            
            // If the advertiser exist we create a new cloud account and save the credentials
            if (persistedAdvetiser!=null){
                //check if the advertiser already have user credentials
                advertiserCloudInformation = getAdvertiserCloudInformation(persistedAdvetiser);
                
                if (advertiserCloudInformation==null){
                    // User provisioning (AmazonS3 and IAM)
                    advertiserCloudInformation = createAmazonUser(persistedAdvetiser);
                    
                    // Store user credentials in ToolsDB (ToolsDB)
                    advertiserCloudInformation = advertiserCloudInformationDao.create(advertiserCloudInformation);
                }
            }else{
                LOG.log(Level.FINE, "Can not create cloud information for advertiser {0}. Advertiser does not exist in our database", advertiser.getExternalID());
            }
        }catch(Exception ex){
            throw new AdvertiserCloudManagerException(ex);
        }
        
        return advertiserCloudInformation;
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteAdvertiserCloudInformation(Advertiser advertiser) throws AdvertiserCloudManagerException{

        try{
            // Get advertiser information from ToolsDB
            Advertiser persistedAdvetiser = null;
            if (advertiser!=null){
                persistedAdvetiser = advertiserDao.getById(advertiser.getId());
            }
        
            // If the advertiser exists we create cloud account and save the credentials
            if (persistedAdvetiser!=null){
                AdvertiserCloudInformation AdvertiserCloudInformation = getAdvertiserCloudInformation(persistedAdvetiser);
                
                if (AdvertiserCloudInformation!=null){                
                    // Delete user credentials from ToolsDB (ToolsDB)
                    advertiserCloudInformationDao.delete(AdvertiserCloudInformation);
                }
                
                // User decomission (AmazonS3 and IAM)
                deleteAmazonUser(persistedAdvetiser, (AdvertiserCloudInformation!=null ? AdvertiserCloudInformation.getPath() : getAmazonS3UserFolder(persistedAdvetiser)));
            }else{
                LOG.log(Level.FINE, "Can not create cloud information for advertiser {0}. User does not exist in our database", advertiser.getExternalID());
            }
        }catch(Exception ex){
            throw new AdvertiserCloudManagerException(ex);
        }   
    }

    private AdvertiserCloudInformation createAmazonUser(Advertiser advertiser) {
        // User name
        String userName = getAmazonUserName(advertiser);
        
        // S3 User folder
        String bucketName = getAmazonS3UserFolder(advertiser);
        
        //Create user  (Amazon IAM)
        LOG.log(Level.FINE, "Creating new cloud user {0} for advertiser {1}", new Object[]{userName, advertiser.getExternalID()});
        CreateUserRequest createUserRequest = new CreateUserRequest(userName);
        createUserRequest.setPath(FOLDER_SEPARATOR + this.fileMoverGroup + FOLDER_SEPARATOR + fileMoverEnv + FOLDER_SEPARATOR);
        CreateUserResult createUserResult = iamClient.createUser(createUserRequest);
        
        AccessKey accessKey = null;
        try{
            // Assign user to FileMover group  (Amazon IAM)
            LOG.log(Level.FINE, "Adding advertiser {0} to file mover group {1}", new Object[]{advertiser.getExternalID(), this.fileMoverGroup});
            iamClient.addUserToGroup(new AddUserToGroupRequest(this.fileMoverGroup, userName));
            
            // Create Access and Secret key (Amazon IAM)
            LOG.log(Level.FINE, "Generating credentials for advertiser {0}", advertiser.getExternalID());
            CreateAccessKeyRequest createAccessKeyRequest = new CreateAccessKeyRequest();
            createAccessKeyRequest.setUserName(userName);
            CreateAccessKeyResult createAccessKeyResult = iamClient.createAccessKey(createAccessKeyRequest);
            accessKey = createAccessKeyResult.getAccessKey();
            
            // Assign User Policy to be able to: list, write, read and delete objects inside this bucket
            String userPolicyName = getIAMUserPolicyName(userName);
            String userPolicyDocument = getIAMUserPolicyDocument(bucketName);
            LOG.log(Level.FINE, "Assigning user policy {0} to advertiser {1}", new Object[]{userPolicyName, advertiser.getExternalID()});
            LOG.log(Level.FINER, "Policy document assigned to advertiser {0}: {1}", new Object[]{advertiser.getExternalID(), userPolicyDocument});
            iamClient.putUserPolicy(new PutUserPolicyRequest(userName, userPolicyName, userPolicyDocument));
            
            // Create user bucket
            LOG.log(Level.FINE, "Creating bucket for advertiser {0}: {1}", new Object[]{advertiser.getExternalID(), bucketName});
            createS3Folder(bucketName);
        }catch(AmazonServiceException ex){
            // User was created, we will delete it (delete user)
            LOG.log(Level.FINE, "There was an error creating user cloud information for advertiser {0}", advertiser.getExternalID());
            deleteIamUser(advertiser);
            
            throw ex;
        }
        
        return new AdvertiserCloudInformation(advertiser, createUserResult.getUser().getArn(), accessKey.getAccessKeyId(), accessKey.getSecretAccessKey(), bucketName);
    }
    
    private void deleteAmazonUser(Advertiser advertiser, String folderName) {
        // Delete bucket (Amazon S3)
        deleteUserFolder(advertiser, folderName);
        
        // Delete user (Amazon IAM)
        deleteIamUser(advertiser);
    }
    
    private void deleteUserFolder(Advertiser advertiser, String folderName) {
        ObjectListing folderObjects = s3Client.listObjects(this.fileMoverBucket, folderName + FOLDER_SEPARATOR);
        for (S3ObjectSummary objectSummary : folderObjects.getObjectSummaries()) {
            LOG.log(Level.FINE, "Deleting object for advertiser {0}: {1}", new Object[]{advertiser.getExternalID(), objectSummary.getKey()});
            s3Client.deleteObject(this.fileMoverBucket, objectSummary.getKey());
        }
    }

    private void deleteIamUser(Advertiser advertiser) {
        // User name
        String userName = getAmazonUserName(advertiser);
        
        // Try to remove user from filemover group
        try{
            LOG.log(Level.FINE, "Removing advertiser {0} from file mover group {1}", new Object[]{advertiser.getExternalID(), this.fileMoverGroup});
            iamClient.removeUserFromGroup(new RemoveUserFromGroupRequest(fileMoverGroup, userName));
        }catch(NoSuchEntityException nse){
            LOG.log(Level.FINE, "Advertiser {0} does not belong to file mover group {1}", new Object[]{advertiser.getExternalID(), this.fileMoverGroup});
        }
        
        // Try to delete inline user policy
        String userPolicyName = getIAMUserPolicyName(userName);
        try{
            LOG.log(Level.FINE, "Deleting user policy {0} to advertiser {1}", new Object[]{userPolicyName, advertiser.getExternalID()});
            iamClient.deleteUserPolicy(new DeleteUserPolicyRequest(userName, getIAMUserPolicyName(userName)));
        }catch(NoSuchEntityException nse){
            LOG.log(Level.FINE, "User policy {0} is not present for advertiser {1}", new Object[]{userPolicyName, advertiser.getExternalID()});
        }
        
        // Try to delete user keys
        try{
            LOG.log(Level.FINE, "Deleting cloud user {0} for advertiser {1}", new Object[]{userName, advertiser.getExternalID()});
            ListAccessKeysRequest listAccessKeysRequest = new ListAccessKeysRequest();
            listAccessKeysRequest.setUserName(userName);
            ListAccessKeysResult listAccessKeyResult = iamClient.listAccessKeys(listAccessKeysRequest);
            for (AccessKeyMetadata accessKeyMetadata : listAccessKeyResult.getAccessKeyMetadata()){
                DeleteAccessKeyRequest deleteAccessKeyRequest = new DeleteAccessKeyRequest(userName, accessKeyMetadata.getAccessKeyId());
                iamClient.deleteAccessKey(deleteAccessKeyRequest);
            }
        }catch(NoSuchEntityException nse){
            LOG.log(Level.FINE, "Access keys do not exist for advertiser {1}", new Object[]{userName, advertiser.getExternalID()});
        }    
        
        // Try to delete the user
        try{
            LOG.log(Level.FINE, "Deleting cloud user {0} for advertiser {1}", new Object[]{userName, advertiser.getExternalID()});
            iamClient.deleteUser(new DeleteUserRequest(userName));
        }catch(NoSuchEntityException nse){
            LOG.log(Level.FINE, "Cloud user {0} does not exist for advertiser {1}", new Object[]{userName, advertiser.getExternalID()});
        }
    }
    
    private void createS3Folder(String foldername) {
        // Create metadata for your folder & set content-length to 0
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);

        // Create empty content
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

        // Send request to S3 to create folder
        s3Client.putObject(new PutObjectRequest(this.fileMoverBucket, foldername + FOLDER_SEPARATOR, emptyContent, metadata));
    }

    private String getAmazonS3UserFolder(Advertiser advertiser) {
        return this.fileMoverEnv + FOLDER_SEPARATOR + advertiser.getCompany().getExternalID() + FOLDER_SEPARATOR + advertiser.getExternalID();
    }

    private String getAmazonUserName(Advertiser advertiser) {
        return this.fileMoverUserPrefix + this.fileMoverEnv + "_" + advertiser.getExternalID();
    }
    
    private String getIAMUserPolicyName(String userName){
        return "filemover_user_policy_" + userName;
    }
    
    private String getIAMUserPolicyDocument(String path){
        return  "{"+
                " \"Version\": \"2012-10-17\","+
                " \"Statement\": ["+
                "  {"+
                "    \"Effect\": \"Allow\","+
                "    \"Action\": [\"s3:ListBucket\",\"s3:GetBucketLocation\"],"+
                "    \"Resource\": [\"arn:aws:s3:::" + this.fileMoverBucket + "\"],"+
                "    \"Condition\": {"+
                "         \"StringLike\": {"+
                "              \"s3:prefix\": [\"" + path + "/*\"]" +
                "          }"+
                "    }"+
                "  },"+
                "  {"+
                "    \"Effect\": \"Allow\","+
                "    \"Action\": [\"s3:PutObject\",\"s3:GetObject\",\"s3:DeleteObject\"],"+
                "    \"Resource\": \"arn:aws:s3:::" + this.fileMoverBucket + FOLDER_SEPARATOR + path +"/*\""+
                "  }"+
                " ]"+
                "}";
    }
}
