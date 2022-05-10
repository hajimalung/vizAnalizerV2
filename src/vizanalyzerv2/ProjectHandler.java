/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vizanalyzerv2;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author KH2183
 */
public class ProjectHandler {

    private String projectDirectoryPath;
    private File projectDirectory;
    private String selectedDevice;
    private HashMap<String, String> formNamePathMap;

    public final String PROJECT_PROPERTIES_FILE = File.separator + "projectproperties.json";
    public final String RESOURCES_DIRECTORY = File.separator + "resources";
    public final String FORMS_DIRECTORY = File.separator + "forms";
    public final String TEMPLATES_DIRECTORY = File.separator + "templates";
    private final String SEGMENTS_DIRECTORY = File.separator + "segments";

    public final String WIDGETTYPE_IMAGE = "Image";
    public final String WIDGETTYPE_SEGMENT = "Segment";

    public ProjectHandler() {
        projectDirectoryPath = null;
    }

    public ProjectHandler(File ProjectDir) throws Exception {
        this.projectDirectoryPath = ProjectDir.getAbsolutePath();
        if (isVisualizerProject(ProjectDir)) {
            this.projectDirectory = ProjectDir;
        } else {
            throw new Exception(projectDirectoryPath + "  is not a valid visualizer project!!!!");
        }
    }

    private boolean isVisualizerProject(File projectDirectory) {
        return isFileExist(projectDirectory.getPath() + this.PROJECT_PROPERTIES_FILE);
    }

    public void setSelectedDevice(String selectedDeviceType) throws Exception {
        System.out.println("selected device is:" + selectedDeviceType);
        this.selectedDevice = File.separator + selectedDeviceType;
    }

    public List<String> scanForForms() throws Exception {
        String searchFormInDir = projectDirectoryPath + this.FORMS_DIRECTORY + this.selectedDevice;
        if (isFileExist(searchFormInDir)) {
            File formsDirectory = new File(searchFormInDir);
            return getFormsList(formsDirectory);
        } else {
            throw new Exception("resources not found or project got curropted!!!!");
        }
    }

    private ArrayList<String> getFormsList(File formsDir) throws Exception {
        ArrayList<String> formsList = new ArrayList<String>();
        formNamePathMap = new HashMap<>();
        if (formsDir.isDirectory()) {
            for (File tempFile : formsDir.listFiles()) {
                formNamePathMap.put(tempFile.getName(), tempFile.getPath());
                formsList.add(tempFile.getName());
            }
        } else {
            throw new Exception("not a valid forms directory!!!!");
        }
        return formsList;
    }

    private boolean isFileExist(String filePath) {
        return (new File(filePath).exists());
    }

    public HashMap<String, String> scanForImagesInForm(String formName) throws Exception {
        System.out.println("scanning in form :" + formName);
        String formDirPath = formNamePathMap.get(formName);
        HashMap<String, String> widgetAndImageMapper = new HashMap<>();
        if (formDirPath != null) {
            File formDir = new File(formDirPath);
            if (formDir.isDirectory()) {
                for (File widgetJSONFile : formDir.listFiles()) {
                    String widgetJSONContent = readFromFile(widgetJSONFile);
                    JSONObject widgetJSONObject = new JSONObject(widgetJSONContent);
                    if (widgetJSONObject.getString("wType").equals(this.WIDGETTYPE_IMAGE)) {
                        System.out.println("image widget found!!!!!");
                        String imageName = widgetJSONObject.getString("_src_");
                        System.out.println(widgetJSONFile.getName() + " ----> " + imageName);
                        widgetAndImageMapper.put(widgetJSONFile.getName(), imageName);
                    } else if (widgetJSONObject.getString("wType").equals(this.WIDGETTYPE_SEGMENT)) {

                        HashMap<String,String> imagesInTemplate = scanForImagesInTemplate(widgetJSONObject.getString("rowtemplate"));
                            Iterator it = imagesInTemplate.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry)it.next();
                                
                                widgetAndImageMapper.put(widgetJSONFile.getName()+"(segment) ->"+pair.getKey()+" ( in template ) " , pair.getValue() + "(image)");
                                
                                it.remove(); // avoids a ConcurrentModificationException
                            }

                    } else {
                        //this is not an imag widget so start next iteration
                        continue;
                    }
                }
                return widgetAndImageMapper;
            } else {
                throw new Exception(formName + "is not a directory!!!");
            }
        } else {
            throw new Exception("invalid form!!!");
        }
    }

    private HashMap scanForImagesInTemplate(String templateMainFlexId) throws Exception {
        System.out.println("scanning in template for Images!!!!!: " + templateMainFlexId);
        HashMap<String,String> templateWidgetImageMap = new HashMap<>();
        String templateDirPath = this.projectDirectoryPath + this.TEMPLATES_DIRECTORY + this.selectedDevice + this.SEGMENTS_DIRECTORY;
        boolean foundTemplateTobeSearched = false;
        File templatesDir = new File(templateDirPath);
        if (templatesDir.isDirectory()) {
                for(File templateItem : templatesDir.listFiles()){
                        if(templateItem.isDirectory() && !foundTemplateTobeSearched){
                            for(File widgetInTemplate:templateItem.listFiles()){
                                if(widgetInTemplate.getName().equals(templateMainFlexId+".json")){
                                    foundTemplateTobeSearched = true;
                                    templateDirPath += File.separator+ templateItem.getName();
                                    break;
                                }
                            }
                        }else{
                            break;
                        }                    
                }
                if(foundTemplateTobeSearched){
                    File templateTobeSearchedDir = new File(templateDirPath);
                        for(File widgetItem:templateTobeSearchedDir.listFiles()){
                            String widgetContent = readFromFile(widgetItem);
                            JSONObject widgetJSON = new JSONObject(widgetContent);
                            if(widgetJSON.getString("wType").equals(this.WIDGETTYPE_IMAGE)){
                                templateWidgetImageMap.put(widgetItem.getName(), widgetJSON.getString("_src_"));
                            }
                        }
                        return templateWidgetImageMap;
                    }
        } else {
            throw new Exception("segment templates not found!!!!");
        }
        return templateWidgetImageMap;
    }
    private String readFromFile(File fileToRead) throws Exception {
        if (fileToRead.isDirectory()) {
            throw new Exception("cant read " + fileToRead.getName());
        }
        String fileContent = "";
        FileReader fileReader = new FileReader(fileToRead);
        int charInFile = fileReader.read();
        while (charInFile != -1) {
            fileContent += (char) charInFile;
            charInFile = fileReader.read();
        }
        return fileContent;
    }
}