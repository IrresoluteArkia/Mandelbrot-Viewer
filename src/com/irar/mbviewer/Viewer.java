package com.irar.mbviewer;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;



public class Viewer extends Canvas implements Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int iter = 256;
	public static JFrame window;
	public static Viewer instance = new Viewer();
	public static int CWIDTH = 512;
	public static int CHEIGHT = 512;
	public static int WIDTH = 512;
	public static int HEIGHT = 512;
	public static int resW = 512;
	public static int resH = 512;
	public static Complex power = new Complex(2, 0);
//	public static int WIDTH = 640 * 2;
//	public static int HEIGHT = 480 * 2;
	public static volatile BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	public static BigDecimal locX;
	public static BigDecimal locY;
	public static SizedDouble zoom;
	public static boolean mousePressed = false;
	public static int pressedX = 0;
	public static int pressedY = 0;
	public static volatile Thread current = null;
	public static volatile List<Thread> waitingFR = new ArrayList<>();
	public static RenderInfo info = new RenderInfo();
	public static TextField iterField;
	public static Palette currentPalette;
	public static boolean hist = false;
	public static List<Palette> palettes = PaletteSaveHandler.getPaletteData();
	static {
		try {
			currentPalette = palettes.get(0);
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
			locX = new BigDecimal(args[0]);
			locY = new BigDecimal(args[1]);
			zoom = SizedDouble.parseSizedDouble(args[2]);
			if(args.length == 4) {
				iter = Integer.parseInt(args[3]);
			}
		}catch(Exception e) {
			try{
				File saved = new File(args[0]);
				if(saved.exists()) {
					MBInfo info1 = MBInfoGetter.getInfo(saved);
					if(info1 != null && info1.wasInitialized()) {
						locX = info1.x;
						locY = info1.y;
						zoom = info1.zoom;
						iter = info1.iterations;
					}else {
						throw new Exception();
					}
				}else {
					throw new Exception();
				}
			}catch(Exception e2) {
				locX = new BigDecimal(0);
				locY = new BigDecimal(0);
				zoom = new SizedDouble(1);
				iter = 256;
			}
//			SizedDouble.test();
		}
		
		window = new JFrame("Viewer");
		
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());
		window.add(instance, BorderLayout.CENTER);
		JPanel panel2 = new JPanel(new BorderLayout());
		addSave(panel2);
		window.add(panel2, BorderLayout.SOUTH);
		JPanel panel1 = new JPanel(new BorderLayout());
		addIter(panel1);
		addRes(panel1);
		addRadio(panel1);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		window.setIconImage(img);
		instance.start();
		
		initialized = true;
		drawFractal(locX, locY, zoom, iter);
	}

	private static void addRadio(JPanel panel) {
		List<JRadioButton> jrbs = new ArrayList<>();
		ActionListener a = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = Integer.parseInt(e.getActionCommand());
				currentPalette = palettes.get(index);
				MBHelper.helper.recolor(bi, currentPalette, iter, hist);
			}
		};
		
		ButtonGroup g1 = new ButtonGroup();
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		p1.add(new JLabel("Palette:"));
		JCheckBox hist = new JCheckBox("Histogram", false);
		hist.addActionListener((e) -> {
			if(hist.isSelected()) {
				Viewer.hist = true;
			}else {
				Viewer.hist = false;
			}
			MBHelper.helper.recolor(bi, currentPalette, iter, Viewer.hist);
		});
		p1.add(hist);
		for(int i = 0; i < palettes.size(); i++) {
			JRadioButton jrb = new JRadioButton(palettes.get(i).name);
			jrb.setActionCommand(i + "");
			jrb.addActionListener(a);
			g1.add(jrb);
			p1.add(jrb);
			jrbs.add(jrb);
		}
		jrbs.get(0).setSelected(true);
		
		panel.add(p1, BorderLayout.EAST);
	}

	private static void addSave(JPanel panel) {
		Button save = new Button("Save To .iaz");
		save.addActionListener(new SaveL());
		Button savep = new Button("Save To .png");
		savep.addActionListener(new SaveP());
		panel.add(save, BorderLayout.NORTH);
		panel.add(savep, BorderLayout.SOUTH);
	}

	private static void addML(Viewer canvas) {
		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == 3) {
					int x = e.getX() - CWIDTH / 2;
					int y = e.getY() - CHEIGHT / 2;
					SizedDouble fX = new SizedDouble(x).divide(CWIDTH).multiply(zoom);
					SizedDouble fY = new SizedDouble(y).divide(CHEIGHT).multiply(zoom);
					locX = locX.add(fX.multiply(4).asBigDecimal(locX.scale() + 4));
					locY = locY.add(fY.multiply(4).asBigDecimal(locY.scale() + 4));
					zoom = zoom.multiply(2);
					BufferedImage tempBI = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics tg = tempBI.getGraphics();
					tg.drawImage(bi, 0, 0, null);
					Graphics g = bi.getGraphics();
					g.clearRect(0, 0, bi.getWidth(), bi.getHeight());
					int ex = e.getX();
					int ey = e.getY();
					g.drawImage(tempBI, WIDTH / 2 - ex / 2, HEIGHT / 2 - ey / 2, WIDTH - ex / 2, HEIGHT - ey / 2, 0, 0, tempBI.getWidth(), tempBI.getHeight(), null);
					drawFractal(locX, locY, zoom, iter);
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
						SizedDouble fX = new SizedDouble(x).divide(CWIDTH).multiply(zoom);
						SizedDouble fY = new SizedDouble(y).divide(CHEIGHT).multiply(zoom);
						locX = locX.add(fX.multiply(4).asBigDecimal(locX.scale() + 4));
						locY = locY.add(fY.multiply(4).asBigDecimal(locY.scale() + 4));
/*						BufferedImage tempBI = new BufferedImage(CWIDTH, CHEIGHT, BufferedImage.TYPE_INT_RGB);
						Graphics tg = tempBI.getGraphics();
						tg.drawImage(bi, difnX, difnY, CWIDTH + difnX, CHEIGHT + difnY, 0, 0, bi.getWidth(), bi.getHeight(), null);
						Graphics g = bi.getGraphics();
						g.drawImage(tempBI, 0, 0, null);*/
						drawFractal(locX, locY, zoom, iter);
					}
					return;
				}
				boolean xGreater = difX > difY;
				if(e.getButton() == 1) {
					int x = pressedX - CWIDTH / 2;
					int y = pressedY - CHEIGHT / 2;
					SizedDouble fX = new SizedDouble(x).divide(CWIDTH).multiply(zoom);
					SizedDouble fY = new SizedDouble(y).divide(CHEIGHT).multiply(zoom);
					locX = locX.add(fX.multiply(4).asBigDecimal(locX.scale() + 4));
					locY = locY.add(fY.multiply(4).asBigDecimal(locY.scale() + 4));
					if(xGreater) {
						zoom = zoom.multiply((double) difX * 2 / CWIDTH);
					}else {
						zoom = zoom.multiply((double) difY * 2 / CHEIGHT);
					}
					mousePressed = false;
					BufferedImage tempBI = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics tg = tempBI.getGraphics();
					tg.drawImage(bi, 0, 0, null);
					Graphics g = bi.getGraphics();
					g.clearRect(0, 0, bi.getWidth(), bi.getHeight());
					if(xGreater) {
						g.drawImage(tempBI, 0, 0, WIDTH, HEIGHT, pressedX - difX, pressedY - difX, pressedX + difX, pressedY + difX, null);
					}else {
						g.drawImage(tempBI, 0, 0, WIDTH, HEIGHT, pressedX - difY, pressedY - difY, pressedX + difY, pressedY + difY, null);
					}
					drawFractal(locX, locY, zoom, iter);
				}
				if(e.getButton() == 2) {
					int x = -difnX;
					int y = -difnY;
					SizedDouble fX = new SizedDouble(x).divide(CWIDTH).multiply(zoom);
					SizedDouble fY = new SizedDouble(y).divide(CHEIGHT).multiply(zoom);
					locX = locX.add(fX.multiply(4).asBigDecimal(locX.scale() + 4));
					locY = locY.add(fY.multiply(4).asBigDecimal(locY.scale() + 4));
					dragPressed = false;
					BufferedImage tempBI = new BufferedImage(CWIDTH, CHEIGHT, BufferedImage.TYPE_INT_RGB);
					Graphics tg = tempBI.getGraphics();
					tg.drawImage(bi, difnX, difnY, CWIDTH + difnX, CHEIGHT + difnY, 0, 0, bi.getWidth(), bi.getHeight(), null);
					Graphics g = bi.getGraphics();
					g.drawImage(tempBI, 0, 0, null);
					drawFractal(locX, locY, zoom, iter);
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});

	}

