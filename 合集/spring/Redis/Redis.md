### NoSQL分类

- KV键值对
  - **Redis**
  - Oracle BDB
  - 等......
- 文档型数据库(bson和json格式一样)
  - **MongoDB**
    - 是一个基于分布式文件存储的数据库，C++编写，主要用来处理大量的文档
    - 是一个介于关系型数据库和非关系型数据库中间的产品，MongoDB是非关系型数据库中功能最丰富，最像关系型数据库的
  - ConthDB
- 列存储数据库
  - **HBase**
  - 分布式文件系统
- 图关系数据库
  - 不是存图的，放的是关系，比如：朋友圈社交网络，广告推荐等
  - **Neo4j**，infoGrid



### Redis

概述：remote dictionary server,远程字典服务

特性：支持多种数据类型，支持持久化，支持集群，支持事务，支持多语言

#### Redis打开

直接双击redis-server即可

![redis1](E:\new add juan\学习\后端spring\Redis\picture\redis1.png)

双击redis-cli启动客户端，简单的连接并存储数据

![redis2](E:\new add juan\学习\后端spring\Redis\picture\redis2.png)

**Redis推荐使用Linux开发**

#### Linux下安装Redis

##### 1.官网下载Linux版的redis安装包，通过xftps传输给centos

##### 2.把redis移动到/opt目录下解压

##### 3.进入解压后的redis文件夹，找到配置文件redis.conf

##### 4.安装gcc-c++，``yum install gcc-c++``。执行``make & make install``命令进行编译

(如果此处报一大推错，就要升级gcc的版本)

##### 5.redis的默认安装路径是/usr/local/bin，在这里面创建一个redis的配置文件夹，把之前的配置文件复制过来

##### 6.进入配置文件修改配置

![](E:\new add juan\学习\后端spring\Redis\picture\redis3.png)

##### 7.配置完成，启动redis

###### 7.1启动redis

``redis-server redisConfig/redis.conf``

其实就是两个文件的位置

###### 7.2进入redis(连接)

``redis-cli -p 6379``

进入后可以使用命令进行存储

###### 7.3关闭redis

在7.2的基础上，输入``shutdown``



#### redis工具

redis-benchmark



#### redis的基础知识

- redis默认有16个数据库，默认使用第0个数据库。可以使用``select n``进行数据库的切换。
- 查看所有的key：`keys *`
- 清空全部：`flushall`
- 清空当前数据库：`flushdb`
- redis是单线程的，redis基于内存操作，CPU并不是redis的性能瓶颈，redis的瓶颈是根据机器的内存和网络带宽。redis是c语言写的，官方提供的数据为100000+的QPS，完全不比memecache差。
- redis是将所有的数据全部放在内存中的，所以说使用单线程去操作效率就是最高的。多线程CPU上下文会切换，非常耗时。而对于内存系统来说，没有上下文切换，效率就是最高的。



#### redis的基本命令

Redis是一个开源(BSD许可)的，内存中的数据结构存储系统，它可以用作数据库、缓存和消息中间件。它支持多种类型的数据结构，如字符串(strings)，散列(hashes)，列表(lists)，集合(sets)，有序集合(sorted sets )与范围查询，bitmaps，hyperloglogs和地理空间(geospatial)索引半径查询。Redis内置了复制(replication) , LUA脚本(Lua scripting)，LRU驱动事件( LRU eviction)，事务( transactions )和不同级别的磁盘持久化( persistence)，并通过Redis哨兵( Sentinel)和自动分区( Cluster )提供高可用性( high ailbility)。

##### redis的key-value

判断是否存在某个key：EXISTS  key值，存在返回1，不存在返回0

~~移除当前key：move  key值  1  (一般不用)~~

设置过期时间：EXPIRE  key值  秒数

查看剩余过期时间：ttl  key值

查看key类型：type  key值

##### redis的五大基本数据类型

###### string字符串

redis可以使用很多字符串方法

- 追加字符串(不存在时就创建一个字符串)：APPEND  字符串类型key值  "具体的字符串"

