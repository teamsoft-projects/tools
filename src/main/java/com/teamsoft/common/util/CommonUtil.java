package com.teamsoft.common.util;

import java.util.UUID;

/**
 * 公共方法
 * @author alex
 * @version 2020/4/29
 */
public class CommonUtil {
	/**
	 * 生成32位的UUId
	 * @return UUId
	 */
	public static String generateUUId() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}