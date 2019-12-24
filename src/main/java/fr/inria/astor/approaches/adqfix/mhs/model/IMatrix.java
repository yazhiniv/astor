package fr.inria.astor.approaches.adqfix.mhs.model;


import java.util.List;

public interface IMatrix<T>{
	public T get(int row,int col);	
	public void set(int row,int col, T value);
	
	public List<T> getRow(int i);
	
	public int rowCount();	
	public int colCount();
	
	//operators
	public void addColumn(int colIndex,T initValue);	
	public void removeCol(int index);
	
	public void removeRow(int index);	
	public void addRow(int index,T initValue);
	
}
