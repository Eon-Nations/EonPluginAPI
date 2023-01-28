package org.eonnations.eonpluginapi.api;

public enum Status {
    SUCCESS,
    FAILURE,
    NOT_IMPLEMENTED;

    public boolean isFailure() {
        return this == FAILURE;
    }
}
