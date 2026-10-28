public class Identity {
    public static void main(String[] args) {
        // == frente a equals()
        String firstText = new String("Luna");
        String secondText = new String("Luna");
        System.out.println(firstText == secondText);      // false
        System.out.println(firstText.equals(secondText)); // true

        // Arreglos
        int[] firstNumbers = {2, 4, 6};
        int[] sameArray = firstNumbers;
        int[] otherArray = {2, 4, 6};

        sameArray[0] = 99;

        System.out.println(firstNumbers[0]);            // 99
        System.out.println(otherArray[0]);              // 2
        System.out.println(firstNumbers == sameArray);  // true
        System.out.println(firstNumbers == otherArray); // false
    }
}
