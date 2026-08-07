# CLAUDE.md

이 저장소에서 작업하는 Claude Code를 위한 가이드입니다.

## 프로젝트 개요

CompoziAI — "보살핌" 팀 MVP 해커톤용 Spring Boot 3.5 / Java 17 백엔드.
구매/조달 증빙(엑셀/CSV/수기)을 수집하고 품목명을 정규화(사전 → 규칙 → Gemini 폴백)하여 검토용 저장.

## 빌드 & 실행

```bash
./gradlew build
./gradlew bootJar   # CI용, -x test
./gradlew test
```

로컬 DB: `docker compose up -d` (MySQL, `local-db`/`mvp`/`1234`, 3306).
`ddl-auto: create` — 재시작마다 스키마 드롭 후 재생성, 데이터 유지 가정 금지.

`application.yml`(Gemini API 키 포함)은 로컬 편의상 커밋됨, CI/CD에서 `APPLICATION_PROPERTIES` secret으로 덮어씀.

Swagger: `/swagger-ui.html`

## 커밋 컨벤션

git hook으로 강제. 클론 후 1회 실행:

```bash
git config core.hooksPath .githooks
chmod +x .githooks/commit-msg
```

형식: `<type>(<scope>): <subject>`, 50자 이내, 마침표 금지.

## 아키텍처

**패키지**: `domain/document`(핵심, 파일 수집·정규화) / `domain/inbox`(검토·트리아지, 대부분 엔티티만 존재) / `general`(공통).

**업로드 흐름**: `FileController` → `FileValidator` → `FileService`. `List<FileParser>`에서 `supports()`로 파서 선택(`CsvParser`/
`ExcelFileParser`) → `File` 저장 → 행 파싱 → `ItemService`가 `ItemNameMapper`로 정규화 후 `Item` 생성. 수기 입력(
`POST /api/v1/manual-document`)은 파싱/File 저장 없이 바로 `Item` 생성.

`FileService.createCommonFile`에 정규화/중복탐지/유효성검사 연결 TODO 있음 — 미완성 영역.

**정규화 파이프라인** (`ItemNameMapper.map`, 첫 변경 단계에서 short-circuit):

1. 사전 완전 일치(`item-dictionary.json`)
2. 규칙 체인(`List<ItemNormalizationRule>` 순차): 약어 전개 → 접미사 제거 → 띄어쓰기 교정. 새 규칙은 인터페이스 구현 + `@Component`만 하면 됨.
3. AI 폴백: Gemini(`gemini-flash-latest`) 호출, 실패 시 로그만 남기고 빈 `Optional`.
4. 끝까지 미변경 시 `"데이터 부족"` 리터럴 반환(null 아님).

**응답/에러**: 성공은 `@ApiSuccess`로 `ApiResponseAdvice`가 자동 래핑. 에러는 `BadStatusCode` enum + `CustomException`,
`GlobalExceptionHandler`가 통일 처리.

**CI/CD**: `main` 머지 시 jar 빌드(테스트 생략) → Docker 빌드/푸시 → EC2에서 `docker compose pull && up -d`. PR 시점 CI 없음 — 머지 전 로컬 테스트
필수.

## 코딩하기 전에 생각하기

**가정하지 마세요. 혼란을 숨기지 마세요. 표면적인 트레이드오프입니다.**.

구현하기 전에:

- 자신의 가정을 명시적으로 설명하세요. 불확실하다면 물어보세요.
- 여러 해석이 존재한다면, 제시하세요 - 조용히 선택하지 마세요.
- 더 간단한 접근 방식이 존재한다면 그렇게 하세요. 필요할 때는 뒤로 미뤄주세요.
- 불분명한 것이 있으면 멈추세요. 무엇이 헷갈리는지 말씀해 주세요. 물어보세요.

## 2. 단순함 우선

**문제를 해결하는 최소 코드. 추측은 없습니다.**.

- 요청된 기능 이상의 기능은 없습니다.
- 일회용 코드에 대한 추상화가 없습니다.
- 요청되지 않은 "유연성"이나 "구성 가능성"은 없습니다.
- 불가능한 시나리오에 대한 오류 처리가 없습니다.
- 200줄을 쓰고 50줄이 될 수 있다면 다시 작성하세요.

스스로에게 물어보세요: "고위 엔지니어가 이것이 너무 복잡하다고 말할까요?" 만약 그렇다면, 간단하게 하세요.

## 3. 외과적 변화

**꼭 만져야 할 것만 만져보세요. 자신의 엉망진창만 정리하세요.**.

기존 코드를 편집할 때:

- 인접한 코드, 댓글 또는 형식을 "개선"하지 마세요.
- 깨지지 않은 것들은 리팩터링하지 마세요.
- 기존 스타일을 다르게 하더라도 매치하세요.
- 관련 없는 데드 코드를 발견하면 삭제하지 말고 언급하세요.

당신의 변화가 고아를 만들 때:

- 변경 사항이 사용되지 않게 만든 가져오기/변수/기능을 제거합니다.
- 요청이 없으면 기존의 데드 코드를 제거하지 마십시오.

테스트: 변경된 모든 줄은 사용자의 요청에 직접 따라 추적해야 합니다.

## 4. 목표 지향적 실행

**성공 기준을 정의합니다. 검증될 때까지 반복합니다.**.

작업을 검증 가능한 목표로 변환합니다:

- "검증 추가" → "잘못된 입력에 대한 테스트를 작성한 다음 통과시킵니다."
- "버그 수정" → "버그를 재현하는 테스트를 작성한 다음 통과시킵니다."
- "리팩터 X" → "시험 전후에 합격 보장"

다단계 작업의 경우 간단한 계획을 작성하세요:

```
1. [단계] → 확인: [확인]
2. [단계] → 확인: [확인]
3. [단계] → 확인: [확인]
```

강력한 성공 기준은 독립적으로 반복할 수 있게 해줍니다. 약한 기준("작동하게 만들기")은 지속적인 설명이 필요합니다.
