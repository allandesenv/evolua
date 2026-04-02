from pathlib import Path
from textwrap import dedent


ROOT = Path(__file__).resolve().parents[1]


SERVICES = {
    "user-service": ("user", 8082, "postgres", "Profile", "profiles", [("displayName", "String"), ("bio", "String"), ("journeyLevel", "Integer"), ("premium", "Boolean")]),
    "content-service": ("content", 8083, "postgres", "Trail", "trails", [("title", "String"), ("description", "String"), ("category", "String"), ("premium", "Boolean")]),
    "emotional-service": ("emotional", 8084, "postgres", "CheckIn", "check-ins", [("mood", "String"), ("reflection", "String"), ("energyLevel", "Integer"), ("recommendedPractice", "String")]),
    "social-service": ("social", 8085, "mongo", "Post", "posts", [("content", "String"), ("community", "String"), ("visibility", "String")]),
    "chat-service": ("chat", 8086, "mongo", "Message", "messages", [("recipientId", "String"), ("content", "String")]),
    "subscription-service": ("subscription", 8087, "postgres", "Subscription", "subscriptions", [("planCode", "String"), ("status", "String"), ("billingCycle", "String"), ("premium", "Boolean")]),
    "notification-service": ("notification", 8088, "redis", "NotificationJob", "notifications", [("channel", "String"), ("message", "String")]),
}


def write(rel_path: str, content: str) -> None:
    path = ROOT / rel_path
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(dedent(content).strip() + "\n", encoding="utf-8")


def snake(name: str) -> str:
    result = []
    for char in name:
        if char.isupper():
            result.append("_")
            result.append(char.lower())
        elif char == "-":
            result.append("_")
        else:
            result.append(char)
    return "".join(result).strip("_")


def root_files() -> None:
    modules = "\n".join(["        <module>gateway-service</module>", "        <module>services/auth-service</module>"] + [f"        <module>services/{name}</module>" for name in SERVICES])
    write(
        "pom.xml",
        f"""
        <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
            <modelVersion>4.0.0</modelVersion>
            <groupId>com.evolua</groupId>
            <artifactId>evolua-backend</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <packaging>pom</packaging>
            <modules>
        {modules}
            </modules>
            <properties>
                <java.version>21</java.version>
                <spring.boot.version>3.5.0</spring.boot.version>
                <spring.cloud.version>2025.0.0</spring.cloud.version>
                <mapstruct.version>1.6.3</mapstruct.version>
                <jjwt.version>0.12.6</jjwt.version>
                <springdoc.version>2.8.9</springdoc.version>
            </properties>
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-dependencies</artifactId>
                        <version>${{spring.boot.version}}</version>
                        <type>pom</type>
                        <scope>import</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-dependencies</artifactId>
                        <version>${{spring.cloud.version}}</version>
                        <type>pom</type>
                        <scope>import</scope>
                    </dependency>
                </dependencies>
            </dependencyManagement>
        </project>
        """,
    )


BASE_DEPS = """
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-actuator</artifactId></dependency>
    <dependency><groupId>org.springframework.cloud</groupId><artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId></dependency>
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>${springdoc.version}</version></dependency>
    <dependency><groupId>org.mapstruct</groupId><artifactId>mapstruct</artifactId><version>${mapstruct.version}</version></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>${jjwt.version}</version></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.mockito</groupId><artifactId>mockito-junit-jupiter</artifactId><scope>test</scope></dependency>
"""


def service_pom(name: str, store: str, websocket: bool = False) -> str:
    extra = {
        "postgres": "<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency><dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency><dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>",
        "mongo": "<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-mongodb</artifactId></dependency>",
        "redis": "<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-redis</artifactId></dependency>",
    }[store]
    websocket_dep = "<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-websocket</artifactId></dependency>" if websocket else ""
    return dedent(
        f"""
        <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
            <modelVersion>4.0.0</modelVersion>
            <parent>
                <groupId>com.evolua</groupId>
                <artifactId>evolua-backend</artifactId>
                <version>0.1.0-SNAPSHOT</version>
                <relativePath>../../pom.xml</relativePath>
            </parent>
            <artifactId>{name}</artifactId>
            <build>
                <plugins>
                    <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
                </plugins>
            </build>
            <dependencies>
        {BASE_DEPS}
        {extra}
        {websocket_dep}
            </dependencies>
        </project>
        """
    )


def common_java(package: str, rel_base: str, title: str, description: str, public_auth: bool = True) -> None:
    common_security_permits = '.requestMatchers("/v1/public/**").permitAll()' if public_auth else ""
    write(f"{rel_base}/config/OpenApiConfig.java", f'package {package}.config; import io.swagger.v3.oas.models.OpenAPI; import io.swagger.v3.oas.models.info.Info; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; @Configuration public class OpenApiConfig {{ @Bean OpenAPI openAPI() {{ return new OpenAPI().info(new Info().title("{title} API").version("v1").description("{description}")); }} }}')
    write(f"{rel_base}/config/SecurityConfig.java", f'package {package}.config; import {package}.infrastructure.security.JwtAuthenticationFilter; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.http.HttpMethod; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; @Configuration public class SecurityConfig {{ @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {{ http.csrf(csrf -> csrf.disable()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll().requestMatchers(HttpMethod.GET, "/v1/public/health").permitAll(){common_security_permits}.anyRequest().authenticated()).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); return http.build(); }} }}')
    write(f"{rel_base}/interfaces/rest/ApiResponse.java", f"package {package}.interfaces.rest; import java.time.Instant; public record ApiResponse<T>(Instant timestamp, int status, String message, T data) {{ public static <T> ApiResponse<T> success(int status, String message, T data) {{ return new ApiResponse<>(Instant.now(), status, message, data); }} }}")
    write(f"{rel_base}/interfaces/rest/ErrorResponse.java", f"package {package}.interfaces.rest; import java.time.Instant; import java.util.List; public record ErrorResponse(Instant timestamp, int status, String error, List<String> details) {{ }}")
    write(f"{rel_base}/interfaces/rest/GlobalExceptionHandler.java", f'package {package}.interfaces.rest; import java.time.Instant; import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.ExceptionHandler; import org.springframework.web.bind.annotation.RestControllerAdvice; @RestControllerAdvice public class GlobalExceptionHandler {{ @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {{ return ResponseEntity.badRequest().body(new ErrorResponse(Instant.now(), 400, "Bad Request", List.of(exception.getMessage()))); }} @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {{ List<String> details = exception.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).toList(); return ResponseEntity.badRequest().body(new ErrorResponse(Instant.now(), 400, "Bad Request", details)); }} @ExceptionHandler(Exception.class) public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {{ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(Instant.now(), 500, "Internal Server Error", List.of("Unexpected error"))); }} }}')
    write(f"{rel_base}/infrastructure/security/AuthenticatedUser.java", f"package {package}.infrastructure.security; import java.util.List; public record AuthenticatedUser(String userId, String email, List<String> roles) {{ }}")
    write(f"{rel_base}/infrastructure/security/JwtService.java", f'package {package}.infrastructure.security; import io.jsonwebtoken.Claims; import io.jsonwebtoken.Jwts; import io.jsonwebtoken.security.Keys; import java.nio.charset.StandardCharsets; import java.util.List; import javax.crypto.SecretKey; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component; @Component public class JwtService {{ private final SecretKey key; public JwtService(@Value("${{app.security.jwt.secret}}") String secret) {{ this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }} public AuthenticatedUser parse(String token) {{ Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); List<String> roles = claims.get("roles", List.class); return new AuthenticatedUser(claims.getSubject(), claims.get("email", String.class), roles == null ? List.of() : roles); }} }}')
    write(f"{rel_base}/infrastructure/security/JwtAuthenticationFilter.java", f'package {package}.infrastructure.security; import jakarta.servlet.FilterChain; import jakarta.servlet.ServletException; import jakarta.servlet.http.HttpServletRequest; import jakarta.servlet.http.HttpServletResponse; import java.io.IOException; import org.springframework.http.HttpHeaders; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter; @Component public class JwtAuthenticationFilter extends OncePerRequestFilter {{ private final JwtService jwtService; public JwtAuthenticationFilter(JwtService jwtService) {{ this.jwtService = jwtService; }} @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {{ String header = request.getHeader(HttpHeaders.AUTHORIZATION); if (header != null && header.startsWith("Bearer ")) {{ AuthenticatedUser user = jwtService.parse(header.substring(7)); SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, header, java.util.List.of())); }} filterChain.doFilter(request, response); }} }}')
    write(f"{rel_base}/infrastructure/security/CurrentUserProvider.java", f'package {package}.infrastructure.security; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component; @Component public class CurrentUserProvider {{ public AuthenticatedUser getCurrentUser() {{ Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal(); if (principal instanceof AuthenticatedUser user) {{ return user; }} throw new IllegalArgumentException("Authenticated user not found"); }} }}')


