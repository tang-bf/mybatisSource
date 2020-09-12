/**
 *    Copyright ${license.git.copyrightYears} the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class CachingExecutor implements Executor {

  private final Executor delegate;
  private final TransactionalCacheManager tcm = new TransactionalCacheManager();

  public CachingExecutor(Executor delegate) {
    this.delegate = delegate;
    delegate.setExecutorWrapper(this);
  }

  @Override
  public Transaction getTransaction() {
    return delegate.getTransaction();
  }

  @Override
  public void close(boolean forceRollback) {
    try {
      //issues #499, #524 and #573
      if (forceRollback) {
        tcm.rollback();
      } else {
        tcm.commit();
      }
    } finally {
      delegate.close(forceRollback);
    }
  }

  @Override
  public boolean isClosed() {
    return delegate.isClosed();
  }

  @Override
  public int update(MappedStatement ms, Object parameterObject) throws SQLException {
    flushCacheIfRequired(ms);
    return delegate.update(ms, parameterObject);
  }
  //getBoundSql 如果是${} 参数在这里映射的  #{}不会处理
  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    //确定缓存的key
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

  @Override
  public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
    flushCacheIfRequired(ms);
    return delegate.queryCursor(ms, parameter, rowBounds);
  }


  //MappedStatement 全局共享
  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
      throws SQLException {
    //二级缓存的Cache
    //开启二级缓存后
//    synchronzedCache
//      delegate  loggingcache
//    if (log.isDebugEnabled()) { spring 4和mybatis整合后
//      log.debug("Cache Hit Ratio [" + getId() + "]: " + getHitRatio());
//    }
//        log slf4jimpl
//          logger log4jloggerAdapter
//      delegate serializedcache
//        delegate lrucache  LruCache使用到了LinkedHashMap
//          delegate Perpetualcache
   // java.util.logging.Logger INFO 级别
    // Spring4使用的是common-logging 原生的JCL，所以在有log4j的时候使用log4j打印日志，没有的时候使用JUL打印日志。
    //spring 5 spring-jcl，看名字就知道是spring自造的包，jcl，更是标注了，它使用的是JCL日志体系。
    //在spring5中，依然使用的是JCL，但是不是原生的，是经过改造的JCL，默认使用的是JUL，而原生JCL中默认使用的是log4j.
    //spring5+mybatis+log4j sql日志不打印  jul级别是info mybatis 又要求是debug级别
    //spring4 +mybatis+log4j 则可以打印 已在testlog项目验证
    //LogFactory
//    static {
//      //在用户没有指定日志的情况下
//      tryImplementation(LogFactory::useSlf4jLogging);//先slf4j
//      tryImplementation(LogFactory::useCommonsLogging);//jcl
//      tryImplementation(LogFactory::useLog4J2Logging);//log4j2
//      tryImplementation(LogFactory::useLog4JLogging);//log4j
//      tryImplementation(LogFactory::useJdkLogging);//jul
//      tryImplementation(LogFactory::useNoLogging);//没有日志
//    }
//    //ScheduledCache 执行增删改查的时候才会去清空 并不是向传统的那种开启后台定时线程去监测
//    @Override
//    public void putObject(Object key, Object object) {
//      clearWhenStale();
//      delegate.putObject(key, object);
//    }
    // jul 日志实现
    // log4j 日志实现
    //log4j2 中的AsyncLogger的内部使用了Disruptor框架。
    //Disruptor简介
    //Disruptor是英国外汇交易公司LMAX开发的一个高性能队列，基于Disruptor开发的系统单线程能支撑每秒600万订单。
    //
    //目前，包括Apache Strom、Log4j2在内的很多知名项目都应用了Disruptor来获取高性能。
    //Disruptor框架内部核心数据结构为RingBuffer，其为无锁环形队列。
    //
    //单线程每秒能够处理600万订单，Disruptor为什么这么快？
    //
    //a.lock-free-使用了CAS来实现线程安全
    //ArrayBlockingQueue使用锁实现并发控制，当get或put时，当前访问线程将上锁，当多生产者、多消费者的大量并发情形下，由于锁竞争、线程切换等，会有性能损失。
    //Disruptor通过CAS实现多生产者、多消费者对RingBuffer的并发访问。CAS相当于乐观锁，其性能优于Lock的性能。
    //
    //b.使用缓存行填充解决伪共享问题
    //计算机体系结构中，内存的访问速度远远低于CPU的运行速度，在内存和CPU之间，加入Cache，CPU首先访问Cache中的数据，CaChe未命中，才访问内存中的数据。
    //伪共享：Cache是以缓存行（cache line）为单位存储的，当多个线程修改互相独立的变量时，如果这些变量共享同一个缓存行，就会无意中影响彼此的性能。
    //关于伪共享的深度分析，可参考《伪共享，并发编程的性能杀手》这篇文章。
    //
    //日志输出方式
    //sync	        同步打印日志，日志输出与业务逻辑在同一线程内，当日志输出完毕，才能进行后续业务逻辑操作
    //Async Appender	异步打印日志，内部采用ArrayBlockingQueue，对每个AsyncAppender创建一个线程用于处理日志输出。
    //Async Logger	异步打印日志，采用了高性能并发框架Disruptor，创建一个线程用于处理日志输出。
    // jcl (jakart commons logging ) 抽象
    // slf4j 抽象
    // logback 实现
    // simplelog  实现
    //jdbclog 实现
    Cache cache = ms.getCache();
    if (cache != null) {
      flushCacheIfRequired(ms);
      if (ms.isUseCache() && resultHandler == null) {
        ensureNoOutParams(ms, boundSql);
        @SuppressWarnings("unchecked")
        //  从tcm中获取缓存的列表,把获取值的职责一路传递,最终到perpetualCache
        List<E> list = (List<E>) tcm.getObject(cache, key);
        if (list == null) {
          list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
          ///如果查询到数据，则调用tcm.putObject方法，往缓存中放入值;不是直接操作缓存，只是在把这次的数据和key放入待提交的Map中
          tcm.putObject(cache, key, list); // issue #578 and #116
        }
        return list;
      }
    }
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

  @Override
  public List<BatchResult> flushStatements() throws SQLException {
    return delegate.flushStatements();
  }

  @Override
  public void commit(boolean required) throws SQLException {
    delegate.commit(required);
    tcm.commit();
  }

  @Override
  public void rollback(boolean required) throws SQLException {
    try {
      delegate.rollback(required);
    } finally {
      if (required) {
        tcm.rollback();
      }
    }
  }

  private void ensureNoOutParams(MappedStatement ms, BoundSql boundSql) {
    if (ms.getStatementType() == StatementType.CALLABLE) {
      for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
        if (parameterMapping.getMode() != ParameterMode.IN) {
          throw new ExecutorException("Caching stored procedures with OUT params is not supported.  Please configure useCache=false in " + ms.getId() + " statement.");
        }
      }
    }
  }

  @Override
  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
  }

  @Override
  public boolean isCached(MappedStatement ms, CacheKey key) {
    return delegate.isCached(ms, key);
  }

  @Override
  public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
    delegate.deferLoad(ms, resultObject, property, key, targetType);
  }

  @Override
  public void clearLocalCache() {
    delegate.clearLocalCache();
  }

  private void flushCacheIfRequired(MappedStatement ms) {
    Cache cache = ms.getCache();
    if (cache != null && ms.isFlushCacheRequired()) {
      tcm.clear(cache);
    }
  }

  @Override
  public void setExecutorWrapper(Executor executor) {
    throw new UnsupportedOperationException("This method should not be called");
  }

}
