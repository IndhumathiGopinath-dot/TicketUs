package com.ticketsystem.service;

import com.ticketsystem.exception.AppException;
import com.ticketsystem.model.Attachment;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.repository.AttachmentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final AttachmentRepository attachmentRepository;

    public FileStorageService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload dir", e);
        }
    }

    public Attachment store(MultipartFile file, Ticket ticket) {
        if (file.isEmpty()) {
            throw new AppException("Empty file");
        }
        try {
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot != -1) ext = original.substring(dot);
            String storedName = UUID.randomUUID() + ext;
            Path target = Paths.get(uploadDir).resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Attachment a = Attachment.builder()
                    .ticket(ticket)
                    .fileName(original)
                    .filePath(storedName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .build();
            return attachmentRepository.save(a);
        } catch (IOException e) {
            throw new AppException("Could not store file: " + e.getMessage());
        }
    }

    public Path load(String storedName) {
        return Paths.get(uploadDir).resolve(storedName);
    }

    public List<Attachment> getByTicket(Ticket t) {
        return attachmentRepository.findByTicket(t);
    }
}
