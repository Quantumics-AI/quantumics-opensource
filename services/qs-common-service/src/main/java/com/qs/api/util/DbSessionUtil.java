package com.qs.api.util;

import com.qs.api.DatabaseSessionManager;
import com.qs.api.tenant.TenantContext;
import org.springframework.stereotype.Component;

@Component
public class DbSessionUtil {

    private final DatabaseSessionManager sessionManager;

    public DbSessionUtil(DatabaseSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * @param schemaName
     */
    public void changeSchema(final String schemaName) {
        sessionManager.unbindSession();
        TenantContext.setCurrentTenant(schemaName);
        sessionManager.bindSession();
    }
}
