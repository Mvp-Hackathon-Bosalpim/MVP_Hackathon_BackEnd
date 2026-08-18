# 0. 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [정규화 품목명 구현 방식](#2-정규화-품목명-구현-방식)
3. [출력 데이터 스키마](#3-출력-데이터-스키마)
4. [예외 판정 규칙](#4-예외-판정-규칙)
5. [OCR 구현 현황](#5-ocr-구현-현황)
6. [구현하지 못한 부분 & 알려진 오류](#6-구현하지-못한-부분--알려진-오류)
7. [소스 코드 정보 및 실행 방법](#7-소스-코드-정보)

---
# 1. 프로젝트 개요

1-1. 서비스 소개
기존 ComfoziAI 앞단에 추가되는 구매 증빙 인박스 검증 시스템입니다.
기존에 수작업으로 처리하던 구매 증빙 등록·검수 과정을,
파일/수기 입력 → 구조화 후보 생성 → 중복·누락·규격·단위 불일치 탐지 → 검수 인박스 → 사람의 수정/승인/반려 및 변경 이력 기록 → 승인 데이터 JSON+CSV 출력
흐름으로 자동화하여, 기존 ComfoziAI가 받을 수 있는 정형 데이터로 변환해주는 역할을 하는 시스템 입니다.

접속 URL: https://mvp-hackathon-front-end.vercel.app/
화면 구성: 대시보드 → 데이터 등록 → 인박스(검수 대기열) → 상세 페이지 총 4단계

1-2. 프로젝트 목표
초기 목표:	창업팀에서 제공한 목표(필수 요건) 100% 완료
중장기 목표: 초기 목표 + 사용자에게 필요한 기능 추가 개발
최종 결과:	필수 요건 8개 중 8개 완료(100%), 추가 요건(OCR) 외 9건의 부가 기능 추가 구현, 사용자 시나리오 전 구간 테스트 검증 완료

1-3. 개발 범위 및 제약 사항
교육을 갓 마친 초급 개발팀의 첫 프로젝트임을 고려하여, 창업팀이 제공한 합성 데이터 20건 기준으로 핵심 흐름이 정상 작동하는 것을 검증 목표로 삼았습니다. Production 품질·기존 시스템으로의 직접 이식·광범위한 입력값에 대한 일반화는 목표 범위에 포함하지 않으며, 미지원/실패 사례는 본 문서(README)에 투명하게 기록합니다.
---

# 2. 정규화 품목명 구현 방식

## 2-1. 정규화 프로세스

* 창업팀에서 제공한 정규화 사전을 사용함을 명시합니다.

```
1. 창업팀 제공 20row 의 사전 탐색
2. '냉감튀', 'S/O' 와 같은 약어 전개
3. 단위 패턴 제거 (K, G 등)
4. 특정 단어 기준 띄어쓰기 적용
```

> AI 기반 정규화는 시도하려고 하였으나 api 호출 및 네트워크 문제 등 응답 시간 문제로 인해서 일단은 제외하였습니다.

---

## 2-2. 데이터 부족 처리 방식

> 1. 위 2-1 의 정규화 결과가 null 값인 경우 `데이터 부족` 으로 처리되도록 하였습니다.<br>
> 2. `데이터 부족` 의 경우도 `필수값 누락` 인 이상 현상에 탐지되도록 하였습니다.<br>
> 3. 또한 기존 요구 사항 중 하나인 `중복 탐지` 로직에서 `정규화 된 물품명 (normalized_item_name)` 이
     > `null` 인 경우에는 `원본 품목명 (raw_item_name)` 과의 비교를 통해 중복탐지가 되도록 구현 하였습니다.

---

## 2-3. 사용한 품목명 사전 정보

| 원문 품목명    | 정규화 품목명    |
|-----------|------------|
| 토마토살사S/O  | 토마토 살사 소스  |
| 허브염지닭정육   | 허브 염지 닭정육  |
| 밀또띠아10인치  | 밀 또띠아 10인치 |
| 로메인쉬레드    | 로메인 쉬레드    |
| 아보카도30입   | 아보카도       |
| 슈레드치즈2.5K | 슈레드 치즈     |
| 사워크림1K    | 사워크림       |
| 라임30과     | 라임         |
| 할라피뇨슬라이스  | 할라피뇨 슬라이스  |
| 나초칩454G   | 나초칩        |
| 블랙빈2.5K   | 블랙빈        |
| 자스민쌀10K   | 자스민쌀       |
| 냉감튀2K     | 냉동 감자튀김    |
| 스모크BBQ소스  | 스모크 바비큐소스  |
| 종이보울500   | 종이 보울      |
| 투명리드500   | 투명 리드      |
| 냉동새우살900  | 냉동 새우살     |
| 냉동돈전지     | 냉동 돼지고기 전지 |
| 고수4단      | 고수         |

---

# 3. 출력 데이터 스키마

## 개요

승인된 항목은 **JSON**과 **UTF-8 CSV** 두 형식으로 내보냅니다. 아래는 각 필드의 정의와 예시입니다.

---

## 3-1. JSON 스키마 예시 (전체 필드)

```json
{
  "doc_id": "DOC-001",
  "source_type": "PDF",
  "supplier_name": "가온푸드",
  "raw_item_name": "토마토살사S/O",
  "normalized_item_name": "토마토 살사 소스",
  "spec": "4kg/PK",
  "unit": "PK",
  "price_before": 32000,
  "price_after": 33600,
  "effective_date": "2026-08-01",
  "review_status": "approved",
  "exception_flags": [],
  "source_ref": {
    "input_method": "file",
    "file_name": "42_해커톤_업로드용_증빙20건_2026-08-04.csv",
    "row_no": 2
  },
  "reviewed_at": "2026-08-10T14:03:00+09:00",
  "review_memo": "단가 인상 5% 확인",
  "change_log": [
    {
      "at": "2026-08-10T14:01:00+09:00",
      "field": "normalized_item_name",
      "from": "토마토살사소스",
      "to": "토마토 살사 소스",
      "action": "edit"
    }
  ]
}
```

---

## 3-2. 필드 상세 설명

### 기본 정보

| 필드                     | 타입     | 설명        | 비고                                                          |
|------------------------|--------|-----------|-------------------------------------------------------------|
| `doc_id`               | String | 원본 증빙 식별자 | 형식: `DOC-###`                                               |
| `source_type`          | String | 원본 증빙 유형  | `PDF` \| `XLSX` \| `IMAGE` \| `MANUAL` \| `CSV`\| `UNKNOWN` |
| `supplier_name`        | String | 공급사명      | 입력값 그대로                                                     |
| `raw_item_name`        | String | 원문 품목명    | 공급사가 쓴 표기 그대로 (공백·약어 포함)                                    |
| `normalized_item_name` | String | 정규화 품목명   | 시스템이 산출하여 정규화 된 값                                           |

### 규격·단위·가격

| 필드               | 타입            | 설명          | 예시                                  |
|------------------|---------------|-------------|-------------------------------------|
| `spec`           | String        | 규격          | `4kg/PK`, `2kg×6PK/BOX`, `30EA/BOX` |
| `unit`           | String        | 단위          | `PK` \| `BOX` \| `EA` \| `PO`       |
| `price_before`   | Integer       | 인상 전 단가 (원) | `32000`                             |
| `price_after`    | Integer       | 인상 후 단가 (원) | `33600`                             |
| `effective_date` | String (Date) | 적용일         | 형식: `YYYY-MM-DD`                    |

### 검수 상태 및 예외

| 필드                | 타입            | 설명        | 값                                                            |
|-------------------|---------------|-----------|--------------------------------------------------------------|
| `review_status`   | String        | 검수 상태     | `new` / `needs_review` / `on_hold` / `approved` / `rejected` |
| `exception_flags` | Array[String] | 탐지된 예외 목록 | 아래 표 참조                                                      |

#### exception_flags 값 정의

| 값                     | 의미     |
|-----------------------|--------|
| `missing_required`    | 필수값 누락 |
| `spec_mismatch`       | 규격 불일치 |
| `unit_mismatch`       | 단위 불일치 |
| `duplicate_suspected` | 중복 의심  |

**주의**: 한 항목이 여러 예외에 해당하면 모두 포함됩니다.  
예외가 없으면 빈 배열 `[]`입니다.

### 입력 출처 및 이력

| 필드                        | 타입                | 설명       | 비고                  |
|---------------------------|-------------------|----------|---------------------|
| `source_ref.input_method` | String            | 입력 방식    | `file` / `manual`   |
| `source_ref.file_name`    | String            | 입력 파일명   | 파일 업로드 시만 기록        |
| `source_ref.row_no`       | Integer           | 입력 행 번호  | 파일 기준 행 번호 (1부터 시작) |
| `reviewed_at`             | String (DateTime) | 검수 완료 시각 | 한국 시간 기준으로 검수 완료 시간 |
| `review_memo`             | String            | 검수 메모    | 승인시 작성한 메모          
| `change_log`              | Array[Object]     | 변경 이력    | 아래 상세 참조            |

#### change_log 구조

```json
{
  "at": "2026-08-10T14:01:00+09:00",
  "field": "normalized_item_name",
  "from": "토마토살사소스",
  "to": "토마토 살사 소스",
  "action": "edit"
}
```

- `at`: 변경 시각
- `field`: 변경된 필드명
- `from`: 변경 전 값
- `to`: 변경 후 값
- `action`: `edit` (수정) / `approve` (승인) / `reject` (반려) / `delete` (삭제) / `re_review` (재검토)

---

## 3-3. CSV 출력 형식

JSON의 중첩 필드는 평탄화하여 CSV로 변환합니다.

### CSV 헤더 (예시)

```
doc_id,source_type,supplier_name,raw_item_name,normalized_item_name,spec,unit,price_before,price_after,effective_date,review_status,exception_flags,input_method,input_file_name,input_row_no,reviewed_at,review_memo
```

### CSV 데이터 행 (예시)

```
DOC-001,PDF,가온푸드,토마토살사S/O,토마토 살사 소스,4kg/PK,PK,32000,33600,2026-08-01,approved,,file,42_해커톤_업로드용_증빙20건_2026-08-04.csv,2,2026-08-10T14:03:00+09:00,단가 인상 5% 확인
```

**인코딩**: UTF-8
**예외 필드**: 여러 항목일 경우 세미콜론(`;`)으로 구분 (예: `missing_required;spec_mismatch`)

---

# 4. 예외 판정 규칙

## 4-1. 4가지 예외 유형 탐지 및 로직 구현 현황

* 중복 탐지
    * `4-2` 의 7개의 필드 값을 하나의 `key` 로 만들어 현재 `입력` 또는 `수정` 되는 값과 db 에 저장된 품목 정보들의 `key` 값들과 비교하여 중복을 탐지합니다.
    * `2. 정규화 품목명 구현 방식` 에서 구현된 정규화 매핑이 안되어 `데이터 부족 (null)` 이 되는 경우는 `원본 품목명 (raw_item_name)` 으로 대신하여 `key` 값을 만듭니다.

<br>

* 필수값 누락
    * `행 번호 (row_no)` 를 제외한 모든 필드에 대해서 값이 누락 된 경우는 `필수값 누락` 탐지가 되도록 하였습니다.
    * 행 번호의 경우는 수기 입력 시에는 null 로 반영되기 때문에 해당 탐지에서 제외하였습니다.
    * `정규화 품목명 (normalized_item_name)` 또한 데이터 부족으로 인한 null 값이 된 경우 해당 예외 유형이 탐지되도록하였습니다.

<br>

* 규격 불일치
    * 요구 사항에 적힌 것과 같이 `기존.. / 변경 .. `패턴에 매칭되면 불일치가 탐지되도록 하였습니다.

<br>

* 단위 불일치
    * 기존 창업팀에서 제시해주신 20개의 row 데이터에서 쓰이는 단위(BOX, KG) 와 OCR 예시 파일에서 제공해주신 단위 (봉, 박스, 팩, 망, 말, 캔 등) 을 추가로 적용하여 해당 단위는 탐지되지
      않도록 하였습니다.

---

## 4-2. 중복 판정 키 7개 필드 설명

> 1. 공급사명
> 2. 정규화 구매 품목 명 (정규화 불가능한 경우 원문 품목명)
> 3. 규격
> 4. 단위
> 5. 기존 단가
> 6. 변경 단가
> 7. 적용일

-> 위 키 값들을 `|` 를 구분자로 연결하여 key 값을 만들었습니다.

```
ex)
"가온푸드(예시)|토마토살사소스|4kg/PK|PK|32000|33600|2026-08-01"
```

---

# 5. OCR 구현 현황

## 5-1. OCR 지원 여부 및 지원 파일 형식

> 1. Naver Clova OCR 을 활용하여 OCR 기능을 구현하였습니다.
> 2. PDF 파일 및 이미지 파일 (png, jpg 등 이미지 형식의 파일) 을 지원합니다.

---

## 5-2. 구현 방식

> * Naver Clova OCR 에 특정 파일 (PDF or IMAGE) 파일을 api 로 요청을 합니다.
> * 요청에 대한 OCR 결과를 응답 받습니다. (좌 -> 우 방향 씩 아래로 내려오며 단어 단위로 추출합니다.)
> * 응답 형식과 파일 형식에 맞게 필요한 정보(구매 물품 정보) 만을 추출하여 예외 탐지 및 db 저장을 수행합니다.

* 참고 : 현재는 제공해 주신 PDF 및 이미지 파일의 아래 두가지 형식에 맞게 구현하였습니다.
    * a. 헤더 및 표 1개에서 데이터 추출
    * b. 표 2개에서 필요 데이터 추출
* 형식이 수정되거나 추가 될 경우 구현 방식 또한 추가되거나 수정 될 수 있습니다.
*

---

## 5-3. 잘 안되는 케이스와 미지원 형식 명시

> * 제공해 주신 PDF 및 이미지 에서, 이미지 파일의 경우 OCR 의 인식이 안되는 부분이 존재 했습니다.
> * 불필요한 데이터가 인식이 되지 않는 경우는 해당 데이터는 추출하지 않음으로써 해결하였습니다.
> * 필요 데이터가 인식이 되지 않는 경우는 `4. 예외 탐지 규칙` 에 따라 예외가 탐지되도록 하였습니다.
> * 이외에도 PDF 및 IMAGE(png, jpg 등) 외의 파일 입력은 OCR을 지원하지 않습니다.

---

# 6. 구현하지 못한 부분 & 알려진 오류

## 미구현 기능

- **파일 내용 기반 중복 파일 판별 기능**
    - 현재 파일명이 동일한 경우가 아닌, **파일명이 서로 다르더라도 파일 내부의 실제 내용이 동일한 파일을 식별하는 기능**은 구현하지 못했습니다.

## 향후 개선 계획

- 파일 업로드 시 파일 내용에 대한 **해시값(Hash)** 을 생성하고, 기존 파일의 해시값과 비교하여 파일명이 다르더라도 내용이 동일한 파일을 식별할 수 있도록 개선할 예정입니다.
- 이를 통해 동일한 파일의 중복 업로드를 사전에 방지하고, 사용자에게 중복 여부를 명확하게 안내할 계획입니다.

---

# 7. 소스 코드 정보

## 7-1. 주요 디렉토리 구조

```
compozi-ai/
├── src/main/java/com/bosalpim/compozi_ai/
│   ├── CompoziAiApplication.java          # Spring Boot 진입점
│   ├── config/                            # Spring 설정 (Swagger, S3, JPA, 비동기 등)
│   ├── domain/
│   │   ├── document/                      # 증빙 수집·파싱·정규화 (핵심)
│   │   │   ├── controller/                # FileController — 파일/수기/OCR 입력 API
│   │   │   ├── service/                   # FileService, ItemService
│   │   │   ├── entity/                    # File, Item
│   │   │   ├── repository/                # JPA·QueryDSL 저장소
│   │   │   ├── component/
│   │   │   │   ├── parser/                # CSV·Excel·OCR 파서
│   │   │   │   ├── mapper/                # ItemNameMapper, 정규화 규칙·사전
│   │   │   │   ├── validator/             # 중복·규격·단위 검증
│   │   │   │   └── ocr/                   # Naver Clova OCR 연동
│   │   │   └── dto/                       # 요청·응답 DTO
│   │   ├── inbox/                         # 검수·트리아지
│   │   │   ├── controller/                # ItemInboxController
│   │   │   ├── service/                   # InboxService
│   │   │   ├── entity/                    # Issue, ChangeLog, DuplicatedGroup
│   │   │   └── repository/                # 이슈·변경이력·중복그룹 저장소
│   │   ├── export/                        # 승인 데이터 내보내기
│   │   │   ├── controller/                # ExportController
│   │   │   ├── service/                   # ExportService
│   │   │   ├── s3/                        # S3 업로드·Presigned URL
│   │   │   └── entity/                    # ExportHistory
│   │   └── dashboard/                     # 대시보드 집계
│   │       ├── controller/                # DashboardController
│   │       └── service/                   # DashboardService
│   └── general/                           # 공통 응답 래핑·예외 처리
│       ├── advice/                        # ApiResponseAdvice, GlobalExceptionHandler
│       ├── response/                      # ApiResponse, PageResponseDto
│       ├── exception/                     # CustomException 등
│       └── enums/                         # BadStatusCode
├── src/main/resources/
│   ├── dictionary/item-dictionary.json    # 창업팀 제공 품목명 정규화 사전
│   └── messages.properties                # 에러 메시지
├── src/test/                              # 단위·통합 테스트
├── compose.yml                            # 로컬 MySQL 컨테이너
├── Dockerfile                             # 배포용 JAR 이미지
├── build.gradle                           # Gradle 빌드 설정 (Java 17, Spring Boot 3.5)
└── .github/workflows/CI-CD.yml            # main 머지 시 빌드·배포 파이프라인
```

---

## 7-2. 핵심 모듈 설명

### domain/document — 구매 증빙 자료 수집 및 분석

구매 아이템 정보를 수집하고 품목명을 정규화한 뒤 DB에 저장하는 핵심 모듈입니다.

| 구성 요소                               | 역할                                                                                         |
|-------------------------------------|--------------------------------------------------------------------------------------------|
| `FileController`                    | 파일 업로드(`POST /api/v1/document`), 수기 입력(`POST /api/v1/manual-document`), OCR 미리보기·확정 API 제공 |
| `FileService`                       | 확장자에 맞는 `FileParser` 선택 → `File` 메타 저장 → `ItemService`로 품목 생성                              |
| `ItemService`                       | 파싱 결과를 `Item` 엔티티로 변환, 중복·규격·단위·필수값 예외 탐지 및 `Issue` 생성                                     |
| `CsvParser` / `ExcelFileParser`     | CSV·XLSX 행 파싱 (Apache POI, OpenCSV)                                                        |
| `OcrType1Parser` / `OcrType2Parser` | Naver Clova OCR 결과에서 표 형식별 데이터 추출                                                          |
| `ItemNameMapper`                    | 사전 완전 일치 → 규칙 체인(약어 전개·단위 제거·띄어쓰기) 순으로 정규화. 미변경 시 `null`(→ `데이터 부족`)                       |
| `ItemDocumentDuplicateValidator`    | 7개 필드 기반 중복 키 생성·탐지                                                                        |
| `ItemSpecAndUnitValidator`          | 규격·단위 불일치 탐지                                                                               |

**입력 흐름 요약**

```
파일 업로드 → FileValidator → FileParser.parse()
  → ItemNameMapper.map() (정규화)
  → 중복·규격·단위·필수값 검증
  → Item + Issue 저장
```

---

### domain/inbox — 검수 도메인

검수자가 품목을 조회·수정·승인·반려하는 모듈입니다.

| 구성 요소                 | 역할                                                                                    |
|-----------------------|---------------------------------------------------------------------------------------|
| `ItemInboxController` | 목록 조회, 상세 조회, 승인/반려(단건·일괄), 수정, 삭제, 중복 그룹 조회 API                                      |
| `InboxService`        | 검수 상태(`ReviewStatus`) 전이, `ChangeLog` 기록, 미해결 `Issue` 검사 후 승인 차단                      |
| `Issue`               | `missing_required`, `spec_mismatch`, `unit_mismatch`, `duplicate_suspected` 4가지 예외 유형 |
| `ChangeLog`           | 필드 수정·승인·반려·삭제 등 검수 이력 저장                                                             |
| `DuplicatedGroup`     | 중복 의심 품목을 그룹으로 묶어 함께 검토                                                               |

---

### domain/export — 승인 데이터 내보내기

승인(`approved`)된 품목을 JSON 또는 CSV로 변환해 S3에 저장하고 다운로드 URL을 제공합니다.

| 구성 요소              | 역할                                                            |
|--------------------|---------------------------------------------------------------|
| `ExportController` | 내보내기 요청(`POST /api/v1/exports`), 이력 조회, Presigned 다운로드 URL 발급 |
| `ExportService`    | 승인 품목 조회 → JSON/CSV 직렬화 → S3 업로드 → `ExportHistory` 기록         |
| `S3Service`        | AWS S3 파일 업로드 및 Presigned URL 생성                              |

---

### domain/dashboard — 대시보드

검수 현황과 이슈 통계를 집계합니다.

| 구성 요소                 | 역할                                                                   |
|-----------------------|----------------------------------------------------------------------|
| `DashboardController` | `GET /api/v1/dashboard/summary`, `GET /api/v1/dashboard/issue-stats` |
| `DashboardService`    | 상태별 건수, 이슈 유형별 건수 집계                                                 |

---

### general — 공통 인프라

| 구성 요소                    | 역할                                                                       |
|--------------------------|--------------------------------------------------------------------------|
| `ApiResponseAdvice`      | `@ApiSuccess` 어노테이션이 붙은 응답을 `{ status, code, message, data }` 형식으로 자동 래핑 |
| `GlobalExceptionHandler` | `CustomException` + `BadStatusCode` 기반 통일 에러 응답                          |
| `BadStatusCode`          | HTTP 상태·에러 코드·메시지 정의                                                     |

---

## 7-3. 실행 방법

### 7-3-1. 접속 주소 및 실행 방법

https://mvp-hackathon-front-end.vercel.app/

* 따로 인증/인가 는 구현하지 않았기 때문에 바로 접속하여 사용이 가능합니다.
* 실행 방법은 첨부된 ppt 파일을 참고 하시면 됩니다.

---

### 7-3-2. 로컬 환경 설정

#### 사전 요구사항

| 항목     | 버전             | 참고 파일                                                  |
|--------|----------------|--------------------------------------------------------|
| Java   | 17             | `build.gradle` (`java.toolchain.languageVersion = 17`) |
| Docker | MySQL 컨테이너 실행용 | `compose.yml`                                          |
| Gradle | Wrapper 포함     | `gradlew`, `gradlew.bat`                               |

#### 1) MySQL 실행

프로젝트 루트의 `compose.yml`로 로컬 DB를 기동합니다.

```bash
docker compose -f compose.yml up -d
```

`compose.yml` 기준 접속 정보:

| 항목         | 값                        |
|------------|--------------------------|
| 컨테이너명      | `mvp-local-db-container` |
| 호스트 / 포트   | `127.0.0.1:3306`         |
| DB명        | `local-db`               |
| 사용자 / 비밀번호 | `mvp` / `1234`           |
| root 비밀번호  | `1234`                   |

#### 2) application.yml 설정

`src/main/resources/application.yml`은 `.gitignore`에 포함되어 저장소에 없습니다.  
로컬에서 직접 생성한 뒤, DB·OCR·S3 등 환경 변수를 설정합니다.

#### 3) 빌드 및 실행

```bash
# 전체 빌드 (테스트 포함)
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# JAR 빌드 후 실행
./gradlew bootJar -x test
java -jar build/libs/compozi-ai-0.0.1-SNAPSHOT.jar
```

> `settings.gradle`의 `rootProject.name`(`compozi-ai`)과 `build.gradle`의 `version`(`0.0.1-SNAPSHOT`) 기준 JAR 파일명입니다.

#### 4) API 확인

| 항목            | URL                                     |
|---------------|-----------------------------------------|
| Swagger UI    | `http://localhost:8080/swagger-ui.html` |
| API Base Path | `/api/v1`                               |

#### 5) 테스트 실행

```bash
./gradlew test
```

---






