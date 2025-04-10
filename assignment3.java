import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class assignment3 {

  static void PriorityWithPreemption(int numProcesses, int[] processNum, int[] arrivalTime, int[] cpuBurst,
      int[] priority) {
    System.out.println("PR_withPREMP");

    PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>(
        (a, b) -> {
          if (a.getKey() != b.getKey()) {
            return a.getKey() - b.getKey();
          } else {
            return processNum[a.getValue()] - processNum[b.getValue()];
          }
        });

    int currProcess = -1;
    int currTime = 0;
    boolean[] isAdded = new boolean[numProcesses];
    int[] waitingTime = new int[numProcesses];
    int[] completionTime = new int[numProcesses];
    int[] remainingBurst = new int[numProcesses];

    // ! Initialize remainingBurst with cpuBurst // numProcesses = 3 elements to
    // copy
    System.arraycopy(cpuBurst, 0, remainingBurst, 0, numProcesses);

    // * For all processes that arrive at time 0
    for (int i = 0; i < numProcesses; i++) {
      if (arrivalTime[i] == 0) {
        queue.offer(new AbstractMap.SimpleEntry<>(priority[i], i));
        isAdded[i] = true;
      }
    }

    while (!queue.isEmpty() || currProcess != -1) {
      // * If no process is currently running, get one from the queue
      if (currProcess == -1) {
        currProcess = queue.poll().getValue();
        System.out.println(currTime + " " + processNum[currProcess]);
      }

      // ! Next arrival time or completion time
      int nextEventTime = currTime + remainingBurst[currProcess];

      int nextArrival = Integer.MAX_VALUE;
      int arrivingProcess = -1;

      // * Find if there's an arrival before process completes
      for (int i = 0; i < numProcesses; i++) {
        if (!isAdded[i] && arrivalTime[i] > currTime && arrivalTime[i] < nextEventTime) {
          if (arrivalTime[i] < nextArrival) {
            nextArrival = arrivalTime[i];
            arrivingProcess = i;
          }
        }
      }

      // * If there is
      if (arrivingProcess != -1) {
        remainingBurst[currProcess] -= (nextArrival - currTime);
        currTime = nextArrival;

        // * Add the arriving process to queue
        queue.offer(new AbstractMap.SimpleEntry<>(priority[arrivingProcess], arrivingProcess));
        isAdded[arrivingProcess] = true;

        // * Check priority
        if (priority[arrivingProcess] < priority[currProcess]) {
          // ! Preempt current process and add it back to queue
          queue.offer(new AbstractMap.SimpleEntry<>(priority[currProcess], currProcess));
          currProcess = queue.poll().getValue();
          System.out.println(currTime + " " + processNum[currProcess]);
        }
      } else {
        currTime += remainingBurst[currProcess];
        completionTime[currProcess] = currTime;
        remainingBurst[currProcess] = 0;
        currProcess = -1; // Flag as no current process

        // Check for any processes that arrived exactly at this time
        for (int i = 0; i < numProcesses; i++) {
          if (!isAdded[i] && arrivalTime[i] == currTime) {
            queue.offer(new AbstractMap.SimpleEntry<>(priority[i], i));
            isAdded[i] = true;
          }
        }
      }

      // * If queue is empty and no process is running, advance time to next arrival
      if (queue.isEmpty() && currProcess == -1) {
        nextArrival = Integer.MAX_VALUE;
        for (int i = 0; i < numProcesses; i++) {
          if (!isAdded[i] && arrivalTime[i] > currTime && arrivalTime[i] < nextArrival) {
            nextArrival = arrivalTime[i];
          }
        }
        // * At the next arrival
        if (nextArrival != Integer.MAX_VALUE) {
          currTime = nextArrival;
          // * For all processes arriving at this time
          for (int i = 0; i < numProcesses; i++) {
            if (arrivalTime[i] == currTime && !isAdded[i]) {
              queue.offer(new AbstractMap.SimpleEntry<>(priority[i], i));
              isAdded[i] = true;
            }
          }
        }
      }
    }

    // * Calculate and print average waiting time
    double totalWaitingTime = 0;
    for (int i = 0; i < numProcesses; i++) {
      waitingTime[i] = completionTime[i] - arrivalTime[i] - cpuBurst[i];
      totalWaitingTime += waitingTime[i];
    }
    System.out.printf("AVG Waiting Time: %.2f\n", totalWaitingTime / numProcesses);
  }

  static void PriorityWithoutPreemption(int numProcesses, int[] processNum, int[] arrivalTime, int[] cpuBurst,
      int[] priority) {
    System.out.println("PR_noPREMP");

    // * No-preemption
    // * Take arrival times into account.
    // * Whenever a new process arrives, add it to a priority
    // * queue, in which the key is the priority value read from the input file.
    // * After Time 0, scheduling decisions are made only when the process that
    // * currently has the CPU terminates (no preemption).
    // * The scheduler will then give the CPU to the process
    // * with the highest priority (smallest priority number).
    // * Ties are broken in favor of the process with the
    // * smaller process number.

    PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>(
        (a, b) -> {
          int burst1 = a.getKey();
          int burst2 = b.getKey();

          // Get process indices
          int processIndex1 = a.getValue();
          int processIndex2 = b.getValue();

          if (burst1 == burst2) {
            // If bursts are equal, sort by process number
            return processNum[processIndex1] - processNum[processIndex2];
          } else {
            // Otherwise sort by CPU burst time
            return burst1 - burst2;
          }
        });

    int currTime = 0;
    boolean[] isAdded = new boolean[numProcesses];
    int[] waitingTime = new int[numProcesses];
    int[] completionTime = new int[numProcesses];

    // * For all processes that arrive at time 0
    for (int i = 0; i < numProcesses; i++) {
      if (arrivalTime[i] == 0) {
        queue.offer(new AbstractMap.SimpleEntry<>(priority[i], i));
        isAdded[i] = true;
      }
    }

    while (!queue.isEmpty()) {
      int currProcess = queue.poll().getValue();
      System.out.println(currTime + " " + processNum[currProcess]);

      int executionTime = cpuBurst[currProcess];
      int previousTime = currTime;
      currTime += executionTime;

      completionTime[currProcess] = currTime;

      // * Check if any process arrives during the running time
      for (int i = 0; i < numProcesses; i++) {
        if (!isAdded[i] && arrivalTime[i] > previousTime && arrivalTime[i] <= currTime) {
          queue.offer(new AbstractMap.SimpleEntry<>(priority[i], i));
          isAdded[i] = true;
        }
      }

      // * If no processes are ready, go to next arrival time
      if (queue.isEmpty()) {
        int nextArrival = Integer.MAX_VALUE;
        for (int i = 0; i < numProcesses; i++) {
          if (!isAdded[i] && arrivalTime[i] > currTime && arrivalTime[i] < nextArrival) {
            nextArrival = arrivalTime[i];
          }
        }
        if (nextArrival != Integer.MAX_VALUE) {
          currTime = nextArrival;
          // Add arriving processes
          for (int i = 0; i < numProcesses; i++) {
            if (arrivalTime[i] == currTime && !isAdded[i]) {
              queue.offer(new AbstractMap.SimpleEntry<>(priority[i], i));
              isAdded[i] = true;
            }
          }
        }
      }
    }

    // * Calculate and print average waiting time
    double totalWaitingTime = 0;
    for (int i = 0; i < numProcesses; i++) {
      waitingTime[i] = completionTime[i] - arrivalTime[i] - cpuBurst[i];
      totalWaitingTime += waitingTime[i];
    }
    System.out.printf("AVG Waiting Time: %.2f\n", totalWaitingTime / numProcesses);
  }

  static void ShortestJobFirst(int numProcesses, int[] processNum, int[] arrivalTime, int[] cpuBurst,
      int[] priority) {
    System.out.println("SJF");

    // * No-preemption
    // * Scheduler considers only processes that have arrived
    // * When a new process arrives, add to priority queue in which key is CPU burst
    // * length
    // * After time 0, decisions made only when process that currently has CPU
    // * terminates
    // * Scheduler gives CPU to process with shortest CPU burst
    // * Ties broken based on arrival time (for processes with same cpu burst and
    // * same arrival time)
    // * If multiple processes have the same CPU burst and arrival time
    // * favor process with the smaller process number

    PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>(
        (a, b) -> {
          // ! Sort by CPU Burst
          if (a.getKey() != b.getKey()) {
            return a.getKey() - b.getKey();
          }
          // ! If bursts are equal, sort by arrival time
          if (arrivalTime[a.getValue()] != arrivalTime[b.getValue()]) {
            return arrivalTime[a.getValue()] - arrivalTime[b.getValue()];
          }
          // ! If arrival times are equal, sort by process number
          return a.getValue() - b.getValue();
        });

    int currTime = 0;
    boolean[] isAdded = new boolean[numProcesses];
    int[] burst = new int[numProcesses];
    int[] waitingTime = new int[numProcesses];
    int[] completionTime = new int[numProcesses];

    System.arraycopy(cpuBurst, 0, burst, 0, numProcesses);

    // * For all processes that arrive at time 0
    for (int i = 0; i < numProcesses; i++) {
      if (arrivalTime[i] == 0) {
        queue.offer(new AbstractMap.SimpleEntry<>(cpuBurst[i], i));
        isAdded[i] = true;
      }
    }

    while (!queue.isEmpty()) {
      int currProcess = queue.poll().getValue(); // ! At Time 0, Gets process 4 (index 3)
      System.out.println(currTime + " " + processNum[currProcess]);

      int executionTime = cpuBurst[currProcess];
      int previousTime = currTime;
      currTime += executionTime;

      completionTime[currProcess] = currTime;

      // * Check if any process arrives during the time quantum
      for (int i = 0; i < numProcesses; i++) {
        if (!isAdded[i] && arrivalTime[i] > previousTime && arrivalTime[i] <= currTime) {
          queue.offer(new AbstractMap.SimpleEntry<>(cpuBurst[i], i));
          isAdded[i] = true;
        }
      }

      // * If no processes are ready, go to next arrival time
      if (queue.isEmpty()) {
        int nextArrival = Integer.MAX_VALUE;
        for (int i = 0; i < numProcesses; i++) {
          if (!isAdded[i] && arrivalTime[i] > currTime && arrivalTime[i] < nextArrival) {
            nextArrival = arrivalTime[i];
          }
        }
        if (nextArrival != Integer.MAX_VALUE) {
          currTime = nextArrival;
          // Add arriving processes
          for (int i = 0; i < numProcesses; i++) {
            if (arrivalTime[i] == currTime && !isAdded[i]) {
              queue.offer(new AbstractMap.SimpleEntry<>(cpuBurst[i], i));
              isAdded[i] = true;
            }
          }
        }
      }
    }

    // * Calculate and print average waiting time
    double totalWaitingTime = 0;
    for (int i = 0; i < numProcesses; i++) {
      waitingTime[i] = completionTime[i] - arrivalTime[i] - cpuBurst[i];
      totalWaitingTime += waitingTime[i];
    }

    System.out.printf("AVG Waiting Time: %.2f\n", totalWaitingTime / numProcesses);
  }

  static void RoundRobin(int numProcesses, int[] processNum, int timeQuantum, int[] arrivalTime, int[] cpuBurst,
      int[] priority) {

    Queue<Integer> queue = new LinkedList<>();
    int currTime = 0;
    int[] remainingBurst = new int[numProcesses];
    int[] waitingTime = new int[numProcesses];
    boolean[] isAdded = new boolean[numProcesses];

    // ! Initialize remainingBurst with cpuBurst // numProcesses = 3 elements to
    // ! copy
    System.arraycopy(cpuBurst, 0, remainingBurst, 0, numProcesses);

    // * Adding processes to queue in order of file that arrive at time 0
    for (int i = 0; i < numProcesses; i++) {
      if (arrivalTime[i] == 0) {
        queue.add(i);
        isAdded[i] = true;
      }
    }

    System.out.println("RR\t" + timeQuantum);

    // * Running processes in queue
    while (!queue.isEmpty()) {
      int currProcess = queue.poll();
      System.out.println(currTime + " " + processNum[currProcess]);

      int executionTime = Math.min(cpuBurst[currProcess], timeQuantum);
      remainingBurst[currProcess] -= executionTime;
      int previousTime = currTime;
      currTime += executionTime;

      // * Check if any process arrives during the time quantum
      for (int i = 0; i < numProcesses; i++) {
        if (!isAdded[i] && arrivalTime[i] > previousTime && arrivalTime[i] <= currTime) {
          queue.add(i);
          isAdded[i] = true;
        }
      }

      // * Add back to queue if process is not finished
      if (remainingBurst[currProcess] > 0) {
        queue.add(currProcess);
      } else {
        waitingTime[currProcess] = currTime - arrivalTime[currProcess] - cpuBurst[currProcess];
      }

      // * If no processes are ready, go to next arrival time
      if (queue.isEmpty()) {
        int nextArrival = Integer.MAX_VALUE;
        for (int i = 0; i < numProcesses; i++) {
          if (!isAdded[i] && arrivalTime[i] > currTime && arrivalTime[i] < nextArrival) {
            nextArrival = arrivalTime[i];
          }
        }
        if (nextArrival != Integer.MAX_VALUE) {
          currTime = nextArrival;
          // Add arriving processes
          for (int i = 0; i < numProcesses; i++) {
            if (arrivalTime[i] == currTime && !isAdded[i]) {
              queue.add(i);
              isAdded[i] = true;
            }
          }
        }
      }

    }

    // * Calculate and print average waiting time
    double avgWaitingTime = 0;
    for (int time : waitingTime) { // for all processes' waiting time
      avgWaitingTime += time;
    }
    avgWaitingTime /= numProcesses;
    System.out.printf("AVG Waiting Time: %.2f\n", avgWaitingTime);
  }

  static void ShortestJobFirst() {
    System.out.println("Shortest Job First");
  }

  public static void main(String[] args) {
    BufferedReader reader = null;
    int timeQuantum = -1;
    try {
      File file = new File("input.txt");
      reader = new BufferedReader(new FileReader(file));
      String[] schedAlgo = reader.readLine().trim().split("\\s+");
      String scheduler = schedAlgo[0];
      if (scheduler.equals("RR")) {
        timeQuantum = Integer.parseInt(schedAlgo[1]);
      }

      // Read second line
      int numProcesses = Integer.parseInt(reader.readLine().trim());

      int[] processNum = new int[numProcesses];
      int[] arrivalTime = new int[numProcesses];
      int[] cpuBurst = new int[numProcesses];
      int[] priority = new int[numProcesses];

      for (int i = 0; i < numProcesses; i++) {
        String[] processDetails = reader.readLine().trim().split("\\s+");
        processNum[i] = Integer.parseInt(processDetails[0]);
        arrivalTime[i] = Integer.parseInt(processDetails[1]);
        cpuBurst[i] = Integer.parseInt(processDetails[2]);
        priority[i] = Integer.parseInt(processDetails[3]);
      }

      switch (scheduler) {
        case ("RR"):
          RoundRobin(numProcesses, processNum, timeQuantum, arrivalTime, cpuBurst, priority);
          break;
        case ("SJF"):
          ShortestJobFirst(numProcesses, processNum, arrivalTime, cpuBurst, priority);
          break;
        case ("PR_noPREMP"):
          PriorityWithoutPreemption(numProcesses, processNum, arrivalTime, cpuBurst, priority);
          break;
        case ("PR_withPREMP"):
          PriorityWithPreemption(numProcesses, processNum, arrivalTime, cpuBurst, priority);
          break;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}