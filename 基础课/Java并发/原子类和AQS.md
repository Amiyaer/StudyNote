## Atomic原子类

简单来说就是具有原子特征或原子操作特征的类。==底层使用的是CAS来实现==。

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
private volatile int state;//共享变量，使用volatile修饰保证线程可见性
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





### AQS对资源的共享方式(默认非公平锁)

1.独占Exclusive：只有一个线程能够执行。如`ReentrantLock`。其中又分为公平锁(按队列顺序拿锁)和非公平锁(通过两次CAS去抢锁，没抢到要再次加入到队列中等待唤醒)。

```html
对于公平锁，先获取state状态，再判断其是否等于0。等于0则需要判断是否有线程在等待。没有线程在等待时才让线程通过CAS去抢锁。如果当前线程已经占有资源，改变state即可。
对于非公平锁，直接先CAS抢一次，如果成功就直接返回。否则就和公平锁一样进入tryAcquire方法。在方法中如果发现锁被释放(state==0)，直接去抢，而不是和公平锁一样先检查有没有线程在等待。
```

2.共享Share：多个线程可同时执行，如`CountDownLatch`、`Semaphore`、 `CyclicBarrier`、`ReadWriteLock`。

`ReentrantReadWriteLock` 可以看成是组合式，因为 `ReentrantReadWriteLock` 也就是读写锁允许多个线程同时对某一资源进行读。

不同的自定义同步器争用共享资源的方式也不同。自定义同步器在实现时只需要实现共享资源 ==state 的获取与释放方式即可==，至于具体线程等待队列的维护（如获取资源失败入队/唤醒出队等），AQS 已经在上层实现好了。





### ~~AQS的设计模式~~ 

~~AQS底层使用了模板方法模式(基于继承)：①使用者继承`AbstractQueuedSynchronizer`并重写指定的方法；②AQS组合在自定义同步组件的实现中，并调用其模板方法。~~ 

自定义同步器需要重写的模板方法，这些方法的实现必须是内部线程安全的。

```java
isHeldExclusively()//该线程是否正在独占资源。只有用到condition才需要去实现它。
tryAcquire(int)//独占方式。尝试获取资源，成功则返回true，失败则返回false。
tryRelease(int)//独占方式。尝试释放资源，成功则返回true，失败则返回false。
tryAcquireShared(int)//共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
tryReleaseShared(int)//共享方式。尝试释放资源，成功则返回true，失败则返回false。
```

一般来说，自定义同步器要么是独占方法，要么是共享方式，他们也只需实现`tryAcquire-tryRelease`、`tryAcquireShared-tryReleaseShared`中的一种即可。但 AQS 也支持自定义同步器同时实现独占和共享两种方式，如`ReentrantReadWriteLock`。



---









## Semaphore 信号量

==可以指定多个线程同时访问某个资源== 

维持了一个可获得许可证(访问共享资源)，初始化时可以表明最大访问数量。仅仅是一个数量，并没有实际的"许可证"对象。

`acquire` 方法申请许可(可以申请多个)，如果当前全部被申请完了，就会阻塞，直到有"许可证"被释放。

`release` 方法释放许可。同样可以释放多个。



两种方式：公平模式(遵循FIFO)和非公平模式(抢占式)。

构造方法，permits为许可证的数量(state)，而fair就表示是否为公平模式。

```java
   public Semaphore(int permits) {
        sync = new NonfairSync(permits);
    }

    public Semaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }
```



---









## CountDownLatch(倒计时器)

允许指定数量(count)的线程执行完毕前，主线程在一个特定的地方阻塞，直到所有线程的任务都执行完毕。

默认构造AQS的`state`值为`count`。

`countDown()` ：==减少线程任务计数==。使用了`tryReleaseShared`方法以CAS的操作来减少state，直到为0；

`await()` ：==阻塞点==。如果state不为0，就会一直阻塞。`CountDownLatch`会自选CAS判断state是否为0，如果等于0，就会释放所有的线程，`await()`之后的语句也得到执行。



### 用法

1. 某一线程在开始运行前等待 n 个线程执行完毕。将 `CountDownLatch` 的计数器初始化为 n ：`new CountDownLatch(n)`，每当一个任务线程执行完毕，就将计数器减 1 `countdownlatch.countDown()`，当计数器的值变为 0 时，在`CountDownLatch上 await()` 的线程就会被唤醒。一个典型应用场景就是启动一个服务时，主线程需要等待多个组件加载完毕，之后再继续执行。
2. 实现多个线程开始执行任务的最大并行性。注意是并行性，不是并发，强调的是多个线程在某一时刻同时开始执行。类似于赛跑，将多个线程放到起点，等待发令枪响，然后同时开跑。做法是初始化一个共享的 `CountDownLatch` 对象，将其计数器初始化为 1 ：`new CountDownLatch(1)`，多个线程在开始执行任务前首先 `coundownlatch.await()`，当主线程调用 `countDown()` 时，计数器变为 0，多个线程同时被唤醒。

