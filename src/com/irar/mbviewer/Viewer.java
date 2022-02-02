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
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
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
import java.nio.file.Files;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.irar.mbviewer.external.DiscordHandler;
import com.irar.mbviewer.mandelbrot.Iteration;
import com.irar.mbviewer.mandelbrot.MBHelper;
import com.irar.mbviewer.mandelbrot.OversampleIteration;
import com.irar.mbviewer.mandelbrot.TinyMBHelper;
import com.irar.mbviewer.mandelbrot.ZoomLoc;
import com.irar.mbviewer.math.Complex;
import com.irar.mbviewer.math.Complex2;
import com.irar.mbviewer.math.SizedDouble;
import com.irar.mbviewer.render.DisplayHandler;
import com.irar.mbviewer.render.FullscreenMode;
import com.irar.mbviewer.render.HistogramRenderer;
import com.irar.mbviewer.render.IterationRenderer;
import com.irar.mbviewer.render.RawRenderer;
import com.irar.mbviewer.render.ViewMode;
import com.irar.mbviewer.render.WindowedMode;
import com.irar.mbviewer.util.C2ArrayList;
import com.irar.mbviewer.util.MBInfo;
import com.irar.mbviewer.util.MBInfoGetter;
import com.irar.mbviewer.util.Palette;
import com.irar.mbviewer.util.PaletteSaveHandler;
import com.irar.mbviewer.util.ProgressMonitorFactory;
import com.irar.mbviewer.util.RandomUtil;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordEventHandlers.Builder;
import net.arikia.dev.drpc.DiscordRPC;



