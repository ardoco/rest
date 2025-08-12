/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response;

/**
 * Enum which represents the different types of tracelinks/ runner-names.
 * It is used primarily in controllers and services to differentiate the types of trace links
 * during processing and determine the appropriate handler for each request based on the type.
 */
public enum TraceLinkType {
    SAD_CODE("SadCodeResult", "ardocode"), SAM_CODE("SamCodeResult", "arcotl"), SAD_SAM("SadSamResult", "swattr"), SAD_SAM_CODE("SadSamCodeResult",
            "transarc"), OTHER("Other", "other");

    private final String keyPrefix;
    private final String endpointName;

    TraceLinkType(String keyPrefix, String endpointName) {
        this.keyPrefix = keyPrefix;
        this.endpointName = endpointName;
    }

    public String getKeyPrefix() {
        return this.keyPrefix;
    }

    public String getEndpointName() {
        return this.endpointName;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public static TraceLinkType fromId(String id) {
        for (TraceLinkType type : TraceLinkType.values()) {
            if (id.startsWith(type.getKeyPrefix())) {
                return type;
            }
        }
        return OTHER;
    }
}
