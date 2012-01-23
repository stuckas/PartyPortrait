package com.github.stuckas.partypictures;

import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

public class PartyPictures extends JFrame {
	
	static final long serialVersionUID = 1;
	
    // hard coded config
	private static boolean FULLSCREEN = true;
	
	private Image full = null;
	private Image[][] saver = new Image[5][5];
	private Random r = new Random(1);
	
	Timer saverTimer = new Timer(2000, new UpdateSaver());
	
	boolean running = false;
	

	public static void main(String[] args) throws Exception {
		new PartyPictures();
	}

	// Class constructor
	private PartyPictures() throws MalformedURLException {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addMouseListener(new ExitOnMouseClickListener());
		addKeyListener(new TakePhotoOnKeyListener());

		this.setUndecorated(true);
		this.setVisible(true);

		if (FULLSCREEN) {
			 GraphicsEnvironment.getLocalGraphicsEnvironment()
			 	.getDefaultScreenDevice().setFullScreenWindow(this);
		} else {
			this.setSize(300, 200);
		}
		
		saverTimer.start();
	}

	public void paint(Graphics g) {
		if (full != null) {
			g.drawImage(full, 0, 0, this);
		}

		if (saver != null) {
			int x=0, y=0;
			for (int i=0; i<saver.length; i++) {
				for (int j=0;j<saver[i].length; j++) {
					g.drawImage(saver[i][j], x, y, this);
					x=x+getWidth()/saver[i].length;
				}
				x=0;
				y=y+getHeight()/saver.length;
			}
		}
	}

	
	private class UpdateSaver implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			File f = new File(".");
			String[] files = f.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".JPG");
				}
			});
			
			String filename = files[r.nextInt(files.length)];
			int x = r.nextInt(saver.length);
			int y = r.nextInt(saver[x].length);
			
			Image img = Toolkit.getDefaultToolkit().getImage(filename);

			img = img.getScaledInstance(getWidth()/saver.length, getHeight()/saver[x].length, Image.SCALE_FAST);
			saver[x][y] = img;
			full = null;
			repaint();
		}
		
	}
	
	private class ExitOnMouseClickListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			System.exit(0);
		}

		public void mousePressed(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}
	}
	
	private class TakePhotoOnKeyListener implements KeyListener {
		
		public void keyPressed(KeyEvent e) {
			
			if (running)
				return;
			
			saverTimer.stop();

			new Thread(new Runnable() {
				public void run() {
					running = true;

					String filename = System.currentTimeMillis() + ".JPG";
					try {
						Process p = Runtime.getRuntime().exec(
								"gphoto2 --capture-image-and-download --filename=" + filename);
						p.waitFor();
						full = Toolkit.getDefaultToolkit().getImage(filename);
						full = full.getScaledInstance(getWidth(), -1, Image.SCALE_FAST);
					} catch (IOException ex) {
						ex.printStackTrace();
					} catch (java.lang.InterruptedException ex) {
					} finally {
						running = false;
						init(saver);
						repaint();
						saverTimer.start();
					}
				}
				
				public void init(Image[][] in) {
					for (int i=0; i<in.length; i++) {
						for (int j=0; j<in[i].length; j++) {
							in[i][j] = null;
						}
					}
				}
			}).start();
		}

		public void keyReleased(KeyEvent e) {}

		public void keyTyped(KeyEvent e) {}
	}
}
