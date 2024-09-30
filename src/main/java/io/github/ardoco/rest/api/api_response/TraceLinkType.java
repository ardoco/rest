package io.github.ardoco.rest.api.api_response;

public enum TraceLinkType {
    SAD_CODE("SadCodeResult:"),
    SAM_CODE("SamCodeResult:"),
    SAD_SAM("SadSamResult:"),
    SAD_SAM_CODE("SadSamCodeResult:"),
    OTHER("Other:");

    private final String keyPrefix;

    TraceLinkType(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getKeyPrefix() {
        return this.keyPrefix;
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
