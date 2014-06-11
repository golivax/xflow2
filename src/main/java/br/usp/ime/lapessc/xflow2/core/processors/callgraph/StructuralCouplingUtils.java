package br.usp.ime.lapessc.xflow2.core.processors.callgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;

public class StructuralCouplingUtils {

	public static Set<DependencySet<FileDependencyObject, FileDependencyObject>> createDependencySets(
			List<FileDependencyObject> dependencyObjectList,
			List<StructuralDependency> dependencies) {

		Set<DependencySet<FileDependencyObject, FileDependencyObject>> setOfDependencySets = 
				new HashSet<DependencySet<FileDependencyObject, FileDependencyObject>>();
		
		for(FileDependencyObject supplier : dependencyObjectList){

			//Create the dependencySet
			DependencySet<FileDependencyObject, FileDependencyObject> dependencySet = 
					new DependencySet<FileDependencyObject, FileDependencyObject>();
			
			//Sets the supplier
			dependencySet.setSupplier(supplier);
			
			//Now fills the dependencyMap for the supplier
			Map<FileDependencyObject, Integer> dependencyMap = 
					new HashMap<FileDependencyObject, Integer>();
			
			for(StructuralDependency dependency : dependencies){
				String client = dependency.getClient();
				FileDependencyObject clientFile = getFileDependencyObject(client, dependencyObjectList);
				
				int degree = 0;
				if(dependency.hasSupplier(supplier.getFile().getPath())){
					degree = dependency.getDegree(supplier.getFile().getPath());
					
				}
				
				dependencyMap.put(clientFile, degree);				
			}
			
			//Adds the supplier itself to the dependencyMap
			dependencyMap.put(supplier, 1);
			
			//Sets the dependenciesMap in the dependencySet
			dependencySet.setClientsMap(dependencyMap);
			
			//Adds the dependencySet to the set of dependencySets
			setOfDependencySets.add(dependencySet);
		}
		
		return setOfDependencySets;
	}
	
	public static Set<DependencySet<FileDependencyObject, FileDependencyObject>> createDependencySetsForOldSuppliers(
			FileDependencyObject clientFile,
			List<FileDependencyObject> oldSuppliers,
			List<StructuralDependency> dependencies) {

		Set<DependencySet<FileDependencyObject, FileDependencyObject>> setOfDependencySets = 
				new HashSet<DependencySet<FileDependencyObject, FileDependencyObject>>();
		
		for(FileDependencyObject supplier : oldSuppliers){

			//Create the dependencySet
			DependencySet<FileDependencyObject, FileDependencyObject> dependencySet = 
					new DependencySet<FileDependencyObject, FileDependencyObject>();
			
			//Sets the supplier
			dependencySet.setSupplier(supplier);
			
			//Now fills the dependencyMap for the supplier
			Map<FileDependencyObject, Integer> dependencyMap = 
					new HashMap<FileDependencyObject, Integer>();
			
			for(StructuralDependency dependency : dependencies){
				int degree = 0;
				if(dependency.hasSupplier(supplier.getFile().getPath())){
					degree = dependency.getDegree(supplier.getFile().getPath());
					
				}
				
				dependencyMap.put(clientFile, degree);				
			}
			
			//Adds the supplier itself to the dependencyMap
			dependencyMap.put(supplier, 1);
			
			//Sets the dependenciesMap in the dependencySet
			dependencySet.setClientsMap(dependencyMap);
			
			//Adds the dependencySet to the set of dependencySets
			setOfDependencySets.add(dependencySet);
		}
		
		return setOfDependencySets;
	}
	
	private static FileDependencyObject getFileDependencyObject(String filePath, 
			List<FileDependencyObject> dependencyObjectList){
		
		for(FileDependencyObject file : dependencyObjectList){
			if(filePath.equals(file.getFile().getPath())){
				return file;
			}
		}
		
		System.out.println("Fuck!");
		System.out.println(filePath);
		return null;
	}
}
