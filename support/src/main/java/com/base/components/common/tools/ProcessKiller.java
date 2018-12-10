package com.base.components.common.tools;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationPid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ProcessKiller
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-19 19:32
 */
public class ProcessKiller {
  private static final OS OS_TYPE = OS.getOS();
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final Pattern PATTERN = Pattern.compile("^ *[a-zA-Z]+ +\\S+");

  private Set<Integer> ports = Sets.newHashSet();
  private Set<Integer> pidSet = Sets.newHashSet();
  private long waitMilliseconds;

  private ProcessKiller(long waitMilliseconds) {
    this.waitMilliseconds = waitMilliseconds;
    logger.info("ProcessKiller init, os is {}", OS_TYPE);
  }

  /**
   * kill 掉 build 时找到的进程
   */
  public void kill() {
    if (!pidSet.isEmpty() && OS_TYPE != null) {
      @SuppressWarnings("all")
      Thread daemonThread = new Thread(()->{
        //      CompletableFuture.runAsync(() -> {
        if (waitMilliseconds > 0) {
          logger.info("ProcessKiller > ready to executing kill command after {} milliseconds > port:{}, pid:{}", waitMilliseconds, ports, pidSet);
          Uninterruptibles.sleepUninterruptibly(waitMilliseconds, TimeUnit.MILLISECONDS);
        }
        int i = 0;
        for (Integer pid : pidSet) {
          try {
            Runtime.getRuntime().exec(replacePort(OS_TYPE.getKillCommand(), pid));
            i++;
          } catch (Exception ignore) {
          }
        }
        logger.warn("executing kill command > port:{}, pid:{}, {} process was killed !", ports, pidSet, i);
      });
      //守护线程
      daemonThread.setDaemon(true);
      daemonThread.start();
    }
  }

  private void startPort() {
    if (OS_TYPE == null) {
      return;
    }
    Runtime runtime = Runtime.getRuntime();
    for (Integer port : ports) {
      try {
        String[] command = replacePort(OS_TYPE.getFindCommand(), port);
        logger.info("find command > {}", StringUtils.join(command, " "));
        Process p = runtime.exec(command);
        //查找进程号
        try (InputStream inputStream = p.getInputStream()) {
          for (String line : read(inputStream)) {
            Integer pid = OS_TYPE.getReadLineFunction().apply(line, ports);
            if (pid != null && pid > 0) {
              pidSet.add(pid);
            }
          }
        }
        startLogs();
      } catch (Exception ignore) {
      }
    }
  }

  private void startLogs(){
    if (!pidSet.isEmpty()) {
      logger.info("ProcessKiller init, build with port:{}, find pid:{} !", ports, pidSet);
    } else {
      logger.info("ProcessKiller init, build with port:{}, but can not find pid !", ports);
    }
  }

  /**
   * 获取通过构建后，找到的进程数
   * @return pid count
   */
  public int getFindPidCount(){
    return pidSet.size();
  }

  private static Integer readPidInWindows(String line, Set<Integer> checkPorts) {
    Integer port = validPortInWindows(line);
    int pid = 0;
    if (checkPorts.contains(port) && StringUtils.isNotBlank(line)) {
      int offset = line.lastIndexOf(" ");
      String pidStr = line.substring(offset).trim();
      try {
        pid = Integer.parseInt(pidStr);
      } catch (Exception ignore) {
      }
    }
    return pid;
  }

  private static Integer validPortInWindows(String str) {
    Matcher matcher = PATTERN.matcher(str);
    if (matcher.find()) {
      String find = matcher.group();
      int index = find.lastIndexOf(":");
      find = find.substring(index + 1);
      try {
        return Integer.parseInt(find);
      } catch (Exception ignore) {
      }
    }
    return null;
  }

  private static Integer readPidInLinux(String line, Set<Integer> checkPorts) {
    int pid = 0;
    if (StringUtils.isNotBlank(line)) {
      try {
        String[] arr = StringUtils.split(line, " ");
        String[] portArr = StringUtils.split(arr[0], ":");
        if (portArr.length > 1) {
          if (checkPorts.contains(Integer.valueOf(portArr[1]))) {
            pid = Integer.parseInt(arr[1]);
          }
        }
      } catch (Exception ignore) {
      }
    }
    return pid;
  }

