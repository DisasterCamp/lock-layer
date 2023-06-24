<p align="center">
    <img width="400" src="https://gitee.com/d__isaster/cornucopia/raw/master/img/lock%20layer.png">
</p>

[Chinese](https://github.com/DisasterCamp/lock-layer/blob/master/README-CH.md)

## 🔥Features
- 🚀 Out of the box
- 🍄 Automatic relocking
- 🔆 Lock retry
- ⛏️ Reentrant lock
- ⚙️ Declarative locking
- ..........（To be continued）
## 🖥 Environment Required
- redis v6.0.0+
- jdk 1.8+
- ......

## 🌎 Architecture

....（To be continued）

## ☀️ Quick Start

### 💊 Dependency
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
### 🛁 USE
#### java primitive

```java
# About Configuration 
LockManager lockManager = LockManager.create(LockConfig.build().setClient(new JedisClient(new JedisPool("127.0.0.1", 6379, null, "123456"))));
RedisLockLayerLayer redisLockLayer = new RedisLockLayerLayer(lockManager);
```
```java
#Using
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
....（To be continued）
#### springboot

##### 1.Configuration

```yaml
# If the lock layer reuses the configuration of spring-redis, the normal use of spring-redis will not be affected
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
      enable: true # This configuration enables declarative locking. The default value is false
```
##### 2.Using

###### 1.Programmatic use

Just DI LockLayer into the class to use

```java
@Autowired
private LockLayer redisLockLayer;
```

###### 2.Declarative use

```java
@Component
public class Lock {

    @LockLayer(key = "test_key", expireTime = 100)
    public void lock() {

    }

    @LockLayer(key = "test_key", expireTime = 100, reentryLock = true)
    public void retryLock() {

    }

    //When an exception is thrown in a annotated method, the lock layer automatically releases the lock
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
##  💐 Configuration

If the lock layer is not dynamically configured, the default lock layer is used

```yaml
#Global Settings, it should be noted that this yml file name must be lock-layer-extend.yml, if it is any other file name, the lock layer will not be able to load its configuration
lock:
  layer:
    max_expire_count: 3 # The maximum number of consecutive locks was set
    max_retry_time: 30000 # The maximum lock retry time, after which the lock fails
    max_expire_time : 60 # Maximum renewal time, which is the same as max_expire_count
    max_reentry_count: 3 # The number of reentrants allowed
    renew:
      type: redis # Whether to retry locks in pub/sub mode. Thread pool is used by default
    log:
      enable: true # Enable lock layer logs. This function is disabled by default
```
##  🧽 Extend Interface
The lock layer provides two interfaces, LockHeatProcessor and LockProcessor, to allow developers to expand operations when locks are successfully added, fail to be added, and when the lock duration times out

```java
//LockHeatProcessor Indicates the interface for subsequent operations performed when the lock timeout occurs
@Service
public class LockHeatProcessorImpl implements LockHeatProcessor {
    @Override
    public void lockHeartRemovedProcessor(LockHeartBeatEntity value) {
        System.out.println(value.getExpireCount());
    }
}
```

```java
//LockProcessor Indicates the extension interface for subsequent operations when a lock succeeds or fails
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

