import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
// import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
// import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
public class solvestrata extends JPanel implements MouseListener, KeyListener {
	public static int getbeigewaitingcolor() {
		Color colorcolor = auto.getPixelColor(beigewaitingx, beigewaitingy);
		return (colorcolor.getRed() << 16) | (colorcolor.getGreen() << 8) | colorcolor.getBlue();
	}
	public static final Color DARK_BLUE = new Color(0, 0, 128);
	public static final Color MID_MAGENTA = new Color(192, 0, 192);
	public static final Color DARK_GREEN = new Color(0, 128, 0);
	public static final int basedirection = 10;
	public static final int scanw = 780;
	public static final int scanh = 610;
	// config settings
	public static int variance = 0;
	public static int varianceoffset = 5;
	public static int bigvarianceoffset = 2;
	public static float slopeleniency = 0.125f;
	public static int minpixelcount = 500;
	public static float rangescale = 0.6875f;
	public static int uniqueColorVariance = 8;
	public static int probeColors = 400;
	// command line config settings
	public static boolean drawUniqueColors = false;
	public static boolean drawGridLines = false;
	// other stuff
	public int width = 400;
	public int height = 400;
	public boolean painting = false;
	public static Robot auto;
	public static int windowx = -1;
	public static int windowy = -1;
	public static int beiger = 128;
	public static int beigeg = 128;
	public static int beigeb = 128;
	public static int beigepixel = 0;
	public static int beigeraverage = 128;
	public static int beigegaverage = 128;
	public static int beigebaverage = 128;
	public static int beigepixelaverage = 0;
	public static BufferedImage realscreen = null;
	public static BufferedImage screen = null;
	public static PointAndDirection[] holder = new PointAndDirection[65536];
	public static Point[][] vertexLists = null;
	public static int colors[] = null;
	public static Point[] centers = null;
	public static int vertexListCount = 0;
	public static int[] xysums = new int[6];
	public static int xysumcount = 0;
	public static int[] xydiffs = new int[6];
	public static int xydiffcount = 0;
	public static int xysummax = 0;
	public static int xysummin = 1000000;
	public static int xydiffmax = -1000000;
	public static int xydiffmin = 1000000;
	public static int[][] xycolors = null;
	public static int[] xysolutions = null;
	public static int[] colorsolutions = null;
	public static int[] uniqueColors = null;
	public static int uniqueColorCount = 0;
	public static int animationFrame = 0;
	public static int nextSolution = 0;
	public static int beigewaitingx = 0;
	public static int beigewaitingy = 0;
	public static int beigewaitingcolor = 0;
	public static boolean findagain = false;
	public static boolean savepuzzle = false;
	// public static boolean solveforme = false;
public static final boolean debug = false;
// public static int frame = 0;
	public static int[] directions = new int[] {
		 1,  0,
		 1,  1,
		 0,  1,
		-1,  1,
		-1,  0,
		-1, -1,
		 0, -1,
		 1, -1,
		//for easy wrapping
		 1,  0,
		 1,  1,
		 0,  1,
		-1,  1,
		-1,  0,
		-1, -1,
		 0, -1,
	};
	public static void main(String[] args) {
// try {
// BufferedImage tiny = auto.createScreenCapture(new Rectangle(959-4, 503-4, 9, 9));
// ImageIO.write(tiny, "png", new File("solvestrata_tiny.png"));
// } catch(Exception e) {}
// System.exit(0);
		try {
			auto = new Robot();
		} catch(Exception e) {
			System.out.println("The robot could not be made");
			System.exit(0);
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-uniquecolors"))
				drawUniqueColors = true;
			else if (args[i].equals("-gridlines"))
				drawGridLines = true;
			// else if (args[i].equals("-solveforme"))
				// solveforme = true;
		}
		findAllVertices();

		JFrame window = new JFrame("Strata Solver");
		solvestrata thepanel = new solvestrata();
		window.setContentPane(thepanel);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		Insets insets = window.getInsets();
		window.setSize(thepanel.width + insets.left + insets.right, thepanel.height + insets.top + insets.bottom);
		window.setLocation(windowx + 820, windowy);
		thepanel.setFocusable(true);
		thepanel.requestFocus();
		window.toFront();

		long nextsolvetime = 0;
		int lastx = -1;
		int lasty = -1;
		while (true) {
//			auto.delay(33);
			if (findagain) {
				findagain = false;
				findAllVertices();
			}
			// get the color and use this wait to pause between frames
			int color = getbeigewaitingcolor();
			if (nextSolution < xysolutions.length) {
				if (!within(color, beigewaitingcolor, variance)) {
					nextSolution++;
					findbeigewaitingxy();
					animationFrame = 1;
				// } else if (solveforme) {
					// Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
					// if (mouseLocation.x != lastx || mouseLocation.y != lasty) {
						// if (lastx == -1) {
							// lastx = mouseLocation.x;
							// lasty = mouseLocation.y;
						// } else
							// solveforme = false;
					// } else {
						// long now = System.currentTimeMillis();
						// if (now >= nextsolvetime) {
							// auto.mouseMove(beigewaitingx, beigewaitingy);
							// auto.mousePress(InputEvent.BUTTON1_MASK);
							// auto.mouseRelease(InputEvent.BUTTON1_MASK);
							// lastx = beigewaitingx;
							// lasty = beigewaitingy;
							// nextsolvetime = now + 800;
						// }
					// }
				}
			}
			if (!thepanel.painting)
			{
				thepanel.painting = true;
				thepanel.repaint();
			}
		}
	}
	public solvestrata() {
		addKeyListener(this);
		addMouseListener(this);
		setBackground(new Color(beigeraverage, beigegaverage, beigebaverage));
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// g.setColor(Color.BLACK);
		// g.setFont(new Font("Monospaced", Font.BOLD, 16));
		// g.drawString("Variance: " + variance + " (" + (variance - varianceoffset) + " + " + varianceoffset + ")", 10, 20);
		// g.drawString("Big Variance: " + (variance + bigvarianceoffset), 10, 40);
		// g.drawString("Beige-Removal Range Scale: " + rangescale, 10, 60);
		int offsetx = 200 - (xysummin + xydiffmax) / 2;
		int offsety = 50 - (xysummin - xydiffmax) / 2;
		g.drawImage(screen.getSubimage(-offsetx, -offsety, 400, 400), 0, 0, null);
// frame++;
// for (int i = (frame / 10) % vertexListCount; i >= 0; i--) {
		for (int i = vertexListCount - 1; i >= 0; i--) {
			Point[] vertexList = vertexLists[i];
			// g.setColor(i == 0 ? Color.BLUE : DARK_BLUE);
			// g.drawOval(vertexList[0].x + offsetx - 1, vertexList[0].y + offsety - 1, 2, 2);
			// for (int j = 1; j < vertexList.length; j++) {
				// Point vertex = vertexList[j];
				// g.setColor(i == 0 ? Color.BLUE : DARK_BLUE);
				// g.drawOval(vertex.x + offsetx - 1, vertex.y + offsety - 1, 2, 2);
				// Point vertex2 = vertexList[j - 1];
				// g.setColor(i == 0 ? Color.MAGENTA : MID_MAGENTA);
				// g.drawLine(vertex.x + offsetx, vertex.y + offsety, vertex2.x + offsetx, vertex2.y + offsety);
			// }
			// g.drawLine(vertexList[vertexList.length - 1].x + offsetx, vertexList[vertexList.length - 1].y + offsety, vertexList[0].x + offsetx, vertexList[0].y + offsety);
			g.setColor(new Color(colors[i]));
			g.fillPolygon(new int[] {
				vertexList[0].x + offsetx,
				vertexList[1].x + offsetx,
				vertexList[2].x + offsetx,
				vertexList[3].x + offsetx
			}, new int[] {
				vertexList[0].y + offsety,
				vertexList[1].y + offsety,
				vertexList[2].y + offsety,
				vertexList[3].y + offsety
			}, 4);
		}
		if (drawGridLines) {
			g.setColor(DARK_GREEN);
			for (int i = 0; i < xysumcount; i++) {
				int xysum = xysums[i];
				int x1 = (xysum + xydiffmin) / 2;
				int x2 = (xysum + xydiffmax) / 2;
				g.drawLine(x1 + offsetx, xysum - x1 + offsety, x2 + offsetx, xysum - x2 + offsety);
			}
			for (int i = 0; i < xydiffcount; i++) {
				int xydiff = xydiffs[i];
				int x1 = (xydiff + xysummin) / 2;
				int x2 = (xydiff + xysummax) / 2;
				g.drawLine(x1 + offsetx, x1 - xydiff + offsety, x2 + offsetx, x2 - xydiff + offsety);
			}
		}
		if (drawUniqueColors) {
			for (int i = 0; i < uniqueColorCount; i++) {
				g.setColor(new Color(uniqueColors[i]));
				g.fillRect(i * 25 + 5, 0, 20, 40);
			}
			g.setColor(Color.GRAY);
			g.fillRect(uniqueColorCount * 25 + 5, 10, 5, 20);
		}
		// for (int r = 0; r < xydiffcount; r++) {
			// for (int c = 0; c < xysumcount; c++) {
				// int color = xycolors[r][c];
				// if (color != 0) {
					// g.setColor(new Color(color));
					// g.fillRect(10 + c * 20, 110 + r * 20, 15, 15);
				// }
			// }
		// }
		for (int i = 0; i < nextSolution - 1; i++) {
			drawShape(g, i, 20, offsetx, offsety);
		}
		if (animationFrame > 0) {
			drawShape(g, nextSolution - 1, animationFrame, offsetx, offsety);
			if (animationFrame < 20)
				animationFrame++;
		}
		if (nextSolution < xysolutions.length) {
			drawShape(g, nextSolution, 0, offsetx, offsety);
		}
		// g.setColor(Color.BLACK);
		// g.drawRect(beigewaitingx + offsetx - 1, beigewaitingy + offsety - 1, 2, 2);
		painting = false;
	}
	public void drawShape(Graphics g, int solution, int animationFrame, int offsetx, int offsety) {
		int rowcol = xysolutions[solution];
		int color = colorsolutions[solution];
		g.setColor(new Color(color));
		int drawx, drawy, sign;
		if (rowcol > 0) {
			int xysum = xysummin + ((rowcol - 1) * (xysummax - xysummin) / (xysumcount - 1));
			drawx = (xysum + xydiffmin) / 2;
			drawy = xysum - drawx;
			sign = 1;
		} else {
			int xydiff = xydiffmax + ((rowcol + 1) * (xydiffmax - xydiffmin) / (xydiffcount - 1));
			drawx = (xydiff + xysummax) / 2;
			drawy = drawx - xydiff;
			sign = -1;
		}
		int ydist = (int)(((xydiffmax - xydiffmin) / (xydiffcount- 1)) * 0.28125f);
		int offsetdist = (int)(ydist * 2.5f);
		drawx -= sign * offsetdist;
		drawy += offsetdist;
		int dist = animationFrame * 20 + offsetdist;
		int drawx2 = drawx + sign * dist;
		int[] xs = new int[] {
			drawx + offsetx,
			drawx + offsetx,
			drawx2 + offsetx,
			drawx2 + offsetx
		};
		int[] ys = new int[] {
			drawy - ydist + offsety,
			drawy + ydist + offsety,
			drawy + ydist - dist + offsety,
			drawy - ydist - dist + offsety
		};
		g.fillPolygon(xs, ys, 4);
		g.setColor(new Color(((((color >> 16) & 255) / 2) << 16) | ((((color >> 8) & 255) / 2) << 8) | ((color & 255) / 2)));
		g.drawPolygon(xs, ys, 4);
	}
	public void mousePressed(MouseEvent evt) {
		if (evt.getX() < 100 && evt.getY() < 100)
			savepuzzle = true;
		requestFocus();
		findagain = true;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void findImage(BufferedImage thescreen, BufferedImage icon) {
		int width = thescreen.getWidth();
		int height = thescreen.getHeight();
		int iconwidth = icon.getWidth();
		int iconheight = icon.getHeight();
		int pixel = icon.getRGB(0, 0);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (within(pixel, thescreen.getRGB(x, y), 5)) {
					boolean good = true;
					for (int y2 = 0; y2 < iconheight; y2++) {
						for (int x2 = 0; x2 < iconwidth; x2++) {
							if (!within(icon.getRGB(x2, y2), thescreen.getRGB(x + x2, y + y2), 5)) {
								good = false;
								y2 = iconheight;
								break;
							}
						}
					}
					if (good) {
						windowx = x;
						windowy = y;
						return;
					}
				}
			}
		}
	}
	public static boolean within(int pixel1, int pixel2, int v) {
		int r1 = (pixel1 >> 16) & 255;
		int g1 = (pixel1 >> 8) & 255;
		int b1 = pixel1 & 255;
		int r2 = (pixel2 >> 16) & 255;
		int g2 = (pixel2 >> 8) & 255;
		int b2 = pixel2 & 255;
		return r1 <= r2 + v && r1 >= r2 - v && g1 <= g2 + v && g1 >= g2 - v && b1 <= b2 + v && b1 >= b2 - v;
	}
	public static void findAllVertices() {
		try {
			realscreen = auto.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			if (savepuzzle) {
				String s = JOptionPane.showInputDialog("Enter the set, wave, and puzzle numbers:");
				if (s != null) {
					String[] ss = s.split(" ");
					if (Integer.parseInt(ss[2]) < 10)
						ss[2] = "0" + ss[2];
					BufferedImage capture = realscreen.getSubimage(windowx - 1, windowy - 1, scanw + 2, scanh + 2);
					Graphics g = capture.createGraphics();
					g.setColor(Color.GRAY);
					g.drawRect(0, 0, scanw + 1, scanh + 1);
					ImageIO.write(capture, "png", new File("solvestrata_s" + ss[0] + "w" + ss[1] + "p" + ss[2] + ".png"));
					savepuzzle = false;
				}
			}

			findImage(realscreen, ImageIO.read(new File("solvestrata_icon.png")));
			if (windowx == -1) {
				System.out.println("Couldn't find Strata window");
				System.exit(0);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		int rtotal = 0,
			gtotal = 0,
			btotal = 0,
			rmin = 255,
			gmin = 255,
			bmin = 255,
			rmax = 0,
			gmax = 0,
			bmax = 0,
			total = 0;
		screen = realscreen;
		for (int y = 0; y < 128; y++) {
			for (int x = 0; x < 128; x++) {
				int pixel = screen.getRGB(windowx + x, windowy + 30 + y);
				int sr = (pixel >> 16) & 255;
				int sg = (pixel >> 8) & 255;
				int sb = pixel & 255;
				rtotal += sr;
				gtotal += sg;
				btotal += sb;
				rmin = Math.min(rmin, sr);
				gmin = Math.min(gmin, sg);
				bmin = Math.min(bmin, sb);
				rmax = Math.max(rmax, sr);
				gmax = Math.max(gmax, sg);
				bmax = Math.max(bmax, sb);
				total++;
			}
		}
		beigeraverage = rtotal / total;
		beigegaverage = gtotal / total;
		beigebaverage = btotal / total;
		beigepixelaverage = (beigeraverage << 16) | (beigegaverage << 8) | beigebaverage;

		beiger = (rmax + rmin) / 2;
		beigeg = (gmax + gmin) / 2;
		beigeb = (bmax + bmin) / 2;
		beigepixel = (beiger << 16) | (beigeg << 8) | beigeb;
		variance = Math.max(Math.max(rmax - rmin, gmax - gmin), bmax - bmin) / 2 + varianceoffset;

		for (int i = holder.length - 1; i >= 0; i--)
			holder[i] = null;
		vertexLists = new Point[256][];
		colors = new int[256];
		centers = new Point[256];
		vertexListCount = 0;
		xysummax = 0;
		xysummin = 1000000;
		xydiffmax = -1000000;
		xydiffmin = 1000000;
		xysumcount = 0;
		xydiffcount = 0;
		uniqueColorCount = 0;
		int width = realscreen.getWidth();
		int height = realscreen.getHeight();
		screen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		screen.setRGB(0, 0, width, height, realscreen.getRGB(0, 0, width, height, null, 0, width), 0, width);
		// find vertices
		for (int y = 30; y < scanh; y++) {
			for (int x = 0; x < scanw; x++) {
				// found a new pixel, what do we do with it
				int pixel = screen.getRGB(windowx + x, windowy + y);
				if (!within(pixel, beigepixel, variance)) {
					findVertices(windowx + x, windowy + y);
				}
			}
		}
		xycolors = new int[xydiffcount][xysumcount];
		xysolutions = new int[xydiffcount + xysumcount];
		colorsolutions = new int[xydiffcount + xysumcount];
		uniqueColors = new int[256];
		for (int i = 0; i < vertexListCount; i++) {
			int color = colors[i];
			boolean found = false;
			for (int j = 0; j < uniqueColors.length; j++) {
				if (uniqueColors[j] == color) {
					found = true;
					break;
				}
			}
			if (!found) {
				int totalr = (color >> 16) & 255;
				int totalg = (color >> 8) & 255;
				int totalb = color & 255;
				int colorcount = 1;
				for (int k = i + 1; k < colors.length; k++) {
					if (within(colors[k], color, uniqueColorVariance)) {
						int color2 = colors[k];
						totalr += (color2 >> 16) & 255;
						totalg += (color2 >> 8) & 255;
						totalb += color2 & 255;
						colorcount++;
					}
				}
				color = ((totalr / colorcount) << 16) | ((totalg / colorcount) << 8) | (totalb / colorcount);
				for (int k = i + 1; k < colors.length; k++) {
					if (within(colors[k], color, uniqueColorVariance))
						colors[k] = color;
				}
				uniqueColors[uniqueColorCount] = color;
				uniqueColorCount++;
			}

			Point center = centers[i];
			int xydiff = center.x - center.y;
			int xysum = center.x + center.y;
			xycolors[(int)(Math.round((float)((xydiffmax - xydiff) * (xydiffcount - 1)) / (xydiffmax - xydiffmin)))]
				[(int)(Math.round((float)((xysum - xysummin) * (xysumcount - 1)) / (xysummax - xysummin)))] = color;
		}
// if (true) return;

		int[] remainingSolutions = new int[xysolutions.length];
		int remainingSolutionCount = remainingSolutions.length;
		int emptySolutionCount = 0;
		for (int i = 0; i < xydiffcount; i++) {
			remainingSolutions[i] = -i - 1;
		}
		for (int i = xydiffcount; i < xysumcount + xydiffcount; i++) {
			remainingSolutions[i] = i - xydiffcount + 1;
		}
		boolean trylastcolor = false;
		int color = 0;
		while (remainingSolutionCount > 1) {
			boolean found = false;
			int rowcol = 0;
			int i;
			boolean foundcolor = false;
			for (i = 0; i < remainingSolutionCount; i++) {
				rowcol = remainingSolutions[i];
				if (!trylastcolor)
					color = 0;
				found = true;
				foundcolor = false;
				for (int j = 0; (rowcol > 0) ? (j < xydiffcount) : (j < xysumcount); j++) {
					int color2 = (rowcol > 0) ? xycolors[j][rowcol - 1] : xycolors[-1 - rowcol][j];
					if (color2 != 0) {
						foundcolor = true;
						if (color == 0)
							color = color2;
						else if (color != color2) {
							found = false;
							break;
						}
					}
				}
				if (found) {
					trylastcolor = true;
					break;
				}
			}
			if (!found) {
				if (trylastcolor) {
					trylastcolor = false;
					continue;
				}
				System.out.println("No solution");
				break;
			}
			remainingSolutionCount--;
			if (foundcolor) {
				xysolutions[remainingSolutionCount + emptySolutionCount] = rowcol;
				colorsolutions[remainingSolutionCount + emptySolutionCount] = findBeigeRemovedColor(color);
			} else {
				xysolutions[emptySolutionCount] = rowcol;
				colorsolutions[emptySolutionCount] = 0xE0E0E0;
				emptySolutionCount++;
			}
			remainingSolutions[i] = remainingSolutions[remainingSolutionCount];
			for (int j = 0; (rowcol > 0) ? (j < xydiffcount) : (j < xysumcount); j++) {
				if (rowcol > 0)
					xycolors[j][rowcol - 1] = 0;
				else
					xycolors[-1 - rowcol][j] = 0;
			}
		}
		xysolutions[emptySolutionCount] = remainingSolutions[0];
		colorsolutions[emptySolutionCount] = 0xE0E0E0;
		animationFrame = 0;
		nextSolution = 0;
		findbeigewaitingxy();
	};
	public static void findVertices(int startingx, int startingy) {
		int pixelCount = 0;
		int realpixelcount = 0;
		int totalr = 0,
			totalg = 0,
			totalb = 0,
			rmin = 255,
			gmin = 255,
			bmin = 255,
			rmax = 0,
			gmax = 0,
			bmax = 0,
			total = 0;

		// first things first- find out the average color and don't change it afterward
		for (int i = 0; i < probeColors;) {
			boolean positive = true;
			int windowmaxx = windowx + scanw;
			int windowmaxy = windowy + scanh;
			for (int k = startingy; i < probeColors;) {
				for (int j = positive ? startingx : startingx - 1; i < probeColors;) {
					int newpixel = screen.getRGB(j, k);
					if (within(newpixel, beigepixel, variance)) {
						// hitting a beige pixel this early means that this is definitely not what we're looking for
						if (j == startingx)
							i = probeColors;
						break;
					}
					int sr = (newpixel >> 16) & 255;
					int sg = (newpixel >> 8) & 255;
					int sb = newpixel & 255;
// if (startingx == 924 && startingy == 709)
// System.out.println((j - startingx) + ", " + (k - startingy) + ": " + sr + ", " + sg + ", " + sb);
					totalr += sr;
					totalg += sg;
					totalb += sb;
					rmin = Math.min(rmin, sr);
					gmin = Math.min(gmin, sg);
					bmin = Math.min(bmin, sb);
					rmax = Math.max(rmax, sr);
					gmax = Math.max(gmax, sg);
					bmax = Math.max(bmax, sb);
					pixelCount++;
					i++;
					if (positive) {
						j++;
						if (j >= windowmaxx) {
							i = probeColors;
							break;
						}
					} else {
						j--;
						if (j < windowx) {
							i = probeColors;
							break;
						}
					}
				}
				positive = !positive;
				if (positive) {
					k++;
					if (k >= windowmaxy) {
						i = probeColors;
						break;
					}
				}
			}
		}

		int averagepixel = ((totalr / pixelCount) << 16) | ((totalg / pixelCount) << 8) | (totalb / pixelCount);
//		int medianpixel = (((rmax + rmin) / 2) << 16) | (((gmax + gmin) / 2) << 8) | ((bmax + bmin) / 2);
		int bigvariance = Math.max(Math.max(rmax - rmin, gmax - gmin), bmax - bmin) / 2 + bigvarianceoffset;

		int slopex = 0;
		int slopey = 0;
		int slopeCount = 0;
		Stack<PointAndDirection> points = new Stack<PointAndDirection>();
		points.push(new PointAndDirection(startingx, startingy, basedirection));
		int vertexCount = 0;
		byte addvertices = 0;
final int vertextocheck = 3;
final int lowpointx = 943;
final int highpointx = 947;
final int lowpointy = 589;
final int highpointy = 593;
if (debug && vertexListCount == vertextocheck) {
System.out.println("******** New vertex list ********");
System.out.println("Average color (from " + pixelCount + " points): " + (totalr / pixelCount) + ", " + (totalg / pixelCount) + ", " + (totalb / pixelCount) +
" - Beige: " + beiger + ", " + beigeg + ", " + beigeb +
" - Variance: " + variance +
" - Big variance: " + bigvariance);
}
		while (!points.isEmpty()) {
			PointAndDirection point = points.pop();
			int pointx, pointy;
			int newpixel = screen.getRGB(pointx = point.x, pointy = point.y);
if (debug && vertexListCount == vertextocheck && pointx >= lowpointx && pointx <= highpointx && pointy >= lowpointy && pointy <= highpointy)
System.out.println("  Point " + pointx + ", " + pointy +
"- Comparing color " + ((newpixel >> 16) & 255) + ", " + ((newpixel >> 8) & 255) + ", " + (newpixel & 255) +
" to beigepixel " + ((beigepixel >> 16) & 255) + ", " + ((beigepixel >> 8) & 255) + ", " + (beigepixel & 255) +
" variance " + variance);
			if (within(newpixel, beigepixel, variance))
				continue;
			screen.setRGB(pointx, pointy, beigepixel);
			realpixelcount++;

			// averagepixel = ((totalr / pixelCount) << 16) | ((totalg / pixelCount) << 8) | (totalb / pixelCount);
			int direction = point.direction;
			int foundPixels = 0;
if (debug && vertexListCount == vertextocheck && pointx >= lowpointx && pointx <= highpointx && pointy >= lowpointy && pointy <= highpointy)
System.out.println("  Looking around " + pointx + ", " + pointy);
			for (int i = direction + 14; i >= direction; i -= 2) {
				int newx = pointx + directions[i];
				int newy = pointy + directions[i + 1];
				newpixel = screen.getRGB(newx, newy);
if (debug && vertexListCount == vertextocheck && pointx >= lowpointx && pointx <= highpointx && pointy >= lowpointy && pointy <= highpointy)
System.out.println("    Looking at " + newx + ", " + newy + ": color " + ((newpixel >> 16) & 255) + ", " + ((newpixel >> 8) & 255) + ", " + (newpixel & 255));
				if (within(newpixel, averagepixel, bigvariance)) {
if (debug && vertexListCount == vertextocheck && pointx >= lowpointx && pointx <= highpointx && pointy >= lowpointy && pointy <= highpointy)
System.out.println("      It's valid");
					// totalr += (newpixel >> 16) & 255;
					// totalg += (newpixel >> 8) & 255;
					// totalb += newpixel & 255;
					// pixelCount++;
					points.push(new PointAndDirection(newx, newy, (i + 10) % 16));
					foundPixels++;
				}
			}
			if (foundPixels < 1)
				continue;
			if (vertexCount >= 2) {
				PointAndDirection oldpoint = holder[vertexCount - 1];
				int newslopex = pointx - oldpoint.x;
				int newslopey = pointy - oldpoint.y;
				if (addvertices < 2 && newslopex <= 1 && newslopex >= -1 && newslopey <= 1 && newslopey >= -1) {
					int distfromstartx = pointx - startingx;
					int distfromstarty = pointy - startingy;
					if (distfromstartx <= 1 && distfromstartx >= -1 && distfromstarty <= 1 && distfromstarty >= -1) {
						if (addvertices == 1)
{if (debug && vertexListCount == vertextocheck)
System.out.println("Start (" + startingx + ", " + startingy + ") has been reached again (" + pointx + ", " + pointy + ")");
							addvertices = 2;
}
					} else if (addvertices == 0)
						addvertices = 1;
// if (vertexListCount == vertextocheck)
// System.out.println(pointx + ", " + pointy + ", " + newslopex + ", " + newslopey + "; final direction " + direction);
					if (newslopex == slopex && newslopey == slopey)
{if (debug && vertexListCount == vertextocheck)
System.out.println("Modifying last vertex to " + pointx + ", " + pointy + "; final direction " + points.peek().direction);
						holder[vertexCount - 1] = point;
}
					else {
if (debug && vertexListCount == vertextocheck)
System.out.println("Adding vertex " + pointx + ", " + pointy + "; final direction " + points.peek().direction);
						holder[vertexCount] = point;
						vertexCount++;
						slopex = newslopex;
						slopey = newslopey;
					}
				}
// else if (vertexListCount == vertextocheck)
// System.out.println("Skipping " + pointx + ", " + pointy);
			} else {
				holder[vertexCount] = point;
				vertexCount++;
if (debug && vertexListCount == vertextocheck)
System.out.println("Adding vertex " + pointx + ", " + pointy + "; final direction " + points.peek().direction);
				if (vertexCount == 2) {
					slopex = pointx - startingx;
					slopey = pointy - startingy;
				}
			}
		}
if (debug && realpixelcount > 100)
System.out.println("Pixels: " + realpixelcount);
		if (realpixelcount < minpixelcount)
			return;

		// remove vertices with similar slopes
		for (int i = 0; i < vertexCount - 2; i++) {
			// boolean vertical;
			float slope;
			PointAndDirection point0 = holder[i];
			PointAndDirection point1 = holder[i + 1];
// if (vertexListCount == vertextocheck)
// System.out.println("Inspecting vertices for vertices " + (i + 1) + " & " + (i + 2) + " of " + vertexCount + ": " + point0.x + ", " + point0.y + " and " + point1.x + ", " + point1.y);
			int diffx = point1.x - point0.x;
			if (diffx != 0) {
				// vertical = false;
				slope = (float)(point1.y - point0.y) / diffx;

				//Don't do slope checking for horizontal slopes
				if (slope == 0.0f)
					continue;
			} else {
				// vertical = true;
				// slope = 0;

				//Don't do slope checking for vertical slopes
				continue;
			}

// if (vertexListCount == vertextocheck) {
// if (vertical)
// System.out.println("Slope 1 is vertical");
// else
// System.out.println("Slope 1: " + slope);
// }
			boolean keepgoing = true;
			while (keepgoing) {
				keepgoing = false;
				int max = Math.min(i + 10, vertexCount);
				float slope2 = 0;
				int j = i + 2;
				for (; j < max; j++) {
					PointAndDirection point2 = holder[j];
					int diffx2 = point2.x - point1.x;
					if (diffx2 != 0) {
						// if (!vertical) {
							slope2 = (float)(point2.y - point1.y) / diffx2;
// if (vertexListCount == vertextocheck)
// System.out.println(" Slope 2: " + slope2);
							if (Math.abs(slope2 - slope) < slopeleniency && (diffx > 0) == (diffx2 > 0)) {
								keepgoing = true;
								break;
							}
						// }
					// } else if (vertical) {
// if (vertexListCount == vertextocheck)
// System.out.println(" Slope 2 is vertical");
						// keepgoing = true;
						// break;
					}
				}
				if (keepgoing) {
// if (vertexListCount == vertextocheck)
// System.out.println("Kept going on slope " + slope);
					int offset = j - i - 1;
					for (; j < vertexCount; j++) {
						holder[j - offset] = holder[j];
					}
					vertexCount -= offset;
if (debug && vertexListCount == vertextocheck) {
System.out.println("Current vertices:");
if (vertexCount > 20)
System.out.println("(Too many)");
else {
for (int q = 0; q < vertexCount; q++) {
System.out.print(holder[q].x + ", " + holder[q].y + "  ");
if (q > 0) {
if (holder[q].x > holder[q - 1].x)
System.out.print(" right " + (holder[q].x - holder[q - 1].x));
else if (holder[q].x < holder[q - 1].x)
System.out.print(" left " + (holder[q - 1].x - holder[q].x));
if (holder[q].y > holder[q - 1].y)
System.out.print(" down " + (holder[q].y - holder[q - 1].y));
else if (holder[q].y < holder[q - 1].y)
System.out.print(" up " + (holder[q - 1].y - holder[q].y));
}
System.out.println();
}
}
System.out.println("i is " + i + ", vertexCount is " + vertexCount);
}
				}
			}

		}

		// reject if it's not a diamond or if it's too small
		int top = 1000000,
			bottom = 0,
			left = 1000000,
			right = 0;
		for (int i = 0; i < vertexCount; i++) {
			PointAndDirection point = holder[i];
			top = Math.min(point.y, top);
			bottom = Math.max(point.y, bottom);
			left = Math.min(point.x, left);
			right = Math.max(point.x, right);
		}
if (debug)
System.out.println("Vertex count: " + vertexCount + " Top: " + top + " Right: " + right + " Bottom: " + bottom + " Left: " + left);
		final int dist = 8;
		if (bottom - top < dist * 2 || right - left < dist * 2)
// {System.out.println("Too small w-" + (right - left) + " h-" + (bottom - top));
			return;
// }
// if (right - left > 100) return;
		Point center = new Point((left + right) / 2, (top + bottom) / 2);
		for (int i = 0; i < vertexCount; i++) {
if (debug)
System.out.println("Xdist " + (holder[i].x - center.x) + " Ydist " + (holder[i].y - center.y));
			if (Math.abs(holder[i].x - center.x) > dist && Math.abs(holder[i].y - center.y) > dist)
				return;
		}
if (debug)
System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Vertices pass>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		int xysum = center.x + center.y;
		int j = 0;
		for (; j < xysumcount; j++) {
			if (xysum <= xysums[j] + 4 && xysum >= xysums[j] - 4)
				break;
		}
		if (j == xysumcount) {
			xysums[j] = xysum;
			xysummax = Math.max(xysum, xysummax);
			xysummin = Math.min(xysum, xysummin);
			xysumcount++;
		}
		int xydiff = center.x - center.y;
		for (j = 0; j < xydiffcount; j++) {
			if (xydiff <= xydiffs[j] + 4 && xydiff >= xydiffs[j] - 4)
				break;
		}
		if (j == xydiffcount) {
			xydiffs[j] = xydiff;
			xydiffmax = Math.max(xydiff, xydiffmax);
			xydiffmin = Math.min(xydiff, xydiffmin);
			xydiffcount++;
		}

		//int newcolor = findBeigeRemovedColor(averagepixel);

		Point[] vertexList = new Point[] {
			new Point(center.x, top),
			new Point(right, center.y),
			new Point(center.x, bottom),
			new Point(left, center.y)
		};
		// for (int i = 0; i < vertexCount; i++) {
			// vertexList[i] = holder[i];
		// }
		vertexLists[vertexListCount] = vertexList;
		centers[vertexListCount] = center;
		colors[vertexListCount] = averagepixel;//newcolor;
		vertexListCount++;
	}
	public static void findbeigewaitingxy() {
		if (nextSolution < xysolutions.length) {
			int rowcol = xysolutions[nextSolution];
			if (rowcol > 0) {
				int xysum = xysummin + ((rowcol - 1) * (xysummax - xysummin) / (xysumcount - 1));
				beigewaitingx = (xysum + xydiffmin) / 2 - 6 - (xydiffmax - xydiffmin) / (xydiffcount - 1) / 2;
				beigewaitingy = xysum - beigewaitingx;
				// while (!within(realscreen.getRGB(beigewaitingx, beigewaitingy), beigepixel, variance)) {
					// beigewaitingx++;
					// beigewaitingy--;
				// }
			} else {
				int xydiff = xydiffmax + ((rowcol + 1) * (xydiffmax - xydiffmin) / (xydiffcount - 1));
				beigewaitingx = (xydiff + xysummax) / 2 + 6 + (xysummax - xysummin) / (xysumcount - 1) / 2;
				beigewaitingy = beigewaitingx - xydiff;
				// while (!within(realscreen.getRGB(beigewaitingx, beigewaitingy), beigepixel, variance)) {
					// beigewaitingx--;
					// beigewaitingy--;
				// }
			}
		}
		beigewaitingcolor = getbeigewaitingcolor();
	};
	public static int findBeigeRemovedColor(int pixel) {
		final int beigeremovalscale = 2;
		int pr = (pixel >> 16) & 255;
		int pg = (pixel >> 8) & 255;
		int pb = pixel & 255;
		return ((pr * beigeremovalscale - beigeraverage) << 16) |
			((pg * beigeremovalscale - beigegaverage) << 8) |
			(pb * beigeremovalscale - beigebaverage);
		// int diffr = pr - beigeraverage;
		// int diffg = pg - beigegaverage;
		// int diffb = pb - beigebaverage;
		// float userange = 255.0f;
		// if (diffr != 0) {
			// float ranger = (diffr > 0) ? (float)(255 - beigeraverage) / diffr : (float)(-beigeraverage) / diffr - 0.00001f;
			// userange = ranger;
		// }
		// if (diffg != 0) {
			// float rangeg = (diffg > 0) ? (float)(255 - beigegaverage) / diffg : (float)(-beigegaverage) / diffg - 0.00001f;
			// if (rangeg < userange)
				// userange = rangeg;
		// }
		// if (diffb != 0) {
			// float rangeb = (diffb > 0) ? (float)(255 - beigebaverage) / diffb : (float)(-beigebaverage) / diffb - 0.00001f;
			// if (rangeb < userange)
				// userange = rangeb;
		// }
		// userange = Math.max(1.0f, userange * rangescale);
		// return (((int)(userange * diffr) + beigeraverage) << 16) |
			// (((int)(userange * diffg) + beigegaverage) << 8) |
			// ((int)(userange * diffb) + beigebaverage);
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	public void mouseClicked(MouseEvent evt) {}
	public void mouseReleased(MouseEvent evt) {}
	public void mouseEntered(MouseEvent evt) {}
	public void mouseExited(MouseEvent evt) {}
	public void keyTyped(KeyEvent evt) {}
	public void keyPressed(KeyEvent evt) {
		int code = evt.getKeyCode();
		boolean dorepaint = false;
		if (code == KeyEvent.VK_UP) {
			variance++;
			varianceoffset++;
			dorepaint = true;
		} else if (code == KeyEvent.VK_DOWN) {
			variance--;
			varianceoffset--;
			dorepaint = true;
		} else if (code == KeyEvent.VK_LEFT) {
			bigvarianceoffset--;
			dorepaint = true;
		} else if (code == KeyEvent.VK_RIGHT) {
			bigvarianceoffset++;
			dorepaint = true;
		} else if (code == KeyEvent.VK_A) {
			rangescale += 1.0 / 1024;
			dorepaint = true;
		} else if (code == KeyEvent.VK_S) {
			rangescale += 1.0 / 256;
			dorepaint = true;
		} else if (code == KeyEvent.VK_D) {
			rangescale += 1.0 / 64;
			dorepaint = true;
		} else if (code == KeyEvent.VK_F) {
			rangescale += 1.0 / 16;
			dorepaint = true;
		} else if (code == KeyEvent.VK_J) {
			rangescale -= 1.0 / 16;
			dorepaint = true;
		} else if (code == KeyEvent.VK_K) {
			rangescale -= 1.0 / 64;
			dorepaint = true;
		} else if (code == KeyEvent.VK_L) {
			rangescale -= 1.0 / 256;
			dorepaint = true;
		} else if (code == KeyEvent.VK_SEMICOLON) {
			rangescale -= 1.0 / 1024;
			dorepaint = true;
		}
		if (dorepaint) {
			findAllVertices();
			repaint();
		}
	}
	public void keyReleased(KeyEvent evt) {}
	public static class PointAndDirection {
		public int x;
		public int y;
		public int direction;
		public PointAndDirection(int x0, int y0, int direction0) {
			x = x0;
			y = y0;
			direction = direction0;
		}
	}
}