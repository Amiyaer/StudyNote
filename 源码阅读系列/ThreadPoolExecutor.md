继承自`AbstractExecutorService`，也就是实现了`ExecutorService`接口。

## 字段

`ctl`：包含线程池的运行状态和线程池内有效线程的数量。使用Integer的高3位保存线程池状态，低29位保存工作线程。

`workQueue`：等待队列，当任务提交时且没有空闲的核心线程时，任务就会被放入等待队列中。

`workers`：工作线程的集合(线程池)，只有只有`mainLock`的时候才能访问。

`largestPoolSize`：目前达到过的最大线程数。

## 线程Worker

线程池中的每一个线程被封装成一个Worker对象。Worker继承了AQS和Runnable接口，本身就是一个线程。

Worker内部含有一个Thread对象和一个Runnable对象firstTask。**firstTask用来保存传入的任务(线程)**，thread是在调用构造方法时通过ThreadFactory来创建的线程。

run方法就是对当前Worker执行``runWorker``方法。同时，Worker自带lock和unLock方法。



## runWorker方法

用来执行一个任务。

`final void runWorker(Worker w) {}` 

任务就是w中的firstTask。获取到w的任务后会把w的firstTask置为null，让w==变成空闲Worker==。

循环执行任务。如果w中的任务为空，则会循环调用**getTask**方法去阻塞队列中**获取任务**。如果获取到，就开始执行任务。执行任务需要在`w.lock()`下进行，==**表示w执行任务，不应该中断线程**==，在这期间不能去获取其他的任务。

在线程池正常运行且当前线程没有被中断的情况下调用firstTask的run方法开启线程。

如果当前线程为空且队列中也没有线程，说明当前Worker是多余的直接跳出循环；执行完毕后需要销毁task并**解锁**。最后需要执行processWorker方法将当前Worker移除。



## addWorker方法

`private boolean addWorker(Runnable firstTask, boolean core) {}` 

用来创建一个新的Worker并执行，core是否为true表示创建的是否是核心线程。

首先考虑`SHUTDOWN`的情况这种情况下不会接受新提交的任务，所以在firstTask不为空的时候会返回false；如果firstTask为空，并且workQueue也为空，则返回false，因为队列中已经没有任务了，不需要再添加线程了。

使用CAS改变Worker数，改变成功就跳出死循环。然后根据Task来创建一个对象。在mainLock的环境下加入workers集合。

如果添加成功，启动线程(启动失败需要移除线程)，返回true。



## getTask方法

用来从阻塞队列中获取任务。

* 在<font color=blue>**for的死循环中**</font>。维持一个timed变量，表示**是否允许当前线程的过期回收**。

* 进行两个if判断，第一个主要是判断线程池的状态；第二个if如下：

```java
if ((wc > maximumPoolSize || (timed && timedOut)) && (wc > 1 || workQueue.isEmpty())) {
	if (compareAndDecrementWorkerCount(c))
		return null;
	continue;
}
```

这段逻辑大多数情况下是针对非核心线程。

1.判定当前线程是否符合退出的基本条件(大于最大线程数或者超时)，大于最大线程数则一定不是核心线程，如果是核心线程，超时也要退出。

2.判定当前线程池是否支持线程退出。wc大于1，说明还有其他线程，可以退出；**任务队列**为空，说明即使是最后一个线程，因为没有任务了，也可以退出。

3.符合上述两个条件则进行退出，CAS将wc线程数减一后退出，如果失败则**重复上述死循环**。

4.不符合上述条件，不退出。worker线程阻塞等待任务，会响应中断。拿到任务就直接返回这个任务。

```java
Runnable r = timed ?//不阻塞，poll的时间为keepAliveTime，到空闲时间闪人
workQueue.poll(keepAliveTime,TimeUnit.NANOSECONDS) : 
//阻塞，当前线程是核心线程且线程池不允许he'xi'x
workQueue.take();
```

==如果是核心线程且不允许销毁，就会在这里**一直阻塞**获取任务，线程池就是这样保证核心线程不被销毁的。==

==此外，核心线程的定义是，小于等于核心线程数时，剩下的线程全是核心线程==。



## processWorkerExit方法

```java
private void processWorkerExit(Worker w, boolean completedAbruptly) {}
```

将w从工作线程的set集合中移除，并统计他完成的任务数。

判断是否允许核心线程超时不被回收，根据变量min判断。min为0表示允许的核心线程数为0。任务队列不为空时，设置min=1。

若allowCoreThreadTimeOut == true，且当前线程数不大于corePoolSize，就会执行addWorker(null, false);

## execute方法

`public void execute(Runnable command);` 

用于提交一个任务。先取出ctl低29位的值，表示当前活动的线程数。以此来判断是创建新的核心线程还是放入阻塞队列。

如果核心线程数已经达到最大，且加入阻塞队列失败，就会创建一个普通线程来执行任务。如果失败则拒绝任务。

创建普通线程的时候，并没有传入任务，因为任务已经被添加到`workQueue`中了，worker执行的时候直接从队列中获取任务即可。



## 流程梳理

execute方法执行任务

→创建线程addWorker或加入队列

→addWorker创建的线程调用start()方法开启线程执行任务

→start方法会执行runTask的run()方法~(这里执行的是run方法所以不会再开启线程)~ 

→执行完后的Worker循环从Worker队列中获取任务(如果非核心线程超时或线程数超过最大线程数，返回空)

→获取的任务为null(空闲超时)，执行退出方法。











