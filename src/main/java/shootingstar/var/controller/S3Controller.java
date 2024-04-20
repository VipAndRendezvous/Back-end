package shootingstar.var.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import shootingstar.var.Service.S3ImageService;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;

import java.io.IOException;
import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
@Slf4j
public class S3Controller {

    private final S3ImageService s3ImageService;

    @PostMapping("/upload")
    public ResponseEntity<String> create(
            @RequestPart(value = "boardImg", required = false) MultipartFile multipartFile) {
        String fileName = "";
        if (multipartFile != null) { // 파일 업로드한 경우에만
            try {// 파일 업로드
                fileName = s3ImageService.upload(multipartFile); // S3 버킷의 images 디렉토리 안에 저장됨
                log.info("fileName = {}", fileName);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
            }
        } else {
            throw new CustomException(ErrorCode.S3_EMPTY_IMAGE);
        }

        return ResponseEntity.ok().body(fileName);
    }
}
