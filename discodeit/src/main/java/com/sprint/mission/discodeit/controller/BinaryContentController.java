package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/files")
public class BinaryContentController {

    private final BinaryContentRepository binaryContentRepository;

    //  단일 파일 다운로드 (Path 사용으로 단일 조회)
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadSingleFile(@PathVariable UUID fileId) {
        BinaryContent file = binaryContentRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getBytes());
    }

    //  여러 파일 다운로드 (Param 사용으로 다건 조회)
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadMultipleFiles(@RequestParam List<UUID> fileIds) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (UUID fileId : fileIds) {
                BinaryContent file = binaryContentRepository.findById(fileId)
                        .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

                zos.putNextEntry(new ZipEntry(file.getFileName()));
                zos.write(file.getBytes());
                zos.closeEntry();
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"files.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(baos.toByteArray());
    }
}
