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
        variables = new Hashtable<>();
    }

    public void print(String argument) {
        System.out.println(variables.getOrDefault(argument, argument));
    }

    public void assign(String varName, String argument) {
        if (variables.containsKey(argument)) {
            variables.put(varName, variables.get(argument));
        } else {
            variables.put(varName, argument);
        }
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



    public void runInstruction(String instruction) {
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

    public void runProgram(String programFilePath) {
        try {
            File myObj = new File(programFilePath);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String instruction = myReader.nextLine();
                runInstruction(instruction);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
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
