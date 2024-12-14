package seu.se;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RequestMapping("/api")
@RestController
public class ApiController {
    public static abstract class DataObject {
        public Boolean ok;
        public String message;
        public Object otherData;
    }

    public static abstract class ReturnObject {
        public String status;
        public DataObject dataObj;
    }

    @GetMapping(value = "/ping")
    public String Ping() {
        return "Pong!";
    }

    @RequestMapping("/api/user")
    @RestController
    static class UserController {
        private final UserRepository uRepo;

        UserController(UserRepository uRepo) {
            this.uRepo = uRepo;
        }

        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ok", content =
                        {@Content(mediaType = "application/json", schema =
                        @Schema(implementation = ReturnObject.class))}),
                @ApiResponse(responseCode = "400", description = "Invalid userId supplied"),
                @ApiResponse(responseCode = "500", description = "Internal server error")})
        @PostMapping("/login")
        public JsonNode Login(@RequestBody com.fasterxml.jackson.databind.JsonNode body) {
            var id = body.get("userId").asInt();
            var password = body.get("password").asText();
            var user = uRepo.findById(id);
            var retObj = new ObjectMapper().createObjectNode();
            var dataObj = new ObjectMapper().createObjectNode();
            String message;
            Boolean ok;
            if (user != null) {
                if (password.equals(user.getPassword())) {
                    ok = true;
                    message = "Login Success";
                    // save session
                    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                    attr.getRequest().getSession(true).setAttribute("SESSION", user);

                } else {
                    ok = false;
                    message = "Wrong password";
                }
                dataObj = new ObjectMapper().valueToTree(user);
            } else {
                ok = false;
                message = "User not found";
            }
            dataObj.put("ok", ok);
            dataObj.put("message", message);
            retObj.set("data", dataObj);
            retObj.put("status", "ok");
            return retObj;
        }

        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ok", content =
                        {@Content(mediaType = "application/json", schema =
                        @Schema(implementation = ReturnObject.class))}),
                @ApiResponse(responseCode = "500", description = "Internal server error")})
        @PostMapping("/register")
        public JsonNode Register(@RequestBody com.fasterxml.jackson.databind.JsonNode body) {
            var username = body.get("username").asText();
            var password = body.get("password").asText();
            String message;
            Boolean ok;
            var retObj = new ObjectMapper().createObjectNode();
            var dataObj = new ObjectMapper().createObjectNode();
            try {
                var user = uRepo.save(new User(username, password));
                ok = true;
                message = "Registered";
                dataObj = new ObjectMapper().valueToTree(user);
            } catch (User.UserException ue) {
                ok = false;
                message = ue.getMessage();
            }
            dataObj.put("ok", ok);
            dataObj.put("message", message);
            retObj.set("data", dataObj);
            retObj.put("status", "ok");
            return retObj;
        }
    }

    @RequestMapping("/api/node")
    @RestController
    static class TreeNodeController {
        private final TreeNodeRepository tnRepo;

        TreeNodeController(TreeNodeRepository tnRepo) {
            this.tnRepo = tnRepo;
        }

        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ok", content =
                        {@Content(mediaType = "application/json", schema =
                        @Schema(implementation = ReturnObject.class))}),
                @ApiResponse(responseCode = "500", description = "Internal server error")})
        @GetMapping("/content/{nodeId}")
        public JsonNode getContent(@PathVariable String nodeId) {
            String message;
            Boolean ok;
            var retObj = new ObjectMapper().createObjectNode();
            var dataObj = new ObjectMapper().createObjectNode();
            var treeNode = tnRepo.findByNodeId(nodeId);
            if (treeNode != null) {
                ok = true;
                message = "Success";
                dataObj.put("nodeId", treeNode.getNodeId());
                dataObj.put("content", treeNode.getContent());
            } else {
                ok = false;
                message = "Node not found";
            }
            dataObj.put("ok", ok);
            dataObj.put("message", message);
            retObj.set("data", dataObj);
            retObj.put("status", "ok");
            return retObj;
        }

        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ok", content =
                        {@Content(mediaType = "application/json", schema =
                        @Schema(implementation = ReturnObject.class))}),
                @ApiResponse(responseCode = "500", description = "Internal server error")})
        @GetMapping("/exercises/{nodeId}")
        public JsonNode getExercises(@PathVariable String nodeId) {
            String message;
            Boolean ok;
            var retObj = new ObjectMapper().createObjectNode();
            var dataObj = new ObjectMapper().createObjectNode();
            var treeNode = tnRepo.findByNodeId(nodeId);
            if (treeNode != null) {
                ok = true;
                message = "Success";
                dataObj.put("nodeId", treeNode.getNodeId());
                var exercisesObj = new ObjectMapper().createArrayNode();
                for (var i : treeNode.getExercises()) {
                    var exerciseObj = new ObjectMapper().valueToTree(i);
                    exercisesObj.add(exerciseObj);
                }
                dataObj.set("exercises", exercisesObj);
            } else {
                ok = false;
                message = "Node not found";
            }
            dataObj.put("ok", ok);
            dataObj.put("message", message);
            retObj.set("data", dataObj);
            retObj.put("status", "ok");

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            System.out.println(attr.getRequest().getSession(true).getAttribute("SESSION"));

            return retObj;
        }

        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ok", content =
                        {@Content(mediaType = "application/json", schema =
                        @Schema(implementation = ReturnObject.class))}),
                @ApiResponse(responseCode = "500", description = "Internal server error")})
        @PostMapping("/submit/{nodeId}")
        public JsonNode Submit(@PathVariable String nodeId, @RequestBody com.fasterxml.jackson.databind.JsonNode body) {
            String message;
            Boolean ok;
            var retObj = new ObjectMapper().createObjectNode();
            var dataObj = new ObjectMapper().createObjectNode();
            var treeNode = tnRepo.findByNodeId(nodeId);
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            var user = (User) attr.getRequest().getSession(true).getAttribute("SESSION");
            if (treeNode == null) {
                ok = false;
                message = "Node not found";
            } else if (user == null) {
                ok = false;
                message = "Login first";
            } else {
                var score = body.get("score").asDouble();
                var usp = treeNode.getUserScorePair(user.getId(), nodeId);
                if (usp != null) {
                    usp.getScores().add(score);
                } else {
                    treeNode.getUserScores().add(new TreeNode.UserScorePair(treeNode.getNodeId(), user.getId(), List.of(score)));
                }
                tnRepo.save(treeNode);
                ok = true;
                message = "Success";
                dataObj.put("userId", user.getId());
                dataObj.put("nodeId", nodeId);
            }
            dataObj.put("ok", ok);
            dataObj.put("message", message);
            retObj.set("data", dataObj);
            retObj.put("status", "ok");
            return retObj;
        }

        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ok", content =
                        {@Content(mediaType = "application/json", schema =
                        @Schema(implementation = ReturnObject.class))}),
                @ApiResponse(responseCode = "500", description = "Internal server error")})
        @GetMapping("/history/{nodeId}")
        public JsonNode History(@PathVariable String nodeId) {
            String message;
            Boolean ok;
            var retObj = new ObjectMapper().createObjectNode();
            var dataObj = new ObjectMapper().createObjectNode();
            var treeNode = tnRepo.findByNodeId(nodeId);
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            var user = (User) attr.getRequest().getSession(true).getAttribute("SESSION");
            if (treeNode == null) {
                ok = false;
                message = "Node not found";
            } else if (user == null) {
                ok = false;
                message = "Login first";
            } else {
                var usp = treeNode.getUserScorePair(user.getId(), nodeId);
                if (usp != null) {
                    var uspObj = new ObjectMapper().valueToTree(usp);
                    ok = true;
                    message = "Success";
                    dataObj.set("historyScores", uspObj);
                } else {
                    ok = false;
                    message = "No score found";
                }
                dataObj.put("userId", user.getId());
            }
            dataObj.put("ok", ok);
            dataObj.put("message", message);
            retObj.set("data", dataObj);
            retObj.put("status", "ok");
            return retObj;
        }

        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ok", content =
                        {@Content(mediaType = "application/json", schema =
                        @Schema(implementation = ReturnObject.class))}),
                @ApiResponse(responseCode = "500", description = "Internal server error")})
        @GetMapping("/home")
        public JsonNode Home() {
            var retObj = new ObjectMapper().createObjectNode();
            var dataObj = new ObjectMapper().createObjectNode();
            String message;
            Boolean ok;
            var nodes = tnRepo.findAll();
            var roots = new HashSet<TreeNode>();
            for (var n : nodes) {
                if (n.getLevel() == 1) roots.add(n);
            }
            if (roots.isEmpty()) {
                ok = false;
                message = "No root found";
            } else {
                class RecursiveHelper {
                    public final List<TreeNode> nodes;

                    public RecursiveHelper(List<TreeNode> nodes) {
                        this.nodes = nodes;
                    }

                    public ObjectNode RecursiveConstruct(TreeNode tn) {
                        ObjectNode graphNodesObj = new ObjectMapper().valueToTree(tn);
                        ArrayNode graphNodesIterateArr = new ObjectMapper().createArrayNode();
                        var children = tn.getChildren(nodes);
                        if (!children.isEmpty()) {
                            children.forEach(e -> {
                                graphNodesIterateArr.add(RecursiveConstruct(e));
                            });
                            graphNodesObj.set("children", graphNodesIterateArr);
                        }
                        return graphNodesObj;
                    }
                }
                ArrayNode graphNodesIterateArr = new ObjectMapper().createArrayNode();
                for (var root : roots) {
                    graphNodesIterateArr.add(new RecursiveHelper(nodes).RecursiveConstruct(root));
                }
                dataObj.set("graphNodes", graphNodesIterateArr);
                ok = true;
                message = "Success";
            }
            dataObj.put("ok", ok);
            dataObj.put("message", message);
            retObj.set("data", dataObj);
            retObj.put("status", "ok");
            return retObj;
        }

        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ok", content =
                        {@Content(mediaType = "application/json", schema =
                        @Schema(implementation = ReturnObject.class))}),
                @ApiResponse(responseCode = "500", description = "Internal server error")})
        @GetMapping("/review-suggestion")
        public JsonNode ReviewSuggestion() {
            String message;
            Boolean ok;
            var retObj = new ObjectMapper().createObjectNode();
            var dataObj = new ObjectMapper().createObjectNode();
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            var user = (User) attr.getRequest().getSession(true).getAttribute("SESSION");
            if (user == null) {
                ok = false;
                message = "Login first";
            } else {
                var reviewSuggestionsArr = new ObjectMapper().createArrayNode();
                var nodes = tnRepo.findAll();
                for (var node : nodes) {
                    var usp = node.getUserScorePair(user.getId(), node.getNodeId());
                    if (usp == null) continue;
                    var msl = TreeNode.UserScorePair.MasteryLevel.getMasteryLevel(usp.getAverageScore());
                    if (msl.equals(TreeNode.UserScorePair.MasteryLevel.Average) || msl.equals(TreeNode.UserScorePair.MasteryLevel.Bad))
                        reviewSuggestionsArr.add(node.getNodeId());
                }
                ok = true;
                message = "Success";
                dataObj.put("userId", user.getId());
                dataObj.set("reviewSuggestions", reviewSuggestionsArr);
            }
            dataObj.put("ok", ok);
            dataObj.put("message", message);
            retObj.set("data", dataObj);
            retObj.put("status", "ok");
            return retObj;
        }
    }
}