- 截取长度：strlen  key值

- 增长/减少：incr/decr  key值

- 增长/减少多个值：INCRBY/DECRBY  key值  n

- 截取字符串：GETRANGE  key值  起始长度  终止长度，终止长度为-1表示查找全部字符串。

- 替换字符串的部分：SETRANGE  key值  起始位置  替换字符，有几个字符就替换几个位置上的。

- 设置过期时间：setex  key值  秒数  value值

- 先判断是否存在，不存在再设置：setnx  key值  value值

- 设置多个值：mset(nx)  key1值  value1值  key2值  value2值  ...

- 获取多个值：mget  key1  key2  ...

- getset  key值  value值：存在就get原来的值并设置新的值，不存在就先设置值并返回nil。


**设置一个对象(json字符串)**

​		1--set  对象:序号{json}

​		2--mset  对象:序号:属性1  值1  对象:序号:属性2  值2

String类似的场景：value除了可以是字符串还可以是数字。

###### List列表

**在redis里面，我们可以把list用成栈、队列、阻塞队列。**

- 向list中插入值(从左边开始push)：LPUSH  list名  value值
- 向list中插入值(从右边开始push)：RPUSH  list名  value值
- 获取list中特定范围内的值(从左边开始获取)：LRANGE  list名  开始值  结束值
- 从list中移除值(从左边/右边)：LPOP/RPOP  list名
- 通过下标获取list中的某一个值(从左边/右边)：LINDEX/RINDEX  list名  下标
- 返回列表的长度：LLEN  list名
- 移除list中指定的值(从左到右移除)：lrem  list名  移除的个数  value值
- 修剪list(list发生改变，从左到右修)：Ltrim  list名  开始值  结束值
- 将mylist的最后一个值弹出，加入到newlist的头部：RPOPLPUSH mylist newlist
- 更新指定位置的值(从左边开始，这个值要存在)：lset  list名  下标  新的value值
- 在指定值前面/后面插入值：linsert  list名  before/after  指定value  插入的value

list本身是一个链表，如果移除了所有的值，空链表，就代表不存在。

###### set(集合)

set中的值是==不能重复==的

set里的操作都是s开头

- 添加值：sadd  set名  value值
- 查看所有值：smembers  set名
- 判断某一个值是否存在：sismember  set名  value值
- 获取set中元素的个数：scard  set名
- 移除指定元素：srem  set名  value值
- 随机抽出元素：srandmember  n个数
- 移动元素到另一个set：smove  原set  新set  要移动的value
- 查看set与另一个的不同元素：sdiff  set名  另一个set名
- 查看set与另一个的交集：sinter  set名  另一个set名
- 查看set与另一个的并集：sunion  set名  另一个set名

###### Hash(哈希)

map集合，key-map。

hash是一个string类型的==field==和value的映射表，hash特别适合用于存储对象。
Set就是一种简化的Hash,只变动key,而value使用默认值填充。可以将一个Hash表作为一个对象进行存储，表中存放对象的信息。

| 命令                                               | 描述                                                         |
| -------------------------------------------------- | ------------------------------------------------------------ |
| **HSET** key field value                           | 将哈希表 key 中的字段 field 的值设为 value 。重复设置同一个field会覆盖,返回0 |
| **HMSET** key field1 value1 [field2 value2..]      | 同时将多个 field-value (域-值)对设置到哈希表 key 中。        |
| **HSETNX** key field value                         | 只有在字段 field 不存在时，设置哈希表字段的值。              |
| **HEXISTS** key field                              | 查看哈希表 key 中，指定的字段是否存在。                      |
| **HGET** key field                                 | 获取存储在哈希表中指定字段的值                               |
| **HMGET** key field1 [field2..]                    | 获取所有给定字段的值                                         |
| **HGETALL** key                                    | 获取在哈希表key 的所有字段和值                               |
| **HKEYS** key                                      | 获取哈希表key中所有的字段                                    |
| **HLEN** key                                       | 获取哈希表中字段的数量                                       |
| **HVALS** key                                      | 获取哈希表中所有值                                           |
| **HDEL** key field1 [field2..]                     | 删除哈希表key中一个/多个field字段                            |
| **HINCRBY** key field n                            | 为哈希表 key 中的指定字段的整数值加上增量n，并返回增量后结果 一样只适用于整数型字段 |
| **HINCRBYFLOAT** key field n                       | 为哈希表 key 中的指定字段的浮点数值加上增量 n。              |
| **HSCAN** key cursor [MATCH pattern] [COUNT count] | 迭代哈希表中的键值对。                                       |

