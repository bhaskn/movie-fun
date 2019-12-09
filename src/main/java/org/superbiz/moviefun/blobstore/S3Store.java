package org.superbiz.moviefun.blobstore;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.*;
import java.util.Optional;

public class S3Store implements BlobStore {

    private AmazonS3Client amazonS3Client;
    private String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.amazonS3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {

//        File targetFile = new File(blob.name+".jpg");

//        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
//            IOUtils.copy(blob.inputStream,outputStream);
//            amazonS3Client.putObject(photoStorageBucket, blob.name+".jpg" , targetFile);
//        }
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);
        PutObjectResult putObjectResult = amazonS3Client.putObject(photoStorageBucket, blob.name+".jpg", blob.inputStream, objectMetadata );
        String versionId = putObjectResult.getVersionId();
        String contentType = putObjectResult.getMetadata().getContentType();
        System.out.println("Object created in :" + photoStorageBucket + " with version " + versionId + " & with contentType: " + contentType);
        S3Object s3Object = amazonS3Client.getObject(photoStorageBucket, blob.name+".jpg");
        System.out.println("Got Object Key:" + s3Object.getKey() );
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        try
        {
            S3Object s3Object = amazonS3Client.getObject(photoStorageBucket, name+".jpg");
            Blob blob =  new Blob(name, s3Object.getObjectContent(),"image/jpeg");
            Optional<Blob> optionalBlob  = Optional.of(blob);
            return optionalBlob;
        }
        catch(SdkClientException e)
        {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {

    }
}
