package com.report.controller;

import com.report.common.Result;
import com.report.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin // Allow CORS for now, though global config handles it
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/polish")
    public Result<String> polishText(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("Content cannot be empty");
        }
        try {
            String polished = aiService.polishContent(content);
            return Result.success(polished);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI Service Error: " + e.getMessage());
        }
    }
}