```java
/**
*使用
*/
//threadCount是请求的数量
final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
countDownLatch.countDown();//表示一个请求完成
countDownLatch.await();//程序在这里阻塞，请求处理完成后才会继续执行await后面的
```



不足：`CountDownLatch`是一次性的，使用完毕后不能再次被使用。



---









## CyclicBarrier(循环栅栏)

与`CountDownLatch`非常类似，也可以实现线程间的技术等待，但是功能更加强大。



> `CountDownLatch` 的实现是基于 AQS 的，而 `CycliBarrier` 是基于 `ReentrantLock`(`ReentrantLock` 也属于 AQS 同步器)和 `Condition` 的.



主要做的事情：==让一组线程到达一个屏障时被阻塞，直到最后一个线程到达时，开门放行==。



**方法**

`CyclicBarrier(int parties)` ：构造方法，参数是拦截的线程数量。

`await()` ：用来告诉栅栏，当前线程已经到达了屏障，然后当前线程被阻塞。

```java
public CyclicBarrier(int parties) {
    this(parties, null);
}
//更高级的构造函数，线程到达屏障时会优先执行barrierAction。
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}
```



**应用场景**

可以用于多线程计算数据，最后合并计算结果的应用场景。比如我们用一个 Excel 保存了用户所有银行流水，每个 Sheet 保存一个帐户近一年的每笔银行流水，现在需要统计用户的日均银行流水，先用多线程处理每个 sheet 里的银行流水，都执行完之后，得到每个 sheet 的日均银行流水，最后，再用 barrierAction 用这些线程的计算结果，计算出整个 Excel 的日均银行流水。



调用`await()`方法时，实际上调用的是`dowait(false,0L)`方法。

```java
    // 当线程数量或者请求数量达到 count 时 await 之后的方法才会被执行。上面的示例中 count 的值就为 5。
    private int count;
    /**
     * Main barrier code, covering the various policies.
     */
    private int dowait(boolean timed, long nanos)
        throws InterruptedException, BrokenBarrierException,
               TimeoutException {
        final ReentrantLock lock = this.lock;
        // 锁住
        lock.lock();
        try {
            final Generation g = generation;

            if (g.broken)
                throw new BrokenBarrierException();

            // 如果线程中断了，抛出异常
            if (Thread.interrupted()) {
                breakBarrier();
                throw new InterruptedException();
            }
            // count减1
            int index = --count;
            // 当 count 数量减为 0 之后说明最后一个线程已经到达栅栏了，也就是达到了可以执行await 方法之后的条件
            if (index == 0) {  // tripped
                boolean ranAction = false;
                try {
                    final Runnable command = barrierCommand;
                    if (command != null)
                        command.run();
                    ranAction = true;
                    // 将 count 重置为 parties 属性的初始化值
                    // 唤醒之前等待的线程
                    // 下一波执行开始
                    nextGeneration();
                    return 0;
                } finally {
                    if (!ranAction)
                        breakBarrier();
                }
            }

            // loop until tripped, broken, interrupted, or timed out
            for (;;) {
                try {
                    if (!timed)
                        trip.await();
                    else if (nanos > 0L)
                        nanos = trip.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    if (g == generation && ! g.broken) {
                        breakBarrier();
                        throw ie;
                    } else {
                        // We're about to finish waiting even if we had not
                        // been interrupted, so this interrupt is deemed to
                        // "belong" to subsequent execution.
                        Thread.currentThread().interrupt();
                    }
                }

                if (g.broken)
                    throw new BrokenBarrierException();

                if (g != generation)
                    return index;

                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }
        } finally {
            lock.unlock();
        }
    }
```



### CyclicBarrier 和 CountDownLatch 的区别

CountDownLatch是一个计数器，只能够使用一次；而CyclicBarrier能够多次使用。

对于 `CountDownLatch` 来说，重点是“一个线程（多个线程）等待”，而其他的 N 个线程在完成“某件事情”之后，可以终止，也可以等待。而对于 `CyclicBarrier`，重点是多个线程，在任意一个线程没有完成，所有的线程都必须等待。

