## Java基础

### java泛型

​	提供了编译时类型安全检测机制，该机制允许程序员在编译时检测到非法的类型。泛型的本质是参数化类型，也就是说所操作的==数据类型被指定为一个参数==。

**类型擦除**：Java在编译期间，所有泛型信息都会被擦掉。

三种使用方式：泛型类，泛型接口，泛型方法。

#### 1.泛型类

```java
//此处T可以随便写为任意标识，常见的如T、E、K、V等形式的参数常用于表示泛型
//在实例化泛型类时，必须指定T的具体类型
public class Generic<T> {

    private T key;

    public Generic(T key) {
        this.key = key;
    }

    public T getKey() {
        return key;
    }
}

//实例化一个泛型类：类<具体类型> 对象名 = new 类<具体类型>();
Generic<Integer> genericInteger = new Generic<Integer>(123456);
```

#### 2.泛型接口

泛型接口的实现可以指定类型也可以不指定类型

```java
class GeneratorImpl<T> implements Generator<T>{
    @Override
    public T method() {
        return null;
    }
}

class GeneratorImpl implements Generator<String>{
    @Override
    public String method() {
        return "hello";
    }
}
```

#### 3.泛型方法

```java
public static <E> void printArray(E[] inputArray) {
    for (E element : inputArray) {
        System.out.printf("%s", element);
    }
    System.out.println();
}
//调用：参数决定类型。
```

常用的通配符：T，E，K，V，？

T：type，表示具体的一个Java类型。

K  V：key  value，分别代表java键值中的key  value。

E：element，代表Element。

？：表示不确定的java类型。

---





### ==和equals的区别

对于基本数据类型，==比较的是值。对于引用数据类型，比较的是对象的内存地址。

equals() 作用于不能用于判断基本数据类型的变量，只能判断**两个对象是否相等**。



**equals()的两种情况**：

​		类没有覆盖equals()方法：等价于通过==比较这两个对象。

​		类覆盖了equals()方法：自己实现。

```java
public class test1 {
    public static void main(String[] args) {
        String a = new String("ab"); // a 为一个引用
        String b = new String("ab"); // b为另一个引用,对象的内容一样
        String aa = "ab"; // 放在常量池中
        String bb = "ab"; // 从常量池中查找
        if (aa == bb) // true，String 中的 equals 方法是被重写过的，因为 Object 的 equals 方法是比较的对象的内存地址，而 String 的 equals 方法比较的是对象的值。
            System.out.println("aa==bb");
        if (a == b) // false，非同一对象
            System.out.println("a==b");
        if (a.equals(b)) // true
            System.out.println("aEQb");
        if (42 == 42.0) { // true
            System.out.println("true");
        }
    }
}
```

当创建 `String` 类型的对象时，虚拟机会在常量池中查找有没有已经存在的值和要创建的值相同的对象，如果有就把它赋给当前引用。如果没有就在常量池中重新创建一个 `String` 对象。

---





### hashCode()与equals()

hashCode()介绍：作用是获取哈希码，返回一个int整数。哈希码用来确定该对象在哈希表中的索引位置。

该方法定义在jdk的object类中，所以Java的任何类都包含有该函数。<span style="color:red">object的哈希码是本地方法，将对象的内存地址转换为整数之后返回</span>。

1.为什么要有hashCode？

HashSet添加元素时查重就是检查hashCode是否相同(减少equals的次数)。



？

2.为什么重写equals时必须重写hashCode方法？

两个对象有相同的hashCode值，它们也不一定是相等的。

> `hashCode()`的默认行为是对堆上的对象产生独特值。如果没有重写 `hashCode()`，则该 class 的两个对象无论如何都不会相等（即使这两个对象指向相同的数据）



3.为什么两个对象有相同的hashCode也不一定相等？

hashCode()所使用的哈希算法可能会让多个对象刚好传回相同的哈希值。越糟糕的哈希算法越容易碰撞。**这时候就需要使用equals来判断两个拥有相同hashCode的对象是否真的相等**。

---





### 基本类型