def validation_for(type_name: str) -> str:
    return "@jakarta.validation.constraints.NotBlank" if type_name == "String" else "@jakarta.validation.constraints.NotNull"


def generic_service(name: str, package_short: str, port: int, store: str, aggregate: str, plural: str, fields: list[tuple[str, str]]) -> None:
    package = f"com.evolua.{package_short}"
    app_name = package_short.capitalize() + "ServiceApplication"
    base = f"services/{name}/src/main/java/{package.replace('.', '/')}"
    root = f"services/{name}"
    write(f"{root}/pom.xml", service_pom(name, store, websocket=name == "chat-service"))
    write(
        f"{root}/Dockerfile",
        f"""
        FROM maven:3.9.9-eclipse-temurin-21 AS build
        WORKDIR /workspace
        COPY . /workspace
        RUN mvn -pl services/{name} -am -B clean package -DskipTests
        FROM eclipse-temurin:21-jre
        WORKDIR /app
        COPY --from=build /workspace/services/{name}/target/{name}-0.1.0-SNAPSHOT.jar app.jar
        EXPOSE {port}
        ENTRYPOINT ["java", "-jar", "/app/app.jar"]
        """,
    )
    write(f"{base}/{app_name}.java", f"package {package}; import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication; @SpringBootApplication public class {app_name} {{ public static void main(String[] args) {{ SpringApplication.run({app_name}.class, args); }} }}")
    common_java(package, base, name, f"{name} service")
    if store == "postgres":
        data = f"""  datasource:
    url: ${{SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/evolua}}
    username: ${{SPRING_DATASOURCE_USERNAME:evolua}}
    password: ${{SPRING_DATASOURCE_PASSWORD:evolua}}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
"""
    elif store == "mongo":
        data = f"""  data:
    mongodb:
      uri: ${{SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/evolua}}
"""
    else:
        data = f"""  data:
    redis:
      host: ${{SPRING_DATA_REDIS_HOST:localhost}}
      port: ${{SPRING_DATA_REDIS_PORT:6379}}
"""
    write(
        f"{root}/src/main/resources/application.yml",
        f"""server:
  port: ${{SERVER_PORT:{port}}}
spring:
  application:
    name: {name}
{data}management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
springdoc:
  swagger-ui:
    path: /swagger-ui.html
app:
  security:
    jwt:
      secret: ${{APP_SECURITY_JWT_SECRET:change-this-secret-before-production-change-this-secret}}
""",
    )
    write(f"{root}/src/test/java/{package.replace('.', '/')}/{app_name}Tests.java", f"package {package}; import org.junit.jupiter.api.Test; import org.springframework.boot.test.context.SpringBootTest; @SpringBootTest class {app_name}Tests {{ @Test void contextLoads() {{ }} }}")
    if store == "redis":
        return
    field_decl = ", ".join(f"{type_name} {field_name}" for field_name, type_name in fields)
    request_decl = ", ".join(f"{validation_for(type_name)} {type_name} {field_name}" for field_name, type_name in fields)
    response_decl = ", ".join(f"{type_name} {field_name}" for field_name, type_name in fields)
    values = ", ".join(field_name for field_name, _ in fields)
    request_values = ", ".join(f"request.{field_name}()" for field_name, _ in fields)
    base_domain = f"{base}/domain"
    write(f"{base_domain}/{aggregate}.java", f"package {package}.domain; import java.time.Instant; public record {aggregate}({'Long' if store == 'postgres' else 'String'} id, String userId, {field_decl}, Instant createdAt) {{ }}")
    write(f"{base_domain}/{aggregate}Repository.java", f"package {package}.domain; import java.util.List; public interface {aggregate}Repository {{ {aggregate} save({aggregate} item); List<{aggregate}> findAllByUserId(String userId); }}")
    write(f"{base}/application/{aggregate}Service.java", f"package {package}.application; import {package}.domain.{aggregate}; import {package}.domain.{aggregate}Repository; import java.time.Instant; import java.util.List; import org.springframework.stereotype.Service; @Service public class {aggregate}Service {{ private final {aggregate}Repository repository; public {aggregate}Service({aggregate}Repository repository) {{ this.repository = repository; }} public {aggregate} create(String userId, {field_decl}) {{ return repository.save(new {aggregate}(null, userId, {values}, Instant.now())); }} public List<{aggregate}> list(String userId) {{ return repository.findAllByUserId(userId); }} }}")
    write(f"{base}/interfaces/rest/{aggregate}Mapper.java", f'package {package}.interfaces.rest; import {package}.domain.{aggregate}; import org.mapstruct.Mapper; @Mapper(componentModel = "spring") public interface {aggregate}Mapper {{ {aggregate}Response toResponse({aggregate} item); }}')
    write(f"{base}/interfaces/rest/{aggregate}Request.java", f"package {package}.interfaces.rest; public record {aggregate}Request({request_decl}) {{ }}")
    write(f"{base}/interfaces/rest/{aggregate}Response.java", f"package {package}.interfaces.rest; import java.time.Instant; public record {aggregate}Response({'Long' if store == 'postgres' else 'String'} id, String userId, {response_decl}, Instant createdAt) {{ }}")
    write(f"{base}/interfaces/rest/{aggregate}Controller.java", f'package {package}.interfaces.rest; import {package}.application.{aggregate}Service; import {package}.infrastructure.security.CurrentUserProvider; import io.swagger.v3.oas.annotations.Operation; import jakarta.validation.Valid; import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; @RestController @RequestMapping("/v1/{plural}") public class {aggregate}Controller {{ private final {aggregate}Service service; private final {aggregate}Mapper mapper; private final CurrentUserProvider currentUserProvider; public {aggregate}Controller({aggregate}Service service, {aggregate}Mapper mapper, CurrentUserProvider currentUserProvider) {{ this.service = service; this.mapper = mapper; this.currentUserProvider = currentUserProvider; }} @PostMapping @Operation(summary = "Create {aggregate}") public ResponseEntity<ApiResponse<{aggregate}Response>> create(@Valid @RequestBody {aggregate}Request request) {{ var created = service.create(currentUserProvider.getCurrentUser().userId(), {request_values}); return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Created", mapper.toResponse(created))); }} @GetMapping @Operation(summary = "List {plural}") public ResponseEntity<ApiResponse<List<{aggregate}Response>>> list() {{ return ResponseEntity.ok(ApiResponse.success(200, "Listed", service.list(currentUserProvider.getCurrentUser().userId()).stream().map(mapper::toResponse).toList())); }} }}')
    if store == "postgres":
        entity_fields = " ".join(f"private {type_name} {field_name};" for field_name, type_name in fields)
        getters = " ".join(f"public {type_name} get{field_name[0].upper() + field_name[1:]}() {{ return {field_name}; }} public void set{field_name[0].upper() + field_name[1:]}({type_name} {field_name}) {{ this.{field_name} = {field_name}; }}" for field_name, type_name in fields)
        setters = " ".join(f"entity.set{field_name[0].upper() + field_name[1:]}(item.{field_name}());" for field_name, _ in fields)
        projections = ", ".join(f"saved.get{field_name[0].upper() + field_name[1:]}()" for field_name, _ in fields)
        sql_columns = ",\n    ".join(f"{snake(field_name)} " + ("INTEGER NOT NULL" if type_name == "Integer" else "BOOLEAN NOT NULL" if type_name == "Boolean" else "VARCHAR(255) NOT NULL") for field_name, type_name in fields)
        sql_names = ", ".join(snake(field_name) for field_name, _ in fields)
        sql_values = ", ".join("1" if type_name == "Integer" else "true" if type_name == "Boolean" else f"'seed-{snake(field_name)}'" for field_name, type_name in fields)
        write(f"{base}/infrastructure/persistence/{aggregate}Entity.java", f'package {package}.infrastructure.persistence; import jakarta.persistence.*; import java.time.Instant; @Entity @Table(name = "{snake(plural)}") public class {aggregate}Entity {{ @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id; private String userId; {entity_fields} private Instant createdAt; public Long getId() {{ return id; }} public void setId(Long id) {{ this.id = id; }} public String getUserId() {{ return userId; }} public void setUserId(String userId) {{ this.userId = userId; }} {getters} public Instant getCreatedAt() {{ return createdAt; }} public void setCreatedAt(Instant createdAt) {{ this.createdAt = createdAt; }} }}')
        write(f"{base}/infrastructure/persistence/{aggregate}JpaRepository.java", f"package {package}.infrastructure.persistence; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository; public interface {aggregate}JpaRepository extends JpaRepository<{aggregate}Entity, Long> {{ List<{aggregate}Entity> findAllByUserId(String userId); }}")
        write(f"{base}/infrastructure/persistence/{aggregate}PersistenceAdapter.java", f'package {package}.infrastructure.persistence; import {package}.domain.{aggregate}; import {package}.domain.{aggregate}Repository; import java.util.List; import org.springframework.stereotype.Repository; @Repository public class {aggregate}PersistenceAdapter implements {aggregate}Repository {{ private final {aggregate}JpaRepository repository; public {aggregate}PersistenceAdapter({aggregate}JpaRepository repository) {{ this.repository = repository; }} public {aggregate} save({aggregate} item) {{ {aggregate}Entity entity = new {aggregate}Entity(); entity.setId(item.id()); entity.setUserId(item.userId()); {setters} entity.setCreatedAt(item.createdAt()); {aggregate}Entity saved = repository.save(entity); return new {aggregate}(saved.getId(), saved.getUserId(), {projections}, saved.getCreatedAt()); }} public List<{aggregate}> findAllByUserId(String userId) {{ return repository.findAllByUserId(userId).stream().map(saved -> new {aggregate}(saved.getId(), saved.getUserId(), {projections}, saved.getCreatedAt())).toList(); }} }}')
        write(f"{root}/src/main/resources/db/migration/V1__create_{snake(plural)}.sql", f"CREATE TABLE {snake(plural)} (\\n    id BIGSERIAL PRIMARY KEY,\\n    user_id VARCHAR(64) NOT NULL,\\n    {sql_columns},\\n    created_at TIMESTAMP WITH TIME ZONE NOT NULL\\n);\\n\\nINSERT INTO {snake(plural)} (user_id, {sql_names}, created_at) VALUES ('seed-user', {sql_values}, CURRENT_TIMESTAMP);\\n")
    elif store == "mongo":
        document_fields = " ".join(f"private {type_name} {field_name}; public {type_name} get{field_name[0].upper() + field_name[1:]}() {{ return {field_name}; }} public void set{field_name[0].upper() + field_name[1:]}({type_name} {field_name}) {{ this.{field_name} = {field_name}; }}" for field_name, type_name in fields)
        setters = " ".join(f"document.set{field_name[0].upper() + field_name[1:]}(item.{field_name}());" for field_name, _ in fields)
        projections = ", ".join(f"saved.get{field_name[0].upper() + field_name[1:]}()" for field_name, _ in fields)
        write(f"{base}/infrastructure/persistence/{aggregate}Document.java", f'package {package}.infrastructure.persistence; import java.time.Instant; import org.springframework.data.annotation.Id; import org.springframework.data.mongodb.core.mapping.Document; @Document(collection = "{plural}") public class {aggregate}Document {{ @Id private String id; private String userId; {document_fields} private Instant createdAt; public String getId() {{ return id; }} public void setId(String id) {{ this.id = id; }} public String getUserId() {{ return userId; }} public void setUserId(String userId) {{ this.userId = userId; }} public Instant getCreatedAt() {{ return createdAt; }} public void setCreatedAt(Instant createdAt) {{ this.createdAt = createdAt; }} }}')
        write(f"{base}/infrastructure/persistence/{aggregate}MongoRepository.java", f"package {package}.infrastructure.persistence; import java.util.List; import org.springframework.data.mongodb.repository.MongoRepository; public interface {aggregate}MongoRepository extends MongoRepository<{aggregate}Document, String> {{ List<{aggregate}Document> findAllByUserId(String userId); }}")
        write(f"{base}/infrastructure/persistence/{aggregate}PersistenceAdapter.java", f'package {package}.infrastructure.persistence; import {package}.domain.{aggregate}; import {package}.domain.{aggregate}Repository; import java.util.List; import org.springframework.stereotype.Repository; @Repository public class {aggregate}PersistenceAdapter implements {aggregate}Repository {{ private final {aggregate}MongoRepository repository; public {aggregate}PersistenceAdapter({aggregate}MongoRepository repository) {{ this.repository = repository; }} public {aggregate} save({aggregate} item) {{ {aggregate}Document document = new {aggregate}Document(); document.setId(item.id()); document.setUserId(item.userId()); {setters} document.setCreatedAt(item.createdAt()); {aggregate}Document saved = repository.save(document); return new {aggregate}(saved.getId(), saved.getUserId(), {projections}, saved.getCreatedAt()); }} public List<{aggregate}> findAllByUserId(String userId) {{ return repository.findAllByUserId(userId).stream().map(saved -> new {aggregate}(saved.getId(), saved.getUserId(), {projections}, saved.getCreatedAt())).toList(); }} }}')


