/*
 * Copyright (c) 2020. Quantumics.ai, http://quantumics.ai.
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and  limitations under the License.
 */

package ai.quantumics.api.user.util;

import ai.quantumics.api.user.model.Projects;
import ai.quantumics.api.user.model.QsFiles;
import ai.quantumics.api.user.req.ColumnMetaData;
import ai.quantumics.api.user.req.FileJoinColumnMetadata;
import ai.quantumics.api.user.req.Metadata;
import ai.quantumics.api.user.service.FileService;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class MetadataHelper {

  private static final Gson gson = new Gson();
  @Autowired private DbSessionUtil DBUtil;
  @Autowired private FileService fileService;
  
  /**
   * @param metaDataArrayNode
   * @param project
   * @param fileName
   * @param folderName
   */
  public void updateFileMetadata(
      final ArrayNode metaDataArrayNode,
      final Projects project,
      final String fileName,
      final int folderId) {
    try {
      DBUtil.changeSchema(project.getDbSchemaName());
      log.info("Looking for filename {}", fileName);
      //final int folderId = folderService.getFolderByName(folderName).getFolderId();
      final QsFiles fileObject = fileService.getFileByNameWithFolderId(fileName, folderId);
      log.info(
          "Length comparision {} vs {}",
          fileObject.getQsMetaData().length(),
          metaDataArrayNode.toString().length());
      if (fileObject.getQsMetaData().length() != metaDataArrayNode.toString().length()) {
        fileObject.setQsMetaData(metaDataArrayNode.toString());
        fileObject.setModifiedDate(new Date());
        fileService.saveFileInfo(fileObject);
        log.info("Metadata of file content updated : {}", metaDataArrayNode.toPrettyString());
      }
    } catch (final Exception e) {
      log.error("updateFileMetadata - Error - Filename record may not be found !" + e.getMessage());
    }
  }
  
	public String getJoinMetadata(int eventId, String file1MetadataJson, String file2MetadataJson) {
		List<Metadata> joinCols = new ArrayList<>();
		
		FileJoinColumnMetadata file1Metadata = gson.fromJson(file1MetadataJson, FileJoinColumnMetadata.class);
		FileJoinColumnMetadata file2Metadata = gson.fromJson(file2MetadataJson, FileJoinColumnMetadata.class);
		
		joinCols.addAll(file1Metadata.getMetadata());
		joinCols.addAll(file2Metadata.getMetadata());
		
		FileJoinColumnMetadata joinMetadata = new FileJoinColumnMetadata();
		joinMetadata.setEventId(eventId);
		joinMetadata.setMetadata(joinCols);

		return gson.toJson(joinMetadata, FileJoinColumnMetadata.class);
	}
	
	public String getFileMetadata(String file1MetadataJson, int eventId) throws Exception {
		List<ColumnMetaData> colsMetadata = parseColumnMetadataToList(file1MetadataJson);
		
		if(colsMetadata != null && !colsMetadata.isEmpty()) {
			FileJoinColumnMetadata fileJoinColumnMetadata = new FileJoinColumnMetadata();
			fileJoinColumnMetadata.setEventId(eventId);
			fileJoinColumnMetadata.setMetadata(getMetadataForFileorJoin(colsMetadata, eventId));
			
			return gson.toJson(fileJoinColumnMetadata);
		}
		
		return "";
	}
	
	private List<Metadata> getMetadataForFileorJoin(List<ColumnMetaData> colsMetadata, int eventId){
		List<Metadata> fileOrJoinMetadata = new ArrayList<>();
		
		for(ColumnMetaData colMetadata : colsMetadata) {
			Metadata metadata = new Metadata();
			String colName = colMetadata.getColumnName();
			metadata.setKey("a_"+eventId+"_"+colName);
			metadata.setValue(colName);
			
			fileOrJoinMetadata.add(metadata);
		}
		
		return fileOrJoinMetadata;
	}
	
	public List<ColumnMetaData> parseColumnMetadataToList(String fileMetadataStr) throws Exception {
		List<ColumnMetaData> colsMetadata = new ArrayList<>();

		if(fileMetadataStr != null && !fileMetadataStr.isEmpty()) {
			JSONArray jsonArray = new JSONArray(fileMetadataStr);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				ColumnMetaData column = new ColumnMetaData();
				column.setColumnName(obj.getString("column_name"));
				column.setDataType(obj.getString("data_type"));
				
				colsMetadata.add(column);
			}
		}

		return colsMetadata;
	}
	  
}
