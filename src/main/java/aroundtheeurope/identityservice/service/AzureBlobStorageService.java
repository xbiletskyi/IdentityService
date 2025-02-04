package aroundtheeurope.identityservice.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class AzureBlobStorageService {
    private final BlobContainerClient containerClient;

    public AzureBlobStorageService(
            @Value("${azure.storage.account-name}") String accountName,
            @Value("${azure.storage.account-key}") String accountKey,
            @Value("${azure.storage.container-name}") String containerName
    ) {
        String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                accountName, accountKey);

        this.containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String blobName = UUID.randomUUID() + "." + file.getOriginalFilename();
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        blobClient.upload(file.getInputStream(), file.getSize(), true);

        return blobClient.getBlobUrl();
    }

    public void deleteFile(String blobUrl) throws IOException {
        BlobClient blobClient = containerClient.getBlobClient(getBlobNameFromUrl(blobUrl));
        blobClient.delete();
    }

    private String getBlobNameFromUrl(String blobUrl) {
        return blobUrl.substring(blobUrl.lastIndexOf("/") + 1);
    }
}
