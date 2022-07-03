package base.thread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolExecutor extends java.util.concurrent.ThreadPoolExecutor implements RejectedExecutionHandler {

    private final AtomicInteger threadCount = new AtomicInteger(0);

    private final LinkedList<Task> rejectedTasks;

    private final LinkedList<Task> maxPriorityTask;

    private final ExecuteQueueTask executeQueueTask;

    private final String threadNamePrefix;

    private OnTaskRejectedListener onTaskRejectedListener;

    private OnAllThreadBlockListener onAllThreadBlockListener;

    private int maxRejectCount;

    private int queueLength;

    private volatile long lastTaskCompleteTime;

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
        this(corePoolSize, maximumPoolSize, maximumPoolSize);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int queueLength) {
        this(corePoolSize, maximumPoolSize, queueLength, 10000);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int queueLength, long autoCheckTime) {
        this(corePoolSize, maximumPoolSize, queueLength, autoCheckTime, "MThread-");
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, int queueLength, long autoCheckTime, String threadNamePrefix) {
        super(corePoolSize,
                maximumPoolSize,
                1,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(queueLength)
        );
        this.queueLength = queueLength;
        this.threadNamePrefix = threadNamePrefix;
        setThreadFactory((r) -> new Thread(r, threadNamePrefix + threadCount.getAndIncrement()));
        setRejectedExecutionHandler(this);
        rejectedTasks = new LinkedList<>();
        maxPriorityTask = new LinkedList<>();
        maxRejectCount = 2;
        executeQueueTask = new ExecuteQueueTask(this, autoCheckTime);
        executeQueueTask.start();
    }

    public void setOnAllThreadBlockListener(OnAllThreadBlockListener onAllThreadBlockListener) {
        this.onAllThreadBlockListener = onAllThreadBlockListener;
    }

    public void setMaxRejectCount(int maxRejectCount) {
        this.maxRejectCount = maxRejectCount;
    }

    public void setOnTaskRejectedListener(OnTaskRejectedListener onTaskRejectedListener) {
        this.onTaskRejectedListener = onTaskRejectedListener;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    @Override
    public void rejectedExecution(Runnable r, java.util.concurrent.ThreadPoolExecutor executor) {
        Task rr = (Task) r;
        rr.rejectCount++;
        final ProcessType type;
        if (rr.onTaskRejectedListener != null) {
            type = rr.onTaskRejectedListener.onTaskReject(rr.innerTask, rr.rejectCount);
        } else if (onTaskRejectedListener != null) {
            type = onTaskRejectedListener.onTaskReject(rr.innerTask, rr.rejectCount);
        } else {
            type = rr.rejectCount < maxRejectCount ? ProcessType.queue : ProcessType.dead;
        }
        if (type == ProcessType.dead)
            return;
        addToRejectedQueue(rr, type);
    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        synchronized (maxPriorityTask) {
            tasks = new ArrayList<>(maxPriorityTask);
        }
        tasks.addAll(super.shutdownNow());
        synchronized (rejectedTasks) {
            tasks.addAll(rejectedTasks);
        }
        return tasks;
    }

    @Override
    public void execute(Runnable r) {
        if (!(r instanceof Task)) {
            r = new Task(r);
        }
        lastTaskCompleteTime = now();
        super.execute(r);
    }

    public void execute(String taskName, Runnable r, OnTaskRejectedListener l) {
        Task task;
        if (r instanceof Task) {
            task = (Task) r;
            task.onTaskRejectedListener = l;
        } else {
            task = new Task(taskName, r, l);
        }
        lastTaskCompleteTime = now();
        super.execute(task);
    }
    // </editor-folder>

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        lastTaskCompleteTime = now();
        executeQueueTask.check();
        if (t != null)
            return;
        Task task = popMaxPriority();
        while (task != null) {
            task.run();
            task = popMaxPriority();
        }
    }

    private void addToRejectedQueue(Task rr, ProcessType type) {
        if (type == ProcessType.queue) {
            synchronized (rejectedTasks) {
                rejectedTasks.addLast(rr);
            }
        } else {
            synchronized (maxPriorityTask) {
                maxPriorityTask.addLast(rr);
            }
        }
    }

    private Task popMaxPriority() {
        synchronized (maxPriorityTask) {
            if (!maxPriorityTask.isEmpty())
                return maxPriorityTask.removeFirst();
            return null;
        }
    }

    private Task pop() {
        synchronized (rejectedTasks) {
            if (!rejectedTasks.isEmpty())
                return rejectedTasks.removeFirst();
            return null;
        }
    }

    public static final class Task implements Runnable {
        private final Runnable innerTask;
        private final String name;
        private int rejectCount;
        private OnTaskRejectedListener onTaskRejectedListener;

        private Task(Runnable innerTask) {
            this(null, innerTask, null);
        }

        public Task(String name, Runnable inner, OnTaskRejectedListener l) {
            this.name = name;
            this.innerTask = inner;
            this.rejectCount = 0;
            onTaskRejectedListener = l;
        }

        @Override
        public void run() {
            innerTask.run();
        }

        @Override
        public String toString() {
            return String.format("%s - %s",
                    name,
                    String.valueOf(innerTask));
        }
    }

    public static enum ProcessType {

        maxPriority,

        queue,

        dead
    }

    public static interface OnTaskRejectedListener {

        ProcessType onTaskReject(Runnable task, int times);
    }

    public static interface OnAllThreadBlockListener {
        void onThreadBlock(long time);
    }

    private static final class ExecuteQueueTask extends Thread {
        private final ThreadPoolExecutor executor;
        private final long autoCheckTime;
        private long lastCompleteCount;

        private ExecuteQueueTask(ThreadPoolExecutor executor, long autoCheckTime) {
            super("ExecuteQueueTaskThread");
            this.executor = executor;
            this.autoCheckTime = autoCheckTime;
            lastCompleteCount = -1;
        }

        @Override
        public void run() {
            try {
                while (!executor.isShutdown()) {
                    synchronized (this) {
                        if (autoCheckTime > 0)
                            wait(autoCheckTime);
                        else
                            wait();
                    }
                    if (checkIfBlockAllThread()) {
                        continue;
                    }
                    Task task = executor.pop();
                    if (task != null && !executor.isShutdown())
                        executor.execute(task);
                }
            } catch (InterruptedException ignore) {
            }
        }

        private boolean checkIfBlockAllThread() {
            long nowCompleteCount = executor.getCompletedTaskCount();
            if (lastCompleteCount == -1 || lastCompleteCount != nowCompleteCount) {
                lastCompleteCount = nowCompleteCount;
                return false;
            }
            if (executor.isShutdown())
                return false;
            if (executor.getActiveCount() < executor.getMaximumPoolSize())
                return false;
            if (executor.getQueue().size() < executor.queueLength)
                return false;
            if (executor.onAllThreadBlockListener != null)
                executor.onAllThreadBlockListener.onThreadBlock(now() - executor.lastTaskCompleteTime);
            return true;
        }

        private synchronized void check() {
            notifyAll();
        }
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
