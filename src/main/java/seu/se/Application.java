package seu.se;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@EnableTransactionManagement
@EnableNeo4jRepositories
@SpringBootApplication
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @ControllerAdvice
    public static class ExHandler {
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Object> handle(Exception ex, HttpServletRequest request, HttpServletResponse response) {
            HttpStatus code = HttpStatus.INTERNAL_SERVER_ERROR;
            String status = "internal server error";
            if (ex instanceof NullPointerException) {
                code = HttpStatus.BAD_REQUEST;
                status = "bad request";
            }
            if (ex instanceof NoHandlerFoundException) {
                code = HttpStatus.NOT_FOUND;
                status = "not found";
            }
            var retObj = new ObjectMapper().createObjectNode();
            retObj.put("status", status);
            retObj.put("message", ex.getMessage());
            return ResponseEntity.status(code).body(retObj);
        }
    }

    // in-mem session
    @EnableSpringHttpSession
    @Configuration(proxyBeanMethods = false)
    public static class SpringHttpSessionConfig {
        @Bean
        public MapSessionRepository sessionRepository() {
            return new MapSessionRepository(new ConcurrentHashMap<>());
        }
    }

    @Bean
    public CommandLineRunner init(UserRepository ur, TreeNodeRepository tnr) {
        return (args) -> {
            // Load users
            try {
                List<User> users = new ObjectMapper().readValue(
                        new ClassPathResource("users.json").getFile(),
                        new TypeReference<>() {
                        }
                );
                ur.saveAll(users);
            } catch (IOException e) {
                log.error("Error loading users from JSON file", e);
            }

            // Load tree nodes
            TreeNode.tnRepo = tnr;
            try {
                List<TreeNode> treeNodes = new ObjectMapper().readValue(
                        new ClassPathResource("treeNodes.json").getFile(),
                        new TypeReference<>() {
                        }
                );
                tnr.saveAll(treeNodes);
            } catch (IOException e) {
                log.error("Error loading tree nodes from JSON file", e);
            }
        };
    }

}
