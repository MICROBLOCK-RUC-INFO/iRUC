package com.example.scriptparser.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class ExecuteRequest {

    @NotBlank
    private String scriptName;

    @Setter
    @Getter
    @NotNull
    private Map<String, String> init;

    @JsonProperty("scriptname")
    public String getScriptName() {
        return scriptName;
    }

    @JsonProperty("scriptname")
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }
}
