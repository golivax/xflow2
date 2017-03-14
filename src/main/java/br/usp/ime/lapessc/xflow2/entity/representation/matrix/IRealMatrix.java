package br.usp.ime.lapessc.xflow2.entity.representation.matrix;


public interface IRealMatrix {

	/*
	 * MATRIX OPERATION AND GENERAL HANDLING METHODS.
	 */
	public int getRows();
	public int getColumns();
	public Double getValueAt(int row, int column);
	public void putValueAt(double value, int row, int column);
	public void incrementValueAt(double increment, int row, int column);
	public IRealMatrix multiply(IRealMatrix anotherMatrix);
	public IRealMatrix sum(IRealMatrix anotherMatrix);
	public IRealMatrix sumDifferentOrderMatrix(IRealMatrix anotherMatrix);
	public IRealMatrix getTransposeMatrix();
	
}