| 基本类型                           | 位数 | 字节 | 默认值  |
| ---------------------------------- | ---- | ---- | ------- |
| `int`                              | 32   | 4    | 0       |
| `short`                            | 16   | 2    | 0       |
| `long`(使用时一定要加上L)          | 64   | 8    | 0L      |
| `byte`                             | 8    | 1    | 0       |
| `char`                             | 16   | 2    | 'u0000' |
| `float`                            | 32   | 4    | 0f      |
| `double`                           | 64   | 8    | 0d      |
| `boolean`(依赖于JVM厂商的具体实现) | 1    |      | false   |

这八种基本类型都有对应的包装类分别为：`Byte`、`Short`、`Integer`、`Long`、`Float`、`Double`、`Character`、`Boolean` 。

包装类型不赋值就是 `Null` ，而基本类型有默认值且不是 `Null`。



#### 自动装箱与拆箱

- **装箱**：将基本类型用它们对应的引用类型包装起来；
- **拆箱**：将包装类型转换为基本数据类型；

装箱就是调用了包装类的`valueOf()`方法，拆箱其实就是调用了`xxxValue()`方法。



#### 8种数据类型的包装类和常量池

Java基本类型的包装类大部分都实现了常量池技术。Byte，Short，Integer，Long默认创建了数值[-128,127]的相应类型的缓存数据。Character则是创建了数值在[0,127]范围的缓存数据，Boolean直接返回True或者False。

Integer部分缓存的源码

```java
//此方法将始终缓存-128到127（包括端点）范围内的值，并可以缓存此范围之外的其他值。
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
      return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}

private static class IntegerCache {
    static final int low = -128;
    static final int high;
    static final Integer cache[];
}
```

如果超出对应的范围仍然会去创建新的对象，缓存的范围区间的大小只是在性能和资源之间的权衡。

`Float`，`Double`并没有实现常量池技术。

```java
Integer i1 = 33;
Integer i2 = 33;
System.out.println(i1 == i2);// 输出 true

Float i11 = 333f;
Float i22 = 333f;
System.out.println(i11 == i22);// 输出 false

Double i3 = 1.2;
Double i4 = 1.2;
System.out.println(i3 == i4);// 输出 false
```

**PS：所有的整形包装类对象之间值的比较，全部使用equals方法比较。**

