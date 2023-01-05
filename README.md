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
      enable: true #此配置用来开启声明式加锁功能
```
##### 2.使用

###### 1.编程式使用

只需DI LockLayer 到类中即可使用

```java
@Autowired
private LockLayer redisLockLayer;
```

2.声明式使用

```java
@Component
public class Lock {

    @LockLayer(key = "test_key")
    public void lock(){

    }
  
		#当方法里面抛异常lack layer会自动释放锁
    @LockLayer(key = "test_key")
    public void lockException(){
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

