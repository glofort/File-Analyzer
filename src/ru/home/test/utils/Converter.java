package ru.home.test.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;

public class Converter {

    private static final String RN = "\r\n";
    private static final String TAB = "\t";
    private static final String Q = "\"";
    private static final String COLON = ":";
    private static final String S = " ";
    private static final String COMMA = ",";
    private static final String SEMICOLON = ";";

    public static String convertToJsonStr(List<Pair<String, String>> dataList) {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(RN);
        int i = 0;
        for (Pair<String, String> data: dataList) {
            i += 1;
            sb.append(TAB);
            sb.append(Q).append(data.getFirst()).append(Q).append(S);
            sb.append(COLON).append(S);
            sb.append(data.getSecond());
            if (dataList.size() - i > 0) {
                sb.append(COMMA);
            }
            sb.append(RN);
        }

        sb.append("}");
        return sb.toString();
    }

    public static String convertToPlainStr(List<Pair<String, String>> dataList) {
        StringBuilder sb = new StringBuilder();
        for (Pair<String, String> data: dataList) {
            sb.append(data.getFirst()).append(COLON).append(S).append(data.getSecond()).append(SEMICOLON).append(S);
        }
        return sb.toString();
    }


    public static String convertToXmlStr(List<Pair<String, String>> dataList) {
        final StringBuilder result = new StringBuilder();
        try {
            result.append(createXml(dataList)) ;
        } catch (TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return result.toString();
    }


    private static String createXml(List<Pair<String, String>> dataList) throws ParserConfigurationException, TransformerException {
        final DocumentBuilderFactory documentBuilderFactory =DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder =documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.newDocument();
        final Element rootElement = document.createElement("STATS");
        document.appendChild(rootElement);


        for (Pair<String, String> data : dataList) {
            final Element el = document.createElement("STAT");

            final Element em = document.createElement("KEY");
            em.appendChild(document.createTextNode(data.getFirst()));
            el.appendChild(em);

            final Element em1 = document.createElement("VALUE");
            em1.appendChild(document.createTextNode(data.getSecond()));
            el.appendChild(em1);

            rootElement.appendChild(el);
        }

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        final DOMSource source = new DOMSource(document);

        final StreamResult result =  new StreamResult(new StringWriter());

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
        transformer.transform(source, result);

        return result.getWriter().toString();
    }

}
