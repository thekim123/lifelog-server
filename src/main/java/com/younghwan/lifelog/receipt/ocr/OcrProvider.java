package com.younghwan.lifelog.receipt.ocr;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface OcrProvider {
    OcrResult extract(MultipartFile file) throws IOException;
}
