package de.foellix.aql.datastructure.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;

public class AnswerHandler {
	/**
	 * Creates and XML String that contains the answer.
	 *
	 * @param answer
	 *            The input AQL-Answer (Java object)
	 * @return The input AQL-Answer converted into an XML String. Return null when answer is no AQL-Answer object.
	 */
	public static String createXMLString(final Object answer) {
		if (answer instanceof Answer) {
			return createXMLString((Answer) answer);
		} else {
			try {
				final JAXBContext jaxbContext = JAXBContext.newInstance(answer.getClass());
				final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");

				final OutputStream outputStream = new ByteArrayOutputStream();
				jaxbMarshaller.marshal(answer, outputStream);
				final String returnStr = outputStream.toString();
				outputStream.close();

				return returnStr;
			} catch (final JAXBException | IOException e) {
				return null;
			} catch (final Exception e) {
				return null;
			}
		}
	}

	/**
	 * Creates and XML String that contains the answer.
	 *
	 * @param answer
	 *            The input AQL-Answer (Java object)
	 * @return The input AQL-Answer converted into an XML String. Returns null on error.
	 */
	public static String createXMLString(final Answer answer) {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Answer.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");

			final OutputStream outputStream = new ByteArrayOutputStream();
			jaxbMarshaller.marshal(answer, outputStream);
			final String returnStr = outputStream.toString();
			outputStream.close();

			return returnStr;
		} catch (final JAXBException | IOException e) {
			Log.error("Something went wrong while creating the XML string.");
			return null;
		}
	}

	/**
	 * Creates and XML File that contains the answer.
	 *
	 * @param answer
	 *            The input AQL-Answer (Java object)
	 * @param xmlFile
	 *            The input AQL-Answer will be stored in this file.
	 */
	public static void createXML(final Answer answer, final File xmlFile) {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Answer.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");

			final OutputStream outputStream = new FileOutputStream(xmlFile);
			jaxbMarshaller.marshal(answer, outputStream);
			outputStream.close();
		} catch (final JAXBException | IOException e) {
			Log.error("Something went wrong while writing the following file: " + xmlFile.getAbsolutePath());
			return;
		}
	}

	/**
	 * Parsing an AQL-Answer in XML format into an AQL-Answer object.
	 *
	 * @param xmlFile
	 *            AQL-Answer to be parsed.
	 * @return AQL-Answer object. Return null on error.
	 */
	public static Answer parseXML(final File xmlFile) {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Answer.class);

			final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			final Answer answer = (Answer) jaxbUnmarshaller.unmarshal(xmlFile);

			return answer;
		} catch (final JAXBException e) {
			Log.error("Something went wrong while reading the following file: " + xmlFile.getAbsolutePath());
			return null;
		}
	}

	/**
	 * Parsing an AQL-Answer from a String into an AQL-Answer object.
	 *
	 * @param xmlString
	 *            AQL-Answer to be parsed.
	 * @return AQL-Answer object. Return null on error.
	 */
	public static Answer parseXML(final String xmlString) {
		try {
			final StringReader reader = new StringReader(xmlString);

			final JAXBContext jaxbContext = JAXBContext.newInstance(Answer.class);

			final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			final Answer answer = (Answer) jaxbUnmarshaller.unmarshal(reader);
			reader.close();

			return answer;
		} catch (final JAXBException e) {
			if (xmlString.length() < 1000) {
				Log.error("There has to be an error in your AQL-Answer: " + e.getMessage() + "\n" + xmlString);
			} else {
				Log.error("There has to be an error in your AQL-Answer: " + e.getMessage() + "\n"
						+ xmlString.substring(0, 1000));
			}
			return null;
		}
	}
}