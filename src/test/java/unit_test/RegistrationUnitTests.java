package unit_test;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import steps.Registration;

import java.sql.Connection;

public class RegistrationUnitTests {
    private static String url = "jdbc:sqlite:database.sqlite";
    private Registration registration = new Registration();
    private static Connection connection = null;

    /**
     * test if connected to database
     * @throws SQLException
     */
    @Test
    public void iAmConnectedToTheWOFDatabase() throws SQLException {
        connection = DriverManager.getConnection(url);
    }

    /**
     * test registering user
     * @throws SQLException
     */
    @Test
    public void testRegisterUser()throws SQLException{
        //connection = DriverManager.getConnection(url);
        String result = registration.insertOwner(connection, "tally", "tally", "tally@gmail.com", "1234567");
        String expected = "tally tally is successfully registered.Now you can register your vehicle.";
        Assert.assertEquals(result,expected);
    }

    /**
     * test registering a vehicle
     * @throws SQLException
     */
    @Test
    public void testRegisterVehicle() throws SQLException{
       // connection = DriverManager.getConnection(url);
        String result = registration.registerVehicle(connection, "OPI121", "mc", "PETROL", "1990-09-01", "TOYOTA", "RAV4", "tally@gmail.com");
        String expected = "TOYOTA RAV4 of plate OPI121 is successfully registered.";
        Assert.assertEquals(result,expected);

    }

    /**
     * test checking email format
     * @throws SQLException
     */
    @Test
    public void testCheckEmail() throws Exception{
        boolean correctFormat1 = registration.checkEmail("kelly");
        boolean wrong = false;
        boolean correct = true;
        Assert.assertEquals(correctFormat1, wrong);
        boolean correctFormat2 = registration.checkEmail("kelly@");
        Assert.assertEquals(correctFormat2, wrong);
        boolean correctFormat3 = registration.checkEmail("kelly@gmail.com");
        Assert.assertEquals(correctFormat3, correct);
    }

    /**
     * test a user exists in db
     * @throws SQLException
     */
    @Test
    public void testUserExists() throws SQLException {
        connection = DriverManager.getConnection(url);
        String email1 = "kelly@gmail.com";
        String email2 = "haha@gmail.com";
        boolean userExists = registration.userExists(connection, email1);
        boolean expected = true;
        Assert.assertEquals(userExists,expected);
        userExists = registration.userExists(connection, email2);
        expected = false;
        Assert.assertEquals(userExists,expected);
    }

    /**
     * Test validating a manufacture date
     * @throws Exception
     */
    @Test
    public void testValidDate() throws Exception{
        String date1= "19900102";
        String date2= "1990/01/02";
        String date3 = "1990-01-02";
        boolean valid = registration.validDate(date1);
        boolean expected = false;
        Assert.assertEquals(valid,expected);
        valid = registration.validDate(date2);
        Assert.assertEquals(valid,expected);
        expected = true;
        valid = registration.validDate(date3);
        Assert.assertEquals(valid,expected);

    }

    @Test
    public void testVehicleExists() throws SQLException{
        connection = DriverManager.getConnection(url);
        String plate = "UPI123";
        String plate1 = "11111";
        boolean vehicleExists = registration.vehicleExists(connection,plate);
        boolean expected = true;
        Assert.assertEquals(vehicleExists,expected);
        vehicleExists=registration.vehicleExists(connection,plate1);
        expected = false;
        Assert.assertEquals(vehicleExists,expected);


    }
}
