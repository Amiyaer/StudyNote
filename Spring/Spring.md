# Spring基础部分

## 重要的Spring模块

`Spring Core`：核心模块，主要提供IoC依赖注入功能的支持。

`Spring Aspects`：与AspectJ的集成提供支持。

`Spring AOP`：提供了面向切面编程实现。

`Spring Data Access/integration`：Spring Data Access/Integration 由 5 个模块组成：

- spring-jdbc : 提供了对数据库访问的抽象 JDBC。不同的数据库都有自己独立的 API 用于操作数据库，而 Java 程序只需要和 JDBC API 交互，这样就屏蔽了数据库的影响。
- spring-tx : 提供对事务的支持。
- spring-orm : 提供对 Hibernate 等 ORM 框架的支持。
- spring-oxm ： 提供对 Castor 等 OXM 框架的支持。
- spring-jms : Java 消息服务。

`Spring Web`：Spring Web 由 4 个模块组成：

- spring-web ：对 Web 功能的实现提供一些最基础的支持。
- spring-webmvc ： 提供对 Spring MVC 的实现。
- spring-websocket ： 提供了对 WebSocket 的支持，WebSocket 可以让客户端和服务端进行双向通信。
- spring-webflux ：提供对 WebFlux 的支持。WebFlux 是 Spring Framework 5.0 中引入的新的响应式框架。与 Spring MVC 不同，它不需要 Servlet API，是完全异步.

`Spring Test`：测试模块，对JUnit、TestNG等常用的测试框架支持的都比较好。



## Spring IoC

控制反转：主要思想就是将原本在程序中手动创建对象的控制权，交由Spring框架来管理。

优点：将对象之间的相互依赖关系交给IoC容器来管理，交由容器完成对象的注入，可以很大程度上简化应用的开发，把应用从复杂的依赖关系中解放出来。(**对象之间的耦合度降低；资源变得容易管理**)。

IoC容器是Spring用来实现IoC的载体实际上是一个map，map中存放的是各种对象。





## Spring AOP

(Aspect oriented programming)面向切面编程。

OOP的局限性：面向对象编程可以解决大部分的代码重复问题。但是有一些问题解决不了。比如在父类Animal中的多个方法的相同位置出现了重复的代码，OOP就解决不了。

AOP就是用来解决这类问题的。AOP将横切代码和业务逻辑代码分离。

<img src="https://mmbiz.qpic.cn/mmbiz_png/iaIdQfEric9TxWKzVAbIedB8n720icmGDzAudIhHxlaZbKSiacWPCU2BYgSHfGpYNafpqPg7RLcbZb3LKu2vvtYlUA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1" alt="图片" style="zoom:50%;" />



AOP主要用来解决：在不改变原有业务逻辑的情况下，增强横切逻辑代码，根本上解耦合，避免横切逻辑代码重复。

### AOP 为什么叫面向切面编程

**切**：指的是横切逻辑，原有业务逻辑代码不动，**==只能操作横切逻辑代码==**，所以面向横切逻辑。

**面**：横切逻辑代码往往要**==影响的是很多个方法==**，每个方法如同一个点，多个点构成一个面。这里有一个面的概念。



---



# Spring源码部分

基本的Spring容器启动的例子：`ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationfile.xml");` 

在ClassPath中寻找xml文件，根据xml文件来构建ApplicationContext。

Application在启动的过程中，会负责创建实例Bean，往各个bean中注入依赖等。



通过ApplicationContext对象来获取，而不是new一个对象。**==ApplicationContext继承自BeanFactory，但是实际上不应该被理解成是一个继承类，而是内部持有一个BeanFactory，所有的BeanFactory相关的操作实际上是委托给这个BeanFactory处理的==**。

```java
public class App {
    public static void main(String[] args) {
        // 用我们的配置文件来启动一个 ApplicationContext
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:application.xml");

        System.out.println("context 启动成功");

        // 从 context 中取出我们的 Bean，而不是用 new MessageServiceImpl() 这种方式
        MessageService messageService = context.getBean(MessageService.class);
        // 这句将输出: hello world
        System.out.println(messageService.getMessage());
    }
}
```



## BeanFactory简介

生产Bean的工厂，负责生产和管理各个bean实例。

**ApplicationContext其实就是一个BeanFactory**。它继承了ListableBeanFactory，表示通过这个接口可以获取多个Bean。它同时还继承了HierarchicalBeanFactory(我们可以起多个BeanFactory，并设置他们之间的父子关系)。

AutowireCapableBeanFactory ，用来自动装配Bean。ApplicationContext没有继承它，但是通过组合的方式，仍然可以使用它。

？ConfigurableListableBeanFactory，继承了以上三个接口，但是没有被App使用。



## Spring启动过程

### 1.ClassPathXmlApplication的构造方法

```java
// 如果已经有 ApplicationContext 并需要配置成父子关系，那么调用这个构造方法
  public ClassPathXmlApplicationContext(ApplicationContext parent) {
    super(parent);
  }
  ...
  public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
      throws BeansException {

    super(parent);
    // 根据提供的路径，处理成配置文件数组(以分号、逗号、空格、tab、换行符分割)
    setConfigLocations(configLocations);
    if (refresh) {
      refresh(); // 核心方法
    }
  }
```

### 1.5 BeanDefinition

**BeanDefinition**：实际上指的就是Bean，自己定义的各个Bean其实会转换成一个BeanDefinition存在于Spring的BeanFactory中。

BeanDefinition中保存了Bean的信息，当前Bean指向的是哪个类、是否单例等等。并提供了继承父Bean的配置信息、设置Bean的类名称(**将来通过反射获取实例**)、设置是否可以注入等配置、primary是否为true(被Spring优先选择)等。

### 2.== `refresh()` 方法==

容器的重建方法，会将原来的ApplicationContext销毁，然后再重新执行一次初始化操作。

该方法会先加锁，防止在创建容器的过程中，其他线程把容器销毁。随后会调用一系列的方法。

执行步骤：

#### 1 创建Bean容器前的准备工作，记录启动时间、校验配置文件等。

#### 2 创建Bean容器，加载并注册Bean。

这里会执行`ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();`将BeanFactory进行初始化，并进行Bean的加载和注册。

**--** `obtainFreshBeanFactory()`方法会关闭旧的BeanFactory并创建新的BeanFactory(`refreshBeanFactory()`)，然后返回。

**---** `refreshBeanFactory()`判断当前ApplicationContext是否有已经创建好的BeanFactory，有则删除。然后初始化一个 `DefaultListableBeanFactory`。设置BeanFactory的配置属性(`customizeBeanFactory`)：是否允许覆盖、是否允许循环引用。最后把Bean加载到BeanFactory中(`loadBeanDefinitions`)。

##### `loadBeanDefinitions()`方法，根据配置加载各个Bean，然后放到BeanFactory中。

该方法会通过一个XmlBeanDefinitionReader实例来加载各个Bean。实例化一个XmlBeanDefinitionReader，并用其来加载xml配置





为什么使用`DefaultListableBeanFactory`：它是`ConfigurableListableBeanFactory`唯一的实现类，而`ConfigurableListableBeanFactory`实现了BeanFactory下面一层的所有三个接口。** `DefaultListableBeanFactory`基本涵盖了BeanFactory所有的功能**。



