/*	private static void addArrows(JFrame frame) {
		BorderLayout layout = new BorderLayout();
		JPanel panel = new JPanel(layout);
		Button up = new Button("^");
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				locY = locY.subtract(zoom.divide(20).asBigDecimal());
				drawFractal(locX, locY, zoom, iter);
			}
		});
		panel.add(up, BorderLayout.NORTH);
		Button down = new Button("v");
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				locY = locY.subtract(zoom.divide(20).asBigDecimal());
				drawFractal(locX, locY, zoom, iter);
			}
		});
		panel.add(down, BorderLayout.SOUTH);
		Button left = new Button("<");
		left.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				locY = locY.subtract(zoom.divide(20).asBigDecimal());
				drawFractal(locX, locY, zoom, iter);
			}
		});
		panel.add(left, BorderLayout.WEST);
		Button right = new Button(">");
		right.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				locY = locY.subtract(zoom.divide(20).asBigDecimal());
				drawFractal(locX, locY, zoom, iter);
			}
		});
		panel.add(right, BorderLayout.EAST);
		frame.add(panel, BorderLayout.NORTH);
	}*/

	private static void addIter(JPanel frame) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel panela = new JPanel();
		panela.setLayout(new BoxLayout(panela, BoxLayout.X_AXIS));
		Button up = new Button("x2");
		iterField = new TextField(iter + "");
		iterField.setColumns(6);
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				iter = Math.max(32, iter * 2);
				iterField.setText("" + iter);
				drawFractal(locX, locY, zoom, iter);
			}
		});
		Button down = new Button("/2");
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				iter = Math.max(32, iter / 2);
				iterField.setText("" + iter);
				drawFractal(locX, locY, zoom, iter);
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
		info.addToPanel(panel);
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
				drawFractal(locX, locY, zoom, iter);
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
				drawFractal(locX, locY, zoom, iter);
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
				drawFractal(locX, locY, zoom, iter);
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
				drawFractal(locX, locY, zoom, iter);
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
				drawFractal(locX, locY, zoom, iter);
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
				drawFractal(locX, locY, zoom, iter);
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
				drawFractal(locX, locY, zoom, iter);
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
				drawFractal(locX, locY, zoom, iter);
			}
		});
		Button custom2 = new Button("Set Power:");
		custom2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double x = Double.parseDouble(field2.getText());
				double y = Double.parseDouble(field2b.getText());
				power = new Complex(x, y);
				drawFractal(locX, locY, zoom, iter);
			}
		});
		
		Button custom3 = new Button("Set Oversample:");
		custom3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				oversample = Integer.parseInt(field3.getText());
				drawFractal(locX, locY, zoom, iter);
			}
		});
		
		Button custom4 = new Button("Set Blur:");
		custom4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				blur = Double.parseDouble(field4.getText());
				drawFractal(locX, locY, zoom, iter);
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
								ZoomLoc comp = new MBHelper().findMini(locX, locY, zoom, resW, resH, zoomMod);
								compLoc = comp.loc;
								compZoom = comp.zoom;
								if(comp.loc != null) {
									if(compZoom.compareTo(SizedDouble.ZERO) == 0) {
										info.minIter.setText("Failed to find minibrot");
									}else {
										locX = comp.loc.x;
										locY = comp.loc.y;
										zoom = comp.zoom;
										MBHelper.lastHelper = null;
										drawFractal(locX, locY, zoom, iter);
									}
								}
							}catch(Exception e) {
								failCount++;
							}
							if(failCount >= 10) {
								break;
							}
						}
