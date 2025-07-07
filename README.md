# 🛡️ 332-sentinel-server

<p align="center">
  <img src="https://github.com/user-attachments/assets/8a6660fc-4057-4ae7-88e8-46943649fbfb" width="400" />
</p>

### ✅ **개요**

- 삼삼이 플랫폼의 인증·인가를 통합 처리하는 게이트웨이 서버입니다.
- 아울러, 각 서비스의 로그를 중앙화하여 효율적으로 모니터링합니다.

### ⏱ **개발 기간**

- `v1.0.0` (`2025-04-09 ~ 2025-06-15`) : 로그 중앙화를 위한 `ELK` 스택 도입 및 플랫폼 통합 인증·인가 기능 개발
- `v1.0.1` (`2025-06-25 ~ 2025-07-07`) : 각 서비스가 계정 `ID`를 활용할 수 있도록 필터 추가
- `v1.0.2` (`2025-07-08`) : Blokey-Land 서비스명 변경에 따른 프록시 경로 오류 핫픽스

### 🛠 **프로젝트 환경**

- 언어 : `Java 21`
- 프레임워크 : `Spring boot 3.4.4`
- 데이터베이스 : `MongoDB`
- 캐시 : `Redis`
- IDE : `IntelliJ IDEA`
- `DevOps` : `Docker`, `Docker-compose`
- 보안 : `Spring Security`
- 빌드 도구 : `Gradle`
- 로깅 및 모니터링: `Logback`, `ELK`

### 🚀 **`Swagger`**

- http://localhost:8080/webjars/swagger-ui/index.html
