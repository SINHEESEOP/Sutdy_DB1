# 데이터베이스 접근 기술 학습 프로젝트 (feat. 스프링)

JDBC의 기본 방식부터, 자바 표준인 DataSource, 커넥션 풀, 트랜잭션 관리에서 좋은 코드를 작성하기 위해 이후부터는 스프링 프레임워크가 데이터베이스 접근을 어떻게 단계별로 추상화하고 편리하게 제공하는지 학습하는 실습 프로젝트입니다.

## 프로젝트 개요

Java 데이터베이스 접근 기술이 어떻게 발전해왔는지를 단계별로 학습할 수 있는 프로젝트입니다. 
기본 JDBC부터 시작해서 커넥션 풀, 트랜잭션 관리, Spring 트랜잭션 추상화까지 각 단계의 문제점과 해결 방안을 실제 코드로 구현했습니다.

## 학습 목표

- JDBC 기본 원리와 문제점 이해
- 커넥션 풀 도입의 필요성과 효과
- 트랜잭션 관리 방식의 발전 과정
- Spring 트랜잭션 추상화의 장점
- 코드 중복 제거와 관심사 분리

## 기술 스택

- Java 17
- Spring Boot 3.5.0
- Spring JDBC
- H2 Database
- Lombok
- JUnit 5

## 단계별 발전 과정

### 1단계: 순수 JDBC (V0)

**구현 클래스**
- MemberRepositoryV0.java
- MemberServiceV1.java

**특징**
- DriverManager를 사용한 직접적인 DB 연결
- 매번 새로운 커넥션 생성/해제
- 수동 리소스 관리

**문제점**
- 매번 새로운 커넥션 생성으로 인한 성능 오버헤드
- 수동 close() 호출로 인한 리소스 누수 위험
- 트랜잭션 미지원으로 데이터 정합성 보장 불가

```java
// 기본 JDBC 패턴
Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
PreparedStatement pstmt = con.prepareStatement(sql);
// 비즈니스 로직
con.close(); // 수동 리소스 해제
```

### 2단계: 커넥션 풀 도입 (V1)

**구현 클래스**
- MemberRepositoryV1.java

**개선사항**
- DataSource 인터페이스 도입으로 추상화
- 커넥션 풀 사용으로 성능 최적화
- Spring JdbcUtils 활용으로 안전한 리소스 해제

**장점**
- 커넥션 재사용으로 성능 향상
- 예외 상황에서도 안전한 리소스 해제
- DataSource 구현체 교체 가능

```java
// 커넥션 풀 활용
private final DataSource dataSource;
Connection con = dataSource.getConnection(); // 풀에서 재사용
JdbcUtils.closeConnection(con); // 안전한 해제
```

### 3단계: 트랜잭션 도입 - 매개변수 방식 (V2)

**구현 클래스**
- MemberRepositoryV2.java
- MemberServiceV2.java

**개선사항**
- 트랜잭션 지원으로 데이터 정합성 보장
- Connection을 매개변수로 전달
- 수동 commit/rollback 제어

**문제점**
- 트랜잭션 코드가 서비스 로직에 침투
- 서비스가 JDBC에 직접 의존
- 비즈니스 로직과 기술적 관심사 혼재

```java
// 수동 트랜잭션 관리
Connection con = dataSource.getConnection();
con.setAutoCommit(false);
try {
    bizLogic(con, fromId, toId, money);
    con.commit();
} catch (Exception e) {
    con.rollback();
}
```

### 4단계: 트랜잭션 매니저 도입 (V3_1)

**구현 클래스**
- MemberRepositoryV3.java
- MemberServiceV3_1.java

**개선사항**
- Spring PlatformTransactionManager 도입
- 트랜잭션 동기화 매니저 활용
- 서비스 계층에서 Connection 매개변수 제거

**장점**
- 비즈니스 로직과 트랜잭션 로직 분리
- JDBC 외 다른 기술로 교체 가능
- Spring이 제공하는 안정적인 트랜잭션 관리

