<h1>Spring Boot User Detail Security Example</h1>

<h3>Spring boot security example with users from database. The database is initialized with liquibase.</h3>

<h3>Add dependencies</h3>

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
            <version>1.5.6.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>1.5.6.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
            <version>1.5.6.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
            <version>1.5.6.RELEASE</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.43</version>
        </dependency>

        <dependency>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-core</artifactId>
            <version>3.5.3</version>
        </dependency>

    </dependencies>
```

<h3>Main.java</h3>

```java
@SpringBootApplication
public class Main implements CommandLineRunner {

    public static void main(String [] args){
        SpringApplication.run(Main.class, args);
    }

    public void run(String... strings) throws Exception {
    }
}
```

<h3>Add role entity</h3>

```java
@Entity
public class Role {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, precision = 11)
    private Integer id;

    @NotBlank
    @Column(name = "role_name", nullable = false, length = 255)
    private String roleName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
```

<h3>Add user entity</h3>

```java
@Entity
public class User {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, precision = 11)
    private Integer id;

    @NotBlank
    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    @NotBlank
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Role.class)
    @JoinColumn(name = "role_id" , referencedColumnName = "id")
    private Role role;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
```

<h3>Add role repository</h3>

```java
public interface RoleRepository extends CrudRepository<Role , Long> {
}
```

<h3>Add user repository</h3>

```java
@Repository
public interface UserRepository extends CrudRepository<User, Long>{

    User findByUserName(String userName);
}
```

<h3>Add user detail service</h3>

```java
@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        User user = userRepository.findByUserName(s);
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getRoleName());
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(), Arrays.asList(authority));

        return userDetails;
    }
}
```

<h3>Add security config</h3>

```java
@Controller
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyUserDetailService userDetailService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/","/home","/about").permitAll()
                .antMatchers("/admin/**").hasAuthority("ADMIN")
                .antMatchers("/user/**").hasAnyAuthority("USER","ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login").permitAll()
                .and()
                .logout().permitAll()
                .and()
                .exceptionHandling().accessDeniedPage("/403");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userDetailService).passwordEncoder(new BCryptPasswordEncoder());
    }
}
```

<h3>Add default controller</h3>

```java
@Controller
public class DefaultController {

    @GetMapping("/")
    public String home() {
        return "/home";
    }

    @GetMapping("/home")
    public String home1() {
        return "/home";
    }

    @GetMapping("/admin")
    public String admin() {
        return "/admin";
    }

    @GetMapping("/user")
    public String user() {
        return "/user";
    }

    @GetMapping("/about")
    public String about() {
        return "/about";
    }

    @GetMapping("/login")
    public String login() {
        return "/login";
    }

    @GetMapping("/403")
    public String invalidUser() {
        return "/invalidUser";
    }
}
```

<h3>Add application config</h3>

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/security?autoReconnect=true&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=qwertyui
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5Dialect

spring.jpa.hibernate.ddl-auto=none

spring.jpa.show-sql=true

liquibase.change-log=classpath:/db/changelog/db.changelog-master.xml
```

<h3>Create role table</h3>

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
		http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd">


    <changeSet author="pethoalpar" id="tbl_role" runOnChange="true">
        <preConditions onFail="MARK_RAN">
            <not> <tableExists tableName="role" /> </not>
        </preConditions>
        <createTable tableName="role">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints nullable="false" primaryKey="true"/>
            </column>
            <column name="role_name" type="varchar(100)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>

    <changeSet author="pethoalpar" id="uk_role_1" runOnChange="true">
        <preConditions onFail="MARK_RAN">
            <not> <indexExists indexName="uk_role_1" /> </not>
        </preConditions>
        <addUniqueConstraint
                constraintName="uk_role_1"
                tableName="role"
                columnNames="role_name"/>
    </changeSet>

</databaseChangeLog>
```
