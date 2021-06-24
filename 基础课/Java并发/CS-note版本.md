## 1 线程

三种方法：

- 实现Runnable接口(run方法，void)
- 实现Callable<T>接口(call方法，可以有返回值，返回值通过FutureTask进行封装)
- 继承Thread类(重写run方法)

无论哪种方式实现，最后都要通过Thread来调用，或者通过线程池。

```java
public static void main(String[] args) {
    MyRunnable instance = new MyRunnable();
    Thread thread = new Thread(instance);
    thread.start();
}

public static void main(String[] args) throws ExecutionException, InterruptedException {
    MyCallable mc = new MyCallable();
    FutureTask<Integer> ft = new FutureTask<>(mc);
    Thread thread = new Thread(ft);
    thread.start();
    System.out.println(ft.get());
}
```

一般实现接口会好一些，因为==java不能多继承，但是可以实现多个接口==。







## 2 基础线程机制

### Executor

管理多个异步任务的执行。

主要分为三种：

* CachedThreadPool：一个任务创建一个线程；

* FixedThreadPool：所有任务只能使用固定大小的线程；

* SingleThreadPool：相当于大小为1的FixedThreadPool。

用法

```java
//开启一个服务，然后执行线程。最后关闭
public static void main(String[] args) {
    ExecutorService executorService = Executors.newCachedThreadPool();
    for (int i = 0; i < 5; i++) {
        executorService.execute(new MyRunnable());
    }
    executorService.shutdown();
}
```



### Daemon(守护线程)

程序运行时在后台提供服务的线程。程序中只剩下守护线程的时候，进程就会结束，同时杀死守护线程。

main()是非守护线程。

设置线程为守护线程：`thread.setDaemon(true)`。



### sleep()和yield()

sleep(millisec)会休眠当前正在执行的进程，参数单位为毫秒。

yield()的调用声明了当前线程已经完成了生命周期中最重要的部分，可以切换给其他线程来执行。该方法仅仅是一个建议，不一定会起效。

---







## 3 中断

实现：调用一个线程的interrupt()方法来中断线程。如果线程处于阻塞或是等待状态，就会抛出InterruptedException，并提取结束该线程，否则不会被提前结束。

==不能中断I/O阻塞和synchronized锁阻塞==。



### interrupted()

使用interrupt()能中断线程，但不能提前结束所有线程(比如正处于无限循环的线程)。==但是interrupt方法会设置线程中的中断标记==，此时就是interrupted()方法的作用，不论是否提前结束，interrupted()方法在中断后将会返回true，此时就可以通过该方法来提前结束线程。

```java
public class InterruptExample {

    private static class MyThread2 extends Thread {
        @Override
        public void run() {
            while (!interrupted()) {
                // ..
            }
            System.out.println("Thread end");
        }
    }
}
```



### Executor的中断操作

方法由Executors.newCachedThreadPool调用，也就是ExecutorService

shutdown()方法：中断所有线程，但是等待线程执行完毕后再关闭。

shutdownNow()方法：相当于调用每个线程的interrupt()方法，中断所有的线程。

submit()方法：用来中断仅仅一个线程做的操作。会返回一个Future<?>对象，调用该对象的`cancel(true)`方法就可以中断。

---







## 4 互斥同步

两种锁机制实现多线程对共享资源的互斥访问：synchronized和ReentrantLock。



### synchronized

==JVM实现==  

用法：同步一个代码块、一个方法、一个静态方法、一个类。

```java
//①同步代码块，只作用于一个对象
synchronized (this) {
    // ...
}

//②同步一个方法，同样是只作用于同一个对象
public synchronized void func(){
    //TODO
}

//③同步一个类,两个线程作用于一个类上的不同对象，它们在同步语句上也会进行同步
synchronized (XXXX类名.class) {
    // ...
}

//④同步一个静态方法，同样会作用于整个类
public synchronized static void func(){
    //TODO
}
```



### ReentrantLock

==JDK实现==  

java.util.concurrent包中的锁。是一个类。

```java
public class LockExample {
	//新建一个ReentrantLock类
    private Lock lock = new ReentrantLock();

    public void func() {
        //上锁
        lock.lock();
        try {
            for (int i = 0; i < 10; i++) {
                System.out.print(i + " ");
            }
        } finally {
            lock.unlock(); // 确保释放锁，从而避免发生死锁。
        }
    }
}
```



