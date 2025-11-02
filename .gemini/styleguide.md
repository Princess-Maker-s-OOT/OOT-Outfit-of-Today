# Gemini Instruction Guide

코드리뷰 유의 사항: 코드리뷰는 한글로 답변해줘.

## 1. 패키지 구조

```
* common
   * common
      * advice
      * config
      * entity
      * exception
      * init
      * response
      * service
      * util
* domain
   * closet
      * controller
      * dto
         * request
         * response
      * entity
      * exception
      * repository
      * service
         * CommandService (CUD)
         * QueryService (R)
```

---

## 2. 클래스 네이밍

### DTO

```java
// Request DTO: 대문자로 시작, 도메인명 + 동작 + Request
ClosetCreateRequest
        UserSignupRequest

// Response DTO: 대문자로 시작, 도메인명 + 동작(필요 시) + Response
ClosetResponse
        UserResponse
```

### Service

```java
// Interface
ClosetCommandService.java
ClosetQueryService.java

// Implementation
ClosetCommandServiceImpl.java
ClosetQueryServiceImpl.java
```

---

## 3. Naming Conventions (정적 팩토리 메서드)

### from

* **용도**: 하나의 파라미터를 받아 형변환하여 객체를 생성합니다.
* **특징**: 단일 객체 → 다른 타입 객체로 변환
* **규칙**: 파라미터가 1개일 때 사용

```java
// Entity -> Response DTO (파라미터 1개)
UserResponseDto.from(user);

// 타입 변환 (파라미터 1개)
Instant instant = Instant.from(dateTime);
```

### of

* **용도**: 여러 파라미터를 받아 적절하게 통합/조합하여 객체를 생성합니다.
* **특징**: 여러 값을 조합해서 하나의 객체 생성
* **규칙**: 파라미터가 2개 이상일 때 사용

```java
// 여러 값을 조합 (파라미터 2개 이상)
UserResponse.of(user, orders);
EnumSet.

of(Role.ADMIN, Role.USER);
LocalDate.

of(2025,9,3);
```

### valueOf

* **용도**: `from`이나 `of`와 유사하지만, 더 자세하게 설명하는 이름입니다.
* **특징**: 타입 변환의 의미를 명확하게 표현

```java
BigInteger.valueOf(12345);
String.

valueOf(true);
```

### getInstance

* **용도**: 인스턴스를 반환하지만, 항상 새로운 객체를 생성하지는 않습니다.
* **특징**: 싱글톤(Singleton) 패턴에 자주 사용됩니다.

```java
Calendar.getInstance();
NumberFormat.

getInstance();
```

### create / newInstance

* **용도**: 호출할 때마다 항상 새로운 객체를 생성합니다.
* **특징**: 매번 새 인스턴스 보장

```java
Array.newInstance(String .class, 10);
```

### get[Type]

* **용도**: getInstance와 유사하지만, 다른 클래스의 객체를 생성할 때 사용됩니다.
* **특징**: [Type]에는 반환할 객체 타입이 들어갑니다.

```java
Files.getFileStore(path);
```

### new[Type]

* **용도**: get[Type]과 유사하지만, 매번 새로운 객체를 생성합니다.
* **특징**: 다른 타입의 새 인스턴스를 반환

```java
Files.newBufferedReader(path);
```

### 비즈니스 의미를 나타내는 메서드명 (★ 최우선 권장)

* **정적 팩토리 메소드 네이밍은 비즈니스를 나타낼 수 있으면 제일 좋습니다!**

```java
// from, of 보다 비즈니스 의미가 명확한 메서드명 사용
User.signUp(username, name, email, password, role);
Order.

placeOrder(userId, items);
Payment.

processPayment(orderId, amount);
```

---

## 4. 주석 처리

### 기본 규칙

* **간단한 경우**: 한 줄 주석 사용
* **클래스**: 자바독(Javadoc) 주석 사용
* **메서드**: 한 줄 주석 사용

