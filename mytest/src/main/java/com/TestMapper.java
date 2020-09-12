package com;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface TestMapper {

  public void insert(String name, String age);
  public List<Map<String,Object>> selectAll(String name, String age);
}
