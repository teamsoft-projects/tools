package com.teamsoft.common.model;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * 常量类
 * @author alex
 * @version 2020/2/27
 */
public class Constants {
	public interface System {
		Integer SUCCESS_FLAG = 100101;
		Integer FAILURE_FLAG = 100102;
		Integer FAILURE_NOTICE_FLAG = 100503;

		// GSON常量
		Gson GSON = new Gson();

		// 日期时间格式化常量
		DateFormat FORMAT_YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * QQ相关常量
	 */
	public interface QQ {
	}
}