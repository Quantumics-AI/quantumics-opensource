package com.qs.api.request;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderCleanseInfo {
    private int projectId;
    private int userId;
    private int engFlowId;

    private Set<FolderInfo> folders;
}
