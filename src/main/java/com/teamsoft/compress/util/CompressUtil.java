package com.teamsoft.compress.util;

import com.teamsoft.common.exception.BusinessException;
import com.teamsoft.compress.model.CommandLineRunner;
import com.yahoo.platform.yui.compressor.CssCompressor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 压缩工具类
 * @author alex
 * @version 2019/12/10
 */
public class CompressUtil {
	// 不处理的文件夹名
	private static List<String> ignoreFiles;
	// 不处理的文件后缀
	private static List<String> ignoreSuffix;

	/**
	 * 检查目录下所有后缀满足要求的文件
	 * @param directory 待检查目录
	 * @return 所有符合条件的文件列表
	 */
	public static List<File> checkAllFiles(File directory, List<String> ignore, List<String> suffixs) {
		if (directory == null || !directory.exists() || !directory.isDirectory()) {
			throw new BusinessException("源文件夹不存在");
		}
		ignoreFiles = ignore;
		ignoreSuffix = suffixs;
		List<File> ret = new ArrayList<>();
		reduceForFiles(ret, directory);
		return ret;
	}

	/**
	 * 递归检查所有的子文件夹
	 */
	private static void reduceForFiles(List<File> total, File parent) {
		File[] fs = parent.listFiles();
		if (fs == null) {
			return;
		}
		for (File f : fs) {
			if (!isFileLegal(f)) {
				continue;
			}
			if (f.isDirectory()) {
				reduceForFiles(total, f);
			} else {
				total.add(f);
			}
		}
	}

	/**
	 * 文件是否合法
	 * @param f 待检测文件
	 * @return 文件是否合法(后缀满足检查条件, 不在忽略列表中)
	 */
	public static boolean isFileLegal(File f) {
		if (f == null || !f.exists()) {
			return false;
		}
		String name = f.getName();
		// 文件(夹)是否需忽略
		for (String ignore : ignoreFiles) {
			if (name.equalsIgnoreCase(ignore)) {
				return false;
			}
		}
		// 后缀满足检查条件(JS&CSS...)
		for (String suffix : ignoreSuffix) {
			if (name.endsWith(suffix)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 压缩文件
	 * @param source 待压缩源文件
	 * @param target 压缩后目标文件
	 */
	public static void doCompress(File source, File target) {
		String name = source.getName();
		int pointIdx = name.lastIndexOf(".");
		String suffix = pointIdx > -1 ? name.substring(pointIdx) : "";
		if (suffix.equalsIgnoreCase(".js")) {
			compressJs(source, target);
		} else if (suffix.equalsIgnoreCase(".css")) {
			compressCss(source, target);
		}
	}

	/**
	 * 压缩并加密JS文件
	 * @param source 待压缩源文件
	 * @param target 压缩后目标文件
	 */
	private static void compressJs(File source, File target) {
		try {
			String inputPath = source.getAbsolutePath();
			String outPath = target.getAbsolutePath();
			String[] args = {"--js_output_file=" + outPath, inputPath};
			CommandLineRunner runner = new CommandLineRunner(args);
			int result = -1;
			if (runner.shouldRunCompiler()) {
				result = runner.doRun();
			}
			if (result != 0) {
				Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new BusinessException("压缩JS失败，源文件: " + source.getAbsolutePath() + ", 错误信息: " + e.getMessage());
		}
	}

	/**
	 * 压缩CSS文件
	 * @param source 待压缩源文件
	 * @param target 压缩后目标文件
	 */
	private static void compressCss(File source, File target) {
		try (Reader in = new InputStreamReader(new FileInputStream(source), StandardCharsets.UTF_8);
		     Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
			CssCompressor compressor = new CssCompressor(in);
			compressor.compress(writer, -1);
			writer.flush();
		} catch (IOException e) {
			try {
				Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ignored) {
			}
		}
	}
}