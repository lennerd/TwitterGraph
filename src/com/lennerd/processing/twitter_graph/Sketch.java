package com.lennerd.processing.twitter_graph;

import com.lennerd.processing.twitter_graph.status_collector.DataCollector;
import com.lennerd.processing.twitter_graph.status_collector.RequestProcessor;
import com.lennerd.processing.twitter_graph.status_collector.RequestQueue;
import de.looksgood.ani.Ani;
import org.apache.commons.cli.*;
import processing.core.PApplet;
import processing.core.PFont;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.List;

public class Sketch extends PApplet {

    public static final int ANI_DURATION = 3;
    public static final int BACKGROUND_COLOR = 255;
    public static final float LINE_OPACITY = 130F;
    public static final long SERIALIZATION_ID = 4L;

    private static final long serialVersionUID = 1L;

    private Sky sky;
    private SkyCache skyCache;
    private TimeZones timeZones;
    private InfoBox infoBox;
    private boolean createPDF = false;
    private Configuration configuration;

    public void setup() {
        this.size(800, 600);//this.displayWidth, this.displayHeight);
        this.background(Sketch.BACKGROUND_COLOR);
        this.frameRate(40);

        PFont font = this.loadFont("DIN-Regular-12.vlw");

        Ani.init(this);
        Ani.setDefaultEasing(Ani.EXPO_IN_OUT);

        this.timeZones = new TimeZones(this, font);

        this.skyCache = new SkyCache(this.dataFile("sky.data"));
        int field = Calendar.MINUTE;
        int amount = -30;

        try {
            this.sky = this.skyCache.restore(field, amount);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (this.sky == null) {
            this.sky = new Sky(field, amount);
        }

        this.infoBox = new InfoBox(this, this.sky, font);

        TwitterFactory factory = new TwitterFactory(this.getConfiguration());
        Twitter twitter = factory.getInstance();

        RequestQueue requestQueue = new RequestQueue(180);
        RequestProcessor requestProcessor = new RequestProcessor(twitter, requestQueue);
        DataCollector dataCollector = new DataCollector(this.sky, requestProcessor);

        TwitterStreamFactory streamFactory = new TwitterStreamFactory(this.getConfiguration());
        TwitterStream twitterStream = streamFactory.getInstance();
        twitterStream.addListener(dataCollector.new StreamListener());
        twitterStream.sample();

        requestProcessor.start(100);

        // Caching
        Runtime runtime = Runtime.getRuntime();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Sketch.this.skyCache.cache(Sketch.this.sky);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        runtime.addShutdownHook(thread);
    }

    public void draw() {
        List<SkyObject> removeDrawables = this.sky.getRemoveDrawable();

        if (this.createPDF) {
            this.beginRecord(PApplet.PDF, "data/sky-####.pdf");
        }

        if (removeDrawables.size() > 0) {
            for (SkyObject drawable : removeDrawables) {
                drawable.remove();
            }
            removeDrawables.clear();
        }

        this.background(Sketch.BACKGROUND_COLOR);

        this.timeZones.draw();

        try {
            this.drawList(this.sky.getCircles());
            this.drawList(this.sky.getLines());
            this.drawList(this.sky.getPoints());
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

        this.infoBox.draw();

        if (this.createPDF) {
            this.endRecord();
            this.saveFrame("data/sky-####.png");
            this.createPDF = false;
        }
    }

    private void drawList(List<SkyObject> drawables) {
        for (SkyObject drawable : drawables) {
            if (drawable.isRemoved()) {
                drawables.remove(drawable);
                continue;
            }

            drawable.draw(this);
        }
    }

    public static float calculateLongitude(PApplet sketch, GeoLocation location) {
        return PApplet.map((float) location.getLongitude(), -180, 180, 0, sketch.width);
    }

    public static float calculateLatitude(PApplet sketch, GeoLocation location) {
        return PApplet.map((float) location.getLatitude(), 90, -90, 0, sketch.height);
    }

    public void mouseMoved() {
        this.infoBox.mouseMoved(this.mouseX, this.mouseY);
    }

    public void keyPressed() {
        if (this.key == ' ') {
            this.createPDF = true;
        }
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public static void main(String args[]) {
        CommandLineParser parser = new BasicParser();
        Options options = Sketch.getOptions();
        CommandLine line;

        try {
            line = parser.parse(options, args);
        } catch(ParseException exp) {
            System.err.println(exp.getMessage());

            Sketch.printHelp(options);
            return;
        }

        if (line.hasOption('h')) {
            Sketch.printHelp(options);
            return;
        }

        Sketch sketch = new Sketch();

        // Configuration for the Twitter API
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(line.getOptionValue("k"));
        cb.setOAuthConsumerSecret(line.getOptionValue("s"));
        cb.setOAuthAccessToken(line.getOptionValue("t"));
        cb.setOAuthAccessTokenSecret(line.getOptionValue("S"));

        sketch.setConfiguration(cb.build());

        String[] leftOverArgs = new String[] { /*"--full-screen",*/ line.getOptionValue("name", Sketch.class.getName()) };
        PApplet.concat(leftOverArgs, line.getArgs());

        PApplet.runSketch(leftOverArgs, sketch);
    }

    protected static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("twitter_graph", options);
        System.exit(1);
    }

    protected static Options getOptions() {
        Options options = new Options();

        Option name = new Option("name", false, "Applet name");
        name.setArgName("name");

        Option consumerKey = new Option("k", "consumer-key", true, "Twitter OAuth Consumer Key");
        consumerKey.setArgName("key");
        consumerKey.setRequired(true);

        Option consumerSecret = new Option("s", "consumer-secret", true, "Twitter OAuth Consumer Secret");
        consumerSecret.setArgName("secret");
        consumerSecret.setRequired(true);

        Option accessToken = new Option("t", "access-token", true, "Twitter OAuth Access Token");
        accessToken.setArgName("token");
        accessToken.setRequired(true);

        Option accessTokenSecret = new Option("S", "access-token-secret", true, "Twitter OAuth Access Token Secret");
        accessTokenSecret.setArgName("secret");
        accessTokenSecret.setRequired(true);

        Option help = new Option("h", "help", false, "Display this help");

        options.addOption(help);
        options.addOption(name);
        options.addOption(consumerKey);
        options.addOption(consumerSecret);
        options.addOption(accessToken);
        options.addOption(accessTokenSecret);

        return options;
    }
}