```java
// 트랜잭션 매니저 패턴
TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
try {
    bizLogic(fromId, toId, money); // Connection 매개변수 제거
    transactionManager.commit(status);
} catch (Exception e) {
    transactionManager.rollback(status);
}
```

### 5단계: 트랜잭션 템플릿 활용 (V3_2)

**구현 클래스**
- MemberServiceV3_2.java

**개선사항**
- TransactionTemplate 도입으로 보일러플레이트 코드 제거
- 템플릿 콜백 패턴 활용
- 코드 간소화

**장점**
- 트랜잭션 관리 코드 대폭 축소
- 개발자가 핵심 로직에만 집중 가능
- Spring이 모든 예외 상황 처리

```java
// 트랜잭션 템플릿 패턴
txTemplate.executeWithoutResult(status -> {
    try {
        bizLogic(fromId, toId, money); // 순수 비즈니스 로직만 남음
    } catch (SQLException e) {
        throw new IllegalStateException(e);
    }
});
```

### 6단계: JdbcTemplate 도입 (V4, V5)

**구현 클래스**
- MemberRepositoryV5.java
- MemberServiceV4Test.java

**개선사항**
- JdbcTemplate으로 SQL 실행 코드 대폭 간소화
- SQL 예외를 Spring의 DataAccessException으로 변환
- RowMapper를 통한 결과 매핑 자동화
- 완전한 JDBC 보일러플레이트 코드 제거

**장점**
- 개발자는 SQL 작성에만 집중 가능
- 자동 리소스 관리 (Connection, PreparedStatement, ResultSet)
- 일관된 예외 처리 및 변환
- Spring의 검증된 JDBC 추상화 활용

**주요 특징**
- `template.update()`: INSERT, UPDATE, DELETE 실행
- `template.queryForObject()`: 단건 조회
- `RowMapper`: ResultSet → 객체 변환 자동화
- 스프링 컨테이너와 완벽한 통합

```java
// JdbcTemplate 활용 패턴 🎯
@Override
public Member save(Member member) {
    String sql = "insert into member(member_id, money) values (?, ?)";
    template.update(sql, member.getMemberId(), member.getMoney());
    return member;
}

@Override
public Member findById(String memberId) {
    String sql = "select * from member where member_id = ?";
    return template.queryForObject(sql, memberRowMapper(), memberId);
}

// RowMapper로 결과 매핑 자동화 🔄
private RowMapper<Member> memberRowMapper() {
    return (rs, rowNum) -> {
        Member member = new Member();
        member.setMemberId(rs.getString("member_id"));
        member.setMoney(rs.getInt("money"));
        return member;
    };
}
```

## 진화 과정 요약

| 단계 | 기술 | 주요 개선점 | 남은 문제점 |
|------|------|-------------|-------------|
| V0 | 순수 JDBC | - | 성능, 안정성, 트랜잭션 |
| V1 | DataSource + 커넥션풀 | 성능 개선, 리소스 안전성 | 트랜잭션 부재 |
| V2 | 수동 트랜잭션 | 데이터 정합성 보장 | 코드 중복, 높은 결합도 |
| V3_1 | 트랜잭션 매니저 | 관심사 분리, 기술 독립성 | 보일러플레이트 코드 |
| V3_2 | 트랜잭션 템플릿 | 코드 간소화, 완전한 추상화 | JDBC 코드 중복 |
| V4, V5 | JdbcTemplate | SQL 실행 코드 완전 추상화, 예외 변환 | - |

## 핵심 학습 포인트

### 성능 관점
- V0→V1: 커넥션 풀로 인한 성능 향상
- 커넥션 생성 시간 vs 풀에서 가져오는 시간 비교

### 안정성 관점
- V1: JdbcUtils 도입으로 리소스 누수 방지
- V2: 트랜잭션으로 데이터 무결성 보장
- V3: Spring의 검증된 트랜잭션 관리

### 설계 관점
- 구체적 구현에서 인터페이스 기반 설계로 발전
- 비즈니스 로직과 기술적 관심사의 분리
- 템플릿 패턴을 통한 공통 로직 추출
