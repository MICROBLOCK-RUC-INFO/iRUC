package com.example.scriptparser.controller;

import com.example.api.HandlerService;
import com.example.scriptparser.service.ScriptService;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class UpdateController {

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private PluginManager pluginManager;

    @Value("${plugin.directory:plugins}")
    private String pluginsDir;

    @PostMapping("/update")
    public ResponseEntity<String> update(
            @RequestParam("gqlp") MultipartFile[] gqlpFiles,
            @RequestParam("gqlpk") MultipartFile[] gqlpkFiles,
            @RequestParam(value = "jar", required = false) MultipartFile[] jarFiles
    ) {
        try {
            // 处理jar文件
            if (jarFiles != null && jarFiles.length > 0) {
                Path pluginsPath = Paths.get(pluginsDir);
                if (!Files.exists(pluginsPath)) {
                    Files.createDirectories(pluginsPath);
                }
                for (MultipartFile jar : jarFiles) {
                    if (jar.isEmpty()) continue;
                    String filename = StringUtils.cleanPath(jar.getOriginalFilename());
                    Path destination = pluginsPath.resolve(filename);
                    Files.copy(jar.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // 处理gqlp文件
            Map<String, String> newGqlpScripts = new HashMap<>();
            if (gqlpFiles != null) {
                for (MultipartFile gqlp : gqlpFiles) {
                    if (gqlp.isEmpty()) continue;
                    String scriptName = getFileNameWithoutExtension(gqlp.getOriginalFilename(), ".gqlp");
                    String content = new String(gqlp.getBytes(), "UTF-8");
                    newGqlpScripts.put(scriptName, content);
                }
                scriptService.updateGqlpScripts(newGqlpScripts);
            }

            // 处理gqlpk文件
            Map<String, String> newGqlpkQueries = new HashMap<>();
            if (gqlpkFiles != null) {
                Pattern regex = Pattern.compile("\\[([^\\]]+)\\]:\\[\\s*([\\s\\S]*?)\\s*\\]");
                for (MultipartFile gqlpk : gqlpkFiles) {
                    if (gqlpk.isEmpty()) continue;
                    String content = new String(gqlpk.getBytes(), "UTF-8");
                    Matcher matcher = regex.matcher(content);
                    while (matcher.find()) {
                        String key = matcher.group(1).trim();
                        String value = matcher.group(2).trim();
                        if (!key.isEmpty() && !value.isEmpty()) {
                            newGqlpkQueries.put(key, value);
                        }
                    }
                }
                scriptService.updateGqlpkQueries(newGqlpkQueries);
            }

            // 重启插件
            reloadPlugins();

            return ResponseEntity.ok("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新失败: " + e.getMessage());
        }
    }

    private void reloadPlugins() {
        pluginManager.stopPlugins();
        pluginManager.unloadPlugins();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        List<HandlerService> extensions = pluginManager.getExtensions(HandlerService.class);
        for (HandlerService service : extensions) {
            System.out.println("启动插件: " + service.getName());
        }
    }

    private String getFileNameWithoutExtension(String filename, String extension) {
        if (filename == null || !filename.endsWith(extension)) {
            return filename;
        }
        return filename.substring(0, filename.length() - extension.length());
    }
}
