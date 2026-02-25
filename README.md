# lifelog-mvp-spring

개인용 생활데이터 서비스 MVP 백엔드 (Spring Boot)

## 주요 기능
- 기존 기능: 영수증/지출/재고/레시피/추천 API
- 신규 인증/승인 흐름 (JWT, Stateless):
  - 아이디/비밀번호 로그인 시 Access Token 발급
  - 회원가입 시 `PENDING` 상태로 생성
  - 관리자 승인 후에만 로그인 가능
  - 초기 관리자 부트스트랩 (`ADMIN_USERNAME`, `ADMIN_PASSWORD`)
  - 관리자 전용 승인 API (`/api/admin/**`)
  - 인증 API 일부 제외 모든 API Bearer 토큰 필요

---

## 환경 변수
- `ADMIN_USERNAME` (선택): 초기 관리자 아이디
- `ADMIN_PASSWORD` (선택): 초기 관리자 비밀번호

앱 시작 시 해당 username이 없으면 `APPROVED + ADMIN`으로 자동 생성됩니다.

예시:
```bash
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=admin1234
```

JWT 설정 (`application.yml`):
- `app.jwt.secret` (32바이트 이상 필수)
- `app.jwt.access-token-expiry-seconds` (기본 3600)

OCR 설정 (`application.yml`):
- `app.ocr.provider`: `mock` | `tesseract` (기본 `mock`)
- 실 OCR 사용 시 맥미니에 tesseract 설치 필요:
```bash
brew install tesseract tesseract-lang
```
- 실행 시:
```bash
export OCR_PROVIDER=tesseract
```

---

## 실행
1) PostgreSQL + Redpanda 실행
```bash
docker compose up -d
```

2) Redpanda 토픽 생성(최초 1회)
```bash
docker exec -it lifelog-redpanda rpk topic create ocr.requested
docker exec -it lifelog-redpanda rpk topic create ocr.completed
docker exec -it lifelog-redpanda rpk topic create ocr.failed
docker exec -it lifelog-redpanda rpk topic create ocr.dlq
```

3) OCR 워커 실행 (선택: docker compose로 함께 기동)
```bash
# backend compose에서 worker 포함 기동
docker compose up -d

# 또는 worker 단독 실행
cd ../lifelog-ocr-worker
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8090
```

4) 앱 실행
```bash
./gradlew bootRun
```

4) 테스트 실행
```bash
./gradlew test
```

### 테스트 커버리지 범위 (단위 테스트)
- `AuthServiceTest`: 회원가입 시 `PENDING` 생성, 중복 username 예외
- `AuthControllerTest`: 승인 사용자 로그인 성공, 대기 사용자 로그인 차단, 토큰 응답 필드 검증
- `JwtTokenProviderTest`: 토큰 생성/클레임 파싱/만료 토큰/비정상 토큰 처리
- `JwtAuthenticationFilterTest`: Bearer 파싱, 승인 사용자 SecurityContext 설정, 예외 시 컨텍스트 초기화
- `AdminApprovalControllerTest`: 대기 사용자 목록 조회, 승인 전환, 미존재 사용자 처리
- `RecommendationServiceTest`: 매칭 비율/누락 페널티/임박 만료 보너스(상한 20) 점수식 검증
- `ExpenseControllerTest`: 월간 합계/건수/카테고리별 집계 및 기간 경계값 검증

---

## JWT 인증 흐름
1. `POST /api/auth/login` 성공 시 `accessToken` 수신
2. 이후 요청 헤더에 `Authorization: Bearer <accessToken>` 포함
3. `POST /api/auth/logout`은 서버 세션 제거가 아닌 **클라이언트 토큰 폐기 의미**
4. `GET /api/auth/me`로 현재 토큰 사용자 확인

---

## 인증/승인 API
### Auth
- `POST /api/auth/signup`
  - body: `{ "username": "user1", "password": "pass" }`
  - 결과: `PENDING` 사용자 생성
- `POST /api/auth/login`
  - body: `{ "username": "user1", "password": "pass" }`
  - 결과: `{ accessToken, tokenType, expiresInSeconds, user }`
  - 승인된 사용자만 성공
- `POST /api/auth/logout`
- `GET /api/auth/me`
  - 현재 토큰 사용자 정보

### Admin approvals (ADMIN 전용)
- `GET /api/admin/approvals/pending`
- `POST /api/admin/approvals/{userId}/approve`

---

## 기존 도메인 API (로그인 필요)
- `POST /api/receipts/upload` (비동기 OCR 요청 발행)
- `GET /api/receipts?householdId=...`
- `GET /api/receipts/{receiptId}`
- `PUT /api/receipts/{receiptId}/confirm`
- `POST /api/receipts/{receiptId}/ocr/retry` (OCR 재요청)
- `GET /api/expenses`
- `GET /api/expenses/summary/monthly?year=2026&month=2`
- `GET /api/inventory/stocks`
- `GET/POST /api/recipes`
- `GET /api/recommendations/today`

---

## 빠른 API 테스트 예시 (curl)
```bash
# 1) 관리자 로그인해서 토큰 받기
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin1234"}' | jq -r '.accessToken')

# 2) 가입 요청 (비인증)
curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"username":"user1","password":"pass1234"}'

# 3) 관리자 토큰으로 pending 조회
curl http://localhost:8080/api/admin/approvals/pending \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 4) 승인
curl -X POST http://localhost:8080/api/admin/approvals/2/approve \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 5) 승인 사용자 로그인
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user1","password":"pass1234"}' | jq -r '.accessToken')

# 6) 인증 필요한 기존 API 호출
curl http://localhost:8080/api/expenses \
  -H "Authorization: Bearer $USER_TOKEN"
```
