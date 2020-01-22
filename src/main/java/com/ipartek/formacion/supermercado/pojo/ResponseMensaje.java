package com.ipartek.formacion.supermercado.pojo;

import java.util.ArrayList;
import java.util.List;

public class ResponseMensaje {
	private String texto;
	private List<String> errores;

	public ResponseMensaje() {
		super();
		this.texto = "";
		this.errores = new ArrayList<String>();
	}
	public ResponseMensaje(String texto) {
		this();
		this.texto = texto;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public List<String> getErrores() {
		return errores;
	}

	public void setErrores(List<String> errores) {
		this.errores = errores;
	}

	@Override
	public String toString() {
		return "ResponseMensaje [texto=" + texto + ", errores=" + errores + "]";
	}





}
