package edu.uob.parser;

import edu.uob.action.GameAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class ActionParser {
    private LinkedHashSet<GameAction> gameActions; // use LinkedHashSet to maintain insertion order

    public ActionParser(File actionsFile) throws IOException, SAXException, ParserConfigurationException {
        this.gameActions = new LinkedHashSet<>();

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(actionsFile);
        Element root = document.getDocumentElement();
        NodeList actions = root.getChildNodes();
        // only the odd items are actually actions - 1, 3, 5 etc
        for (int i = 1; i < actions.getLength(); i += 2) {
            Element action = (Element) actions.item(i);

            Element triggersElement = (Element) action.getElementsByTagName("triggers").item(0);
            HashSet<String> triggers = this.parseActionPropertyToSet(triggersElement, "keyphrase");

            Element subjectsElement = (Element) action.getElementsByTagName("subjects").item(0);
            HashSet<String> subjects = this.parseActionPropertyToSet(subjectsElement, "entity");

            Element consumedElement = (Element) action.getElementsByTagName("consumed").item(0);
            LinkedList<String> consumed = this.parseActionPropertyToList(consumedElement, "entity");

            Element producedElement = (Element) action.getElementsByTagName("produced").item(0);
            LinkedList<String> produced = this.parseActionPropertyToList(producedElement, "entity");

            String narration = action.getElementsByTagName("narration").item(0).getTextContent();

            GameAction gameAction = new GameAction(triggers, subjects, consumed, produced, narration);
            this.gameActions.add(gameAction);
        }
    }

    /** triggers -- keyphrase
     *  subjects / consumed / produced -- entity
     */
    private HashSet<String> parseActionPropertyToSet(Element actionProperty, String childTagName) {
        HashSet<String> childrenTextContent = new HashSet<>();

        NodeList children = actionProperty.getElementsByTagName(childTagName);
        for (int i = 0; i < children.getLength(); i++) {
            String childText = Normaliser.normalizeString(children.item(i).getTextContent());
            childrenTextContent.add(childText);
        }

        return childrenTextContent;
    }

    private LinkedList<String> parseActionPropertyToList(Element actionProperty, String childTagName) {
        LinkedList<String> childrenTextContent = new LinkedList<>();

        NodeList children = actionProperty.getElementsByTagName(childTagName);
        for (int i = 0; i < children.getLength(); i++) {
            String childText = Normaliser.normalizeString(children.item(i).getTextContent());
            childrenTextContent.add(childText);
        }

        return childrenTextContent;
    }

    public HashSet<GameAction> getGameActions() {
        return this.gameActions;
    }
}
