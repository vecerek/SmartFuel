package sk.codekitchen.smartfuel.util;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * @author Attila Večerek
 */
public class GPXGenerator {

	public static final String APP_DIR = "sk.codekitchen.smartfuel";
	public static final String ACTIVITIES_DIR = "driving_activities";
	public static final String PENDING_DIR = "pending_activities";

	private static final String ACTIVITY_PREFIX = "Driving Activity";
	private static final String PENDING_FILE_PREFIX = "_";
	private static final String FILENAME_DELIMITER = "_";
	private static final String EXTENSION = ".gpx";

	private static final String CREATOR = "SmartFuel App - Company name";
	private static final String GPX_VERSION = "1.1";

	private static final String XMLNS = "http://www.topografix.com/GPX/1/1";
	private static final String XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String XSI_SCHEMA_LOCATION = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd";
	private static final String GPX_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String NAME_TIME_FORMAT = "yyyy-MM-dd-HH-mm-ss";

	private Document doc;
	//TimeStamps
	private long tsStartDate;
	private long tsEndDate;
	private String createdAt;

	protected Vector<Location> locations;
	protected Context ctx;

	public GPXGenerator(Context ctx, Vector<Location> locations)
			throws ParserConfigurationException {

		this.locations = locations;
		this.ctx = ctx;

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		doc = docBuilder.newDocument();

		tsStartDate = locations.firstElement().getTime();
		tsEndDate = locations.lastElement().getTime();
	}

	public GPXGenerator(Context ctx, File activityFile)
			throws IOException, SAXException, ParserConfigurationException {

		this.ctx = ctx;
		this.locations = new Vector<>();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		doc = dBuilder.parse(activityFile);

		parseXML();
	}

	public Vector<Location> getLocations() { return locations; }

	public int getNumLocations() { return locations.size(); }

	public String getCreatedAt() { return createdAt; }

	public int getNodesNumberOf(String nodeName) {
		return doc.getElementsByTagName(nodeName).getLength();
	}