def notification_service() -> None:
    package = "com.evolua.notification"
    base = "services/notification-service/src/main/java/com/evolua/notification"
    generic_service("notification-service", "notification", 8088, "redis", "NotificationJob", "notifications", [("channel", "String"), ("message", "String")])
    write(f"{base}/domain/NotificationJob.java", "package com.evolua.notification.domain; import java.time.Instant; public record NotificationJob(String id, String userId, String channel, String message, Instant createdAt) { }")
    write(f"{base}/domain/NotificationJobRepository.java", "package com.evolua.notification.domain; import java.util.List; public interface NotificationJobRepository { NotificationJob save(NotificationJob item); List<NotificationJob> findAllByUserId(String userId); }")
    write(f"{base}/application/NotificationJobService.java", "package com.evolua.notification.application; import com.evolua.notification.domain.NotificationJob; import com.evolua.notification.domain.NotificationJobRepository; import java.time.Instant; import java.util.List; import java.util.UUID; import org.springframework.stereotype.Service; @Service public class NotificationJobService { private final NotificationJobRepository repository; public NotificationJobService(NotificationJobRepository repository) { this.repository = repository; } public NotificationJob create(String userId, String channel, String message) { return repository.save(new NotificationJob(UUID.randomUUID().toString(), userId, channel, message, Instant.now())); } public List<NotificationJob> list(String userId) { return repository.findAllByUserId(userId); } }")
    write(f"{base}/infrastructure/persistence/NotificationJobRedisRepository.java", 'package com.evolua.notification.infrastructure.persistence; import com.evolua.notification.domain.NotificationJob; import com.evolua.notification.domain.NotificationJobRepository; import com.fasterxml.jackson.databind.ObjectMapper; import java.util.ArrayList; import java.util.List; import org.springframework.data.redis.core.StringRedisTemplate; import org.springframework.stereotype.Repository; @Repository public class NotificationJobRedisRepository implements NotificationJobRepository { private final StringRedisTemplate redisTemplate; private final ObjectMapper objectMapper; public NotificationJobRedisRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) { this.redisTemplate = redisTemplate; this.objectMapper = objectMapper; } public NotificationJob save(NotificationJob item) { try { redisTemplate.opsForList().rightPush("notification-jobs:" + item.userId(), objectMapper.writeValueAsString(item)); return item; } catch (Exception ex) { throw new IllegalArgumentException("Could not save notification"); } } public List<NotificationJob> findAllByUserId(String userId) { List<String> rows = redisTemplate.opsForList().range("notification-jobs:" + userId, 0, -1); if (rows == null) { return List.of(); } List<NotificationJob> result = new ArrayList<>(); for (String row : rows) { try { result.add(objectMapper.readValue(row, NotificationJob.class)); } catch (Exception ex) { throw new IllegalArgumentException("Could not parse notification"); } } return result; } }')
    write(f"{base}/interfaces/rest/NotificationJobController.java", 'package com.evolua.notification.interfaces.rest; import com.evolua.notification.application.NotificationJobService; import com.evolua.notification.domain.NotificationJob; import com.evolua.notification.infrastructure.security.CurrentUserProvider; import io.swagger.v3.oas.annotations.Operation; import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank; import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; @RestController @RequestMapping("/v1/notifications") public class NotificationJobController { private final NotificationJobService service; private final CurrentUserProvider currentUserProvider; public NotificationJobController(NotificationJobService service, CurrentUserProvider currentUserProvider) { this.service = service; this.currentUserProvider = currentUserProvider; } @PostMapping @Operation(summary = "Create notification") public ResponseEntity<ApiResponse<NotificationJob>> create(@Valid @RequestBody NotificationJobRequest request) { NotificationJob created = service.create(currentUserProvider.getCurrentUser().userId(), request.channel(), request.message()); return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Created", created)); } @GetMapping @Operation(summary = "List notifications") public ResponseEntity<ApiResponse<List<NotificationJob>>> list() { return ResponseEntity.ok(ApiResponse.success(200, "Listed", service.list(currentUserProvider.getCurrentUser().userId()))); } public record NotificationJobRequest(@NotBlank String channel, @NotBlank String message) { } }')


