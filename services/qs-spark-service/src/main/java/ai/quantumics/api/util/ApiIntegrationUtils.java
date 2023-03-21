package ai.quantumics.api.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import ai.quantumics.api.req.ApiIntegrationRequest;
import ai.quantumics.api.vo.ApiIntegrationResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApiIntegrationUtils {
  
  @Autowired
  private Environment applicationProperties;
  
  public File processApiResponse(final String fileName, final String apiResponse) throws JsonMappingException, JsonProcessingException, IOException{
    JsonNode jsonTree = parseApiResponse(apiResponse);
    
    JsonNode firstObject = jsonTree.elements().next();
    
    CsvSchema csvSchema = generateSchema(firstObject);
    
    
    File csvFile = getCsv(fileName, jsonTree, csvSchema);
    
    return csvFile;
  }

  /**
   * @param fileName
   * @param jsonTree
   * @param csvSchema
   * @param csvMapper
   * @return
   * @throws IOException
   * @throws JsonGenerationException
   * @throws JsonMappingException
   */
  public File getCsv(final String fileName, JsonNode jsonTree, CsvSchema csvSchema) throws IOException, JsonGenerationException, JsonMappingException {
    CsvMapper csvMapper = new CsvMapper();
    String tmpdir = System.getProperty("java.io.tmpdir"); // Newly created file is stored in the Default temp location of the OS..
    log.info("Temp file path: {}", tmpdir);
    File csvFile = new File(tmpdir+File.separator+fileName);
    csvMapper.writerFor(JsonNode.class)
      .with(csvSchema)
      .writeValue(csvFile, jsonTree);
    return csvFile;
  }

  /**
   * @param apiResponse
   * @return
   * @throws JsonProcessingException
   * @throws JsonMappingException
   */
  public JsonNode parseApiResponse(final String apiResponse)
      throws JsonProcessingException, JsonMappingException {
    ObjectMapper mapper = new ObjectMapper();
    ApiIntegrationResponse response = mapper.readValue(apiResponse, ApiIntegrationResponse.class);
    List<Object> dataRecords = null;
    String dataRecordsStr = null;
    
    if(response != null) {
      dataRecords = response.getData();
    }
    
    // Converting the data into String for easy formatting...
    dataRecordsStr = mapper.writeValueAsString(dataRecords);
    log.info("Data records string: {}", dataRecordsStr);
    
    JsonNode jsonTree = mapper.readTree(dataRecordsStr);
    return jsonTree;
  }

  /**
   * @param firstObject
   * @return
   */
  public CsvSchema generateSchema(JsonNode firstObject) {
    Builder csvSchemaBuilder = CsvSchema.builder();
    // Reading the first record to fetch the Field Names used subsequently as CSV Column headers...
    firstObject.fieldNames().forEachRemaining(fieldName -> {csvSchemaBuilder.addColumn(fieldName);} );
    CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
    return csvSchema;
  }
  
  public File processHubSpotApiResponse(final String fileName, final String...pagingResponse) throws IOException {
    ArrayNode results = (ArrayNode) getHubspotEntityList(pagingResponse[0], "results");
    JsonNode properties = results.get(0).at(applicationProperties.getProperty("hubsport.api.responsePath"));
    CsvSchema csvSchema = generateSchema(properties);
    
    if(pagingResponse.length >1) {
      results.addAll((ArrayNode) getHubspotEntityList(pagingResponse[1], "results"));
     }
      List<JsonNode> data = new ArrayList<>();
      results.elements().forEachRemaining((result) -> {
        JsonNode propertiesLocal = result.get("properties");
        data.add(propertiesLocal);
      });
      ArrayNode propertiesArray = new ArrayNode(JsonNodeFactory.instance, data);
      File csvFile = getCsv(fileName, propertiesArray, csvSchema);
    
    return csvFile;
  }

  /**
   * @param apiResponse
   * @return
   * @throws JsonProcessingException
   * @throws JsonMappingException
   */
  public JsonNode getHubspotEntityList(final String apiResponse, String fieldName)
      throws JsonProcessingException, JsonMappingException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonTree = objectMapper.readTree(apiResponse);
    JsonNode node = jsonTree.get(fieldName);
    JsonNode results = (node instanceof ArrayNode) ? (ArrayNode) jsonTree.get("results")
                        : node;
    return results;
  }
  
  /*
   * public static void main(String args[]) throws JsonProcessingException, IOException {
   * ObjectMapper mapper = new ObjectMapper(); File file = new
   * File("src/main/resources/connector/hubspot_company_response.json"); JsonNode jsonTree =
   * mapper.readTree(file); Builder csvSchemaBuilder = CsvSchema.builder(); ArrayNode results =
   * (ArrayNode) jsonTree.get("results"); JsonNode properties = results.get(0).at("/properties");
   * List<JsonNode> data = new ArrayList<>();
   * 
   * results.elements().forEachRemaining((result) -> { JsonNode propertiesLocal =
   * result.get("properties"); data.add(propertiesLocal); });
   * properties.fieldNames().forEachRemaining((fieldName) -> {
   * {csvSchemaBuilder.addColumn(fieldName);} });
   * 
   * ArrayNode propertiesArray = new ArrayNode(JsonNodeFactory.instance, data); CsvSchema csvSchema
   * = csvSchemaBuilder.build().withHeader(); CsvMapper csvMapper = new CsvMapper();
   * 
   * csvMapper.writerFor(JsonNode.class) .with(csvSchema) .writeValue(new
   * File("src/main/resources/contacts.csv"), propertiesArray); }
   */
  
  public String resolveHubSpotApiParameters(ApiIntegrationRequest apiIntegrationRequest) {
    
    return String.format("%s/crm/v3/objects/%s?hapikey=%s&properties=%s&archived=false&limit=100", 
        apiIntegrationRequest.getApiEndPoint(),apiIntegrationRequest.getEntity(),
        apiIntegrationRequest.getApiKey(),applicationProperties.getProperty("hubsport.api.selectitems"));
    
  }

  public boolean hasNext(String responseBody) throws IOException {
    JsonNode paging = getHubspotEntityList(responseBody, "paging");
    return paging != null;
  }
}
