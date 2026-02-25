# IMPLEMENTATION_STATUS

## 이번 마일스톤 완료 사항

1. **엔티티/리포지토리 추가**
   - `ReceiptItem`, `ItemCatalog`, `InventoryTransaction` 엔티티 추가
   - 각 엔티티 전용 Repository 추가

2. **영수증 업로드 파이프라인 리팩터링**
   - 컨트롤러 중심 로직을 `ReceiptService`로 이동
   - Multipart request의 품목 필드(`itemName/itemQuantity/itemUnit/itemPrice`)를 파싱해 `Receipt + ReceiptItem` 저장

3. **OCR Provider 추상화 도입**
   - `OcrProvider` 인터페이스 추가
   - `MockOcrProvider` 기본 구현 추가 (추후 실제 API Provider 교체 가능)

4. **품목 정규화 서비스 추가**
   - `ItemNormalizationService` 추가
   - `ItemCatalog.aliases` CSV를 파싱하여 canonical name으로 정규화

5. **추천 점수식 고도화**
   - `RecommendationService` 분리
   - 점수식: `matchRatio*100 - missingCount*8 + expiringSoonBonus`
   - expiring soon 보너스(3일 이내 만료, 최대 20점)

6. **신규 API 추가**
   - `GET /api/receipts/{receiptId}`: 영수증 상세 + 품목 목록
   - `PUT /api/receipts/{receiptId}/confirm`: OCR/업로드 결과 교정 확정
   - `GET /api/expenses/summary/monthly?year=YYYY&month=M`: 월별 합계/카테고리별 요약

7. **문서 업데이트**
   - `README.md` 실행/테스트/API 예시 최신화

---

## 남은 로드맵 (추천)

1. **DB 마이그레이션 정식화**
   - Flyway/Liquibase 도입
   - 현재 `ddl-auto` 의존 제거

2. **OCR 실서비스 연동**
   - 외부 OCR API provider 구현 (`OcrProvider` 실구현)
   - 실패 재시도/timeout/circuit breaker

3. **확정(Confirm) 로직 보강**
   - 기존 품목 대비 재고 delta 반영
   - 수정 이력(감사로그) 저장

4. **정규화 정확도 개선**
   - aliases를 별도 테이블로 분리
   - 유사도(오타/형태소) 매칭 도입

5. **추천 품질 개선**
   - 레시피 난이도/조리시간/개인 선호도 반영
   - 계절성/소비기한 임박 소비 우선 정책

6. **테스트 강화**
   - 서비스 단위테스트 + 컨트롤러 통합테스트 추가
   - Testcontainers 기반 DB 테스트
