# 보살핌 팀 CompoziAI

## 커밋 컨벤션

이 프로젝트는 커밋 메시지 컨벤션을 git hook(`commit-msg`)으로 강제합니다.
형식에 맞지 않는 커밋 메시지는 커밋이 거부됩니다.

### 최초 1회 설정 (클론 후 필수)

> ⚠️ 아래 명령어는 반드시 `.git` 폴더가 있는 프로젝트 루트 경로에서, **Git Bash**로 실행해야 합니다.

\`\`\`bash
git config core.hooksPath .githooks
chmod +x .githooks/commit-msg
\`\`\`

### 커밋 메시지 형식

\`\`\`
<type>(<scope>): <subject>
\`\`\`

### 허용 타입

| 타입 | 설명 |
|---|---|
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 |
| style | 코드 포맷팅, 세미콜론 누락 등 (로직 변경 없음) |
| refactor | 코드 리팩토링 |
| test | 테스트 코드 추가/수정 |
| chore | 빌드, 설정 파일 수정 |
| perf | 성능 개선 |
| build | 빌드 시스템, 외부 종속성 변경 |
| ci | CI 설정 변경 |
| revert | 커밋 되돌리기 |

### 규칙

- subject는 50자 이내
- 마침표(.) 금지
- 한글 또는 영어 모두 가능

### 예시

\`\`\`
feat: 로그인 기능 추가
fix(auth): 토큰 만료 버그 수정
refactor(user): 회원가입 로직 분리
\`\`\`