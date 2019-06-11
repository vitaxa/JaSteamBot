package com.vitaxa.jasteambot.serialize;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {
    @JsonProperty("response")
    private T response;

    public T getResponse() {
        return response;
    }
}
