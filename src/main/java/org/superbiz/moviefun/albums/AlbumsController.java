package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.FileStore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;

    @Autowired
    BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile, format("covers/%d", albumId));

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException, ClassNotFoundException {
        Path coverFilePath = getExistingCoverPath(albumId);
        byte[] imageBytes = readAllBytes(coverFilePath);
        HttpHeaders headers = createImageHttpHeaders(coverFilePath, imageBytes);

        return new HttpEntity<>(imageBytes, headers);
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, String targetFileName) throws IOException {

        // make a blob out of uploadedFile with name as targetFile

        Blob blob = new Blob(targetFileName,uploadedFile.getInputStream(),uploadedFile.getContentType());
        // use FileStore to save Blob

//        FileStore fileStore = new FileStore();
        blobStore.put(blob);
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d.jpg", albumId);
        File file = new File(coverFileName);
        OutputStream outputStream = null;
        InputStream blobInputStream = null;
        try {
//            FileStore fileStore = new FileStore();
            Optional<Blob> optionalBlob=blobStore.get(coverFileName);
            Blob blob;
            if(optionalBlob.isPresent()) {
                blob = optionalBlob.get();
                outputStream = new FileOutputStream(file);
                IOUtils.copy(blob.inputStream,outputStream);
            }
            else
            {
                Class clazz = Class.forName("org.superbiz.moviefun.albums.AlbumsController");
                ClassLoader classLoader = clazz.getClassLoader();
                file = new File(classLoader.getResource("default-cover.jpg").getFile());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (blobInputStream!=null) {
                try {
                    blobInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file;
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException, ClassNotFoundException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
//            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
            Class clazz = Class.forName("org.superbiz.moviefun.albums.AlbumsController");
            ClassLoader classLoader = clazz.getClassLoader();
            coverFile = new File(classLoader.getResource("default-cover.jpg").getFile());
            coverFilePath = coverFile.toPath();
        }

        return coverFilePath;
    }
}
