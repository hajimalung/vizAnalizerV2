/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vizanalyzerv2;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;

/**
 * FXML Controller class
 *
 * @author KH2183
 */
public class VizAnalyzerUIController implements Initializable {

    @FXML
    private TextField projectPathField;
    @FXML
    private Button browseButton;
    @FXML
    private Button scanProjectButton;
    @FXML
    private TextArea console;
    @FXML
    private AnchorPane mainPanel;
    @FXML
    private Accordion optionsAccordian;
    @FXML
    private TextField searchBoxField;
    @FXML
    private TextArea skinCode;
    @FXML
    private ChoiceBox<String> deviceSelector;
    @FXML
    private TitledPane formsPane;
    @FXML
    private ListView<String> formsListView;
    @FXML
    private ListView<String> widgetImageList;
    @FXML
    private Label lblFormName;
    
    private int logLineNumber;
    private ProjectHandler vizProject;
    @FXML
    private Button exportButton;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logLineNumber = 0;
        vizProject = null;
        logEntry("tool initialized!!!!");
    }    

    @FXML
    private void browseButtonOnClickAction(ActionEvent event) {
        logEntry("browsing project!!");
        File projectDir = browseProject();
        if(!isFileNull(projectDir)){
            logEntry("selected project is : "+projectDir.getAbsolutePath());            
            try{
                vizProject = new ProjectHandler(projectDir);
                projectPathField.setText(projectDir.getAbsolutePath());
                final String[] deviceChoices = new String[]{"desktop","mobile","tablet","watch"};
                deviceSelector.setItems(FXCollections.observableArrayList(deviceChoices));
                deviceSelector.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>(){
                    @Override
                    public void changed(ObservableValue observable, Number oldValue, Number newValue) {
                        try {
                            
                            vizProject.setSelectedDevice(deviceChoices[newValue.intValue()]);
                            
                            logEntry("selected device :"+deviceChoices[newValue.intValue()]);
                            scanProjectButton.disableProperty().set(false);
                        } catch (Exception ex) {
                            logEntry(ex.toString());
                            showAlert(ex.getMessage());
                        }
                    }    
            });
            deviceSelector.disableProperty().set(false); 
            }catch(Exception prjectException){
                logEntry(prjectException.toString());
                showAlert(prjectException.getMessage());
            }
            
        }else{
            projectPathField.setText("");
            logEntry("nothing selected!!!");
            showAlert("Project Not Selected!!!!");
        }
    }
    private boolean isFileNull(File f){
        return (f == null);
    }
     private File browseProject()
    {
        File f;
        DirectoryChooser dirWindow=new DirectoryChooser();
        f=dirWindow.showDialog(null);
        return f;
    }
    @FXML
    private void scanProjectOnCLickAction(ActionEvent event) {
        logEntry("scanning project...");
        try {
            List formsList = vizProject.scanForForms();
            
            ObservableList<String> formItems =FXCollections.observableArrayList ();
            
            for (Iterator it = formsList.iterator(); it.hasNext();) {
                String formName = (String) it.next();
                formItems.add(formName);
            }
            
            formsListView.setItems(formItems);
            
            formsListView.setOnMouseClicked(new EventHandler<MouseEvent>(){
                @Override
                public void handle(MouseEvent event) {
                 
                    logEntry("clicked on :"+formsListView.getSelectionModel().getSelectedItem());
                    logEntry("scanning for images in form!!!!");
                    try {
                        HashMap<String,String> widgetImageMap = vizProject.scanForImagesInForm(formsListView.getSelectionModel().getSelectedItem());
                        
                        ObservableList<String> widgetImageitems =FXCollections.observableArrayList ();
                        if(widgetImageMap.entrySet().isEmpty()){
                            showAlert("there are no images in this form!!!!!");
                            return;
                        }
                        Iterator it = widgetImageMap.entrySet().iterator();
                        while(it.hasNext()){
                            Map.Entry wIpair = (Map.Entry) it.next();
                            widgetImageitems.add(wIpair.getKey()+"  -->  "+wIpair.getValue());
                        }
                        
                        widgetImageList.setItems(widgetImageitems);
                        widgetImageList.disableProperty().set(false);
                        
                        lblFormName.setText(formsListView.getSelectionModel().getSelectedItem());
                        
                        
                    } catch (Exception ex) {
                        Logger.getLogger(VizAnalyzerUIController.class.getName()).log(Level.SEVERE, null, ex);
                        logEntry(ex.toString());
                        showAlert("something went wrong while scanning!!!");
                    }
                }
            });            
            optionsAccordian.disableProperty().set(false);
            optionsAccordian.setExpandedPane(formsPane);
            
            logEntry("forms list generated!!!!");
            
        } catch (Exception ex) {
            Logger.getLogger(VizAnalyzerUIController.class.getName()).log(Level.SEVERE, null, ex);
            logEntry(ex.toString());
            showAlert("something went wrong while scanning!!!");
        }
    }

    @FXML
    private void searchingAction(KeyEvent event) {
    }
    private void logEntry(String logMessage){
        logLineNumber++;
        console.appendText("\n"+logLineNumber+". "+new Date()+" ---->"+logMessage);
    }
    private void showAlert(String message)
    {
        Alert myAlert=new Alert(Alert.AlertType.INFORMATION,message,ButtonType.OK);
        myAlert.show();
    }

    @FXML
    private void exportToFileClicked(ActionEvent event) {
        try {
                    logEntry("Export to file started!!!!");
                    File exportToDirectory = browseProject();
                    
                    if(isFileNull(exportToDirectory)){
                        logEntry("nothing selected to export!!!");
                        showAlert("directory Not Selected!!!!");
                        return;
                    }
                    
            String htmlContent = "<html>\n" +
"<head>\n" +
"	<title></title>\n" +
"	<style type=\"text/css\">\n" +
"		table{\n" +
"\n" +
"		}\n" +
"		tr{\n" +
"\n" +
"		}\n" +
"		td{\n" +
"			padding: 5px;\n" +
"		}\n" +
"	</style>\n" +
"</head>\n" +
"<body>\n" +
"	<h4>Forms, Resources Mapping</h4>\n" +
"	<table border=2>";
            List<String> formsList=vizProject.scanForForms();
            for (Iterator it = formsList.iterator(); it.hasNext();) {
                String formName = (String) it.next();
                        htmlContent += "<tr><td><b>"+formName+"</b></td></tr>";        
                HashMap<String,String> widgetImageMap = vizProject.scanForImagesInForm(formName);
                Iterator it2 = widgetImageMap.entrySet().iterator();
                        while(it2.hasNext()){
                            Map.Entry wIpair = (Map.Entry) it2.next();
                            htmlContent += "<tr><td>"+wIpair.getKey()+"</td><td>"+wIpair.getValue()+"</td></tr>";
                        }
            }
            htmlContent += "</table>\n" +
"</body>\n" +
"</html>";
            
            File exportToFile = new File(exportToDirectory.getPath()+File.separator+"FormResourceMappings.html");
            
            FileWriter fileExporter = new FileWriter(exportToFile);
            fileExporter.write(htmlContent);            
            fileExporter.close();
            
            showAlert("file exported in :"+exportToFile.getPath());
            logEntry("file exported in :"+exportToFile.getPath());
        } catch (Exception ex) {
            Logger.getLogger(VizAnalyzerUIController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(ex.toString());
            logEntry(ex.toString());
        }
        
    }
    
}
