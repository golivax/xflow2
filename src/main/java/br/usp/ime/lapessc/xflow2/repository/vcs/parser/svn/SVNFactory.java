package br.usp.ime.lapessc.xflow2.repository.vcs.parser.svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import br.usp.ime.lapessc.xflow2.exception.cm.svn.SVNProtocolNotSupportedException;

public class SVNFactory {

	public static SVNRepository create(String URI, String username, 
			String password) throws SVNProtocolNotSupportedException{
		
		if (URI.startsWith("svn")){
			SVNRepositoryFactoryImpl.setup();
		}
		else if(URI.startsWith("http://") || URI.startsWith("https://")){
			DAVRepositoryFactory.setup();	
		}
		else{
			FSRepositoryFactory.setup();			
		}
		
		SVNRepository repository = null;
				
		try {
			repository = SVNRepositoryFactory.create(
					SVNURL.parseURIEncoded(URI));
			
			ISVNAuthenticationManager authManager = 
					SVNWCUtil.createDefaultAuthenticationManager(username, 
							password.toCharArray());
		
			repository.setAuthenticationManager(authManager);
			
		} catch (SVNException svne) {
			throw new SVNProtocolNotSupportedException();
		}
		
		return repository;
	}
	
}
