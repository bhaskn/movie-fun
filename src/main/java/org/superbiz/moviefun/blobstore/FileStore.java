package org.superbiz.moviefun.blobstore;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.*;
import java.util.Optional;


public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = new File(blob.name+".jpg");
//        if(targetFile.exists())
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            IOUtils.copy(blob.inputStream,outputStream);
        }

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File targetFile = new File(name+".jpg");
        if (targetFile.exists())
        {
            InputStream inputStream = new FileInputStream(targetFile);
            Blob blob =  new Blob(name, inputStream,"image/jpeg");
            Optional<Blob> optionalBlob  = Optional.of(blob);
            return optionalBlob;
        }
        return Optional.empty();
    }

    @Override
    public void deleteAll() {
        // ...
    }
}
