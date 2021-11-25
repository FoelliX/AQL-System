package de.foellix.aql.ui.gui;

import de.foellix.aql.helper.Helper;

public class LogMsg {
	public static final int TYPE_MSG = 0;
	public static final int TYPE_NOTE = 1;
	public static final int TYPE_WARNING = 2;
	public static final int TYPE_ERROR = 3;
	public static final int TYPE_IMPORTANT = 4;

	private String msg;
	private int type;

	public LogMsg(String msg, int type) {
		super();
		this.msg = msg;
		this.type = type;
	}

	public String getMsg() {
		return this.msg;
	}

	public int getType() {
		return this.type;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return Helper.shorten(this.msg, 25);
	}
}