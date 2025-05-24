package com.opsbeach.sharedlib.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.HttpRequest;
import com.google.cloud.tasks.v2.QueueName;
import com.google.cloud.tasks.v2.Task;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.FileNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.ApplicationConfig;
import com.opsbeach.sharedlib.utils.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCloudService {

    private final ResponseMessage responseMessage;
    private final ApplicationConfig applicationConfig;

    private Credentials getCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(System.getenv("BUCKET_SA_CREDENTIALS")));
    }

    private Storage getStorage() throws IOException {
        var projectId = applicationConfig.getGcloud().get(Constants.PROJECT_ID);
        return StorageOptions.newBuilder().setCredentials(getCredentials()).setProjectId(projectId).build().getService();
    }

    public void publish(String bucketName, String objectName, String filePath, byte[] content) {

        // Optional: set a generation-match precondition to avoid potential race
        // conditions and data corruptions. The request returns a 412 error if the
        // preconditions are not met.
        // For a target object that does not yet exist, set the DoesNotExist precondition.
        Storage.BlobTargetOption precondition = Storage.BlobTargetOption.doesNotExist();
        // If the destination already exists in your bucket, instead set a generation-match
        // precondition:
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try {
            if (Boolean.FALSE.equals(ObjectUtils.isEmpty(filePath))) {
                content = Files.readAllBytes(Paths.get(filePath));
            }
            var storage = getStorage();
            storage.delete(blobId);
            storage.create(blobInfo, content, precondition);
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.FILE_NOT_FOUND, e.getMessage()));
        }
    }

    public void downloadFile(String bucketName, String objectName, String downloadPath) {
        var blob = pull(bucketName, objectName);
        blob.downloadTo(Path.of(downloadPath));
    }

    public Blob pull(String bucketName, String objectName) {
        try {
            return getStorage().get(BlobId.of(bucketName, objectName));
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.FILE_NOT_FOUND, e.getMessage()));
        }
    }

    @Async
    public void pushRequestInTask(List<HttpRequest> httpRequests) throws IOException {
        CloudTasksClient client = CloudTasksClient.create();
        String projectId = applicationConfig.getGcloud().get(Constants.PROJECT_ID);
        String locationId = applicationConfig.getGcloud().get(Constants.LOCATION_ID);
        String queueId = applicationConfig.getGcloud().get(Constants.QUEUE_ID);
        // https://cloud.google.com/kubernetes-engine/docs/tutorials/authenticating-to-cloud-platform#importing_credentials_as_a_secret
        // set GOOGLE_APPLICATION_CREDENTIALS = path/to/cloud_tasks-sa.json env var when running from local
        // run ./ngrok http 7081 in terminal and set that address in here

        // Construct the fully qualified queue name.
        String queuePath = QueueName.of(projectId, locationId, queueId).toString();

        log.info("Google Task Queue Path : "+ queuePath);
        // Add your service account email to construct the OIDC token.
        // in order to add an authentication header to the request.

        httpRequests.forEach(httpRequest -> {
            Task.Builder taskBuilder = Task.newBuilder().setHttpRequest(httpRequest);
            // Send create task request.
            Task task = client.createTask(queuePath, taskBuilder.build());
            log.info("Task created: " + task.getName());
        });
        client.close();
    }
}