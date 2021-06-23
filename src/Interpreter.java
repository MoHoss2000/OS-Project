import java.io.*;
import java.util.*;

public class Interpreter {

    private static final List<String> acceptableInstructions = Arrays.asList("print", "assign", "add",
            "writeFile", "readFile", "input");
    Stack<String> stack;
    Queue<Integer> readyQueue;
    Pair[] memory;
    final static int memSize = 500;
    final static int procSize = 100;
    final static int varStart = 20;

    public Interpreter() {
        stack = new Stack<>();
        memory = new Pair[memSize];
        readyQueue = new LinkedList<>();

        for (int i = 0; i < memory.length; i++) {
            memory[i] = new Pair();
        }
    }

    private String getVariable(int pid, String argument) {
        int varIdx = pid * procSize + varStart;
        int maxBoundary = getMaxBoundary(pid);
        for (; varIdx < maxBoundary; varIdx++) {
            if (memory[varIdx].key != null && memory[varIdx].key.equals(argument)) {
                System.out.println("Memory read at index "+varIdx+": "+memory[varIdx]);
                return memory[varIdx].val;

            }
        }
        return argument;
    }


    public void print(String argument, int pid) {
        System.out.println(getVariable(pid, argument));
    }


    public void assign(String varName, String argument, int pid) {
        int varIdx = pid * procSize + varStart;
        int maxBoundary = getMaxBoundary(pid);

        String variableValue = getVariable(pid, argument);

        for (; varIdx < maxBoundary; varIdx++) {
            if (memory[varIdx].key != null && memory[varIdx].key.equals(varName)) {
                memory[varIdx].val = variableValue;
                System.out.println("Memory write at index "+varIdx+": "+memory[varIdx]);
                return;
            }

            if (memory[varIdx].val == null) {
                memory[varIdx].key = varName;
                memory[varIdx].val = variableValue;
                System.out.println("Memory write at index "+varIdx+": "+memory[varIdx]);
                return;
            }
        }
//        if (variables.containsKey(argument)) {
//            variables.put(varName,
//            ));
//        } else {
//            variables.put(varName, argument);
//        }

    }

    public String readFile(String argument, int pid) throws IOException {
        String filePath = getVariable(pid, argument);

        filePath += ".txt";
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }

        // delete the last new line separator
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        reader.close();

