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
package com;

import com.alibaba.druid.pool.DruidDataSource;
import com.mysql.jdbc.Driver;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 开始运行一直报mybaits无法找到，因为idea不会编译resource，需要在project structure加上test目录下为source root 解决
 * MyBatis中可以使用OGNL的地方有两处：
 * 动态SQL表达式中
 * ${param}参数中
 * 上面这两处地方在MyBatis中处理的时候都是使用OGNL处理的。
 * 1.<settings> 二级缓存全局默认是打开的，但是各个mapper内的是关闭的，二级缓存对事物的处理是个难点TransactionalCacheManager tcm  tcm.getObject(cache, key);
 * baseexecutor cacheexecutor (二级缓存打开时用的)
 *  可配置flushCache=" " 默认正删改会刷新查询不会刷新
 *     <!--开启二级缓存--> configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
 *     全局的默认是配置开启的
 *     <setting name="cacheEnabled" value="true"/>
 * </settings>
 * Mybatis的官方文档中可以看出已经默认开启了二级缓存
 * 在使用缓存的时候要记得序列化实体类，否则会抛出实体类未序列化异常
 * 级缓存是基于namespace级别的，在同一个Mapper下有效
 * 所有的数据都会先放在一级缓存中
 * 只有当会话提交或关闭时，才会提交到二级缓存中
 * 2.需要将映射的javapojo类实现序列化
 *           class Student implements Serializable{}
 * 3.<!--开启本Mapper的namespace下的二级缓存-->
 * <cache eviction="LRU" flushInterval="10000"/>
 * 回到顶部
 *  cache属性的简介：
 *  eviction:代表的是缓存回收策略，目前MyBatis提供以下策略。
 * (1) LRU（Least Recently Used）,最近最少使用的，最长时间不用的对象
 * (2) FIFO（First In First Out）,先进先出，按对象进入缓存的顺序来移除他们
 * (3) SOFT,软引用，移除基于垃圾回收器状态和软引用规则的对象
 * (4) WEAK,弱引用，更积极的移除基于垃圾收集器状态和弱引用规则的对象。这里采用的是LRU，
 * 移除最长时间不用的对形象
 * flushInterval:刷新间隔时间，单位为毫秒，这里配置的是100秒刷新，如果你不配置它，那么当
 * SQL被执行的时候才会去刷新缓存。
 * size:引用数目，一个正整数，代表缓存最多可以存储多少个对象，不宜设置过大。设置过大会导致内存溢出。
 * 这里配置的是1024个对象
 * readOnly:只读，意味着缓存数据只能读取而不能修改，这样设置的好处是我们可以快速读取缓存，缺点是我们没有
 * 办法修改缓存，他的默认值是false，不允许我们修改
 * 一级缓存   MappedStatement 类的 id 、分页属性、 SQL 再句、
 * 查询参数、 environmentid 作为构 cacheKey 参数
 * 场景问题：原来数据库abc三条数据，插入一条d,数据为abcd，查询，缓存则为abcd,最后提交事务失败，数据库会回滚，
 * 但是缓存不会，mybatis怎么处理的？
 * TransactionalCacheManager final Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();
 * TransactionalCache
 * //缓存对象
 *   private final Cache delegate;
 *   //是否需要清空提交空间的标识
 *   private boolean clearOnCommit;
 *   //所有待提交的缓存
 *   private final Map<Object, Object> entriesToAddOnCommit;
 *   //未命中的缓存集合，用于统计缓存命中率
 *   private final Set<Object> entriesMissedInCache;
 *
 *  ：
 *
 * Mybatis核心对象
 * SqlSession	作为MyBatis工作的主要顶层API，表示和数据库交互的会话，完成必要数据库增删改查功能
 * Executor	MyBatis执行器，是MyBatis 调度的核心，负责SQL语句的生成和查询缓存的维护
 * StatementHandler	封装了JDBC Statement操作，负责对JDBC statement 的操作，如设置参数、将Statement结果集转换成List集合
 * ParameterHandler	负责对用户传递的参数转换成JDBC Statement 所需要的参数
 * ResultSetHandler	负责将JDBC返回的ResultSet结果集对象转换成List类型的集合；
 * TypeHandler	负责java数据类型和jdbc数据类型之间的映射和转换
 * MappedStatement	MappedStatement维护了一条mapper.xml文件里面 select 、update、delete、insert节点的封装
 * SqlSource	负责根据用户传递的parameterObject，动态地生成SQL语句，将信息封装到BoundSql对象中，并返回
 * BoundSql	表示动态生成的SQL语句以及相应的参数信息
 * Configuration	MyBatis所有的配置信息都维持在Configuration对象之中
 *  Mybatis拦截器并不是每个对象里面的方法都可以被拦截的。
 *  Mybatis拦截器只能拦截Executor、ParameterHandler、StatementHandler、ResultSetHandler四个对象里面的方法。
 */
public class Test {



  public static void main(String[] args) throws Exception {
    String resource = "mybatis.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    //xml解析完成
    //其实我们mybatis初始化方法 除了XML意外 其实也可以0xml完成
//   new SqlSessionFactoryBuilder().b
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    Configuration configuration = sqlSessionFactory.getConfiguration();
    //使用者可以随时使用或者销毁缓存
    //默认sqlsession不会自动提交
    //从SqlSession对象打开开始 缓存就已经存在
    SqlSession sqlSession = sqlSessionFactory.openSession();

    //从调用者角度来讲 与数据库打交道的对象 SqlSession
    //通过动态代理 去帮我们执行SQL
    //拿到一个动态代理后的Mapper
    DemoMapper mapper = sqlSession.getMapper(DemoMapper.class);
    //TestMapper mapper1 = sqlSession.getMapper(TestMapper.class);
    Map<String,Object> map = new HashMap<>();
    //Map<String,Object> map1 = new HashMap<>();
   // map1.put("id","1");
    System.out.println(mapper.selectAll("1", "1"));
    map.put("id","1");
    //因为一级缓存 这里不会调用两次SQL
    System.out.println(mapper.selectAll("1", "1"));
    //如果有二级缓存 这里就不会调用两次SQL
    //当调用 sqlSession.close() 或者说刷新缓存方法， 或者说配置了定时清空缓存方法  都会销毁缓存
    sqlSession.close();

  }
}
