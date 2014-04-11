package br.usp.ime.lapessc.xflow2.entity.representation.matrix;


public interface Matrix {

	/*
	 * MATRIX OPERATION AND GENERAL HANDLING METHODS.
	 */
	public int getRows();
	public int getColumns();
	public int getValueAt(final long row, final long column);
	public void putValueAt(final int value, final long row, final long column);
	public void incrementValueAt(final int value, final long row, final long column);
	public Matrix multiply(Matrix anotherMatrix);
	public Matrix sum(Matrix anotherMatrix);
	public Matrix sumDifferentOrderMatrix(Matrix anotherMatrix);
	public Matrix getTransposeMatrix();
	public Matrix createIdentityMatrix(final int size);
	public void applyStatisticalFilters(int support, double confidence);
	public Iterable<long[]> availableCoordinates();
	
	/*
	 * AUGMENT MATRIX SIZE METHODS
	 */
	public void incrementMatrixRowsTo(int newSize);
	public void incrementMatrixColumnsTo(int newSize);
	
}