public class Viewer extends JPanel implements Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static JFrame window;
	public static Viewer instance = new Viewer();
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
	public static boolean hist = false;
	public static List<Palette> palettes = PaletteSaveHandler.getPaletteData();
	public static StatusBar statusBar;
	public static DisplayHandler<Integer> iterationDisplay;
	public static ViewMode view = new WindowedMode();
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
	private static boolean autoZoom;

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
		
        try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		window = new JFrame("Mandelbrot Set Viewer");
		
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());
		addMenu(window);
		window.add(instance, BorderLayout.CENTER);
		statusBar = new StatusBar();
		window.add(statusBar, BorderLayout.SOUTH);
		JPanel panel1 = new JPanel(new BorderLayout());
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
		DiscordHandler.setup();
		window.setIconImage(img);
		view.applyMode(window, instance);
		instance.start();
		
		initialized = true;
		drawFractal(info);
	}
	
	public Viewer() {
		this.setDropTarget(new DropTarget() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = -1615979291619341165L;

			public synchronized void drop(DropTargetDropEvent evt) {
		        try {
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            @SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>)
		                evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		            for (File file : droppedFiles) {
		                if(file.getName().endsWith(".iaz") && file.exists() && file.isFile()) {
		                	setFile(file);
		    				drawFractal(info);
		                	break;
		                }
		            }
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		    }
		});
	}
	
	public void setSize(int width, int height) {
		super.setSize(width, height);
		Dimension size = new Dimension(width, height);
		this.setMinimumSize(size);
		this.setMaximumSize(size);
		this.setPreferredSize(size);
	}
	
	private static void setFile(File file) {
		if(file.exists()) {
			MBInfo info1 = MBInfoGetter.getInfo(file);
			if(info1 != null && info1.wasInitialized()) {
				info = info1;
				if(iterationDisplay != null) {
					iterationDisplay.display(info.getIterations());
				}
				if(info.getPalette() == null) {
					info.setPalette(palettes.get(0));
				}
			}
		}
	}

	private static void addMenu(JFrame window) {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu viewMenu = new JMenu("View");
		JMenu paletteMenu = new JMenu("Palette");
		JMenu iterMenu = new JMenu("Iteration");
		JMenu toolsMenu = new JMenu("Tools");
		JMenu helpMenu = new JMenu("Help");
		
		JMenuItem iterationView = new JMenuItem("Iterations: " + info.getIterations());
		iterationDisplay = iter -> {
			iterationView.setText("Iterations: " + iter);
		};
		JMenuItem iterup = new JMenuItem("Double Iterations");
		JMenuItem iterdown = new JMenuItem("Half Iterations");
		iterup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				info.setIterations(Math.max(32, info.getIterations() * 2));
				info.syncPrev();
				iterationDisplay.display(info.getIterations());
				drawFractal(info);
			}
		});
		iterdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				info.setIterations(Math.max(32, info.getIterations() / 2));
				info.syncPrev();
				iterationDisplay.display(info.getIterations());
				drawFractal(info);
			}
		});
		
		
		JMenuItem openFile = new JMenuItem("Open");
		openFile.addActionListener(new OpenF());
		
		JMenuItem saveLoc = new JMenuItem("Save Location");
		JMenuItem saveImage = new JMenuItem("Save Image");
		JMenuItem saveRaw = new JMenuItem("Save Raw Image Data");
		saveLoc.addActionListener(new SaveL());
		saveImage.addActionListener(new SaveP());
		saveRaw.addActionListener(new SaveR());
		
		JCheckBoxMenuItem fullscreen = new JCheckBoxMenuItem("Fullscreen", false);
		fullscreen.addActionListener(e -> {
			if(fullscreen.isSelected()) {
				view = new FullscreenMode();
				view.applyMode(window, instance);
			}else {
				view = new WindowedMode();
				view.applyMode(window, instance);
			}
		});
		
		List<JCheckBoxMenuItem> pButtons = new ArrayList<>();
		JCheckBoxMenuItem histB = new JCheckBoxMenuItem("Histogram", false);
		histB.addActionListener((e) -> {
			if(histB.isSelected()) {
				info.setDoHist(true);
				helper.setRenderer(new HistogramRenderer());
			}else {
				info.setDoHist(false);
				helper.setRenderer(new IterationRenderer());
			}
			helper.recolor(bi, info, new ProgressMonitorFactory(statusBar));
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
					helper.recolor(bi, info, new ProgressMonitorFactory(statusBar));
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
		
		JCheckBoxMenuItem autoZoom = new JCheckBoxMenuItem("Autozoom (Experimental)");
		autoZoom.setSelected(false);
		autoZoom.addActionListener((e) -> {
			Viewer.autoZoom = !Viewer.autoZoom;
			autoZoom.setSelected(Viewer.autoZoom);
			if(helper == null || (helper != null && (helper.interrupted || helper.iterations != null))) {
				drawFractal(info);
			}
		});
		JMenuItem period = new JMenuItem("Find Minibrot");
		period.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				thread = new Thread(new Runnable(){
					@Override
					public void run() {
						SizedDouble compZoom = SizedDouble.ZERO;
						try {
							if(helper == null) {
								helper = new MBHelper();
							}
							ZoomLoc comp = helper.findMini(info, resW, resH, 1.8);
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
						}
						window.validate();
						window.pack();
					}
				});
				thread.start();
			}
		});
		
		fileMenu.add(openFile);
		fileMenu.add(saveLoc);
		fileMenu.add(saveImage);
		fileMenu.add(saveRaw);
		
		viewMenu.add(fullscreen);
		
		iterMenu.add(iterationView);
		iterMenu.add(iterup);
		iterMenu.add(iterdown);
		
		helpMenu.add(clearCache);
		
		toolsMenu.add(autoZoom);
		toolsMenu.add(period);
		
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(paletteMenu);
		menuBar.add(iterMenu);
		menuBar.add(toolsMenu);
		menuBar.add(helpMenu);
		
		window.setJMenuBar(menuBar);
	}

	private static void clearCache() {
		C2ArrayList.clearCache();
	}

	private static void addML(Viewer canvas) {
		canvas.addMouseListener(new MouseListener() {
			int x(int x) {
				double ratio = (double) WIDTH / view.getViewWidth();
				x -= view.getViewOffsetX();
				return (int) (x * ratio);
			}
			int y(int y) {
				double ratio = (double) HEIGHT / view.getViewHeight();
				y -= view.getViewOffsetY();
				return (int) (y * ratio);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == 3) {
					int x = x(e.getX()) - WIDTH / 2;
					int y = y(e.getY()) - HEIGHT / 2;
					SizedDouble fX = new SizedDouble(x).divide(WIDTH).multiply(info.getZoom());
					SizedDouble fY = new SizedDouble(y).divide(HEIGHT).multiply(info.getZoom());
					info.setX(info.getX().add(fX.multiply(4).asBigDecimal(info.getX().scale() + 4)));
					info.setY(info.getY().add(fY.multiply(4).asBigDecimal(info.getY().scale() + 4)));
					info.setZoom(info.getZoom().multiply(2));
					drawFractal(info);
				}
			}

			public void mousePressed(MouseEvent e) {
				pressedX = x(e.getX());
				pressedY = y(e.getY());
				dragX = x(e.getX());
				dragY = y(e.getY());
				if(e.getButton() == 1) {
					mousePressed = true;
				}
				if(e.getButton() == 2) {
					dragPressed = true;
				}
			}
			public void mouseReleased(MouseEvent e) {
				int relX = x(e.getX());
				int relY = y(e.getY());
				int difX = Math.abs(relX - pressedX);
				int difY = Math.abs(relY - pressedY);
				int difnX = relX - pressedX;
				int difnY = relY - pressedY;
				if(difX == 0 && difY == 0) {
					if(e.getButton() == 2) {
						dragPressed = false;
						int x = relX - WIDTH / 2;
						int y = relY - HEIGHT / 2;
						SizedDouble fX = new SizedDouble(x).divide(Math.min(WIDTH, HEIGHT)).multiply(info.getZoom());
						SizedDouble fY = new SizedDouble(y).divide(Math.min(WIDTH, HEIGHT)).multiply(info.getZoom());
						info.setX(info.getX().add(fX.multiply(4).asBigDecimal(info.getX().scale() + 4)));
						info.setY(info.getY().add(fY.multiply(4).asBigDecimal(info.getY().scale() + 4)));
						drawFractal(info);
					}
					return;
				}
				boolean xGreater = difX > difY;
				if(e.getButton() == 1) {
					int x = pressedX - WIDTH / 2;
					int y = pressedY - HEIGHT / 2;
					SizedDouble fX = new SizedDouble(x).divide(Math.min(WIDTH, HEIGHT)).multiply(info.getZoom());
					SizedDouble fY = new SizedDouble(y).divide(Math.min(WIDTH, HEIGHT)).multiply(info.getZoom());
					info.setX(info.getX().add(fX.multiply(4).asBigDecimal(info.getX().scale() + 4)));
					info.setY(info.getY().add(fY.multiply(4).asBigDecimal(info.getY().scale() + 4)));
					if(xGreater) {
						info.setZoom(info.getZoom().multiply((double) difX * 2 / WIDTH));
					}else {
						info.setZoom(info.getZoom().multiply((double) difY * 2 / HEIGHT));
					}
					mousePressed = false;
					drawFractal(info);
				}
				if(e.getButton() == 2) {
					int x = -difnX;
					int y = -difnY;
					SizedDouble fX = new SizedDouble(x).divide(WIDTH).multiply(info.getZoom());
					SizedDouble fY = new SizedDouble(y).divide(HEIGHT).multiply(info.getZoom());
					info.setX(info.getX().add(fX.multiply(4).asBigDecimal(info.getX().scale() + 4)));
					info.setY(info.getY().add(fY.multiply(4).asBigDecimal(info.getY().scale() + 4)));
					dragPressed = false;
					drawFractal(info);
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});

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
				info.syncPrev();
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
				info.syncPrev();
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
				info.syncPrev();
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
				info.syncPrev();
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
				info.syncPrev();
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
				info.syncPrev();
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
				info.syncPrev();
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
				info.syncPrev();
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
				info.syncPrev();
				drawFractal(info);
			}
		});
		
		Button custom3 = new Button("Set Oversample:");
		custom3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				info.setOversample(Integer.parseInt(field3.getText()));
				info.syncPrev();
				drawFractal(info);
			}
		});
		
		Button custom4 = new Button("Set Blur:");
		custom4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				blur = Double.parseDouble(field4.getText());
				info.syncPrev();
				drawFractal(info);
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
//		panel1.add(sp);
		panelx.add(new JLabel("Resolution:"));
		panelx.add(panel1);
		frame.add(panelx, BorderLayout.CENTER);
	}
	static Thread thread;
	protected static double blur = 0;
	protected static boolean shufflePoints = true;
	public static MBHelper helper;
	public static float zoomAnimationProgress;
	private static void drawFractal(MBInfo info) {
		DiscordHandler.createNewPresence(info);
		thread = new Thread(new Runnable(){
			@Override
			public void run() {
				if(info.getPower().x == 2 && info.getPower().y == 0) {
					helper = new MBHelper();
				}else {
					helper = new TinyMBHelper();
				}
				helper.getSet(bi, info, new ProgressMonitorFactory(statusBar), info.shouldDoHist() ? new HistogramRenderer() : new IterationRenderer());
				iterationDisplay.display(info.getIterations());
				if(autoZoom) {
					selectAndAutoZoom(info, helper);
				}
			}
		});
		if(current != null) {
			current = thread;
		}
		thread.start();
	}
	
	protected static void selectAndAutoZoom(MBInfo info, MBHelper helper) {
		if(helper == null) {
			drawFractal(info);
			return;
		}
		if(helper.iterations == null || helper.interrupted) {
			return;
		}
		OversampleIteration selected = selectGoodZoomLocationFrom(helper.iterations);
		Complex2 selectedLocation = selected.getIterations().get(0).getActualLocation();
		info.setX(selectedLocation.x);
		info.setY(selectedLocation.y);
		info.setZoom(info.getZoom().divide(2));
		drawFractal(info);
	}

	private static OversampleIteration selectGoodZoomLocationFrom(OversampleIteration[][] iterations) {
		HashMap<OversampleIteration, Integer> weightMap = getWeightMap(iterations);
		return RandomUtil.pickWeighted(weightMap);
	}

	private static HashMap<OversampleIteration, Integer> getWeightMap(OversampleIteration[][] iterations) {
		HashMap<OversampleIteration, Integer> weightMap = new HashMap<>();
		for(int x = 0; x < iterations.length; x++) {
			for(int y = 0; y < iterations[x].length; y++) {
				OversampleIteration iter = iterations[x][y];
				if(iter.getIterations().size() == 0 || x < iterations.length / 4 || x > iterations.length*3/4 || y < iterations[x].length / 4 || y > iterations[x].length*3/4) {
					weightMap.put(iter, 0);
					continue;
				}
				int comp1 = 0;
				int comp2 = 0;
				int comp3 = 0;
				int comp4 = 0;
				if(x > 0) {
					comp1 += compareIterations(iter, iterations[x-1][y]);
				}else {
					comp1 = 1;
				}
				if(y > 0) {
					comp2 += compareIterations(iter, iterations[x][y-1]);
				}else {
					comp2 = 1;
				}
				if(x < iterations.length-1) {
					comp3 += compareIterations(iter, iterations[x+1][y]);
				}else {
					comp3 = 1;
				}
				if(y < iterations[x].length-1) {
					comp4 += compareIterations(iter, iterations[x][y+1]);
				}else {
					comp4 = 1;
				}
				double comp = comp1+comp2+comp3+comp4;
				if(comp1 == 0) {
					comp /= 5;
				}
				if(comp2 == 0) {
					comp /= 5;
				}
				if(comp3 == 0) {
					comp /= 5;
				}
				if(comp4 == 0) {
					comp /= 5;
				}
				weightMap.put(iter, (int) Math.ceil(comp));
			}
		}
		return weightMap;
	}

	private static int compareIterations(OversampleIteration iter1, OversampleIteration iter2) {
		int actIter1 = getAvgIter(iter1);
		int actIter2 = 0;
		if(iter2.getIterations().size() > 0) {
			actIter2 = getAvgIter(iter2);
		}else {
			return 0;
		}
		return Math.abs(actIter1 - actIter2);
	}

	private static int getAvgIter(OversampleIteration iter) {
		int total = 0;
		int running = 0;
		for(Iteration iteration : iter.getIterations()) {
			total++;
			running += iteration.iterations;
		}
		return running / total;
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
	
	double xBase, yBase;
	double zoomDifBase;
	boolean zoomInProgress;
	BufferedImage inter;
	
	@Override
	public void paint(Graphics g2) {
		super.paint(g2);
		if(inter == null || inter.getWidth() != WIDTH || inter.getHeight() != HEIGHT) {
			inter = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		}
		Graphics g = inter.getGraphics();
		
		if(dragPressed) {
			int mX = MouseInfo.getPointerInfo().getLocation().x - this.getLocationOnScreen().x;
			int mY = MouseInfo.getPointerInfo().getLocation().y - this.getLocationOnScreen().y;
			int difX = mX - dragX;
			int difY = mY - dragY;
			g.drawImage(bi, difX, difY, WIDTH + difX, HEIGHT + difY, 0, 0, bi.getWidth(), bi.getHeight(), null);
		}else {
			if(zoomInProgress) {
				if(Viewer.zoomAnimationProgress == 0) {
					zoomInProgress = false;
				}
			}else {
				if(zoomAnimationProgress != 0) {
					zoomInProgress = true;
					SizedDouble fromCenterX = SizedDouble.parseSizedDouble(info.getX().subtract(info.getPrevX()));
					SizedDouble fromCenterY = SizedDouble.parseSizedDouble(info.getY().subtract(info.getPrevY()));
					
					xBase = (int) fromCenterX.divide(4).multiply(Math.min(WIDTH, HEIGHT)).divide(info.getPrevZoom()).asDouble();
					yBase = (int) fromCenterY.divide(4).multiply(Math.min(WIDTH, HEIGHT)).divide(info.getPrevZoom()).asDouble();
					zoomDifBase = (info.getPrevZoom().divide(info.getZoom()).asDouble());
				}
			}
			
			double zoomDif = (zoomDifBase-1) * Viewer.zoomAnimationProgress;
			
			
			double xOffBase = -(xBase * Viewer.zoomAnimationProgress);
			double yOffBase = -(yBase * Viewer.zoomAnimationProgress);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.drawImage(bi, (int) (-WIDTH * zoomDif/2 + xOffBase*zoomDifBase), (int) (-HEIGHT * zoomDif/2 + yOffBase*zoomDifBase), (int) (WIDTH + WIDTH * zoomDif/2 + xOffBase*zoomDifBase), (int) (HEIGHT + HEIGHT * zoomDif/2 + yOffBase*zoomDifBase), 0, 0, bi.getWidth(), bi.getHeight(), null);
		}
		
		if(mousePressed) {
			int mX = MouseInfo.getPointerInfo().getLocation().x - this.getLocationOnScreen().x;
			int mY = MouseInfo.getPointerInfo().getLocation().y - this.getLocationOnScreen().y;
			mX = ((mX-view.getViewOffsetX())*WIDTH/view.getViewWidth());
			mY = ((mY-view.getViewOffsetY())*HEIGHT/view.getViewHeight());
			int difX = Math.abs(mX - pressedX);
			int difY = Math.abs(mY - pressedY);
			boolean xGreater = (double) difX / WIDTH > (double) difY / HEIGHT;
			g.setColor(Color.WHITE);
			if(xGreater) {
				g.drawRect(pressedX - difX, pressedY - (difX*HEIGHT/WIDTH), (int) (difX * 2), (int) ((double) HEIGHT / WIDTH * difX * 2));
			}else {
				g.drawRect(pressedX - (difY*WIDTH/HEIGHT), pressedY - difY, (int) ((double) WIDTH / HEIGHT * difY * 2), (int) (difY * 2));
			}
		}
		
		g.dispose();
		view.draw(g2, inter);
		g2.dispose();
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
	static class SaveR implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();
			FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Raw Mandelbrot Image Data (*.rmi)", "rmi");
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
				if(c.getSelectedFile().getName().endsWith(".rmi")) {
					saveFile(c.getSelectedFile());
				}else {
					c.setSelectedFile(new File(c.getSelectedFile().getPath() + ".rmi"));
					saveFile(c.getSelectedFile());
				}
			}
			if (rVal == JFileChooser.CANCEL_OPTION) {}
		}

		private void saveFile(File selectedFile) {
			try {
				OversampleIteration[][] iterations = null;
				if(info.getPower().x == 2 && info.getPower().y == 0) {
					iterations = MBHelper.iterations;
				}else {
					iterations = TinyMBHelper.iterations;
				}
				BufferedImage raw = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
				new RawRenderer().drawIterations(iterations, raw, info);
				System.out.println(ImageIO.write(raw, "png", selectedFile));
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

	public static int getUnusableHeight() {
		return statusBar.getHeight() + window.getJMenuBar().getHeight();
	}

}
