public class YABUT { 
    public static void main(String[] args) {
        displayProfile(); // Pagtawag sa method
    }

    public static void displayProfile() {
        String fullName = "RENYER N. YABUT"; 
        int age = 19;
        String courseAndYear = "Bachelor of Science in Information Technology                 |\n|                   (Information and Network Security Elective Track), 2nd Year";
        String school = "University of Makati (UMak)";
        String hobbies = "Watching Movies, Playing Video Games, and Exploring New       |\n|                   Technologies";
        String favTech = "My favorite tech or language is java but i want to explore   |\n|                   other technologies like networking, and web development,     |\n|                   and explore things in technology world.";
        String reasonForJava = "To build a strong foundation in programming and software     |\n|                   development and use this to systems to my business soon.";

        // Isang System.out.println lamang na nakapaloob sa border
        System.out.println(
            "+--------------------------------------------------------------------------------+\n" +
            "|                         DATA STRUCTURES AND ALGORITHMS                         |\n" +
            "+--------------------------------------------------------------------------------+\n" +
            "| Full Name:       " + fullName + "                                               |\n" +
            "| Age:             " + age + "                                                            |\n" +
            "| Course & Year:   " + courseAndYear + "  |\n" +
            "| School:          " + school + "                                   |\n" +
            "| Hobbies:         " + hobbies + "       |\n" +
            "| Favorite Programming Language or Technology:                                   |\n" +
            "|                   " + favTech + "     |\n" +
            "| Reason why you want to learn Java:                                             |\n" +
            "|                   " + reasonForJava + "     |\n" +
            "+--------------------------------------------------------------------------------+"
        );

        System.out.println("THE END");
       
}
}