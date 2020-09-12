package com;

/**
 * @ProjectName: mybatisStudy
 * @Package: PACKAGE_NAME
 * @ClassName: TestInterface
 * @Description:
 * @Author: tbf
 * @CreateDate: 2020-09-12 11:49
 * @UpdateUser: Administrator
 * @UpdateDate: 2020-09-12 11:49
 * @UpdateRemark:
 * @Version: 1.0
 */
//没有加-parameters 时 methods code下没有methodparameters  方法参数存有localvariabletable中
  //而接口是没有方法体的，所以没有局部变量表 加上之后-parameters  接口也会有这个methodparameters
  //就能拿到方法参数名  Parameter
public interface TestInterface {
  public void TestInterface(double d);
}
