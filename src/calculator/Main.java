package calculator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HashMap<String, Integer> variables = new HashMap<>();
        while (true) {
            String input = scanner.nextLine().trim();
            switch (input) {
                case "/exit":
                    System.out.println("Bye!");
                    System.exit(0);
                    break;
                case "/help":
                    System.out.println("Escriba una operación matemática (suma, resta, multiplicaciónn y division");
                    System.out.println("Es posible asignar valores a varibles");
                    break;
                case "":
                    continue;
                default:
                    if (input.matches("^/.*")) {
                        System.out.println("Unknown command");
                    } else if (input.matches("\\w+\\s*=\\s*[+-]?\\w+")) {
                        evaluateAssignment(input, variables);
                    } else if (input.matches("\\w+")) {
                        printVariable(input, variables);
                    } else {
                        input = formatExpression(input);
                        try {
                            solveOperation(input, variables);
                        } catch (Exception e) {
                            System.out.println("Invalid Expression");
                        }
                    }
            }
        }

    }
    public static void solveOperation(String input, HashMap<String, Integer> variables) throws Exception {
        Deque<String> postfix = new ArrayDeque<>();
        Deque<String> stack = new ArrayDeque<>();
        Map<String, String> element;
        String incommingElement;
        String remaining;

        while (!input.isEmpty()) {
            element = nextElement(input);  // Obtener el siguiente elemento y el restante de la entrada
            incommingElement = element.get("nextElement");
            remaining = element.get("remaining");

            //System.out.println(element);

            if (!isOperator(incommingElement)) {
                // Si el elemento es un operando (número o variable), añadirlo al resultado
                postfix.addLast(incommingElement);
            } else {
                if (incommingElement.equals("(")) {
                    // Si el operador es un paréntesis de apertura, agregarlo a la pila
                    stack.addFirst(incommingElement);
                } else if (incommingElement.equals(")")) {
                    // Si es un parentesis de cierre, vaciamos hasta encontrar el paréntesis de apertura
                    while (!stack.isEmpty() && !stack.peekFirst().equals("(")) {
                        postfix.addLast(stack.pollFirst());
                    }
                    stack.pollFirst();  // Eliminar el paréntesis de apertura
                } else {
                    // Para operadores, usamos comparePrecedence para manejar la precedencia
                    while (!stack.isEmpty() && !stack.peekFirst().equals("(")
                            && comparePrecedence(stack.peekFirst(), incommingElement) >= 0) {
                        postfix.addLast(stack.pollFirst());
                    }
                    // Agregar el operador actual a la pila
                    stack.addFirst(incommingElement);
                }
            }

            input = remaining;  // Actualizar la entrada restante


        }

// Vaciar el resto de la pila al final
        while (!stack.isEmpty()) {
            postfix.addLast(stack.pollFirst());
        }

        //System.out.println("Postfix: " + postfix);
        System.out.println(calculateResult(postfix, variables));
    }

    public static int calculateResult(Deque<String> postfix, Map<String, Integer> variables){
        Deque<Integer> stack = new ArrayDeque<>();
        String incommingElement;
        String operator;
        int a, b, result = 0;
        while(!postfix.isEmpty()){
            incommingElement = postfix.peekFirst();
            if(isNumber(incommingElement)){
                stack.addFirst(Integer.parseInt(postfix.pop()));
            } else if(incommingElement.matches("\\w+")){
                if(variables.containsKey(incommingElement)){
                    stack.addFirst(variables.get(incommingElement));
                    postfix.pop();

                }else{
                    //TODO
                }
            }else{
                operator = postfix.pop();
                a = stack.pop();
                b = stack.pop();
                 switch (operator) {
                     case "*":
                         result = a * b;
                         break;
                     case "/":
                         result = b / a;
                         break;
                     case "+":
                         result = b + a;
                         break;
                     case "-":
                         result = b - a;
                         break;

                };
                stack.addFirst(result);

            }
        }
        //System.out.println("Stack: " + stack);
        return stack.peekFirst();
    }

    public static boolean isNumber(String str){
        try{
            int value = Integer.parseInt(str);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public static Map<String, String> nextElement(String input) throws Exception {
        HashMap<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile("[a-zA-Z]+|[0-9]+");
        Matcher matcher = pattern.matcher(input);
        if(isOperator(input.substring(0,1))){
            result.put("nextElement", input.substring(0,1));
            result.put("remaining", input.substring(1));
        }else if(input.substring(0,1).matches("[a-zA-Z0-9]")){
            if(matcher.find()){
                result.put("nextElement", matcher.group());
                result.put("remaining", input.substring(matcher.end()));
            }
        }else{
            throw new Exception();
        }
        return result;
    }

    public static int comparePrecedence(String a, String b) {
        HashMap<String, Integer> precedenceValues = new HashMap<>();
        precedenceValues.put("^", 3);  // Potenciación: mayor precedencia
        precedenceValues.put("*", 2);  // Multiplicación: intermedia
        precedenceValues.put("/", 2);  // División: intermedia
        precedenceValues.put("+", 1);  // Suma: baja precedencia
        precedenceValues.put("-", 1);  // Resta: baja precedencia

        if (b == null || b.matches("[+-]?[0-9]+")) {
            // Si b es un número o es nulo, darle precedencia a 'a'
            return 1;
        }

        int precedenceA = precedenceValues.getOrDefault(a, -1); // Usar -1 si no está definido
        int precedenceB = precedenceValues.getOrDefault(b, -1); // Usar -1 si no está definido

        // Comparar precedencias
        if (precedenceA > precedenceB) {
            return 1;  // 'a' tiene mayor precedencia
        } else if (precedenceA < precedenceB) {
            return -1; // 'a' tiene menor precedencia
        } else {
            return 0;  // 'a' y 'b' tienen igual precedencia
        }
    }



    public static boolean isOperator(String str){
        return "+-/*^()".contains(str);
    }

    public static String  formatExpression(String input){
        do{
            input = input.replaceAll("\\s+","")
                    .replaceAll("(\\+-)|(-\\+)","-")
                    .replaceAll("(--)+", "+")
                    .replaceAll("\\+{2,}", "+");
        }while(input.contains("-+") || input.contains("+-"));
        return input;
    }

    public static int getSum(String input, HashMap<String, Integer> variables) throws Exception {
        List<String> inputArray = Arrays.asList(input.split("\\s+"));
        ListIterator<String> iterator = inputArray.listIterator();
        String firstValue = iterator.next();
        int result = variables.containsKey(firstValue)? variables.get(firstValue) : Integer.parseInt(firstValue);
        while (iterator.hasNext()) {
            String nextOperator = iterator.next();
            String nextAddend = iterator.next();
            try {
                if (nextOperator.matches("\\++|(--)*")){
                    if(variables.containsKey(nextAddend)) result += variables.get(nextAddend);
                    else result += Integer.parseInt(nextAddend);
                } else if (nextOperator.matches("(--)*-")){
                    if(variables.containsKey(nextAddend)) result -= variables.get(nextAddend);
                    else result -= Integer.parseInt(nextAddend);
                } else{
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("Invalid Expression");
                System.exit(0);
            }

        }
        return result;
    }

    public static void evaluateAssignment(String input, HashMap<String, Integer> variables) {
        List<String> inputArr = Arrays.asList(input.split("\\s*=\\s*"));
        if (isValidIdentifier(inputArr.get(0))) {
            if (inputArr.get(1).matches("[+-]?[0-9]+")) {
                variables.put(inputArr.get(0), Integer.parseInt(inputArr.get(1)));
            }else if(isValidIdentifier(inputArr.get(1))){
                if(variables.containsKey(inputArr.get(1))){
                    variables.put(inputArr.get(0), variables.get(inputArr.get(1)));
                }else{
                    System.out.println("Unknown variable");
                }
            }else {
                System.out.println("Invalid assignment");
            }
        }
    }

    public static boolean isValidIdentifier(String str) {
        if (str.matches("[a-zA-Z]+")) {
            return true;
        }
        System.out.println("Invalid identifier");
        return false;
    }
    public static void printVariable(String key, HashMap<String, Integer> variables){
        if(isValidIdentifier(key)){
            if(variables.containsKey(key)){
                System.out.println(variables.get(key));
            }else{
                System.out.println("Unknown variable");
            }
        }
    }
}
