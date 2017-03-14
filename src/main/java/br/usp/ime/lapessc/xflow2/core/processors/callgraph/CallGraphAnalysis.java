package br.usp.ime.lapessc.xflow2.core.processors.callgraph;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.AnalysisType;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.IRealMatrix;

@Entity(name = "callgraph_analysis")
public class CallGraphAnalysis extends Analysis{

	@Transient
	private IRealMatrix matrixCache = null;
	
	@Column(name = "is_whole_system_snapshot")
	private boolean wholeSystemSnapshot;
	
	@Transient
	private DependencyGraph dependencyCache = null;
	
	@Transient
	private JUNGGraph graphCache = null;
	
	public CallGraphAnalysis(){
		this.setType(AnalysisType.CALLGRAPH_ANALYSIS.getValue());
		this.setWholeSystemSnapshot(false);
	}
	

	@Override
	public boolean checkCutoffValues(Commit entry) {
		if(this.getMaxFilesPerRevision() <= 0) return true;
		if(entry.getEntryFiles().size() > this.getMaxFilesPerRevision()){
			return false;
		}
		return true;
	}

	public void setWholeSystemSnapshot(boolean wholeSystemSnapshot) {
		this.wholeSystemSnapshot = wholeSystemSnapshot;
	}

	public boolean isWholeSystemSnapshot() {
		return wholeSystemSnapshot;
	}

}
