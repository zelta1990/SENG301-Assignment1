package steps;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.sqlite.SQLiteJDBCLoader;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;


public class RegistrationTestSteps {
    private static String url = "jdbc:sqlite:database.sqlite";
    private Registration registration = new Registration();
    //variables for owner test
    private String firstname = "";
    private String lastname = "";
    private String owner_email = "";
    private String owner_pword = "";
    private Boolean userExists=false;
    //variables for vehicle test
    private String plate = "";
    private String vtype = "";
    private String ftype = "";
    private String mdate = "";
    private String make = "";
    private String model = "";
    private Boolean vehicleExists=false;
    //common use
    private static Connection connection = null;
    private String result = "";

    //check if db is connected
    @Given("^I am connected to the WOF database$")
    public void iAmConnectedToTheWOFDatabase() throws SQLException {
        connection = DriverManager.getConnection(url);
    }

    @When("^I enter \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\"$")
    public void iEnter(String fname, String lname, String email, String pword)throws SQLException {
        firstname = fname;
        lastname = lname;
        owner_email = email;
        owner_pword = pword;
        result = registration.insertOwner(connection,firstname,lastname,owner_email,owner_pword);
    }

    //test whether an owner is added to the database
    @Then("^I should see \"([^\"]*)\"$")
    public void iShouldSee(String confirmation) throws SQLException {
        ResultSet set = Registration.retrieveOwner(connection,owner_email);
        Assert.assertEquals(set.getString("email"),owner_email);
    }

    //test whether owner receives confirmation upon registering
    @Then("^I should get \"([^\"]*)\"$")
    public void iShouldGet(String confirmation) throws SQLException {
        Assert.assertEquals(result, confirmation);
    }

    @Given("^\"([^\"]*)\" is already in the WOF database$")
    public void isAlreadyInTheWOFDatabase(String email) throws SQLException {
         owner_email = email;
    }

    @When("^I submit \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\"$")
    public void iSubmit(String fname, String lname, String email, String pword) throws SQLException {
        firstname = fname;
        lastname = lname;
        owner_email = email;
        owner_pword = pword;

    }
    //check if an existing owner can still register using the same email
    @Then("^I should find \"([^\"]*)\"$")
    public void iShouldFind(boolean exists) throws SQLException {
        userExists = registration.userExists(connection, owner_email);
        Assert.assertEquals(exists, userExists);
    }

    //user has to log in to register a vehicle
    @Given("^I am logged in as \"([^\"]*)\"$")
    public void iAmLoggedInAs(String email) throws SQLException {
        owner_email= email;
    }

    @When("^I type in \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\"$")
    public void iTypeIn(String vehiclePlate, String vehicleType, String fuelType, String manufactureDate, String vehicleMake, String vehicleModel) throws SQLException {
        plate = vehiclePlate;
        vtype = vehicleType;
        ftype = fuelType;
        mdate = manufactureDate;
        make = vehicleMake;
        model = vehicleModel;
        result = registration.registerVehicle(connection,plate,vtype,ftype,mdate,make,model,owner_email);
    }

    @Then("^I should be able to see vehicle with plate \"([^\"]*)\" is registered$")
    public void iShouldBeAbleToSeeVehicleWithPlateIsRegistered(String vehiclePlate) throws SQLException {
        ResultSet set = Registration.retrieveVehicle(connection,vehiclePlate);
        Assert.assertEquals(set.getString("plate"),vehiclePlate);
    }

    @Then("^I should be able to get \"([^\"]*)\"$")
    public void iShouldBeAbleToGet(String confirmation) throws SQLException{
        Assert.assertEquals(result, confirmation);
    }

    @Given("^\"([^\"]*)\" is in the WOF database$")
    public void isInTheWOFDatabase(String vehiclePlate) throws SQLException {
       plate= vehiclePlate;
    }

    @When("^I try to register \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\"$")
    public void iTryToRegister(String vehiclePlate, String vehicleType, String fuelType, String manufactureDate, String vehicleMake, String vehicleModel) throws SQLException {
        plate = vehiclePlate;
        vtype = vehicleType;
        ftype = fuelType;
        mdate = manufactureDate;
        make = vehicleMake;
        model = vehicleModel;
    }

    @Then("^I should find vehicle already exsists is \"([^\"]*)\"$")
    public void iShouldFindVehicleAlreadyExsistsIs(Boolean info) throws SQLException {
        vehicleExists = Registration.vehicleExists(connection,plate);
        Assert.assertEquals(vehicleExists, info);
    }





}
