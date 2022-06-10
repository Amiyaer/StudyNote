# ==HashMap的底层实现==

## 扩容机制

### 1.结论

1. 底层维护`Node`类型的数组`table`。每一个元素都是一个`Node`。
2. 创建对象时，将负载因子`loadfactor`初始化为0.75。
3. 第一次添加元素：需要扩容`table`的容量为16，临界值`threshold`为12(16*`loadfactor`)。
4. 以后再进行扩容时，每一次扩容为原来的2倍，临界值也设置为原来的2倍。

树化：在Java8中如果**链表**里的元素超过`TREEIFY_THRESHOLD`(默认为8)，并且`table`的大小大于等于`MIN_TREEIFY_CAPACITY`(默认为64)，就会进行树化。





## 源码

### 0.一些重要的属性

#### 0.1`Node`

`HashMap`的一个内部类，本质是一个映射(键值对)

#### 0.2 字段

`threshold` :允许的最大元素数目。

`loadFactor` :负载因子。

`modCount` :`HashMap`内部发生变化的次数。

`DEFAULT_INITIAL_CAPACITY`：初始数组最大容量1<<4(16)。

`TREEIFY_THRESHOLD`：树化最小结点(链表)。

`UNTREEIFY_THRESHOLD`：链表化临界结点数(红黑树)。

`MIN_TREEIFY_CAPACITY`：发生树化时，数组的最小长度。默认64。



---



### 1.执行构造器

初始化加载因子`loadfactor`为0.75。

`Node[] table = null;` 



---



### 2.`put`方法

#### 2.1 调用`hash`方法，计算`key`的哈希值

`(h = key.hashCode())^(h>>>16)` 

#### 2.2 执行`putVal()`方法进行数据插入

* 首先判断当前的`table`是否为空。
  * 如果为空或长度为0，直接对`table`进行扩容`resize()`，该方法会初始化空间，新建一个`Node`类型的数组并将它赋给现在的`table`，第一次会扩容到16。
    * `Node<K,V>[] newTab = (Node<K,V>)new Node[newCap];` 
    * `table = newTab;` 
* 搞定`table`后，开始插入数据，先根据key得到哈希值`(n-1)&hash`，看数组中相应的位置是否为空。
  * 如果是空，就创建一个新的结点`Node`并把数据插入。
  * 如果不为空，也就是说，插入位置发生哈希碰撞。此时需要别的判断。
    * 声明一个`Node`结点`e`用于保存当前发生碰撞的结点。
    * 如果二者的哈希值和`key`相同，或者仅仅是值相同(不为空)，就进行记录e并直接`break`。
    * 如果当前结点已经是一个红黑树，就直接按照红黑树的方法进行添加。
    * **如果是链表的情况**，就要进行循环比较链表中的各个值，找到了相同的key就同上，记录e，然后break；如果没找到相同的key，就加到链表的最后。
      * 加入到末尾后需要判断当前链表的长度是否已到达`TREEIFY_THRESHOLD`8，如果到达了就要调用`treeifyBin(tab,hash)`方法进行红黑树的树化。
      * 树化方法会对数组元素是否达到`MIN_TREEIFY_CAPACITY`64进行判断，只有数组元数个数也到达了64才会进行树化，否则就仅是扩容。
* 如果走完一遍，e(某节点)不为空，就表示要进行替换操作。
* 插入数据完成后，`size++`，判断当前的数组的大小`size`有没有达到临界值 ``threshold``，**达到临界值就直接扩容`resize()`**，而不是等到最大容量。

