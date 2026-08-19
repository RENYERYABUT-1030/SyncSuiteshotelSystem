public class YABUT {
    public static void main(String[] args) {
        // 1. Simple addition with literals
        int a = 5;
        int b = 3;
        int sum = a + b;
        System.out.println("5 + 3 = " + sum);  // Output: 5 + 3 = 8

        // 2. Addition with user input
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.print("Enter first number: ");
        double num1 = scanner.nextDouble();
        System.out.print("Enter second number: ");
        double num2 = scanner.nextDouble();
        double result = num1 + num2;
        System.out.println("Sum: " + result);

        // 3. Compound assignment
        int counter = 10;
        counter += 5;  // Same as counter = counter + 5
        System.out.println("Counter after += 5: " + counter);  // 15

        // 4. Adding multiple values
        int total = 1 + 2 + 3 + 4 + 5;
        System.out.println("1+2+3+4+5 = " + total);  // 15

        scanner.close();
    }

    // 5. Reusable method for addition
    public static int add(int x, int y) {
        return x + y;
    }
}