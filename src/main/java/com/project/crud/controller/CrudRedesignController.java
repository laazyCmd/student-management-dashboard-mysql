package com.project.crud.controller;

import com.project.crud.database.Database;
import com.project.crud.listener.Listen;
import com.project.crud.model.Student;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.util.*;
import java.util.regex.Pattern;

public class CrudRedesignController implements Initializable  {

    ObservableList< String > yearLevelList = FXCollections.observableArrayList( "1", "2", "3", "4" );
    ObservableList< String > genderList = FXCollections.observableArrayList( "Male", "Female" );
    ObservableList< String > programList = FXCollections.observableArrayList("College of Arts", "College of Business Education", "College of Engineering and Architecture", "College of Information Technology Education","Graduate Program", "Maritime Education");
    ObservableList< String > sortingList = FXCollections.observableArrayList( "Unsorted", "Student Number", "Last Name" );

    @FXML private BorderPane windowProgram;
    @FXML private Button addButton, editButton, deleteButton;
    @FXML private GridPane grid;
    @FXML private HBox addStudentPane, editStudentPane, deleteStudentPane, studentInfoPane;
    @FXML private VBox studentListPane;
    @FXML private TextField
            searchField,
            studentIdField, firstNameField, lastNameField, ageField,
            editFirstNameField, editLastNameField, editAgeField;
    @FXML private ComboBox< String >
            yearLevelBox, genderBox, programBox,
            editYearLevelBox, editGenderBox, editProgramBox,
            sortingBox;
    @FXML private Label
            editStudentIdLabel,
            deleteStudentIdText, deleteFirstNameText, deleteLastNameText, deleteYearLevelText, deleteAgeText, deleteGenderText, deleteProgramText,
            infoStudentIdLabel, infoFirstNameLabel, infoLastNameLabel, infoAgeLabel, infoGenderLabel, infoYearLevelLabel, infoProgramLabel;
    @FXML private ImageView
            addStudentImage,
            editStudentImage,
            deleteStudentImage,
            infoStudentImage,
            viewSign;

    private double xOffset;
    private double yOffset;

    private List<Student> students = new ArrayList<>();
    private Student selectedStudent;
    private Listen listener;

    BufferedReader read = null;
    BufferedWriter write = null;

    Database db;
    Connection con;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        db = new Database();

