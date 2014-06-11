package br.usp.ime.lapessc.xflow2.teste;

import java.io.IOException;
import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class TestaBanco {

	public static void main(String[] args) throws DatabaseException, IOException {

		List<DependencyGraph> dependencies = (List<DependencyGraph>) new br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyDAO().findAll(DependencyGraph.class);
		for (DependencyGraph<FileDependencyObject, FileDependencyObject> dependency : dependencies) {
			System.out.println("*** REVISION "+dependency.getAssociatedEntry().getRevision()+" ***");
			for (DependencySet<FileDependencyObject, FileDependencyObject> dependencySet : dependency.getDependencies()) {
				System.out.println("DEPENDED FILE: "+dependencySet.getSupplier().getDependencyObjectName()+" ("+dependencySet.getSupplier().getAssignedStamp()+")");
				System.out.println("DEPENDENTS: ");
				for (DependencyObject dependedFile : dependencySet.getClientsMap().keySet()) {
					FileDependencyObject dependedFile2 = (FileDependencyObject) dependedFile;
					System.out.print("("+dependedFile2.getAssignedStamp()+") "+dependedFile2.getDependencyObjectName());
					System.out.println(" DEGREE: "+dependencySet.getClientsMap().get(dependedFile));
				}
				System.out.println("-------------------");
			}
			System.out.println();
		}
	}

}
