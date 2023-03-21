/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.adapter;

import ai.quantumics.api.constants.QsConstants;
import ai.quantumics.api.model.Projects;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.amazonaws.services.glue.AWSGlue;
import com.amazonaws.services.glue.model.CreateDatabaseRequest;
import com.amazonaws.services.glue.model.CreateDatabaseResult;
import com.amazonaws.services.glue.model.DatabaseInput;
import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.model.CreateNamedQueryRequest;
@Slf4j
@Component
public class AwsResourceHandler {

  private final AmazonS3 amazonS3Client;
  private final AWSGlue awsGlueClient;
  private final AmazonAthena athenaClient;

  public AwsResourceHandler(AmazonS3 amazonS3ClientCi, AWSGlue awsGlueClientCi, AmazonAthena athenaClientCi) {
    amazonS3Client = amazonS3ClientCi;
    awsGlueClient = awsGlueClientCi;
    athenaClient = athenaClientCi;
  }

  private void createBucketInS3(final String s3BucketName) throws AmazonServiceException {
    String bucketLocation;
    if (amazonS3Client.doesBucketExistV2(s3BucketName)) {
      log.warn("Bucket with name {} already Exists ! ", s3BucketName);
    } else {
      amazonS3Client.createBucket(new CreateBucketRequest(s3BucketName));
      bucketLocation = amazonS3Client.getBucketLocation(new GetBucketLocationRequest(s3BucketName));
      log.info("New S3 bucket created ::: {}", bucketLocation);
    }
  }

  private void createDatabaseOperation(final String name) throws AmazonServiceException {
    final CreateDatabaseRequest createDatabaseRequest = new CreateDatabaseRequest();
    final DatabaseInput databaseInput = new DatabaseInput();
    databaseInput.setName(name);
    databaseInput.setDescription("Database create by QuantumSpark");
    createDatabaseRequest.setDatabaseInput(databaseInput);
    final CreateDatabaseResult createDatabase = awsGlueClient.createDatabase(createDatabaseRequest);
    log.info("Database created ::: {}", createDatabase.getSdkResponseMetadata().getRequestId());
  }

  private void createAthenaDatabaseOperation(final String name) throws AmazonServiceException {
    CreateNamedQueryRequest request = new CreateNamedQueryRequest()
            .withDatabase(name);
    athenaClient.createNamedQuery(request);
    //log.info("Database created ::: {}", createDatabase.getSdkResponseMetadata().getRequestId());
  }

  public boolean createProjectSequence(final Projects project) throws AmazonServiceException {
    final String hyphen = "-";
    final String bucketNameTemp = project.getBucketName();
    final String bucketNameRaw = bucketNameTemp + hyphen + QsConstants.RAW;
    final String bucketNameProcessed = bucketNameTemp + hyphen + QsConstants.PROCESSED;
    final String bucketNameEng = bucketNameTemp + hyphen + QsConstants.ENG;
    boolean result = false;
    try {
      createBucketInS3(bucketNameRaw);
      createBucketInS3(bucketNameProcessed);
      createBucketInS3(bucketNameEng);
      createDatabaseOperation(project.getRawDb());
      createDatabaseOperation(project.getProcessedDb());
      createDatabaseOperation(project.getEngDb());
      //createAthenaDatabaseOperation(project.getRawDb());
      //createAthenaDatabaseOperation(project.getProcessedDb());
      //createAthenaDatabaseOperation(project.getEngDb());
      result = true;
    } catch (final AmazonServiceException ex) {
      log.error(" Error Creating a project {}", ex.getMessage());
    }
    return result;
  }

  @SuppressWarnings("unused")
  private String s3StylePath(final String fileName) {
    return fileName.isEmpty() ? "" : "s3://" + fileName;
  }
}