def auth_service() -> None:
    package = "com.evolua.auth"
    base = "services/auth-service/src/main/java/com/evolua/auth"
    root = "services/auth-service"
    write(f"{root}/pom.xml", service_pom("auth-service", "postgres"))
    write(
        f"{root}/Dockerfile",
        """
        FROM maven:3.9.9-eclipse-temurin-21 AS build
        WORKDIR /workspace
        COPY . /workspace
        RUN mvn -pl services/auth-service -am -B clean package -DskipTests
        FROM eclipse-temurin:21-jre
        WORKDIR /app
        COPY --from=build /workspace/services/auth-service/target/auth-service-0.1.0-SNAPSHOT.jar app.jar
        EXPOSE 8081
        ENTRYPOINT ["java", "-jar", "/app/app.jar"]
        """,
    )
    write(f"{base}/AuthServiceApplication.java", "package com.evolua.auth; import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication; @SpringBootApplication public class AuthServiceApplication { public static void main(String[] args) { SpringApplication.run(AuthServiceApplication.class, args); } }")
    common_java(package, base, "auth-service", "auth service", public_auth=False)
    write(f"{base}/config/SecurityConfig.java", 'package com.evolua.auth.config; import com.evolua.auth.infrastructure.security.JwtAuthenticationFilter; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.http.HttpMethod; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; @Configuration public class SecurityConfig { @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception { http.csrf(csrf -> csrf.disable()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll().requestMatchers(HttpMethod.POST, "/v1/public/auth/register", "/v1/public/auth/login", "/v1/public/auth/refresh").permitAll().requestMatchers(HttpMethod.GET, "/v1/public/health").permitAll().anyRequest().authenticated()).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); return http.build(); } @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); } }')
    write(f"{root}/src/main/resources/application.yml", "server:\n  port: ${SERVER_PORT:8081}\nspring:\n  application:\n    name: auth-service\n  datasource:\n    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/auth}\n    username: ${SPRING_DATASOURCE_USERNAME:evolua}\n    password: ${SPRING_DATASOURCE_PASSWORD:evolua}\n  jpa:\n    hibernate:\n      ddl-auto: validate\n    open-in-view: false\n  flyway:\n    enabled: true\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: health,info,prometheus,metrics\nspringdoc:\n  swagger-ui:\n    path: /swagger-ui.html\napp:\n  security:\n    jwt:\n      secret: ${APP_SECURITY_JWT_SECRET:change-this-secret-before-production-change-this-secret}\n")
    write(f"{root}/src/test/java/com/evolua/auth/AuthServiceApplicationTests.java", "package com.evolua.auth; import org.junit.jupiter.api.Test; import org.springframework.boot.test.context.SpringBootTest; @SpringBootTest class AuthServiceApplicationTests { @Test void contextLoads() { } }")
    write(f"{base}/domain/AuthUser.java", "package com.evolua.auth.domain; import java.time.Instant; import java.util.List; public record AuthUser(Long id, String userId, String email, String passwordHash, List<String> roles, Instant createdAt) { }")
    write(f"{base}/domain/RefreshSession.java", "package com.evolua.auth.domain; import java.time.Instant; public record RefreshSession(Long id, String userId, String refreshToken, Instant createdAt, boolean revoked) { }")
    write(f"{base}/domain/AuthUserRepository.java", "package com.evolua.auth.domain; import java.util.Optional; public interface AuthUserRepository { AuthUser save(AuthUser user); Optional<AuthUser> findByEmail(String email); Optional<AuthUser> findByUserId(String userId); }")
    write(f"{base}/domain/RefreshSessionRepository.java", "package com.evolua.auth.domain; import java.util.Optional; public interface RefreshSessionRepository { RefreshSession save(RefreshSession session); Optional<RefreshSession> findByRefreshToken(String refreshToken); void revokeAll(String userId); }")
    write(f"{base}/application/AuthTokens.java", "package com.evolua.auth.application; public record AuthTokens(String accessToken, String refreshToken) { }")
    write(f"{base}/infrastructure/security/TokenIssuer.java", 'package com.evolua.auth.infrastructure.security; import io.jsonwebtoken.Jwts; import io.jsonwebtoken.security.Keys; import java.nio.charset.StandardCharsets; import java.time.Instant; import java.util.Date; import java.util.List; import javax.crypto.SecretKey; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component; @Component public class TokenIssuer { private final SecretKey key; public TokenIssuer(@Value("${app.security.jwt.secret}") String secret) { this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); } public String accessToken(String userId, String email, List<String> roles) { Instant now = Instant.now(); return Jwts.builder().subject(userId).claim("email", email).claim("roles", roles).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(1800))).signWith(key).compact(); } public String refreshToken(String userId, String email, List<String> roles) { Instant now = Instant.now(); return Jwts.builder().subject(userId).claim("email", email).claim("roles", roles).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(2592000))).signWith(key).compact(); } }')
    write(f"{base}/application/AuthService.java", 'package com.evolua.auth.application; import com.evolua.auth.domain.AuthUser; import com.evolua.auth.domain.AuthUserRepository; import com.evolua.auth.domain.RefreshSession; import com.evolua.auth.domain.RefreshSessionRepository; import com.evolua.auth.infrastructure.security.TokenIssuer; import java.time.Instant; import java.util.List; import java.util.UUID; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; @Service public class AuthService { private final AuthUserRepository authUserRepository; private final RefreshSessionRepository refreshSessionRepository; private final PasswordEncoder passwordEncoder; private final TokenIssuer tokenIssuer; public AuthService(AuthUserRepository authUserRepository, RefreshSessionRepository refreshSessionRepository, PasswordEncoder passwordEncoder, TokenIssuer tokenIssuer) { this.authUserRepository = authUserRepository; this.refreshSessionRepository = refreshSessionRepository; this.passwordEncoder = passwordEncoder; this.tokenIssuer = tokenIssuer; } @Transactional public AuthUser register(String email, String password) { authUserRepository.findByEmail(email).ifPresent(existing -> { throw new IllegalArgumentException("Email already registered"); }); return authUserRepository.save(new AuthUser(null, UUID.randomUUID().toString(), email, passwordEncoder.encode(password), List.of("ROLE_USER"), Instant.now())); } @Transactional public AuthTokens login(String email, String password) { AuthUser user = authUserRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid credentials")); if (!passwordEncoder.matches(password, user.passwordHash())) { throw new IllegalArgumentException("Invalid credentials"); } refreshSessionRepository.revokeAll(user.userId()); String access = tokenIssuer.accessToken(user.userId(), user.email(), user.roles()); String refresh = tokenIssuer.refreshToken(user.userId(), user.email(), user.roles()); refreshSessionRepository.save(new RefreshSession(null, user.userId(), refresh, Instant.now(), false)); return new AuthTokens(access, refresh); } @Transactional public AuthTokens refresh(String refreshToken) { RefreshSession session = refreshSessionRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new IllegalArgumentException("Invalid refresh token")); if (session.revoked()) { throw new IllegalArgumentException("Refresh token revoked"); } AuthUser user = authUserRepository.findByUserId(session.userId()).orElseThrow(() -> new IllegalArgumentException("User not found")); refreshSessionRepository.revokeAll(user.userId()); String access = tokenIssuer.accessToken(user.userId(), user.email(), user.roles()); String nextRefresh = tokenIssuer.refreshToken(user.userId(), user.email(), user.roles()); refreshSessionRepository.save(new RefreshSession(null, user.userId(), nextRefresh, Instant.now(), false)); return new AuthTokens(access, nextRefresh); } public AuthUser me(String userId) { return authUserRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User not found")); } }')
    write(f"{base}/infrastructure/persistence/AuthUserEntity.java", 'package com.evolua.auth.infrastructure.persistence; import jakarta.persistence.*; import java.time.Instant; @Entity @Table(name = "auth_users") public class AuthUserEntity { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id; private String userId; private String email; private String passwordHash; private String roles; private Instant createdAt; public Long getId() { return id; } public void setId(Long id) { this.id = id; } public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; } public String getEmail() { return email; } public void setEmail(String email) { this.email = email; } public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; } public String getRoles() { return roles; } public void setRoles(String roles) { this.roles = roles; } public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; } }')
    write(f"{base}/infrastructure/persistence/RefreshSessionEntity.java", 'package com.evolua.auth.infrastructure.persistence; import jakarta.persistence.*; import java.time.Instant; @Entity @Table(name = "refresh_sessions") public class RefreshSessionEntity { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id; private String userId; @Column(length = 1024) private String refreshToken; private Instant createdAt; private boolean revoked; public Long getId() { return id; } public void setId(Long id) { this.id = id; } public String getUserId() { return userId; } public void setUserId(String userId) { this.userId = userId; } public String getRefreshToken() { return refreshToken; } public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; } public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; } public boolean isRevoked() { return revoked; } public void setRevoked(boolean revoked) { this.revoked = revoked; } }')
    write(f"{base}/infrastructure/persistence/AuthUserJpaRepository.java", "package com.evolua.auth.infrastructure.persistence; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository; public interface AuthUserJpaRepository extends JpaRepository<AuthUserEntity, Long> { Optional<AuthUserEntity> findByEmail(String email); Optional<AuthUserEntity> findByUserId(String userId); }")
    write(f"{base}/infrastructure/persistence/RefreshSessionJpaRepository.java", "package com.evolua.auth.infrastructure.persistence; import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository; public interface RefreshSessionJpaRepository extends JpaRepository<RefreshSessionEntity, Long> { Optional<RefreshSessionEntity> findByRefreshToken(String refreshToken); List<RefreshSessionEntity> findAllByUserId(String userId); }")
    write(f"{base}/infrastructure/persistence/AuthPersistenceAdapter.java", 'package com.evolua.auth.infrastructure.persistence; import com.evolua.auth.domain.AuthUser; import com.evolua.auth.domain.AuthUserRepository; import com.evolua.auth.domain.RefreshSession; import com.evolua.auth.domain.RefreshSessionRepository; import java.util.List; import java.util.Optional; import org.springframework.stereotype.Repository; @Repository public class AuthPersistenceAdapter implements AuthUserRepository, RefreshSessionRepository { private final AuthUserJpaRepository authUserJpaRepository; private final RefreshSessionJpaRepository refreshSessionJpaRepository; public AuthPersistenceAdapter(AuthUserJpaRepository authUserJpaRepository, RefreshSessionJpaRepository refreshSessionJpaRepository) { this.authUserJpaRepository = authUserJpaRepository; this.refreshSessionJpaRepository = refreshSessionJpaRepository; } public AuthUser save(AuthUser user) { AuthUserEntity entity = new AuthUserEntity(); entity.setId(user.id()); entity.setUserId(user.userId()); entity.setEmail(user.email()); entity.setPasswordHash(user.passwordHash()); entity.setRoles(String.join(",", user.roles())); entity.setCreatedAt(user.createdAt()); AuthUserEntity saved = authUserJpaRepository.save(entity); return new AuthUser(saved.getId(), saved.getUserId(), saved.getEmail(), saved.getPasswordHash(), List.of(saved.getRoles().split(",")), saved.getCreatedAt()); } public Optional<AuthUser> findByEmail(String email) { return authUserJpaRepository.findByEmail(email).map(saved -> new AuthUser(saved.getId(), saved.getUserId(), saved.getEmail(), saved.getPasswordHash(), List.of(saved.getRoles().split(",")), saved.getCreatedAt())); } public Optional<AuthUser> findByUserId(String userId) { return authUserJpaRepository.findByUserId(userId).map(saved -> new AuthUser(saved.getId(), saved.getUserId(), saved.getEmail(), saved.getPasswordHash(), List.of(saved.getRoles().split(",")), saved.getCreatedAt())); } public RefreshSession save(RefreshSession session) { RefreshSessionEntity entity = new RefreshSessionEntity(); entity.setId(session.id()); entity.setUserId(session.userId()); entity.setRefreshToken(session.refreshToken()); entity.setCreatedAt(session.createdAt()); entity.setRevoked(session.revoked()); RefreshSessionEntity saved = refreshSessionJpaRepository.save(entity); return new RefreshSession(saved.getId(), saved.getUserId(), saved.getRefreshToken(), saved.getCreatedAt(), saved.isRevoked()); } public Optional<RefreshSession> findByRefreshToken(String refreshToken) { return refreshSessionJpaRepository.findByRefreshToken(refreshToken).map(saved -> new RefreshSession(saved.getId(), saved.getUserId(), saved.getRefreshToken(), saved.getCreatedAt(), saved.isRevoked())); } public void revokeAll(String userId) { for (RefreshSessionEntity session : refreshSessionJpaRepository.findAllByUserId(userId)) { session.setRevoked(true); refreshSessionJpaRepository.save(session); } } }')
    write(f"{base}/interfaces/rest/AuthController.java", 'package com.evolua.auth.interfaces.rest; import com.evolua.auth.application.AuthService; import com.evolua.auth.infrastructure.security.CurrentUserProvider; import io.swagger.v3.oas.annotations.Operation; import jakarta.validation.Valid; import jakarta.validation.constraints.Email; import jakarta.validation.constraints.NotBlank; import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; @RestController @RequestMapping public class AuthController { private final AuthService authService; private final CurrentUserProvider currentUserProvider; public AuthController(AuthService authService, CurrentUserProvider currentUserProvider) { this.authService = authService; this.currentUserProvider = currentUserProvider; } @PostMapping("/v1/public/auth/register") @Operation(summary = "Register user") public ResponseEntity<ApiResponse<AuthUserResponse>> register(@Valid @RequestBody RegisterRequest request) { var user = authService.register(request.email(), request.password()); return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, "Created", new AuthUserResponse(user.userId(), user.email(), user.roles()))); } @PostMapping("/v1/public/auth/login") @Operation(summary = "Login user") public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) { var tokens = authService.login(request.email(), request.password()); return ResponseEntity.ok(ApiResponse.success(200, "Logged in", new TokenResponse(tokens.accessToken(), tokens.refreshToken()))); } @PostMapping("/v1/public/auth/refresh") @Operation(summary = "Refresh tokens") public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) { var tokens = authService.refresh(request.refreshToken()); return ResponseEntity.ok(ApiResponse.success(200, "Refreshed", new TokenResponse(tokens.accessToken(), tokens.refreshToken()))); } @GetMapping("/v1/auth/me") @Operation(summary = "Current account") public ResponseEntity<ApiResponse<AuthUserResponse>> me() { var user = authService.me(currentUserProvider.getCurrentUser().userId()); return ResponseEntity.ok(ApiResponse.success(200, "Current user", new AuthUserResponse(user.userId(), user.email(), user.roles()))); } public record RegisterRequest(@Email String email, @NotBlank String password) { } public record LoginRequest(@Email String email, @NotBlank String password) { } public record RefreshRequest(@NotBlank String refreshToken) { } public record TokenResponse(String accessToken, String refreshToken) { } public record AuthUserResponse(String userId, String email, List<String> roles) { } }')
    write(f"{root}/src/main/resources/db/migration/V1__create_auth_schema.sql", "CREATE TABLE auth_users (\\n    id BIGSERIAL PRIMARY KEY,\\n    user_id VARCHAR(64) NOT NULL UNIQUE,\\n    email VARCHAR(255) NOT NULL UNIQUE,\\n    password_hash VARCHAR(255) NOT NULL,\\n    roles VARCHAR(255) NOT NULL,\\n    created_at TIMESTAMP WITH TIME ZONE NOT NULL\\n);\\n\\nCREATE TABLE refresh_sessions (\\n    id BIGSERIAL PRIMARY KEY,\\n    user_id VARCHAR(64) NOT NULL,\\n    refresh_token VARCHAR(1024) NOT NULL UNIQUE,\\n    created_at TIMESTAMP WITH TIME ZONE NOT NULL,\\n    revoked BOOLEAN NOT NULL\\n);\\n")


