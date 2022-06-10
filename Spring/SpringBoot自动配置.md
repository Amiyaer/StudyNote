SpringBoot给我们配置好了所有web开发的常见场景

默认的包结构：主程序所在包及其下面的所有子包里面的组件都会被默认扫描进来。

## 底层注解

`@Configuration`：告诉springboot这是一个配置类。

`@Conditional`：条件装配，满足Conditional指定的条件，则进行组件注入。该组件有很多派生的组件，比如`@ConditionalOnBean`。

`@ImportResource`：导入以前Spring的配置文件让其生效。

`@ConfigurationProperties`：配置绑定，可以把当前类与配置文件中设定的信息进行绑定，从容器中获取该类的对象时，可以获得绑定的值(需要`@Component`加入到容器中)。



## 自动配置原理

** `主要注解：@SpringBootApplication` **：内部包含`@SpringBootConfiguration`、`@EnableAutoConfiguration`、`ComponentScan`三个注解。

`@EnableAutoConfiguration`：是实现自动装配的核心注解。这个注解中含有`@AutoConfigurationPackage`，能够给容器中导入一系列组件。同时还引入了`AutoConfigurationImportSelector`，这个类能够==**加载自动装配类**==。



### AutoConfigurationImportSelector:加载自动装配类

`AutoConfigurationImportSelector` 类实现了 `ImportSelector`接口，也就实现了这个接口中的 `selectImports`方法，该方法主要用于**获取所有符合条件的类的全限定类名，这些类需要被加载到 IOC 容器中**。

```java
private static final String[] NO_IMPORTS = new String[0];

public String[] selectImports(AnnotationMetadata annotationMetadata) {
        // <1>.判断自动装配开关是否打开
        if (!this.isEnabled(annotationMetadata)) {
            return NO_IMPORTS;
        } else {
          //<2>.获取所有需要装配的bean
            AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
            AutoConfigurationImportSelector.AutoConfigurationEntry autoConfigurationEntry = this.getAutoConfigurationEntry(autoConfigurationMetadata, annotationMetadata);
            return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
        }
    }
```

** `getAutoConfigurationEntry()`**方法：主要负责加载自动配置类。

方法流程：

* 判断自动装配开关是否打开，默认打开，可在配置文件中配置。
* 获取`EnableAutoConfiguration`注解中的 `exclude` 和 `excludeName`。
* 获取需要自动装配的所有配置类，读取`META-INF/spring.factories`。这里所有Spring Boot Starter下的该文件都会被读取到。`spring.factories`中，并不是所有的配置都要全部加载。这一步有经历了一遍筛选，`@ConditionalOnXXX` 中的所有条件都满足，该类才会生效。

