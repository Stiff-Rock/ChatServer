package com.yago.chatServer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FilesController {

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path uploadsDir = Paths.get(System.getProperty("user.dir"), "uploads");
        Path filePath = uploadsDir.resolve(fileName);

        try {
            Files.createDirectories(uploadsDir);
            Files.write(filePath, file.getBytes());

            String imageUrl = "http://{ipAndPort}/uploads/" + fileName;
            return ResponseEntity.ok(Collections.singletonMap("url", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
