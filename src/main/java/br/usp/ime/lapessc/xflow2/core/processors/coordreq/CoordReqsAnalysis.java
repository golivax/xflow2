package br.usp.ime.lapessc.xflow2.core.processors.coordreq;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.CoordReqsMatrixFactory;
import br.usp.ime.lapessc.xflow2.entity.CoordinationRequirementsMatrix;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

@Entity(name = "coordreq_analysis")
public class CoordReqsAnalysis extends Analysis{

	public CoordReqsAnalysis(){}
	
	@ManyToOne
	private CoChangesAnalysis coChangesAnalysis;
	
	public CoordReqsAnalysis(CoChangesAnalysis coChangesAnalysis){
		this.coChangesAnalysis = coChangesAnalysis;
	}
	
	public CoordinationRequirementsMatrix getCoordinationReqsMatrix() throws DatabaseException{
				
		CoordReqsMatrixFactory coordReqMatrixFactory = 
				new CoordReqsMatrixFactory();
		
		CoordinationRequirementsMatrix coordReqMatrix = 
				coordReqMatrixFactory.build(this);
		
		return coordReqMatrix;		
	}
	
	public CoChangesAnalysis getCoChangesAnalysis(){
		return coChangesAnalysis;
	}

	@Override
	public boolean checkCutoffValues(Commit commit) {
		return coChangesAnalysis.checkCutoffValues(commit);
	}

}