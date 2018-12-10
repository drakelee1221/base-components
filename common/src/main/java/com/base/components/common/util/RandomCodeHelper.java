/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;


import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 随机数生成工具
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-19 10:28
 */
public abstract class RandomCodeHelper {

  //  private static final String DATE_PATTERN = "yyyyMMddHHmmssSSS";
  //  private static final DateTimeFormatter format = DateTimeFormat.forPattern(DATE_PATTERN);
  //  private static final String COMPANY_NAME = "uxz";
  //
  //  /** 类别长度 */
  //  private static final int TYPE_LENGTH = 4;
  //  /** 随机数长度 */
  //  private static final int RANDOM_LENGTH = 8;
  //  /** 订单号长度 */
  //  public static final int orderCodeLength =
  //      DATE_PATTERN.length() + COMPANY_NAME.length() + TYPE_LENGTH + RANDOM_LENGTH;
  //
  //  /**
  //   * 获取订单编号
  //   *
  //   * @return <p>如：uxz00002017021312483001409913335 (共32位)
  //   *         <p> uxz(平台缩写 3位)
  //   *         <p> 0000(类别 {@link #TYPE_LENGTH}位)
  //   *         <p> 20170213124830(yyyyMMddHHmmss 14位)
  //   *         <p> 01409913335(随机数 {@link #RANDOM_LENGTH}位)
  //   */
  //  public static String getOrderCode() {
  //    return getOrderCode(null);
  //  }
  //
  //  /**
  //   * 获取订单编号
  //   *
  //   * @param type
  //   *        类别，可空，长度见：{@link #TYPE_LENGTH}
  //   * @return <p>如：uxz00012017021312483001409913335 (共32位)
  //   *         <p> uxz(平台缩写 3位)
  //   *         <p> 0001(类别 {@link #TYPE_LENGTH}位)
  //   *         <p> 20170213124830(yyyyMMddHHmmss 14位)
  //   *         <p> 01409913335(随机数 {@link #RANDOM_LENGTH}位)
  //   */
  //  public static String getOrderCode(String type) {
  //    return new StringBuffer(COMPANY_NAME)
  //        .append(null == type || type.length() != TYPE_LENGTH ? "0000" : type)
  //        .append(format.print(System.currentTimeMillis())).append(random(RANDOM_LENGTH)).toString();
  //  }
  //
  //  /**
  //   * 获取没有type的订单编号
  //   *
  //   * @return <p>如：20170213124830014 (共17 + randomLength位)
  //   *         <p> 01409913335(随机数 {@link #RANDOM_LENGTH}位)
  //   */
  //  public static String getOrderCodeWithoutType(int randomLength) {
  //    return new StringBuffer()
  //        .append(format.print(System.currentTimeMillis()))
  //        .append(random(randomLength <= RANDOM_LENGTH? RANDOM_LENGTH: randomLength)).toString();
  //  }

  /**
   * 获取随机数字
   *
   * @param randomLength 长度
   *
   * @return
   */
  public static String random(int randomLength) {
    int randomInt = new SecureRandom(UUID.randomUUID().toString().getBytes()).nextInt();
    String random = String.valueOf(Math.abs(randomInt));
    return subString(random, randomLength);
  }

  private static String subString(String random, int randomLength) {
    if (random.length() >= randomLength) {
      return random.substring(0, randomLength);
    } else {
      return subString("0" + random, randomLength);
    }
  }

  //  /**
  //   * 将 COMPANY_NAME + TYPE 交换至订单号最后，<br/>
  //   * 如：
  //   * uxz00002017031714272529049135792，交换后为，2017021610271583922010946uxz0000 <br/>
  //   * 2017021610271583922010946uxz0000，交换后为，uxz00002017031714272529049135792 <br/>
  //   * 用于支付宝退款接口的 batch_no 字段
  //   * @param orderCode
  //   * @return
  //   */
  //  public static String exchangeCompanyNameAndType(String orderCode){
  //    if(orderCode != null && orderCode.length() == orderCodeLength){
  //      if(orderCode.startsWith(COMPANY_NAME)){
  //        return orderCode.substring(COMPANY_NAME.length()+TYPE_LENGTH)
  //               + orderCode.substring(0, COMPANY_NAME.length()+TYPE_LENGTH);
  //      }else{
  //        return orderCode.substring(orderCode.length() - (COMPANY_NAME.length()+TYPE_LENGTH), orderCode.length())
  //               + orderCode.substring(0, orderCode.length() - (COMPANY_NAME.length()+TYPE_LENGTH));
  //      }
  //    }
  //    return null;
  //  }


  //  public static void main(String[] args) throws Exception {
  //    System.out.println((getOrderCodeWithoutType(15) + getOrderCode() ).substring(32).length());
  //  }

  //  public static void test() throws InterruptedException, ExecutionException {
  //    int length = 20000;
  //    Set<String> set = Sets.newConcurrentHashSet();
  //    CountDownLatch cdl = new CountDownLatch(length);
  //    ExecutorService exe = Executors.newFixedThreadPool(length);
  //    List<Callable<String>> calls = Lists.newArrayList();
  //
  //    for (int i = 0; i < length; i++) {
  //      calls.add(new Callable<String>() {
  //        @Override
  //        public String call() throws Exception {
  //          String code = getOrderCode(null);
  //          cdl.countDown();
  //          return code;
  //        }
  //      });
  //    }
  //    List<Future<String>> result = exe.invokeAll(calls);
  //    cdl.await();
  //    exe.shutdown();
  //    for (Future<String> stringFuture : result) {
  //      set.add(stringFuture.get());
  //    }
  //    System.out.println(set.size());
  //  }


  /**
   * 检查缓存中的code是否相同
   *
   * @param paramCode 请求code
   * @param cacheCode 缓存中的code
   * @param errorMsg 异常信息
   */
  public static void checkCacheCode(String paramCode, String cacheCode, String errorMsg) {
    Assert.hasText(paramCode, errorMsg);
//    DebugCode debugCode = SpringContextUtil.getBean(DebugCode.class);
//    if (StringUtils.isNotBlank(debugCode.getCode()) && paramCode.equalsIgnoreCase(debugCode.getCode())) {
//      return;
//    }
    Assert.isTrue(paramCode.equalsIgnoreCase(cacheCode), errorMsg);
  }

  /**
   * 检查缓存中的code是否相同
   * @param paramCode 请求code
   * @param cacheCode 缓存中的code
   *
   * @return
   */
  public static boolean checkCacheCode(String paramCode, String cacheCode) {
//    DebugCode debugCode = SpringContextUtil.getBean(DebugCode.class);
//    if (StringUtils.isNotBlank(debugCode.getCode()) && paramCode.equalsIgnoreCase(debugCode.getCode())) {
//      return true;
//    }
    if(paramCode.equalsIgnoreCase(cacheCode)){
      return true;
    }
    return false;
  }

  public static void main(String[] args) {
    System.out.println(random(15));
  }

}