```java
/**
 * 다른 클래스에서 메서드 및 클래스를 참조해야하는 경우
 * 여러줄 주석으로 작성
 * -> 다른 클래스에서 참조할 때 설명 확인 가능
 */
public class UserService {

    // 그 외에는 한 줄 주석을 쓰는 경우
    private final UserRepository userRepository;
}
```

---

## 5. 메서드 작성 규칙

### 메서드명 규칙

#### 도메인 명칭 붙이기

```java
// 단건 조회
public ClosetResponse getCloset(Long closetId);

// 다건 조회 - (s) 붙이기
public List<ClosetResponse> getClosets(Long closetId);

```

#### 생성 메서드

```java
// 일반 생성
createCloset()

createClothes()

// 회원가입
signup()  // 유저 생성의 경우 특별히 signup 사용
```

#### 조회 메서드 예시

```java
// 유저 화면에 보이는 전체 조회
getClosets()

// 필터 처리가 있는 조회
getClosets(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String sortBy
)
```

### 파라미터 개수에 따른 포맷팅

#### 0~1개 파라미터: 한 줄로 표기

```java
public ResponseEntity<UserResponse> getUserInfo(@PathVariable Long userId) {
    // 코드 구현
}
```

#### 2개 이상 파라미터: 첫 파라미터 기준 정렬

```java
// Controller 메서드
public ResponseEntity<PostListResponse> getPostsByPeriod(
        @RequestParam LocalDateTime startDate,
        @RequestParam LocalDateTime endDate,
        @RequestParam(defaultValue = "0") int page
) {
    // 코드 구현
}

// DTO, Entity의 경우
public record UserResponse(

        @Size(max = 20)
        Long id,

        @Size(max = 20)
        String name
) {
}
```

### 중간 변수 활용

* Service에서 받은 response 객체 반환 시 중간 변수를 명시적으로 활용
* Service에서도 return 전에 dto 변수를 명시해서 반환

```java

@PatchMapping
public ResponseEntity<UserResponse> updateUserInfo(
        @LoginUserResolver User user,
        @RequestBody @Valid UpdateUserInfoRequest request
) {
    UserResponse updatedUser = userService.updateUserInfo(user.getId(), request);

    return ResponseEntity.ok(updatedUser);
    // 변수는 어떤 객체를 받았는지 알기 쉽게 작명
}
```

### 줄바꿈 규칙

#### 1. 메서드 선언부와 코드 구현 사이

```java
public ResponseEntity<PostListResponse> getPosts(@RequestParam int page) {

    // 코드 구현
}
```

#### 2. 로직 단위 사이 & return/save 전

```java
// 예외 처리
if(userRepository.findByEmail(signupRequest.email()).

isPresent()){
        throw new

IllegalArgumentException("이미 존재하는 이메일입니다.");
}

// 비즈니스 로직 (비슷한 로직끼리 한 묶음)
User user = userMapper.toEntity(signupRequest);
user.

updatePassword(passwordEncoder.encode(signupRequest.password()));

// 저장
        userRepository.

save(user);

// save와 return 사이에 로직이 있으면 위아래 줄바꿈 추가
return UserResponse.

toResponse(User)；
```

### 중괄호 사용 규칙

* **한 줄 메서드와 if문이라도 항상 중괄호 사용**

```java
// 올바른 예시
@Transactional
public void method() {
    return ...;
}

if(a ==b){
        return true;
        }

// 잘못된 예시
public void method() {
    return ...;
}  // ❌

if(a ==b)return true;  // ❌
```

---

## 6. DTO 규칙

### Record 사용 자율

