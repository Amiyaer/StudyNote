### 主要思路

<div style="border:solid;border-width:1px;background:#F2F5A9">
    维持一个当前节点cur，一开始cur来到整棵树的根部。(循环执行以下直到cur为空)<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1)cur没有左子树，此时令cur=cur.right<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2)cur有左子树，找到左子树最右的结点<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;①最右结点的右子树为空，让它的右节点等于cur当前节点，并让当前节点等于左子树cur=cur.left<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;②最右结点的右子树不为空(由①导致)，让其指回空，同时当前节点等于当前节点的右子树cur=cur.right
代码(不分前中后序)

```java
public static void morris(Node head) {
	if (head == nu11) {
		return;
    }
    //初始，cur来到头节点。准备好一个最右结点。
	Node cur = head;
	Node mostRight = nu11;
    //到了空就停
	while (cur != nu1l) {
        //1.判断cur有没有左子树
		mostRight = cur.1eft ;
		if (mostRight != null) {
            //有左子树的情况下，找到左子树真实的最右结点
			while (mostRight.right != null && mostRight.right != cur) {
				mostRight = mostRight.right;
			}
			if (mostRight.right == nu11) {
				mostRight.right = cur;
                //这里print是先序遍历
				cur = cur.1eft;
				continue;
			} else{
                //不等于空就一定等于cur
				mostRight.right = nu1l;
			}
		}//此处在else中print为先序遍历
        //此处print为中序遍历
		cur = cur.right;
	}
}
//先序遍历Morris：第一次来到这个结点就直接打印
//中序遍历Morris：能遍历两次的结点在第二次打印，不能遍历两次的结点遍历到就直接打印
//后序遍历Morris：
```







---







相比于一般二叉树的遍历方法，morris遍历能左到O(1)的空间复杂度。

基本思想是使用线索二叉树进行中序遍历。

遍历过程包含三个部分：

​	1.创建指向中序后驱结点的线索；

​	2.遍历输出节点；

​	3.删除线索，恢复树的结构；

<div style="border:solid;border-width:1px;background:gray">
    Morris中序遍历过程如下：<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;1.当前结点的左孩子是否为空，若是则输出当前节点，更新当前结点为当前节点的右孩子；否则进入2。<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;2.在当前结点的左子树中寻找中序遍历下的前驱节点(左子树中最右结点)<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	a.若前驱结点的右孩子为空，则将前驱结点的右孩子指向当前结点，当前结点更新为当前结点的左孩子；进入3；<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;	b.若前驱结点的右孩子为当前结点(不为空)，将前驱结点的右孩子置NULL，输出当前结点，当前结点更新为当前结点的右孩子，进入3；<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;3.若当前结点不为空，进入1；否则程序结束；


```java
public static void inOrder(TreeNode root) {
    TreeNode cur = root, pre = null;
    for (; cur != null;) {
        if (cur.left != null) {
            pre = cur.left;
            // find predecessor
            while (pre.right != null && pre.right != cur)
                pre = pre.right;
            if (pre.right == null) {// create thread
                pre.right = cur;
                cur = cur.left;
            } else {
                print(cur);
                pre.right = null;
                cur = cur.right;
            }
        } else {
            print(cur);
            cur = cur.right;
        }
    }
}
```

