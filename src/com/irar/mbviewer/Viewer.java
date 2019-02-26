package com.irar.mbviewer;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.RenderingHints;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;



public class Viewer extends JPanel implements Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static JFrame window;
	public static Viewer instance = new Viewer();
	public static int CWIDTH = 512;
	public static int CHEIGHT = 512;
	public static int WIDTH = 512;
	public static int HEIGHT = 512;
	public static int resW = 512;
	public static int resH = 512;
	public static volatile BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	public static MBInfo info = new MBInfo();
	public static boolean mousePressed = false;
	public static int pressedX = 0;
	public static int pressedY = 0;
	public static volatile Thread current = null;
	public static volatile List<Thread> waitingFR = new ArrayList<>();
	public static RenderInfo renderInfo = new RenderInfo(instance);
	public static TextField iterField;
	public static boolean hist = false;
	public static List<Palette> palettes = PaletteSaveHandler.getPaletteData();
	static {
		try {
			info.setPalette(palettes.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static boolean dragPressed = false;
	public static int dragX = 0;
	public static int dragY = 0;
	public static boolean initialized = false;

	public static void main(String[] args) {
		try {
			info.setX(new BigDecimal(args[0]));
			info.setY(new BigDecimal(args[1]));
			info.setZoom(SizedDouble.parseSizedDouble(args[2]));
			if(args.length == 4) {
				info.setIterations(Integer.parseInt(args[3]));
			}
		}catch(Exception e) {
			try{
				File saved = new File(args[0]);
				setFile(saved);
			}catch(Exception e2) {}
		}
		
		window = new JFrame("Viewer");
		
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());
		addMenu(window);
		window.add(instance, BorderLayout.CENTER);
		JPanel panel2 = new JPanel(new BorderLayout());
		window.add(panel2, BorderLayout.SOUTH);
		JPanel panel1 = new JPanel(new BorderLayout());
		addIter(panel1);
		addRes(panel1);
		window.add(panel1, BorderLayout.EAST);
		window.pack();
		window.setResizable(false);
		window.setVisible(true);

		instance.setSize(new Dimension(WIDTH, HEIGHT));
		instance.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		instance.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		instance.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		addML(instance);
		
		window.pack();
		window.setLocationRelativeTo(null);

		URL url = Viewer.class.getResource("res/smback1.png");
		BufferedImage img = null;
		try {
			img = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		window.setIconImage(img);
		instance.start();
		
		initialized = true;
		drawFractal(info);
	}
	
	private static void setFile(File file) {
		if(file.exists()) {
			MBInfo info1 = MBInfoGetter.getInfo(file);
			if(info1 != null && info1.wasInitialized()) {
				info = info1;
				if(iterField != null) {
					iterField.setText("" + info.getIterations());
				}
			}
		}
	}

	private static void addMenu(JFrame window) {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu paletteMenu = new JMenu("Palette");
		JMenu helpMenu = new JMenu("Help");
		
		JMenuItem openFile = new JMenuItem("Open");
		openFile.addActionListener(new OpenF());
		
		JMenuItem saveLoc = new JMenuItem("Save Location");
		JMenuItem saveImage = new JMenuItem("Save Image");
		saveLoc.addActionListener(new SaveL());
		saveImage.addActionListener(new SaveP());
		
		List<JCheckBoxMenuItem> pButtons = new ArrayList<>();
		JCheckBoxMenuItem histB = new JCheckBoxMenuItem("Histogram", false);
		histB.addActionListener((e) -> {
			if(histB.isSelected()) {
				Viewer.hist = true;
			}else {
				Viewer.hist = false;
			}
			helper.recolor(bi, info, new ProgressMonitorFactory(renderInfo));
		});
		paletteMenu.add(histB);
		paletteMenu.addSeparator();
		ActionListener a = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = Integer.parseInt(e.getActionCommand());
				for(int i = 0; i < pButtons.size(); i++) {
					if(i == index) {
						pButtons.get(i).setSelected(true);
					}else {
						pButtons.get(i).setSelected(false);
					}
				}
				new Thread(() -> {
					info.setPalette(palettes.get(index));
					helper.recolor(bi, info, new ProgressMonitorFactory(renderInfo));
				}).start();
			}
		};
		for(int i = 0; i < palettes.size(); i++) {
			JCheckBoxMenuItem pb = new JCheckBoxMenuItem(palettes.get(i).name);
			pb.setActionCommand(i + "");
			pb.addActionListener(a);
			paletteMenu.add(pb);
			pButtons.add(pb);
		}
		
		JMenuItem clearCache = new JMenuItem("Clear Cache");
		clearCache.addActionListener((act) -> {
			clearCache();
		});
		
		fileMenu.add(openFile);
		fileMenu.add(saveLoc);
		fileMenu.add(saveImage);
		
		helpMenu.add(clearCache);
		
		menuBar.add(fileMenu);
		menuBar.add(paletteMenu);
		menuBar.add(helpMenu);
		
		window.setJMenuBar(menuBar);
	}

	private static void clearCache() {
		C2ArrayList.clearCache();
	}

	private static void addML(Viewer canvas) {
		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == 3) {
					int x = e.getX() - CWIDTH / 2;
					int y = e.getY() - CHEIGHT / 2;
					SizedDouble fX = new SizedDouble(x).divide(CWIDTH).multiply(info.getZoom());
					SizedDouble fY = new SizedDouble(y).divide(CHEIGHT).multiply(info.getZoom());
					info.setX(info.getX().add(fX.multiply(4).asBigDecimal(info.getX().scale() + 4)));
					info.setY(info.getY().add(fY.multiply(4).asBigDecimal(info.getY().scale() + 4)));
					info.setZoom(info.getZoom().multiply(2));
					BufferedImage tempBI = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics tg = tempBI.getGraphics();
					tg.drawImage(bi, 0, 0, null);
					Graphics g = bi.getGraphics();
					g.clearRect(0, 0, bi.getWidth(), bi.getHeight());
					int ex = e.getX();
					int ey = e.getY();
					g.drawImage(tempBI, WIDTH / 2 - ex / 2, HEIGHT / 2 - ey / 2, WIDTH - ex / 2, HEIGHT - ey / 2, 0, 0, tempBI.getWidth(), tempBI.getHeight(), null);
					drawFractal(info);
				}
			}

			public void mousePressed(MouseEvent e) {
				pressedX = e.getX();
				pressedY = e.getY();
				dragX = e.getX();
				dragY = e.getY();
				if(e.getButton() == 1) {
					mousePressed = true;
				}
				if(e.getButton() == 2) {
					dragPressed = true;
				}
			}
			public void mouseReleased(MouseEvent e) {
				int relX = e.getX();
				int relY = e.getY();
				int difX = Math.abs(relX - pressedX);
				int difY = Math.abs(relY - pressedY);
				int difnX = relX - pressedX;
				int difnY = relY - pressedY;
				if(difX == 0 && difY == 0) {
					if(e.getButton() == 2) {
						dragPressed = false;
						difX = Math.abs(relX - CWIDTH / 2);
						difY = Math.abs(relY - CHEIGHT / 2);
						difnX = relX - CWIDTH / 2;
						difnY = relY - CHEIGHT / 2;
						int x = difnX;
						int y = difnY;
						SizedDouble fX = new SizedDouble(x).divide(CWIDTH).multiply(info.getZoom());
						SizedDouble fY = new SizedDouble(y).divide(CHEIGHT).multiply(info.getZoom());
						info.setX(info.getX().add(fX.multiply(4).asBigDecimal(info.getX().scale() + 4)));
						info.setY(info.getY().add(fY.multiply(4).asBigDecimal(info.getY().scale() + 4)));
						drawFractal(info);
					}
					return;
				}
				boolean xGreater = difX > difY;
				if(e.getButton() == 1) {
					int x = pressedX - CWIDTH / 2;
					int y = pressedY - CHEIGHT / 2;
					SizedDouble fX = new SizedDouble(x).divide(CWIDTH).multiply(info.getZoom());
					SizedDouble fY = new SizedDouble(y).divide(CHEIGHT).multiply(info.getZoom());
					info.setX(info.getX().add(fX.multiply(4).asBigDecimal(info.getX().scale() + 4)));
					info.setY(info.getY().add(fY.multiply(4).asBigDecimal(info.getY().scale() + 4)));
					if(xGreater) {
						info.setZoom(info.getZoom().multiply((double) difX * 2 / CWIDTH));
					}else {
						info.setZoom(info.getZoom().multiply((double) difY * 2 / CHEIGHT));
					}
					mousePressed = false;
					BufferedImage tempBI = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics tg = tempBI.getGraphics();
					tg.drawImage(bi, 0, 0, null);
					Graphics g = bi.getGraphics();
					g.clearRect(0, 0, bi.getWidth(), bi.getHeight());
					if(g instanceof Graphics2D) {
						((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					}
					if(xGreater) {
						g.drawImage(tempBI, 0, 0, WIDTH, HEIGHT, pressedX - difX, pressedY - difX, pressedX + difX, pressedY + difX, null);
					}else {
						g.drawImage(tempBI, 0, 0, WIDTH, HEIGHT, pressedX - difY, pressedY - difY, pressedX + difY, pressedY + difY, null);
					}
					drawFractal(info);
				}
				if(e.getButton() == 2) {
					int x = -difnX;
					int y = -difnY;
					SizedDouble fX = new SizedDouble(x).divide(CWIDTH).multiply(info.getZoom());
					SizedDouble fY = new SizedDouble(y).divide(CHEIGHT).multiply(info.getZoom());
					info.setX(info.getX().add(fX.multiply(4).asBigDecimal(info.getX().scale() + 4)));
					info.setY(info.getY().add(fY.multiply(4).asBigDecimal(info.getY().scale() + 4)));
					dragPressed = false;
					BufferedImage tempBI = new BufferedImage(CWIDTH, CHEIGHT, BufferedImage.TYPE_INT_RGB);
					Graphics tg = tempBI.getGraphics();
					tg.drawImage(bi, difnX, difnY, CWIDTH + difnX, CHEIGHT + difnY, 0, 0, bi.getWidth(), bi.getHeight(), null);
					Graphics g = bi.getGraphics();
					g.drawImage(tempBI, 0, 0, null);
					drawFractal(info);
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});

	}

	private static void addIter(JPanel frame) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel panela = new JPanel();
		panela.setLayout(new BoxLayout(panela, BoxLayout.X_AXIS));
		Button up = new Button("x2");
		iterField = new TextField(info.getIterations() + "");
		iterField.setColumns(6);
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				info.setIterations(Math.max(32, info.getIterations() * 2));
				iterField.setText("" + info.getIterations());
				drawFractal(info);
			}
		});
		Button down = new Button("/2");
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				info.setIterations(Math.max(32, info.getIterations() / 2));
				iterField.setText("" + info.getIterations());
				drawFractal(info);
			}
		});
		
		iterField.setMaximumSize(new Dimension(10000, 30));
		up.setMaximumSize(new Dimension(10000, 30));
		down.setMaximumSize(new Dimension(10000, 30));
		
		panela.add(down);
		panela.add(iterField);
		panela.add(up);
		panel.add(new JLabel("Iterations:"));
		panel.add(panela);
		renderInfo.addToPanel(panel);
		frame.add(panel, BorderLayout.WEST);
	}
	static JCheckBox sp;
	private static void addRes(JPanel frame) {
		JPanel panelx = new JPanel();
		panelx.setLayout(new BoxLayout(panelx, BoxLayout.Y_AXIS));
		JPanel panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
		JPanel panel2z = new JPanel();
		panel2z.setLayout(new BoxLayout(panel2z, BoxLayout.X_AXIS));
		JPanel panel2a = new JPanel();
		panel2a.setLayout(new BoxLayout(panel2a, BoxLayout.X_AXIS));
		JPanel panel2b = new JPanel();
		panel2b.setLayout(new BoxLayout(panel2b, BoxLayout.X_AXIS));
		JPanel panel2c = new JPanel();
		panel2c.setLayout(new BoxLayout(panel2c, BoxLayout.X_AXIS));
		JPanel panel2d = new JPanel();
		panel2d.setLayout(new BoxLayout(panel2d, BoxLayout.X_AXIS));
		JPanel panel2e = new JPanel();
		panel2e.setLayout(new BoxLayout(panel2e, BoxLayout.X_AXIS));
		JPanel resSc4 = new JPanel();
		resSc4.setLayout(new BoxLayout(resSc4, BoxLayout.X_AXIS));

		TextField field = new TextField(resW + " x " + resH);
		field.setColumns(6);
		TextField field2 = new TextField("" + 2);
		field2.setColumns(6);
		TextField field2b = new TextField("" + 0);
		field2b.setColumns(6);
		TextField field3 = new TextField("" + 1);
		field3.setColumns(6);
		TextField field4 = new TextField("" + 0);
		field4.setColumns(6);
		
		Button up = new Button("x2");
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resH = (int) Math.max(4, (resH * 2));
				HEIGHT = resH;
				BufferedImage bi = new BufferedImage(resW, resH, BufferedImage.TYPE_INT_RGB);
				bi.getGraphics().drawImage(Viewer.bi, 0, 0, resW, resH, 0, 0, Viewer.bi.getWidth(), Viewer.bi.getHeight(), null);
				Viewer.bi = bi;
				field.setText(resW + " x " + resH);
				drawFractal(info);
			}
		});
		Button down = new Button("/2");
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resH = (int) Math.max(4, resH / 2);
				HEIGHT = resH;
				BufferedImage bi = new BufferedImage(resW, resH, BufferedImage.TYPE_INT_RGB);
				bi.getGraphics().drawImage(Viewer.bi, 0, 0, resW, resH, 0, 0, Viewer.bi.getWidth(), Viewer.bi.getHeight(), null);
				Viewer.bi = bi;
				field.setText(resW + " x " + resH);
				drawFractal(info);
			}
		});
		Button left = new Button("x2");
		left.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resW = (int) Math.max(4, (resW * 2));
				WIDTH = resW;
				BufferedImage bi = new BufferedImage(resW, resH, BufferedImage.TYPE_INT_RGB);
				bi.getGraphics().drawImage(Viewer.bi, 0, 0, resW, resH, 0, 0, Viewer.bi.getWidth(), Viewer.bi.getHeight(), null);
				Viewer.bi = bi;
				field.setText(resW + " x " + resH);
				drawFractal(info);
			}
		});
		Button right = new Button("/2");
		right.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resW = (int) Math.max(4, resW / 2);
				WIDTH = resW;
				BufferedImage bi = new BufferedImage(resW, resH, BufferedImage.TYPE_INT_RGB);
				bi.getGraphics().drawImage(Viewer.bi, 0, 0, resW, resH, 0, 0, Viewer.bi.getWidth(), Viewer.bi.getHeight(), null);
				Viewer.bi = bi;
				field.setText(resW + " x " + resH);
				drawFractal(info);
			}
		});
		Button dfault = new Button("Default Resolution");
		dfault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resW = 512;
				WIDTH = resW;
				resH = 512;
				HEIGHT = resH;
				BufferedImage bi = new BufferedImage(resW, resH, BufferedImage.TYPE_INT_RGB);
				bi.getGraphics().drawImage(Viewer.bi, 0, 0, resW, resH, 0, 0, Viewer.bi.getWidth(), Viewer.bi.getHeight(), null);
				Viewer.bi = bi;
				field.setText(resW + " x " + resH);
				drawFractal(info);
			}
		});
		Button res = new Button("Screen Resolution");
		res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resW = Toolkit.getDefaultToolkit().getScreenSize().width;
				WIDTH = resW;
				resH = Toolkit.getDefaultToolkit().getScreenSize().height;
				HEIGHT = resH;
				BufferedImage bi = new BufferedImage(resW, resH, BufferedImage.TYPE_INT_RGB);
				bi.getGraphics().drawImage(Viewer.bi, 0, 0, resW, resH, 0, 0, Viewer.bi.getWidth(), Viewer.bi.getHeight(), null);
				Viewer.bi = bi;
				field.setText(resW + " x " + resH);
				drawFractal(info);
			}
		});
		Button k4 = new Button("4k");
		k4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resW = 3840;
				WIDTH = resW;
				resH = 2160;
				HEIGHT = resH;
				BufferedImage bi = new BufferedImage(resW, resH, BufferedImage.TYPE_INT_RGB);
				bi.getGraphics().drawImage(Viewer.bi, 0, 0, resW, resH, 0, 0, Viewer.bi.getWidth(), Viewer.bi.getHeight(), null);
				Viewer.bi = bi;
				field.setText(resW + " x " + resH);
				drawFractal(info);
			}
		});
		JTextField width = new JTextField();
		JTextField height = new JTextField();
		width.setColumns(7);
		height.setColumns(7);
		width.setText(512 + "");
		height.setText(512 + "");
		Button custom = new Button("Custom:");
		custom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resW = Integer.parseInt(width.getText());
				WIDTH = resW;
				resH = Integer.parseInt(height.getText());
				HEIGHT = resH;
				BufferedImage bi = new BufferedImage(resW, resH, BufferedImage.TYPE_INT_RGB);
				bi.getGraphics().drawImage(Viewer.bi, 0, 0, resW, resH, 0, 0, Viewer.bi.getWidth(), Viewer.bi.getHeight(), null);
				Viewer.bi = bi;
				field.setText(resW + " x " + resH);
				drawFractal(info);
			}
		});
		Button custom2 = new Button("Set Power:");
		custom2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double x = Double.parseDouble(field2.getText());
				double y = Double.parseDouble(field2b.getText());
				info.setPower(new Complex(x, y));
				drawFractal(info);
			}
		});
		
		Button custom3 = new Button("Set Oversample:");
		custom3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				info.setOversample(Integer.parseInt(field3.getText()));
				drawFractal(info);
			}
		});
		
		Button custom4 = new Button("Set Blur:");
		custom4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				blur = Double.parseDouble(field4.getText());
				drawFractal(info);
			}
		});
		
		Button period = new Button("Find Minibrot");
		period.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				thread = new Thread(new Runnable(){
					@Override
					public void run() {
						Complex2 compLoc = null;
						SizedDouble compZoom = SizedDouble.ZERO;
						int zoomMod = 1;
						int failCount = 0;
						while(compLoc == null || compZoom.compareTo(SizedDouble.ZERO) == 0) {
							zoomMod++;
							failCount++;
							try {
								if(helper == null) {
									helper = new MBHelper();
								}
								ZoomLoc comp = helper.findMini(info, resW, resH, zoomMod);
								compLoc = comp.loc;
								compZoom = comp.zoom;
								if(comp.loc != null) {
									if(compZoom.compareTo(SizedDouble.ZERO) == 0) {
										renderInfo.minIter.setText("Failed to find minibrot");
									}else {
										info.setX(comp.loc.x);
										info.setY(comp.loc.y);
										info.setZoom(comp.zoom);
										drawFractal(info);
									}
								}
							}catch(Exception e) {
								failCount++;
							}
							if(failCount >= 10) {
								break;
							}
						}
						window.validate();
						window.pack();
					}
				});
				thread.start();
			}
		});
		
		sp = new JCheckBox("Shuffle Points");
		sp.setSelected(true);
		sp.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent e) {
		        if(e.getStateChange() == ItemEvent.SELECTED) {
		        	shufflePoints = true;
		        } else {
		        	shufflePoints = false;
		        };
		    }
		});
		
		field.setMaximumSize(new Dimension(10000, 30));
		field2.setMaximumSize(new Dimension(10000, 30));
		field2b.setMaximumSize(new Dimension(10000, 30));
		field3.setMaximumSize(new Dimension(10000, 30));
		field4.setMaximumSize(new Dimension(10000, 30));
		width.setMaximumSize(new Dimension(10000, 30));
		height.setMaximumSize(new Dimension(10000, 30));
		up.setMaximumSize(new Dimension(10000, 30));
		down.setMaximumSize(new Dimension(10000, 30));
		left.setMaximumSize(new Dimension(10000, 30));
		right.setMaximumSize(new Dimension(10000, 30));
		custom.setMaximumSize(new Dimension(10000, 30));
		custom2.setMaximumSize(new Dimension(10000, 30));
		custom3.setMaximumSize(new Dimension(10000, 30));
		custom4.setMaximumSize(new Dimension(10000, 30));
		period.setMaximumSize(new Dimension(10000, 30));
		dfault.setMaximumSize(new Dimension(10000, 30));
		res.setMaximumSize(new Dimension(10000, 30));
		k4.setMaximumSize(new Dimension(10000, 30));
		
		resSc4.add(res);
		resSc4.add(k4);
		panel2c.add(custom2);
		panel2c.add(field2);
		panel2c.add(field2b);
		panel2d.add(custom3);
		panel2d.add(field3);
		panel2e.add(custom4);
		panel2e.add(field4);
		panel2b.add(new JLabel("Height:  "));
		panel2b.add(up);
		panel2b.add(down);
		panel2a.add(new JLabel("Width:  "));
		panel2a.add(left);
		panel2a.add(right);
		panel2z.add(custom);
		panel2z.add(new JLabel("  Width:  "));
		panel2z.add(width);
		panel2z.add(new JLabel("  Height:  "));
		panel2z.add(height);
		panel1.add(field);
		panel1.add(dfault);
		panel1.add(resSc4);
		panel1.add(panel2z);
		panel1.add(panel2a);
		panel1.add(panel2b);