Hash变更的数据user name age，尤其是用户信息之类的，经常变动的信息！Hash更适合于**对象**的存储，Sring更加适合**字符**串存储！

###### Zset

不同的是每个元素都会关联一个double类型的==分数==（score）。redis正是通过分数来为集合中的成员进行从小到大的排序。
如果score相同，则按字典顺序排序
有序集合的成员是唯一的,但分数(score)却可以重复。

| 命令                                            | 描述                                                         |
| ----------------------------------------------- | ------------------------------------------------------------ |
| ZADD key score member1 [score2 member2]         | 向有序集合添加一个或多个成员，或者更新已存在成员的分数       |
| ZCARD key                                       | 获取有序集合的成员数                                         |
| ZCOUNT key min max                              | 计算在有序集合中指定区间score的成员数                        |
| ZINCRBY key n member                            | 有序集合中对指定成员的分数加上增量 n                         |
| ZSCORE key member                               | 返回有序集中，成员的分数值                                   |
| ZRANK key member                                | 返回有序集合中指定成员的索引                                 |
| ZRANGE key start end                            | 通过索引区间返回有序集合成指定区间内的成员                   |
| ZRANGEBYLEX key min max                         | 通过字典区间返回有序集合的成员                               |
| ==ZRANGEBYSCORE== key min max                   | 通过分数返回有序集合指定区间内的成员==-inf 和 +inf分别表示最小最大值，只支持开区间()==\|\|**从最小值到最大值的范围内排序** |
| ZRANGEBYSCORE key min max withscores            | 返回成员的同时附带score值                                    |
| ZLEXCOUNT key min max                           | 在有序集合中计算指定字典区间内成员数量                       |
| ZREM key member1 [member2..]                    | 移除有序集合中一个/多个成员                                  |
| ZREMRANGEBYLEX key min max                      | 移除有序集合中给定的字典区间的**所有**成员                   |
| ZREMRANGEBYRANK key start stop                  | 移除有序集合中给定的排名区间的*所有*成员                     |
| ZREMRANGEBYSCORE key min max                    | 移除有序集合中给定的分数区间的*所有*成员                     |
| ZREVRANGE key start end                         | 返回有序集中指定区间内的成员，通过索引，分数从高到底         |
| ==ZREVRANGEBYSCORRE== key max min               | 返回有序集中指定分数区间内的成员，分数从高到低排序           |
| ZREVRANGEBYLEX key max min                      | 返回有序集中指定字典区间内的成员，按字典顺序倒序             |
| ZREVRANK key member                             | 返回有序集合中指定成员的排名，有序集成员按分数值递减(从大到小)排序 |
| ZINTERSTORE destination numkeys key1 [key2 ..]  | 计算给定的一个或多个有序集的交集并将结果集存储在新的有序集合 key 中，numkeys：表示参与运算的集合数，将score相加作为结果的score |
| ZUNIONSTORE destination numkeys key1 [key2..]   | 计算给定的一个或多个有序集的交集并将结果集存储在新的有序集合 key 中 |
| ZSCAN key cursor [MATCH pattern\] [COUNT count] | 迭代有序集合中的元素（包括元素成员和元素分值）               |

应用案例：

- set排序，存储班级成绩表，工资表排序！
- 普通消息1；重要消息 2，带权重进行判断
- 排行榜应用实现，取Top N测试

