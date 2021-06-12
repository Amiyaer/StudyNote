#### 1.启动器

spring-boot导入的依赖

​		`spring-boot-starter-web`:springboot场景启动器，帮我们导入了web模块正常运行所依赖的组件；

​		Spring Boot将所有功能场景抽取处理，做成一个个场景==启动器==，需要使用哪个场景就导入那个场景的==启动器==。

​		

#### 2.主程序类、主入口类

​		@SpringBootApplication：标注在某个类上，说明这个类是springboot的主配置类，运行这个类的main方法就可以启动一个springboot应用。

​		@SpringBootConfiguration：springboot的配置类；

​				标注在某个类上，表示这是一个SpringBoot的配置类；

​				@Configuration：配置类上来标注这个注解；

​						配置类------配置文件；配置类也是容器中的一个组件；@Component

​		@EnableAutoConfiguration：开启自动配置功能；

​				以前需要配置的东西，SpringBoot帮助我们自动配置；

​				@EnableAutoConfiguration告诉SpringBoot开启自动配置功能；这样自动配置才能生效；

```java
@AutoConfigurationPackage
@Import({EnableAutoConfigurationImportSelector.class})
public @interface EnableAutoConfiguration {
```

@AutoConfigurationPackage：自动配置包

​		@Import({Registrar.class})：Spring的底层注解@Import，给容器导入一个组件；导入的组件由Registrar.class将==主配置类(@SpringBootApplication标注的类)的所在包以及下面所有子包里面的所有组件==扫描到Spring容器；

@Import({EnableAutoConfigurationImportSelector.class})

​		给容器中导入组件



