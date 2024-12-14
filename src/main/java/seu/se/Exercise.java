package seu.se;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Node
public class Exercise {
    @Id
    private String questionId;
    private String question;
    private List<OptionPair> options;
    private String answer;

    public interface Choice {
        String A = "A";
        String B = "B";
        String C = "C";
        String D = "D";
    }

    @Node
    @JsonSerialize(using = OptionPair.OptionPairSerializer.class)
    public static class OptionPair {
        @Id
        private final String id;
        private final String key;
        private final String value;

        public static class OptionPairSerializer extends StdSerializer<OptionPair> {
            public OptionPairSerializer() {
                this(null);
            }

            public OptionPairSerializer(Class<OptionPair> t) {
                super(t);
            }

            @Override
            public void serialize(OptionPair value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeStartObject();
                gen.writeStringField(value.key, value.value);
                gen.writeEndObject();
            }
        }

        public OptionPair(String id, String key, String value) {
            this.id = id;
            this.key = key;
            this.value = value;
        }

        public String value() {
            return value;
        }

        public String key() {
            return key;
        }

        public String getId() {
            return id;
        }
    }

    @JsonCreator
    public Exercise(@JsonProperty("questionId") String questionId, @JsonProperty("question") String question, @JsonProperty("options") Map<String, String> options, @JsonProperty("answer") String answer) {
        this.questionId = questionId;
        this.question = question;
        this.options = new LinkedList<OptionPair>();
        options.forEach((k, v) -> this.options.add(new OptionPair(String.format("%s.%s", questionId, k), k, v)));
        this.answer = answer;
    }

    @Autowired
    @PersistenceCreator
    // https://stackoverflow.com/questions/55827640/how-to-fix-failed-to-instantiate-classname-using-constructor-no-constructor
    public Exercise(String questionId, String question, List<OptionPair> options, String answer) {
        this.questionId = questionId;
        this.question = question;
        this.options = options;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestionId() {
        return questionId;
    }

    public List<OptionPair> getOptions() {
        return options;
    }

    public void setOptions(List<OptionPair> options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
