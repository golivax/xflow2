package br.usp.ime.lapessc.xflow2.entity.representation.matrix;

import br.usp.ime.lapessc.xflow2.entity.representation.matrix.sparse.UJMPSparseMatrixWrapper;

public abstract class MatrixFactory {
	
	public static Matrix createMatrix(){
		return new UJMPSparseMatrixWrapper();
	}
	
	public static Matrix createMatrix(long rows, long columns){
		return new UJMPSparseMatrixWrapper(rows, columns);
	}
}
