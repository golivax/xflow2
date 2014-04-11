package br.usp.ime.lapessc.xflow2.entity.representation.matrix.sparse;

import java.util.ArrayList;
import java.util.List;

import org.ujmp.core.exceptions.MatrixException;
import org.ujmp.core.intmatrix.impl.DefaultSparseIntMatrix;

import br.usp.ime.lapessc.xflow2.entity.representation.matrix.Matrix;

public class UJMPSparseMatrixWrapper extends DefaultSparseIntMatrix implements Matrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8822469000920140479L;

	public UJMPSparseMatrixWrapper() {
		super(0L, 0L);
	}
	
	public UJMPSparseMatrixWrapper(long rows, long columns) {
		super(rows, columns);
	}
	
	public UJMPSparseMatrixWrapper(org.ujmp.core.Matrix m) throws MatrixException {
		super(m);
	}

	@Override
	public int getRows() {
		return (int) getRowCount();
	}

	@Override
	public int getColumns() {
		return (int) getColumnCount();
	}

	@Override
	public int getValueAt(long row, long column) {
		return getInt(row, column);
	}

	@Override
	public void putValueAt(int value, long row, long column) {
		setInt(value, row, column);
	}

	@Override
	public void incrementValueAt(int value, long row, long column) {
		int previousValue = getInt(row, column);
		setInt(previousValue+value, row, column);
	}

	@Override
	public Matrix multiply(Matrix anotherMatrix) {
		return new UJMPSparseMatrixWrapper(mtimes((org.ujmp.core.Matrix) anotherMatrix));
	}

	@Override
	public Matrix sum(Matrix anotherMatrix) {
		return new UJMPSparseMatrixWrapper(((org.ujmp.core.Matrix) anotherMatrix));
	}

	@Override
	public Matrix sumDifferentOrderMatrix(Matrix anotherMatrix) {
		try {
			return new UJMPSparseMatrixWrapper(plus((DefaultSparseIntMatrix) anotherMatrix));
		} catch (IllegalArgumentException e ){
			final int maxRows = (int) Math.max(this.getSize(0), anotherMatrix.getRows());
			final int maxColumns = (int) Math.max(this.getSize(1), anotherMatrix.getColumns());
			
			this.setSize(maxRows, maxColumns);
			anotherMatrix.incrementMatrixRowsTo(maxRows);
			anotherMatrix.incrementMatrixColumnsTo(maxColumns);
			
			return new UJMPSparseMatrixWrapper(plus((DefaultSparseIntMatrix) anotherMatrix));
		}
	}

	@Override
	public Matrix getTransposeMatrix() {
		return (Matrix) new UJMPSparseMatrixWrapper(transpose());
	}

	@Override
	public Matrix createIdentityMatrix(int size) {
		return null;
	}

	@Override
	public void applyStatisticalFilters(int minSupport, double minConfidence) {
		System.out.println("Applying filters - Only the support filter is working by now");
		List<Long> toClear = new ArrayList<Long>();
		
		for(long[] coordinate : this.availableCoordinates()){
			long i = coordinate[0];
			long j = coordinate[1];
			int support = this.getValueAt(i,j);
			if(support < minSupport){
				toClear.add(i);
				toClear.add(j);
			}
		}
		
		for(int k = 0; k < toClear.size(); k = k + 2){
			
			long i = toClear.get(k);
			long j = toClear.get(k + 1);
			this.putValueAt(0, i, j);
			this.putValueAt(0, j, i);
		
		}
	}

	@Override
	public void incrementMatrixRowsTo(int newSize) {
		this.setSize(newSize, this.getSize()[1]);
	}

	@Override
	public void incrementMatrixColumnsTo(int newSize) {
		this.setSize(this.getSize()[0], newSize);
	}
}
