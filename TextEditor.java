package scheme_reader_eclipse;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fxmisc.richtext.StyleClassedTextArea;


import org.fxmisc.flowless.VirtualizedScrollPane;
import java.util.Collections;
import java.util.StringTokenizer;

public class TextEditor extends Application {
	
	private String[] Special_Names = {"define", "if", "quote", "'", "lambda", "cond"};
	private String delim = "';() \n";
	private String[] colors = {"red", "orange", "green", "blue"};
    public ArrayList<String> global_open_tabs_paths = new ArrayList<String>();
    
    public static void main(String[] args) {
            launch(args);
    }
    private void save(String name, String context){
        FileWriter fWriter = null;
        BufferedWriter writer = null;
        try {
            File path = new File(name + ".scm");
            fWriter = new FileWriter(path);
            writer = new BufferedWriter(fWriter);
            writer.write(context);
            writer.close(); //make sure you close the writer object 
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    private String open(String path){
        BufferedReader br = null;
        FileReader fr = null;
        String context = "";
        try{
            fr = new FileReader(path+".scm");
            br = new BufferedReader(fr);
            String sCurrentLine = "";
            while(sCurrentLine != null){
                context += sCurrentLine + "\n";
                sCurrentLine = br.readLine();
            }
        } catch (IOException error){
            error.printStackTrace();
        } finally {
            try {
                if (br != null){
                    br.close();
                }
                if (fr != null){
                    fr.close();
                }
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return context.substring(1);
    }
    private void keyAction(StyleClassedTextArea editor) {
		StringTokenizer st = new StringTokenizer(editor.getText(), delim, true);
		int count = 0;
		while(st.hasMoreTokens()) {
			String here = st.nextToken();
			count += here.length();
			for(int i = 0; i < Special_Names.length; i++) {
				if(here.equals(Special_Names[i])) {
					editor.setStyle(count-here.length(), count, Collections.singleton("special"));
				}
			}
			if(here.equals("(")) {
				editor.setStyle(count-here.length(), count, Collections.singleton(colors[0]));
				String v = colors[0];
				for (int i2 = 0; i2 < colors.length-1; ++i2) {
					colors[i2] = colors[i2+1];
				}
				colors[colors.length-1] = v;
			} else if(here.equals(")")) {
				editor.setStyle(count-here.length(), count, Collections.singleton(colors[colors.length-1]));
				String v = colors[colors.length-1];
				for (int i2 = colors.length-2; i2 >= 0; --i2) {
					colors[i2+1] = colors[i2];
				}
				colors[0] = v;
			} else if(here.equals(";")) {
				editor.setStyle(count-here.length(), count, Collections.singleton("comment"));
				int start = count;
				while(st.hasMoreTokens()) {
					here = st.nextToken();
					count += here.length();
					if(here.equals("\n")) {
						break;
					}else {
						editor.setStyle(start, count, Collections.singleton("comment"));
					}
				}
			}
		}
    }
    private EventHandler<ActionEvent> fn_event(){
        return new EventHandler<ActionEvent>(){
            public void handle(ActionEvent event){
                System.out.println(global_open_tabs_paths.toString());
                MenuItem mItem = (MenuItem) event.getSource();
                String option = mItem.getText();
                if (option.equals("Save")){
                    Stage saveStage = new Stage();
                    VBox root = new VBox();
                    Path rel_p = Paths.get("");
                    String abs_p = rel_p.toAbsolutePath().toString();
                    Tab thisTab = (Tab) mItem.getUserData();
                    VirtualizedScrollPane<StyleClassedTextArea> context = (VirtualizedScrollPane<StyleClassedTextArea>) thisTab.getContent();
                    TextField name = new TextField(thisTab.getText());
                    TextField path = new TextField(abs_p);
                    Button saveButton = new Button("Save");
                    saveButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override public void handle(ActionEvent e){
                            if(name.getText().equals("untitled")){
                                System.out.println("ERROR! Cannot save file as untitled.");
                                System.out.println(global_open_tabs_paths.toString());
                                return; // file name cannot be called untitiled
                            }
                            for (int i = 0; i < global_open_tabs_paths.size(); i++){
                                if(name.getText().equals(global_open_tabs_paths.get(i))){
                                    if(!name.getText().equals(thisTab.getText())){
                                        System.out.println("ERROR! Cannot save file as name of another opened file!");
                                        System.out.println(global_open_tabs_paths.toString());
                                        return;
                                    }
                                }
                            }
                            String total_path = path.getText()+"\\"+ name.getText();
                            if (thisTab.getText().equals("untitled")){
                                global_open_tabs_paths.add(name.getText());
                            } else{
                                global_open_tabs_paths.remove(thisTab.getText());
                                global_open_tabs_paths.add(name.getText());
                            }
                            thisTab.setText(name.getText());
                            save(total_path, context.getContent().getText());
                            saveStage.close();
                        }
                    });
                    root.getChildren().addAll(name, path, saveButton);
                    Scene scene = new Scene(root, 300, 100);
                    scene.getStylesheets().add(this.getClass().getResource("/OpenStylesheet.css").toExternalForm());
                    saveStage.setTitle("Save");
                    saveStage.setScene(scene);
                    saveStage.show();
                } else if(option.equals("Open")){
                    Stage openStage = new Stage();
                    VBox root = new VBox();
                    Path rel_p = Paths.get("");
                    String abs_p = rel_p.toAbsolutePath().toString();
                    Object[] data = (Object[]) mItem.getUserData();
                    TabPane thisTabPane = (TabPane) data[0];
                    MenuItem savePointer = (MenuItem) data[1];
                    TextField name = new TextField();
                    TextField path = new TextField(abs_p);
                    Button openButton = new Button("Open");
                    openButton.setOnAction(new EventHandler<ActionEvent>(){
                        @Override public void handle(ActionEvent e){
                            String total_path = path.getText()+"\\"+name.getText();
                            for(int i = 0; i < global_open_tabs_paths.size(); i++){
                                if(name.getText().equals(global_open_tabs_paths.get(i))){
                                    System.out.println("ERROR! File is already opened!");
                                    System.out.println(global_open_tabs_paths.toString());
                                    //error out
                                    return;
                                }
                            }
                            String context = open(total_path);
                            global_open_tabs_paths.add(name.getText());
                            Tab newTab = new Tab();
                            newTab.setText(name.getText());
                            newTab.setOnClosed(new EventHandler<javafx.event.Event>() {
                                @Override
                                public void handle(javafx.event.Event e) {
                                    if (!newTab.getText().equals("untitled")){
                                        System.out.println(global_open_tabs_paths.toString());
                                        global_open_tabs_paths.remove(newTab.getText());
                                        System.out.println(global_open_tabs_paths.toString());
                                    }
                                 }
                            });
                            StyleClassedTextArea  newTextArea = new StyleClassedTextArea();
                            newTextArea.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                            	@Override
                            	public void handle(KeyEvent e) {
                            		newTextArea.clearStyle(newTextArea.getCaretPosition()-1, newTextArea.getCaretPosition());
                            		if(e.getCode().compareTo(KeyCode.ENTER) == 0)
                            			keyAction(newTextArea);
                            	}
                            });
                            newTextArea.insertText(0, context);
                            keyAction(newTextArea);
                            VirtualizedScrollPane<StyleClassedTextArea> newPane = new VirtualizedScrollPane<>(newTextArea);
                            newTab.setContent(newPane);
                            thisTabPane.getTabs().add(newTab);
                            thisTabPane.getSelectionModel().select(newTab);
                            openStage.close();
                        }
                    });
                    root.getChildren().addAll(name, path, openButton);
                    Scene scene = new Scene(root, 300, 100);
                    scene.getStylesheets().add(this.getClass().getResource("/OpenStylesheet.css").toExternalForm());
                    openStage.setTitle("Open");
                    openStage.setScene(scene);
                    openStage.show();
                } else if(option.equals("New")){
                    Object[] data = (Object[]) mItem.getUserData();
                    TabPane thisTabPane = (TabPane) data[0];
                    MenuItem savePointer = (MenuItem) data[1];
                    Tab newTab = new Tab();
                    newTab.setOnClosed(new EventHandler<javafx.event.Event>() {
                        @Override
                        public void handle(javafx.event.Event e) {
                            if (!newTab.getText().equals("untitled")){
                                System.out.println(global_open_tabs_paths.toString());
                                global_open_tabs_paths.remove(newTab.getText());
                            }
                         }
                    });
                    newTab.setText("untitled");
                    StyleClassedTextArea  newTextArea = new StyleClassedTextArea ();
                    newTextArea.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                    	@Override
                    	public void handle(KeyEvent e) {
                    		newTextArea.clearStyle(newTextArea.getCaretPosition()-1, newTextArea.getCaretPosition());
                    		if(e.getCode().compareTo(KeyCode.ENTER) == 0)
                    			keyAction(newTextArea);
                    	}
                    });
                    VirtualizedScrollPane<StyleClassedTextArea> newPane = new VirtualizedScrollPane<>(newTextArea);
                    newTab.setContent(newPane);
                    thisTabPane.getTabs().add(newTab);
                    thisTabPane.getSelectionModel().select(newTab);
                } 
            }
        };
    }
    @Override
    public void start(Stage primaryStage) { 
        VBox root = new VBox();
        HBox ribbon = new HBox();
        MenuItem New = new MenuItem("New");
        MenuItem Open = new MenuItem("Open");
        MenuItem Save = new MenuItem("Save");
        EventHandler<ActionEvent> fn = fn_event();
        New.setOnAction(fn);
        Save.setOnAction(fn);
        Open.setOnAction(fn);
        MenuButton File = new MenuButton("File", null, New, Open, Save);
        ribbon.getChildren().add(File);
        TabPane tabPane = new TabPane();
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
                Save.setUserData(newTab);
            }
        });
        Tab tab = new Tab();
        tab.setOnClosed(new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {
                if (!tab.getText().equals("untitled")){
                    System.out.println(global_open_tabs_paths.toString());
                    global_open_tabs_paths.remove(tab.getText());
                }
             }
        });
        tab.setText("untitled");
        StyleClassedTextArea mainA = new StyleClassedTextArea ();
        mainA.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
        	@Override
        	public void handle(KeyEvent e) {
        		mainA.clearStyle(mainA.getCaretPosition()-1, mainA.getCaretPosition());
        		if(e.getCode().compareTo(KeyCode.ENTER) == 0)
        			keyAction(mainA);
        	}
        });
        VirtualizedScrollPane<StyleClassedTextArea> vsPane = new VirtualizedScrollPane<>(mainA);
        tab.setContent(vsPane);
        tabPane.getTabs().add(tab);
        Save.setUserData(tab);
        Object[] openData = {tabPane, Save};
        Open.setUserData(openData);
        New.setUserData(openData);
        root.getChildren().addAll(ribbon, tabPane);
        Scene scene = new Scene(root, 600, 600);
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {mainA.setPrefHeight(newVal.doubleValue()-20); tabPane.setPrefHeight(newVal.doubleValue());});
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {mainA.setPrefWidth(newVal.doubleValue()-20); tabPane.setPrefWidth(newVal.doubleValue());});
        scene.getStylesheets().add(this.getClass().getResource("/stylesheet.css").toExternalForm());
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(600);
        mainA.setPrefHeight(primaryStage.getHeight());
        tabPane.setPrefHeight(primaryStage.getHeight());
        primaryStage.setTitle("Scheme Text Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}