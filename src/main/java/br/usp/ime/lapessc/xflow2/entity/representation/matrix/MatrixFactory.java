package br.usp.ime.lapessc.xflow2.entity.representation.matrix;

import br.usp.ime.lapessc.xflow2.entity.representation.matrix.sparse.ApacheSparseMatrixWrapper;

public abstract class MatrixFactory {
	
	public static IRealMatrix createMatrix(int rows, int columns){
		return new ApacheSparseMatrixWrapper(rows, columns);
	}
}
