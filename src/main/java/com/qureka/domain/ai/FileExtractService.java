package com.qureka.domain.ai;

import com.qureka.global.exception.CustomException;
import com.qureka.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Service
public class FileExtractService {

    private static final int MIN_LENGTH = 50;

    // ── PDF 텍스트 추출 ──────────────────────────────────────────────────────
    public String extractFromPdf(byte[] bytes) {
        try (PDDocument doc = Loader.loadPDF(bytes)) {   // ✅ PDFBox 3.x API
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(doc).trim();
            if (text.length() < MIN_LENGTH)
                throw new CustomException(ErrorCode.FILE_EXTRACT_FAILED);
            log.debug("[PDFBox] 텍스트 추출 완료 - {}자", text.length());
            return text;
        } catch (CustomException e) {
            throw e;
        } catch (IOException e) {
            log.error("[PDFBox] 추출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_EXTRACT_FAILED);
        }
    }

    // ── PPTX 텍스트 추출 ─────────────────────────────────────────────────────
    public String extractFromPptx(byte[] bytes) {
        try (XMLSlideShow pptx = new XMLSlideShow(new ByteArrayInputStream(bytes))) {
            StringBuilder sb = new StringBuilder();
            for (XSLFSlide slide : pptx.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape ts) {
                        String text = ts.getText();
                        if (text != null && !text.isBlank()) sb.append(text).append("\n");
                    }
                }
                sb.append("\n");
            }
            String result = sb.toString().trim();
            if (result.length() < MIN_LENGTH)
                throw new CustomException(ErrorCode.FILE_EXTRACT_FAILED);
            return result;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[POI] PPTX 추출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_EXTRACT_FAILED);
        }
    }

    // ── 토큰 수 기준 텍스트 자르기 ───────────────────────────────────────────
    // 한국어 혼합 문서 근사: 1 token ≈ 1.5 chars
    public String truncateByTokens(String text, int maxTokens) {
        int maxChars = (int) (maxTokens * 1.5);
        if (text.length() <= maxChars) return text;
        log.warn("텍스트 토큰 초과 - 원본 {}자 → {}자로 자름", text.length(), maxChars);
        return text.substring(0, maxChars);
    }
}