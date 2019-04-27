/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.anest.top;

import com.anest.utils.LicenseUtils;
import com.anest.utils.VNCharacterUtils;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Shado
 */
public class App {

    private Result result;

    private boolean suspended;
    private boolean unlocking;
    private boolean peeking;

    private List<String> searchResults;
    private int resultIdx;

    static int countClick = 0;
    static Point startPoint = new Point();
    static Point endPoint = new Point();

    static String text = "";

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    static FileHandler fh;

    public App() {
        EventQueue.invokeLater(() -> {
            App.this.result = new Result();
        });
        this.suspended = true;
        this.unlocking = false;
        this.peeking = false;
    }

    public static void main(String[] args) {

        final App app = new App();
        LicenseUtils licenseUtils = new LicenseUtils();

        String license = "";
        try {
            license = licenseUtils.readFile("license.dat");
        } catch (IOException ex) {
            System.err.println("Not found license.");
            logInfo("Not found license.");
        }

        if (licenseUtils.isCheckLicense(license)) {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);

            try {
                GlobalScreen.registerNativeHook();
            } catch (NativeHookException ex) {
                System.err.println("There was a problem registering the native hook.");
                System.err.println(ex.getMessage());
                System.exit(1);
            }

            int[] codeConfig = app.getConfigCode();

            GlobalScreen.addNativeMouseListener(new NativeMouseListener() {
                @Override
                public void nativeMouseClicked(NativeMouseEvent nme) {

                    if (nme.getButton() == 2) {
                        countClick++;

                        if (countClick == 1) {
                            startPoint = nme.getPoint();
                            System.out.println("start point " + startPoint);
                        }

                        if (countClick == 2) {
                            endPoint = nme.getPoint();
                            text = app.getText(startPoint, endPoint);
                            System.out.println("end point " + endPoint);

                            try {
                                if (text.length() != 0) {
                                    text = VNCharacterUtils.removeAccent(text);

                                    final FileProcess fileProc = new FileProcess();

                                    app.result.getLblQuery().setText(text);
                                    app.searchResults = fileProc.search(text);
                                    app.resultIdx = 0;
                                    if (!app.searchResults.isEmpty()) {
                                        String searchResult = app.searchResults.get(app.resultIdx);
                                        String[] substring = searchResult.split("\\|", 2);
                                        app.result.getLblQuery().setText(text);
                                        app.result.getLblQ().setText(substring[0]);
                                        app.result.getLblA().setText(substring[1]);
                                    } else {
                                        app.result.getLblQ().setText("N/A");
                                        app.result.getLblA().setText("N/A");
                                    }
                                } else {
                                    app.result.getLblQuery().setText("N/A");
                                    app.result.getLblQ().setText("N/A");
                                    app.result.getLblA().setText("N/A");
                                }
                            } catch (IOException ex) {
                            }

                            text = "";
                            countClick = 0;
                        }
                    }

                    if (nme.getButton() == 3) {
                        app.peeking = false;
                        app.result.setVisible(false);

                        countClick = 0;

                        app.result.getLblQuery().setText("");
                        app.result.getLblQ().setText("");
                        app.result.getLblA().setText("_");

                        System.out.println("RESETED");
                    }
                    
                    if (nme.getButton() == 1) {
                        app.peeking = false;
                        app.result.setVisible(false);
                    }
                }

                @Override
                public void nativeMousePressed(NativeMouseEvent nme) {
                    if (app.suspended && nme.getButton() == 2 && MouseInfo.getPointerInfo().getLocation().getX() >= Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 3.0 && MouseInfo.getPointerInfo().getLocation().getY() >= Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 3.0) {
                        new Thread(() -> {
                            app.unlocking = true;
                            for (int i = 0; i < 30; ++i) {
                                if (app.unlocking && MouseInfo.getPointerInfo().getLocation().getX() >= Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 3.0 && MouseInfo.getPointerInfo().getLocation().getY() >= Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 3.0) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ex) {
                                    }
                                    continue;
                                }
                                return;
                            }

                            System.out.println("OPENED");
                            app.suspended = false;
                            app.result.setVisible(true);
                            app.result.getLblQ().setText("- - - - - - - - - - - - - - - - - - - - - - - - - READY - - - - - - - - - - - - - - - - - - - - - - - - - ");

                            new Thread(() -> {
                                while (!app.suspended && !app.peeking) {
                                    try {
                                        app.result.toFront();
                                        Thread.sleep(50);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Result.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }).start();
                        }).start();
                    }
                }

                @Override
                public void nativeMouseReleased(NativeMouseEvent nme) {
                    if (nme.getButton() == 1) {
                        app.unlocking = false;
                    }
                }
            });

            GlobalScreen.addNativeMouseMotionListener(new NativeMouseMotionListener() {
                @Override
                public void nativeMouseMoved(NativeMouseEvent nme) {
                    if (!app.suspended && MouseInfo.getPointerInfo().getLocation().getX() <= 15.0 && MouseInfo.getPointerInfo().getLocation().getY() >= Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 15.0) {
                        app.peeking = true;
                        app.result.setVisible(true);
                        app.result.setAlwaysOnTop(true);
                        app.result.toFront();
                    }
                }

                @Override
                public void nativeMouseDragged(NativeMouseEvent nme) {
                }
            });

            GlobalScreen.addNativeMouseWheelListener(new NativeMouseWheelListener() {
                @Override
                public void nativeMouseWheelMoved(NativeMouseWheelEvent nmwe) {
                    if (app.searchResults == null || app.searchResults.isEmpty()) {
                        return;
                    }

                    String searchResult = "";
                    if (nmwe.getWheelRotation() > 0) {
                        if (app.resultIdx < app.searchResults.size() - 1) {
                            searchResult = app.searchResults.get(++app.resultIdx);
                            String[] substring = searchResult.split("\\|", 2);
                            app.result.getLblQ().setText(substring[0]);
                            app.result.getLblA().setText(substring[1]);
                        }
                    } else if (nmwe.getWheelRotation() < 0 && app.resultIdx > 0) {
                        searchResult = app.searchResults.get(--app.resultIdx);
                        String[] substring = searchResult.split("\\|", 2);
                        app.result.getLblQ().setText(substring[0]);
                        app.result.getLblA().setText(substring[1]);
                    }
                }
            });

            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyPressed(NativeKeyEvent nke) {
                    if (!app.suspended && nke.getKeyCode() == codeConfig[0]) {
                        app.peeking = true;
                        app.result.setVisible(true);
                        app.result.toFront();
                    }
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent nke) {
                    if (!app.suspended && nke.getKeyCode() == codeConfig[0]) {
                        app.peeking = false;
                        app.result.setVisible(false);
                    }

                    if (nke.getKeyCode() == codeConfig[2]) {
                        try {
                            BufferedImage screenFullImage = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                            new File("Screenshots").mkdirs();
                            ImageIO.write(screenFullImage, "png", new File("Screenshots/" + System.currentTimeMillis() + ".png"));
                        } catch (AWTException | IOException ex) {
                        }
                    }

                    if (nke.getKeyCode() == codeConfig[3]) {
                        System.exit(0);
                    }
                }

                @Override
                public void nativeKeyTyped(NativeKeyEvent nke) {
                }
            });

        } else {
            System.err.println("Error key");
            logInfo("Error license.");
        }
    }

    private String getText(Point startPoint, Point endPoint) {
        int x;
        int y;
        int distanceX;
        int distanceY;
        String resultText = "";

        try {
            distanceX = Math.abs((int) (endPoint.getX() - startPoint.getX()));
            distanceY = Math.abs((int) (endPoint.getY() - startPoint.getY()));
            x = (int) startPoint.getX();
            y = (int) startPoint.getY();
            if ((double) x > endPoint.getX()) {
                x = (int) endPoint.getX();
            }
            if ((double) y > endPoint.getY()) {
                y = (int) endPoint.getY();
            }
        } catch (Exception e) {
            x = 0;
            y = 0;
            distanceX = 0;
            distanceY = 0;
        }

        if (distanceX != 0 && distanceY != 0) {
            try {
                BufferedImage screenFullImage = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                screenFullImage = screenFullImage.getSubimage(x, y, distanceX, distanceY);
                new File("temp").mkdirs();
                BufferedImage after = new BufferedImage(distanceX * 3, distanceY * 3, 1);
                AffineTransform at = new AffineTransform();
                at.scale(3.0, 3.0);
                AffineTransformOp scaleOp = new AffineTransformOp(at, 2);
                after = scaleOp.filter(screenFullImage, after);
                String filename = "temp/" + System.currentTimeMillis() + ".png";
                ImageIO.write(after, "png", new File(filename));
                resultText = scanfImgToText(filename);
                new File(filename).delete();
            } catch (AWTException | IOException screenFullImage) {
                resultText = "NA";
            } finally {
                startPoint = null;
                endPoint = null;
                countClick = 0;
            }
        }
        return resultText;
    }

    private static String scanfImgToText(String filename) {
        Tesseract tesseract = new Tesseract();
        //tesseract.setLanguage("vie");
        try {
            tesseract.setDatapath("tessdata");
            return tesseract.doOCR(new File(filename));
        } catch (TesseractException e) {
            e.printStackTrace(System.out);
        }
        return "NA";
    }

    private int[] getConfigCode() {
        Properties properties = new Properties();

        try {
            properties.load(new FileReader(new File("config.properties")));
        } catch (IOException ex) {
            System.err.println("Config file not found.");
            logInfo("Config file not found.");
        }

        int[] temp = {
            Integer.valueOf(properties.getProperty("hide_Show")),
            Integer.valueOf(properties.getProperty("clearText")),
            Integer.valueOf(properties.getProperty("screenshot")),
            Integer.valueOf(properties.getProperty("exit"))
        };
        return temp;
    }

    private static void logInfo(String message) {
        try {
            // This block configure the logger with handler and formatter  
            fh = new FileHandler("MyLogFile.log");
            LOGGER.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            LOGGER.info(message);           
        } catch (SecurityException | IOException e) {
        }
    }

}
