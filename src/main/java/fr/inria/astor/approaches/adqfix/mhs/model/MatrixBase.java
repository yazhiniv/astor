package fr.inria.astor.approaches.adqfix.mhs.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MatrixBase<T> implements IMatrix<T>{
	protected final List<List<T>> data;
	protected final T defaultValue;
	
	
	protected MatrixBase(){
		defaultValue=null;
		data = new Vector<List<T>>();				
	}
	
	public MatrixBase(int N,int M){				
		this(N,M,null);
	}
	
	public MatrixBase(int N,int M,T initValue){		
		defaultValue=initValue;		
		data = new ArrayList<List<T>>(N);
		for(int i=0;i<N;i++){
			data.add(new ArrayList<T>());
			for(int j=0;j<M;j++)
				data.get(i).add(initValue);
		}		
	}
	
	public MatrixBase(MatrixBase<T> obj) {
		this(obj.rowCount(),obj.colCount());		
	}
	public T get(int row,int col){
		return data.get(row).get(col);
	}
	
	public void set(int row,int col, T value){
		data.get(row).set(col, value);
	}
	public List<T> getRow(int i){
		return data.get(i);
	}
	
	boolean checkRange(int row,int col){
		return !(row<0||row>=rowCount() || col<0||col>=colCount());	
	}
	
	public int rowCount(){
		return data.size();
	}
	
	public int colCount(){		
		if (data.size()==0)
			return 0;
		return data.get(0).size();
	}
	
	//operators
	public void addColumn(int colIndex,T initValue){
		for(int i=0;i<rowCount();i++)
			data.get(i).add(colIndex, initValue);
	}
	public void addColumn(int colIndex){
		addColumn(colIndex, defaultValue);
	}
	
	public void removeCol(int index){		
		for(int i=0;i<rowCount();i++)
			data.get(i).remove(index);
	}
	
	public void removeRow(int index){
		data.remove(index);
	}	
	
	public void addRow(int index,T initValue){
		List<T> row = new ArrayList<T>(colCount());
		for(int i=0;i<colCount();i++)
			row.add(i, initValue);
		data.add(index, row);
	}
	public void addRow(int index){
		addRow(index,defaultValue);
	}
}