        yearLevelBox.getItems().addAll( yearLevelList );
        genderBox.getItems().addAll( genderList );
        programBox.getItems().addAll( programList );
        editYearLevelBox.getItems().addAll( yearLevelList );
        editGenderBox.getItems().addAll( genderList );
        editProgramBox.getItems().addAll( programList );
        sortingBox.getItems().addAll( sortingList );
        sortingBox.getSelectionModel().select( 0 );

//        try {
//            addStudents( getStudents() );
//        } catch ( IOException err ) {
//            System.err.println( "Warning! IOException has occurred at initialize() function: " + err.getMessage() );
//        }
    }

    public List< Student > getStudents() throws IOException {
        List< Student > studentEntries = new ArrayList<>();

        Task< Void > getStudentsTask = new Task< Void >() {
            @Override
            protected Void call() throws Exception {

                List< Student > students = new ArrayList<>();

                try {
                    read = new BufferedReader( new FileReader( "database/students-list.txt" ) );

                    String s;
                    while ( ( s = read.readLine() ) != null ) {
                        if ( s.trim().isEmpty() ) continue;

                        String[] entry = s.split( "&" );
                        students.add( new Student( Integer.parseInt( entry[0] ), entry[1], entry[2], Integer.parseInt( entry[3] ), Integer.parseInt( entry[4] ), entry[5], entry[6], entry[7] ) );
                    }
                } catch ( IOException err ) {
                    System.err.println( "Warning! IOException has occurred at getStudents() function: " + err.getMessage() );
                } finally {
                    if ( read != null ) read.close();
                    studentEntries.addAll( students );
                }

                return null;
            }
        };

        getStudentsTask.run();

        return studentEntries;
    }

    public void addStudents( List< Student > list ) {
        grid.getChildren().clear();
        viewSign.setImage( new Image( Objects.requireNonNull( this.getClass().getResourceAsStream("/com/project/crud/images/loading.png") ) ) );
        viewSign.setVisible( true );

        Task< Void > backgroundTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                List< Student > givenList = new ArrayList<>( list );
                students.clear();
                students.addAll( givenList );

                if ( sortingBox.getSelectionModel().getSelectedIndex() == 1 ) givenList.sort( Comparator.comparing( Student::getStudentNumber ) );
                else if ( sortingBox.getSelectionModel().getSelectedIndex() == 2 ) givenList.sort( Comparator.comparing( Student::getLastName ) );
                else {
                    givenList.clear();
                    givenList.addAll( students );
                }

                if ( givenList.size() > 0 ) {
                    listener = new Listen() {
                        @Override
                        public void onClickListener( MouseEvent event, Student student ) {
                            if ( event.getButton().equals( MouseButton.PRIMARY ) ) {
                                selectedStudent = student;

                                if ( event.getClickCount() == 2 ) {
                                    infoStudentIdLabel.setText( String.valueOf( student.getStudentNumber() ) );
                                    infoFirstNameLabel.setText( student.getFirstName() );
                                    infoLastNameLabel.setText( student.getLastName() );
                                    infoAgeLabel.setText( String.valueOf( student.getAge() ) );
                                    infoGenderLabel.setText( student.getGender() );
                                    infoYearLevelLabel.setText( String.valueOf( student.getYearLevel() ) );
                                    infoProgramLabel.setText( student.getProgram() );

                                    if ( !student.getImagePath().equals( "null" ) ) infoStudentImage.setImage( new Image( student.getImagePath() ) );

                                    if ( infoStudentImage.getImage().isError() || student.getImagePath().equals( "null" ) ) {
                                        if ( student.getGender().equals( "Male" ) ) infoStudentImage.setImage( new Image( Objects.requireNonNull(this.getClass().getResourceAsStream("/com/project/crud/images/male-student.png") ) ) );
                                        else infoStudentImage.setImage( new Image( Objects.requireNonNull(this.getClass().getResourceAsStream("/com/project/crud/images/female-student.png") ) ) );
                                    }


                                    studentInfoPane.toFront();
                                }
                            }
                        }
                    };
                }

                Platform.runLater( new Runnable() {
                    @Override
                    public void run() {
                        int column = 0;
                        int row = 1;

                        try {
                            for ( Student student : givenList ) {
                                FXMLLoader loader = new FXMLLoader();
                                loader.setLocation( getClass().getResource( "/com/project/crud/components/student-model.fxml" ) );
                                VBox vbox = loader.load();

                                StudentController studentController = loader.getController();
                                studentController.setData( student, listener );

                                if ( column == 4 ) {
                                    column = 0;
                                    row++;
                                }

                                grid.add( vbox, column++, row );
                                grid.setMinWidth( Region.USE_COMPUTED_SIZE );
                                grid.setPrefWidth( Region.USE_COMPUTED_SIZE );
                                grid.setMaxWidth( Region.USE_PREF_SIZE );

                                grid.setMinHeight( Region.USE_COMPUTED_SIZE );
                                grid.setPrefHeight( Region.USE_COMPUTED_SIZE );
                                grid.setMaxHeight( Region.USE_PREF_SIZE );

                                GridPane.setMargin( vbox, new Insets( 17 ) );
                            }
                        } catch ( IOException err ) {
                            System.err.println( "Warning! IOException/InterruptedException has occurred at GridThread: " + err.getMessage() );
                        } finally {
                            if ( givenList.size() == 0 ) {
                                viewSign.setImage( new Image( Objects.requireNonNull( this.getClass().getResourceAsStream( "/com/project/crud/images/no-entry-found.png") ) ) );
                                viewSign.setVisible( true );
                            } else {
                                viewSign.setVisible( false );
                            }
                        }
                    }
                } );

                return null;
            }
        };

        backgroundTask.run();
    }

    @FXML
    void addImage() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog( windowProgram.getScene().getWindow() );

        if ( file == null ) return;
        addStudentImage.setImage( new Image( "file:/" + file.toString() ) );
    }

    @FXML
    void editImage() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog( windowProgram.getScene().getWindow() );

        if ( file == null ) return;
        editStudentImage.setImage( new Image( "file:/" + file.toString() ) );
    }

    @FXML
    void addStudent() {
        boolean isFilledOut =
                !studentIdField.getText().trim().isEmpty() &&
                        !firstNameField.getText().trim().isEmpty() &&
                        !lastNameField.getText().trim().isEmpty() &&
                        !yearLevelBox.getSelectionModel().isEmpty() &&
                        !ageField.getText().trim().isEmpty() &&
                        !genderBox.getSelectionModel().isEmpty() &&
                        !programBox.getSelectionModel().isEmpty();

        if ( isFilledOut ) {
            boolean studentIdIsNumber = checkIfNumber( studentIdField.getText().trim() );
            boolean ageIsNumber = checkIfNumber( ageField.getText().trim() );

            if ( studentIdIsNumber && ageIsNumber ) {
//                boolean isDuplicate = checkDuplicate( studentIdField.getText() );

                    int studentId = Integer.parseInt( studentIdField.getText().trim() );
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    int yearLevel = Integer.parseInt( yearLevelBox.getValue() );
                    int age = Integer.parseInt( ageField.getText().trim() );
                    String gender = genderBox.getValue();
                    String program = programBox.getValue();
                    String imagePath = "null";

                    if ( addStudentImage.getImage().getUrl() != null ) {
                        if ( Pattern.compile( "(\\w|\\d)+((\\.jpg)|(\\.jpeg)|(\\.png))" ).matcher( addStudentImage.getImage().getUrl() ).find() ) {
                            imagePath = addStudentImage.getImage().getUrl();
                        }
                    }

                    db.insertStudents( studentId, firstName, lastName, yearLevel, age, gender, program, imagePath );
                    closePane();

                    Alert alert = new Alert( Alert.AlertType.INFORMATION );
                    alert.setTitle( "Success!" );
                    alert.setHeaderText( "You have successfully added a student." );

                    DialogPane dialog = alert.getDialogPane();
                    dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource( "/com/project/crud/styles/styles.css" ) ).toString() );
                    dialog.getStyleClass().add( "dialog" );

                    alert.showAndWait();
            } else if ( !studentIdIsNumber ) {
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Warning!" );
                alert.setHeaderText( "The Student ID must be a number." );
                alert.setContentText( "Please try again." );

                DialogPane dialog = alert.getDialogPane();
                dialog.getStylesheets().add( Objects.requireNonNull(getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
                dialog.getStyleClass().add( "dialog" );

                alert.showAndWait();
            } else {
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Warning!" );
                alert.setHeaderText( "The student's age must be a number." );
                alert.setContentText( "Please try again." );

                DialogPane dialog = alert.getDialogPane();
                dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource( "/com/project/crud/styles/styles.css") ).toString() );
                dialog.getStyleClass().add( "dialog" );

                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Warning!" );
            alert.setHeaderText( "You must fill up everything before adding an entry." );
            alert.setContentText( "Please try again." );

            DialogPane dialog = alert.getDialogPane();
            dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource( "/com/project/crud/styles/styles.css") ).toString() );
            dialog.getStyleClass().add( "dialog" );

            alert.showAndWait();
        }

    }

