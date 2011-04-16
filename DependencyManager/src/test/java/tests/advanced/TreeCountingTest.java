package tests.advanced;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;
import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;
import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.Combinator;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.util.Function2;
import org.workcraft.util.Nothing;

public class TreeCountingTest {

	interface TreeNodeVisitor<T> {
		T visitNil();
		T visitBranch(Variable<TreeNode> left, Variable<TreeNode> right);
	}
	
	interface TreeNode {
		<T> T apply(TreeNodeVisitor<T> v);
	}

	static final TreeNode nilNode = new TreeNode() {
		@Override
		public <T> T apply(TreeNodeVisitor<T> v) {
			return v.visitNil();
		}
	};
	
	static Variable<TreeNode> buildTree(int depth) {
		if(depth == 0)
			return Variable.create(nilNode);
		else
			return branch(buildTree(depth-1), buildTree(depth-1));
	}

	private static Variable<TreeNode> branch(final Variable<TreeNode> left, final Variable<TreeNode> right) {
		return Variable.<TreeNode>create(new TreeNode() {
			@Override
			public <T> T apply(TreeNodeVisitor<T> v) {
				return v.visitBranch(left, right);
			}
		});
	}
	
	static Random r = new Random();
	
	@Test
	public void testTreeCount() throws InterruptedException {
		int initialDepth = 16;
		Variable<TreeNode> tree = buildTree(initialDepth);
		int expectedCount = 1<<initialDepth;
		Expression<? extends Integer> treeCount = treeCount(tree);
		assertEquals(expectedCount, eval(treeCount).intValue());

		for(int i=0;i<100;i++) {
			long iterationStartTime = System.currentTimeMillis();
			addRandomChild(tree);
			expectedCount++;
			assertEquals(expectedCount, eval(treeCount).intValue());
			long iterationDuration = System.currentTimeMillis()-iterationStartTime;
			assertTrue("iteration should be faster than 200ms (actually " + iterationDuration + "ms)", iterationDuration < 200);
			if(iterationDuration > 20)
				System.err.println("warning: O(16) iteration took " + iterationDuration + " ms");
		}
	}
	
	private void addRandomChild(final Variable<TreeNode> node) {
		node.getValue().apply(new TreeNodeVisitor<Nothing>(){
			@Override
			public Nothing visitNil() {
				node.setValue(buildTree(1).getValue());
				return Nothing.VALUE;
			}

			@Override
			public Nothing visitBranch(Variable<TreeNode> left, Variable<TreeNode> right) {
				boolean useLeft = r.nextBoolean();
				addRandomChild(useLeft? left : right);
				return Nothing.VALUE;
			}
		});
	}

	
	static Expression<? extends Integer> sum(Expression<? extends Integer> e1, Expression<? extends Integer> e2) {
		return fmap(new Function2<Integer, Integer, Integer>() {
			@Override
			public Integer apply(Integer a_val, Integer b_val) {
				return a_val + b_val;
			}
		}, e1, e2);
	}

	/**
	 * treeCount :: Expression TreeNode -> Expression Integer
	 * treeCount tree = tree >>= tc
	 *     where
	 *         tc Nil = return 1
	 *         tc (Branch a b) = (+) <$> treeCount a <*> treeCount b
	 */
	private static Expression<? extends Integer> treeCount(final Expression<TreeNode> tree) {
		
		return bind(tree, new Combinator<TreeNode, Integer>(){
			@Override
			public Expression<? extends Integer> apply(TreeNode treeValue) {
				return treeValue.apply(new TreeNodeVisitor<Expression<? extends Integer>>() {
					@Override
					public Expression<? extends Integer> visitNil() {
						return Expressions.constant(1);
					}
					@Override
					public Expression<? extends Integer> visitBranch(Variable<TreeNode> left, Variable<TreeNode> right) {
						return sum(treeCount(left), treeCount(right));
					}
				});
			}
		});
	}
}
