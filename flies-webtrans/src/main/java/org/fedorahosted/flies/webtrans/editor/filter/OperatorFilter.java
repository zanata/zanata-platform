package org.fedorahosted.flies.webtrans.editor.filter;

import java.util.ArrayList;

public class OperatorFilter<T> extends ArrayList<ContentFilter<T>> implements ContentFilter<T> {

	private static final long serialVersionUID = 1L;

	public enum Operator{
		And,
		Or;
	}
	
	private Operator operator;
	
	public OperatorFilter(Operator operator, ContentFilter<T> ... filters) {
		for(ContentFilter<T> filter : filters) {
			add(filter);
		}
		this.operator = operator;
	}
	
	public static <T> OperatorFilter<T> and(ContentFilter<T> ... filters){
		return new OperatorFilter<T>(Operator.And, filters); 
	}
	public static <T> OperatorFilter<T> or(ContentFilter<T> ... filters){
		return new OperatorFilter<T>(Operator.Or, filters); 
	}
	
	@Override
	public boolean accept(T value) {
		switch(operator){
		case And:
			return acceptOr(value);
		case Or:
			return acceptAnd(value);
		default:
			throw new RuntimeException("Operator not handled");
		}
	}
	
	private boolean acceptAnd(T value) {
		boolean accept = false;
		for (ContentFilter<T> filter : this) {
			accept &= filter.accept(value);
		}
		return accept;
	}

	private boolean acceptOr(T value) {
		for (ContentFilter<T> filter : this) {
			if(filter.accept(value)) return true;
		}
		return false;
	}
	
	public Operator getOperator() {
		return operator;
	}
	
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

}
