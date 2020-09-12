package com;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Connection;
import java.util.Properties;

/**
 * @ProjectName: mybatisStudy
 * @Package: com
 * @ClassName: Inteceptor
 * @Description:
 * @Author: tbf
 * @CreateDate: 2020-09-12 11:05
 * @UpdateUser: Administrator
 * @UpdateDate: 2020-09-12 11:05
 * @UpdateRemark:
 * @Version: 1.0
 *
 * Statement、PreparedStatement和CallableStatement的一些区别
 */
@Intercepts(@Signature(type = StatementHandler.class,
            method = "prepare",
          args = {Connection.class, Integer.class} ))
public class Inteceptor implements Interceptor {
  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    invocation.proceed();
    //MetaObject
    return null;
  }

  @Override
  public Object plugin(Object target) {
    return null;
  }

  @Override
  public void setProperties(Properties properties) {

  }
}
