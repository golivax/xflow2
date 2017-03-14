package br.usp.ime.lapessc.xflow2.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import br.usp.ime.lapessc.xflow2.entity.representation.matrix.IRealMatrix;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.sparse.ApacheSparseMatrixWrapper;

public class CoordinationRequirementsMatrix{

	//Symmetric coordination requirements matrix
	private IRealMatrix innerMatrix;
	private Map<Integer, Author> rowToAuthorMap;
	
	public CoordinationRequirementsMatrix(IRealMatrix coordReqsMatrix,
			Map<Integer, Author> rowToAuthorMap) {
		
		this.innerMatrix = coordReqsMatrix;
		this.rowToAuthorMap = rowToAuthorMap;
	}
	
	public CoordinationRequirementsMatrix getBinaryVersion(){
		
		IRealMatrix binaryMatrix = new ApacheSparseMatrixWrapper(size(),size());
				
		//Transforma a matriz em binária
		for (int i = 0; i < innerMatrix.getRows(); i++){
			for (int j = 0; j < innerMatrix.getColumns(); j++){
				if (innerMatrix.getValueAt(i,j) > 0){
					binaryMatrix.putValueAt(1, i, j);
				}
			}
		}
		
		return new CoordinationRequirementsMatrix(binaryMatrix, rowToAuthorMap);
	}
	
	public int size(){
		return rowToAuthorMap.size();
	}

	//TODO: Make it a toString
	public void print(){
		//Printa a matriz
		for (int i = 0; i < innerMatrix.getRows(); i++){
			for (int j = 0; j < innerMatrix.getColumns(); j++){
				System.out.print(innerMatrix.getValueAt(i, j));
				System.out.print(" ");
			}
			System.out.println();
		}
	}

	//Not the best solution ever, but works well for now
	public List<RawDependency<Author,Author,Integer>> getPairwiseCoordReqs(){
		List<RawDependency<Author, Author, Integer>> pairWiseCoordReqs = 
				new ArrayList<>();
				
		for (int i = 0; i < innerMatrix.getRows(); i++){
			for (int j = i+1; j < innerMatrix.getColumns(); j++){
				if(innerMatrix.getValueAt(i, j) != 0){
					Author a1 = getAuthorAtRow(i);
					Author a2 = getAuthorAtRow(j);
					int value = innerMatrix.getValueAt(i, j).intValue();
					
					RawDependency<Author, Author, Integer> pairWiseCoordReq = 
							new RawDependency<>(a1,a2,value);
							
					pairWiseCoordReqs.add(pairWiseCoordReq);		
				}
			}
		}
		
		return pairWiseCoordReqs;
	}
	
	private Author getAuthorAtRow(int row){
		return rowToAuthorMap.get(row);
	}
	
	public Collection<Author> getAuthors(){
		return rowToAuthorMap.values();
	}

}
