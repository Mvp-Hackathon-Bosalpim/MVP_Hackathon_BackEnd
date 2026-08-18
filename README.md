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

# 6. 구현하지 못한 부분 & 알려진 오류

## 미구현 기능

- **파일 내용 기반 중복 파일 판별 기능**
    - 현재 파일명이 동일한 경우가 아닌, **파일명이 서로 다르더라도 파일 내부의 실제 내용이 동일한 파일을 식별하는 기능**은 구현하지 못했습니다.

## 향후 개선 계획

- 파일 업로드 시 파일 내용에 대한 **해시값(Hash)** 을 생성하고, 기존 파일의 해시값과 비교하여 파일명이 다르더라도 내용이 동일한 파일을 식별할 수 있도록 개선할 예정입니다.
- 이를 통해 동일한 파일의 중복 업로드를 사전에 방지하고, 사용자에게 중복 여부를 명확하게 안내할 계획입니다.

### 정규화 프로세스

```
1. 창업팀 제공 20row 의 사전 탐색
2. '냉감튀', 'S/O' 와 같은 약어 전개
3. 단위 패턴 제거 (K, G 등)
4. 특정 단어 기준 띄어쓰기 적용
5. 적용되지 않은 단어는 AI 기반 정규화 수행
```
