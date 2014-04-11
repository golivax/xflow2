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
 *  Filter.java
 *  ===========
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class Filter {
	
	@Column(name = "FILTER")
	private String regex;
	
	@Transient
	private Pattern pattern;
	
	public Filter(){
		
	}
	
	public Filter(String parameter){
		this.regex = parameter;
		this.pattern = Pattern.compile(regex);		
	}
	
	public boolean match(final String s){
		final Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}

	public String getRegex() {
		return regex;
	}
	
	public static void main(String args[]){
		//Filter filter = new Filter(".*?(\\.).*?");
		//System.out.println(filter.match("/app/CHANGE.java"));
		
		Filter f = new Filter(".*?\\.java");
		System.out.println(
				f.match("/ant/core/trunk/src/main/org/apache/tools/ant/" +
						"taskdefs/optional/net/SetProxy.java"));
	}
	
}
