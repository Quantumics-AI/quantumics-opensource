package com.qs.api.request;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderInfo {
    private int folderId;
    private String folderName;

    private Set<FileInfo> files;
}
