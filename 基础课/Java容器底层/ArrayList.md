# ==ArrayList==底层原理

## 1.一些注意事项

* `ArrayList`可以放入任意类型的元素，甚至是空值。

* 由数组来实现数据存储。
* 线程不安全，但是执行效率高。



## 2.源码

### 结论

1. 维护一个`Object`类型的数组`elementData`。
   1. `transient Object[] elementData;` transient表示瞬间的，短暂的，表示该属性不会被序列化。
2. 如果创建`ArrayList`对象时使用的构造方法是无参构造。则`elementData`的容量会被初始化为0。第一次添加，`elementData`会==扩容为**10**==。之后每次扩容时扩容为原来的==1.5==倍。
3. 如果用的是指定大小的构造器，就会初始化容量为指定大小。



### 构造方法

无参构造：`this.elementData = DEFAULTCAPACITY_EMPTY_ELEMRNTDATA;` 创建了一个空数组。



### ==添加方法==

#### 1.`add()`

先调用`ensureCapacityInternal(size+1)`确认有空间来进行添加，然后再在数组中来进行添加。

```java
/**
     * 在此列表中的指定位置插入指定的元素。
     *先调用 rangeCheckForAdd 对index进行界限检查；然后调用 ensureCapacityInternal 方法保证capacity足够大；
     *再将从index开始之后的所有成员后移一个位置；将element插入index位置；最后size加1。
     */
    public void add(int index, E element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);  // Increments modCount!!
        //arraycopy()这个实现数组之间复制的方法一定要看一下，下面就用到了arraycopy()方法实现数组自己复制自己
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);
        elementData[index] = element;
        size++;
    }
```



#### 2.`ensureCapacityInternal(int minCapacity)`

首先确认当前数组是否是一个空数组，如果是一个空数组就求当前传入的参数(size+1)`minCapacity`和初始长度10的最大值。

将它传入`ensureExplicitCapacity(minCapacity)`方法。

#### 3.`ensureExplicitCapacity(int minCapacity)`

记录当前数据结构修改的次数++(防止多线程出现异常)。

判断当前传入的值(需要的长度)是否大于数组实际有的长度，如果大于，就需要扩容`grow(int minCapacity)`。

#### final.`grow(int minCapacity)`

==真正的扩容== 第一次扩容初始容量为0，因此`newCapacity=10`。第二次及以后按照1.5倍扩容。

* 记录原来的数组长度。

* 新数组的长度为原来的长度加上原来的长度右移一位(除以2)。相当于是原来的1.5倍。
* 判断前一步计算得到的容量与之前的传入的容量差是否小于0(是否是第一次扩容/不够用)
  * 是则使用传入的(10)作为容量。
  * 如果已经超过能够达到的最大容量`MAX_ARRAY_SIZE`，就会执行`newCapacity = hugeCapacity(minCapacity);`方法来比较 `minCapacity` 和 `MAX_ARRAY_SIZE`，如果 `minCapacity` 大于最大容量，则新容量则为`Integer.MAX_VALUE`，否则，新容量大小则为 `MAX_ARRAY_SIZE` 即为 `Integer.MAX_VALUE - 8`。
* 扩容，使用`Arrays.copyOf(elementData,newCapaticy);`这种方式可以保留原来数组的数据。









