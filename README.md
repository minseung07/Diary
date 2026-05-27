# Diary Lite

Diary Lite는 개인 일기를 빠르게 쓰고 로컬에서 관리하기 위한 Android 앱입니다. 일기 데이터는 기본적으로 기기에만 저장되며 서버 동기화, 광고, 분석 기능을 사용하지 않습니다.

## 주요 기능

- 일기 작성, 수정, 삭제
- 최근 일기와 전체 일기 목록
- 날짜별 일기 확인을 위한 달력
- 제목과 내용 기반 로컬 검색
- 고정 기분 선택
- 전체 일기 Markdown 내보내기
- Material 3 기반 라이트/다크 모드

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
