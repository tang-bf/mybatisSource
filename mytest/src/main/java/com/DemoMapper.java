package com; /**
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

import java.util.List;
import java.util.Map;

/**
 *
 */
//@Alias("DemoMapper") 别名是不区分大小写的，会都给你转成小写
 // @Mapper()
public interface DemoMapper {

//    @Select()
  // arg0 arg1 arg2
  public List<Map<String,Object>>  selectAll(String name, String age);

  public void insert(String name, String age);

  default void test(){
    System.out.println(111);

  }
}
