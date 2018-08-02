import java.io.*;

public class InvalidInputException extends Exception  {
    private String decision;
    public InvalidInputException(String input){
        this.decision = decision;
    }
    public String getInput(){
        return decision;
    }
}
