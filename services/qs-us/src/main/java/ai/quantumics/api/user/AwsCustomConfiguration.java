/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.AmazonAthenaClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
public class AwsCustomConfiguration {

	@Value("${s3.credentials.accessKey}")
	private String s3AccessKey;

	@Value("${s3.credentials.secretKey}")
	private String s3SecretKey;

	@Value("${athena.credentials.accessKey}")
	private String athenaAccessKey;

	@Value("${athena.credentials.secretKey}")
	private String athenaSecretKey;

	@Value("${qs.cloud.region}")
	private String cloudRegion;


	@Value("${qs.file.max-size}")
	private long maxFileSize;

	private static final String SESSION_NAME = "qsaiConsumer";

	@Bean
	public AmazonAthena awsAthenaClient() {
		final BasicAWSCredentials awsAthenaCredentials = new BasicAWSCredentials(athenaAccessKey, athenaSecretKey);

		return AmazonAthenaClient.builder().withRegion(cloudRegion)
				.withCredentials(new AWSStaticCredentialsProvider(awsAthenaCredentials)).build();
	}

	@Bean
	@Primary
	public AmazonS3 awsS3Client() {
		final BasicAWSCredentials awsS3Credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
		ClientConfiguration config = new ClientConfiguration();
		config.setMaxConnections(2000);
		return AmazonS3ClientBuilder.standard().withRegion(cloudRegion).withClientConfiguration(config)
				.withCredentials(new AWSStaticCredentialsProvider(awsS3Credentials)).build();
	}

	@Bean
	RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
		final CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		multipartResolver.setMaxUploadSize(maxFileSize);
		return multipartResolver;
	}

	private MessageConverter jackson2MessageConverter(final ObjectMapper mapper) {
		final MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setObjectMapper(mapper);
		converter.setStrictContentTypeMatch(false);
		return converter;
	}

}