![img](https://img-blog.csdnimg.cn/20210422164544846.png)

---







### 方法

#### 一些问题

##### 1.在一个静态方法内调用一个非静态成员为什么是非法的？

答：静态方法属于类，在JVM进行类加载的时候就会分配内存。而非静态成员属于实例对象，只有在对象实例化之后才存在。**在类的非静态成员不存在的时候静态成员就已经存在了**，此时调用在内存中**还不存在的非静态成员**，属于非法操作。



##### 2.==为什么Java中只有值传递==？

答：Java程序设计语言总是采用按值调用。也就是说，方法得到的是所有参数值的一个**==拷贝==**。即，方法==**仅仅会对这个拷贝经行操作**==，不能修改传递给他的任何参数变量的内容。

一个方法不能修改一个基本数据类型的参数，不能让对象参数引用一个新的对象，但是可以改变一个对象参数的状态(比如一个数组的某个元素)。

```java
public static void main(String[] args) {
    int num1 = 10;
    int num2 = 20;

    swap(num1, num2);
    //交换的是num1和num2两个拷贝的值，所以他们本身不会发生改变。
    System.out.println("num1 = " + num1);
    System.out.println("num2 = " + num2);
}

public static void swap(int a, int b) {
    int temp = a;
    a = b;
    b = temp;

    System.out.println("a = " + a);
    System.out.println("b = " + b);
}
```



---





#### 方法的重载和重写

重载：发生在同一个类或父子类中，方法名必须相同，参数类型不同、个数不同、顺序不同，方法返回值和访问修饰符可以不同。

重写：发生在运行期，子类对于父类的允许访问的方法的实现过程进行重新编写。

1. 返回值类型、方法名、参数列表必须相同，抛出的异常范围小于等于父类，**访问修饰符范围大于等于父类**。
2. 如果父类方法访问修饰符为 `private/final/static` 则子类就不能重写该方法，但是被 static 修饰的方法能够被再次声明。
3. 构造方法无法被重写。
4. 子类方法的返回值类型应该比父类方法返回值类型**更小或者相等**(不是一定要相等)。`也就是说，如果父类方法的返回类型是void和基本数据类型，那么重写时就不能修改返回值类型。但如果方法的返回值是引用类型，重写时是可以返回该引用类型的子类的。`

---









### java面向对象

面向对象易于维护、易于复用和扩展，但是性能比面向过程要低。



#### 成员变量与局部变量的区别

成员变量是属于类的，而局部变量是在代码块或方法中定义的变量或是方法的参数。他们都能被final关键字修饰。

成员变量随着对象的创建而存在，局部变量随着方法的调用自动消失。

成员变量如果没有被赋初值，则会自动以类型的默认值而赋值，而局部变量不会自动赋值。(`特例：被final关键字修饰的成员变量也必须显示地赋值`)



#### 创建一个对象用什么运算符?对象实体与对象引用有何不同?

创建一个对象使用new 运算符，new 创建对象实例（对象实例在堆内存中），对象引用指向对象实例（对象引用存放在栈内存中）。

一个对象引用可以指向 0 个或 1 个对象（一根绳子可以不系气球，也可以系一个气球）;一个对象可以有 n 个引用指向它（可以用 n 条绳子系住一个气球）。



#### 三大特征

##### 1 封装

封装是指把一个对象的状态信息（也就是属性）隐藏在对象内部，不允许外部对象直接访问对象的内部信息。

##### 2 继承

继承是使用已存在的类的定义作为基础建立新类的技术，新类的定义可以**增加新的数据或新的功能**，也可以用父类的功能，但不能选择性地继承父类。通过使用继承，可以快速地创建新的类，可以提高代码的重用，程序的可维护性，节省大量创建新类的时间 ，提高我们的开发效率。

**关于继承如下 3 点请记住：**

1. 子类拥有父类对象所有的属性和方法（包括私有属性和私有方法），但是父类中的私有属性和方法子类是无法访问，**只是拥有**。
2. 子类可以拥有自己属性和方法，即子类可以对父类进行扩展。
3. 子类可以用自己的方式实现父类的方法。（以后介绍）。

##### 3 多态

示一个对象具有多种的状态。具体表现为父类的引用指向子类的实例。





#### String、StringBuffer、StringBuilder的区别是什么？String为什么是不可变的？

String类使用final关键字修饰字符数组来保存字符串，所以`String`对象是不可变的。

StringBuilder和StringBuffer都继承自AbstractStringBuilder类，也是使用字符数组保存字符串。**但是没有用final关键字进行修饰**，因此这种对象都是可变的。

```java
abstract class AbstractStringBuilder implements Appendable, CharSequence {
    /**
     * The value is used for character storage.
     */
    char[] value;

    /**
     * The count is the number of characters used.
     */
    int count;

    AbstractStringBuilder(int capacity) {
        value = new char[capacity];
    }}
```

String中的对象不可改变，也就可以理解为常量，线程安全。

StringBuffer对方法加了同步锁或者对调用的方法**加了同步锁**，所以是线程安全的。

StringBuilder则没有，因此是非线程安全的。



性能：String改变的时候都是生成一个新的String对象然后将指针指向新的String对象。后两者是对本身进行操作。因此操作少量数据宜用String，单线程大量数据宜用StringBuilder，多线程大量数据宜用StringBuffer。





#### Object类常用方法

Object 类是一个特殊的类，是所有类的父类。它主要提供了以下 11 个方法：

```java
//native方法，用于返回当前运行时对象的Class对象，使用了final关键字修饰，故不允许子类重写。
public final native Class<?> getClass()


//native方法，用于返回对象的哈希码，主要使用在哈希表中，比如JDK中的HashMap。
public native int hashCode() 


//用于比较2个对象的内存地址是否相等，String类对该方法进行了重写用户比较字符串的值是否相等。    
public boolean equals(Object obj)


//naitive方法，用于创建并返回当前对象的一份拷贝。一般情况下，对于任何对象 x，表达式 x.clone() != x 为true，x.clone().getClass() == x.getClass() 为true。Object本身没有实现Cloneable接口，所以不重写clone方法并且进行调用的话会发生CloneNotSupportedException异常。
protected native Object clone() throws CloneNotSupportedException


//返回类的名字@实例的哈希码的16进制的字符串。建议Object所有的子类都重写这个方法。
public String toString()


//native方法，并且不能重写。唤醒一个在此对象监视器上等待的线程(监视器相当于就是锁的概念)。如果有多个线程在等待只会任意唤醒一个。
public final native void notify()


//native方法，并且不能重写。跟notify一样，唯一的区别就是会唤醒在此对象监视器上等待的所有线程，而不是一个线程。
public final native void notifyAll()


//native方法，并且不能重写。暂停线程的执行。注意：sleep方法没有释放锁，而wait方法释放了锁 。timeout是等待时间。
public final native void wait(long timeout) throws InterruptedException

    
//多了nanos参数，这个参数表示额外时间（以毫微秒为单位，范围是 0-999999）。 所以超时的时间还需要加上nanos毫秒。
public final void wait(long timeout, int nanos) throws InterruptedException

   
//跟之前的2个wait方法一样，只不过该方法一直等待，没有超时时间这个概念
public final void wait() throws InterruptedException

    
//实例被垃圾回收器回收的时候触发的操作
protected void finalize() throws Throwable { }
```



---









### 反射

通过反射可以获取任意一个类的所有属性和方法，还可以调用这些方法和属性。



优点：让代码更加灵活。

缺点：增加了安全问题(可以无视泛型参数的安全检查)，性能稍差。



应用场景：SSM等框架中都大量使用了反射机制。注解的实现也使用到了反射。



JDK实现动态代理的示例代码，使用了反射类<span style="color:orange">`Method`</span>来调用指定的方法。

```java
public class DebugInvocationHandler implements InvocationHandler {
    /**
     * 代理类中的真实对象
     */
    private final Object target;

    public DebugInvocationHandler(Object target) {
        this.target = target;
    }


    //Method
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        System.out.println("before method " + method.getName());
        Object result = method.invoke(target, args);
        System.out.println("after method " + method.getName());
        return result;
    }
}
```



---







### 异常

![img](https://guide-blog-images.oss-cn-shenzhen.aliyuncs.com/2020-12/Java%E5%BC%82%E5%B8%B8%E7%B1%BB%E5%B1%82%E6%AC%A1%E7%BB%93%E6%9E%84%E5%9B%BE.png)

在 Java 中，所有的异常都有一个共同的祖先 `java.lang` 包中的 `Throwable` 类。`Throwable` 类有两个重要的子类 `Exception`（异常）和 `Error`（错误）。`Exception` 能被程序本身处理(`try-catch`)， ==`Error` 是无法处理的==(只能尽量避免)。

`Exception` 和 `Error` 二者都是 Java 异常处理的重要子类，各自都包含大量子类。

- **`Exception`** :程序本身可以处理的异常，可以通过 `catch` 来进行捕获。`Exception` 又可以分为 受检查异常(必须处理) 和 不受检查异常(可以不处理)。
- **`Error`** ：`Error` 属于程序无法处理的错误 ，我们没办法通过 `catch` 来进行捕获 。例如，Java 虚拟机运行错误（`Virtual MachineError`）、虚拟机内存不够错误(`OutOfMemoryError`)、类定义错误（`NoClassDefFoundError`）等 。这些异常发生时，==Java 虚拟机（JVM）一般会选择线程终止==。



**受检查异常**

Java在编译的过程中，这类异常没有被catch或throw的话就没办法通过编译。

除了`RuntimeException`和他的子类，其他的Exception和它们的子类都属于受检查异常。



**不受检查异常**

Java 代码在编译过程中 ，我们即使不处理不受检查异常也可以正常通过编译。

`RuntimeException` 及其子类都统称为非受检查异常，例如：`NullPointerException`、`NumberFormatException`（字符串转换为数字）、`ArrayIndexOutOfBoundsException`（数组越界）、`ClassCastException`（类型转换错误）、`ArithmeticException`（算术错误）等。



**Throwable**类常用方法

- **`public string getMessage()`**:返回异常发生时的简要描述
- **`public string toString()`**:返回异常发生时的详细信息
- **`public string getLocalizedMessage()`**:返回异常对象的本地化信息。使用 `Throwable` 的子类覆盖这个方法，可以生成本地化信息。如果子类没有覆盖该方法，则该方法返回的信息与 `getMessage（）`返回的结果相同
- **`public void printStackTrace()`**:在控制台上打印 `Throwable` 对象封装的异常信息



#### try-catch-finally

- **`try`块：** 用于捕获异常。其后可接零个或多个 `catch` 块，如果==没有 `catch` 块，则必须跟一个 `finally` 块==。
- **`catch`块：** 用于处理 try 捕获到的异常。
- **`finally` 块：** 无论是否捕获或处理异常，`finally` 块里的语句都会被执行。当在 `try` 块或 `catch` 块中遇到 `return` 语句时，**==`finally` 语句块将在方法返回之前被执行，并且finally语句中的返回值会覆盖原始语句中的返回值==**。

3种finally不执行的情况：

1. try或catch中使用了`System.exit(int)`退出程序。如果该语句在异常语句之后，finally还是会被执行。
2. 程序所在的线程死亡。
3. 关闭CPU。



#### try-catch-resources

适用范围：任何实现`java.lang.AutoCloseable`或者`java.io.Closeable`的对象。

执行顺序：在该语句中，任何catch或finally块在声明的资源关闭后运行。

优势：面对**必须要关闭的资源**，我们应该优先使用try-catch-resources而不是try-finally。

```java
        //读取文本文件的内容
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("D://read.txt"));
            while (scanner.hasNext()) {
                System.out.println(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

		//使用try-with-resources语句改造上面的代码，把需要关闭的资源(包括他的声明)放到try()括号中去，同时省去其他的手动关闭。
		//使用分号分割还能声明多个资源。
		try (Scanner scanner = new Scanner(new File("test.txt"))) {
    		while (scanner.hasNext()) {
        		System.out.println(scanner.nextLine());
    		}
		} catch (FileNotFoundException fnfe) {
    		fnfe.printStackTrace();
		}
```



---







### I/O流

**序列化**：将数据结构或对象转换成二进制字节流的过程。

**反序列化**：将在序列化过程中生成的二进制字节流转化成数据结构或者对象的过程。



序列化的主要目的是通过网络传输对象或者说是将对象存储到文件系统、数据库、内存中。

![img](https://my-blog-to-use.oss-cn-beijing.aliyuncs.com/2020-8/a478c74d-2c48-40ae-9374-87aacf05188c.png)



#### transient关键字

阻止实例中用此关键字修饰的变量序列化；当对象被反序列化时，被transient修饰的变量值不会被持久化和恢复。只能修饰变量，不能修饰类和方法。



#### 获取键盘输入

两种方法：`Scanner`和`BufferReader`。

```java
Scanner input = new Scanner(System.in);
String s  = input.nextLine();
input.close();
//////////////////////////////////////////////////////
BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
String s = input.readLine();
```



#### IO流的种类

- 按照流的流向分，可以分为输入流和输出流；
- 按照操作单元划分，可以划分为字节流和字符流；
- 按照流的角色划分为节点流和处理流。

Java Io 流共涉及 40 多个类，这些类看上去很杂乱，但实际上很有规则，而且彼此之间存在非常紧密的联系， Java I0 流的 40 多个类都是从如下 4 个抽象类基类中派生出来的。

- InputStream/Reader: 所有的输入流的基类，前者是字节输入流，后者是字符输入流。
- OutputStream/Writer: 所有输出流的基类，前者是字节输出流，后者是字符输出流。



为什么有了字节(最小单元)流还需要字符流

字符流是由Java虚拟机将字节转换得到的，这个过程==非常的耗时==，并且如果不知道编码类型就很容易出现乱码的问题。所以IO直接提供了操作字符的接口方便我们使用。



---





















## Java易错的问题

### GROUP1(equals)

p1.引用类型使用equals方法容易报错，因为引用类型可能为null。可以使用更为安全的equals(null,value)，判断前会先查看两个对象是否为空。

p2.null值可以使用==或!=来比较，但不能使用其他逻辑操作。

p3.**不能使用一个值为null的引用类型变量来调用非静态方法**，否则会抛出异常。



### GROUP2(包装类)

p1.所有整型包装类对象之间值的比较，全部使用equals()方法。



### GROUP3(BigDecimal)

问题引出：浮点数之间的等值判断，基本类型的浮点数不能用==比较，包装类的浮点数类型不能用equals()比较。

用处：定义浮点数的值。

