<p align="center">
    <img width="400" src="https://gitee.com/d__isaster/cornucopia/raw/master/img/lock%20layer.png">
</p>


## 🔥特性（Features）
- 🚀 开箱即用
- 🍄 自动续锁
- 🔆 锁重试
- ⛏️ 可重入锁
- ⚙️ 声明式加锁
- ..........（待续）
## 🖥 环境要求 （Environment Required）
- redis v6.0.0+
- jdk 1.8+
- ......

## 🌎 整体架构 （Architecture）

....（待续）

## ☀️ 快速开始（Quick Start）

### 💊 依赖 (Dependency)
#### java、spring
```java 
<dependency>
    <groupId>io.github.disaster1-tesk</groupId>
    <artifactId>lock-layer-core</artifactId>
    <version>1.1.0</version>
</dependency>
```
#### springboot
```java 
<dependency>
    <groupId>io.github.disaster1-tesk</groupId>
    <artifactId>lock-layer-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```
### 🛁 使用
#### java原生

```java
#配置相关
LockManager lockManager = LockManager.create(LockConfig.build().setClient(new JedisClient(new JedisPool("127.0.0.1", 6379, null, "123456"))));
RedisLockLayerLayer redisLockLayer = new RedisLockLayerLayer(lockManager);
```
```java
#使用
Thread thread = new Thread(() -> {
            redisLockLayer.tryLock("test_key");
            try {
                Thread.sleep(10000);
                redisLockLayer.unLock("test_key");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "thread1");
        Thread thread1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            redisLockLayer.tryLock("test_key");
        }, "thread2");
        thread.start();
        thread1.start();
        while (true) {

        }
```

#### spring
....（待续）
#### springboot

##### 1.配置

```yaml
#lock layer只是复用了spring-redis的配置并不会影响其spring-redis 的正常使用
spring:
  redis:
    port: 6379
    host: localhost
    password: 123456
    jedis:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
    timeout: 2000

lock:
  layer:
    declaration:
      enable: true #此配置用来开启声明式加锁功能，默认为false
```
##### 2.使用

###### 1.编程式使用

只需DI LockLayer 到类中即可使用

```java
@Autowired
private LockLayer redisLockLayer;
```

###### 2.声明式使用

```java
@Component
public class Lock {

    @LockLayer(key = "test_key", expireTime = 100)
    public void lock() {

    }

    @LockLayer(key = "test_key", expireTime = 100, reentryLock = true)
    public void retryLock() {

    }

 #当注解标注的方法中抛异常，lock layer 会自动释放锁
    @LockLayer(key = "test_key", expireTime = 10)
    public void lockException() {
        throw new RuntimeException();
    }
}

```

```java
@SpringBootApplication
public class LockLayerApplication {
    @Autowired
    private LockLayer redisLockLayer;
    @Autowired
    private Lock lock;

    public static void main(String[] args) {
        SpringApplication.run(LockLayerApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner1(){
        return args -> {
            lock.lock();
            lock.lockException();
        };
    }

    @Bean
    public ApplicationRunner applicationRunner(){
        return args -> {
            Thread thread = new Thread(() -> {
                redisLockLayer.tryLock("test_key");
                try {
                    Thread.sleep(10000);
                    redisLockLayer.unLock("test_key");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "thread1");
            Thread thread1 = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                redisLockLayer.tryLock("test_key");
            }, "thread2");
            thread.start();
            thread1.start();
        };
    }
}
```
##  💐 配置 （Configuration）

如果不进行动态配置则会使用lock layer默认的配置

```yaml
#全局设置，需要注意的是，此yml文件名必须是lock-layer-extend.yml，如果是其他文件名，lock layer将无法加载其配置
lock:
  layer:
    max_expire_count: 3 #配置最大续锁的次数
    max_retry_time: 30000 #最大锁重试时间，超过此时间则会锁失败
    max_expire_time : 60 #最大续锁时间，此配置与max_expire_count共同
    max_reentry_count: 3 #可重入次数
    renew:
      type: redis #是否以pub/sub模式重试锁定。默认情况下使用线程池
    log:
      enable: true #开启lock layer日志,默认不开启
```
##  🧽 扩展接口 （Extend Api）
lock layer 提供LockHeatProcessor、LockProcessor两个接口让开发人员可以在加锁成功、失败、锁续时超时时进行扩展操作

```java
#LockHeatProcessor接口作用锁续时超时时进行的后续操作扩展接口
@Service
public class LockHeatProcessorImpl implements LockHeatProcessor {
    @Override
    public void lockHeartRemovedProcessor(LockHeartBeatEntity value) {
        System.out.println(value.getExpireCount());
    }
}
```

```java
#LockProcessor接口作用加锁成功、失败时的后续操作扩展接口
@Service
public class LockProcessorImpl implements LockProcessor {
    @Override
    public void failLockProcessor(LockEntity lockEntity) {
        System.out.println("lock failure"+lockEntity.get_key());
    }

    @Override
    public void successLockProcessor(LockEntity lockEntity) {
        System.out.println("lock success"+lockEntity.get_key());
    }
}
```