        return stringBuilder.toString();
    }

    public void writeFile(String arg1, String arg2, int pid) {

//        String filePath = variables.getOrDefault(arg1, arg1);
//        String dataToWrite = variables.getOrDefault(arg2, arg2);
        String filePath = getVariable(pid, arg1);
        String dataToWrite = getVariable(pid, arg2);
        filePath += ".txt";

        try (PrintWriter out = new PrintWriter(filePath)) {
            out.println(dataToWrite);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void add(String arg1, String arg2, int pid) {
        int no1 = Integer.parseInt(getVariable(pid, arg1));
        int no2 = Integer.parseInt(getVariable(pid, arg2));

        int res = no1 + no2;
//        variables.put(arg1, Integer.toString(res));
        assign(arg1, Integer.toString(res), pid);
    }


    public void runInstruction(String instruction, int pid) {
        String[] terms = instruction.trim().split("\\s+");

        for (String term : terms) {
            stack.push(term);
        }

        String op1 = "";
        String op2 = "";

        while (!stack.isEmpty()) {
            String term = stack.pop();

            if (acceptableInstructions.contains(term)) {
                switch (term) {
                    case "print":
                        print(op1, pid);
                        op1 = "";
                        break;
                    case "assign":
                        assign(op2, op1, pid);
                        op1 = "";
                        op2 = "";
                        break;
                    case "input":
                        Scanner sc = new Scanner(System.in);
                        System.out.println("Please Enter a value:");
                        stack.push(sc.nextLine());
                        break;
                    case "readFile":
                        try {
                            stack.push(readFile(op1, pid));
                            op1 = "";
                        } catch (IOException e) {
                            System.out.println("The system cannot find the file specified");
//                            e.printStackTrace();
                        }
                        break;
                    case "writeFile":
                        writeFile(op2, op1, pid);
                        op1 = "";
                        op2 = "";
                        break;
                    case "add":
                        add(op2, op1, pid);
                        op1 = "";
                        op2 = "";
                        break;

                }
            } else {
                if (op1.isEmpty())
                    op1 = term;
                else
                    op2 = term;
            }

        }
    }

//    public void runProgram(String programFilePath, int pid) {
//        try {
//            File myObj = new File(programFilePath);
//            Scanner myReader = new Scanner(myObj);
//            while (myReader.hasNextLine()) {
//                String instruction = myReader.nextLine();
//                runInstruction(instruction, pid);
//            }
//            myReader.close();
//        } catch (FileNotFoundException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }
//    }

    public void runPrograms(String[] programNames) {

        for (int i = 0; i < programNames.length; i++) {
            loadToMemory(programNames[i], i);
            readyQueue.add(i);
        }

        scheduler();
    }

    private void scheduler() {
        System.out.println("The scheduler program has started");
        int[] quanta = new int[3];
        while (!readyQueue.isEmpty()) {
            int pid = readyQueue.remove();
            System.out.println("\nProcess "+pid+" ==========>> CPU");
            System.out.println("Process " + pid + " is currently running...");

            quanta[pid]++;
            setState(pid, "Running");
            int pc = getPC(pid);
            int start = pid * procSize;
            String inst1 = getInst(pc);
            System.out.println("PC = " + pc);
            System.out.println(memory[pc].key + " is being executed: " + inst1);
            runInstruction(inst1, pid);
            pc++;
            setPC(pid, pc);
            if (processDone(pid)) {
                System.out.println("Process " + pid + " has executed 1 instruction and terminated");
                System.out.println("Quanta taken: " + quanta[pid]);
                System.out.println("Finished <<========== Process "+pid);

                continue;
            }
            String inst2 = getInst(pc);
            System.out.println("PC = " + pc);
            System.out.println(memory[pc].key + " is being executed: " + inst2);
            runInstruction(inst2, pid);
            pc++;
            setPC(pid, pc);
            setState(pid, "Not Running");
            if (!processDone(pid)) {
                readyQueue.add(pid);
                System.out.println("Process " + pid + " has executed 2 instructions and went back to the ready queue");
                System.out.println("Ready Queue <<========= Process "+pid);
            } else {
                System.out.println("Process " + pid + " has executed 2 instruction and terminated");
                System.out.println("Quanta taken: " + quanta[pid]);
                System.out.println("Finished <<========= Process "+pid);
            }


        }

        System.out.println("\nAll processes have finished execution");
    }

    private boolean processDone(int pid) {
        int pc = getPC(pid);
        if (getInst(pc) == null) {
            setState(pid, "Not Running");
            return true;
        }

        return false;
    }

    private void setPC(int pid, int val) {
        memory[pid * procSize + 2].val = val + "";
    }

    private int getMaxBoundary(int pid) {
        return Integer.parseInt(memory[pid * procSize + 4].val);
    }

    private int getMinBoundary(int pid) {
        return Integer.parseInt(memory[pid * procSize + 3].val);
    }

    private String getInst(int pc) {
        return memory[pc].val;
    }


    private void setState(int pid, String state) {
        memory[pid * procSize + 1].val = state;
    }


    private int getPC(int pid) {
        return Integer.parseInt(memory[pid * procSize + 2].val);
    }

    private void loadToMemory(String programName, int id) {
        // PCB 5 -> Instruction 15 -> Variables from 20 to remaining
        // id
        int minBoundary = id * procSize;
        int maxBoundary = minBoundary + procSize - 1;
        String state = "Not Running";
        int pc = minBoundary + 5;

        // add PCB to memory
        memory[minBoundary].key = "pid";
        memory[minBoundary].val = id + "";
        memory[minBoundary + 1].key = "state";
        memory[minBoundary + 1].val = state;
        memory[minBoundary + 2].key = "PC";
        memory[minBoundary + 2].val = pc + "";
        memory[minBoundary + 3].key = "minBoundary";
        memory[minBoundary + 3].val = minBoundary + "";
        memory[minBoundary + 4].key = "maxBoundary";
        memory[minBoundary + 4].val = maxBoundary + "";

        // print PCB content
        System.out.println("Process " + id + " has been loaded to memory");
        System.out.println("\nPCB contents:");
        System.out.println("Index: key -> value");
        for (int i = minBoundary; i < minBoundary + 5; i++)
            System.out.println(i + ": " + memory[i]);


        // add instructions to memory
        loadInstructionsToMemory(programName, pc);

        // print instruction in memory
        System.out.println("\nInstructions in memory:");
        System.out.println("Index: key -> value");
        for (int i = pc; i < pc + 15 && memory[i].key != null; i++) {
            System.out.println(i + ": " + memory[i]);
        }
        System.out.println("-----------------------------------------------------------------------------");
    }

    private void loadInstructionsToMemory(String programFilePath, int Idx) {

        try {
            int line = 1;
            File myObj = new File(programFilePath);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String instruction = myReader.nextLine();
                memory[Idx].key = "Line " + line;
                memory[Idx].val = instruction;
                Idx++;
                line++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void runAllPrograms() {
        String[] programs = new String[]{"src/Program 1.txt", "src/Program 2.txt", "src/Program 3.txt"};
        runPrograms(programs);

    }

    public static void main(String[] args) {
        Interpreter interpreter = new Interpreter();

        String program1 = "src/Program 1.txt";
        String program2 = "src/Program 2.txt";
        String program3 = "src/Program 3.txt";

        interpreter.runAllPrograms();

//        try {
//            System.out.println(interpreter.readFile("src/test.txt"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
