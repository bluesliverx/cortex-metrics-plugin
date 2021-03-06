package com.adobe.dx.xeng.cortexmetrics.config;

import hudson.ExtensionList;
import hudson.model.Item;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;

/**
 * Provider of Cortex Metrics configuration options.
 * @author saville
 */
public abstract class CortexMetricsConfigProvider {
    /**
     * Return the Cortex URL.
     * @return the Cortex URL
     * @param item the current item
     */
    public abstract String getUrl(Item item);

    /**
     * Return the bearer token in plain text.
     * @return the bearer token
     * @param item the current item
     */
    public abstract Secret getBearerToken(Item item);

    /**
     * Return the namespace for metric names.
     * @return the namespace
     * @param item the current item
     */
    public abstract String getNamespace(Item item);

    /**
     * Retrieves all Cortex metrics configuration providers.
     * @return the Cortex metrics config providers
     */
    private static ExtensionList<CortexMetricsConfigProvider> all() {
        return ExtensionList.lookup(CortexMetricsConfigProvider.class);
    }

    /**
     * Retrieves the first configured Cortex URL, either on a parent folder or in global configuration.
     * @param item the current item
     * @return the configured Cortex URL
     */
    public static String getConfiguredUrl(Item item) {
        for (CortexMetricsConfigProvider provider : all()) {
            if (provider == null) {
                continue;
            }
            String url = provider.getUrl(item);
            if (!StringUtils.isBlank(url)) {
                return url;
            }
        }
        return null;
    }

    /**
     * Retrieves the first configured bearer token, either on a parent folder or in global configuration.
     * @param item the current item
     * @return the configured bearer token
     */
    public static Secret getConfiguredBearerToken(Item item) {
        for (CortexMetricsConfigProvider provider : all()) {
            if (provider == null) {
                continue;
            }
            Secret bearerToken = provider.getBearerToken(item);
            if (bearerToken != null && !StringUtils.isBlank(bearerToken.getPlainText())) {
                return bearerToken;
            }
        }
        return null;
    }

    /**
     * Retrieves the first configured namespace, either on a parent folder or in global configuration.
     * @param item the current item
     * @return the configured namespace
     */
    public static String getConfiguredNamespace(Item item) {
        for (CortexMetricsConfigProvider provider : all()) {
            if (provider == null) {
                continue;
            }
            String namespace = provider.getNamespace(item);
            if (!StringUtils.isBlank(namespace)) {
                return namespace;
            }
        }
        return null;
    }
}
