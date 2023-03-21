package ai.quantumics.api.util;

public enum JobRunState {
    STARTING("STARTING"),
    RUNNING("RUNNING"),
    STOPPING("STOPPING"),
    STOPPED("STOPPED"),
    SUCCEEDED("SUCCEEDED"),
    FAILED("FAILED"),
    TIMEOUT("TIMEOUT");

    private String value;

    private JobRunState(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}
