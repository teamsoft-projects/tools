package com.teamsoft.compress.model;

import com.google.common.annotations.GwtIncompatible;

import java.io.IOException;

/**
 * 重写CommandLineRunner，提升构造函数权限
 * @author alex
 * @version 2019/12/13
 */
public class CommandLineRunner extends com.google.javascript.jscomp.CommandLineRunner {
	public CommandLineRunner(String[] args) {
		super(args);
	}

	@GwtIncompatible("Unnecessary")
	public int doRun() throws IOException {
		return super.doRun();
	}
}
