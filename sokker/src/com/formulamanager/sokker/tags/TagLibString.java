package com.formulamanager.sokker.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class TagLibString extends BodyTagSupport {
	private static final long serialVersionUID = 1L;
	private String begin_index = null;
	private String end_index = null;
	private String value = null;

	@Override
	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}
	
	@Override
	public void doInitBody() throws JspException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int doAfterBody() throws JspException {
		try {
			BodyContent bodycontent = getBodyContent();
			if (value == null) {
				value = bodycontent.getString();
			}
			JspWriter out = bodycontent.getEnclosingWriter();
			out.print(value.substring(Integer.valueOf(begin_index), Integer.valueOf(end_index)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		begin_index = null;
		end_index = null;
		value = null;

		return super.doEndTag();
	}
	
	/**
	 * G&S
	 */

	public String getBegin_index() {
		return begin_index;
	}

	public void setBegin_index(String begin_index) {
		this.begin_index = begin_index;
	}

	public String getEnd_index() {
		return end_index;
	}

	public void setEnd_index(String end_index) {
		this.end_index = end_index;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}