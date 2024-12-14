package seu.se;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TreeNodeRepository extends PagingAndSortingRepository<TreeNode, String>, ListCrudRepository<TreeNode, String> {
    TreeNode findByNodeId(String nodeId);
}
