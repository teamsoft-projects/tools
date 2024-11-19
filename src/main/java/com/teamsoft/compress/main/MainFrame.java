package com.teamsoft.compress.main;

import com.teamsoft.common.exception.BusinessException;
import com.teamsoft.compress.util.CompressUtil;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JOptionPane.YES_NO_OPTION;

/**
 * 压缩工具主入口
 * @author alex
 * @version 2019/12/10
 */
public class MainFrame extends JFrame {
	// 选择文件夹按钮
	private JButton btnChooseDir;
	// JS复选框
	private JCheckBox boxJs;
	// CSS复选框
	private JCheckBox boxCss;
	// 已选择文件夹
	private JTextField txtfChoosed;
	// 压缩按钮
	private JButton btnCompress;
	// 进度条
	private JProgressBar progress;
	// 进度条进度
	private int progressVal;
	// 进度条总进度
	private int totalVal;

	// 当前选中的文件夹
	private File inputDirectory;
	// 输出的文件夹
	private File outDirectory;
	// 需压缩的文件后缀
	private final List<String> compressSuffix = new ArrayList<>();
	// 不处理的文件夹名
	private final List<String> ignoreFiles = Arrays.asList(".svn", ".idea", "target", ".git");
	// 不处理的文件后缀
	private final List<String> ignoreSuffix = Arrays.asList(".iml", ".gitignore");

	/// 构造方法
	private MainFrame() {
		_setMain();
		_addActionListener();
	}

