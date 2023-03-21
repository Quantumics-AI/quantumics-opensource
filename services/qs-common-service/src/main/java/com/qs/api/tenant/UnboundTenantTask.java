package com.qs.api.tenant;

import java.util.concurrent.Callable;

import static com.qs.api.util.QsConstants.QS_DEFAULT_TENANT_ID;

public abstract class UnboundTenantTask<T> implements Callable<T> {

    @Override
    public T call() throws Exception {
        TenantContext.setCurrentTenant(QS_DEFAULT_TENANT_ID);
        return callInternal();
    }

    protected abstract T callInternal();
}
