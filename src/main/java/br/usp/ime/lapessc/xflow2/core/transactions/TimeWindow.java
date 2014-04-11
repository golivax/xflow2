package br.usp.ime.lapessc.xflow2.core.transactions;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;

import br.usp.ime.lapessc.xflow2.entity.Commit;

@Entity(name = "time_window")
public class TimeWindow extends Commit{

	private Double lengthInMinutes;
	
	public Double getLengthInMinutes() {
		return lengthInMinutes;
	}

	public void setLengthInMinutes(Double length) {
		this.lengthInMinutes = length;
	}

	@ElementCollection
	@CollectionTable(
			name="window_entries",
			joinColumns=@JoinColumn(name = "window_id")
	)
	@Column(name="entry_number")
	private List<Long> entryNumbers;
	
	public TimeWindow(){
		entryNumbers = new ArrayList<Long>();
	}
	
	public void addEntryNumber(Commit entry){
		this.entryNumbers.add(entry.getRevision());
	}
	
}
