package ai.quantumics.api.util;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.apache.http.HttpStatus;

public class BitBucketUtil {

	public static String createFileInRepository(final String bitBucketApiUrl, final String userName, 
			final String ApiPermissionToken, final String repositoryName, final String branchName, 
			final String fileName, String fileContent) {
		HttpURLConnection connection = null;
		try {
			//String urlToAccess = "https://api.bitbucket.org/2.0/repositories/"+userName+"/"+repositoryName+"/src/";
			String urlToAccess = String.format("%s/%s/%s/src/", bitBucketApiUrl, userName, repositoryName);
			URL repositoryUrl = new URL (urlToAccess);
			connection = (HttpURLConnection) repositoryUrl.openConnection();
			//For authentication
			connection.addRequestProperty("Authorization", "Basic "+Base64.getEncoder().encodeToString((userName+":"+ApiPermissionToken).getBytes("UTF-8"))); 
			connection.setRequestMethod("GET");			   
			connection.setDoOutput(true);
			//For setting the form data in the post request to add a new file and fill it with your data
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write("branch="+branchName+"&"+"/"+fileName+"="+fileContent.replace("+", "%2B"));
			out.close();
			connection.connect();

			if(connection.getResponseCode() == HttpStatus.SC_CREATED) {
				return String.format("%s%s/%s", urlToAccess, branchName, fileName);
			}

		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(connection != null)connection.disconnect();
		}
		return null;
	}
}