  private static Integer readPidInMac(String line, Set<Integer> checkPorts) {
    int pid = 0;
    if (StringUtils.isNotBlank(line)) {
      try {
        String[] arr = StringUtils.split(line, " ");
        if (arr.length > 8) {
          String[] portArr = StringUtils.split(arr[8], ":");
          if (portArr.length > 1) {
            if (checkPorts.contains(Integer.valueOf(portArr[1].trim()))) {
              pid = Integer.parseInt(arr[1].trim());
            }
          }
        }
      } catch (Exception ignore) {
      }
    }
    return pid;
  }

  private List<String> read(InputStream in) throws IOException {
    List<String> data = new ArrayList<>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    String line;
    while ((line = reader.readLine()) != null) {
      data.add(line);
    }
    return data;
  }

  private String[] replacePort(String[] srcArray, Integer port) {
    String[] newArray = new String[srcArray.length];
    for (int i = 0; i < srcArray.length; i++) {
      String s = srcArray[i];
      if (s.contains("%s")) {
        newArray[i] = String.format(s, port);
      } else {
        newArray[i] = s;
      }
    }
    return newArray;
  }

  /**
   * 根据 端口号 构建
   * @param killWaitMilliseconds  调用 kill 方法时等待的时间，小于等于 0 则不等待
   * @param portArray             -
   *
   * @return ProcessKiller
   */
  public static ProcessKiller buildWithPort(long killWaitMilliseconds, int... portArray) {
    ProcessKiller processKiller = new ProcessKiller(killWaitMilliseconds);
    if (portArray != null && portArray.length > 0) {
      for (int port : portArray) {
        processKiller.ports.add(port);
      }
    }
    processKiller.startPort();
    return processKiller;
  }

  /**
   * 根据 PID 构建
   * @param killWaitMilliseconds  调用 kill 方法时等待的时间，小于等于 0 则不等待
   * @param pidArray              -
   *
   * @return ProcessKiller
   */
  public static ProcessKiller buildWithPid(long killWaitMilliseconds, int... pidArray) {
    ProcessKiller processKiller = new ProcessKiller(killWaitMilliseconds);
    if (pidArray != null && pidArray.length > 0) {
      for (int pid : pidArray) {
        if (pid > 0) {
          processKiller.pidSet.add(pid);
        }
      }
    }
    processKiller.startLogs();
    return processKiller;
  }

  /**
   * 获取当前应用的 PID
   * @return PID
   */
  public static int getCurrentPid(){
    try {
      return Integer.valueOf(new ApplicationPid().toString());
    } catch (Exception ignore) {
    }
    return 0;
  }


  public enum OS {
    WIN(new String[] {"cmd", "/C", "netstat -ano | findstr %s"}, new String[] {"cmd", "/C", "taskkill /F /pid %s"},
        ProcessKiller::readPidInWindows
    ),

    LINUX(new String[] {"/bin/sh", "-c", "netstat -nlp | grep :%s | awk '{print $4,$7}' | awk -F\"/\" '{ print $1 }'"},
          new String[] {"/bin/sh", "-c", "kill -9 %s"}, ProcessKiller::readPidInLinux
    ),

    MAC(
      new String[] {"/bin/sh", "-c", "lsof -iTCP -sTCP:LISTEN -P -n |grep %s"},
      new String[] {"/bin/sh", "-c", "kill -9 %s"}, ProcessKiller::readPidInMac
    );

    private String[] findCommand;
    private String[] killCommand;
    private BiFunction<String, Set<Integer>, Integer> readLineFunction;

    OS(String[] findCommand, String[] killCommand, BiFunction<String, Set<Integer>, Integer> readLineFunction) {
      this.findCommand = findCommand;
      this.killCommand = killCommand;
      this.readLineFunction = readLineFunction;
    }

    public String[] getFindCommand() {
      return findCommand;
    }

    public String[] getKillCommand() {
      return killCommand;
    }

    public BiFunction<String, Set<Integer>, Integer> getReadLineFunction() {
      return readLineFunction;
    }

    public static OS getOS() {
      String name = System.getProperty("os.name");
      OS os = null;
      if (name != null) {
        name = name.toUpperCase();
        if (name.startsWith(WIN.toString())) {
          os = WIN;
        } else if (name.startsWith(LINUX.toString())) {
          os = LINUX;
        } else if (name.startsWith(MAC.toString())) {
          os = MAC;
        }
      }
      return os;
    }
  }
}