//						int per = new MBHelper().getPeriod(locX, locY, zoom, resW, resH);
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
	protected static int oversample = 1;
	protected static double blur = 0;
	protected static boolean shufflePoints = true;
	private static void drawFractal(BigDecimal locX, BigDecimal locY, SizedDouble zoom2, int iter) {
		thread = new Thread(new Runnable(){
			@Override
			public void run() {
//				if(power == (double) 2) {
					new MBHelper().getSetP(bi, currentPalette, locX, locY, zoom2, iter, oversample, blur, power, shufflePoints, hist);
/*				}else if(Math.abs(zoom2.size) <= 14) {
					new MBHelper().getSet(bi, Main.currentPalette, locX.doubleValue(), locY.doubleValue(), zoom2, iter - 1, power);
				}else {
					new MBHelper().getSet(bi, Main.currentPalette, locX, locY, zoom2, iter - 1, power);
				}*/
			}
		});
		if(current != null) {
			current = thread;
		}
		thread.start();
	}

/*	private static void drawFractalE(BigDecimal locX, BigDecimal locY, double zoom, int iter) {
//		Main.getSet(bi, locX, locY, zoom, iter);
//		if(Math.abs(Math.log10(zoom)) <= 14) {
//			Main.getSet(bi, locX.doubleValue(), locY.doubleValue(), zoom, iter);
//		}else {
			Main.getSet(bi, locX, locY, zoom, iter - 1);
//		}
		System.out.println("Drew Fractal");
	}*/

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
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(2);
			requestFocus();
			return;
		}

		Graphics g = bs.getDrawGraphics();

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
				g.drawRect(pressedX - difX, pressedY - difX, (int) (/*(double) WIDTH / HEIGHT * */difX * 2), (int) ((double) CHEIGHT / CWIDTH * difX * 2));
			}else {
				g.drawRect(pressedX - difY, pressedY - difY, (int) ((double) CWIDTH / CHEIGHT * difY * 2), (int) (/*(double) HEIGHT / WIDTH * */difY * 2));
			}
		}
		
		g.dispose();
		bs.show();
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
				writer.println("x:" + locX);
				writer.println("y:" + locY);
				writer.println("zoom:" + zoom);
				writer.println("iterations:" + iter);
				writer.println("power:" + power);
				String pl = "palette:0:" + currentPalette.name + ":" + currentPalette.colorlength + ":" + currentPalette.loop;
				for(int color : currentPalette.init) {
					pl += ":" + color;
				}
				writer.println(pl);
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private HashMap<String, String> getData() {
		HashMap<String, String> data = new HashMap<>();
		data.put("x", "" + locX);
		data.put("y", "" + locY);
		data.put("zoom", "" + zoom);
		data.put("iterations", "" + iter);
		data.put("power", "" + power);
		String pl = "0:" + currentPalette.name + ":" + currentPalette.colorlength + ":" + currentPalette.loop;
		for(int color : currentPalette.init) {
			pl += ":" + color;
		}
		data.put("palette", "" + pl);
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

}
