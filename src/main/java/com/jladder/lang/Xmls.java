package com.jladder.lang;

import com.jladder.lang.ext.BiMap;
import com.jladder.lang.func.Action2;
import com.sun.xml.internal.ws.util.UtilException;
import org.springframework.util.Assert;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.*;

public class Xmls {
    /**
     * 在XML中无效的字符 正则
     */
    public static final String INVALID_REGEX = "[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]";
    /**
     * 默认的DocumentBuilderFactory实现
     */
    private static String defaultDocumentBuilderFactory = "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";
    /**
     * 是否打开命名空间支持
     */
    private static boolean namespaceAware = true;

    /**
     * 通过XPath方式读取XML节点等信息<br>
     * Xpath相关文章：https://www.ibm.com/developerworks/cn/xml/x-javaxpathapi.html
     *
     * @param expression XPath表达式
     * @param source     资源，可以是Docunent、Node节点等
     * @param returnType 返回类型，{@link javax.xml.xpath.XPathConstants}
     * @return 匹配返回类型的值
     * @since 3.2.0
     */
    public static Object getByXPath(String expression, Object source, QName returnType) {
        NamespaceContext nsContext = null;
        if (source instanceof Node) {
            nsContext = new UniversalNamespaceCache((Node) source, false);
        }
        return getByXPath(expression, source, returnType, nsContext);
    }
    public static Object getByXPath(String expression, Object source, QName returnType, NamespaceContext nsContext) {
        final XPath xPath = createXPath();
        if (null != nsContext) {
            xPath.setNamespaceContext(nsContext);
        }
        try {
            if (source instanceof InputSource) {
                return xPath.evaluate(expression, (InputSource) source, returnType);
            } else {
                return xPath.evaluate(expression, source, returnType);
            }
        } catch (XPathExpressionException e) {
            throw new UtilException(e);
        }
    }
    public static XPath createXPath() {
        return XPathFactory.newInstance().newXPath();
    }
    /**
     * 根据节点名获得第一个子节点
     *
     * @param element 节点
     * @param tagName 节点名
     * @return 节点中的值
     */
    public static String getString(Element element, String tagName) {
        Element child = getElement(element, tagName);
        return child == null ? null : child.getTextContent();
    }
    /**
     * 根据节点名获得第一个子节点
     *
     * @param element 节点
     * @param tagName 节点名
     * @return 节点
     */
    public static Element getElement(Element element, String ...tagName) {
        if(tagName==null)return null;
        NodeList nodeList = null;
        for(String tag : tagName){
            for(String t : tag.split(",")){
                nodeList = element.getElementsByTagName(t);
                if(nodeList != null && nodeList.getLength()>0)break;
            }
            if(nodeList != null && nodeList.getLength()>0)break;
        }
        if (nodeList == null || nodeList.getLength() < 1) {
            return null;
        }
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Element childEle = (Element) nodeList.item(i);
            if (childEle == null || childEle.getParentNode() == element) {
                return childEle;
            }
        }
        return null;
    }
    /**
     * 根据节点名获得子节点列表
     *
     * @param element 节点
     * @param tagName 节点名，如果节点名为空（null或blank），返回所有子节点
     * @return 节点列表
     */
    public static List<Element> getElements(Element element, String tagName) {
        final NodeList nodeList = Strings.isBlank(tagName) ? element.getChildNodes() : element.getElementsByTagName(tagName);
        return transElements(element, nodeList);
    }
    /**
     * 将NodeList转换为Element列表<br>
     * 非Element节点将被忽略
     *
     * @param parentEle 父节点，如果指定将返回此节点的所有直接子节点，null返回所有就节点
     * @param nodeList  NodeList
     * @return Element列表
     */
    public static List<Element> transElements(Element parentEle, NodeList nodeList) {
        int length = nodeList.getLength();
        final ArrayList<Element> elements = new ArrayList<>(length);
        Node node;
        Element element;
        for (int i = 0; i < length; i++) {
            node = nodeList.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                element = (Element) nodeList.item(i);
                if (parentEle == null || element.getParentNode() == parentEle) {
                    elements.add(element);
                }
            }
        }

        return elements;
    }
    /**
     * 将String类型的XML转换为XML文档
     *
     * @param xml XML字符串
     * @return XML文档
     */
    public static Document parseXml(String xml) {
        if (Strings.isBlank(xml)) {
            throw new IllegalArgumentException("XML content string is empty !");
        }
        xml = cleanInvalid(xml);
        return readXML(Strings.getReader(xml));
    }
    /**
     * 去除XML文本中的无效字符
     *
     * @param xmlContent XML文本
     * @return 当传入为null时返回null
     */
    public static String cleanInvalid(String xmlContent) {
        if (xmlContent == null) {
            return null;
        }
        return xmlContent.replaceAll(INVALID_REGEX, "");
    }

    public static Document readXML(File file) {
        Assert.notNull(file, "Xml file is null !");
        if (false == file.exists()) {
            throw new UtilException("File [{}] not a exist!", file.getAbsolutePath());
        }
        if (false == file.isFile()) {
            throw new UtilException("[{}] not a file!", file.getAbsolutePath());
        }

        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            // ignore
        }

        BufferedInputStream in = null;
        try {
            in = Files.getInputStream(file);
            return readXML(in);
        } finally {
            Streams.close(in);
        }
    }
    public static Document readXML(InputStream inputStream) throws UtilException {
        return readXML(new InputSource(inputStream));
    }

    /**
     * 读取解析XML文件
     *
     * @param reader XML流
     * @return XML文档对象
     * @throws UtilException IO异常或转换异常
     */
    public static Document readXML(Reader reader) throws UtilException {
        return readXML(new InputSource(reader));
    }
    /**
     * 读取解析XML文件<br>
     * 编码在XML中定义
     *
     * @param source {@link InputSource}
     * @return XML文档对象
     */
    public static Document readXML(InputSource source) {
        final DocumentBuilder builder = createDocumentBuilder();
        try {
            return builder.parse(source);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 创建 DocumentBuilder
     * @return DocumentBuilder
     */
    public static DocumentBuilder createDocumentBuilder() {
        DocumentBuilder builder;
        try {
            builder = createDocumentBuilderFactory().newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return builder;
    }
    public static DocumentBuilderFactory createDocumentBuilderFactory() {
        final DocumentBuilderFactory factory;
        if (Strings.isBlank(defaultDocumentBuilderFactory)) {
            factory = DocumentBuilderFactory.newInstance(defaultDocumentBuilderFactory, null);
        } else {
            factory = DocumentBuilderFactory.newInstance();
        }
        // 默认打开NamespaceAware，getElementsByTagNameNS可以使用命名空间
        factory.setNamespaceAware(namespaceAware);
        return disableXXE(factory);
    }
    /**
     * 关闭XXE，避免漏洞攻击<br>
     * see: https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J
     *
     * @param dbf DocumentBuilderFactory
     * @return DocumentBuilderFactory
     */
    private static DocumentBuilderFactory disableXXE(DocumentBuilderFactory dbf) {
        String feature;
        try {
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            feature = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(feature, true);
            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            feature = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(feature, false);
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, false);
            // Disable external DTDs as well
            feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            dbf.setFeature(feature, false);
            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            // ignore
        }
        return dbf;
    }

    public static Map<String, Object> toMap(Element element) {
        return toMap(element,null);

    }
    public static Map<String, Object> toMap(Element element, Action2<Element,Map<String, Object>> action) {
        Map<String,Object> ret = new HashMap<>();
        String tag = element.getTagName();
        String value = element.getTextContent();
        NamedNodeMap attrs = element.getAttributes();
        if(attrs!=null && attrs.getLength()>0){
            for (int i = 0; i < attrs.getLength(); i++) {
                Node node = attrs.item(i);
                ret.put(node.getNodeName(),node.getNodeValue());
            }
        }
        if(element.hasChildNodes()){
            NodeList child = element.getChildNodes();
            Map<String,List<Map<String,Object>>> ts= new HashMap<String,List<Map<String,Object>>>();
            for(int m=0;m<child.getLength();m++){
                Node node = child.item(m);
//                System.out.println(node.getNodeType());
                switch (node.getNodeType()){

                    case Node.CDATA_SECTION_NODE:
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        Map<String, Object> map = toMap((Element) node);
                        if(!ts.containsKey(node.getNodeName()))ts.put(node.getNodeName(),new ArrayList<Map<String,Object>>());
                        List<Map<String, Object>> array = ts.get(node.getNodeName());
                        array.add(map);
                        break;
                }
            }
            if(ts.size()==1){
                String key = ts.keySet().stream().findFirst().get();
                if(ts.get(key).size()>1){
                    ret.put(tag,ts.get(key));
                }else{
                    if(ts.get(key).size()==1){
                        String k = ts.get(key).get(0).keySet().stream().findFirst().get();
                        if(k.equals(key)){
                            ret.put(key, ts.get(key).get(0).get(k));
                        }else {
                            ret.put(key, ts.get(key).get(0));
                        }
                    }else{
                        ret.put(key,ts.get(key));
                    }

                }
            }else{
                ts.forEach((k,v)->{
                    if(v.size()>1)ret.put(k,v);
                    else{
                        Map<String, Object> node = v.get(0);
                        if(node.keySet().size()==1 && node.containsKey(k)){
                            ret.put(k,node.get(k));
                        }else{
                            ret.put(k,v.get(0));
                        }
                    }
                });
            }

        }
        if(ret.size()<1) ret.put(tag,value);
        if(action!=null) {
            try {
                action.invoke(element,ret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
    public static class UniversalNamespaceCache implements NamespaceContext {
        private static final String DEFAULT_NS = "DEFAULT";
        private final BiMap<String, String> prefixUri = new BiMap<>(new HashMap<>());

        /**
         * This constructor parses the document and stores all namespaces it can
         * find. If toplevelOnly is true, only namespaces in the root are used.
         *
         * @param node         source Node
         * @param toplevelOnly restriction of the search to enhance performance
         */
        public UniversalNamespaceCache(Node node, boolean toplevelOnly) {
            examineNode(node.getFirstChild(), toplevelOnly);
        }

        /**
         * A single node is read, the namespace attributes are extracted and stored.
         *
         * @param node            to examine
         * @param attributesOnly, if true no recursion happens
         */
        private void examineNode(Node node, boolean attributesOnly) {
            final NamedNodeMap attributes = node.getAttributes();
            if (null != attributes) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attribute = attributes.item(i);
                    storeAttribute(attribute);
                }
            }

            if (false == attributesOnly) {
                final NodeList childNodes = node.getChildNodes();
                if (null != childNodes) {
                    Node item;
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        item = childNodes.item(i);
                        if (item.getNodeType() == Node.ELEMENT_NODE)
                            examineNode(item, false);
                    }
                }
            }
        }

        /**
         * This method looks at an attribute and stores it, if it is a namespace
         * attribute.
         *
         * @param attribute to examine
         */
        private void storeAttribute(Node attribute) {
            if (null == attribute) {
                return;
            }
            // examine the attributes in namespace xmlns
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI())) {
                // Default namespace xmlns="uri goes here"
                if (XMLConstants.XMLNS_ATTRIBUTE.equals(attribute.getNodeName())) {
                    prefixUri.put(DEFAULT_NS, attribute.getNodeValue());
                } else {
                    // The defined prefixes are stored here
                    prefixUri.put(attribute.getLocalName(), attribute.getNodeValue());
                }
            }

        }

        /**
         * This method is called by XPath. It returns the default namespace, if the
         * prefix is null or "".
         *
         * @param prefix to search for
         * @return uri
         */
        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                return prefixUri.get(DEFAULT_NS);
            } else {
                return prefixUri.get(prefix);
            }
        }

        /**
         * This method is not needed in this context, but can be implemented in a
         * similar way.
         */
        @Override
        public String getPrefix(String namespaceURI) {
            return prefixUri.getInverse().get(namespaceURI);
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            // Not implemented
            return null;
        }

    }
}
