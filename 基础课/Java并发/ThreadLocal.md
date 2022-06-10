# ThreadLocal

==主要实现的问题是，每一个线程都有自己的专属本地变量==，让每个线程绑定自己的值，可以将`ThreadLocal`类形象的比喻成存放数据的盒子，盒子中可以存储每个线程的私有数据。

访问`ThreadLocal`变量的每个线程都会有这个变量的本地副本



## 四种引用类型

- **强引用**：我们常常new出来的对象就是强引用类型，只要强引用存在，垃圾回收器将永远不会回收被引用的对象，哪怕内存不足的时候。
- **软引用**：使用SoftReference修饰的对象被称为软引用，软引用指向的对象在内存要溢出的时候被回收。
- **弱引用**：使用WeakReference修饰的对象被称为弱引用，只要发生垃圾回收，若这个对象只被弱引用指向，那么就会被回收。
- **虚引用**：虚引用是最弱的引用，在 Java 中使用 PhantomReference 进行定义。虚引用中唯一的作用就是用队列接收对象即将死亡的通知。



## 数据结构

ThreadLocalMap，类似于`hashmap`，只是没有链表结构。每个线程有一个自己的`ThreadLocalMap`。

![img](https://snailclimb.gitee.io/javaguide/docs/java/multi-thread/images/thread-local/2.png)

可以将它的key视作`ThreadLocal`的一个弱引用，value为代码中放入的值。

每个线程放值的时候，都往自己的map里存，读也是在自己的map里去查找。

注意`Entry`， 它的`key`是`ThreadLocal<?> k` ，继承自`WeakReference`， 也就是我们常说的弱引用类型。



## 原理

Thread通过get和set方法对ThreadLocalMap进行操作。

**最终的变量放在当前线程的 `ThreadLocalMap` 中，并不是存在 `ThreadLocal` 上，`ThreadLocal` 可以理解为只是`ThreadLocalMap`的封装，传递了变量值。** `ThrealLocal` 类中可以通过`Thread.currentThread()`获取到当前线程对象后，直接通过`getMap(Thread t)`可以访问到该线程的`ThreadLocalMap`对象。

![ThreadLocal数据结构](https://snailclimb.gitee.io/javaguide/docs/java/multi-thread/images/threadlocal%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84.png)





## ThreadLocal的内存泄露问题

`ThreadLocalMap`中使用的key为ThreadLocal的弱引用，但是`value是强引用`。

如果`ThreadLocal`在垃圾回收的时候被清理，而value是强引用不会被清理。如果不采取措施，此时key为null，value永远无法被GC回收，这个时候就有可能发生内存泄露。

解决方法：`ThreadLocalMap`在调用set()，get()，remove()方法的时候，会清理掉key为null的Entry。这样就能避免内存泄露。



**弱引用介绍：**

> 如果一个对象只具有弱引用，那就类似于**可有可无的生活用品**。弱引用与软引用的区别在于：只具有弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它 所管辖的内存区域的过程中，一旦发现了==只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存==。不过，由于垃圾回收器是一个优先级很低的线程， 因此不一定会很快发现那些只具有弱引用的对象。
>
> 弱引用可以和一个引用队列（ReferenceQueue）联合使用，如果弱引用所引用的对象被垃圾回收，Java 虚拟机就会把这个弱引用加入到与之关联的引用队列中。







---

---

---







# 源码

## `ThreadLocal.set()`方法 

![img](https://snailclimb.gitee.io/javaguide/docs/java/multi-thread/images/thread-local/6.png)

主要是判断`ThreadLocalMap`是否存在，然后使用`ThreadLocal`中的`set`方法进行数据处理。

```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}

void createMap(Thread t, T firstValue) {
    t.threadLocals = new ThreadLocalMap(this, firstValue);
}
```





---





## Hash算法

```java
int i = key.threadLocalHashCode & (len-1);
```

`ThreadLocalMap`中`hash`算法很简单，这里`i`就是当前 key 在散列表中对应的数组下标位置。

`ThreadLocal`中有一个属性为`HASH_INCREMENT = 0x61c88647` ,以及`nextHashCode`。每当创建一个`ThreadLocal`对象，这个`ThreadLocal.nextHashCode` 这个值就会增长 `HASH_INCREMENT` 。

这样的算法会使得hash分布十分均匀。



### 哈希冲突

向后查找，直到找到可以插入的点，然后插入。





---







## `ThreadLocalMap.set()`方法 

1. 通过哈希计算得到的槽位为空，直接插入。
2. 通过哈希计算得到的槽位数据不为空：
   1. 如果key一致，替换。
   2. 如果key不一致，向后查找：
      1. 先查找到空槽位或key相等的槽位，直接插入/替换。
      2. 先查到key为null的槽位，执行`replaceStaleEntry()`方法(替换过期数据的逻辑)，从**当前位置向前迭代查找**，到了头部就转到数组最后的index，直到==查到的Entry为空==就停止。~初始有一个变量slotToExpunge为当前下标，每次查到一个key为null的，就更新slotToExpunge为查的下标~。然后还需要在当前位置**往后再查找一次**。如果找到key相等的元素就替换，没有找到(第一次遇到null)就插入到当前key值为null的位置。~~替换完成后也是进行过期元素清理工作，清理工作主要是有两个方法：`expungeStaleEntry()`和`cleanSomeSlots()`~~。