//    @FXML
//    void addStudent() throws IOException {
//        boolean isFilledOut =
//                !studentIdField.getText().trim().isEmpty() &&
//                !firstNameField.getText().trim().isEmpty() &&
//                !lastNameField.getText().trim().isEmpty() &&
//                !yearLevelBox.getSelectionModel().isEmpty() &&
//                !ageField.getText().trim().isEmpty() &&
//                !genderBox.getSelectionModel().isEmpty() &&
//                !programBox.getSelectionModel().isEmpty();
//
//        if ( isFilledOut ) {
//            boolean studentIdIsNumber = checkIfNumber( studentIdField.getText().trim() );
//            boolean ageIsNumber = checkIfNumber( ageField.getText().trim() );
//
//            if ( studentIdIsNumber && ageIsNumber ) {
////                boolean isDuplicate = checkDuplicate( studentIdField.getText() );
//
//                if ( !isDuplicate ) {
//                    String studentId = studentIdField.getText().trim();
//                    String firstName = firstNameField.getText().trim();
//                    String lastName = lastNameField.getText().trim();
//                    String yearLevel = yearLevelBox.getValue();
//                    String age = ageField.getText().trim();
//                    String gender = genderBox.getValue();
//                    String program = programBox.getValue();
//                    String imagePath = "null";
//
//                    if ( addStudentImage.getImage().getUrl() != null ) {
//                        if ( Pattern.compile( "(\\w|\\d)+((\\.jpg)|(\\.jpeg)|(\\.png))" ).matcher( addStudentImage.getImage().getUrl() ).find() ) {
//                            imagePath = addStudentImage.getImage().getUrl();
//                        }
//                    }
//
//                    try {
//                        write = new BufferedWriter( new FileWriter( "database/students-list.txt", true ) );
//                        write.append( studentId + "&" + firstName + "&" + lastName + "&" + yearLevel + "&" + age + "&" + gender + "&" + program + "&" + imagePath );
//                        write.append( "\n" );
//                    } catch ( IOException err ) {
//                        System.err.println( "Warning! IOException has occurred at addStudent() function: " + err.getMessage() );
//                    } finally {
//                        if ( write != null ) write.close();
//                    }
//
////                    addStudents( getStudents() );
//                    closePane();
//
//                    Alert alert = new Alert( Alert.AlertType.INFORMATION );
//                    alert.setTitle( "Success!" );
//                    alert.setHeaderText( "You have successfully added a student." );
//
//                    DialogPane dialog = alert.getDialogPane();
//                    dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource( "/com/project/crud/styles/styles.css" ) ).toString() );
//                    dialog.getStyleClass().add( "dialog" );
//
//                    alert.showAndWait();
//                } else {
//                    Alert alert = new Alert( Alert.AlertType.WARNING );
//                    alert.setTitle( "Warning!" );
//                    alert.setHeaderText( "A duplicate entry has been found." );
//                    alert.setContentText( "You should edit the entry instead." );
//
//                    DialogPane dialog = alert.getDialogPane();
//                    dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource( "/com/project/crud/styles/styles.css" ) ).toString() );
//                    dialog.getStyleClass().add( "dialog" );
//
//                    alert.showAndWait();
//                }
//            } else if ( !studentIdIsNumber ) {
//                Alert alert = new Alert( Alert.AlertType.WARNING );
//                alert.setTitle( "Warning!" );
//                alert.setHeaderText( "The Student ID must be a number." );
//                alert.setContentText( "Please try again." );
//
//                DialogPane dialog = alert.getDialogPane();
//                dialog.getStylesheets().add( Objects.requireNonNull(getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
//                dialog.getStyleClass().add( "dialog" );
//
//                alert.showAndWait();
//            } else {
//                Alert alert = new Alert( Alert.AlertType.WARNING );
//                alert.setTitle( "Warning!" );
//                alert.setHeaderText( "The student's age must be a number." );
//                alert.setContentText( "Please try again." );
//
//                DialogPane dialog = alert.getDialogPane();
//                dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource( "/com/project/crud/styles/styles.css") ).toString() );
//                dialog.getStyleClass().add( "dialog" );
//
//                alert.showAndWait();
//            }
//        } else {
//            Alert alert = new Alert( Alert.AlertType.WARNING );
//            alert.setTitle( "Warning!" );
//            alert.setHeaderText( "You must fill up everything before adding an entry." );
//            alert.setContentText( "Please try again." );
//
//            DialogPane dialog = alert.getDialogPane();
//            dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource( "/com/project/crud/styles/styles.css") ).toString() );
//            dialog.getStyleClass().add( "dialog" );
//
//            alert.showAndWait();
//        }
//
//    }

    boolean checkDuplicate( String studentNumber ) throws IOException {
        boolean isDuplicate = false;

        try {
            read = new BufferedReader( new FileReader( "database/students-list.txt" ) );

            String s;
            while ( ( s = read.readLine() ) != null ) {
                if ( Pattern.compile( studentNumber ).matcher( s ).find() ) {
                    isDuplicate = true;
                    break;
                }
            }
        } catch( IOException err ) {
            System.err.println( "Warning! IOException has occurred at checkDuplicate() function: " + err.getMessage() );
        } finally {
            if ( read != null ) read.close();
        }

        return isDuplicate;
    }

    @FXML
    void editStudent() throws IOException {
        boolean success = false;

        try {
            read = new BufferedReader( new FileReader( "database/students-list.txt" ) );
            StringBuilder fileContent = new StringBuilder();

            boolean ageIsNumber = checkIfNumber( editAgeField.getText().trim() );

            if ( ageIsNumber ) {
                    String s;
                    while ((s = read.readLine()) != null) {
                        if (Pattern.compile(String.valueOf(selectedStudent.getStudentNumber())).matcher(s).find()) {
                            String firstName = (!editFirstNameField.getText().trim().isEmpty()) ? editFirstNameField.getText().trim() : selectedStudent.getFirstName();
                            String lastName = (!editLastNameField.getText().trim().isEmpty()) ? editLastNameField.getText().trim() : selectedStudent.getLastName();
                            String yearLevel = (!editYearLevelBox.getSelectionModel().isEmpty()) ? editYearLevelBox.getValue() : String.valueOf(selectedStudent.getYearLevel());
                            String age = (!editAgeField.getText().trim().isEmpty()) ? editAgeField.getText().trim() : String.valueOf(selectedStudent.getAge());
                            String gender = (!editGenderBox.getSelectionModel().isEmpty()) ? editGenderBox.getValue() : selectedStudent.getGender();
                            String program = (!editProgramBox.getSelectionModel().isEmpty()) ? editProgramBox.getValue() : selectedStudent.getProgram();

                            String image = (editStudentImage.getImage().getUrl() != null) ? editStudentImage.getImage().getUrl() : "null";

                            selectedStudent = new Student( selectedStudent.getStudentNumber(), firstName, lastName, Integer.parseInt( yearLevel ), Integer.parseInt( age ), gender, program , image );

                            fileContent.append( selectedStudent.getStudentNumber() + "&" + firstName + "&" + lastName + "&" + yearLevel + "&" + age + "&" + gender + "&" + program + "&" + image );
                            fileContent.append("\n");

                            continue;
                        }

                        fileContent.append(s);
                        fileContent.append("\n");
                    }

                    write = new BufferedWriter(new FileWriter("database/students-list.txt"));
                    write.write(fileContent.toString());

                    success = true;
            } else {
                Alert alert = new Alert( Alert.AlertType.WARNING );
                alert.setTitle( "Warning!" );
                alert.setHeaderText( "The student's age must be a number." );
                alert.setContentText( "Please try again." );

                DialogPane dialog = alert.getDialogPane();
                dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource( "/com/project/crud/styles/styles.css") ).toString() );
                dialog.getStyleClass().add( "dialog" );

                alert.showAndWait();
            }
        } catch ( IOException err ) {
            System.err.println( "Warning! IOException has occurred at editStudent() function: " + err.getMessage() );
        } finally {
            if ( read != null ) read.close();
            if ( write != null ) write.close();

            if ( success ) {
                searchStudents();
                closePane();

                Alert alert = new Alert( Alert.AlertType.INFORMATION );
                alert.setTitle( "Success!" );
                alert.setHeaderText( "You have successfully edited a student entry." );

                DialogPane dialog = alert.getDialogPane();
                dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource( "/com/project/crud/styles/styles.css") ).toString() );
                dialog.getStyleClass().add( "dialog" );

                alert.showAndWait();
            }
        }
    }

    @FXML
    void confirmDeleteStudent() throws IOException {
        boolean success = false;

        Alert confirm = new Alert( Alert.AlertType.CONFIRMATION );
        confirm.setTitle( "Confirmation Required" );
        confirm.setHeaderText( "Would you like to delete " + selectedStudent.getFirstName() + "'s entry?" );

        DialogPane dialog = confirm.getDialogPane();
        dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
        dialog.getStyleClass().add( "dialog" );

        Optional< ButtonType > result = confirm.showAndWait();
        if ( result.get() == ButtonType.OK) {
            try {
                read = new BufferedReader( new FileReader( "database/students-list.txt" ) );
                StringBuilder newFileContent = new StringBuilder();

                String s;
                while ( ( s = read.readLine() ) != null ) {
                    if ( Pattern.compile( String.valueOf( selectedStudent.getStudentNumber() ) ).matcher( s ).find() ) continue;

                    newFileContent.append( s );
                    newFileContent.append( "\n" );
                }

                write = new BufferedWriter( new FileWriter( "database/students-list.txt" ) );
                write.write( newFileContent.toString() );

                success = true;
            } catch ( IOException err ) {
                System.err.println( "Warning! IOException has occurred at confirmDeleteStudent() function: " + err.getMessage() );
            } finally {
                if ( read != null ) read.close();
                if ( write != null ) write.close();

                if ( success ) {
                    List< Student > updatedList = new ArrayList<>( students );
                    updatedList.remove( selectedStudent );
                    addStudents( updatedList );
                    closePane();
                    selectedStudent = null;

                    Alert alert = new Alert( Alert.AlertType.INFORMATION );
                    alert.setTitle( "Success!" );
                    alert.setHeaderText( "You have successfully deleted a student entry." );

                    DialogPane dialogSuccess = alert.getDialogPane();
                    dialogSuccess.getStylesheets().add( Objects.requireNonNull( getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
                    dialogSuccess.getStyleClass().add( "dialog" );

                    alert.showAndWait();
                }
            }
        }
    }

    @FXML
    void searchStudents() throws IOException {
        List< Student > students = new ArrayList<>();

        if ( !searchField.getText().trim().isEmpty() ) {
            Task< Void > searchStudentsTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {

                    try {
                        read = new BufferedReader(new FileReader("database/students-list.txt"));
                        String pattern = searchField.getText().replaceAll(" +", "|");
                        String s;
                        while ((s = read.readLine()) != null) {
                            if (!Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(s).find()) continue;

                            String[] entry = s.split("&");
                            students.add(new Student(Integer.parseInt(entry[0]), entry[1], entry[2], Integer.parseInt(entry[3]), Integer.parseInt(entry[4]), entry[5], entry[6], entry[7]));
                        }

                            addStudents(students);
                        } catch(IOException err ){
                            System.err.println("Warning! IOException has occurred at searchStudents() function: " + err.getMessage());
                        } finally{
                            if (read != null) read.close();
                        }

                        return null;
                    }
                };

            searchStudentsTask.run();
        } else {
            addStudents( getStudents() );
        }
    }

    @FXML
    void sortStudents() throws IOException {
        List< Student > currentList = new ArrayList<>();
        currentList.addAll( students );
        addStudents( currentList );
    }

    @FXML
    void closePane() {
        studentListPane.toFront();
        addButton.getStyleClass().clear();
        editButton.getStyleClass().clear();
        deleteButton.getStyleClass().clear();
        addButton.getStyleClass().add( "button" );
        editButton.getStyleClass().add( "button" );
        deleteButton.getStyleClass().add( "button" );
    }

    @FXML
    void openAddStudentPane() {
        studentListPane.toFront();
        addStudentPane.toFront();

        addButton.getStyleClass().clear();
        editButton.getStyleClass().clear();
        deleteButton.getStyleClass().clear();
        addButton.getStyleClass().add( "button-selected" );
        editButton.getStyleClass().add( "button" );
        deleteButton.getStyleClass().add( "button" );

        addStudentImage.setImage( new Image( Objects.requireNonNull( this.getClass().getResourceAsStream("/com/project/crud/images/no-image.png") ) ) );
        studentIdField.setText( "" );
        firstNameField.setText( "" );
        lastNameField.setText( "" );
        yearLevelBox.getSelectionModel().select( 0 );
        ageField.setText( "" );
        genderBox.getSelectionModel().select( 0 );
        programBox.getSelectionModel().select( 0 );
    }

    @FXML
    void openEditStudentPane() {
        if ( selectedStudent != null ) {
            studentListPane.toFront();
            editStudentPane.toFront();

            addButton.getStyleClass().clear();
            editButton.getStyleClass().clear();
            deleteButton.getStyleClass().clear();
            addButton.getStyleClass().add( "button" );
            editButton.getStyleClass().add( "button-selected" );
            deleteButton.getStyleClass().add( "button" );

            editStudentImage.setImage( null );
            try {
                editStudentImage.setImage(new Image(selectedStudent.getImagePath()));
            } catch ( IllegalArgumentException | NullPointerException err ) {
                System.err.println( "Warning! IllegalArgumentException/NullPointerException has occurred at openEditStudentPane() function: " + err.getMessage() );
            } finally {
                if ( selectedStudent.getImagePath().equals( "null" ) || editStudentImage.getImage().isError() ) {
                    if ( selectedStudent.getGender().equals( "Male" ) ) editStudentImage.setImage( new Image( Objects.requireNonNull( this.getClass().getResourceAsStream("/com/project/crud/images/male-student.png" ) ) ) );
                    else editStudentImage.setImage( new Image( Objects.requireNonNull( this.getClass().getResourceAsStream("/com/project/crud/images/female-student.png" ) ) ) );
                }
            }

            editStudentIdLabel.setText( String.valueOf( selectedStudent.getStudentNumber() ) );
            editFirstNameField.setText( selectedStudent.getFirstName() );
            editLastNameField.setText( selectedStudent.getLastName() );
            editYearLevelBox.getSelectionModel().select( String.valueOf( selectedStudent.getYearLevel() ) );
            editAgeField.setText( String.valueOf( selectedStudent.getAge() ) );
            editGenderBox.getSelectionModel().select( selectedStudent.getGender() );
            editProgramBox.getSelectionModel().select( selectedStudent.getProgram() );
        } else {
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Warning!" );
            alert.setHeaderText( "You must choose a student first before opening the editing interface." );

            DialogPane dialog = alert.getDialogPane();
            dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
            dialog.getStylesheets().add( getClass().getResource( "/com/project/crud/styles/styles.css" ).toString() );
            dialog.getStyleClass().add( "dialog" );

            alert.showAndWait();
        }
    }

    @FXML
    void openDeleteStudentPane() {
        if ( selectedStudent != null ) {
            studentListPane.toFront();
            deleteStudentPane.toFront();

            addButton.getStyleClass().clear();
            editButton.getStyleClass().clear();
            deleteButton.getStyleClass().clear();
            addButton.getStyleClass().add( "button" );
            editButton.getStyleClass().add( "button" );
            deleteButton.getStyleClass().add( "button-selected" );

            deleteStudentImage.setImage( null );

            deleteStudentImage.setImage( null );
            try {
                deleteStudentImage.setImage(new Image(selectedStudent.getImagePath()));
            } catch ( IllegalArgumentException | NullPointerException err ) {
                System.err.println( "Warning! IllegalArgumentException/NullPointerException has occurred at openEditStudentPane() function: " + err.getMessage() );
            } finally {
                if ( selectedStudent.getImagePath().equals( "null" ) || deleteStudentImage.getImage().isError() ) {
                    if ( selectedStudent.getGender().equals( "Male" ) ) deleteStudentImage.setImage( new Image( Objects.requireNonNull( this.getClass().getResourceAsStream("/com/project/crud/images/male-student.png" ) ) ) );
                    else deleteStudentImage.setImage( new Image( Objects.requireNonNull( this.getClass().getResourceAsStream("/com/project/crud/images/female-student.png" ) ) ) );
                }
            }

            deleteStudentIdText.setText( String.valueOf( selectedStudent.getStudentNumber() ) );
            deleteFirstNameText.setText( selectedStudent.getFirstName() );
            deleteLastNameText.setText( selectedStudent.getLastName() );
            deleteYearLevelText.setText( String.valueOf( selectedStudent.getYearLevel() ) );
            deleteAgeText.setText( String.valueOf( selectedStudent.getAge() ) );
            deleteGenderText.setText( selectedStudent.getGender() );
            deleteProgramText.setText( selectedStudent.getProgram() );
        } else {
            Alert alert = new Alert( Alert.AlertType.WARNING );
            alert.setTitle( "Warning!" );
            alert.setHeaderText( "You must choose a student first before opening the delete interface." );

            DialogPane dialog = alert.getDialogPane();
            dialog.getStylesheets().add( Objects.requireNonNull(getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
            dialog.getStylesheets().add( getClass().getResource( "/com/project/crud/styles/styles.css" ).toString() );
            dialog.getStyleClass().add( "dialog" );

            alert.showAndWait();
        }
    }

    @FXML
    void onClickedMouse( MouseEvent event ) {
        xOffset = windowProgram.getScene().getWindow().getX() - event.getScreenX();
        yOffset = windowProgram.getScene().getWindow().getY() - event.getScreenY();
    }

    @FXML
    void onDraggedMouse( MouseEvent event ) {
        windowProgram.getScene().getWindow().setX( event.getScreenX() + xOffset );
        windowProgram.getScene().getWindow().setY( event.getScreenY() + yOffset );
    }

    @FXML
    void closeProgram() {
        Alert confirm = new Alert( Alert.AlertType.CONFIRMATION );
        confirm.setTitle( "Confirmation Required" );
        confirm.setHeaderText( "You are about to close the program.");

        DialogPane dialog = confirm.getDialogPane();
        dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
        dialog.getStylesheets().add( getClass().getResource( "/com/project/crud/styles/styles.css" ).toString() );
        dialog.getStyleClass().add( "dialog" );

        Optional< ButtonType > result = confirm.showAndWait();
        if ( result.get() == ButtonType.OK ) {
            System.exit( 0 );
        }
    }

    @FXML
    void minimizeProgram() {
        Stage stage = ( Stage ) windowProgram.getScene().getWindow();
        stage.setIconified( true );
    }

    boolean checkIfNumber( String input ) {
        try {
            Integer.parseInt( input );
            return true;
        } catch ( NumberFormatException err ) {
            return false;
        }
    }

    @FXML
    void addStudentHelp() {
        Alert info = new Alert( Alert.AlertType.INFORMATION );
        info.setTitle( "Add a Student" );
        info.setHeaderText( "Adding a student is easy,");
        info.setContentText( "Simply click on the \"Add a Student\" button on the left side or press CTRL + A in your keyboard." );

        DialogPane dialog = info.getDialogPane();
        dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
        dialog.getStylesheets().add( getClass().getResource( "/com/project/crud/styles/styles.css" ).toString() );
        dialog.getStyleClass().add( "dialog" );

        info.showAndWait();
    }

    @FXML
    void editStudentHelp() {
        Alert info = new Alert( Alert.AlertType.INFORMATION );
        info.setTitle( "Edit a Student" );
        info.setHeaderText( "Before editing a student,");
        info.setContentText( "You must click on a student first and simply click on the \"Edit a Student\" button on the left side or press CTRL + E in your keyboard." );

        DialogPane dialog = info.getDialogPane();
        dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
        dialog.getStylesheets().add( getClass().getResource( "/com/project/crud/styles/styles.css" ).toString() );
        dialog.getStyleClass().add( "dialog" );

        info.showAndWait();
    }

    @FXML
    void deleteStudentHelp() {
        Alert info = new Alert( Alert.AlertType.INFORMATION );
        info.setTitle( "Delete a Student" );
        info.setHeaderText( "Before deleting a student,");
        info.setContentText( "You must click on a student first and simply click on the \"Delete a Student\" button on the left side or press CTRL + D in your keyboard." );

        DialogPane dialog = info.getDialogPane();
        dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
        dialog.getStylesheets().add( getClass().getResource( "/com/project/crud/styles/styles.css" ).toString() );
        dialog.getStyleClass().add( "dialog" );

        info.showAndWait();
    }

    @FXML
    void whatIsThis() {
        Alert info = new Alert( Alert.AlertType.INFORMATION );
        info.setTitle( "What is this?" );
        info.setHeaderText( "This is a dashboard application, made in JavaFX that allows you to easily manage student information.");

        DialogPane dialog = info.getDialogPane();
        dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
        dialog.getStylesheets().add( getClass().getResource( "/com/project/crud/styles/styles.css" ).toString() );
        dialog.getStyleClass().add( "dialog" );

        info.showAndWait();
    }

    @FXML
    void programVersion() {
        Alert info = new Alert( Alert.AlertType.INFORMATION );
        info.setTitle( "Program Version" );
        info.setHeaderText( "Version 1.0 – Revision 2");
        info.setContentText( "Made by Team Positive.");

        DialogPane dialog = info.getDialogPane();
        dialog.getStylesheets().add( Objects.requireNonNull( getClass().getResource("/com/project/crud/styles/styles.css") ).toString() );
        dialog.getStylesheets().add( getClass().getResource( "/com/project/crud/styles/styles.css" ).toString() );
        dialog.getStyleClass().add( "dialog" );

        info.showAndWait();
    }
}