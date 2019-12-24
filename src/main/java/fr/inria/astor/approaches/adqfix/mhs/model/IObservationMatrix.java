package fr.inria.astor.approaches.adqfix.mhs.model;


public interface IObservationMatrix<CompType> extends IMatrix<Integer>{
	public static final int RESULT_PASS=0;
	public static final int RESULT_FAILED =1;
	
	boolean isDirty();
	
	void setHit(int row,int col,int hit);
	int getHit(int row,int col);
	void setErrorTrace(int row,int error);
	int getError(int row);
	
	int getN11(int col);
	int getN10(int col);
	int getN01(int col);
	int getN00(int col);
	double getScore(int col);
	//double setScore(int col);

	IObservationMatrix<CompType> copy();

	CompType getComponentIDAt(int i);
	int getComponentIndex(CompType componentName);
}
