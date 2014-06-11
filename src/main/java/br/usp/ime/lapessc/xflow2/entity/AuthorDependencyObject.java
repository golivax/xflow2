package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity(name="author_dependency")
public class AuthorDependencyObject extends DependencyObject {

	@OneToOne
	@JoinColumn(name = "AUTHOR_ID", nullable = false)
	private Author author;

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(final Author author) {
		this.author = author;
	}

	
	public AuthorDependencyObject() {
		super(DependencyObjectType.AUTHOR_DEPENDENCY.getValue());
	}

	@Override
	public String getDependencyObjectName() {
		return this.author.getName();
	}
	
}
