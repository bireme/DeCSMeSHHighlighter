/*=========================================================================

    DeDup © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/DeDup/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.ddp;

import br.bireme.ngrams.NGIndex;
import br.bireme.ngrams.NGSchema;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Heitor Barbieri
 * date: 20151013
 */
public class Instances {
    private final Map<String, NGSchema> schemas;
    private final Map<String, NGIndex> indexes;
    //private final Map<String, Set<NGIndex>> databases;

    public Instances(final String workDir,
                     final String confFile) throws ParserConfigurationException,
                                                   SAXException,
                                                   IOException {
        if (confFile == null) {
            throw new NullPointerException("confFile");
        }
        schemas = new TreeMap<String, NGSchema>();
        indexes = new TreeMap<String, NGIndex>();
        //databases = new TreeMap<String, Set<NGIndex>>();

        parseConfig(workDir, confFile);
    }

    public Map<String, NGSchema> getSchemas() {
        return schemas;
    }

    public Map<String, NGIndex> getIndexes() {
        return indexes;
    }

    /*public Map<String, Set<NGIndex>> getDatabases() {
        return databases;
    }*/

    private void parseConfig(final String workDir,
                             final String confFile) throws
                                                   ParserConfigurationException,
                                                   SAXException,
                                                   IOException {
        assert confFile != null;

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final String path = getPath(workDir, confFile);
        final File file = new File(path);

        if (!file.exists()) {
            throw new IOException("missing DeDup configuration file:" + path);
        }
        final Document doc = db.parse(file);
        final Node configNode = doc.getFirstChild();
        if (! "config".equals(configNode.getNodeName())) {
            throw new IOException("missing 'config' node");
        }

        parseSchemas(workDir, confFile, configNode);
        parseIndexes(workDir, confFile, configNode);
        //parseDatabases(configNode);
    }

    private String getPath(final String workDir,
                           final String path) {
        assert path != null;

        final String tpath = path.trim();
        final String ret;

        if (workDir == null) {
            ret = tpath;
        } else if (tpath.charAt(0) == '/') {
            ret = tpath;
        } else {
            final String tworkDir = workDir.trim();
            ret = tworkDir + (tworkDir.endsWith("/") ? "" : "/") + tpath;
        }

        return ret;
    }

    private void parseSchemas(final String workDir,
                              final String confFile,
                              final Node config) throws
                                                   ParserConfigurationException,
                                                   SAXException,
                                                   IOException {
        assert config != null;

        for (Node schNode : getNodes(config, "schema")) {
            final String name = getChildContent(confFile, schNode, "name");
            if (schemas.containsKey(name)) {
                throw new IOException("duplicated schema name:" + name);
            }
            final String path = getPath(workDir,
                                    getChildContent(confFile, schNode, "path"));
            final NGSchema schema = new NGSchema(name, path,
                                getChildContent(confFile, schNode, "encoding"));
            schemas.put(name, schema);
        }
    }

    private void parseIndexes(final String workDir,
                              final String confFile,
                              final Node config) throws
                                                   ParserConfigurationException,
                                                   SAXException,
                                                   IOException {
        assert config != null;

        for (Node idxNode : getNodes(config, "index")) {
            final String name = getChildContent(confFile, idxNode, "name");
            if (indexes.containsKey(name)) {
                throw new IOException("duplicated index name:" + name);
            }
            final String path = getPath(workDir,
                                    getChildContent(confFile, idxNode, "path"));
            final NGIndex index = new NGIndex(name, path, false);
            indexes.put(name, index);
        }
    }

    private List<Node> getNodes(final Node root,
                                final String nname) {
        assert root != null;
        assert nname != null;

        final List<Node> lst = new ArrayList<Node>();
        final NodeList child = root.getChildNodes();
        final int len = child.getLength();

        for (int idx = 0; idx < len; idx++) {
            final Node node = child.item(idx);
            if (node.getNodeName().equals(nname)) {
                lst.add(node);
            }
        }

        return lst;
    }

    private String getChildContent(final String confFile,
                                   final Node root,
                                   final String childName) throws IOException {
        assert root != null;
        assert childName != null;

        final NodeList child = root.getChildNodes();
        final int len = child.getLength();
        String content = null;

        for (int idx = 0; idx < len; idx++) {
            final Node node = child.item(idx);
            if (node.getNodeName().equals(childName)) {
                content = node.getTextContent();
                break;
            }
        }
        if (content == null) {
            throw new IOException("[" + confFile + "] - missing '" + childName + 
                                                                      "' node");
        }

        return content.trim();
    }
}