```

### Entity → Response DTO 변환

```java
// from, of만 구분해서 사용
public static AuthRegisterResponse from(User user) {

    return new AuthRegisterResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getName(),
            user.getRole(),
            user.getCreatedAt()
    );
}
```

---

## 7. Entity 규칙

* **Builder + 의미 있는 비즈니스 메서드** 조합

```java

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String username, String email, String password) {

        this.username = username;
        this.email = email;
        this.password = password;
    }

    // 정적 팩토리 메서드 - 비즈니스 의미 표현
    public static User signUp(String username, String email, String password) {

        return User.builder()
                .username(username)
                .email(email)
                .password(password)
                .build();
    }

    // 비즈니스 로직 메서드
    public void updatePassword(String encodedPassword) {

        this.password = encodedPassword;
    }
}
```

---

## 8. 도메인 간 상호작용 (DDD 원칙)

### 핵심 규칙

* **DDD(Domain-Driven Design) 아키텍처를 따릅니다.**
* **타 도메인 데이터가 필요할 때, 반드시 해당 도메인의 Service (interface)를 통해서만 접근합니다.**
* **타 도메인의 Repository를 직접 주입받는 것은 엄격히 금지됩니다.**

### 도메인 간 경계 준수

```java
// ✅ 올바른 예시: Interface를 통한 접근
@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    // 타 도메인 Service Interface만 참조
    private final ProductQueryService productQueryService;  // ✅
    private final UserQueryService userQueryService;        // ✅

    public OrderResponse createOrder(OrderCreateRequest request) {

        // 타 도메인 데이터는 Service를 통해서만 조회
        ProductResponse product = productQueryService.getProduct(request.productId());
        UserResponse user = userQueryService.getUser(request.userId());

        // Order 생성 로직...
        return OrderResponse.from(order);
    }
}

// ❌ 잘못된 예시 1: 구현체 직접 참조
@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private final ProductQueryServiceImpl productQueryServiceImpl;  // ❌ 구현체 참조 금지
}

// ❌ 잘못된 예시 2: 타 도메인 Repository 직접 주입
@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private final ProductRepository productRepository;  // ❌ 타 도메인 Repository 직접 주입 금지

    public OrderResponse createOrder(OrderCreateRequest request) {

        // 이렇게 타 도메인 Repository를 직접 사용하면 안됨
        Product product = productRepository.findById(request.productId());  // ❌
    }
}
```

### DDD를 따르는 이유

1. **도메인 간 결합도 최소화**: 각 도메인이 독립적으로 변경 가능
2. **비즈니스 로직 중앙화**: 도메인별 Service에 로직이 집중되어 관리 용이
3. **테스트 용이성**: Interface를 통한 Mocking이 쉬움
4. **명확한 책임 분리**: 각 도메인이 자신의 데이터에 대한 완전한 제어권 보유

---

## 9. 예외 처리 & 에러 코드

### 예외 처리

* **GlobalException을 사용**
* 추가로 필요한 예외 관련 필드는 extends하여 추가

### 에러 코드

* **ErrorCode를 implement해서 사용**

### 성공 코드

* **SuccessCode를 implement해서 사용**

---

## 10. 통일 사항

* 빌더 사용
* 정적 팩토리 메서드 사용
* 전역 예외 처리
    * 각자 도메인별로 커스텀 예외 생성

---

- **어노테이션**:
    - **자율**:
        - Response에서 생성자 자동 생성 어노테이션
    - **통일**:
        - 엔티티: 클래스 단위로 @Table(name = )
        - 클래스 단위로 @Transactional
        - 클래스 단위로 @ReqeustMapping(”/url”)

---

- **페이지네이션**:
    - 오프셋 기반 페이지네이션
    - `@RequestParam` 방식
    - page → size → sort → direction 순

    ```
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "createdAt") String sort,
    @RequestParam(defaultValue = "DESC") String direction
    ```
    - chat, chatroom은 오프셋 기반 페이지네이션 사용 안함, 무한스크롤 방식

---

- **메서드명**
    - 조회: get~
        - 단건 조회: 단수형으로
            - 예: getCloset
        - 전체 조회: 복수형으로
            - 예: getClosets
    - 생성: creat~
    - 수정: update~
    - 삭제: delete~