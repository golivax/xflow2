package br.usp.ime.lapessc.xflow2.core.processors.callgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;

public class StructuralCouplingIdentifier {

	public static List<StructuralDependency> calcStructuralCoupling(FileDependencyObject client, 
			List<FileDependencyObject> fileList){
		
		try {
			
			//Creates and fills files
			Map<String, String> mapTempFilePathToRealFilePath = 
					new HashMap<String, String>();
				
			if(StringUtils.isNotBlank(client.getFile().getSourceCode())){
				String tmpFilePath = "/tmp/0.java";				
				createAndFillFile(tmpFilePath, client.getFile().getSourceCode());
				mapTempFilePathToRealFilePath.put(tmpFilePath, client.getFile().getPath());
			}			
			
			for(int i = 0; i<fileList.size(); i++){
				FileArtifact file = fileList.get(i).getFile();
				if(StringUtils.isNotBlank(file.getSourceCode())){
					String tmpFilePath = "/tmp/" + (i+1) + ".java";					
					createAndFillFile(tmpFilePath, file.getSourceCode());
					mapTempFilePathToRealFilePath.put(tmpFilePath, file.getPath());
				}
			}
			
			//Calculates structural coupling (counts operation calls)
			List<StructuralDependency> dependencies = calculateDependencies(mapTempFilePathToRealFilePath, true);
			return dependencies;
						
		} catch (Exception e) {
			System.out.println("Unable to calculate structural coupling");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}	
	}
	
	public static List<StructuralDependency> calcStructuralCoupling(List<FileDependencyObject> fileList){
		
		try {
			
			//Creates and fills files
			Map<String, String> mapTempFilePathToRealFilePath = 
					new HashMap<String, String>();
			
			for(int i = 0; i<fileList.size(); i++){
				FileArtifact file = fileList.get(i).getFile();
				if(StringUtils.isNotBlank(file.getSourceCode())){
					String tmpFilePath = "/tmp/" + i + ".java";					
					createAndFillFile(tmpFilePath, file.getSourceCode());
					mapTempFilePathToRealFilePath.put(tmpFilePath, file.getPath());
				}
			}
		
			//Calculates structural coupling (counts operation calls)
			List<StructuralDependency> dependencies = calculateDependencies(
					mapTempFilePathToRealFilePath, false);
			return dependencies;
						
		} catch (Exception e) {
			System.out.println("Unable to calculate structural coupling");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}	
	}

	
	private static void createAndFillFile(String filepath, String sourceCode) throws IOException{
		//Creates and fills first temp file
		File file = new File(filepath);
		FileUtils.writeStringToFile(file, sourceCode);
	}
		
	public static List<StructuralDependency> calculateDependencies(
			Map<String, String> mapTempFilePathToRealFilePath,
			boolean firstOnly) throws IOException{
		
		Runtime runtime = Runtime.getRuntime();
		//Process process = runtime.exec("D:\\cygwin\\home\\Francisco\\doxyparse\\bin\\doxyparse" + " " + filepathA + " " + filepathB);
		
		//Building the command
		StringBuilder command = new StringBuilder("doxyparse");
		for(String tmpFilePath : mapTempFilePathToRealFilePath.keySet()){
			command.append(" " + tmpFilePath);
		}
	
		Process process = runtime.exec(command.toString());
		String doxyOutput = IOUtils.toString(process.getInputStream());
		process.destroy();
		
		Map<String,String> mapModuleToFile = 
				mapModuleToFile(doxyOutput, mapTempFilePathToRealFilePath);
		
		String[] outputPerClass = doxyOutput.split("file /tmp/.*java");
		
		List<StructuralDependency> structuralDepsList = 
				new ArrayList<StructuralDependency>();
		
		//The first slot is empty, so we start in the second one
		for(int i = 1; i < outputPerClass.length; i++){
			
			String classOutput = outputPerClass[i];
			
			if(StringUtils.isNotBlank(classOutput)){
			
				StructuralDependency dependency = 
						parseDoxyOutputToCountFuctionCalls(classOutput, mapModuleToFile);
				
				structuralDepsList.add(dependency);
	
				//Doxyparse orders the output per filepath, so "0.java" will 
				//always be the first one to be processed
				if(firstOnly) break;
			}
			else{
				//This can be an Aspect class or some other odd stuff
				//It can  be a single class in a revision
				//It can also be a class that has no self-dependencies and also no dependencies to the other classes
				//TODO:Find a way to figure out the class name
			}
		}
		
		return structuralDepsList;
	}
	
