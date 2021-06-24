## volatile关键字







## Atomic原子类

简单来说就是具有原子特征或原子操作特征的类。

J.U.C的原子类都存放在`java.util.concurrent.atomic`下。

### JUC包中的原子类

有以下4类

<span style="background:aqua;color:black;padding:5px;border-radius:7px">**基本类型**</span> 

使用原子的方式更新基本类型

- `AtomicInteger`：整形原子类
- `AtomicLong`：长整型原子类
- `AtomicBoolean`：布尔型原子类

<span style="background:aqua;color:black;padding:5px;border-radius:7px">**数组类型**</span> 

使用原子的方式更新数组里的某个元素

- `AtomicIntegerArray`：整形数组原子类
- `AtomicLongArray`：长整形数组原子类
- `AtomicReferenceArray`：引用类型数组原子类

<span style="background:aqua;color:black;padding:5px;border-radius:7px">**引用类型**</span> 

- `AtomicReference`：引用类型原子类
- `AtomicStampedReference`：原子更新带有版本号的引用类型。该类将整数值与引用关联起来，可用于解决原子的更新数据和数据的版本号，可以解决使用 CAS 进行原子更新时可能出现的 ABA 问题。
- `AtomicMarkableReference` ：原子更新带有标记位的引用类型

<span style="background:aqua;color:black;padding:5px;border-radius:7px">**对象的属性修改类型**</span> 

- `AtomicIntegerFieldUpdater`：原子更新整形字段的更新器
- `AtomicLongFieldUpdater`：原子更新长整形字段的更新器
- `AtomicReferenceFieldUpdater`：原子更新引用类型字段的更新器



### `AtomicInteger`的使用

```java
public final int get() //获取当前的值
public final int getAndSet(int newValue)//获取当前的值，并设置新的值
public final int getAndIncrement()//获取当前的值，并自增
public final int getAndDecrement() //获取当前的值，并自减
public final int getAndAdd(int delta) //获取当前的值，并加上预期的值
boolean compareAndSet(int expect, int update) //如果输入的数值等于预期值，则以原子方式将该值设置为输入值（update）
public final void lazySet(int newValue)//最终设置为newValue,使用 lazySet 设置之后可能导致其他线程在之后的一小段时间内还是可以读到旧的值。
```

使用`AtomicInteger`类后，不用对`increment()`方法加锁也能保证线程的安全。



### `AtomicInteger`类的线程安全原理

部分源码如下：

```java
// setup to use Unsafe.compareAndSwapInt for updates（更新操作时提供“比较并替换”的作用）
private static final Unsafe unsafe = Unsafe.getUnsafe();
private static final long valueOffset;

static {
    try {
        valueOffset = unsafe.objectFieldOffset
            (AtomicInteger.class.getDeclaredField("value"));
    } catch (Exception ex) {
        throw new Error(ex); 
    }
}

private volatile int value;
```

该类主要利用CAS(compare and swap)+volatile 和 native 方法来保证原子操作，从而避免synchronized的高开销，提升效率。

CAS：拿期望的值和原本的一个值作比较，如果相同则更新成新的值。

`unsafe.objectFieldOffset`方法是一个本地方法，用来拿到“原来的值”的内存地址。value是一个volatile变量，在内存中可见，因此JVM能保证在任何时刻，任何线程总能拿到该变量的最新值，从而保证线程安全。

---







## AQS

全称称为`AbstractQueuedSynchronizer`，在`java.util.concurrent.locks`包下。

是一个用来==构建锁和同步器==的框架，使用AQS能简单且高效地构造出应用广泛地大量的同步器，比如 `ReentrantLock`，`Semaphore`，其他的诸如 `ReentrantReadWriteLock`，`SynchronousQueue`，`FutureTask` 等等皆是基于 AQS 的。

### AQS原理

核心思想：线程对共享资源发出请求，如果被请求的共享资源空闲，则==将当前发出请求的线程设置为有效工作线程，并将共享资源设定为锁定状态==；如果被请求的共享资源被占用，就需要一套线程阻塞等待以及被唤醒时锁分配的机制，这个机制AQS是用CLH队列锁实现的：==将暂时获取不到锁的线程加入到队列中==。

> CLH队列是一个虚拟的双向队列(通过结点之间的关联关系维持)。AQS将每条请求共享资源的线程封装成一个CLH锁队列的一个结点来实现锁的分配。

![AQS原理图](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2019-6/AQS%E5%8E%9F%E7%90%86%E5%9B%BE.png)

AQS使用一个int成员变量`state`来表示同步状态，通过内置的FIFO队列来完成获取资源线程的排队工作。AQS使用CAS+volatile对该同步状态进行原子操作实现对其值的修改。

```java
//返回同步状态的当前值
protected final int getState() {
    return state;
}
//设置同步状态的值
protected final void setState(int newState) {
    state = newState;
}
//原子地（CAS操作）将同步状态值设置为给定值update如果当前同步状态的值等于expect（期望值）
protected final boolean compareAndSetState(int expect, int update) {
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```





### AQS对资源的共享方式

独占Exclusive：只有一个线程能够执行。如`ReentrantLock`。

共享Share：多个线程可同时执行，如`CountDownLatch`、`Semaphore`、 `CyclicBarrier`、`ReadWriteLock`。

`ReentrantReadWriteLock` 可以看成是组合式，因为 `ReentrantReadWriteLock` 也就是读写锁允许多个线程同时对某一资源进行读。

不同的自定义同步器争用共享资源的方式也不同。自定义同步器在实现时只需要实现共享资源 state 的获取与释放方式即可，至于具体线程等待队列的维护（如获取资源失败入队/唤醒出队等），AQS 已经在顶层实现好了。





### ~~AQS的设计模式~~

~~AQS底层使用了模板方法模式：①使用者继承`AbstractQueuedSynchronizer`并重写指定的方法；②AQS组合在自定义同步组件的实现中，并调用其模板方法。~~















