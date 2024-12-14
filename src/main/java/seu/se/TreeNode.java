package seu.se;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Node
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreeNode {
    @Id
    final private String nodeId;
    final private String label;
    @JsonIgnore
    final private String content;
    final private String relation;
    @JsonIgnore
    final private TreeNode father;
    @JsonIgnore
    private List<Exercise> exercises;
    @JsonIgnore
    private List<UserScorePair> userScores;
    public static TreeNodeRepository tnRepo;

    @Node
    public static class UserScorePair {
        @Id
        private final String id;
        private final String nodeId;
        private final Long userId;
        private List<Double> scores;

        public interface MasteryLevel {
            String Excellent = "完美";
            String Good = "很好";
            String Average = "一般";
            String Bad = "差";

            static String getMasteryLevel(double averageScore) {
                if (averageScore > 95)
                    return Excellent;
                else if (averageScore > 80)
                    return Good;
                else if (averageScore > 60)
                    return Average;
                else
                    return Bad;
            }
        }

        public static class UserScoreSerializer extends StdSerializer<UserScorePair> {
            public UserScoreSerializer() {
                this(null);
            }

            public UserScoreSerializer(Class<UserScorePair> t) {
                super(t);
            }

            @Override
            public void serialize(UserScorePair value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeStartObject();
                gen.writeStringField("nodeId", value.nodeId);
                var avg = value.getAverageScore();
                gen.writeNumberField("averageScore", avg);
                gen.writeNumberField("lastScore", value.getLastScore());
                gen.writeStringField("masteryLevel", MasteryLevel.getMasteryLevel(avg));
                gen.writeEndObject();
            }
        }

        public UserScorePair(String nodeId, Long userId, List<Double> scores) {
            this(String.format("%s.%d", nodeId, userId), nodeId, userId, scores);
        }

        @Autowired
        @PersistenceCreator
        public UserScorePair(String id, String nodeId, Long userId, List<Double> scores) {
            this.id = id;
            this.nodeId = nodeId;
            this.userId = userId;
            this.scores = scores;
        }

        public String getId() {
            return id;
        }

        public String getNodeId() {
            return nodeId;
        }

        public Long getUserId() {
            return userId;
        }

        public List<Double> getScores() {
            return scores;
        }

        public Double getLastScore() {
            return scores.getLast();
        }

        public Double getAverageScore() {
            Double sum = 0.0;
            for (var i : scores)
                sum += i;
            return scores.isEmpty() ? sum : sum / scores.size();
        }

        public void setScores(List<Double> scores) {
            this.scores = scores;
        }
    }

    // MUST only be called during deserialization!!
    @JsonCreator
    public TreeNode(@JsonProperty("nodeId") String nodeId, @JsonProperty("label") String label, @JsonProperty("content") String content, @JsonProperty("relation") String relation, @JsonProperty("fatherId") String fatherId, @JsonProperty("exercises") List<Exercise> exercises) {
        this(nodeId, label, content, relation, tnRepo.findByNodeId(fatherId), exercises, new ArrayList<>());
    }

    public TreeNode(String nodeId, String label, String content, String relation, TreeNode father, List<Exercise> exercises) {
        this(nodeId, label, content, relation, father, exercises, new ArrayList<>());
    }

    @Autowired
    @PersistenceCreator
    public TreeNode(String nodeId, String label, String content, String relation, TreeNode father, List<Exercise> exercises, List<UserScorePair> userScores) {
        this.nodeId = nodeId;
        this.label = label;
        this.content = content;
        this.relation = relation;
        this.father = father;
        this.exercises = exercises;
        this.userScores = userScores;
    }

    public String getLabel() {
        return label;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getContent() {
        return content;
    }

    public TreeNode getFather() {
        return father;
    }

    public String getRelation() {
        return relation;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public List<UserScorePair> getUserScores() {
        return userScores;
    }

    public UserScorePair getUserScorePair(Long userId, String nodeId) {
        var usp = userScores.stream().filter(e -> Objects.equals(e.getUserId(), userId) && e.getNodeId().equals(nodeId)).findFirst();
        return usp.orElse(null);
    }

    public void setUserScores(List<UserScorePair> userScores) {
        this.userScores = userScores;
    }

    public int getLevel() {
        var tmp = this;
        var level = 1;
        while (tmp.father != null) {
            tmp = tmp.father;
            level++;
        }
        return level;
    }

    public Set<TreeNode> getChildren(List<TreeNode> nodes) {
        var children = new HashSet<TreeNode>();
        for (var n : nodes) {
            if (n.getFather() == this) {
                children.add(n);
            }
        }
        return children;
    }
}
