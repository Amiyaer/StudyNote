## 主从模式

节点分为主节点和从节点。

主从模式有以下特点：

* slave挂了，重新启动后会将数据从master同步过来。

* master挂了以后，不影响slave的读，但redis不再提供写服务，master重启后redis将重新对外提供写服务。

* master挂了以后，**不会**在slave节点中重新选一个master。



**工作机制**：

1. 从节点启动后主动向主节点发送SYNC命令。
2. 主节点接受到SYNC命令后在后台保存==**快照RDB**==，并缓存保存快照这段时间的命令(通过一个repl_backlog缓冲区)，然后将保存的快照文件和缓存的命令发送给slave(==全量同步==)。
3. 从节点接收到快照文件和命令后加载快照文件和缓存的执行命令。
4. 主节点每次写入都同步发送给从节点，保证主从一致性(==增量同步==)。



**缺点**：如果主节点挂掉，不会有从节点替代它，redis暂时无法对外提供写服务。不具备高可用性。repl_backlog类似于redolog，如果环形写满就会继续覆盖，也就是说如果主节点和从节点差的太多主节点会把部分落后的数据覆盖，这样只能执行全量同步。



**操作**：(一主二从)创建三个文件夹，每个文件夹里粘贴一份redis的配置文件(修改端口号和IP)，开启三个会话启动redis实例。对从库使用slaveof [ip 端口]或者直接修改配置文件，永久当从库。



**减少主节点压力**：不要让所有的从节点slaveof主节点，可以slaveof一部分从节点，变成从从节点。



## sentinel模式

建立在主从模式的基础上。

sentinel：用来监听一个集群，自己本身不参与数据的读写。负责新的主节点的选举，并及时把新的主节点通知给客户端(Java客户端等)。**==一个redis集群一般由多个sentinel监听==**。

当主节点挂了以后，sentinel会再**从节点中选择一个节点作为主节点**，并修改他们的配置文件。其他从节点的配置文件也会被修改，比如slaveof会指向新的主节点。

挂掉的主节点重新启动后，不再是主节点了，而是从节点。



**工作机制**：

每个sentinel以每秒钟一次的频率向它所知的主节点和从节点，以及其他sentinel实例发送一个PING命令。

某一个实例回复PING命令的时间太长(超过`down-after-milliseconds`)，这个实例会被标记为**==主观下线==**。

监视主观下线的实例的sentinel要以每秒一次的频率确认它的确下线了。有足够数量的确定时，**主观下线的实例会被标记为客观下线**。

~~在一般情况下， 每个sentinel会以每 10 秒一次的频率向它已知的所有节点发送 INFO 命令。当主节点被标记为客观下线时，向它的所有从节点的发送命令的频率会从10秒一次改成1秒一次~~。

若没有足够数量(quorum)的sentinel同意某个主节点节点已下线，它的客观下线模式就会被解除。如果它重新向sentinel的ping命令返回有效回复，那么它的主观下线状态就会被移除。



**选举机制**：

* 首先判断slave节点与主节点断开时间的长短，超过指定值(down-after-milliseconds*10)的slave会被直接排除。
* 判断slave节点的slave-priority值，越小优先级越高，如果是0表示不参与选举。
* ==判断slave的offset，越大说明数据越新，优先级越高==。
* offset相同，id越小优先级越高。



**故障转移过程**：

* sentinel给新的主节点发送`slaveof no one`。
* sentinel让其他从节点`slaveof`新的主节点
* sentinel把故障节点标记为slave。



**搭建**：

* 主从模式的配置(略)

* 创建三个(多个)sentinel文件夹，分别在每个目录下创建sentinel.conf文件。

  * ```ini
    port 27001
    sentinel annouce-ip 192.168.71.7
    sentinel monitor mymaster 192.168.71.7 7001 2
    sentinel down-after-milliseconds mymaster 5000
    sentinel failover-timeout mymaster 60000
    dir "/tmp/s1"
    ```

  * `sentinel monitor [集群命名] [主节点ip和端口] [quorum]`。

* 运行各个目录中的配置文件`redis-sentinel s*/sentinel.conf`。



## Cluster模式

当数据量过大到一台服务器存放不下的情况时，主从模式或sentinel模式就不能满足需求了，这个时候需要对存储的数据进行**分片**，将数据存储到多个Redis实例中。cluster模式的出现就是为了解决**单机Redis容量有限的问题**，将Redis的数据根据一定的规则分配到多台机器。



特点：

* 所有的节点都是一主一从（也可以是一主多从），其中从不提供服务，仅作为备用。
* 集群中有多个master，每个master保存不同的数据。redis会把每一个master节点映射到0~16383共16384个插槽上。**==数据不与节点绑定，而是与插槽绑定==**。redis会根据key的有效部分计算插槽值
* master之间**通过ping监控彼此的健康状态**，类似于哨兵，分片集群不再需要哨兵机制了。
* 不支持同时处理多个key（如MSET/MGET），因为redis需要把key均匀分布在各个节点上，  并发量很高的情况下同时创建key-value会降低性能并导致不可预测的行为。
* 客户端可以连接任何一个主节点进行读写，即使数据不在当前分片，最终也会被转发到正确的分片上。
* 将同一类数据固定的保存在同一个redis实例：让这一类数据拥有**==相同的有效部分==**，如都以`{typeId}`为前缀。



**搭建**

* 在`/tmp`下创建六个文件夹(一主一从)，7001，7002，7003，8001，8002，8003。

* 在`/tmp`下创建一个新的配置文件`redis.conf`。

  * ```ini
    port 7001
    # 开启集群
    cluster-enabled yes
    # 集群的配置文件名称，不需要自己创建，由redis自己维护
    cluster-config-file /tmp/7001/nodes.conf
    # 节点心跳失败的超时时间
    cluster-node-timeout 5000
    # 持久化文件存放目录
    dir /tmp/7001
    # 绑定地址
    bind 0.0.0.0
    # 让redis后台执行
    daemonize yes
    # 注册的实例ip
    replica-announce-ip 192.168.71.7
    # 保护模式
    protected-mode no
    # 数据库数量
    database 1
    # 日志
    logfile /tmp/7001/run.log
    ```

* 把创建的配置文件分别复制到7001、7002、7003、8001、8002、8003各个文件夹下，并修改相应的端口号。

* 分别启动所有redis实例。

* 创建集群：`redis-cli --cluster create --cluster-replicas 1 [所有redis的ip:port]`，主节点写在前面，redis会根据传入的数字(1，表示一从)分配主节点和从节点。确认redis的配置，就完成了集群的搭建。



**集群伸缩**：指redis集群中节点的添加和移除。

添加节点：`redis-cli --cluster add-node [ip:port] [ip:port]`。后者为集群中已经存在的一个IP:端口。

重新分配插槽：`redis-cli --cluster reshard [ip:port]`，ip:port为使用的节点，随后需要选择移动数量，目标节点和源节点。