def gateway_service() -> None:
    write("gateway-service/pom.xml", dedent("""<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"><modelVersion>4.0.0</modelVersion><parent><groupId>com.evolua</groupId><artifactId>evolua-backend</artifactId><version>0.1.0-SNAPSHOT</version><relativePath>../pom.xml</relativePath></parent><artifactId>gateway-service</artifactId><build><plugins><plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin></plugins></build><dependencies><dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-actuator</artifactId></dependency><dependency><groupId>org.springframework.cloud</groupId><artifactId>spring-cloud-starter-gateway</artifactId></dependency><dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency><dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webflux-ui</artifactId><version>${springdoc.version}</version></dependency><dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency></dependencies></project>"""))
    write(
        "gateway-service/Dockerfile",
        """
        FROM maven:3.9.9-eclipse-temurin-21 AS build
        WORKDIR /workspace
        COPY . /workspace
        RUN mvn -pl gateway-service -am -B clean package -DskipTests
        FROM eclipse-temurin:21-jre
        WORKDIR /app
        COPY --from=build /workspace/gateway-service/target/gateway-service-0.1.0-SNAPSHOT.jar app.jar
        EXPOSE 8080
        ENTRYPOINT ["java", "-jar", "/app/app.jar"]
        """,
    )
    write("gateway-service/src/main/java/com/evolua/gateway/GatewayServiceApplication.java", "package com.evolua.gateway; import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication; @SpringBootApplication public class GatewayServiceApplication { public static void main(String[] args) { SpringApplication.run(GatewayServiceApplication.class, args); } }")
    write("gateway-service/src/main/java/com/evolua/gateway/config/GatewaySecurityConfig.java", "package com.evolua.gateway.config; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.security.config.web.server.ServerHttpSecurity; import org.springframework.security.web.server.SecurityWebFilterChain; @Configuration public class GatewaySecurityConfig { @Bean SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) { return http.csrf(ServerHttpSecurity.CsrfSpec::disable).authorizeExchange(exchange -> exchange.anyExchange().permitAll()).build(); } }")
    write("gateway-service/src/main/resources/application.yml", "server:\n  port: ${SERVER_PORT:8080}\nspring:\n  application:\n    name: gateway-service\n  cloud:\n    gateway:\n      routes:\n        - id: auth-service\n          uri: http://auth-service:8081\n          predicates: [ Path=/v1/auth/**,/v1/public/auth/** ]\n        - id: user-service\n          uri: http://user-service:8082\n          predicates: [ Path=/v1/profiles/** ]\n        - id: content-service\n          uri: http://content-service:8083\n          predicates: [ Path=/v1/trails/** ]\n        - id: emotional-service\n          uri: http://emotional-service:8084\n          predicates: [ Path=/v1/check-ins/** ]\n        - id: social-service\n          uri: http://social-service:8085\n          predicates: [ Path=/v1/posts/** ]\n        - id: chat-service\n          uri: http://chat-service:8086\n          predicates: [ Path=/v1/messages/**,/ws/chat/** ]\n        - id: subscription-service\n          uri: http://subscription-service:8087\n          predicates: [ Path=/v1/subscriptions/** ]\n        - id: notification-service\n          uri: http://notification-service:8088\n          predicates: [ Path=/v1/notifications/** ]\nspringdoc:\n  swagger-ui:\n    path: /swagger-ui.html\n")
    write("gateway-service/src/test/java/com/evolua/gateway/GatewayServiceApplicationTests.java", "package com.evolua.gateway; import org.junit.jupiter.api.Test; import org.springframework.boot.test.context.SpringBootTest; @SpringBootTest class GatewayServiceApplicationTests { @Test void contextLoads() { } }")