	/**
	 * 设置窗体UI
	 */
	private void _setMain() {
		JPanel pnlMain = (JPanel) getContentPane();
		pnlMain.setLayout(null);

		// 顶层容器
		JPanel pnlTop = new JPanel(null);
		pnlTop.setBounds(10, 10, 377, 272);
		pnlTop.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		pnlMain.add(pnlTop);

		// 选择压缩类型
		JLabel lblCompressType = new JLabel("压缩类型");
		lblCompressType.setBounds(20, 20, 80, 25);
		pnlTop.add(lblCompressType);

		// JS
		boxJs = new JCheckBox();
		boxJs.setSelected(true);
		boxJs.setBounds(100, 20, 25, 25);
		pnlTop.add(boxJs);
		JLabel lblJs = new JLabel("JS");
		lblJs.setBounds(125, 20, 80, 25);
		pnlTop.add(lblJs);

		// CSS
		boxCss = new JCheckBox();
		boxCss.setSelected(true);
		boxCss.setBounds(175, 20, 25, 25);
		pnlTop.add(boxCss);
		JLabel lblCss = new JLabel("CSS");
		lblCss.setBounds(205, 20, 80, 25);
		pnlTop.add(lblCss);

		// 选择文件夹
		JLabel lblChooseDir = new JLabel("选择文件夹");
		lblChooseDir.setBounds(20, 60, 100, 25);
		pnlTop.add(lblChooseDir);
		btnChooseDir = new JButton("选择...");
		btnChooseDir.setBounds(102, 60, 90, 25);
		pnlTop.add(btnChooseDir);

		// 已选择文件夹
		JLabel lblChoosed = new JLabel("已选定路径");
		lblChoosed.setBounds(20, 100, 80, 25);
		pnlTop.add(lblChoosed);
		txtfChoosed = new JTextField();
		txtfChoosed.setBounds(102, 100, 260, 25);
		txtfChoosed.setEditable(false);
		pnlTop.add(txtfChoosed);

		// 压缩按钮
		btnCompress = new JButton("压缩...");
		btnCompress.setBounds(102, 140, 100, 25);
		pnlTop.add(btnCompress);

		// 进度条
		progress = new JProgressBar(0, 100);
		progress.setBounds(40, 210, 300, 18);
		progress.setStringPainted(true);
		progress.setVisible(false);
		pnlTop.add(progress);

		setTitle("批量JS/CSS压缩");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(400, 320);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * 添加事件监听
	 */
	private void _addActionListener() {
		// 监听选择文件夹按钮
		btnChooseDir.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("选择待压缩的文件夹");
			chooser.setFileSelectionMode(DIRECTORIES_ONLY);
			int retVal = chooser.showOpenDialog(MainFrame.this);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				inputDirectory = chooser.getSelectedFile();
				txtfChoosed.setText(inputDirectory.getAbsolutePath());
				progress.setValue(0);
				progressVal = 0;
				progress.setVisible(false);
			}
		});
		// 压缩按钮
		btnCompress.addActionListener(e -> {
			boolean isSelectJs = boxJs.isSelected();
			boolean isSelectCss = boxCss.isSelected();
			if (!isSelectJs && !isSelectCss) {
				JOptionPane.showMessageDialog(MainFrame.this, "未选中任何需压缩的文件类型");
				return;
			}
			if (inputDirectory == null || !inputDirectory.exists() || !inputDirectory.isDirectory()) {
				JOptionPane.showMessageDialog(MainFrame.this, "未选中文件夹或文件夹类型错误");
				return;
			}
			compressSuffix.clear();
			if (isSelectJs) {
				compressSuffix.add(".js");
			}
			if (isSelectCss) {
				compressSuffix.add(".css");
			}
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("选择输出路径");
			chooser.setFileSelectionMode(DIRECTORIES_ONLY);
			int retVal = chooser.showOpenDialog(MainFrame.this);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				File outDirectory = chooser.getSelectedFile();
				List<File> allFiles = CompressUtil.checkAllFiles(inputDirectory, ignoreFiles, ignoreSuffix);
				totalVal = allFiles.size();
				progress.setMaximum(totalVal);
				progressVal = 0;
				progress.setValue(0);
				progress.setVisible(true);
				excuteCompress(outDirectory);
				refreshProgressBar();
			}
		});
	}

	/**
	 * 执行压缩命令
	 */
	private void excuteCompress(File outParentDirectory) {
		new Thread(() -> {
			// 获取项目名
			String name = inputDirectory.getName();
			outDirectory = new File(outParentDirectory.getAbsolutePath() + File.separator + name);
			if (outDirectory.exists()) {
				int option = JOptionPane.showConfirmDialog(MainFrame.this, "目标文件夹内已存在同名项目文件夹 " + name + " 是否继续?", "温馨提示", YES_NO_OPTION);
				if (option != JOptionPane.YES_OPTION) {
					return;
				}
			} else {
				outDirectory.mkdirs();
			}
			excuteFiles(inputDirectory);
			progressVal = totalVal;
			JOptionPane.showMessageDialog(MainFrame.this, "压缩完成!");
		}).start();
	}

	/**
	 * 递归处理所有子文件
	 */
	private void excuteFiles(File source) {
		if (ignoreFiles.contains(source.getName())) {
			return;
		}
		String inputPath = inputDirectory.getAbsolutePath();
		// 如果是文件夹，在目标位置创建文件夹
		File outputDir = new File(outDirectory.getAbsolutePath() + File.separator + source.getAbsolutePath().replace(inputPath, ""));
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		File[] fs = source.listFiles();
		if (fs == null) {
			return;
		}
		for (File f : fs) {
			if (f.isFile()) {
				if (!CompressUtil.isFileLegal(f)) {
					continue;
				}
				File outputFile = new File(outDirectory.getAbsolutePath() + File.separator + f.getAbsolutePath().replace(inputPath, ""));
				try {
					String name = f.getName();
					int pointIdx = name.lastIndexOf(".");
					String suffix = pointIdx > -1 ? name.substring(pointIdx) : "";
					// 如果是JS或CSS文件
					if (compressSuffix.contains(suffix)) {
						CompressUtil.doCompress(f, outputFile);
					} else {
						Files.copy(f.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
				} catch (IOException e) {
					e.printStackTrace();
					throw new BusinessException("拷贝文件出错!");
				}
				progressVal++;
			} else {
				excuteFiles(f);
			}
		}
	}

	/**
	 * 更新进度条进度
	 */
	private void refreshProgressBar() {
		new Thread(() -> {
			for (; ; ) {
				if (progressVal >= totalVal) {
					progress.setValue(totalVal);
					break;
				}
				progress.setValue(progressVal);
				try {
					Thread.sleep(20);
				} catch (InterruptedException ignored) {
				}
			}
		}).start();
	}

	/**
	 * 调用入口
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			SwingUtilities.invokeLater(MainFrame::new);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
}