//		panel1.add(panel2c);
		panel1.add(panel2d);
		panel1.add(panel2e);
		panel1.add(period);
//		panel1.add(sp);
		panelx.add(new JLabel("Resolution:"));
		panelx.add(panel1);
		frame.add(panelx, BorderLayout.CENTER);
	}
	static Thread thread;
	protected static double blur = 0;
	protected static boolean shufflePoints = true;
	protected static MBHelper helper;
	private static void drawFractal(MBInfo info) {
		thread = new Thread(new Runnable(){
			@Override
			public void run() {
				helper = new MBHelper();
				helper.getSet(bi, info, new ProgressMonitorFactory(renderInfo));
				iterField.setText("" + info.getIterations());
			}
		});
		if(current != null) {
			current = thread;
		}
		thread.start();
	}
	
	private void start() {
		new Thread(this).start();
	}

	@Override
	public void run() {
		long lastTime = System.nanoTime();
		double unprocessed = 0;
		double nsPerTick = 1000000000.0 / 60;
		int frames = 0;
		int ticks = 0;
		long lastTimer1 = System.currentTimeMillis();
		long lastTimer2 = System.currentTimeMillis();

		while (true) {
			long now = System.nanoTime();
			unprocessed += (now - lastTime) / nsPerTick;
			lastTime = now;
			boolean shouldRender = true;
			while (unprocessed >= 1) {
				ticks++;
//				tick();
				unprocessed -= 1;
				shouldRender = true;
			}

			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (shouldRender) {
				frames++;
				render();
			}

			if (System.currentTimeMillis() - lastTimer1 > 1000) {
				lastTimer1 += 1000;
//				System.out.println((ticks) + " ticks, " + (frames) + " fps, " + "x: " + locX.doubleValue() + ", y: " + locY.doubleValue() + ", zoom: " + zoom);
				frames = 0;
				ticks = 0;
			}
			if (System.currentTimeMillis() - lastTimer2 > 15896) {
				lastTimer2 += 15896;
			}
		}
	}

	private void render() {
		this.repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.clearRect(0, 0, CWIDTH, CHEIGHT);
		
		if(dragPressed) {
			int mX = MouseInfo.getPointerInfo().getLocation().x - this.getLocationOnScreen().x;
			int mY = MouseInfo.getPointerInfo().getLocation().y - this.getLocationOnScreen().y;
			int difX = mX - dragX;
			int difY = mY - dragY;
			g.drawImage(bi, difX, difY, CWIDTH + difX, CHEIGHT + difY, 0, 0, bi.getWidth(), bi.getHeight(), null);
		}else {
			g.drawImage(bi, 0, 0, CWIDTH, CHEIGHT, 0, 0, bi.getWidth(), bi.getHeight(), null);
		}
		
		if(mousePressed) {
			int mX = MouseInfo.getPointerInfo().getLocation().x - this.getLocationOnScreen().x;
			int mY = MouseInfo.getPointerInfo().getLocation().y - this.getLocationOnScreen().y;
			int difX = Math.abs(mX - pressedX);
			int difY = Math.abs(mY - pressedY);
			boolean xGreater = (double) difX / CWIDTH > (double) difY / CHEIGHT;
			g.setColor(new Color(255, 255, 255));
			if(xGreater) {
				g.drawRect(pressedX - difX, pressedY - difX, (int) (difX * 2), (int) ((double) CHEIGHT / CWIDTH * difX * 2));
			}else {
				g.drawRect(pressedX - difY, pressedY - difY, (int) ((double) CWIDTH / CHEIGHT * difY * 2), (int) (difY * 2));
			}
		}
		
		g.dispose();
	}
	
	static class SaveL implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();
			FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("IrresoluteArkia Mandelbrot Zoom Data (*.iaz)", "iaz");
	        // add filters
	        c.addChoosableFileFilter(txtFilter);
	        c.setFileFilter(txtFilter);
			File theDir = new File("toconvert");
			if (!theDir.exists()) {
			    System.out.println("creating directory: " + theDir.getName());
			    boolean result = false;
			    try{
			        theDir.mkdir();
			        result = true;
			    } 
			    catch(SecurityException se){
			    }        
			    if(result) {    
			        System.out.println("DIR created");  
			    }
			}
			c.setCurrentDirectory(theDir);
			// Demonstrate "Save" dialog:
			int rVal = c.showSaveDialog(window);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				if(c.getSelectedFile().getName().endsWith(".iaz")) {
					saveFile(c.getSelectedFile());
				}else {
					c.setSelectedFile(new File(c.getSelectedFile().getPath() + ".iaz"));
					saveFile(c.getSelectedFile());
				}
			}
			if (rVal == JFileChooser.CANCEL_OPTION) {}
		}

		private void saveFile(File selectedFile) {
			try {
				PrintWriter writer = new PrintWriter(selectedFile);
				HashMap<String, String> data = getData();
				for(String key : data.keySet()) {
					writer.println(key + ":" + data.get(key));
				}
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private static HashMap<String, String> getData() {
		HashMap<String, String> data = info.getData();
		return data;
	}


	static class SaveP implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();
			FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("PNG Image File (*.png)", "png");
	        c.addChoosableFileFilter(txtFilter);
	        c.setFileFilter(txtFilter);
			File theDir = new File("export");
			if (!theDir.exists()) {
			    System.out.println("creating directory: " + theDir.getName());
			    boolean result = false;
			    try{
			        theDir.mkdir();
			        result = true;
			    } 
			    catch(SecurityException se){
			    }        
			    if(result) {    
			        System.out.println("DIR created");  
			    }
			}
			c.setCurrentDirectory(theDir);
			int rVal = c.showSaveDialog(window);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				if(c.getSelectedFile().getName().endsWith(".png")) {
					saveFile(c.getSelectedFile());
				}else {
					c.setSelectedFile(new File(c.getSelectedFile().getPath() + ".png"));
					saveFile(c.getSelectedFile());
				}
			}
			if (rVal == JFileChooser.CANCEL_OPTION) {}
		}

		private void saveFile(File selectedFile) {
			try {
				ImageIO.write(bi, "png", selectedFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	static class OpenF implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();
			FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("IrresoluteArkia Mandelbrot Zoom Data (*.iaz)", "iaz");
	        c.addChoosableFileFilter(txtFilter);
	        c.setFileFilter(txtFilter);
			File theDir = new File("export");
			if (!theDir.exists()) {
			    System.out.println("creating directory: " + theDir.getName());
			    boolean result = false;
			    try{
			        theDir.mkdir();
			        result = true;
			    } 
			    catch(SecurityException se){
			    }        
			    if(result) {    
			        System.out.println("DIR created");  
			    }
			}
			c.setCurrentDirectory(theDir);
			int rVal = c.showOpenDialog(window);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				setFile(c.getSelectedFile());
				drawFractal(info);
			}
			if (rVal == JFileChooser.CANCEL_OPTION) {}
		}
	}

	public void pack() {
		window.pack();
	}

}
