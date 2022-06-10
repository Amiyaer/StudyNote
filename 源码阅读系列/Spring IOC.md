## 一、spring-context包：IOC的容器上下文

### **1.ApplicationContext接口**

```java
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver
```

**==最顶层的接口==**，通过继承BeanFactory接口的方法，定义了与BeanFactory的关联绑定，以及其他功能组件，如Environment，MessageSource等的关联。

**实际的bean容器为内部绑定的BeanFactory**。由BeanFactory来存放bean的元数据beanDefinitions，具体存放在BeanFactory的实现类的一个类型为ConcurrentHashMap的map中，其中key为beanName，value为BeanDefinition；以及bean实例的创建。

### 2.ConfigurableApplicationContext接口

```java
public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle, Closeable
```

继承于ApplicationContext接口，提供与applicationListener，environment，beanFactoryProcessor等相关的get/set方法，还有<font color=blue>**启动入口方法refresh**</font>。

### 3.AbstractApplicationContext抽象类

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext
```

refresh方法：容器启动的骨架实现，使用了模板设计模式。提供对ConfigurableApplicationContext接口的refresh方法的模板实现，即定义了ApplicationContext的启动步骤，但是不提供具体每步的实现，由子类提供。
成员变量定义：定义了applicationListener，environment，beanFactoryProcessor等相关的成员变量。

### 4. AbstractRefreshableApplicationContext抽象类

### ......





## BeanFactory体系结构

在Spring框架内部设计当中，ApplicationContext是Spring容器所管理、维护的beans对象的一个运行环境，即ApplicationContext包含一些功能组件：保存外部属性文件（properties文件，yml文件等）的**属性键值对集合的Environment**，**容器配置的位置contextConfigLocation**等等，用于创建bean对象需要的一些外部依赖。

BeanFactory是顶层接口，主要提供getBean方法，从该BeanFactory获取给定beanName，对应的bean对象实例；

### DefaultListableBeanFactory

BeanFactory接口体系的默认实现类，实现以上接口的功能，**==提供BeanDefinition的存储map，Bean对象对象的存储map==**。

其中Bean对象实例的存储map，定义在FactoryBeanRegistrySupport，FactoryBeanRegistrySupport实现了SingletonBeanRegistry接口，而DefaultListableBeanFactory的基类AbstractBeanFactory，继承于FactoryBeanRegistrySupport。


Bean的获取：在BeanFactory的接口继承体系中，主要是提供获取bean，如getBean；列举bean，如ListableBeanFactory；提供BeanFactory创建bean需要的组件的ConfigurableBeanFactory；以及对bean注入其他beans的AutowireCapableBeanFactory。



### bean的注册

#### 1.BeanDefinition的注册

* 提供BeanDefinition注册功能的是BeanDefinitionRegistry接口，在这个接口定义注册beanDefinition到BeanFactory的方法声明。
* BeanFactory的实现类会实现BeanDefinitionRegistry，并实现BeanDefinitionRegistry接口的registerBeanDefinition系列方法来将给定的BeanDefinition注册到BeanFactory中。

#### 2.Bean对象实例的注册

* 在SingletonBeanRegistry接口的实现类中提供存储的map和注册方法，BeanFactory实现SingletonBeanRegistry接口。