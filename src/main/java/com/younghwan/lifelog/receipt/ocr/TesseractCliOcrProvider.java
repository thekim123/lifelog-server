package com.younghwan.lifelog.receipt.ocr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "app.ocr.provider", havingValue = "tesseract")
public class TesseractCliOcrProvider implements OcrProvider {
    private static final Pattern TOTAL_PATTERN = Pattern.compile("(?i)(합계|총\\s*액|total)\\D{0,8}([0-9][0-9,]{2,})");
    private static final Pattern DATE_PATTERN = Pattern.compile("(20\\d{2})[-/.](\\d{1,2})[-/.](\\d{1,2})");

    @Override
    public OcrResult extract(MultipartFile file) throws IOException {
        Path temp = Files.createTempFile("receipt-", "-" + safeExt(file.getOriginalFilename()));
        try {
            file.transferTo(temp);
            String raw = runTesseract(temp);

            return OcrResult.builder()
                    .rawText(raw)
                    .storeName(parseStoreName(raw).orElse("UNKNOWN"))
                    .purchasedAt(parseDate(raw).orElse(LocalDateTime.now()))
                    .totalAmount(parseTotal(raw).orElse(BigDecimal.ZERO))
                    .items(List.of())
                    .build();
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private String runTesseract(Path imagePath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("tesseract", imagePath.toString(), "stdout", "-l", "kor+eng");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line).append('\n');
            }
        }

        try {
            int code = process.waitFor();
            if (code != 0) throw new IOException("tesseract exited with code " + code + ": " + out);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("tesseract interrupted", e);
        }

        return out.toString();
    }

    private Optional<String> parseStoreName(String raw) {
        return raw.lines()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .findFirst();
    }

    private Optional<BigDecimal> parseTotal(String raw) {
        Matcher m = TOTAL_PATTERN.matcher(raw);
        if (m.find()) {
            return Optional.of(new BigDecimal(m.group(2).replace(",", "")));
        }

        // fallback: biggest numeric token
        return raw.lines()
                .flatMap(line -> Pattern.compile("[0-9][0-9,]{2,}").matcher(line).results())
                .map(match -> new BigDecimal(match.group().replace(",", "")))
                .max(BigDecimal::compareTo);
    }

    private Optional<LocalDateTime> parseDate(String raw) {
        Matcher m = DATE_PATTERN.matcher(raw);
        if (!m.find()) return Optional.empty();

        String normalized = String.format(Locale.ROOT, "%s-%02d-%02d 00:00:00",
                m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        return Optional.of(LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private String safeExt(String name) {
        if (name == null || !name.contains(".")) return "img";
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
