import java.io.*;
import java.util.*;

public class Interpreter {

    private static final List acceptableInstructions = Arrays.asList("print", "assign", "add", "writeFile", "readFile", "input");
    Stack<String> stack;
    Hashtable<String, String> variables;

    public Interpreter() {
        stack = new Stack<String>();
        variables = new Hashtable<String, String>();
    }

    public void print(String argument) {
        if (variables.containsKey(argument)) {
            System.out.println(variables.get(argument));
        } else {
            System.out.println(argument);

        }
    }

    public void assign(String varName, String argument) {
        if (variables.containsKey(argument)) {
            variables.put(varName, variables.get(argument));
        } else {
            variables.put(varName, argument);
        }
    }

    public String readFile(String argument) throws IOException {
        String filePath;

        if (variables.containsKey(argument)) {
            filePath = variables.get(argument);
        } else {
            filePath = argument;
        }

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        // delete the last new line separator
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        reader.close();

        String content = stringBuilder.toString();
        return content;
    }

    public void writeFile(String arg1, String arg2) {
        String filePath;
        String dataToWrite;

        if (variables.containsKey(arg1)) {
            filePath = variables.get(arg1);
        } else {
            filePath = arg1;
        }

        if (variables.containsKey(arg2)) {
            dataToWrite = variables.get(arg2);
        } else {
            dataToWrite = arg2;
        }


        try (PrintWriter out = new PrintWriter(filePath)) {
            out.println(dataToWrite);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void add(String arg1, String arg2) {
        int no1;
        int no2;

        no1 = Integer.parseInt(variables.get(arg1));

        if (variables.containsKey(arg2)) {
            no2 = Integer.parseInt(variables.get(arg2));
        } else {
            no2 = Integer.parseInt(arg2);
        }

        int res = no1 + no2;
        variables.put(arg1, String.valueOf(res));

    }


    public void runInstruction(String instruction) {
        String[] terms = instruction.split(" ");
        for (String term : terms) {
            stack.push(term);
        }

        String op1 = "";
        String op2 = "";

        while (stack.size() > 0) {
            String term = stack.pop();

            if (acceptableInstructions.contains(term)) {
                switch (term) {
                    case "print":
                        print(op1);
                        op1 = "";
                        break;
                    case "assign":
                        assign(op2, op1);
                        op1 = "";
                        op2 = "";
                        break;
                    case "input":
                        Scanner sc = new Scanner(System.in);
                        stack.push(sc.next());
                        break;
                    case "readFile":
                        try {
                            stack.push(readFile(op1));
                            op1 = "";
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "writeFile":
                        writeFile(op2, op1);
                        op1 = "";
                        op2 = "";
                        break;
                    case "add":
                        add(op2, op1);
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

//            System.out.println(variables);
        }
    }

    public void runProgram(String programFilePath) {
        File file = new File(programFilePath);
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

        interpreter.runProgram(program1);
    }
}