def chat_support() -> None:
    base = "services/chat-service/src/main/java/com/evolua/chat"
    write(f"{base}/config/WebSocketConfig.java", 'package com.evolua.chat.config; import org.springframework.context.annotation.Configuration; import org.springframework.messaging.simp.config.MessageBrokerRegistry; import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker; import org.springframework.web.socket.config.annotation.StompEndpointRegistry; import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer; @Configuration @EnableWebSocketMessageBroker public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { @Override public void configureMessageBroker(MessageBrokerRegistry registry) { registry.enableSimpleBroker("/topic", "/queue"); registry.setApplicationDestinationPrefixes("/app"); } @Override public void registerStompEndpoints(StompEndpointRegistry registry) { registry.addEndpoint("/ws/chat").setAllowedOriginPatterns("*"); } }')
    write(f"{base}/interfaces/rest/ChatSocketController.java", 'package com.evolua.chat.interfaces.rest; import com.evolua.chat.application.MessageService; import org.springframework.messaging.handler.annotation.MessageMapping; import org.springframework.messaging.handler.annotation.Payload; import org.springframework.messaging.simp.SimpMessagingTemplate; import org.springframework.stereotype.Controller; @Controller public class ChatSocketController { private final MessageService service; private final SimpMessagingTemplate messagingTemplate; public ChatSocketController(MessageService service, SimpMessagingTemplate messagingTemplate) { this.service = service; this.messagingTemplate = messagingTemplate; } @MessageMapping("/chat.send") public void send(@Payload MessageRequest request) { var saved = service.create(request.senderId(), request.recipientId(), request.content()); messagingTemplate.convertAndSend("/topic/chat/" + request.recipientId(), saved); } public record MessageRequest(String senderId, String recipientId, String content) { } }')


