public class YABUT {
    public static void main(String[] args) {
        displayProfile();
    }

    public static void displayProfile() {
        String fullName = "Renyer Yabut";
        int age = 19;
        String courseAndYear = "BS Information Technology Major in Information Network Security - 2nd Year";
        String school = "University of Makati (UMak)";
        String hobbies = "Partying, Video Games, Coding, and Watching Movies";
        String favTech = "Java, Web Development, Networking, Database";
        String reasonForJava = "I want to learn Java because it was the first programming language I learned back in senior high school, and it sparked my interest in technology. I want to continue building on that foundation, explore new tech stacks, and improve my skills in backend development, networking, and front-end development.";

        System.out.println(
            "+--------------------------------------------------------------------------------+\n" +
            "|                         STUDENT INFORMATION                                    |\n" +
            "+--------------------------------------------------------------------------------+\n" +
            "| Full Name:       " + fullName + "                                               |\n" +
            "| Age:             " + age + "                                                    |\n" +
            "| Course & Year:   " + courseAndYear + "                             |\n" +
            "| School:          " + school + "                                           |\n" +
            "| Hobbies:         " + hobbies + "                                       |\n" +
            "| Favorite Programming Language/Technology: " + favTech + "          |\n" +
            "| Reason why I want to learn Java:                                             |\n" +
            "|                   " + reasonForJava + " |\n" +
            "+--------------------------------------------------------------------------------+"
        );

        System.out.println("THE END");
    }
}

}