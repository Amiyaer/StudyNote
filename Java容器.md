### 容器一览

![](E:\new add juan\学习\面试\pic\java-collection-hierarchy[1].png)

除map外都实现了collection接口，map结尾的类都实现了map接口。

#### List，Set，Map的区别

List：元素有序可重复

Set：元素无序不可重复

Map：使用键值对存储。键无序不可重复，值无序可重复。每个键最多映射到一个值。



#### Collection接口

##### List

ArrayList：数组

Vector：数组

LinkedList：双向链表，jdk1.6之前为循环链表

##### Set

HashSet（无序，唯一）: 基于 HashMap 实现的，底层采用 HashMap 来保存元素。
LinkedHashSet：HashSet 的子类，并且其内部是通过 LinkedHashMap 来实现的。有点类似于我们之前说的 LinkedHashMap 其内部是基于 HashMap 实现一样，不过还是有一点区别。
TreeSet（有序，唯一）： 红黑树(自平衡的排序二叉树)

##### Map

1.==**HashMap**==:jdk1.8之前是数组加链表组成，数组是主体，而链表为了解决哈希冲突。jdk1.8之后当链表长度大于阈值将链表转化为**红黑树**，以减少搜索时间。

2.LinkedHashMap：继承自HashMap。在HashMap的基础上增加了一条双向链表，使得上面的结构可以保持键值对的插入顺序。同时通过对链表进行相应的操作，实现了访问顺序相关逻辑。

