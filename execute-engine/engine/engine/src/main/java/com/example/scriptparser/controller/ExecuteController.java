package com.example.scriptparser.controller;

import com.example.scriptparser.service.ExecutionService;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ExecuteController {

    @Autowired
    private ExecutionService executionService;

    @PostMapping("/execute")
    public ResponseEntity<?> execute(@Valid @RequestBody ExecuteRequest request) {
        try {
            JsonNode result = executionService.executeScript(request.getScriptName(), request.getInit());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("执行失败: " + e.getMessage());
        }
    }
}
