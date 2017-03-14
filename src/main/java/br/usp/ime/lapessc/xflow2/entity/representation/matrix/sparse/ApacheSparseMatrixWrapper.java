package br.usp.ime.lapessc.xflow2.entity.representation.matrix.sparse;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import br.usp.ime.lapessc.xflow2.entity.representation.matrix.IRealMatrix;

public class ApacheSparseMatrixWrapper implements IRealMatrix {

	private OpenMapRealMatrix internalMatrix;

	public ApacheSparseMatrixWrapper(int rows, int columns) {
		internalMatrix = new OpenMapRealMatrix(rows,columns);
	}
	
	//FIXME: This might take a while, since we are iterating
	//through a sparse matrix. A more elegant solution would be
	//to extend OpenMapRealMatrix and use its iterator to iterate
	//through all matrix values in the hashmap
	public ApacheSparseMatrixWrapper(IRealMatrix matrix){
		
		int rows = matrix.getRows();
		int columns = matrix.getColumns();
		internalMatrix = new OpenMapRealMatrix(rows,columns);
		
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < columns; j++){
				double value = matrix.getValueAt(i, j);
				if(value != 0d) internalMatrix.setEntry(i, j, value);
			}
		}
	}
	
	public ApacheSparseMatrixWrapper(RealMatrix matrix){
		int rows = matrix.getRowDimension();
		int columns = matrix.getColumnDimension();
		internalMatrix = new OpenMapRealMatrix(rows,columns);
		
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < columns; j++){
				double value = matrix.getEntry(i, j);
				if(value != 0d) internalMatrix.setEntry(i, j, value);
			}
		}
	}
	
	public ApacheSparseMatrixWrapper(OpenMapRealMatrix matrix){
		this.internalMatrix = matrix;
	}

	@Override
	public int getRows() {
		return internalMatrix.getRowDimension();
	}

	@Override
	public int getColumns() {
		return internalMatrix.getColumnDimension();
	}

	@Override
	public Double getValueAt(int row, int column) {
		return internalMatrix.getEntry(row, column);
	}

	@Override
	public void putValueAt(double value, int row, int column) {
		internalMatrix.setEntry(row, column, value);
	}

	@Override
	public void incrementValueAt(double increment, int row, int column) {
		internalMatrix.addToEntry(row, column, increment);
	}

	@Override
	public IRealMatrix multiply(IRealMatrix anotherMatrix) {
		ApacheSparseMatrixWrapper anotherMatrixWrapper = new ApacheSparseMatrixWrapper(anotherMatrix);
		OpenMapRealMatrix result = internalMatrix.multiply(anotherMatrixWrapper.internalMatrix);
		return new ApacheSparseMatrixWrapper(result);
	}

	@Override
	public IRealMatrix sum(IRealMatrix anotherMatrix) {
		ApacheSparseMatrixWrapper anotherMatrixWrapper = new ApacheSparseMatrixWrapper(anotherMatrix);
		OpenMapRealMatrix result = internalMatrix.add(anotherMatrixWrapper.internalMatrix);
		return new ApacheSparseMatrixWrapper(result);
	}

	@Override
	public IRealMatrix sumDifferentOrderMatrix(IRealMatrix anotherMatrix) {
		
		int maxRows = Math.max(this.getRows(), anotherMatrix.getRows());
		int maxColumns = Math.max(this.getColumns(), anotherMatrix.getColumns());
		
		OpenMapRealMatrix newThis = new OpenMapRealMatrix(maxRows, maxColumns);
		for(int i = 0; i < this.getRows(); i++){
			for(int j = 0; j < this.getColumns(); j++){
				double value = this.getValueAt(i, j);
				newThis.setEntry(i, j, value);
			}
		}
		
		OpenMapRealMatrix newAnother = new OpenMapRealMatrix(maxRows, maxColumns);
		for(int i = 0; i < anotherMatrix.getRows(); i++){
			for(int j = 0; j < anotherMatrix.getColumns(); j++){
				double value = anotherMatrix.getValueAt(i, j);
				newAnother.setEntry(i, j, value);
			}
		}
		
		OpenMapRealMatrix result = newThis.add(newAnother);
		return new ApacheSparseMatrixWrapper(result);
	}

	@Override
	public IRealMatrix getTransposeMatrix() {
		RealMatrix transpose = internalMatrix.transpose();
		return new ApacheSparseMatrixWrapper(transpose);
	}

	public static IRealMatrix createIdentityMatrix(int size) {
		OpenMapRealMatrix identityMatrix = new OpenMapRealMatrix(size,size);
		for(int i = 0; i < size; i++){
			identityMatrix.setEntry(i, i, 1);
		}
		return new ApacheSparseMatrixWrapper(identityMatrix);
	}
	
	public static void main(String[] args) {
		OpenMapRealMatrix internalMatrix = new OpenMapRealMatrix(10,15);
		internalMatrix.setEntry(1, 4, 10);
		
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 15; j++){
				System.out.println(internalMatrix.getEntry(i, j) == 0d);
			}
		}
	}
}
