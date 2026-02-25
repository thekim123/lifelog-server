package com.younghwan.lifelog.receipt.ocr;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Primary
public class MockOcrProvider implements OcrProvider {
    @Override
    public OcrResult extract(MultipartFile file) throws IOException {
        return OcrResult.builder()
                .rawText("MOCK_OCR: " + file.getOriginalFilename())
                .storeName("UNKNOWN")
                .purchasedAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .items(List.of())
                .build();
    }
}
