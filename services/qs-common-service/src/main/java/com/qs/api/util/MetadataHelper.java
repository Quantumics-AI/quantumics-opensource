package com.qs.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.qs.api.model.EngFlowEvent;
import com.qs.api.model.QsFiles;
import com.qs.api.request.ColumnMetaData;
import com.qs.api.request.DataFrameRequest;
import com.qs.api.request.FileInfo;
import com.qs.api.request.FileJoinColumnMetadata;
import com.qs.api.request.Metadata;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MetadataHelper {

  private static final Gson gson = new Gson();

  public String getJoinMetadata(final int eventId, final String file1MetadataJson,
      final String file2MetadataJson) {
    List<Metadata> joinCols = new ArrayList<>();

    log.info("File1 Metadata JSON: {}", file1MetadataJson);

    FileJoinColumnMetadata file1Metadata = null;

    if (file1MetadataJson != null && !file1MetadataJson.isEmpty()) {
      file1Metadata = gson.fromJson(file1MetadataJson, FileJoinColumnMetadata.class);
      log.info("File1 Metadata Object parsing using GSON API: {}", file1Metadata.getMetadata());

      joinCols.addAll(file1Metadata.getMetadata());
    }

    log.info("File2 Metadata JSON: {}", file2MetadataJson);

    FileJoinColumnMetadata file2Metadata = null;

    if (file2MetadataJson != null && !file2MetadataJson.isEmpty()) {
      file2Metadata = gson.fromJson(file2MetadataJson, FileJoinColumnMetadata.class);
      log.info("File2 Metadata Object parsing using GSON API: {}", file2Metadata.getMetadata());

      joinCols.addAll(file2Metadata.getMetadata());
    }

    FileJoinColumnMetadata joinMetadata = new FileJoinColumnMetadata();
    joinMetadata.setEventId(eventId);
    joinMetadata.setMetadata(joinCols);

    String finalMetadataStr = gson.toJson(joinMetadata, FileJoinColumnMetadata.class);
    log.info("Final Metadata JSON for the Join is: {}", finalMetadataStr);

    return finalMetadataStr;
  }

  public String getFileMetadata(final String file1MetadataJson, final int eventId) throws Exception {
    List<ColumnMetaData> colsMetadata = parseColumnMetadataToList(file1MetadataJson);
    if (colsMetadata != null && !colsMetadata.isEmpty()) {
      FileJoinColumnMetadata fileJoinColumnMetadata = new FileJoinColumnMetadata();
      fileJoinColumnMetadata.setEventId(eventId);
      fileJoinColumnMetadata.setMetadata(getMetadataForFileorJoin(colsMetadata, eventId));

      log.info("File Metadata is: {}", fileJoinColumnMetadata);

      return gson.toJson(fileJoinColumnMetadata);
    }

    return "";
  }

  private List<Metadata> getMetadataForFileorJoin(final List<ColumnMetaData> colsMetadata,
      final int eventId) {
    if (colsMetadata != null && !colsMetadata.isEmpty()) {

      List<Metadata> fileOrJoinMetadata = colsMetadata.stream().map(
          obj -> (new Metadata("a_" + eventId + "_" + obj.getColumnName(), obj.getColumnName())))
          .collect(Collectors.toList());
      log.info("File or Join Metadata returned is: {}", fileOrJoinMetadata);

      return fileOrJoinMetadata;
    }

    return Collections.emptyList();
  }

  public List<ColumnMetaData> parseColumnMetadataToList(final String fileMetadataStr) throws JSONException {
    List<ColumnMetaData> colsMetadata = new ArrayList<>();
    if (fileMetadataStr != null && !fileMetadataStr.isEmpty()) {
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

  public Set<FileInfo> getFolderCleanseInfo(final int folderId, final List<EngFlowEvent> fileEvents,
      final List<QsFiles> files) {
    if (files != null && !files.isEmpty()) {
      Set<FileInfo> filesInfo = new HashSet<FileInfo>(
          files.stream().map(file -> (new FileInfo(file.getFileId(), file.getFileName(),
              getProcessedFlag(fileEvents, file)))).collect(Collectors.toList()));

      log.info("Files information related to Folder Id: {} is: {}", folderId, filesInfo);

      return filesInfo;
    }

    return Collections.emptySet();
  }

  private boolean getProcessedFlag(final List<EngFlowEvent> fileEvents, final QsFiles file) {
    if (fileEvents != null && !fileEvents.isEmpty()) {
      return fileEvents.stream()
          .anyMatch(efe -> gson.fromJson(efe.getEngFlowConfig(), DataFrameRequest.class)
              .getFileId() == file.getFileId());
    }

    return false;
  }

}
