import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.*;

public class assignment3 {

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
        (a, b) -> a.getKey() - b.getKey());
    int currTime = 0;
    boolean[] isAdded = new boolean[numProcesses];
    int[] burst = new int[numProcesses];
    int[] waitingTime = new int[numProcesses];

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
      System.out.println(currTime + "\t" + processNum[currProcess]);

      int executionTime = cpuBurst[currProcess];
      int previousTime = currTime;
      currTime += executionTime;

      // * Check if any process arrives during the time quantum
      for (int i = 0; i < numProcesses; i++) {
        if (!isAdded[i] && arrivalTime[i] > previousTime && arrivalTime[i] <= currTime) {
          queue.offer(new AbstractMap.SimpleEntry<>(cpuBurst[i], i));
          isAdded[i] = true;
          waitingTime[i] += executionTime;
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
    double averageWaitTime = 0;
    for (int time: waitingTime) {
      averageWaitTime += time;
    }
    System.out.println(averageWaitTime);
  }

  static void RoundRobin(int numProcesses, int[] processNum, int timeQuantum, int[] arrivalTime, int[] cpuBurst,
      int[] priority) {

    Queue<Integer> queue = new LinkedList<>();
    int currTime = 0;
    int[] remainingBurst = new int[numProcesses];
    int[] waitingTime = new int[numProcesses];
    boolean[] isAdded = new boolean[numProcesses];

    // ! Initialize remainingBurst with cpuBurst // numProcesses = 3 elements to
    // copy
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
      System.out.println(currTime + "\t" + processNum[currProcess]);

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