	private static Map<String, String> mapModuleToFile(String doxyOutput, 
			Map<String, String> mapTempFilePathToRealFilePath) {
		
		Map<String,String> mapModuleToFile = new HashMap<String, String>();
		String[] lines = doxyOutput.split(System.getProperty("line.separator"));
		for(int i = 0; i < lines.length; i++){
			String line = lines[i];
			
			//Found a file
			if(line.matches("file /tmp/.*java")){
				
				String[] words = line.trim().split(" ");
				String tmpFilePath = words[words.length-1];
				String filePath = mapTempFilePathToRealFilePath.get(tmpFilePath);
				
				int nextIndex = i + 1;
				if(nextIndex < lines.length){
	
					String nextLine = lines[i+1];
					
					//Found the respective module
					if (nextLine.matches("module .*")){
						words = nextLine.trim().split(" ");
						String module = words[words.length-1];
						mapModuleToFile.put(module, filePath);
					}
				}
			}
		}
		
		return mapModuleToFile;
	}

	private static StructuralDependency parseDoxyOutputToCountFuctionCalls(String output, Map<String,String> mapModuleToFile){
		StructuralDependency dependency = new StructuralDependency();
		
		//Finds module name (should be the first line of output)
		Pattern modulePattern = 
				Pattern.compile("module .*");
		
		Matcher moduleMatcher = modulePattern.matcher(output);
		
		String thisModule = new String();
		moduleMatcher.find();
		String line = moduleMatcher.group();
		String[] words = line.trim().split(" ");
		thisModule = words[words.length-1];
		
		dependency.setClient(mapModuleToFile.get(thisModule));
		
		//Finds all operation calls to other classes
		Pattern functionCallPattern = 
				Pattern.compile("uses function.*defined in.*");
		
		Matcher matcher = functionCallPattern.matcher(output);
		
		while(matcher.find()){
			line = matcher.group();
			words = line.trim().split(" ");
			String referredModule = words[words.length-1];
			if(!thisModule.equals(referredModule)){
				dependency.addDegree(mapModuleToFile.get(referredModule), 1);
			}
		}
		
		return dependency;
	}

	
	//FIXME: Dirty code to be removed
	public static boolean checkStructuralCoupling(FileArtifact a, FileArtifact b){
		
		String sourceCodeofA = a.getSourceCode();
		String sourceCodeofB = b.getSourceCode();
		
		//If at least of the files is empty, there is nothing to be done
		if (sourceCodeofA == null || sourceCodeofB == null){
			return false;
		}
		//Calculation of references is needed
		else{
			try {
				//Creates and fills temp files
				String tmpFilepathForA = "/tmp/A.java";
				String tmpFilepathForB = "/tmp/B.java";
				createAndFillFile(tmpFilepathForA, sourceCodeofA);			
				createAndFillFile(tmpFilepathForB, sourceCodeofB);
				
				//Calculates structural coupling (counts calls from A to B and vice-versa)
				return checkCallsBetweenPairOfFiles(tmpFilepathForA, tmpFilepathForB, a.getPath(), b.getPath());

			} catch (Exception e) {
				System.out.println("Unable to calculate structural coupling");
				System.out.println(e.getMessage());
				e.printStackTrace();
				return false;
			}	
		}
	}


	
	private static boolean checkCallsBetweenPairOfFiles(String filepathA, 
			String filepathB, String fa, String fb) throws IOException{
		
		Pattern modulePattern = Pattern.compile("module .*");
		Pattern functionCallPattern = Pattern.compile("uses function.*defined in.*");
		String currentModule = null;
		int refs = 0;
				
		Runtime runtime = Runtime.getRuntime();
//		Process process = runtime.exec("D:\\cygwin\\home\\Francisco\\doxyparse\\bin\\doxyparse" + " " + filepathA + " " + filepathB);
		Process process = runtime.exec("doxyparse" + " " + filepathA + " " + filepathB);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
        boolean aDependsOnB = false;
        boolean finishScanOfA = false;

        String line;
        while((line = bufferedReader.readLine()) != null && !finishScanOfA){
                line = line.trim();
                //Found a module
                if (modulePattern.matcher(line).matches()){
                        //Found the first module
                        if (currentModule == null){
                                currentModule = line.replace("module ", "");
                        }
                        //Found the second module and it's not an inner class
                        else if (currentModule != null && !line.contains(currentModule + "::")){
                                if (refs > 0 ) aDependsOnB = true;
                                finishScanOfA = true;
                        }
                }
                //Found a function call
                else if(functionCallPattern.matcher(line).matches()){
                        String[] words = line.trim().split(" ");
                        String referredModule = words[words.length-1];
                        if(!currentModule.equals(referredModule)){
                                refs++;
                        }
                }
        }

        bufferedReader.close();
        process.destroy();
        return aDependsOnB;
	}
}