import com.sun.org.apache.xpath.internal.operations.Bool;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class Registration {


    private static String currentUser ="";
    private static boolean loggedIn = false;
    private static boolean finished = false; //when finished is true, program terminates
    private static Connection connection = null;

    /**
     * Log in using input from command line
     * @param connection a non-null connection to the database
     * @return the boolean value indicating whether user has successfully logged in
     * @throws SQLException if any error occurred regarding database
     */
    public static boolean login(Connection connection) throws SQLException{
        assert null != connection;
        boolean exist = false;  //when exist is true, move on to input password, or otherwise be asked to create an account
        boolean loggedIn= false;
        int count = 0; // number of tries on login
        //create a scanner to read the command-line input
        Scanner scanner = new Scanner(System.in);
        while((exist==false || loggedIn==false) && count < 3) {
            //prompt for the user's input of email
            System.out.print("Please enter your username(email): ");
            String email = scanner.next().toLowerCase();
            boolean userExists = userExists(connection,email);
            if (userExists) {
                exist=true;
                System.out.print("Please enter your password: ");
                String password = scanner.next();
                //check if password matches
                PreparedStatement authorize = connection.prepareStatement("select * from OWNER where EMAIL = ? and PWORD = ? ");
                authorize.setString(1, email);
                authorize.setString(2, password);
                ResultSet set = authorize.executeQuery();
                if (set.next()){
                    System.out.println("login successful!");
                    currentUser= email;          //set current logged-in user's email to currentUser for later use
                    loggedIn =true;
                }else{                      //if password doesn't match, tries left decrement by one
                    count+=1;
                    System.out.println("Password doesn't match. Please try again. ");
                }
            } else {           //if email doesn't exist, tries left decrement by one
                count+=1;
                System.out.println("User doesn't exist.Please try again(username should be an email address) ");
            }
        }
        if(count==3){          //when user attempts more than 4 times login, login is locked
            System.out.println("Sorry you are not authorized to register your vehicle. Please create an account.");
            readOwner(connection);
        }
        return loggedIn;
    }

    /**
     * view all vehicles registered under current user's email
     * @param connection a non-null connection to the database
     * @param loggedIn a boolean used to check if user is loggedIn, if not user won't be able to view the vehicles
     * @throws SQLException if any error occurred regarding database
     */
    public static void viewVehicles(Connection connection, boolean loggedIn)throws SQLException{
        assert null != connection;
        if(loggedIn==false) {
            loggedIn = login(connection);
        }
        PreparedStatement showAll = connection.prepareStatement("select * from vehicle where OWNERID = ? ");
        showAll.setString(1, currentUser);
        ResultSet vehicleSet = showAll.executeQuery();
        System.out.println("Here are vehicles you have registered:");
        //print all vehicles registered by currentUser line by line
        while(vehicleSet.next()){
            ResultSetMetaData rsmd = vehicleSet.getMetaData();
            int col_n = rsmd.getColumnCount();
            for (int i= 1;i<= col_n;i++){
                if(i>1) System.out.print(" ");
                String colVal = vehicleSet.getString(i);
                System.out.print(colVal+ " ");
            }
            System.out.println("");
        }
        System.out.println();
        //ask user whether he/she would like to registr a new vehicle
        System.out.println("register a new vehicle(y/n)? ");
        //call makeDecision function to execute according to user's decision
        makeDecision(connection, currentUser, loggedIn);
    }

    /**
     * user makes decisions when asked to register a vehicle or not and program execute the decision
     * @param connection a non-null connection to the database
     * @param loggedIn a boolean used to check if user is loggedIn, if he/she is already logged in will be able to register vehicle direclty
     * @throws SQLException if any error occurred regarding database
     */
    public static void makeDecision(Connection connection, String email, boolean loggedIn) throws SQLException{
        assert null != connection;
        String decision = "";
        //create a scanner to read the command-line input
        Scanner scanner = new Scanner(System.in);
        while(!decision.equalsIgnoreCase("y")&&!decision.equalsIgnoreCase("n")&&!decision.equalsIgnoreCase("v")){
            decision = scanner.next();
            if(decision.equalsIgnoreCase("y")){
                //when user is not logged in, he/she will need to log in first
                if(loggedIn==false) {
                    loggedIn = login(connection);
                }
                readVehicle(connection,email,loggedIn);
            }
            //if user choose not to register a vehicle, program terminates
            else if (decision.equalsIgnoreCase("n")){
                System.out.println("Thanks.Bye.");
                System.exit(0);
            }else if(decision.equalsIgnoreCase("v")){
                viewVehicles(connection, loggedIn);          //user chooses to view all the vehicles under his name
            }
            else{
                System.out.println("Invalid input, please try again ");

            }
       }
    }

    /**
     * After registering a vehicle, user can choose whether to continue registering another vehicle
     * @param connection a non-null connection to the database
     * @return a boolean continueAdd indicates whether user wants to add a new vehicle or not
     * @throws SQLException if any error occurred regarding database
     */
    public static boolean continueAddDecision(Connection connection) throws SQLException{
        assert null != connection;
        boolean continueAdd = false;
        Scanner scanner = new Scanner(System.in);
        String decision = "";
        while (!decision.equalsIgnoreCase("y") && !decision.equalsIgnoreCase("n")) {
            decision = scanner.next();
            if (decision.equalsIgnoreCase("y")) {
                continueAdd = true;
                readVehicle(connection, currentUser,loggedIn);
            } else if (decision.equalsIgnoreCase("n")) {
                continueAdd = false;
                System.out.println("Thanks.Bye.");
                System.exit(0);
            }else{
                System.out.println("Invalid input, please try again");

            }
        }
        return continueAdd;
    }

    /**
     * Check if email is in correct form
     * @param email
     * @return a boolean match whether the email is correctedly formatted
     */
    public static boolean checkEmail(String email){
        //regex string used to check if email input is in correct format
        String emailFormat = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
        Boolean match = false;
        Pattern pattern = Pattern.compile(emailFormat);
        Matcher matcher = pattern.matcher(email);
        match = matcher.matches();
        return match;
    }

    /**
     * check if user exists in the database
     * @param connection a non-null connection to the database
     * @param email
     * @return a boolean userExists whether user already exists
     * @throws SQLException if any error occurred regarding database
     */
    public static boolean userExists(Connection connection, String email)throws SQLException{
        assert null != connection;
        Boolean userExists = false;
        PreparedStatement check_statement = connection.prepareStatement("select fname from OWNER where EMAIL = ? ");
        check_statement.setString(1, email);
        ResultSet resultSet = check_statement.executeQuery();
        if(resultSet.next()){
            userExists = true;
        }
        return userExists;
    }

    /**
     * Register an owner
     * @param connection a non-null connection to the database
     * @throws SQLException if any error occurred regarding database
     */
    public static void insertOwner(Connection connection, String fname, String lname, String email, String pword) throws SQLException{
        assert null != connection;
        String insert = "insert into owner(email, lname, fname, pword) values (?, ?, ?, ?)";
        PreparedStatement insert_statement = connection.prepareStatement(insert);
        insert_statement.setString(1, email);
        insert_statement.setString(2, lname);
        insert_statement.setString(3, fname);
        insert_statement.setString(4, pword);
        insert_statement.executeUpdate();
        //confirm user is registered
        String information = fname + " " + lname + " is successfully registered.Now you can register your vehicle.";
        System.out.println(information);
        loggedIn = true;
        currentUser = email;
        System.out.print("Register an vehicle(y/n)?");
        makeDecision(connection, email, loggedIn);

    }

    /**
     * check if manufacture date is in correct format
     * @param mdate
     */
    public static boolean validDate(String mdate){
        Boolean match = false;
        //regex string used to check if date format is correct
        String dateFormat = "^[0-9]{4}-(3[01]|[12][0-9]|0[1-9])-(1[0-2]|0[1-9])$";
        Pattern pattern = Pattern.compile(dateFormat);
        Matcher matcher = pattern.matcher(mdate);
        match = matcher.matches();
        return match;
    }

    /**
     * check if vehicle is already in the database
     *
     * @param connection  a non-null connection to the database
     * @param plate
     * @return a boolean vehicleExists whether vehicle is in database already
     * @throws SQLException if any error occured regarding the database
     */
    public static boolean vehicleExists(Connection connection, String plate)throws SQLException{
        assert null != connection;
        PreparedStatement check_statement = connection.prepareStatement("select * from vehicle where plate = ? ");
        check_statement.setString(1, plate);
        ResultSet resultSet = check_statement.executeQuery();
        boolean vehicleExists = false;
        if(resultSet.next()){
            vehicleExists = true;
        }
        return vehicleExists;
    }

    /**
     * Register a vehicle after reading valid inputs
     * @param connection a non-null connection to the database
     * @param plate
     * @param vtype vehicle type
     * @param ftype fuel type
     * @param mdate manufacture date
     * @param make
     * @param model
     * @param email ownerid
     * @throws SQLException if any error occured regarding the database
     */
    public static void registerVehicle(Connection connection, String plate, String vtype, String ftype, String mdate, String make, String model, String email) throws SQLException{
        assert null != connection;
        boolean continueAdd = false;
        String insert = "insert into vehicle(PLATE, VTYPE, FTYPE, MDATE, MODEL, MAKE, OWNERID) values (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement insert_statement = connection.prepareStatement(insert);

        insert_statement.setString(1, plate);
        insert_statement.setString(2, vtype);
        insert_statement.setString(3, ftype);
        insert_statement.setString(4, mdate);
        insert_statement.setString(5, model);
        insert_statement.setString(6, make);
        insert_statement.setString(7, email);

        insert_statement.executeUpdate();
        //confirm vehicle is registered
        System.out.println(make + " " + model + " of plate " + plate + " is successfully registered.");
        //return all vehicles registered under this owner
        System.out.println();
        loggedIn = true;
        viewVehicles(connection,loggedIn);
        System.out.println();
        String information ="Add another vehicle(y/n)? ";
        System.out.println(information);
        continueAdd = continueAddDecision(connection);
    }
    /**
     * Register a vehicle after user logged in
     * @param connection a non-null connection to the database, a String email used to link ownerid, a boolean loggedIn used to check if loggedIn
     * @throws SQLException if any error occured regarding the database
     */
    public static void readVehicle(Connection connection, String email, boolean loggedIn) throws SQLException{
        assert null != connection;
        boolean continueAdd = true;

        //arrayList used to check whether vehicle type input is correct
        List<String> vehicleTypes = new ArrayList<String>();
        vehicleTypes.add("MA");
        vehicleTypes.add("MB");
        vehicleTypes.add("MC");
        vehicleTypes.add("T");
        vehicleTypes.add("O");
        //arrayList used to check whetherfuel type input is correct
        List<String> fuelTypes = new ArrayList<String>();
        fuelTypes.add("PETROL");
        fuelTypes.add("DIESEL");
        fuelTypes.add("ELECTRIC");
        fuelTypes.add("GAS");
        fuelTypes.add("OTHERS");

        Scanner scanner = new Scanner(System.in);
        if(loggedIn == true){
            while(continueAdd==true) {
                //prompt for the user's input for vehicle information
                System.out.print("Enter vehicle's plate number: ");
                String plate = scanner.next().toUpperCase();
                //check if vehicle already exists
                boolean vehicleExists = vehicleExists(connection,plate);
                if(vehicleExists==false) {
                    System.out.print("Enter vehicle's type(choose from MA,MB,MC,T,O): ");
                    String vtype = scanner.next().toUpperCase();
                    //check if vehicle type input correct
                    while(!vehicleTypes.contains(vtype)){
                        System.out.println("Invalid input");
                        System.out.print("Enter vehicle's type(choose from MA,MB,MC,T,O): ");
                        vtype = scanner.next().toUpperCase();
                    }
                    System.out.print("Enter vehicle's fuel type(choose from petrol, diesel, electric,gas or others): ");
                    String ftype = scanner.next().toUpperCase();
                    //check if fuel type input correct
                    while(!fuelTypes.contains(ftype)){
                        System.out.println("Invalid input");
                        System.out.print("Enter vehicle's fuel type(choose from petrol, diesel, electric,gas or other): ");
                        ftype = scanner.next().toUpperCase();
                    }
                    System.out.print("Enter vehicle's manufacture date(yyyy-mm-dd): ");
                    String mdate = scanner.next().toUpperCase();
                    //check if manufacture date input correct
                    boolean match = validDate(mdate);
                    while(match==false) {
                        match = validDate(mdate);
                        System.out.println("Invalid input");
                        System.out.print("Enter vehicle's manufacture date(yyyy-mm-dd): ");
                        mdate = scanner.next().toUpperCase();
                    }
                    System.out.print("Enter vehicle's make: ");
                    String make = scanner.next().toUpperCase();
                    System.out.print("Enter vehicle's model: ");
                    String model = scanner.next().toUpperCase();
                    registerVehicle(connection, plate, vtype,ftype,mdate,make,model,email);

                }else{
                    System.out.print("Vehicle aready exists. Register another vehicle(y/n)? ");
                    continueAdd = continueAddDecision(connection);
                    if(continueAdd==true){
                        readVehicle(connection, email,loggedIn);
                    }

                }
            }
        }else{
            System.out.println("Sorry you are not authorized to register your vehicle. Please create an account.");
            readOwner(connection);
        }
    }

    /**
     * read in owner inputs for registration
     * @param connection
     * @throws SQLException if any error occured regarding the database
     */
    public static void readOwner(Connection connection) throws SQLException{
        assert null != connection;
        //create a scanner to read the command-line input
        Scanner scanner = new Scanner(System.in);
        //prompt for the user's input
        System.out.print("Create an account?(y/n) ");
        String decision = "";
        //only when user finished deciding or program terminate flag switched to true will loop break
        while((!decision.equalsIgnoreCase("y") || !decision.equalsIgnoreCase("n"))|| finished == false) {
            decision = scanner.next();
            if (decision.equalsIgnoreCase("y")){
                System.out.print("Please enter your first name: ");
                String fname=scanner.next().toLowerCase();
                System.out.print("Please enter your last name: ");
                String lname=scanner.next().toLowerCase();
                System.out.print("Please enter your email address: ");
                String email=scanner.next().toLowerCase();
                //check if email matches format, otherwise ask for new input
                boolean match=checkEmail(email);
                while(match==false){
                    match=checkEmail(email);
                    System.out.println("Invalid input");
                    System.out.print("Please enter a valid email address: ");
                    email=scanner.next().toLowerCase();
                }
                boolean userExists= userExists(connection, email);
                if(userExists==true){
                    System.out.print("User already exists.Log in to register a vehicle?(y/n) ");
                    makeDecision(connection,email,loggedIn);
                }else {
                    System.out.print("Please enter your password: ");
                    String pword = scanner.next().toLowerCase();
                    insertOwner(connection, fname, lname, email, pword);
                }
            }else if  (decision.equalsIgnoreCase("n")) {   //ask user if he wants to login and do stuff
                System.out.print("Log in to register or view vehicle(s)?(y-register,n-cancel,v-view) ");
                makeDecision(connection, currentUser, loggedIn);

            } else {
                System.out.print("Invalid input, please try again: ");
            }
        }
    }

    /**
     * Main program.
     * @param args
     */
    public static void main(String[] args){
        String url = "jdbc:sqlite:database.sqlite";
        System.out.println("open connection to "+url);
        try (Connection connection = DriverManager.getConnection(url)){
            readOwner(connection);

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }


    }
}
