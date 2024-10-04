package io.github.ardoco.rest.api.api_response;

public enum TraceLinkType {
    SAD_CODE("SadCodeResult:", "sad-code"),
    SAM_CODE("SamCodeResult:", "sam-code"),
    SAD_SAM("SadSamResult:", "sad-sam"),
    SAD_SAM_CODE("SadSamCodeResult:", "sad-sam-code"),
    OTHER("Other:", "other");

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