[LinkedHashMap源码](https://www.imooc.com/article/22931)

3.Hashtable： 数组+链表组成的，数组是 HashMap 的主体，链表则是主要为了解决哈希冲突而存在的。
4.TreeMap： 红黑树（自平衡的排序二叉树）

##### 集合的选用

why：集合提高了数据存储的灵活性，可以用来存储不同类型不同数量的对象，还可以保存具有映射关系的数据。

Map：需要排序使用TreeMap，需要保证线程安全就使用ConcurrentHashMap。

List：需要保证元素唯一可以使用TreeSet或HashSet。



------

### List

#### ArrayList和Vector的区别

ArrayList适用于频繁的查找工作做，线程不安全，而vector是线程安全的。

#### ArrayList和LinkedList的区别

两者都不能保证线程安全。

ArrayList底层使用的是Object[]数组，LinkedList底层使用的是双向链表的数据结构。

ArrayList的插入和删除受==元素位置==的影响，指定位置是O(n-i)。LinkedList不会受到影响。插入，删除的时间复杂度近似于O(1)，指定位置是O(n)。

LinkedList不支持==高效的随即元素访问==，而ArrayList支持(get(int index))。

ArrayList 的空间浪费：list 列表的结尾会预留一定的容量空间。

 LinkedList 的空间花费：每一个元素都需要消耗比 ArrayList 更多的空间（因为要存放直接后继和直接前驱以及数据）。

##### 双向链表和双向循环链表

包含两个指针：前驱prev和后继next。

双向循环链表：最后一个节点的next 指向head，而head的prev指向最后一个节点，构成一个环。

##### RandomAccess接口

该接口中什么都没有定义。该接口起到一个标识作用，标识这实现了这个接口的类具有随机访问的功能。

```java
    public static <T>
    int binarySearch(List<? extends Comparable<? super T>> list, T key) {
        if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
            return Collections.indexedBinarySearch(list, key);
        else
            return Collections.iteratorBinarySearch(list, key);
    }
```

ArrayList实现了RandomAccess接口然而LinkedList没有实现。

RandomAccess接口只是一个标识，并不是说要实现了这个接口才具有随机访问的功能。

#### ArrayList的扩容机制(见后)



------

### Set

#### Comparable和Comparator的区别

Comparable接口：出自java.lang包 它有一个 ==compareTo==(Object obj)方法用来排序。
Comparator接口：出自 java.util 包它有一个==compare==(Object obj1, Object obj2)方法用来排序。

一般使用一个集合自定义排序时需要重写这两种方法。

##### Comparator定制排序

如果只有一个arrayList参数就是使用默认的排序

```JAVA
Collections.sort(arrayList, new Comparator<类型>() {

            @Override
            public int compare(类型 o1, 类型 o2) {
                return o2.compareTo(o1);
            }
        });
```

##### 重写compareTo方法

作为某个特定类(或数据类型)的方法

```java
 @Override
    public int compareTo(类 o) {
        if (/*比较条件1*/) {
            return 1;
            //return 1 表示往比较对象后排
        }
        if (/*比较条件2*/) {
            return -1;
            //return -1 表示往比较对象前面排
        }
        return 0;
        //相等，不需要排序
    }
```



#### 无序性和不可重复性

无序性：不等于随机性，无序性表示存储的数据在==底层的数组==中并非按照数组索引的顺序添加，而是根据数据的哈希值决定。

不可重复性：添加的元素按照equals()判断时返回false，需要同时重写equals()方法和HashCode()方法。



#### HashSet、LinkedHashSet和TreeSet三者的异同

HashSet是Set接口的主要实现类，底层是HashMap，线程不安全，可以存储null值。

LinkedHashSet是HashSet的子类，能够按照添加的顺序遍历。

TreeSet：底层使用红黑树，能够按照添加元素的顺序进行遍历，排序的方式有自然排序和定制排序。



------

### Map接口

#### HashMap和Hashtable的区别

1.HashMap线程不安全，HashTable内部的方法使用synchronized修饰，是线程安全的。(但是保证线程安全使用ConcurrentHashMap！😅)

2.HashMap的效率更高。HashTable基本被淘汰了。

3.HashMap可以储存null的key和value，但是HashTable不能。

4.==**初始大小与扩容机制**==

==HashMap的默认初始大小为**16**，之后每次扩容为原来的2倍。如果设置了初始大小，会扩充为**2的幂次方**(**tableSizeFor()**方法进行了保证)。==

HashTable的默认初始大小为11，每次扩充容量变为原来的2n+1。如果设置了初始大小，就会直接使用给定的大小。

5.底层数据结构：jdk1.8以后的HashMap在解决哈希冲突的时候有了较大的变化，当链表长度大于阈值(默认为8)时，将链表转化为红黑树。HashTable没有这样的机制。

*<u>将链表转换成红黑树前会判断，如果当前数组的长度小于 64，那么会选择先进行数组扩容，而不是转换为红黑树。</u>*



#### HashMap和HashSet的区别

HashSet底层基于HashMap实现，除了clone()、writeObject()、readObject()是自己实现的方法，其他方法都是直接调用HashMap

| HashMap             | HashSet                                                      |
| ------------------- | ------------------------------------------------------------ |
| 实现了Map接口       | 实现Set接口                                                  |
| 存储键值对          | 存储对象                                                     |
| 调用put()添加元素   | 调用add()方法向Set中添加元素                                 |
| 使用key计算hashcode | 使用成员对象来计算hashcode值，对于两个对象来说，hashcode可能相同，所以equals()方法用来判断对象的相等性 |



#### HashMap和TreeMap的区别

都继承自AbstractMap，TreeMap还实现了NavigableMap接口(搜索能力)和SortedMap接口(根据键``默认升序``排序的能力)。

*Compare方法return正数时交换位置*

自己实现TreeMap中的compare方法

```java
TreeMap<Person, String> treeMap = new TreeMap<>((person1, person2) -> {
  int num = person1.getAge() - person2.getAge();
  return Integer.compare(num, 0);
});

```



#### HashSet查重

把对象加入到HashSet中，HashSet会计算对象的hashcode判断其加入的位置。如果发现有相同hashcode的对象，就会调用equals()方法来检查hashcode相等的对象是否真的相同。如果相同，HashSet就不会让加入操作成功。

两个对象有相同的hashcode值，他们也不一定相等。

如果equals()方法被覆盖过，则hashcode()方法也必须被覆盖。？



**==与 equals 的区别**
对于基本类型来说，前者比较的是值是否相等；
对于引用类型来说，前者比较的是两个引用是否==指向同一个对象地址==（两者在内存中存放的地址（堆内存地址）是否指向同一个地方）；
对于引用类型（包括包装类型）来说，equals 如果没有被重写，对比它们的==地址是否相等==；如果 equals()方法被重写（例如 String），则比较的是地址里的内容。



#### ==HashMap的底层实现==

~~见手抄~~

treeifyBin方法

```java
 final void treeifyBin(Node<K,V>[] tab, int hash) {
        //定义几个变量，n是数组长度，index是索引
        int n, index; Node<K,V> e;
        //这里的tab指的是本HashMap中的数组，n为数字长度，如果数组为null或者数组长度小于64
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            //则调用resize()方法直接扩容，不转红黑树
            resize();
        //否则说明满足转红黑树条件，通过按位与运算取得索引index，并将该索引对应的node节点赋值给e，e不为null时    
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            //定义几个变量，hd代表头节点，tl代表尾节点
            TreeNode<K,V> hd = null, tl = null;
            do {
                //先把e节点转成TreeNode类型，并赋值给p
                TreeNode<K,V> p = replacementTreeNode(e, null);
                //如果尾节点tl为空，则说明还没有根节点，试想下，这时元素数目都超过8个了，还能没有尾节点么，所以没有尾节点只能说明还没设置根节点
                if (tl == null)
                    //设置根节点，把p赋值给根节点hd
                    hd = p;
                else {
                    //把tl设置为p的前驱节点
                    p.prev = tl;
                    //把p设置为tl的后继节点，这两步其实就是我指向你，你指向我的关系，为了形成双向链表 
                    tl.next = p;
                }
                //把首节点设置成p后，把p赋值给尾节点tl，然后会再取链表的下一个节点，转成TreeNode类型后再赋值给p，如此循环
                tl = p;
                //取下一个节点，直到下一个节点为空，也就代表这链表遍历好了
            } while ((e = e.next) != null);
            //用新生成的双向链表替代旧的单向链表，其实就是把这个数组对应的位置重新赋值成新双向链表的首节点
            if ((tab[index] = hd) != null)
                //这个方法里就开始做各种比较，左旋右旋，然后把双向链表搞成一个红黑树
                hd.treeify(tab);
        }
    }

```

