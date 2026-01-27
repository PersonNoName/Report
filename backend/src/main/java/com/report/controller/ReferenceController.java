package com.report.controller;

import com.report.common.Result;
import com.report.entity.ReferenceMaterial;
import com.report.service.ReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/references")
@RequiredArgsConstructor
public class ReferenceController {

    private final ReferenceService referenceService;

    /**
     * 搜索参考资料
     */
    @GetMapping
    public Result<List<ReferenceMaterial>> search(
            @RequestParam String sectionKey,
            @RequestParam(required = false) String keyword) {
        return Result.success(referenceService.search(sectionKey, keyword));
    }

    /**
     * 保存为标准话术
     */
    @PostMapping
    public Result<ReferenceMaterial> saveAsStandard(@RequestBody Map<String, Object> body) {
        String sectionKey = (String) body.get("sectionKey");
        String contentText = (String) body.get("contentText");
        String tags = (String) body.get("tags");
        Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;

        return Result.success(referenceService.saveAsStandard(sectionKey, contentText, tags, userId));
    }

    /**
     * 删除参考资料
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        referenceService.removeById(id);
        return Result.success(null);
    }
}
