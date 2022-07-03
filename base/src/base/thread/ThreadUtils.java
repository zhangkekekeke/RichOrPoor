package base.thread;


import base.util.LogUtilKt;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;


public class ThreadUtils {

    private static ExecutorService threadPoolExecutor;

    private final static Lock lock = new ReentrantLock();

    private static List<Runnable> shutdownTasks;

    static {
        createThreadPool();
    }

    private static void createThreadPool() {
        lock.lock();
        threadPoolExecutor = Executors.newCachedThreadPool();
//        threadPoolExecutor = new ThreadPoolExecutor(2, 10, 10, 5000, "IMGT-");
//        threadPoolExecutor.setOnTaskRejectedListener((task, times) -> {
//            log(String.format("on task(%s) reject %d times", task, times));
//            if (times == 1)
//                return ThreadPoolExecutor.ProcessType.queue;
//            return ThreadPoolExecutor.ProcessType.dead;
//        });
//        threadPoolExecutor.setOnAllThreadBlockListener((time) -> {
//            if (time > 10000) {
//                log(dumpAllThread(null));
//                shutdownThreadPool();
//                createThreadPool();
//            }
//        });
        if (shutdownTasks != null) {
            LogUtilKt.log(String.format("getInstance thread pool and task is not null, size is: %d", shutdownTasks.size()));
            for (Runnable r : shutdownTasks) {
                threadPoolExecutor.execute(r);
            }
            shutdownTasks = null;
        }
        lock.unlock();
    }

    private static void shutdownThreadPool() {
        lock.lock();
        if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
            shutdownTasks = threadPoolExecutor.shutdownNow();
        }
        lock.unlock();
    }

    public static void execute(Runnable task) {
        execute(null, task, null);
    }

    public static void execute(String taskName, Runnable task) {
        execute(taskName, task, null);
    }

    public static void execute(String taskName, Runnable task, ThreadPoolExecutor.OnTaskRejectedListener l) {
        lock.lock();
        if (threadPoolExecutor == null || threadPoolExecutor.isShutdown()) {
            createThreadPool();
        }
        lock.unlock();
//        threadPoolExecutor.execute(taskName, task, l);
        threadPoolExecutor.execute(task);
    }

    public static String dumpAllThread(String threadNamePattern) {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = threadGroup.getParent()) != null) {
            threadGroup = parent;
        }
        int activeThread = threadGroup.activeCount();
        Thread[] allThread = new Thread[activeThread];
        int len = threadGroup.enumerate(allThread);

        Thread current = Thread.currentThread();
        StringBuilder sb = new StringBuilder("Dump thread stack in ").append(current).append('\n');
        Pattern pattern = threadNamePattern != null ? Pattern.compile(threadNamePattern) : null;
        for (int i = 0; i < len; i++) {
            Thread t = allThread[i];
            if (t == current ||
                    (pattern != null && !pattern.matcher(t.getName()).find()))
                continue;
            sb.append(t).append('-').append(t.getState()).append(" : ");
            dumpThread(t, sb);
            sb.append('\n');
        }
        return sb.toString();
    }

    private static void dumpThread(Thread t, StringBuilder sb) {
        for (StackTraceElement e : t.getStackTrace()) {
            sb.append("\n\tat ").append(e);
        }
    }
}
