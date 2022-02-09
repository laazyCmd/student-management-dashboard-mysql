package com.project.crud.database;

import com.project.crud.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    public Database() {
        createStudentsTable();
    }

    public Connection getConnection() {
        try {
            String url = "URL";
            String username = "USERNAME";
            String password = "PASSWORD";

            System.out.println( "Successfully connected to the database!" );
            return DriverManager.getConnection( url, username, password );
        } catch ( Exception err ) {
            System.err.println( "Warning! An exception has occurred in getConnection() method: " + err.getMessage() );
        }

        return null;
    }

    public List< Student > getStudents() {
        List< Student > list = new ArrayList<>();

        try ( final Connection conn = getConnection() ) {
            final String query = "SELECT * FROM `students`";
            final PreparedStatement getStudents = conn.prepareStatement( query );
            final ResultSet response = getStudents.executeQuery();

            while ( response.next() ) {
                list.add( new Student(
                        Integer.parseInt( response.getString( 1 ) ),
                        response.getString( 2 ),
                        response.getString( 3 ),
                        Integer.parseInt( response.getString( 4 ) ),
                        Integer.parseInt( response.getString( 5 ) ),
                        response.getString( 6 ),
                        response.getString( 7 ),
                        response.getString( 8 ) )
                );
            }
        } catch ( Exception err ) {
            System.err.println( "Warning! An exception has occurred in getStudents() method: " + err.getMessage() );
        }

        return list;
    }

    public void createStudentsTable() {
        try ( final Connection conn = getConnection() ) {
            final PreparedStatement createTable = conn.prepareStatement( "create table if not exists students ( studentNo int not null, firstName char(32) not null, lastName char(32) not null, yearLevel int not null, age int not null, gender char(10) not null, program char(64) not null, imagePath char(255) not null )" );
            final int tableExists = createTable.executeUpdate();

            if ( tableExists == 0 ) System.out.println( "A `students` table already exists. Skipping..." );
            else System.out.println( "A `students` table doesn't exist. Successfully created one!" );
        } catch ( Exception err ) {
            System.err.println( "Warning! An exception has occurred in createStudentsTable() method: " + err.getMessage() );
        }
    }

    public void insertStudent( int studentNumber, String firstName, String lastName, int yearLevel, int age, String gender, String program, String imagePath ) {
        try ( final Connection conn = getConnection() ) {
            final String query = String.format( "insert into students ( studentNo, firstName, lastName, yearLevel, age, gender, program, imagePath ) values ( %d, '%s', '%s', %d, %d, '%s', '%s', '%s' )", studentNumber, firstName, lastName, yearLevel, age, gender, program, imagePath );
            final PreparedStatement insertStudent = conn.prepareStatement( query );
            final int successful = insertStudent.executeUpdate();

            if ( successful == 1 ) System.out.println( "Successfully added a new entry into the `students` table." );
            else System.err.println( "Something went wrong! You should check it out, maybe it's the database... or you!" );
        } catch ( Exception err ) {
            System.err.println( "Warning! An exception has occurred in insertStudent() method: " + err.getMessage() );
        }
    }

    public void updateStudent( int studentNumber, String updatedFirstName, String updatedLastName, int updatedYearLevel, int updatedAge, String updatedGender, String updatedProgram, String updatedImagePath ) {
        try ( final Connection conn = getConnection() ) {
            final String query = String.format( "update `students` set firstName = '%s', lastName = '%s', yearLevel = %d, age = %d, gender = '%s', program = '%s', imagePath = '%s' where studentNo = %d", updatedFirstName, updatedLastName, updatedYearLevel, updatedAge, updatedGender, updatedProgram, updatedImagePath, studentNumber  );
            final PreparedStatement updateStudent = conn.prepareStatement( query );
            final int successful = updateStudent.executeUpdate();

            if ( successful == 1 ) System.out.println( "Successfully updated the entry with a student number " + studentNumber + " into the `students` table." );
            else System.err.println( "Something went wrong! You should check it out, maybe it's the database... or you!" );
        } catch ( Exception err ) {
            System.err.println( "Warning! An exception has occurred in updateStudent() method: " + err.getMessage() );
        }
    }

    public void removeStudent( int studentNumber ) {
        try ( final Connection conn = getConnection() ) {
            final String query = "delete from `students` where studentNo = " + studentNumber;
            final PreparedStatement removeStudent = conn.prepareStatement( query );
            final int successful = removeStudent.executeUpdate();

            if ( successful == 1 ) System.out.println( "Successfully removed the entry with a student number " + studentNumber + " from the `students` table." );
            else System.err.println( "Something went wrong! You should check it out, maybe it's the database... or you!" );
        } catch ( Exception err ) {
            System.err.println( "Warning! An exception has occurred in removeStudent() method: " + err.getMessage() );
        }
    }

    public List< Student > searchStudents( String keywords ) {
        List< Student > searchedStudents = new ArrayList<>();

        try ( final Connection conn = getConnection() ) {
            final String query = "select * from `students` where concat( studentNo, firstName, lastName, program ) REGEXP '" + keywords + "'";
            final PreparedStatement searchStudents = conn.prepareStatement( query );
            final ResultSet response = searchStudents.executeQuery();

            while ( response.next() ) {
                searchedStudents.add( new Student(
                        Integer.parseInt( response.getString( 1 ) ),
                        response.getString( 2 ),
                        response.getString( 3 ),
                        Integer.parseInt( response.getString( 4 ) ),
                        Integer.parseInt( response.getString( 5 ) ),
                        response.getString( 6 ),
                        response.getString( 7 ),
                        response.getString( 8 ) )
                );
            }
        } catch ( Exception err ) {
            System.err.println( "Warning! An exception has occurred in searchStudents() method: " + err.getMessage() );
        }

        return searchedStudents;
    }

    public boolean checkIfDuplicate( int studentNumber ) {
        try ( final Connection conn = getConnection() ){
            final String query = String.format( "select * from `students` where `studentNo` like %d", studentNumber );
            final PreparedStatement checkDuplicate = conn.prepareStatement( query );
            final ResultSet hasDuplicate = checkDuplicate.executeQuery();

            return hasDuplicate.next();
        } catch ( Exception err ) {
            System.err.println( "Warning! An exception has occurred in checkIfDuplicate() method: " + err.getMessage() );
        }

        return false;
    }
}
