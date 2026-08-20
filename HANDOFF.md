# PodGauge 작업 인계

## 1. 현재 상태

Android Studio에서 열고 빌드할 수 있는 Kotlin/Jetpack Compose 기반 PodGauge MVP 골격이 완성되었다.
Android 버전에 따른 Bluetooth 런타임 권한 처리, BLE 스캔, Apple manufacturer data(0x004C) 추출,
AirPods 형태 패킷 판별, StateFlow 기반 MVVM 전달, Material 3 UI가 연결되어 있다.

AirPods 광고 포맷은 Apple/Android의 공식 공개 규격이 아니다. Galaxy 실기기에서 직접 캡처한
`07 0F 00 0E 20 00 C5 85 ...` 샘플과 여러 공개 디코더에서 일치하는 필드만 적용했다. 현재 모델과
left/right/case 배터리는 파싱하며, 해석이 더 필요한 충전 여부는 `null`로 안전하게 유지한다.

## 2. 완료된 작업

- Gradle Kotlin DSL Android 앱 프로젝트 생성
- Kotlin, Jetpack Compose, Material 3, MVVM, Coroutines/StateFlow 구성
- `minSdk 26`, `compileSdk 35`, `targetSdk 35` 설정
- Android 12 이상 `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT` 권한 처리
- Android 11 이하 `ACCESS_FINE_LOCATION` 및 legacy Bluetooth 권한 처리
- Compose Runtime Permission 요청 UI 구현
- `BluetoothLeScanner` 기반 BLE scan 시작/중지 구현
- Apple company ID `0x004C` ScanFilter 및 manufacturer data 추출 구현
- 관찰된 Apple Proximity Pairing envelope `07 19`와 길이를 이용한 보수적 AirPods 판별
- 중복 패킷 상태 발행을 최대 5초에 한 번으로 제한
- Debug 빌드에서만 AirPods raw manufacturer data Hex Logcat 출력
- Scan 실패, Bluetooth OFF, BLE 미지원, 권한 없음, 미감지 UI 구현
- Left/Right/Case 독립 Card, Dark Mode/동적 시스템 색상, fake Preview 구현
- 배터리 UI 표시 전 0~100 범위 재검증 및 `null`일 때 `--` 표시
- parser 단위 테스트 작성 및 통과
- Galaxy SM-S936N에서 Apple manufacturer data 수신 및 `07 0F` AirPods 패킷 감지 확인
- 실제 캡처 기반 모델 ID(`0E20` = AirPods Pro) 및 배터리 nibble 파싱 구현
- Debug APK 빌드 및 Android Lint 통과

## 3. 미완료 작업

- 실제 패킷에 근거한 left/right/case charging bit 파싱
- AirPods Pro 외 모델의 model identifier와 배터리 방향 실기기 교차 검증
- Galaxy 실기기에서 권한 요청, Bluetooth 토글, 장시간 scan 동작 확인
- AirPods 세대별 패킷 차이가 확인될 경우 parser 분리

## 4. 다음 작업 순서

1. Debug APK를 Galaxy 실기기에 설치하고 Nearby devices 권한 요청을 확인한다.
2. 화면의 Left 50%, Right 100%, Case 80%가 실제 AirPods 상태와 일치하는지 확인한다.
3. 이어버드를 사용/충전하며 여러 시점의 Hex와 실제 배터리 값을 함께 기록한다.
4. 모델별로 여러 샘플을 비교해 charging flag와 좌우 방향을 추가 검증한다.
5. 검증된 근거가 확보된 charging 필드만 `AirPodsParser.kt`에 구현한다.
6. 다른 AirPods 모델의 identifier 매핑을 실기기로 교차 검증한다.
7. parser 단위 테스트에 익명화된 실제 샘플과 경계값을 추가한다.
8. `testDebugUnitTest`, `lintDebug`, `assembleDebug`를 다시 실행한다.

## 5. 확인이 필요한 사항

- Galaxy의 Android 11 이하/12 이상에서 각각 올바른 권한 화면이 표시되는지
- 권한 거부 및 영구 거부 시 crash 없이 안내 화면에 머무는지
- Bluetooth OFF 상태에서 안내가 보이고, ON 후 Try again으로 scan이 재개되는지
- AirPods 각 모델에서 manufacturer data가 `0x004C` 및 자체 길이가 일치하는 `07 xx` 형태로 수신되는지
- 화면이 꺼지거나 앱이 백그라운드로 갈 때 scan이 정상 중지되는지
- 동일 패킷 반복 시 UI가 과도하게 갱신되지 않는지
- 제조사/모델별 실제 charging flag
- 현재 캡처의 Left 50%, Right 100%, Case 80% 해석이 실제 기기 값과 일치하는지

## 6. 빌드 상태

마지막 기능/테스트/APK 빌드 명령:

```text
.\gradlew.bat testDebugUnitTest assembleDebug

Result: BUILD SUCCESSFUL
45 actionable tasks: 2 executed, 43 up-to-date
```

추가 정적 분석 명령:

```text
.\gradlew.bat lintDebug

Result: BUILD SUCCESSFUL
```

생성 APK: `app/build/outputs/apk/debug/app-debug.apk`

## 7. 주요 파일

- `app/build.gradle.kts`: Android/Compose 빌드 설정과 의존성
- `app/src/main/AndroidManifest.xml`: BLE feature 및 버전별 권한
- `app/src/main/java/com/example/podgauge/bluetooth/AirPodsScanner.kt`: BLE scan, Apple data 추출, 중복 제한, Debug Hex 로그
- `app/src/main/java/com/example/podgauge/bluetooth/AirPodsParser.kt`: 패킷 판별과 보수적 parser 진입점
- `app/src/main/java/com/example/podgauge/model/AirPodsBatteryState.kt`: 배터리 상태 모델
- `app/src/main/java/com/example/podgauge/model/AirPodsModel.kt`: AirPods model enum
- `app/src/main/java/com/example/podgauge/repository/AirPodsRepository.kt`: scanner StateFlow 전달
- `app/src/main/java/com/example/podgauge/viewmodel/MainViewModel.kt`: UI용 상태 및 scan lifecycle 진입점
- `app/src/main/java/com/example/podgauge/MainActivity.kt`: 권한 launcher와 Activity lifecycle
- `app/src/main/java/com/example/podgauge/ui/MainScreen.kt`: 화면 상태 및 Preview
- `app/src/main/java/com/example/podgauge/ui/components/BatteryCard.kt`: 배터리 Card
- `app/src/test/java/com/example/podgauge/bluetooth/AirPodsParserTest.kt`: parser 안전성 테스트

## 8. 변경 금지 사항

확정되지 않은 packet offset이나 bit를 임의로 추가하지 않는다. 유효성이 확인되지 않은 배터리는 `null`, 모델은
`UNKNOWN`으로 유지한다. 배터리 값은 반드시 0~100 범위를 검증한다. Release 빌드에 raw Bluetooth 로그를 넣지 않는다.

특히 아래 함수 이름은 리팩터링 중에도 변경하거나 대체하지 않는다.

```kotlin
startAirPodsScan()
stopAirPodsScan()
parseAirPodsData()
isAirPodsPacket()
getAirPodsModel()
```