```java
 public V put(K key, V value) {
      // 对key的hashCode()做hash
      return putVal(hash(key), key, value, false, true);
  }
  
  final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                 boolean evict) {
      Node<K,V>[] tab; Node<K,V> p; int n, i;
      // 步骤①：tab为空则创建
     if ((tab = table) == null || (n = tab.length) == 0)
         n = (tab = resize()).length;
     // 步骤②：计算index，并对null做处理 
     if ((p = tab[i = (n - 1) & hash]) == null) 
         tab[i] = newNode(hash, key, value, null);
     else {
         Node<K,V> e; K k;
         // 步骤③：节点key存在，直接覆盖value
         if (p.hash == hash &&
             ((k = p.key) == key || (key != null && key.equals(k))))
             e = p;
         // 步骤④：判断该链为红黑树
         else if (p instanceof TreeNode)
             e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
         // 步骤⑤：该链为链表
         else {
             for (int binCount = 0; ; ++binCount) {
                 if ((e = p.next) == null) {
                     p.next = newNode(hash, key,value,null);
                        //链表长度大于8转换为红黑树进行处理
                     if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st  
                         treeifyBin(tab, hash);
                     break;
                 }
                    // key已经存在直接覆盖value
                 if (e.hash == hash &&
                     ((k = e.key) == key || (key != null && key.equals(k))))                                       break;
                 p = e;
             }
         }         
         if (e != null) { // existing mapping for key
             V oldValue = e.value;
             if (!onlyIfAbsent || oldValue == null)
                 e.value = value;
             afterNodeAccess(e);
             return oldValue;
         }
     }

     ++modCount;
     // 步骤⑥：超过最大容量 就扩容
     if (++size > threshold)
         resize();
     afterNodeInsertion(evict);
     return null;
 }
```



---



### 3.`hash`方法

```java
//方法一：
static final int hash(Object key) {   //jdk1.8 & jdk1.7
     int h;
     // h = key.hashCode() 为第一步 取hashCode值
     // h ^ (h >>> 16)  为第二步 高位参与运算
     return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
//方法二：
static int indexFor(int h, int length) {  //jdk1.7的源码，jdk1.8没有这个方法，但是实现原理一样的
     return h & (length-1);  //第三步 取模运算
}

```



---



### 4.扩容

#### JDK1.7中的方法。

如果扩容前数组的长度已经到达了能够扩容的最大值了(2^30^)，就不再进行扩容。

否则就初始化一个新的数组，将元素转移到新的数组里，再修改新的阈值。扩容时所有的元素会全部重新计算一次哈希，所以不一定会在哈希表中保持原来的结构。

```java
void resize(int newCapacity) {   //传入新的容量
    Entry[] oldTable = table;    //引用扩容前的Entry数组
    int oldCapacity = oldTable.length;         
    if (oldCapacity == MAXIMUM_CAPACITY) {  //扩容前的数组大小如果已经达到最大(2^30)了
        threshold = Integer.MAX_VALUE; //修改阈值为int的最大值(2^31-1)，这样以后就不会扩容了
        return;
    }
 
    Entry[] newTable = new Entry[newCapacity];  //初始化一个新的Entry数组
    transfer(newTable);                         //！！将数据转移到新的Entry数组里
    table = newTable;                           //HashMap的table属性引用新的Entry数组
    threshold = (int)(newCapacity * loadFactor);//修改阈值
}
```



#### JDK1.8的优化

我们使用的是2次幂的扩展，所以扩容后元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。

![img](https://img-blog.csdnimg.cn/20190728110949685.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190728111006889.png)

这样就不用像JDK1.7一样重新计算hash，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+`oldCap`”。由于新增位是1还是0可以认为是随机的，因此resize的过程就把原来的冲突节点相对分散了。

> `e.hash & oldCap`是关键，他决定当前节点在扩容后被分到原来的位置还是原来的位置+旧容量

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```



---



### 5.线程不安全

可能会引起`java.util.ConcurrentModificationException`异常。

#### 扩容引起：`transfer()`方法将原数组的元素迁移到新数组中 

<font color=blue>**主要原因**</font> 

1. 多线程扩容。
2. JDK1.7采用头插法。

**==Details==**：两个线程对同一个状态的HashMap进行扩容。其中一个线程在确定扩容时阻塞，另一个线程先完成了扩容。原来同一个位置的`A→B`恰好在新数组中下标也相同，在新的数组中变成了`B→A`，此时原来阻塞的线程再次运行，进行扩容，内部逻辑会执行`A.next=newNode`，也就是B，这样`A→B→A`就形成了环形链表，从而死循环。



假设两个线程同时执行put，且都进行扩容，执行到了`transfer()`环节。

* 两个线程执行过程中，e(当前元素)和next都指向了同一个链表里的同一个元素。
* 其中一个线程会休眠，而另一个线程会先完成扩容。由于JDK1.7使用头插法，扩容后另一个线程指向的e和next会被颠倒。
* 另一个线程苏醒，开始进行扩容，执行e = next，此时e就指向了扩容后的前一个元素。而下一次循环的`next = e.next`导致了next指向了扩容后的后一个元素。
* `e.next = newTable[i]`，会导致后一个元素指向链表第一个元素，从而形成环。