def main() -> None:
    root_files()
    gateway_service()
    auth_service()
    for name, (package_short, port, store, aggregate, plural, fields) in SERVICES.items():
        if name == "notification-service":
            notification_service()
        else:
            generic_service(name, package_short, port, store, aggregate, plural, fields)
    chat_support()


if __name__ == "__main__":
    main()
    write(".gitignore", "target/\n.idea/\n.vscode/\n.env\n")
    write(
        "README.md",
        """
        # Evolua Backend

        Backend em microsservicos para o Evolua com Java 21, Spring Boot 3+, Docker Compose, OpenAPI e Clean Architecture.
        """,
    )
    write(
        ".github/workflows/ci.yml",
        """
        name: backend-ci
        on: [push, pull_request]
        jobs:
          build:
            runs-on: ubuntu-latest
            steps:
              - uses: actions/checkout@v4
              - uses: actions/setup-java@v4
                with:
                  distribution: temurin
                  java-version: 21
                  cache: maven
              - run: mvn -B clean verify
        """,
    )
    write("checkstyle.xml", '<!DOCTYPE module PUBLIC "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN" "https://checkstyle.org/dtds/configuration_1_3.dtd"><module name="Checker"><module name="TreeWalker"><module name="AvoidStarImport"/><module name="UnusedImports"/></module></module>')
    write("pmd.xml", '<?xml version="1.0"?><ruleset name="evolua-rules" xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"><description>Basic rules.</description><rule ref="category/java/bestpractices.xml"/><rule ref="category/java/codestyle.xml"/></ruleset>')
    write(
        "docker-compose.yml",
        """
        version: "3.9"
        services:
          gateway-service:
            build:
              context: .
              dockerfile: gateway-service/Dockerfile
            ports: ["8080:8080"]
            depends_on: [auth-service, user-service, content-service, emotional-service, social-service, chat-service, subscription-service, notification-service]
          auth-service:
            build:
              context: .
              dockerfile: services/auth-service/Dockerfile
            ports: ["8081:8081"]
            environment:
              SERVER_PORT: 8081
              SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-auth:5432/auth
              SPRING_DATASOURCE_USERNAME: evolua
              SPRING_DATASOURCE_PASSWORD: evolua
              APP_SECURITY_JWT_SECRET: change-this-secret-before-production-change-this-secret
            depends_on: [postgres-auth]
          user-service:
            build:
              context: .
              dockerfile: services/user-service/Dockerfile
            ports: ["8082:8082"]
            environment:
              SERVER_PORT: 8082
              SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-user:5432/user
              SPRING_DATASOURCE_USERNAME: evolua
              SPRING_DATASOURCE_PASSWORD: evolua
              APP_SECURITY_JWT_SECRET: change-this-secret-before-production-change-this-secret
            depends_on: [postgres-user]
          content-service:
            build:
              context: .
              dockerfile: services/content-service/Dockerfile
            ports: ["8083:8083"]
            environment:
              SERVER_PORT: 8083
              SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-content:5432/content
              SPRING_DATASOURCE_USERNAME: evolua
              SPRING_DATASOURCE_PASSWORD: evolua
              APP_SECURITY_JWT_SECRET: change-this-secret-before-production-change-this-secret
            depends_on: [postgres-content]
          emotional-service:
            build:
              context: .
              dockerfile: services/emotional-service/Dockerfile
            ports: ["8084:8084"]
            environment:
              SERVER_PORT: 8084
              SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-emotional:5432/emotional
              SPRING_DATASOURCE_USERNAME: evolua
              SPRING_DATASOURCE_PASSWORD: evolua
              APP_SECURITY_JWT_SECRET: change-this-secret-before-production-change-this-secret
            depends_on: [postgres-emotional]
          social-service:
            build:
              context: .
              dockerfile: services/social-service/Dockerfile
            ports: ["8085:8085"]
            environment:
              SERVER_PORT: 8085
              SPRING_DATA_MONGODB_URI: mongodb://evolua:evolua@mongo-social:27017/social?authSource=admin
              APP_SECURITY_JWT_SECRET: change-this-secret-before-production-change-this-secret
            depends_on: [mongo-social]
          chat-service:
            build:
              context: .
              dockerfile: services/chat-service/Dockerfile
            ports: ["8086:8086"]
            environment:
              SERVER_PORT: 8086
              SPRING_DATA_MONGODB_URI: mongodb://evolua:evolua@mongo-chat:27017/chat?authSource=admin
              APP_SECURITY_JWT_SECRET: change-this-secret-before-production-change-this-secret
            depends_on: [mongo-chat]
          subscription-service:
            build:
              context: .
              dockerfile: services/subscription-service/Dockerfile
            ports: ["8087:8087"]
            environment:
              SERVER_PORT: 8087
              SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-subscription:5432/subscription
              SPRING_DATASOURCE_USERNAME: evolua
              SPRING_DATASOURCE_PASSWORD: evolua
              APP_SECURITY_JWT_SECRET: change-this-secret-before-production-change-this-secret
            depends_on: [postgres-subscription]
          notification-service:
            build:
              context: .
              dockerfile: services/notification-service/Dockerfile
            ports: ["8088:8088"]
            environment:
              SERVER_PORT: 8088
              SPRING_DATA_REDIS_HOST: redis
              APP_SECURITY_JWT_SECRET: change-this-secret-before-production-change-this-secret
            depends_on: [redis]
          postgres-auth:
            image: postgres:17-alpine
            environment: { POSTGRES_DB: auth, POSTGRES_USER: evolua, POSTGRES_PASSWORD: evolua }
          postgres-user:
            image: postgres:17-alpine
            environment: { POSTGRES_DB: user, POSTGRES_USER: evolua, POSTGRES_PASSWORD: evolua }
          postgres-content:
            image: postgres:17-alpine
            environment: { POSTGRES_DB: content, POSTGRES_USER: evolua, POSTGRES_PASSWORD: evolua }
          postgres-emotional:
            image: postgres:17-alpine
            environment: { POSTGRES_DB: emotional, POSTGRES_USER: evolua, POSTGRES_PASSWORD: evolua }
          postgres-subscription:
            image: postgres:17-alpine
            environment: { POSTGRES_DB: subscription, POSTGRES_USER: evolua, POSTGRES_PASSWORD: evolua }
          mongo-social:
            image: mongo:8
            environment: { MONGO_INITDB_ROOT_USERNAME: evolua, MONGO_INITDB_ROOT_PASSWORD: evolua }
          mongo-chat:
            image: mongo:8
            environment: { MONGO_INITDB_ROOT_USERNAME: evolua, MONGO_INITDB_ROOT_PASSWORD: evolua }
          redis:
            image: redis:8-alpine
        """,
    )
