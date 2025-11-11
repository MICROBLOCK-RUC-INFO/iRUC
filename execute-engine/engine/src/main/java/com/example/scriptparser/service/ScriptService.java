package com.example.scriptparser.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class ScriptService {

    private final Map<String, String> gqlpScripts = new ConcurrentHashMap<>();
    private final Map<String, String> gqlpkQueries = new ConcurrentHashMap<>();

    public void updateGqlpScripts(Map<String, String> newScripts) {
        gqlpScripts.putAll(newScripts);
    }

    public void updateGqlpkQueries(Map<String, String> newQueries) {
        gqlpkQueries.putAll(newQueries);
    }

    public String getGqlpScript(String scriptName) {
        return gqlpScripts.get(scriptName);
    }

    public String getGqlpkQuery(String graphqlKey) {
        return gqlpkQueries.get(graphqlKey);
    }
}
