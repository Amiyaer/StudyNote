全称为`compareAndSet`，比较并设置值。此方法的内部是原子的。

`v.compareAndSet(prev,next)`，要修改v，比较v和prev的值。相等就修改成next，不相等就不进行修改。



## 工作原理

在修改值的时候，将==当前线程中的值和实际对象中的最新值==进行比较(相当于是抢夺资源)。如果两个值不一致，说明别的线程已经将其修改过了，因此这一次修改不进行，返回false。如果一致(抢到了)，才进行修改。

CAS的底层是`lock cmpxchg`指令，在单核CPU和多核CPU下都能够保证原子性。



CAS的操作需要volatile，volatile能够让线程看到当前的最新值。



结合CAS和volatile可以实现无锁并发，适用于线程数少，多核CPU的场景下。

* CAS是基于乐观锁的思想。
* synchronized是基于悲观锁的思想，防着其他线程来修改共享变量。
* **CAS体现的是无锁并发、==无阻塞并发==，能够提升效率**。
* 但是如果竞争激烈，重试的发生必然会频繁，效率反而会收到影响(抢很多次还是抢不过别人)。

