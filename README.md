# Diary Lite

Diary Lite는 오늘의 일기를 빠르게 쓰고 기기 안에서만 관리하기 위한 Android 일기 앱입니다. 일기 데이터는 기본적으로 로컬 Room 데이터베이스에 저장되며 서버 동기화, 계정, 광고, 분석 기능을 사용하지 않습니다.

<img width="1613" height="1040" alt="image" src="https://github.com/user-attachments/assets/e965a44f-1531-4aa8-a5a9-a049f07f6cf9" />

## 주요 기능

- 오늘 일기 작성, 수정, 삭제
- 홈의 최근 일기 목록
- 달력 기반 날짜별 일기 확인
- 전체 일기 기록 목록
- 제목과 내용 기반 로컬 검색
- 전체 일기 Markdown 내보내기
- Material 3 기반 라이트/다크 모드

## 화면 구성

- 하단 탭: 달력, 홈, 기록
- 홈: 최근 일기와 하단 중앙의 일기 쓰기 버튼
- 달력: 일기를 쓴 날짜는 배경색으로 표시하고, 오늘은 굵은 글자와 테두리로 표시
- 기록: 모든 일기를 최신순으로 확인
- 검색/설정: 홈 상단 아이콘에서 접근

## 작성 정책

Diary Lite는 당일 일기 작성을 기준으로 동작합니다.

- 새 일기는 항상 오늘 날짜로 저장됩니다.
- 작성 시 날짜, 시간, 기분은 직접 지정하지 않습니다.
- 시간과 기분은 비어 있는 상태로 저장됩니다.
- 일기 내용은 입력한 줄바꿈을 보존합니다.

## 개인정보

- 일기 데이터는 기본적으로 기기에만 저장됩니다.
- 서버 동기화, 광고, 분석 기능은 사용하지 않습니다.
- 네트워크 API나 계정 시스템을 사용하지 않습니다.
- Markdown 내보내기는 사용자가 직접 실행할 때만 Android Storage Access Framework를 통해 저장합니다.

## 개발 환경

- Android Studio 또는 Android SDK가 설치된 로컬 환경
- JDK 17
- Gradle Wrapper 사용

Android SDK 경로가 자동으로 잡히지 않으면 `ANDROID_HOME` 또는 `ANDROID_SDK_ROOT`를 설정합니다.

## 실행

Android Studio에서 프로젝트를 연 뒤 `app` 구성을 선택하고 연결된 Android 기기 또는 에뮬레이터에서 실행합니다.

터미널에서는 디버그 APK를 빌드한 뒤 연결된 기기에 설치할 수 있습니다.

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 빌드와 검증

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

샌드박스처럼 홈 디렉터리가 쓰기 불가능한 환경에서는 Gradle 캐시 위치를 쓰기 가능한 경로로 지정합니다.

```bash
GRADLE_USER_HOME=/tmp/diary-gradle-home \
ANDROID_HOME=/path/to/android/sdk \
ANDROID_SDK_ROOT=/path/to/android/sdk \
./gradlew testDebugUnitTest
```

실제 기기나 에뮬레이터가 없는 환경에서는 `connectedAndroidTest`를 건너뛰고 단위 테스트, 린트, 디버그 빌드 결과로 검증합니다.

## 라이선스

이 프로젝트는 MIT License로 배포됩니다. 자세한 내용은 [LICENSE](LICENSE)를 확인하세요.
