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
 *  ==================
 *  AccessFactory.java
 *  ==================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  Pedro Treccani, Jean Costa;
 *  
 */

package br.usp.ime.lapessc.xflow2.repository.vcs.parser;

import br.usp.ime.lapessc.xflow2.entity.MiningSettings;
import br.usp.ime.lapessc.xflow2.exception.cm.svn.SVNProtocolNotSupportedException;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.svn.SVNLogParser;

public abstract class VCSLogParserFactory {

	public static VCSLogParser create(MiningSettings settings) {
		
		switch(settings.getVcs().getType()) {
			case Subversion: return createSVNLogParser(settings);  
			default: return null;
		}
	}
	
	private static VCSLogParser createSVNLogParser(MiningSettings settings){
		
		SVNLogParser svnLogParser = null;
		
		try{
			svnLogParser = new SVNLogParser(settings);
			
		}catch(SVNProtocolNotSupportedException e){
			e.printStackTrace();
		}
		
		return svnLogParser;
	}
}
