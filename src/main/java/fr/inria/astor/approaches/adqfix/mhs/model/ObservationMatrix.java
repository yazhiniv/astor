package fr.inria.astor.approaches.adqfix.mhs.model;


import java.util.ArrayList;
import java.util.List;

public class ObservationMatrix<CompType> extends MatrixBase<Integer> implements IObservationMatrix<CompType>{
	public static final int RESULT_PASS=0;
	public static final int RESULT_FAILED =1;
	
	final List<CompType> componentIDs;
	
	//final int N,M;
	final List<Integer> errorVector;
	final List<Integer> n11;
	final List<Integer> n10;
	final List<Integer> n01;
	final List<Integer> n00;	
	
	final List<Double> score;				

	boolean dirty=true;
	public boolean isDirty(){
		return dirty;
	}
	
	protected void markDirty(){
		dirty=true;
	}
	protected void markClean(){
		dirty=false;
	}
	
	public ObservationMatrix(int numTestCase,List<CompType> components){
		super(numTestCase,components==null?0:components.size(),0);		
		int N=numTestCase;
		int M=components.size();			
		componentIDs = new ArrayList<CompType>(components);						
		
		errorVector = new ArrayList<Integer>(N);
		n11= new ArrayList<Integer>(M);
		n10= new ArrayList<Integer>(M);
		n01= new ArrayList<Integer>(M);
		n00= new ArrayList<Integer>(M);
		score= new ArrayList<Double>(M);
		for(int i=0;i<N;i++){
			errorVector.add(null);			
		}
		for(int i=0;i<M;i++){			
			n11.add(-1); //-1 mean not inited
			n10.add(-1);
			n01.add(-1);
			n00.add(-1);
			score.add(0D);
		}
		markDirty();
	}
		
	public ObservationMatrix(ObservationMatrix<CompType> matrix) {		
		super(matrix);
		componentIDs = new ArrayList<>(matrix.componentIDs);		
		
		int N=matrix.rowCount();
		int M=matrix.colCount();				
		errorVector = new ArrayList<Integer>(N);
		n11= new ArrayList<Integer>(M);
		n10= new ArrayList<Integer>(M);
		n01= new ArrayList<Integer>(M);
		n00= new ArrayList<Integer>(M);
		score= new ArrayList<Double>(M);
		for(int i=0;i<N;i++){
			errorVector.add(null);			
		}
		for(int i=0;i<M;i++){			
			n11.add(-1); //-1 mean not inited
			n10.add(-1);
			n01.add(-1);
			n00.add(-1);
			score.add(0D);
		}
		for(int i=0;i<rowCount();i++){
			for(int j=0;j<colCount();j++)
				this.set(i, j, matrix.get(i, j));
			errorVector.set(i, matrix.getError(i));
		}
		markDirty();
	}
	
	public CompType getComponentIDAt(int i) {
		return this.componentIDs.get(i);
	}
	
	public int getComponentIndex(CompType comp) {
		return this.componentIDs.indexOf(comp);
	}

	
	public void setHit(int row,int col,int hit){
		set(row,col,hit);	
		markDirty();
	}
	public int getHit(int row,int col){		
		return get(row,col);
	}
	public void setErrorTrace(int row,int error){
		errorVector.set(row, error);		
		markDirty();
	}
	public int getError(int row){
		return errorVector.get(row);
	}
	
	public void removeRow(int index) {
		super.removeRow(index);
		errorVector.remove(index);
		markDirty();
	}
	public void addRow(int index) {
		super.addRow(index);
		errorVector.add(index, null);
		markDirty();
	}
	
	@Override
	public void addColumn(int colIndex, Integer initValue) {		
		super.addColumn(colIndex, initValue);			
		n11.add(colIndex, -1);
		n10.add(colIndex, -1);
		n01.add(colIndex, -1);
		n00.add(colIndex, -1);
		markDirty();
	}
	
	@Override
	public void removeCol(int index) {
		super.removeCol(index);
		this.componentIDs.remove(index);
		n11.remove(index);
		n10.remove(index);
		n01.remove(index);
		n00.remove(index);
		markDirty();
	}
	
	void computeHit(){
		for(int col=0;col<colCount();col++){
			n11.set(col, 0);
			n10.set(col, 0);
			n01.set(col, 0);
			n00.set(col, 0);
			for(int row=0;row<rowCount();row++)
				if(get(row, col)==1)
					if(getError(row)== ObservationMatrix.RESULT_FAILED)
						n11.set(col, n11.get(col)+1);
					else
						n10.set(col, n10.get(col)+1);
				else
					if (getError(row)==ObservationMatrix.RESULT_FAILED)
						n01.set(col, n01.get(col)+1);
					else
						n00.set(col, n00.get(col)+1);		
			//compute score
			score.set(col, n11.get(col)/Math.sqrt((n11.get(col)+n10.get(col))*(n11.get(col)+n01.get(col))));			
		}
		markClean();
	}
	
	public int getN11(int col) {
		if (isDirty())
			computeHit();
		return n11.get(col);
	}

	public int getN10(int col) {
		if (isDirty())
			computeHit();
		return n10.get(col);
	}

	public int getN01(int col) {
		if (isDirty())
			computeHit();
		return n01.get(col);
	}

	public int getN00(int col) {
		if (isDirty())
			computeHit();
		return n00.get(col);
	}
	public double getScore(int col) {		
		if (isDirty())
			computeHit();
		return score.get(col);
	}

	public IObservationMatrix<CompType> copy() {
		return new ObservationMatrix<CompType>(this);				
	}	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();		
		for(int i=0;i<colCount();i++)
			sb.append("\t"+getComponentIDAt(i));
		sb.append("\n");
		for(int i=0;i<rowCount();i++){
			for(int j=0;j<colCount();j++){
				sb.append("\t"+getHit(i, j));
			}
			sb.append("\t| "+getError(i)+"\n");
		}
		//==================== hit info ==============
		for(int j=0;j<colCount();j++){
			sb.append("\t----");			
		}
		sb.append("\nn11:");
		for(int j=0;j<colCount();j++){
			sb.append("\t"+getN11(j));
		}
		sb.append("\nn10:");
		for(int j=0;j<colCount();j++){
			sb.append("\t"+getN10(j));
		}
		sb.append("\nn01:");
		for(int j=0;j<colCount();j++){
			sb.append("\t"+getN01(j));
		}
		sb.append("\nn00:");
		for(int j=0;j<colCount();j++){
			sb.append("\t"+getN00(j));
		}
		sb.append("\nH:");
		for(int j=0;j<colCount();j++){
			sb.append("\t"+String.format("%.3f", getScore(j)));
		}
		return sb.toString();
	}	
}
