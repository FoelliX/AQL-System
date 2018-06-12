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
	public static String createXMLString(final Object answer) {
		if (answer instanceof Answer) {
			return createXMLString((Answer) answer);
		} else {
			return null;
		}
	}

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

	public static Answer parseXML(final String xmlString) {
		try {
			final StringReader reader = new StringReader(xmlString);

			final JAXBContext jaxbContext = JAXBContext.newInstance(Answer.class);

			final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			final Answer answer = (Answer) jaxbUnmarshaller.unmarshal(reader);
			reader.close();

			return answer;
		} catch (final JAXBException e) {
			Log.error("There has to be an error in your AQL-Answer: " + e.getMessage());
			return null;
		}
	}
}