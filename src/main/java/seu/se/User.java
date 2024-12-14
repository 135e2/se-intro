package seu.se;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @JsonIgnore
    private String password;

    @JsonCreator
    public User(@JsonProperty("name") String name, @JsonProperty("password") String password) throws UserException {
        setName(name);
        setPassword(password);
    }

    static public class UserException extends Exception {
        public UserException() {
        }

        public UserException(String message) {
            super(message);
        }
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return String.format("User id: %d, name: %s", id, name);
    }

    public void setName(String name) throws UserException {
        if (name.length() > 22)
            throw new UserException("Username too long!");
        this.name = name;
    }

    public void setPassword(String password) throws UserException {
        if (password.length() < 8)
            throw new UserException("Password too short!");
        this.password = password;
    }
}

