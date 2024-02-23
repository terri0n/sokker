package com.formulamanager.sokker.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang3.StringEscapeUtils;

import com.formulamanager.sokker.auxiliares.Util;

public class TagLibDesplegable extends TagSupport {
	private static final long serialVersionUID = 1L;
	private String name = null;
	private String style = null;
	private String class_ = null;
	private String value = null;
	private String onchange = null;
	
	@Override
	public int doStartTag() throws JspException {
		String resp = "<div class='dropdown " + Util.nvl(class_) + "' style='" + Util.nvl(style) + "'";
		if (name != null) {
			resp += " name='" + name + "'";
		}
		if (onchange != null) {
			resp += " data-onchange='" + StringEscapeUtils.escapeJava(onchange) + "'";
		}
		resp += ">";
		resp += "<span>";
		resp += "</span>⏷";
		resp += "<ul class='dropdown-menu' style='text-align: left;'>";
		
		try {
			pageContext.getOut().print(resp);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return EVAL_BODY_INCLUDE;
	}
	
	
	
	public int doEndTag() throws JspException {
		String resp = "</ul></div>";

		if (value != null) {
			resp += "<script>dropdown_init('" + value + "')</script>";
		}
		
		try {
			pageContext.getOut().print(resp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		name = null;
		style = null;
		value = null;
		
		return EVAL_PAGE;
    }

	/**
	 * G&S
	 */
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}



	public String getOnchange() {
		return onchange;
	}



	public void setOnchange(String onchange) {
		this.onchange = onchange;
	}



	public String getClass_() {
		return class_;
	}



	public void setClass_(String class_) {
		this.class_ = class_;
	}
}