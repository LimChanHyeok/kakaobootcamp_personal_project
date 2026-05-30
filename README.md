## 프로젝트 소개

Spring Boot와 MySQL을 기반으로 구현한 게시글 커뮤니티 API 프로젝트입니다.  
JPA 대신 JDBC와 JdbcTemplate을 사용하여 SQL과 DB 연결 흐름을 직접 제어하며 개발했습니다.

## 설계 구조

프로젝트는 Controller, Service, Repository, Domain, DTO, Global Exception 계층으로 나누어 구현했습니다.

- Controller: HTTP 요청과 응답 처리
- Service: 비즈니스 로직과 트랜잭션 처리
- Repository: JdbcTemplate을 사용한 SQL 실행
- Domain: DB 테이블과 매핑되는 도메인 객체
- DTO: 요청/응답 데이터 전달 객체
- Global Exception: 공통 예외 처리

## 주요 기능

- 회원가입, 로그인, 회원 조회, 회원 수정, 회원 삭제
- 게시글 등록, 목록 조회, 상세 조회, 수정, 삭제
- 댓글 등록, 목록 조회, 수정, 삭제
- 게시글 좋아요 등록, 취소
- 이미지 파일 업로드
- Cursor 기반 페이징
- 공통 응답 및 예외 처리

## 설계 특징

게시글과 댓글 목록 조회는 cursor-based pagination 방식으로 구현했습니다.  
cursor는 마지막으로 조회한 데이터의 id를 JSON으로 만든 뒤 Base64로 인코딩하여 사용합니다.

댓글 수와 좋아요 수는 매번 COUNT 쿼리로 계산하지 않고, posts 테이블의 comment_count, like_count 컬럼에 저장했습니다.  
댓글 등록/삭제, 좋아요 등록/취소 시 해당 count 값을 증가 또는 감소시켜 조회 성능을 고려했습니다.

좋아요 여부는 post_like 테이블을 기준으로 판단합니다.  
post_like 테이블에는 user_id와 post_id를 저장하여 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인합니다.

현재는 JWT 인증을 도입하기 전 단계이므로 loginUserId를 임시로 1L로 고정하여 사용하고 있습니다.  
이후 JWT를 도입하면 토큰에서 사용자 id를 추출하여 기존 loginUserId 자리에 적용할 예정입니다.