### 两者的比较

两者的性能大致相同。在其他特性上有一定的区别。

<u>等待时可中断：持有锁的线程长期不释放锁的时候，其他等待的线程可以改为先处理其他事</u>。ReentrantLock支持这种中断，而synchronized不行。

<u>公平锁：多个线程等待一个锁时，是按照申请顺序来获得锁的</u>。synchronized的锁不公平，而ReentrantLock的锁默认不公平但是可以设置为公平锁。

<u>锁绑定多个条件</u>：一个ReentrantLock可以同时绑定多个条件对象。

优先使用synchronized，除非需要使用高级机制。因为使用ReentrantLock可能会因为==JDK版本不支持==而无法使用，并且使用synchronized不用担心锁的释放情况，==JVM会确保锁的释放==。

---







## 5 线程之间的协作

一些方法，调用其可以影响当前线程或其他线程。

### join()

当前线程调用==另一个线程的join()方法==，会将当前线程挂起，直到目标线程结束。



### wait()、notify()、notifyAll()

都属于Object的一部分，不属于Thread，直接调用即可。只能在同步方法或同步块中使用，否则会抛出异常。

wait()：使得当前的线程等待。使用wait()挂起的期间，挂起的线程会释放锁。

notify()：唤醒阻塞的线程(随机一个)。

notifyAll()：唤醒当前阻塞的所有进程。



### ？await()、signal()和signalAll()

作用类似于wait()，notify()，notifyAll()。不同的是可以在Condition上调用这些方法，它们可以指定等待的条件，更加灵活。==singal()能够唤醒指定的线程==，而不再是随机的线程了。

> Condition：J.U.C类库中提供的类来实现线程之间的协调。
>
> 获取Condition对象：使用Lock

```java
public class AwaitSignalExample{
    
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void before() {
        lock.lock();
        try {
            System.out.println("before");
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void after() {
        lock.lock();
        try {
            condition.await();
            System.out.println("after");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

```


---









## 6 线程状态

### 创建(NEW)

线程刚刚创建完成，尚未启动。

### 可运行(Runnable)

当前线程可以在JVM中运行。它可能处于运行状态，也可能正在等待资源调度(处理机资源等，调度完成就进入运行状态)。操作系统只能看到runnable状态，所以java一般把ready和runnable统称为runnable。

### 阻塞(Blocked)

想要访问同步代码块或方法。但其他线程占用了锁，因此处于阻塞状态。需要其他线程释放锁才能进入运行状态。

### 无限期等待(waiting)

等待其他线程显示唤醒。不同于阻塞的被动等待，当前状态是主动调用wait()进入的。

| 进入方法                                   | 退出方法                             |
| ------------------------------------------ | ------------------------------------ |
| 没有设置 Timeout 参数的 Object.wait() 方法 | Object.notify() / Object.notifyAll() |
| 没有设置 Timeout 参数的 Thread.join() 方法 | 被调用的线程执行完毕                 |
| LockSupport.park() 方法                    | LockSupport.unpark(Thread)           |

### 期限等待(timed_waiting)

无需等待其他线程的显示唤醒，在一定时间之后会被系统自动唤醒。

| 进入方法                                 | 退出方法                                        |
| ---------------------------------------- | ----------------------------------------------- |
| Thread.sleep() 方法                      | 时间结束                                        |
| 设置了 Timeout 参数的 Object.wait() 方法 | 时间结束 / Object.notify() / Object.notifyAll() |
| 设置了 Timeout 参数的 Thread.join() 方法 | 时间结束 / 被调用的线程执行完毕                 |
| LockSupport.parkNanos() 方法             | LockSupport.unpark(Thread)                      |
| LockSupport.parkUntil() 方法             | LockSupport.unpark(Thread)                      |

### 死亡(Terminated)

线程任务完成后自己结束，或产生了异常而结束。

---









## 9 线程不安全示例

多个线程对同一个共享数据进行访问但不采取同步的话，操作的结果是不一致的。

比如1000个线程不采取同步，同时对cnt执行自增操作，操作结束后它的值有可能小于1000。

---









## 10 Java内存模型TODO





## 11 线程安全





## 12 锁优化

















