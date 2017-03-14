/* 
 * 
 * XFlow
 * _______
 * 
 *  
 *  (C) Copyright 2010, by Universidade Federal do Pará (UFPA), Francisco Santana, Jean Costa, Pedro Treccani and Cleidson de Souza.
 * 
 *  This file is part of XFlow.
 *
 *  XFlow is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  XFlow is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XFlow.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  ===========
 *  Access.java
 *  ===========
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  Jean Costa, Pedro Treccani;
 *  
 */

package br.usp.ime.lapessc.xflow2.repository.vcs.parser;

import java.util.Date;
import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.MiningSettings;
import br.usp.ime.lapessc.xflow2.exception.cm.CMException;

public interface VCSLogParser {
	
	public MiningSettings getSettings();

	public List<CommitDTO> parse() throws CMException;
	
	public List<CommitDTO> parse(long startCommit, long endCommit) throws CMException;

	public List<CommitDTO> parse(Date startDate, Date endDate) 
			throws CMException;

}