	protected void parseXML() {
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("time");
		Element eTime = (Element) nList.item(0);
		createdAt = eTime.getTextContent();

		NodeList trkPoints = doc.getElementsByTagName("trkpt");
		double lat, lon, alt;
		float speed;
		long time;
		Location location;
		for (int i = 0; i < trkPoints.getLength(); i++) {
			Node nNode = trkPoints.item(i);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				try {
					Element trkpt = (Element) nNode;
					lat = Double.parseDouble(trkpt.getAttribute("lat"));
					lon = Double.parseDouble(trkpt.getAttribute("lon"));
					alt = Double.parseDouble(
                            trkpt.getElementsByTagName("ele")
                                    .item(0).getTextContent()
                    );
                    NodeList nlSpeed = trkpt.getElementsByTagName("speed");
                    speed = nlSpeed.getLength() > 0
                            ? Float.parseFloat(nlSpeed.item(0).getTextContent())
                            : 0f;
					time = (new SimpleDateFormat(GPX_TIME_FORMAT).parse(
							trkpt.getElementsByTagName("time")
									.item(0).getTextContent()
					)).getTime();

					location = new Location("");
					location.setLatitude(lat);
					location.setLongitude(lon);
					location.setAltitude(alt);
					location.setSpeed(speed);
					location.setTime(time);

					locations.add(location);

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void createXML() {

		//root element
		Element rootElement = doc.createElement("gpx");
		doc.appendChild(rootElement);

		//root element attributes
		//creator
		Attr attr = doc.createAttribute("creator");
		attr.setValue(CREATOR);
		rootElement.setAttributeNode(attr);
		//version
		attr = doc.createAttribute("version");
		attr.setValue(GPX_VERSION);
		rootElement.setAttributeNode(attr);
		//xmlns
		attr = doc.createAttribute("xmlns");
		attr.setValue(XMLNS);
		rootElement.setAttributeNode(attr);
		//xmlns:xsi
		attr = doc.createAttribute("xmlns:xsi");
		attr.setValue(XMLNS_XSI);
		rootElement.setAttributeNode(attr);
		//xsi:schemaLocation
		attr = doc.createAttribute("xsi:schemaLocation");
		attr.setValue(XSI_SCHEMA_LOCATION);
		rootElement.setAttributeNode(attr);

		//metadata
		Element metadata = doc.createElement("metadata");
		rootElement.appendChild(metadata);
		//time
		Element time = doc.createElement("time");
		time.appendChild(doc.createTextNode(new SimpleDateFormat(GPX_TIME_FORMAT).format(tsStartDate)));
		metadata.appendChild(time);

		//trk
		Element trk = doc.createElement("trk");
		rootElement.appendChild(trk);
		//name
		Element name = doc.createElement("name");
		String sd = new SimpleDateFormat(NAME_TIME_FORMAT).format(tsStartDate);
		String ed = new SimpleDateFormat(NAME_TIME_FORMAT).format(tsEndDate);
		String activityName = ACTIVITY_PREFIX + " " + sd + " - " + ed;
		name.appendChild(doc.createTextNode(activityName));
		trk.appendChild(name);

		//trkseg
		Element trkseg = doc.createElement("trkseg");
		trk.appendChild(trkseg);

		DecimalFormat gps = new DecimalFormat("#.#######");
		DecimalFormat elev = new DecimalFormat("#.#");
		DecimalFormat speed = new DecimalFormat("#.##");
		//trkpt
		for (Location loc : locations) {
			Element trkpt = doc.createElement("trkpt");
			trkseg.appendChild(trkpt);
			//lat attribute
			Attr lat = doc.createAttribute("lat");
			lat.setValue(gps.format(loc.getLatitude()));
			trkpt.setAttributeNode(lat);
			//lon attribute
			Attr lon = doc.createAttribute("lon");
			lon.setValue(gps.format(loc.getLongitude()));
			trkpt.setAttributeNode(lon);

			//elevation
			Element ele = doc.createElement("ele");
			ele.appendChild(doc.createTextNode(elev.format(loc.getAltitude())));
			trkpt.appendChild(ele);
			//speed
			Element velocity = doc.createElement("speed");
			velocity.appendChild(doc.createTextNode(speed.format(loc.getSpeed())));
			trkpt.appendChild(velocity);
			//time
			time = doc.createElement("time");
			time.appendChild(doc.createElement(new SimpleDateFormat(GPX_TIME_FORMAT).format(loc.getTime())));
			trkpt.appendChild(time);
		}
	}

	public String getFileName() {
		String sd = new SimpleDateFormat(NAME_TIME_FORMAT).format(tsStartDate);
		String ed = new SimpleDateFormat(NAME_TIME_FORMAT).format(tsEndDate);
		String delim = FILENAME_DELIMITER;
		return sd + delim + ed + EXTENSION;
	}

	public String saveAsPendingActivity() throws IOException, TransformerException {
		File pendingActivitiesDir = new File(Environment.getDataDirectory()
				+ GPXGenerator.PENDING_DIR);

		String filename = PENDING_FILE_PREFIX;
		if (pendingActivitiesDir.exists()) {
			File[] dirFiles = pendingActivitiesDir.listFiles();
			filename += String.valueOf(dirFiles.length + 1) +
					EXTENSION;
		}

		return save(filename, PENDING_DIR);
	}

	public String save() throws IOException, TransformerException {
		return save(getFileName(), ACTIVITIES_DIR);
	}

    private String save(String filename, String directoryName)
            throws IOException, TransformerException {

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource source = new DOMSource(doc);

            File activitiesDirectory = ctx.getDir(directoryName, Context.MODE_PRIVATE);
            if (!activitiesDirectory.mkdirs()) {
                Log.e("FILE_SAVE", "Directory not created");
            }

            File activity = new File(activitiesDirectory, filename);
            /*
             * The free space should be at least 2 times bigger than the needed space
             * because just a slight difference does not make the operation sure.
             */
            if (activity.length() * 2 <= activity.getFreeSpace()) {
                FileOutputStream _stream = new FileOutputStream(activity);
                StreamResult result = new StreamResult(_stream);
                transformer.transform(source, result);

            } else {
                throw new IOException("Not enough free space.");
            }

            return activity.getAbsolutePath();

        } catch (FileNotFoundException | TransformerException e) {
            e.printStackTrace();
            Log.e("FILE_SAVE", "File not found or transformer exception");
            throw e;
        }
    }

	public String toString() {
		if (doc != null) {
			try {
				StringWriter sw = new StringWriter();
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();

				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

				transformer.transform(new DOMSource(doc), new StreamResult(sw));
				return sw.toString();

			} catch (TransformerException e) {
				e.printStackTrace();
			}

		}

		return null;
	}
}
